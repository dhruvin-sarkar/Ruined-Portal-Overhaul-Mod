/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.jspecify.annotations.Nullable;

public final class SkyLightEngine
extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final ChunkSkyLightSources emptyChunkSources;

    public SkyLightEngine(LightChunkGetter lightChunkGetter) {
        this(lightChunkGetter, new SkyLightSectionStorage(lightChunkGetter));
    }

    @VisibleForTesting
    protected SkyLightEngine(LightChunkGetter lightChunkGetter, SkyLightSectionStorage skyLightSectionStorage) {
        super(lightChunkGetter, skyLightSectionStorage);
        this.emptyChunkSources = new ChunkSkyLightSources(lightChunkGetter.getLevel());
    }

    private static boolean isSourceLevel(int i) {
        return i == 15;
    }

    private int getLowestSourceY(int i, int j, int k) {
        ChunkSkyLightSources chunkSkyLightSources = this.getChunkSources(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
        if (chunkSkyLightSources == null) {
            return k;
        }
        return chunkSkyLightSources.getLowestSourceY(SectionPos.sectionRelative(i), SectionPos.sectionRelative(j));
    }

    private @Nullable ChunkSkyLightSources getChunkSources(int i, int j) {
        LightChunk lightChunk = this.chunkSource.getChunkForLighting(i, j);
        return lightChunk != null ? lightChunk.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(long l) {
        boolean bl;
        int n;
        int i = BlockPos.getX(l);
        int j = BlockPos.getY(l);
        int k = BlockPos.getZ(l);
        long m = SectionPos.blockToSection(l);
        int n2 = n = ((SkyLightSectionStorage)this.storage).lightOnInSection(m) ? this.getLowestSourceY(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (n != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(i, k, n);
        }
        if (!((SkyLightSectionStorage)this.storage).storingLightForSection(m)) {
            return;
        }
        boolean bl2 = bl = j >= n;
        if (bl) {
            this.enqueueDecrease(l, REMOVE_SKY_SOURCE_ENTRY);
            this.enqueueIncrease(l, ADD_SKY_SOURCE_ENTRY);
        } else {
            int o = ((SkyLightSectionStorage)this.storage).getStoredLevel(l);
            if (o > 0) {
                ((SkyLightSectionStorage)this.storage).setStoredLevel(l, 0);
                this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(o));
            } else {
                this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
            }
        }
    }

    private void updateSourcesInColumn(int i, int j, int k) {
        int l = SectionPos.sectionToBlockCoord(((SkyLightSectionStorage)this.storage).getBottomSectionY());
        this.removeSourcesBelow(i, j, k, l);
        this.addSourcesAbove(i, j, k, l);
    }

    private void removeSourcesBelow(int i, int j, int k, int l) {
        if (k <= l) {
            return;
        }
        int m = SectionPos.blockToSectionCoord(i);
        int n = SectionPos.blockToSectionCoord(j);
        int o = k - 1;
        int p = SectionPos.blockToSectionCoord(o);
        while (((SkyLightSectionStorage)this.storage).hasLightDataAtOrBelow(p)) {
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(m, p, n))) {
                int q = SectionPos.sectionToBlockCoord(p);
                int r = q + 15;
                for (int s = Math.min(r, o); s >= q; --s) {
                    long t = BlockPos.asLong(i, s, j);
                    if (!SkyLightEngine.isSourceLevel(((SkyLightSectionStorage)this.storage).getStoredLevel(t))) {
                        return;
                    }
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(t, 0);
                    this.enqueueDecrease(t, s == k - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
                }
            }
            --p;
        }
    }

    private void addSourcesAbove(int i, int j, int k, int l) {
        int m = SectionPos.blockToSectionCoord(i);
        int n = SectionPos.blockToSectionCoord(j);
        int o = Math.max(Math.max(this.getLowestSourceY(i - 1, j, Integer.MIN_VALUE), this.getLowestSourceY(i + 1, j, Integer.MIN_VALUE)), Math.max(this.getLowestSourceY(i, j - 1, Integer.MIN_VALUE), this.getLowestSourceY(i, j + 1, Integer.MIN_VALUE)));
        int p = Math.max(k, l);
        long q = SectionPos.asLong(m, SectionPos.blockToSectionCoord(p), n);
        while (!((SkyLightSectionStorage)this.storage).isAboveData(q)) {
            if (((SkyLightSectionStorage)this.storage).storingLightForSection(q)) {
                int r = SectionPos.sectionToBlockCoord(SectionPos.y(q));
                int s = r + 15;
                for (int t = Math.max(r, p); t <= s; ++t) {
                    long u = BlockPos.asLong(i, t, j);
                    if (SkyLightEngine.isSourceLevel(((SkyLightSectionStorage)this.storage).getStoredLevel(u))) {
                        return;
                    }
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(u, 15);
                    if (t >= o && t != k) continue;
                    this.enqueueIncrease(u, ADD_SKY_SOURCE_ENTRY);
                }
            }
            q = SectionPos.offset(q, Direction.UP);
        }
    }

    @Override
    protected void propagateIncrease(long l, long m, int i) {
        BlockState blockState = null;
        int j = this.countEmptySectionsBelowIfAtBorder(l);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int k;
            int o;
            long n;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(m, direction) || !((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(n = BlockPos.offset(l, direction))) || (o = i - 1) <= (k = ((SkyLightSectionStorage)this.storage).getStoredLevel(n))) continue;
            this.mutablePos.set(n);
            BlockState blockState2 = this.getState(this.mutablePos);
            int p = i - this.getOpacity(blockState2);
            if (p <= k) continue;
            if (blockState == null) {
                BlockState blockState3 = blockState = LightEngine.QueueEntry.isFromEmptyShape(m) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
            }
            if (this.shapeOccludes(blockState, blockState2, direction)) continue;
            ((SkyLightSectionStorage)this.storage).setStoredLevel(n, p);
            if (p > 1) {
                this.enqueueIncrease(n, LightEngine.QueueEntry.increaseSkipOneDirection(p, SkyLightEngine.isEmptyShape(blockState2), direction.getOpposite()));
            }
            this.propagateFromEmptySections(n, direction, p, true, j);
        }
    }

    @Override
    protected void propagateDecrease(long l, long m) {
        int i = this.countEmptySectionsBelowIfAtBorder(l);
        int j = LightEngine.QueueEntry.getFromLevel(m);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int k;
            long n;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(m, direction) || !((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(n = BlockPos.offset(l, direction))) || (k = ((SkyLightSectionStorage)this.storage).getStoredLevel(n)) == 0) continue;
            if (k <= j - 1) {
                ((SkyLightSectionStorage)this.storage).setStoredLevel(n, 0);
                this.enqueueDecrease(n, LightEngine.QueueEntry.decreaseSkipOneDirection(k, direction.getOpposite()));
                this.propagateFromEmptySections(n, direction, k, false, i);
                continue;
            }
            this.enqueueIncrease(n, LightEngine.QueueEntry.increaseOnlyOneDirection(k, false, direction.getOpposite()));
        }
    }

    private int countEmptySectionsBelowIfAtBorder(long l) {
        int i = BlockPos.getY(l);
        int j = SectionPos.sectionRelative(i);
        if (j != 0) {
            return 0;
        }
        int k = BlockPos.getX(l);
        int m = BlockPos.getZ(l);
        int n = SectionPos.sectionRelative(k);
        int o = SectionPos.sectionRelative(m);
        if (n == 0 || n == 15 || o == 0 || o == 15) {
            int p = SectionPos.blockToSectionCoord(k);
            int q = SectionPos.blockToSectionCoord(i);
            int r = SectionPos.blockToSectionCoord(m);
            int s = 0;
            while (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(p, q - s - 1, r)) && ((SkyLightSectionStorage)this.storage).hasLightDataAtOrBelow(q - s - 1)) {
                ++s;
            }
            return s;
        }
        return 0;
    }

    private void propagateFromEmptySections(long l, Direction direction, int i, boolean bl, int j) {
        if (j == 0) {
            return;
        }
        int k = BlockPos.getX(l);
        int m = BlockPos.getZ(l);
        if (!SkyLightEngine.crossedSectionEdge(direction, SectionPos.sectionRelative(k), SectionPos.sectionRelative(m))) {
            return;
        }
        int n = BlockPos.getY(l);
        int o = SectionPos.blockToSectionCoord(k);
        int p = SectionPos.blockToSectionCoord(m);
        int q = SectionPos.blockToSectionCoord(n) - 1;
        int r = q - j + 1;
        while (q >= r) {
            if (!((SkyLightSectionStorage)this.storage).storingLightForSection(SectionPos.asLong(o, q, p))) {
                --q;
                continue;
            }
            int s = SectionPos.sectionToBlockCoord(q);
            for (int t = 15; t >= 0; --t) {
                long u = BlockPos.asLong(k, s + t, m);
                if (bl) {
                    ((SkyLightSectionStorage)this.storage).setStoredLevel(u, i);
                    if (i <= 1) continue;
                    this.enqueueIncrease(u, LightEngine.QueueEntry.increaseSkipOneDirection(i, true, direction.getOpposite()));
                    continue;
                }
                ((SkyLightSectionStorage)this.storage).setStoredLevel(u, 0);
                this.enqueueDecrease(u, LightEngine.QueueEntry.decreaseSkipOneDirection(i, direction.getOpposite()));
            }
            --q;
        }
    }

    private static boolean crossedSectionEdge(Direction direction, int i, int j) {
        return switch (direction) {
            case Direction.NORTH -> {
                if (j == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (j == 0) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (i == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (i == 0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
        super.setLightEnabled(chunkPos, bl);
        if (bl) {
            ChunkSkyLightSources chunkSkyLightSources = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x, chunkPos.z), (Object)this.emptyChunkSources);
            int i = chunkSkyLightSources.getHighestLowestSourceY() - 1;
            int j = SectionPos.blockToSectionCoord(i) + 1;
            long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
            int k = ((SkyLightSectionStorage)this.storage).getTopSectionY(l);
            int m = Math.max(((SkyLightSectionStorage)this.storage).getBottomSectionY(), j);
            for (int n = k - 1; n >= m; --n) {
                DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayerToWrite(SectionPos.asLong(chunkPos.x, n, chunkPos.z));
                if (dataLayer == null || !dataLayer.isEmpty()) continue;
                dataLayer.fill(15);
            }
        }
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        long l = SectionPos.getZeroNode(chunkPos.x, chunkPos.z);
        ((SkyLightSectionStorage)this.storage).setLightEnabled(l, true);
        ChunkSkyLightSources chunkSkyLightSources = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x, chunkPos.z), (Object)this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources2 = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x, chunkPos.z - 1), (Object)this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources3 = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x, chunkPos.z + 1), (Object)this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources4 = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x - 1, chunkPos.z), (Object)this.emptyChunkSources);
        ChunkSkyLightSources chunkSkyLightSources5 = (ChunkSkyLightSources)Objects.requireNonNullElse((Object)this.getChunkSources(chunkPos.x + 1, chunkPos.z), (Object)this.emptyChunkSources);
        int i = ((SkyLightSectionStorage)this.storage).getTopSectionY(l);
        int j = ((SkyLightSectionStorage)this.storage).getBottomSectionY();
        int k = SectionPos.sectionToBlockCoord(chunkPos.x);
        int m = SectionPos.sectionToBlockCoord(chunkPos.z);
        for (int n = i - 1; n >= j; --n) {
            long o = SectionPos.asLong(chunkPos.x, n, chunkPos.z);
            DataLayer dataLayer = ((SkyLightSectionStorage)this.storage).getDataLayerToWrite(o);
            if (dataLayer == null) continue;
            int p = SectionPos.sectionToBlockCoord(n);
            int q = p + 15;
            boolean bl = false;
            for (int r = 0; r < 16; ++r) {
                for (int s = 0; s < 16; ++s) {
                    int t = chunkSkyLightSources.getLowestSourceY(s, r);
                    if (t > q) continue;
                    int u = r == 0 ? chunkSkyLightSources2.getLowestSourceY(s, 15) : chunkSkyLightSources.getLowestSourceY(s, r - 1);
                    int v = r == 15 ? chunkSkyLightSources3.getLowestSourceY(s, 0) : chunkSkyLightSources.getLowestSourceY(s, r + 1);
                    int w = s == 0 ? chunkSkyLightSources4.getLowestSourceY(15, r) : chunkSkyLightSources.getLowestSourceY(s - 1, r);
                    int x = s == 15 ? chunkSkyLightSources5.getLowestSourceY(0, r) : chunkSkyLightSources.getLowestSourceY(s + 1, r);
                    int y = Math.max(Math.max(u, v), Math.max(w, x));
                    for (int z = q; z >= Math.max(p, t); --z) {
                        dataLayer.set(s, SectionPos.sectionRelative(z), r, 15);
                        if (z != t && z >= y) continue;
                        long aa = BlockPos.asLong(k + s, z, m + r);
                        this.enqueueIncrease(aa, LightEngine.QueueEntry.increaseSkySourceInDirections(z == t, z < u, z < v, z < w, z < x));
                    }
                    if (t >= p) continue;
                    bl = true;
                }
            }
            if (!bl) break;
        }
    }
}

