/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.level.progress.LevelLoadProgressTracker;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LevelLoadTracker
implements LevelLoadListener {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long CLIENT_WAIT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30L);
    public static final long LEVEL_LOAD_CLOSE_DELAY_MS = 500L;
    private final LevelLoadProgressTracker serverProgressTracker = new LevelLoadProgressTracker(true);
    private @Nullable ChunkLoadStatusView serverChunkStatusView;
    private volatile @Nullable LevelLoadListener.Stage serverStage;
    private @Nullable ClientState clientState;
    private final long closeDelayMs;

    public LevelLoadTracker() {
        this(0L);
    }

    public LevelLoadTracker(long l) {
        this.closeDelayMs = l;
    }

    public void setServerChunkStatusView(ChunkLoadStatusView chunkLoadStatusView) {
        this.serverChunkStatusView = chunkLoadStatusView;
    }

    public void startClientLoad(LocalPlayer localPlayer, ClientLevel clientLevel, LevelRenderer levelRenderer) {
        this.clientState = new WaitingForServer(localPlayer, clientLevel, levelRenderer, Util.getMillis() + CLIENT_WAIT_TIMEOUT_MS);
    }

    public void tickClientLoad() {
        if (this.clientState != null) {
            this.clientState = this.clientState.tick();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isLevelReady() {
        long l;
        ClientState clientState = this.clientState;
        if (!(clientState instanceof ClientLevelReady)) return false;
        ClientLevelReady clientLevelReady = (ClientLevelReady)clientState;
        try {
            long l2;
            l = l2 = clientLevelReady.readyAt();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if (Util.getMillis() < l + this.closeDelayMs) return false;
        return true;
    }

    public void loadingPacketsReceived() {
        if (this.clientState != null) {
            this.clientState = this.clientState.loadingPacketsReceived();
        }
    }

    @Override
    public void start(LevelLoadListener.Stage stage, int i) {
        this.serverProgressTracker.start(stage, i);
        this.serverStage = stage;
    }

    @Override
    public void update(LevelLoadListener.Stage stage, int i, int j) {
        this.serverProgressTracker.update(stage, i, j);
    }

    @Override
    public void finish(LevelLoadListener.Stage stage) {
        this.serverProgressTracker.finish(stage);
    }

    @Override
    public void updateFocus(ResourceKey<Level> resourceKey, ChunkPos chunkPos) {
        if (this.serverChunkStatusView != null) {
            this.serverChunkStatusView.moveTo(resourceKey, chunkPos);
        }
    }

    public @Nullable ChunkLoadStatusView statusView() {
        return this.serverChunkStatusView;
    }

    public float serverProgress() {
        return this.serverProgressTracker.get();
    }

    public boolean hasProgress() {
        return this.serverStage != null;
    }

    @Environment(value=EnvType.CLIENT)
    record WaitingForServer(LocalPlayer player, ClientLevel level, LevelRenderer levelRenderer, long timeoutAfter) implements ClientState
    {
        @Override
        public ClientState loadingPacketsReceived() {
            return new WaitingForPlayerChunk(this.player, this.level, this.levelRenderer, this.timeoutAfter);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static sealed interface ClientState
    permits WaitingForServer, WaitingForPlayerChunk, ClientLevelReady {
        default public ClientState tick() {
            return this;
        }

        default public ClientState loadingPacketsReceived() {
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ClientLevelReady(long readyAt) implements ClientState
    {
    }

    @Environment(value=EnvType.CLIENT)
    record WaitingForPlayerChunk(LocalPlayer player, ClientLevel level, LevelRenderer levelRenderer, long timeoutAfter) implements ClientState
    {
        @Override
        public ClientState tick() {
            return this.isReady() ? new ClientLevelReady(Util.getMillis()) : this;
        }

        private boolean isReady() {
            if (Util.getMillis() > this.timeoutAfter) {
                LOGGER.warn("Timed out while waiting for the client to load chunks, letting the player into the world anyway");
                return true;
            }
            BlockPos blockPos = this.player.blockPosition();
            if (this.level.isOutsideBuildHeight(blockPos.getY()) || this.player.isSpectator() || !this.player.isAlive()) {
                return true;
            }
            return this.levelRenderer.isSectionCompiledAndVisible(blockPos);
        }
    }
}

