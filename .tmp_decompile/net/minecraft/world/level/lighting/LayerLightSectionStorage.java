/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LightEngine;
import org.jspecify.annotations.Nullable;

public abstract class LayerLightSectionStorage<M extends DataLayerStorageMap<M>> {
    private final LightLayer layer;
    protected final LightChunkGetter chunkSource;
    protected final Long2ByteMap sectionStates = new Long2ByteOpenHashMap();
    private final LongSet columnsWithSources = new LongOpenHashSet();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final LongSet changedSections = new LongOpenHashSet();
    protected final LongSet sectionsAffectedByLightUpdates = new LongOpenHashSet();
    protected final Long2ObjectMap<DataLayer> queuedSections = Long2ObjectMaps.synchronize((Long2ObjectMap)new Long2ObjectOpenHashMap());
    private final LongSet columnsToRetainQueuedDataFor = new LongOpenHashSet();
    private final LongSet toRemove = new LongOpenHashSet();
    protected volatile boolean hasInconsistencies;

    protected LayerLightSectionStorage(LightLayer lightLayer, LightChunkGetter lightChunkGetter, M dataLayerStorageMap) {
        this.layer = lightLayer;
        this.chunkSource = lightChunkGetter;
        this.updatingSectionData = dataLayerStorageMap;
        this.visibleSectionData = ((DataLayerStorageMap)dataLayerStorageMap).copy();
        ((DataLayerStorageMap)this.visibleSectionData).disableCache();
        this.sectionStates.defaultReturnValue((byte)0);
    }

    protected boolean storingLightForSection(long l) {
        return this.getDataLayer(l, true) != null;
    }

    protected @Nullable DataLayer getDataLayer(long l, boolean bl) {
        return this.getDataLayer(bl ? this.updatingSectionData : this.visibleSectionData, l);
    }

    protected @Nullable DataLayer getDataLayer(M dataLayerStorageMap, long l) {
        return ((DataLayerStorageMap)dataLayerStorageMap).getLayer(l);
    }

    protected @Nullable DataLayer getDataLayerToWrite(long l) {
        DataLayer dataLayer = ((DataLayerStorageMap)this.updatingSectionData).getLayer(l);
        if (dataLayer == null) {
            return null;
        }
        if (this.changedSections.add(l)) {
            dataLayer = dataLayer.copy();
            ((DataLayerStorageMap)this.updatingSectionData).setLayer(l, dataLayer);
            ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        }
        return dataLayer;
    }

    public @Nullable DataLayer getDataLayerData(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return this.getDataLayer(l, false);
    }

    protected abstract int getLightValue(long var1);

    protected int getStoredLevel(long l) {
        long m = SectionPos.blockToSection(l);
        DataLayer dataLayer = this.getDataLayer(m, true);
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    protected void setStoredLevel(long l, int i) {
        long m = SectionPos.blockToSection(l);
        DataLayer dataLayer = this.changedSections.add(m) ? ((DataLayerStorageMap)this.updatingSectionData).copyDataLayer(m) : this.getDataLayer(m, true);
        dataLayer.set(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)), i);
        SectionPos.aroundAndAtBlockPos(l, arg_0 -> ((LongSet)this.sectionsAffectedByLightUpdates).add(arg_0));
    }

    protected void markSectionAndNeighborsAsAffected(long l) {
        int i = SectionPos.x(l);
        int j = SectionPos.y(l);
        int k = SectionPos.z(l);
        for (int m = -1; m <= 1; ++m) {
            for (int n = -1; n <= 1; ++n) {
                for (int o = -1; o <= 1; ++o) {
                    this.sectionsAffectedByLightUpdates.add(SectionPos.asLong(i + n, j + o, k + m));
                }
            }
        }
    }

    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        return new DataLayer();
    }

    protected boolean hasInconsistencies() {
        return this.hasInconsistencies;
    }

    protected void markNewInconsistencies(LightEngine<M, ?> lightEngine) {
        DataLayer dataLayer2;
        long l;
        if (!this.hasInconsistencies) {
            return;
        }
        this.hasInconsistencies = false;
        LongIterator longIterator = this.toRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            DataLayer dataLayer = (DataLayer)this.queuedSections.remove(l);
            dataLayer2 = ((DataLayerStorageMap)this.updatingSectionData).removeLayer(l);
            if (!this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(l))) continue;
            if (dataLayer != null) {
                this.queuedSections.put(l, (Object)dataLayer);
                continue;
            }
            if (dataLayer2 == null) continue;
            this.queuedSections.put(l, (Object)dataLayer2);
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
        longIterator = this.toRemove.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            this.onNodeRemoved(l);
            this.changedSections.add(l);
        }
        this.toRemove.clear();
        ObjectIterator objectIterator = Long2ObjectMaps.fastIterator(this.queuedSections);
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            long m = entry.getLongKey();
            if (!this.storingLightForSection(m)) continue;
            dataLayer2 = (DataLayer)entry.getValue();
            if (((DataLayerStorageMap)this.updatingSectionData).getLayer(m) != dataLayer2) {
                ((DataLayerStorageMap)this.updatingSectionData).setLayer(m, dataLayer2);
                this.changedSections.add(m);
            }
            objectIterator.remove();
        }
        ((DataLayerStorageMap)this.updatingSectionData).clearCache();
    }

    protected void onNodeAdded(long l) {
    }

    protected void onNodeRemoved(long l) {
    }

    protected void setLightEnabled(long l, boolean bl) {
        if (bl) {
            this.columnsWithSources.add(l);
        } else {
            this.columnsWithSources.remove(l);
        }
    }

    protected boolean lightOnInSection(long l) {
        long m = SectionPos.getZeroNode(l);
        return this.columnsWithSources.contains(m);
    }

    protected boolean lightOnInColumn(long l) {
        return this.columnsWithSources.contains(l);
    }

    public void retainData(long l, boolean bl) {
        if (bl) {
            this.columnsToRetainQueuedDataFor.add(l);
        } else {
            this.columnsToRetainQueuedDataFor.remove(l);
        }
    }

    protected void queueSectionData(long l, @Nullable DataLayer dataLayer) {
        if (dataLayer != null) {
            this.queuedSections.put(l, (Object)dataLayer);
            this.hasInconsistencies = true;
        } else {
            this.queuedSections.remove(l);
        }
    }

    protected void updateSectionStatus(long l, boolean bl) {
        byte c;
        byte b = this.sectionStates.get(l);
        if (b == (c = SectionState.hasData(b, !bl))) {
            return;
        }
        this.putSectionState(l, c);
        int i = bl ? -1 : 1;
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int m = -1; m <= 1; ++m) {
                    if (j == 0 && k == 0 && m == 0) continue;
                    long n = SectionPos.offset(l, j, k, m);
                    byte d = this.sectionStates.get(n);
                    this.putSectionState(n, SectionState.neighborCount(d, SectionState.neighborCount(d) + i));
                }
            }
        }
    }

    protected void putSectionState(long l, byte b) {
        if (b != 0) {
            if (this.sectionStates.put(l, b) == 0) {
                this.initializeSection(l);
            }
        } else if (this.sectionStates.remove(l) != 0) {
            this.removeSection(l);
        }
    }

    private void initializeSection(long l) {
        if (!this.toRemove.remove(l)) {
            ((DataLayerStorageMap)this.updatingSectionData).setLayer(l, this.createDataLayer(l));
            this.changedSections.add(l);
            this.onNodeAdded(l);
            this.markSectionAndNeighborsAsAffected(l);
            this.hasInconsistencies = true;
        }
    }

    private void removeSection(long l) {
        this.toRemove.add(l);
        this.hasInconsistencies = true;
    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            Object dataLayerStorageMap = ((DataLayerStorageMap)this.updatingSectionData).copy();
            ((DataLayerStorageMap)dataLayerStorageMap).disableCache();
            this.visibleSectionData = dataLayerStorageMap;
            this.changedSections.clear();
        }
        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            LongIterator longIterator = this.sectionsAffectedByLightUpdates.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(l));
            }
            this.sectionsAffectedByLightUpdates.clear();
        }
    }

    public SectionType getDebugSectionType(long l) {
        return SectionState.type(this.sectionStates.get(l));
    }

    protected static class SectionState {
        public static final byte EMPTY = 0;
        private static final int MIN_NEIGHBORS = 0;
        private static final int MAX_NEIGHBORS = 26;
        private static final byte HAS_DATA_BIT = 32;
        private static final byte NEIGHBOR_COUNT_BITS = 31;

        protected SectionState() {
        }

        public static byte hasData(byte b, boolean bl) {
            return (byte)(bl ? b | 0x20 : b & 0xFFFFFFDF);
        }

        public static byte neighborCount(byte b, int i) {
            if (i < 0 || i > 26) {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
            return (byte)(b & 0xFFFFFFE0 | i & 0x1F);
        }

        public static boolean hasData(byte b) {
            return (b & 0x20) != 0;
        }

        public static int neighborCount(byte b) {
            return b & 0x1F;
        }

        public static SectionType type(byte b) {
            if (b == 0) {
                return SectionType.EMPTY;
            }
            if (SectionState.hasData(b)) {
                return SectionType.LIGHT_AND_DATA;
            }
            return SectionType.LIGHT_ONLY;
        }
    }

    public static enum SectionType {
        EMPTY("2"),
        LIGHT_ONLY("1"),
        LIGHT_AND_DATA("0");

        private final String display;

        private SectionType(String string2) {
            this.display = string2;
        }

        public String display() {
            return this.display;
        }
    }
}

