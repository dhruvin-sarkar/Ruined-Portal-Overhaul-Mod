/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public abstract class RedstoneWireEvaluator {
    protected final RedStoneWireBlock wireBlock;

    protected RedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
        this.wireBlock = redStoneWireBlock;
    }

    public abstract void updatePowerStrength(Level var1, BlockPos var2, BlockState var3, @Nullable Orientation var4, boolean var5);

    protected int getBlockSignal(Level level, BlockPos blockPos) {
        return this.wireBlock.getBlockSignal(level, blockPos);
    }

    protected int getWireSignal(BlockPos blockPos, BlockState blockState) {
        return blockState.is(this.wireBlock) ? blockState.getValue(RedStoneWireBlock.POWER) : 0;
    }

    protected int getIncomingWireSignal(Level level, BlockPos blockPos) {
        int i = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos4;
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos2);
            i = Math.max(i, this.getWireSignal(blockPos2, blockState));
            BlockPos blockPos3 = blockPos.above();
            if (blockState.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
                blockPos4 = blockPos2.above();
                i = Math.max(i, this.getWireSignal(blockPos4, level.getBlockState(blockPos4)));
                continue;
            }
            if (blockState.isRedstoneConductor(level, blockPos2)) continue;
            blockPos4 = blockPos2.below();
            i = Math.max(i, this.getWireSignal(blockPos4, level.getBlockState(blockPos4)));
        }
        return Math.max(0, i - 1);
    }
}

