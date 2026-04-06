/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.OptionalDynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongListIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SectionStorage<R, P>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final SimpleRegionStorage simpleRegionStorage;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
    private final LongLinkedOpenHashSet dirtyChunks = new LongLinkedOpenHashSet();
    private final Codec<P> codec;
    private final Function<R, P> packer;
    private final BiFunction<P, Runnable, R> unpacker;
    private final Function<Runnable, R> factory;
    private final RegistryAccess registryAccess;
    private final ChunkIOErrorReporter errorReporter;
    protected final LevelHeightAccessor levelHeightAccessor;
    private final LongSet loadedChunks = new LongOpenHashSet();
    private final Long2ObjectMap<CompletableFuture<Optional<PackedChunk<P>>>> pendingLoads = new Long2ObjectOpenHashMap();
    private final Object loadLock = new Object();

    public SectionStorage(SimpleRegionStorage simpleRegionStorage, Codec<P> codec, Function<R, P> function, BiFunction<P, Runnable, R> biFunction, Function<Runnable, R> function2, RegistryAccess registryAccess, ChunkIOErrorReporter chunkIOErrorReporter, LevelHeightAccessor levelHeightAccessor) {
        this.simpleRegionStorage = simpleRegionStorage;
        this.codec = codec;
        this.packer = function;
        this.unpacker = biFunction;
        this.factory = function2;
        this.registryAccess = registryAccess;
        this.errorReporter = chunkIOErrorReporter;
        this.levelHeightAccessor = levelHeightAccessor;
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        LongListIterator longIterator = this.dirtyChunks.iterator();
        while (longIterator.hasNext() && booleanSupplier.getAsBoolean()) {
            ChunkPos chunkPos = new ChunkPos(longIterator.nextLong());
            longIterator.remove();
            this.writeChunk(chunkPos);
        }
        this.unpackPendingLoads();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void unpackPendingLoads() {
        Object object = this.loadLock;
        synchronized (object) {
            ObjectIterator iterator = Long2ObjectMaps.fastIterator(this.pendingLoads);
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)iterator.next();
                Optional optional = ((CompletableFuture)entry.getValue()).getNow(null);
                if (optional == null) continue;
                long l = entry.getLongKey();
                this.unpackChunk(new ChunkPos(l), optional.orElse(null));
                iterator.remove();
                this.loadedChunks.add(l);
            }
        }
    }

    public void flushAll() {
        if (!this.dirtyChunks.isEmpty()) {
            this.dirtyChunks.forEach(l -> this.writeChunk(new ChunkPos(l)));
            this.dirtyChunks.clear();
        }
    }

    public boolean hasWork() {
        return !this.dirtyChunks.isEmpty();
    }

    protected @Nullable Optional<R> get(long l) {
        return (Optional)this.storage.get(l);
    }

    protected Optional<R> getOrLoad(long l) {
        if (this.outsideStoredRange(l)) {
            return Optional.empty();
        }
        Optional<R> optional = this.get(l);
        if (optional != null) {
            return optional;
        }
        this.unpackChunk(SectionPos.of(l).chunk());
        optional = this.get(l);
        if (optional == null) {
            throw Util.pauseInIde(new IllegalStateException());
        }
        return optional;
    }

    protected boolean outsideStoredRange(long l) {
        int i = SectionPos.sectionToBlockCoord(SectionPos.y(l));
        return this.levelHeightAccessor.isOutsideBuildHeight(i);
    }

    protected R getOrCreate(long l) {
        if (this.outsideStoredRange(l)) {
            throw Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        }
        Optional<R> optional = this.getOrLoad(l);
        if (optional.isPresent()) {
            return optional.get();
        }
        R object = this.factory.apply(() -> this.setDirty(l));
        this.storage.put(l, Optional.of(object));
        return object;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CompletableFuture<?> prefetch(ChunkPos chunkPos) {
        Object object = this.loadLock;
        synchronized (object) {
            long l2 = chunkPos.toLong();
            if (this.loadedChunks.contains(l2)) {
                return CompletableFuture.completedFuture(null);
            }
            return (CompletableFuture)this.pendingLoads.computeIfAbsent(l2, l -> this.tryRead(chunkPos));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void unpackChunk(ChunkPos chunkPos) {
        CompletableFuture completableFuture;
        long l2 = chunkPos.toLong();
        Object object = this.loadLock;
        synchronized (object) {
            if (!this.loadedChunks.add(l2)) {
                return;
            }
            completableFuture = (CompletableFuture)this.pendingLoads.computeIfAbsent(l2, l -> this.tryRead(chunkPos));
        }
        this.unpackChunk(chunkPos, ((Optional)completableFuture.join()).orElse(null));
        object = this.loadLock;
        synchronized (object) {
            this.pendingLoads.remove(l2);
        }
    }

    private CompletableFuture<Optional<PackedChunk<P>>> tryRead(ChunkPos chunkPos) {
        RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return ((CompletableFuture)this.simpleRegionStorage.read(chunkPos).thenApplyAsync(optional -> optional.map(compoundTag -> PackedChunk.parse(this.codec, registryOps, compoundTag, this.simpleRegionStorage, this.levelHeightAccessor)), Util.backgroundExecutor().forName("parseSection"))).exceptionally(throwable -> {
            if (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }
            if (throwable instanceof IOException) {
                IOException iOException = (IOException)throwable;
                LOGGER.error("Error reading chunk {} data from disk", (Object)chunkPos, (Object)iOException);
                this.errorReporter.reportChunkLoadFailure(iOException, this.simpleRegionStorage.storageInfo(), chunkPos);
                return Optional.empty();
            }
            throw new CompletionException((Throwable)throwable);
        });
    }

    private void unpackChunk(ChunkPos chunkPos, @Nullable PackedChunk<P> packedChunk) {
        if (packedChunk == null) {
            for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); ++i) {
                this.storage.put(SectionStorage.getKey(chunkPos, i), Optional.empty());
            }
        } else {
            boolean bl = packedChunk.versionChanged();
            for (int j = this.levelHeightAccessor.getMinSectionY(); j <= this.levelHeightAccessor.getMaxSectionY(); ++j) {
                long l = SectionStorage.getKey(chunkPos, j);
                Optional<Object> optional = Optional.ofNullable(packedChunk.sectionsByY.get(j)).map(object -> this.unpacker.apply(object, () -> this.setDirty(l)));
                this.storage.put(l, optional);
                optional.ifPresent(object -> {
                    this.onSectionLoad(l);
                    if (bl) {
                        this.setDirty(l);
                    }
                });
            }
        }
    }

    private void writeChunk(ChunkPos chunkPos) {
        RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
        Dynamic<Tag> dynamic = this.writeChunk(chunkPos, registryOps);
        Tag tag = (Tag)dynamic.getValue();
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            this.simpleRegionStorage.write(chunkPos, compoundTag).exceptionally(throwable -> {
                this.errorReporter.reportChunkSaveFailure((Throwable)throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
                return null;
            });
        } else {
            LOGGER.error("Expected compound tag, got {}", (Object)tag);
        }
    }

    private <T> Dynamic<T> writeChunk(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
        HashMap map = Maps.newHashMap();
        for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); ++i) {
            long l = SectionStorage.getKey(chunkPos, i);
            Optional optional = (Optional)this.storage.get(l);
            if (optional == null || optional.isEmpty()) continue;
            DataResult dataResult = this.codec.encodeStart(dynamicOps, this.packer.apply(optional.get()));
            String string = Integer.toString(i);
            dataResult.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(object -> map.put(dynamicOps.createString(string), object));
        }
        return new Dynamic(dynamicOps, dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString(SECTIONS_TAG), (Object)dynamicOps.createMap((Map)map), (Object)dynamicOps.createString("DataVersion"), (Object)dynamicOps.createInt(SharedConstants.getCurrentVersion().dataVersion().version()))));
    }

    private static long getKey(ChunkPos chunkPos, int i) {
        return SectionPos.asLong(chunkPos.x, i, chunkPos.z);
    }

    protected void onSectionLoad(long l) {
    }

    protected void setDirty(long l) {
        Optional optional = (Optional)this.storage.get(l);
        if (optional == null || optional.isEmpty()) {
            LOGGER.warn("No data for position: {}", (Object)SectionPos.of(l));
            return;
        }
        this.dirtyChunks.add(ChunkPos.asLong(SectionPos.x(l), SectionPos.z(l)));
    }

    public void flush(ChunkPos chunkPos) {
        if (this.dirtyChunks.remove(chunkPos.toLong())) {
            this.writeChunk(chunkPos);
        }
    }

    @Override
    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }

    static final class PackedChunk<T>
    extends Record {
        final Int2ObjectMap<T> sectionsByY;
        private final boolean versionChanged;

        private PackedChunk(Int2ObjectMap<T> int2ObjectMap, boolean bl) {
            this.sectionsByY = int2ObjectMap;
            this.versionChanged = bl;
        }

        public static <T> PackedChunk<T> parse(Codec<T> codec, DynamicOps<Tag> dynamicOps, Tag tag, SimpleRegionStorage simpleRegionStorage, LevelHeightAccessor levelHeightAccessor) {
            Dynamic dynamic2 = new Dynamic(dynamicOps, (Object)tag);
            Dynamic<Tag> dynamic22 = simpleRegionStorage.upgradeChunkTag((Dynamic<Tag>)dynamic2, 1945);
            boolean bl = dynamic2 != dynamic22;
            OptionalDynamic optionalDynamic = dynamic22.get(SectionStorage.SECTIONS_TAG);
            Int2ObjectOpenHashMap int2ObjectMap = new Int2ObjectOpenHashMap();
            for (int i = levelHeightAccessor.getMinSectionY(); i <= levelHeightAccessor.getMaxSectionY(); ++i) {
                Optional optional = optionalDynamic.get(Integer.toString(i)).result().flatMap(dynamic -> codec.parse(dynamic).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)));
                if (!optional.isPresent()) continue;
                int2ObjectMap.put(i, optional.get());
            }
            return new PackedChunk<T>(int2ObjectMap, bl);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PackedChunk.class, "sectionsByY;versionChanged", "sectionsByY", "versionChanged"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PackedChunk.class, "sectionsByY;versionChanged", "sectionsByY", "versionChanged"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PackedChunk.class, "sectionsByY;versionChanged", "sectionsByY", "versionChanged"}, this, object);
        }

        public Int2ObjectMap<T> sectionsByY() {
            return this.sectionsByY;
        }

        public boolean versionChanged() {
            return this.versionChanged;
        }
    }
}

