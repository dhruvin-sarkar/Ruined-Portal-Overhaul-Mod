/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;

public final class BlockLightEngine
extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter lightChunkGetter) {
        this(lightChunkGetter, new BlockLightSectionStorage(lightChunkGetter));
    }

    @VisibleForTesting
    public BlockLightEngine(LightChunkGetter lightChunkGetter, BlockLightSectionStorage blockLightSectionStorage) {
        super(lightChunkGetter, blockLightSectionStorage);
    }

    @Override
    protected void checkNode(long l) {
        int j;
        long m = SectionPos.blockToSection(l);
        if (!((BlockLightSectionStorage)this.storage).storingLightForSection(m)) {
            return;
        }
        BlockState blockState = this.getState(this.mutablePos.set(l));
        int i = this.getEmission(l, blockState);
        if (i < (j = ((BlockLightSectionStorage)this.storage).getStoredLevel(l))) {
            ((BlockLightSectionStorage)this.storage).setStoredLevel(l, 0);
            this.enqueueDecrease(l, LightEngine.QueueEntry.decreaseAllDirections(j));
        } else {
            this.enqueueDecrease(l, PULL_LIGHT_IN_ENTRY);
        }
        if (i > 0) {
            this.enqueueIncrease(l, LightEngine.QueueEntry.increaseLightFromEmission(i, BlockLightEngine.isEmptyShape(blockState)));
        }
    }

    @Override
    protected void propagateIncrease(long l, long m, int i) {
        BlockState blockState = null;
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int j;
            int k;
            long n;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(m, direction) || !((BlockLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(n = BlockPos.offset(l, direction))) || (k = i - 1) <= (j = ((BlockLightSectionStorage)this.storage).getStoredLevel(n))) continue;
            this.mutablePos.set(n);
            BlockState blockState2 = this.getState(this.mutablePos);
            int o = i - this.getOpacity(blockState2);
            if (o <= j) continue;
            if (blockState == null) {
                BlockState blockState3 = blockState = LightEngine.QueueEntry.isFromEmptyShape(m) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(l));
            }
            if (this.shapeOccludes(blockState, blockState2, direction)) continue;
            ((BlockLightSectionStorage)this.storage).setStoredLevel(n, o);
            if (o <= 1) continue;
            this.enqueueIncrease(n, LightEngine.QueueEntry.increaseSkipOneDirection(o, BlockLightEngine.isEmptyShape(blockState2), direction.getOpposite()));
        }
    }

    @Override
    protected void propagateDecrease(long l, long m) {
        int i = LightEngine.QueueEntry.getFromLevel(m);
        for (Direction direction : PROPAGATION_DIRECTIONS) {
            int j;
            long n;
            if (!LightEngine.QueueEntry.shouldPropagateInDirection(m, direction) || !((BlockLightSectionStorage)this.storage).storingLightForSection(SectionPos.blockToSection(n = BlockPos.offset(l, direction))) || (j = ((BlockLightSectionStorage)this.storage).getStoredLevel(n)) == 0) continue;
            if (j <= i - 1) {
                BlockState blockState = this.getState(this.mutablePos.set(n));
                int k = this.getEmission(n, blockState);
                ((BlockLightSectionStorage)this.storage).setStoredLevel(n, 0);
                if (k < j) {
                    this.enqueueDecrease(n, LightEngine.QueueEntry.decreaseSkipOneDirection(j, direction.getOpposite()));
                }
                if (k <= 0) continue;
                this.enqueueIncrease(n, LightEngine.QueueEntry.increaseLightFromEmission(k, BlockLightEngine.isEmptyShape(blockState)));
                continue;
            }
            this.enqueueIncrease(n, LightEngine.QueueEntry.increaseOnlyOneDirection(j, false, direction.getOpposite()));
        }
    }

    private int getEmission(long l, BlockState blockState) {
        int i = blockState.getLightEmission();
        if (i > 0 && ((BlockLightSectionStorage)this.storage).lightOnInSection(SectionPos.blockToSection(l))) {
            return i;
        }
        return 0;
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        this.setLightEnabled(chunkPos, true);
        LightChunk lightChunk = this.chunkSource.getChunkForLighting(chunkPos.x, chunkPos.z);
        if (lightChunk != null) {
            lightChunk.findBlockLightSources((blockPos, blockState) -> {
                int i = blockState.getLightEmission();
                this.enqueueIncrease(blockPos.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(i, BlockLightEngine.isEmptyShape(blockState)));
            });
        }
    }
}

