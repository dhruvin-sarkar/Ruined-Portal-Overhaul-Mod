/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.chunk.DataLayer;
import org.jspecify.annotations.Nullable;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
    private static final int CACHE_SIZE = 2;
    private final long[] lastSectionKeys = new long[2];
    private final @Nullable DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Long2ObjectOpenHashMap<DataLayer> map;

    protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap) {
        this.map = long2ObjectOpenHashMap;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public DataLayer copyDataLayer(long l) {
        DataLayer dataLayer = ((DataLayer)this.map.get(l)).copy();
        this.map.put(l, (Object)dataLayer);
        this.clearCache();
        return dataLayer;
    }

    public boolean hasLayer(long l) {
        return this.map.containsKey(l);
    }

    public @Nullable DataLayer getLayer(long l) {
        DataLayer dataLayer;
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; ++i) {
                if (l != this.lastSectionKeys[i]) continue;
                return this.lastSections[i];
            }
        }
        if ((dataLayer = (DataLayer)this.map.get(l)) != null) {
            if (this.cacheEnabled) {
                for (int j = 1; j > 0; --j) {
                    this.lastSectionKeys[j] = this.lastSectionKeys[j - 1];
                    this.lastSections[j] = this.lastSections[j - 1];
                }
                this.lastSectionKeys[0] = l;
                this.lastSections[0] = dataLayer;
            }
            return dataLayer;
        }
        return null;
    }

    public @Nullable DataLayer removeLayer(long l) {
        return (DataLayer)this.map.remove(l);
    }

    public void setLayer(long l, DataLayer dataLayer) {
        this.map.put(l, (Object)dataLayer);
    }

    public void clearCache() {
        for (int i = 0; i < 2; ++i) {
            this.lastSectionKeys[i] = Long.MAX_VALUE;
            this.lastSections[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}

