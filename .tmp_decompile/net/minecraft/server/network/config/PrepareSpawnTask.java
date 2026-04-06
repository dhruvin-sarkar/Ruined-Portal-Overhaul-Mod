/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network.config;

import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkLoadCounter;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PrepareSpawnTask
implements ConfigurationTask {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("prepare_spawn");
    public static final int PREPARE_CHUNK_RADIUS = 3;
    final MinecraftServer server;
    final NameAndId nameAndId;
    final LevelLoadListener loadListener;
    private @Nullable State state;

    public PrepareSpawnTask(MinecraftServer minecraftServer, NameAndId nameAndId) {
        this.server = minecraftServer;
        this.nameAndId = nameAndId;
        this.loadListener = minecraftServer.getLevelLoadListener();
    }

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER);){
            Optional<ValueInput> optional = this.server.getPlayerList().loadPlayerData(this.nameAndId).map(compoundTag -> TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)this.server.registryAccess(), compoundTag));
            ServerPlayer.SavedPosition savedPosition = optional.flatMap(valueInput -> valueInput.read(ServerPlayer.SavedPosition.MAP_CODEC)).orElse(ServerPlayer.SavedPosition.EMPTY);
            LevelData.RespawnData respawnData = this.server.getWorldData().overworldData().getRespawnData();
            ServerLevel serverLevel = savedPosition.dimension().map(this.server::getLevel).orElseGet(() -> {
                ServerLevel serverLevel = this.server.getLevel(respawnData.dimension());
                return serverLevel != null ? serverLevel : this.server.overworld();
            });
            CompletableFuture completableFuture = savedPosition.position().map(CompletableFuture::completedFuture).orElseGet(() -> PlayerSpawnFinder.findSpawn(serverLevel, respawnData.pos()));
            Vec2 vec2 = savedPosition.rotation().orElse(new Vec2(respawnData.yaw(), respawnData.pitch()));
            this.state = new Preparing(serverLevel, completableFuture, vec2);
        }
    }

    @Override
    public boolean tick() {
        State state = this.state;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Preparing.class, Ready.class}, (Object)state, (int)n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Preparing preparing = (Preparing)state;
                Ready ready = preparing.tick();
                if (ready != null) {
                    this.state = ready;
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                Ready ready = (Ready)state;
                yield true;
            }
            case -1 -> false;
        };
    }

    public ServerPlayer spawnPlayer(Connection connection, CommonListenerCookie commonListenerCookie) {
        State state = this.state;
        if (state instanceof Ready) {
            Ready ready = (Ready)state;
            return ready.spawn(connection, commonListenerCookie);
        }
        throw new IllegalStateException("Player spawn was not ready");
    }

    public void keepAlive() {
        State state = this.state;
        if (state instanceof Ready) {
            Ready ready = (Ready)state;
            ready.keepAlive();
        }
    }

    public void close() {
        State state = this.state;
        if (state instanceof Preparing) {
            Preparing preparing = (Preparing)state;
            preparing.cancel();
        }
        this.state = null;
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }

    final class Preparing
    implements State {
        private final ServerLevel spawnLevel;
        private final CompletableFuture<Vec3> spawnPosition;
        private final Vec2 spawnAngle;
        private @Nullable CompletableFuture<?> chunkLoadFuture;
        private final ChunkLoadCounter chunkLoadCounter = new ChunkLoadCounter();

        Preparing(ServerLevel serverLevel, CompletableFuture<Vec3> completableFuture, Vec2 vec2) {
            this.spawnLevel = serverLevel;
            this.spawnPosition = completableFuture;
            this.spawnAngle = vec2;
        }

        public void cancel() {
            this.spawnPosition.cancel(false);
        }

        public @Nullable Ready tick() {
            if (!this.spawnPosition.isDone()) {
                return null;
            }
            Vec3 vec3 = this.spawnPosition.join();
            if (this.chunkLoadFuture == null) {
                ChunkPos chunkPos = new ChunkPos(BlockPos.containing(vec3));
                this.chunkLoadCounter.track(this.spawnLevel, () -> {
                    this.chunkLoadFuture = this.spawnLevel.getChunkSource().addTicketAndLoadWithRadius(TicketType.PLAYER_SPAWN, chunkPos, 3);
                });
                PrepareSpawnTask.this.loadListener.start(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS, this.chunkLoadCounter.totalChunks());
                PrepareSpawnTask.this.loadListener.updateFocus(this.spawnLevel.dimension(), chunkPos);
            }
            PrepareSpawnTask.this.loadListener.update(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS, this.chunkLoadCounter.readyChunks(), this.chunkLoadCounter.totalChunks());
            if (!this.chunkLoadFuture.isDone()) {
                return null;
            }
            PrepareSpawnTask.this.loadListener.finish(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS);
            return new Ready(this.spawnLevel, vec3, this.spawnAngle);
        }
    }

    static sealed interface State
    permits Preparing, Ready {
    }

    final class Ready
    implements State {
        private final ServerLevel spawnLevel;
        private final Vec3 spawnPosition;
        private final Vec2 spawnAngle;

        Ready(ServerLevel serverLevel, Vec3 vec3, Vec2 vec2) {
            this.spawnLevel = serverLevel;
            this.spawnPosition = vec3;
            this.spawnAngle = vec2;
        }

        public void keepAlive() {
            this.spawnLevel.getChunkSource().addTicketWithRadius(TicketType.PLAYER_SPAWN, new ChunkPos(BlockPos.containing(this.spawnPosition)), 3);
        }

        public ServerPlayer spawn(Connection connection, CommonListenerCookie commonListenerCookie) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(this.spawnPosition));
            this.spawnLevel.waitForEntities(chunkPos, 3);
            ServerPlayer serverPlayer = new ServerPlayer(PrepareSpawnTask.this.server, this.spawnLevel, commonListenerCookie.gameProfile(), commonListenerCookie.clientInformation());
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(serverPlayer.problemPath(), LOGGER);){
                Optional<ValueInput> optional = PrepareSpawnTask.this.server.getPlayerList().loadPlayerData(PrepareSpawnTask.this.nameAndId).map(compoundTag -> TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)PrepareSpawnTask.this.server.registryAccess(), compoundTag));
                optional.ifPresent(serverPlayer::load);
                serverPlayer.snapTo(this.spawnPosition, this.spawnAngle.x, this.spawnAngle.y);
                PrepareSpawnTask.this.server.getPlayerList().placeNewPlayer(connection, serverPlayer, commonListenerCookie);
                optional.ifPresent(valueInput -> {
                    serverPlayer.loadAndSpawnEnderPearls((ValueInput)valueInput);
                    serverPlayer.loadAndSpawnParentVehicle((ValueInput)valueInput);
                });
                ServerPlayer serverPlayer2 = serverPlayer;
                return serverPlayer2;
            }
        }
    }
}

