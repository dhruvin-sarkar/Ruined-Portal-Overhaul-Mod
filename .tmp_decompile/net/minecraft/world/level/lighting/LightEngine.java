/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class LightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>>
implements LayerLightEventListener {
    public static final int MAX_LEVEL = 15;
    protected static final int MIN_OPACITY = 1;
    protected static final long PULL_LIGHT_IN_ENTRY = QueueEntry.decreaseAllDirections(1);
    private static final int MIN_QUEUE_SIZE = 512;
    protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final S storage;
    private final LongOpenHashSet blockNodesToCheck = new LongOpenHashSet(512, 0.5f);
    private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
    private static final int CACHE_SIZE = 2;
    private final long[] lastChunkPos = new long[2];
    private final LightChunk[] lastChunk = new LightChunk[2];

    protected LightEngine(LightChunkGetter lightChunkGetter, S layerLightSectionStorage) {
        this.chunkSource = lightChunkGetter;
        this.storage = layerLightSectionStorage;
        this.clearChunkCache();
    }

    public static boolean hasDifferentLightProperties(BlockState blockState, BlockState blockState2) {
        if (blockState2 == blockState) {
            return false;
        }
        return blockState2.getLightBlock() != blockState.getLightBlock() || blockState2.getLightEmission() != blockState.getLightEmission() || blockState2.useShapeForLightOcclusion() || blockState.useShapeForLightOcclusion();
    }

    public static int getLightBlockInto(BlockState blockState, BlockState blockState2, Direction direction, int i) {
        VoxelShape voxelShape2;
        boolean bl = LightEngine.isEmptyShape(blockState);
        boolean bl2 = LightEngine.isEmptyShape(blockState2);
        if (bl && bl2) {
            return i;
        }
        VoxelShape voxelShape = bl ? Shapes.empty() : blockState.getOcclusionShape();
        VoxelShape voxelShape3 = voxelShape2 = bl2 ? Shapes.empty() : blockState2.getOcclusionShape();
        if (Shapes.mergedFaceOccludes(voxelShape, voxelShape2, direction)) {
            return 16;
        }
        return i;
    }

    public static VoxelShape getOcclusionShape(BlockState blockState, Direction direction) {
        return LightEngine.isEmptyShape(blockState) ? Shapes.empty() : blockState.getFaceOcclusionShape(direction);
    }

    protected static boolean isEmptyShape(BlockState blockState) {
        return !blockState.canOcclude() || !blockState.useShapeForLightOcclusion();
    }

    protected BlockState getState(BlockPos blockPos) {
        int j;
        int i = SectionPos.blockToSectionCoord(blockPos.getX());
        LightChunk lightChunk = this.getChunk(i, j = SectionPos.blockToSectionCoord(blockPos.getZ()));
        if (lightChunk == null) {
            return Blocks.BEDROCK.defaultBlockState();
        }
        return lightChunk.getBlockState(blockPos);
    }

    protected int getOpacity(BlockState blockState) {
        return Math.max(1, blockState.getLightBlock());
    }

    protected boolean shapeOccludes(BlockState blockState, BlockState blockState2, Direction direction) {
        VoxelShape voxelShape = LightEngine.getOcclusionShape(blockState, direction);
        VoxelShape voxelShape2 = LightEngine.getOcclusionShape(blockState2, direction.getOpposite());
        return Shapes.faceShapeOccludes(voxelShape, voxelShape2);
    }

    protected @Nullable LightChunk getChunk(int i, int j) {
        long l = ChunkPos.asLong(i, j);
        for (int k = 0; k < 2; ++k) {
            if (l != this.lastChunkPos[k]) continue;
            return this.lastChunk[k];
        }
        LightChunk lightChunk = this.chunkSource.getChunkForLighting(i, j);
        for (int m = 1; m > 0; --m) {
            this.lastChunkPos[m] = this.lastChunkPos[m - 1];
            this.lastChunk[m] = this.lastChunk[m - 1];
        }
        this.lastChunkPos[0] = l;
        this.lastChunk[0] = lightChunk;
        return lightChunk;
    }

    private void clearChunkCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, null);
    }

    @Override
    public void checkBlock(BlockPos blockPos) {
        this.blockNodesToCheck.add(blockPos.asLong());
    }

    public void queueSectionData(long l, @Nullable DataLayer dataLayer) {
        ((LayerLightSectionStorage)this.storage).queueSectionData(l, dataLayer);
    }

    public void retainData(ChunkPos chunkPos, boolean bl) {
        ((LayerLightSectionStorage)this.storage).retainData(SectionPos.getZeroNode(chunkPos.x, chunkPos.z), bl);
    }

    @Override
    public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        ((LayerLightSectionStorage)this.storage).updateSectionStatus(sectionPos.asLong(), bl);
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
        ((LayerLightSectionStorage)this.storage).setLightEnabled(SectionPos.getZeroNode(chunkPos.x, chunkPos.z), bl);
    }

    @Override
    public int runLightUpdates() {
        LongIterator longIterator = this.blockNodesToCheck.iterator();
        while (longIterator.hasNext()) {
            this.checkNode(longIterator.nextLong());
        }
        this.blockNodesToCheck.clear();
        this.blockNodesToCheck.trim(512);
        int i = 0;
        i += this.propagateDecreases();
        this.clearChunkCache();
        ((LayerLightSectionStorage)this.storage).markNewInconsistencies(this);
        ((LayerLightSectionStorage)this.storage).swapSectionMap();
        return i += this.propagateIncreases();
    }

    private int propagateIncreases() {
        int i = 0;
        while (!this.increaseQueue.isEmpty()) {
            long l = this.increaseQueue.dequeueLong();
            long m = this.increaseQueue.dequeueLong();
            int j = ((LayerLightSectionStorage)this.storage).getStoredLevel(l);
            int k = QueueEntry.getFromLevel(m);
            if (QueueEntry.isIncreaseFromEmission(m) && j < k) {
                ((LayerLightSectionStorage)this.storage).setStoredLevel(l, k);
                j = k;
            }
            if (j == k) {
                this.propagateIncrease(l, m, j);
            }
            ++i;
        }
        return i;
    }

    private int propagateDecreases() {
        int i = 0;
        while (!this.decreaseQueue.isEmpty()) {
            long l = this.decreaseQueue.dequeueLong();
            long m = this.decreaseQueue.dequeueLong();
            this.propagateDecrease(l, m);
            ++i;
        }
        return i;
    }

    protected void enqueueDecrease(long l, long m) {
        this.decreaseQueue.enqueue(l);
        this.decreaseQueue.enqueue(m);
    }

    protected void enqueueIncrease(long l, long m) {
        this.increaseQueue.enqueue(l);
        this.increaseQueue.enqueue(m);
    }

    @Override
    public boolean hasLightWork() {
        return ((LayerLightSectionStorage)this.storage).hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
    }

    @Override
    public @Nullable DataLayer getDataLayerData(SectionPos sectionPos) {
        return ((LayerLightSectionStorage)this.storage).getDataLayerData(sectionPos.asLong());
    }

    @Override
    public int getLightValue(BlockPos blockPos) {
        return ((LayerLightSectionStorage)this.storage).getLightValue(blockPos.asLong());
    }

    public String getDebugData(long l) {
        return this.getDebugSectionType(l).display();
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(long l) {
        return ((LayerLightSectionStorage)this.storage).getDebugSectionType(l);
    }

    protected abstract void checkNode(long var1);

    protected abstract void propagateIncrease(long var1, long var3, int var5);

    protected abstract void propagateDecrease(long var1, long var3);

    public static class QueueEntry {
        private static final int FROM_LEVEL_BITS = 4;
        private static final int DIRECTION_BITS = 6;
        private static final long LEVEL_MASK = 15L;
        private static final long DIRECTIONS_MASK = 1008L;
        private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
        private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

        public static long decreaseSkipOneDirection(int i, Direction direction) {
            long l = QueueEntry.withoutDirection(1008L, direction);
            return QueueEntry.withLevel(l, i);
        }

        public static long decreaseAllDirections(int i) {
            return QueueEntry.withLevel(1008L, i);
        }

        public static long increaseLightFromEmission(int i, boolean bl) {
            long l = 1008L;
            l |= 0x800L;
            if (bl) {
                l |= 0x400L;
            }
            return QueueEntry.withLevel(l, i);
        }

        public static long increaseSkipOneDirection(int i, boolean bl, Direction direction) {
            long l = QueueEntry.withoutDirection(1008L, direction);
            if (bl) {
                l |= 0x400L;
            }
            return QueueEntry.withLevel(l, i);
        }

        public static long increaseOnlyOneDirection(int i, boolean bl, Direction direction) {
            long l = 0L;
            if (bl) {
                l |= 0x400L;
            }
            l = QueueEntry.withDirection(l, direction);
            return QueueEntry.withLevel(l, i);
        }

        public static long increaseSkySourceInDirections(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            long l = QueueEntry.withLevel(0L, 15);
            if (bl) {
                l = QueueEntry.withDirection(l, Direction.DOWN);
            }
            if (bl2) {
                l = QueueEntry.withDirection(l, Direction.NORTH);
            }
            if (bl3) {
                l = QueueEntry.withDirection(l, Direction.SOUTH);
            }
            if (bl4) {
                l = QueueEntry.withDirection(l, Direction.WEST);
            }
            if (bl5) {
                l = QueueEntry.withDirection(l, Direction.EAST);
            }
            return l;
        }

        public static int getFromLevel(long l) {
            return (int)(l & 0xFL);
        }

        public static boolean isFromEmptyShape(long l) {
            return (l & 0x400L) != 0L;
        }

        public static boolean isIncreaseFromEmission(long l) {
            return (l & 0x800L) != 0L;
        }

        public static boolean shouldPropagateInDirection(long l, Direction direction) {
            return (l & 1L << direction.ordinal() + 4) != 0L;
        }

        private static long withLevel(long l, int i) {
            return l & 0xFFFFFFFFFFFFFFF0L | (long)i & 0xFL;
        }

        private static long withDirection(long l, Direction direction) {
            return l | 1L << direction.ordinal() + 4;
        }

        private static long withoutDirection(long l, Direction direction) {
            return l & (1L << direction.ordinal() + 4 ^ 0xFFFFFFFFFFFFFFFFL);
        }
    }
}

