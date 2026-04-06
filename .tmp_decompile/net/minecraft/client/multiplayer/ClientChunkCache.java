/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientChunkCache
extends ChunkSource {
    static final Logger LOGGER = LogUtils.getLogger();
    private final LevelChunk emptyChunk;
    private final LevelLightEngine lightEngine;
    volatile Storage storage;
    final ClientLevel level;

    public ClientChunkCache(ClientLevel clientLevel, int i) {
        this.level = clientLevel;
        this.emptyChunk = new EmptyLevelChunk(clientLevel, new ChunkPos(0, 0), clientLevel.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
        this.lightEngine = new LevelLightEngine(this, true, clientLevel.dimensionType().hasSkyLight());
        this.storage = new Storage(ClientChunkCache.calculateStorageRange(i));
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    private static boolean isValidChunk(@Nullable LevelChunk levelChunk, int i, int j) {
        if (levelChunk == null) {
            return false;
        }
        ChunkPos chunkPos = levelChunk.getPos();
        return chunkPos.x == i && chunkPos.z == j;
    }

    public void drop(ChunkPos chunkPos) {
        if (!this.storage.inRange(chunkPos.x, chunkPos.z)) {
            return;
        }
        int i = this.storage.getIndex(chunkPos.x, chunkPos.z);
        LevelChunk levelChunk = this.storage.getChunk(i);
        if (ClientChunkCache.isValidChunk(levelChunk, chunkPos.x, chunkPos.z)) {
            this.storage.drop(i, levelChunk);
        }
    }

    @Override
    public @Nullable LevelChunk getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        LevelChunk levelChunk;
        if (this.storage.inRange(i, j) && ClientChunkCache.isValidChunk(levelChunk = this.storage.getChunk(this.storage.getIndex(i, j)), i, j)) {
            return levelChunk;
        }
        if (bl) {
            return this.emptyChunk;
        }
        return null;
    }

    @Override
    public BlockGetter getLevel() {
        return this.level;
    }

    public void replaceBiomes(int i, int j, FriendlyByteBuf friendlyByteBuf) {
        if (!this.storage.inRange(i, j)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", (Object)i, (Object)j);
            return;
        }
        int k = this.storage.getIndex(i, j);
        LevelChunk levelChunk = this.storage.chunks.get(k);
        if (!ClientChunkCache.isValidChunk(levelChunk, i, j)) {
            LOGGER.warn("Ignoring chunk since it's not present: {}, {}", (Object)i, (Object)j);
        } else {
            levelChunk.replaceBiomes(friendlyByteBuf);
        }
    }

    public @Nullable LevelChunk replaceWithPacketData(int i, int j, FriendlyByteBuf friendlyByteBuf, Map<Heightmap.Types, long[]> map, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        if (!this.storage.inRange(i, j)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", (Object)i, (Object)j);
            return null;
        }
        int k = this.storage.getIndex(i, j);
        LevelChunk levelChunk = this.storage.chunks.get(k);
        ChunkPos chunkPos = new ChunkPos(i, j);
        if (!ClientChunkCache.isValidChunk(levelChunk, i, j)) {
            levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(friendlyByteBuf, map, consumer);
            this.storage.replace(k, levelChunk);
        } else {
            levelChunk.replaceWithPacketData(friendlyByteBuf, map, consumer);
            this.storage.refreshEmptySections(levelChunk);
        }
        this.level.onChunkLoaded(chunkPos);
        return levelChunk;
    }

    @Override
    public void tick(BooleanSupplier booleanSupplier, boolean bl) {
    }

    public void updateViewCenter(int i, int j) {
        this.storage.viewCenterX = i;
        this.storage.viewCenterZ = j;
    }

    public void updateViewRadius(int i) {
        int j = this.storage.chunkRadius;
        int k = ClientChunkCache.calculateStorageRange(i);
        if (j != k) {
            Storage storage = new Storage(k);
            storage.viewCenterX = this.storage.viewCenterX;
            storage.viewCenterZ = this.storage.viewCenterZ;
            for (int l = 0; l < this.storage.chunks.length(); ++l) {
                LevelChunk levelChunk = this.storage.chunks.get(l);
                if (levelChunk == null) continue;
                ChunkPos chunkPos = levelChunk.getPos();
                if (!storage.inRange(chunkPos.x, chunkPos.z)) continue;
                storage.replace(storage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
            }
            this.storage = storage;
        }
    }

    private static int calculateStorageRange(int i) {
        return Math.max(2, i) + 3;
    }

    @Override
    public String gatherStats() {
        return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
    }

    @Override
    public int getLoadedChunksCount() {
        return this.storage.chunkCount;
    }

    @Override
    public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
        Minecraft.getInstance().levelRenderer.setSectionDirty(sectionPos.x(), sectionPos.y(), sectionPos.z());
    }

    public LongOpenHashSet getLoadedEmptySections() {
        return this.storage.loadedEmptySections;
    }

    @Override
    public void onSectionEmptinessChanged(int i, int j, int k, boolean bl) {
        this.storage.onSectionEmptinessChanged(i, j, k, bl);
    }

    @Override
    public /* synthetic */ @Nullable ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        return this.getChunk(i, j, chunkStatus, bl);
    }

    @Environment(value=EnvType.CLIENT)
    final class Storage {
        final AtomicReferenceArray<@Nullable LevelChunk> chunks;
        final LongOpenHashSet loadedEmptySections = new LongOpenHashSet();
        final int chunkRadius;
        private final int viewRange;
        volatile int viewCenterX;
        volatile int viewCenterZ;
        int chunkCount;

        Storage(int i) {
            this.chunkRadius = i;
            this.viewRange = i * 2 + 1;
            this.chunks = new AtomicReferenceArray(this.viewRange * this.viewRange);
        }

        int getIndex(int i, int j) {
            return Math.floorMod(j, this.viewRange) * this.viewRange + Math.floorMod(i, this.viewRange);
        }

        void replace(int i, @Nullable LevelChunk levelChunk) {
            LevelChunk levelChunk2 = this.chunks.getAndSet(i, levelChunk);
            if (levelChunk2 != null) {
                --this.chunkCount;
                this.dropEmptySections(levelChunk2);
                ClientChunkCache.this.level.unload(levelChunk2);
            }
            if (levelChunk != null) {
                ++this.chunkCount;
                this.addEmptySections(levelChunk);
            }
        }

        void drop(int i, LevelChunk levelChunk) {
            if (this.chunks.compareAndSet(i, levelChunk, null)) {
                --this.chunkCount;
                this.dropEmptySections(levelChunk);
            }
            ClientChunkCache.this.level.unload(levelChunk);
        }

        public void onSectionEmptinessChanged(int i, int j, int k, boolean bl) {
            if (!this.inRange(i, k)) {
                return;
            }
            long l = SectionPos.asLong(i, j, k);
            if (bl) {
                this.loadedEmptySections.add(l);
            } else if (this.loadedEmptySections.remove(l)) {
                ClientChunkCache.this.level.onSectionBecomingNonEmpty(l);
            }
        }

        private void dropEmptySections(LevelChunk levelChunk) {
            LevelChunkSection[] levelChunkSections = levelChunk.getSections();
            for (int i = 0; i < levelChunkSections.length; ++i) {
                ChunkPos chunkPos = levelChunk.getPos();
                this.loadedEmptySections.remove(SectionPos.asLong(chunkPos.x, levelChunk.getSectionYFromSectionIndex(i), chunkPos.z));
            }
        }

        private void addEmptySections(LevelChunk levelChunk) {
            LevelChunkSection[] levelChunkSections = levelChunk.getSections();
            for (int i = 0; i < levelChunkSections.length; ++i) {
                LevelChunkSection levelChunkSection = levelChunkSections[i];
                if (!levelChunkSection.hasOnlyAir()) continue;
                ChunkPos chunkPos = levelChunk.getPos();
                this.loadedEmptySections.add(SectionPos.asLong(chunkPos.x, levelChunk.getSectionYFromSectionIndex(i), chunkPos.z));
            }
        }

        void refreshEmptySections(LevelChunk levelChunk) {
            ChunkPos chunkPos = levelChunk.getPos();
            LevelChunkSection[] levelChunkSections = levelChunk.getSections();
            for (int i = 0; i < levelChunkSections.length; ++i) {
                LevelChunkSection levelChunkSection = levelChunkSections[i];
                long l = SectionPos.asLong(chunkPos.x, levelChunk.getSectionYFromSectionIndex(i), chunkPos.z);
                if (levelChunkSection.hasOnlyAir()) {
                    this.loadedEmptySections.add(l);
                    continue;
                }
                if (!this.loadedEmptySections.remove(l)) continue;
                ClientChunkCache.this.level.onSectionBecomingNonEmpty(l);
            }
        }

        boolean inRange(int i, int j) {
            return Math.abs(i - this.viewCenterX) <= this.chunkRadius && Math.abs(j - this.viewCenterZ) <= this.chunkRadius;
        }

        protected @Nullable LevelChunk getChunk(int i) {
            return this.chunks.get(i);
        }

        private void dumpChunks(String string) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(string);){
                int i = ClientChunkCache.this.storage.chunkRadius;
                for (int j = this.viewCenterZ - i; j <= this.viewCenterZ + i; ++j) {
                    for (int k = this.viewCenterX - i; k <= this.viewCenterX + i; ++k) {
                        LevelChunk levelChunk = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(k, j));
                        if (levelChunk == null) continue;
                        ChunkPos chunkPos = levelChunk.getPos();
                        fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + levelChunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to dump chunks to file {}", (Object)string, (Object)iOException);
            }
        }
    }
}

