/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

public class SkyLightSectionStorage
extends LayerLightSectionStorage<SkyDataLayerStorageMap> {
    protected SkyLightSectionStorage(LightChunkGetter lightChunkGetter) {
        super(LightLayer.SKY, lightChunkGetter, new SkyDataLayerStorageMap((Long2ObjectOpenHashMap<DataLayer>)new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
    }

    @Override
    protected int getLightValue(long l) {
        return this.getLightValue(l, false);
    }

    protected int getLightValue(long l, boolean bl) {
        long m = SectionPos.blockToSection(l);
        int i = SectionPos.y(m);
        SkyDataLayerStorageMap skyDataLayerStorageMap = bl ? (SkyDataLayerStorageMap)this.updatingSectionData : (SkyDataLayerStorageMap)this.visibleSectionData;
        int j = skyDataLayerStorageMap.topSections.get(SectionPos.getZeroNode(m));
        if (j == skyDataLayerStorageMap.currentLowestY || i >= j) {
            if (bl && !this.lightOnInSection(m)) {
                return 0;
            }
            return 15;
        }
        DataLayer dataLayer = this.getDataLayer(skyDataLayerStorageMap, m);
        if (dataLayer == null) {
            l = BlockPos.getFlatIndex(l);
            while (dataLayer == null) {
                if (++i >= j) {
                    return 15;
                }
                m = SectionPos.offset(m, Direction.UP);
                dataLayer = this.getDataLayer(skyDataLayerStorageMap, m);
            }
        }
        return dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
    }

    @Override
    protected void onNodeAdded(long l) {
        long m;
        int j;
        int i = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY > i) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY = i;
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.defaultReturnValue(((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY);
        }
        if ((j = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m = SectionPos.getZeroNode(l))) < i + 1) {
            ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(m, i + 1);
        }
    }

    @Override
    protected void onNodeRemoved(long l) {
        long m = SectionPos.getZeroNode(l);
        int i = SectionPos.y(l);
        if (((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m) == i + 1) {
            long n = l;
            while (!this.storingLightForSection(n) && this.hasLightDataAtOrBelow(i)) {
                --i;
                n = SectionPos.offset(n, Direction.DOWN);
            }
            if (this.storingLightForSection(n)) {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.put(m, i + 1);
            } else {
                ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.remove(m);
            }
        }
    }

    @Override
    protected DataLayer createDataLayer(long l) {
        DataLayer dataLayer2;
        DataLayer dataLayer = (DataLayer)this.queuedSections.get(l);
        if (dataLayer != null) {
            return dataLayer;
        }
        int i = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(SectionPos.getZeroNode(l));
        if (i == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(l) >= i) {
            if (this.lightOnInSection(l)) {
                return new DataLayer(15);
            }
            return new DataLayer();
        }
        long m = SectionPos.offset(l, Direction.UP);
        while ((dataLayer2 = this.getDataLayer(m, true)) == null) {
            m = SectionPos.offset(m, Direction.UP);
        }
        return SkyLightSectionStorage.repeatFirstLayer(dataLayer2);
    }

    private static DataLayer repeatFirstLayer(DataLayer dataLayer) {
        if (dataLayer.isDefinitelyHomogenous()) {
            return dataLayer.copy();
        }
        byte[] bs = dataLayer.getData();
        byte[] cs = new byte[2048];
        for (int i = 0; i < 16; ++i) {
            System.arraycopy(bs, 0, cs, i * 128, 128);
        }
        return new DataLayer(cs);
    }

    protected boolean hasLightDataAtOrBelow(int i) {
        return i >= ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY;
    }

    protected boolean isAboveData(long l) {
        long m = SectionPos.getZeroNode(l);
        int i = ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(m);
        return i == ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY || SectionPos.y(l) >= i;
    }

    protected int getTopSectionY(long l) {
        return ((SkyDataLayerStorageMap)this.updatingSectionData).topSections.get(l);
    }

    protected int getBottomSectionY() {
        return ((SkyDataLayerStorageMap)this.updatingSectionData).currentLowestY;
    }

    protected static final class SkyDataLayerStorageMap
    extends DataLayerStorageMap<SkyDataLayerStorageMap> {
        int currentLowestY;
        final Long2IntOpenHashMap topSections;

        public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap, Long2IntOpenHashMap long2IntOpenHashMap, int i) {
            super(long2ObjectOpenHashMap);
            this.topSections = long2IntOpenHashMap;
            long2IntOpenHashMap.defaultReturnValue(i);
            this.currentLowestY = i;
        }

        @Override
        public SkyDataLayerStorageMap copy() {
            return new SkyDataLayerStorageMap((Long2ObjectOpenHashMap<DataLayer>)this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }

        @Override
        public /* synthetic */ DataLayerStorageMap copy() {
            return this.copy();
        }
    }
}

