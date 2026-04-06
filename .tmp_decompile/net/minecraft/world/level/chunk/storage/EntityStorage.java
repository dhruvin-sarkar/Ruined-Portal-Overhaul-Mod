/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

public class EntityStorage
implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final SimpleRegionStorage simpleRegionStorage;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final ConsecutiveExecutor entityDeserializerQueue;

    public EntityStorage(SimpleRegionStorage simpleRegionStorage, ServerLevel serverLevel, Executor executor) {
        this.simpleRegionStorage = simpleRegionStorage;
        this.level = serverLevel;
        this.entityDeserializerQueue = new ConsecutiveExecutor(executor, "entity-deserializer");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
        if (this.emptyChunks.contains(chunkPos.toLong())) {
            return CompletableFuture.completedFuture(EntityStorage.emptyChunk(chunkPos));
        }
        CompletableFuture<Optional<CompoundTag>> completableFuture = this.simpleRegionStorage.read(chunkPos);
        this.reportLoadFailureIfPresent(completableFuture, chunkPos);
        return completableFuture.thenApplyAsync(optional -> {
            if (optional.isEmpty()) {
                this.emptyChunks.add(chunkPos.toLong());
                return EntityStorage.emptyChunk(chunkPos);
            }
            try {
                ChunkPos chunkPos2 = (ChunkPos)((CompoundTag)optional.get()).read(POSITION_TAG, ChunkPos.CODEC).orElseThrow();
                if (!Objects.equals(chunkPos, chunkPos2)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, chunkPos2});
                    this.level.getServer().reportMisplacedChunk(chunkPos2, chunkPos, this.simpleRegionStorage.storageInfo());
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)chunkPos, (Object)exception);
                this.level.getServer().reportChunkLoadFailure(exception, this.simpleRegionStorage.storageInfo(), chunkPos);
            }
            CompoundTag compoundTag = this.simpleRegionStorage.upgradeChunkTag((CompoundTag)optional.get(), -1);
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(ChunkAccess.problemPath(chunkPos), LOGGER);){
                ValueInput valueInput = TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)this.level.registryAccess(), compoundTag);
                ValueInput.ValueInputList valueInputList = valueInput.childrenListOrEmpty(ENTITIES_TAG);
                List list = EntityType.loadEntitiesRecursive(valueInputList, this.level, EntitySpawnReason.LOAD).toList();
                ChunkEntities chunkEntities = new ChunkEntities(chunkPos, list);
                return chunkEntities;
            }
        }, this.entityDeserializerQueue::schedule);
    }

    private static ChunkEntities<Entity> emptyChunk(ChunkPos chunkPos) {
        return new ChunkEntities<Entity>(chunkPos, List.of());
    }

    @Override
    public void storeEntities(ChunkEntities<Entity> chunkEntities) {
        ChunkPos chunkPos = chunkEntities.getPos();
        if (chunkEntities.isEmpty()) {
            if (this.emptyChunks.add(chunkPos.toLong())) {
                this.reportSaveFailureIfPresent(this.simpleRegionStorage.write(chunkPos, IOWorker.STORE_EMPTY), chunkPos);
            }
            return;
        }
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(ChunkAccess.problemPath(chunkPos), LOGGER);){
            ListTag listTag = new ListTag();
            chunkEntities.getEntities().forEach(entity -> {
                TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector.forChild(entity.problemPath()), entity.registryAccess());
                if (entity.save(tagValueOutput)) {
                    CompoundTag compoundTag = tagValueOutput.buildResult();
                    listTag.add(compoundTag);
                }
            });
            CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            compoundTag.put(ENTITIES_TAG, listTag);
            compoundTag.store(POSITION_TAG, ChunkPos.CODEC, chunkPos);
            this.reportSaveFailureIfPresent(this.simpleRegionStorage.write(chunkPos, compoundTag), chunkPos);
            this.emptyChunks.remove(chunkPos.toLong());
        }
    }

    private void reportSaveFailureIfPresent(CompletableFuture<?> completableFuture, ChunkPos chunkPos) {
        completableFuture.exceptionally(throwable -> {
            LOGGER.error("Failed to store entity chunk {}", (Object)chunkPos, throwable);
            this.level.getServer().reportChunkSaveFailure((Throwable)throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
            return null;
        });
    }

    private void reportLoadFailureIfPresent(CompletableFuture<?> completableFuture, ChunkPos chunkPos) {
        completableFuture.exceptionally(throwable -> {
            LOGGER.error("Failed to load entity chunk {}", (Object)chunkPos, throwable);
            this.level.getServer().reportChunkLoadFailure((Throwable)throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
            return null;
        });
    }

    @Override
    public void flush(boolean bl) {
        this.simpleRegionStorage.synchronize(bl).join();
        this.entityDeserializerQueue.runAll();
    }

    @Override
    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }
}

