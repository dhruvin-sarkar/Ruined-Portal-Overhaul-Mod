/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ThreadedLevelLightEngine
extends LevelLightEngine
implements AutoCloseable {
    public static final int DEFAULT_BATCH_SIZE = 1000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ConsecutiveExecutor consecutiveExecutor;
    private final ObjectList<Pair<TaskType, Runnable>> lightTasks = new ObjectArrayList();
    private final ChunkMap chunkMap;
    private final ChunkTaskDispatcher taskDispatcher;
    private final int taskPerBatch = 1000;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    public ThreadedLevelLightEngine(LightChunkGetter lightChunkGetter, ChunkMap chunkMap, boolean bl, ConsecutiveExecutor consecutiveExecutor, ChunkTaskDispatcher chunkTaskDispatcher) {
        super(lightChunkGetter, true, bl);
        this.chunkMap = chunkMap;
        this.taskDispatcher = chunkTaskDispatcher;
        this.consecutiveExecutor = consecutiveExecutor;
    }

    @Override
    public void close() {
    }

    @Override
    public int runLightUpdates() {
        throw Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.immutable();
        this.addTask(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), TaskType.PRE_UPDATE, Util.name(() -> super.checkBlock(blockPos2), () -> "checkBlock " + String.valueOf(blockPos2)));
    }

    protected void updateChunkStatus(ChunkPos chunkPos) {
        this.addTask(chunkPos.x, chunkPos.z, () -> 0, TaskType.PRE_UPDATE, Util.name(() -> {
            int i;
            super.retainData(chunkPos, false);
            super.setLightEnabled(chunkPos, false);
            for (i = this.getMinLightSection(); i < this.getMaxLightSection(); ++i) {
                super.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i), null);
                super.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i), null);
            }
            for (i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); ++i) {
                super.updateSectionStatus(SectionPos.of(chunkPos, i), true);
            }
        }, () -> "updateChunkStatus " + String.valueOf(chunkPos) + " true"));
    }

    @Override
    public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        this.addTask(sectionPos.x(), sectionPos.z(), () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.updateSectionStatus(sectionPos, bl), () -> "updateSectionStatus " + String.valueOf(sectionPos) + " " + bl));
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> super.propagateLightSources(chunkPos), () -> "propagateLight " + String.valueOf(chunkPos)));
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> super.setLightEnabled(chunkPos, bl), () -> "enableLight " + String.valueOf(chunkPos) + " " + bl));
    }

    @Override
    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
        this.addTask(sectionPos.x(), sectionPos.z(), () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.queueSectionData(lightLayer, sectionPos, dataLayer), () -> "queueData " + String.valueOf(sectionPos)));
    }

    private void addTask(int i, int j, TaskType taskType, Runnable runnable) {
        this.addTask(i, j, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(i, j)), taskType, runnable);
    }

    private void addTask(int i, int j, IntSupplier intSupplier, TaskType taskType, Runnable runnable) {
        this.taskDispatcher.submit(() -> {
            this.lightTasks.add((Object)Pair.of((Object)((Object)taskType), (Object)runnable));
            if (this.lightTasks.size() >= 1000) {
                this.runUpdate();
            }
        }, ChunkPos.asLong(i, j), intSupplier);
    }

    @Override
    public void retainData(ChunkPos chunkPos, boolean bl) {
        this.addTask(chunkPos.x, chunkPos.z, () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.retainData(chunkPos, bl), () -> "retainData " + String.valueOf(chunkPos)));
    }

    public CompletableFuture<ChunkAccess> initializeLight(ChunkAccess chunkAccess, boolean bl) {
        ChunkPos chunkPos = chunkAccess.getPos();
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> {
            LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
            for (int i = 0; i < chunkAccess.getSectionsCount(); ++i) {
                LevelChunkSection levelChunkSection = levelChunkSections[i];
                if (levelChunkSection.hasOnlyAir()) continue;
                int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                super.updateSectionStatus(SectionPos.of(chunkPos, j), false);
            }
        }, () -> "initializeLight: " + String.valueOf(chunkPos)));
        return CompletableFuture.supplyAsync(() -> {
            super.setLightEnabled(chunkPos, bl);
            super.retainData(chunkPos, false);
            return chunkAccess;
        }, runnable -> this.addTask(chunkPos.x, chunkPos.z, TaskType.POST_UPDATE, runnable));
    }

    public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunkAccess, boolean bl) {
        ChunkPos chunkPos = chunkAccess.getPos();
        chunkAccess.setLightCorrect(false);
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> {
            if (!bl) {
                super.propagateLightSources(chunkPos);
            }
            if (SharedConstants.DEBUG_VERBOSE_SERVER_EVENTS) {
                LOGGER.debug("LIT {}", (Object)chunkPos);
            }
        }, () -> "lightChunk " + String.valueOf(chunkPos) + " " + bl));
        return CompletableFuture.supplyAsync(() -> {
            chunkAccess.setLightCorrect(true);
            return chunkAccess;
        }, runnable -> this.addTask(chunkPos.x, chunkPos.z, TaskType.POST_UPDATE, runnable));
    }

    public void tryScheduleUpdate() {
        if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
            this.consecutiveExecutor.schedule(() -> {
                this.runUpdate();
                this.scheduled.set(false);
            });
        }
    }

    private void runUpdate() {
        Pair pair;
        int j;
        int i = Math.min(this.lightTasks.size(), 1000);
        ObjectListIterator objectListIterator = this.lightTasks.iterator();
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() != TaskType.PRE_UPDATE) continue;
            ((Runnable)pair.getSecond()).run();
        }
        objectListIterator.back(j);
        super.runLightUpdates();
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() == TaskType.POST_UPDATE) {
                ((Runnable)pair.getSecond()).run();
            }
            objectListIterator.remove();
        }
    }

    public CompletableFuture<?> waitForPendingTasks(int i, int j) {
        return CompletableFuture.runAsync(() -> {}, runnable -> this.addTask(i, j, TaskType.POST_UPDATE, runnable));
    }

    static enum TaskType {
        PRE_UPDATE,
        POST_UPDATE;

    }
}

