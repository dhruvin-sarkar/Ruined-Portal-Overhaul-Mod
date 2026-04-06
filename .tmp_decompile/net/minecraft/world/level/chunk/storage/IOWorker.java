/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  java.util.SequencedMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class IOWorker
implements ChunkScanAccess,
AutoCloseable {
    public static final Supplier<CompoundTag> STORE_EMPTY = () -> null;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final PriorityConsecutiveExecutor consecutiveExecutor;
    private final RegionFileStorage storage;
    private final SequencedMap<ChunkPos, PendingStore> pendingWrites = new LinkedHashMap();
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap();
    private static final int REGION_CACHE_SIZE = 1024;

    protected IOWorker(RegionStorageInfo regionStorageInfo, Path path, boolean bl) {
        this.storage = new RegionFileStorage(regionStorageInfo, path, bl);
        this.consecutiveExecutor = new PriorityConsecutiveExecutor(Priority.values().length, (Executor)Util.ioPool(), "IOWorker-" + regionStorageInfo.type());
    }

    public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
        ChunkPos chunkPos2 = new ChunkPos(chunkPos.x - i, chunkPos.z - i);
        ChunkPos chunkPos3 = new ChunkPos(chunkPos.x + i, chunkPos.z + i);
        for (int j = chunkPos2.getRegionX(); j <= chunkPos3.getRegionX(); ++j) {
            for (int k = chunkPos2.getRegionZ(); k <= chunkPos3.getRegionZ(); ++k) {
                BitSet bitSet = this.getOrCreateOldDataForRegion(j, k).join();
                if (bitSet.isEmpty()) continue;
                ChunkPos chunkPos4 = ChunkPos.minFromRegion(j, k);
                int l = Math.max(chunkPos2.x - chunkPos4.x, 0);
                int m = Math.max(chunkPos2.z - chunkPos4.z, 0);
                int n = Math.min(chunkPos3.x - chunkPos4.x, 31);
                int o = Math.min(chunkPos3.z - chunkPos4.z, 31);
                for (int p = l; p <= n; ++p) {
                    for (int q = m; q <= o; ++q) {
                        int r = q * 32 + p;
                        if (!bitSet.get(r)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> long2ObjectLinkedOpenHashMap = this.regionCacheForBlender;
        synchronized (long2ObjectLinkedOpenHashMap) {
            CompletableFuture<BitSet> completableFuture = (CompletableFuture<BitSet>)this.regionCacheForBlender.getAndMoveToFirst(l);
            if (completableFuture == null) {
                completableFuture = this.createOldDataForRegion(i, j);
                this.regionCacheForBlender.putAndMoveToFirst(l, completableFuture);
                if (this.regionCacheForBlender.size() > 1024) {
                    this.regionCacheForBlender.removeLast();
                }
            }
            return completableFuture;
        }
    }

    private CompletableFuture<BitSet> createOldDataForRegion(int i, int j) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos chunkPos2 = ChunkPos.minFromRegion(i, j);
            ChunkPos chunkPos22 = ChunkPos.maxFromRegion(i, j);
            BitSet bitSet = new BitSet();
            ChunkPos.rangeClosed(chunkPos2, chunkPos22).forEach(chunkPos -> {
                CompoundTag compoundTag;
                CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));
                try {
                    this.scanChunk((ChunkPos)chunkPos, collectFields).join();
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to scan chunk {}", chunkPos, (Object)exception);
                    return;
                }
                Tag tag = collectFields.getResult();
                if (tag instanceof CompoundTag && this.isOldChunk(compoundTag = (CompoundTag)tag)) {
                    int i = chunkPos.getRegionLocalZ() * 32 + chunkPos.getRegionLocalX();
                    bitSet.set(i);
                }
            });
            return bitSet;
        }, Util.backgroundExecutor());
    }

    private boolean isOldChunk(CompoundTag compoundTag) {
        if (compoundTag.getIntOr("DataVersion", 0) < 4295) {
            return true;
        }
        return compoundTag.getCompound("blending_data").isPresent();
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, CompoundTag compoundTag) {
        return this.store(chunkPos, () -> compoundTag);
    }

    public CompletableFuture<Void> store(ChunkPos chunkPos, Supplier<CompoundTag> supplier) {
        return this.submitTask(() -> {
            CompoundTag compoundTag = (CompoundTag)supplier.get();
            PendingStore pendingStore = (PendingStore)this.pendingWrites.computeIfAbsent((Object)chunkPos, chunkPos -> new PendingStore(compoundTag));
            pendingStore.data = compoundTag;
            return pendingStore.result;
        }).thenCompose(Function.identity());
    }

    public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos chunkPos) {
        return this.submitThrowingTask(() -> {
            PendingStore pendingStore = (PendingStore)this.pendingWrites.get((Object)chunkPos);
            if (pendingStore != null) {
                return Optional.ofNullable(pendingStore.copyData());
            }
            try {
                CompoundTag compoundTag = this.storage.read(chunkPos);
                return Optional.ofNullable(compoundTag);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
                throw exception;
            }
        });
    }

    public CompletableFuture<Void> synchronize(boolean bl) {
        CompletionStage completableFuture = this.submitTask(() -> CompletableFuture.allOf((CompletableFuture[])this.pendingWrites.values().stream().map(pendingStore -> pendingStore.result).toArray(CompletableFuture[]::new))).thenCompose(Function.identity());
        if (bl) {
            return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.submitThrowingTask(() -> {
                try {
                    this.storage.flush();
                    return null;
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to synchronize chunks", (Throwable)exception);
                    throw exception;
                }
            }));
        }
        return ((CompletableFuture)completableFuture).thenCompose(void_ -> this.submitTask(() -> null));
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) {
        return this.submitThrowingTask(() -> {
            try {
                PendingStore pendingStore = (PendingStore)this.pendingWrites.get((Object)chunkPos);
                if (pendingStore != null) {
                    if (pendingStore.data != null) {
                        pendingStore.data.acceptAsRoot(streamTagVisitor);
                    }
                } else {
                    this.storage.scanChunk(chunkPos, streamTagVisitor);
                }
                return null;
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to bulk scan chunk {}", (Object)chunkPos, (Object)exception);
                throw exception;
            }
        });
    }

    private <T> CompletableFuture<T> submitThrowingTask(ThrowingSupplier<T> throwingSupplier) {
        return this.consecutiveExecutor.scheduleWithResult(Priority.FOREGROUND.ordinal(), completableFuture -> {
            if (!this.shutdownRequested.get()) {
                try {
                    completableFuture.complete(throwingSupplier.get());
                }
                catch (Exception exception) {
                    completableFuture.completeExceptionally(exception);
                }
            }
            this.tellStorePending();
        });
    }

    private <T> CompletableFuture<T> submitTask(Supplier<T> supplier) {
        return this.consecutiveExecutor.scheduleWithResult(Priority.FOREGROUND.ordinal(), completableFuture -> {
            if (!this.shutdownRequested.get()) {
                completableFuture.complete(supplier.get());
            }
            this.tellStorePending();
        });
    }

    private void storePendingChunk() {
        Map.Entry entry = this.pendingWrites.pollFirstEntry();
        if (entry == null) {
            return;
        }
        this.runStore((ChunkPos)entry.getKey(), (PendingStore)entry.getValue());
        this.tellStorePending();
    }

    private void tellStorePending() {
        this.consecutiveExecutor.schedule(new StrictQueue.RunnableWithPriority(Priority.BACKGROUND.ordinal(), this::storePendingChunk));
    }

    private void runStore(ChunkPos chunkPos, PendingStore pendingStore) {
        try {
            this.storage.write(chunkPos, pendingStore.data);
            pendingStore.result.complete(null);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to store chunk {}", (Object)chunkPos, (Object)exception);
            pendingStore.result.completeExceptionally(exception);
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.shutdownRequested.compareAndSet(false, true)) {
            return;
        }
        this.waitForShutdown();
        this.consecutiveExecutor.close();
        try {
            this.storage.close();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to close storage", (Throwable)exception);
        }
    }

    private void waitForShutdown() {
        this.consecutiveExecutor.scheduleWithResult(Priority.SHUTDOWN.ordinal(), completableFuture -> completableFuture.complete(Unit.INSTANCE)).join();
    }

    public RegionStorageInfo storageInfo() {
        return this.storage.info();
    }

    static enum Priority {
        FOREGROUND,
        BACKGROUND,
        SHUTDOWN;

    }

    @FunctionalInterface
    static interface ThrowingSupplier<T> {
        public @Nullable T get() throws Exception;
    }

    static class PendingStore {
        @Nullable CompoundTag data;
        final CompletableFuture<Void> result = new CompletableFuture();

        public PendingStore(@Nullable CompoundTag compoundTag) {
            this.data = compoundTag;
        }

        @Nullable CompoundTag copyData() {
            CompoundTag compoundTag = this.data;
            return compoundTag == null ? null : compoundTag.copy();
        }
    }
}

