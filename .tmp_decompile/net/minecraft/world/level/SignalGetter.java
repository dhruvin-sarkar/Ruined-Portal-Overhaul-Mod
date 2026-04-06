/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter
extends BlockGetter {
    public static final Direction[] DIRECTIONS = Direction.values();

    default public int getDirectSignal(BlockPos blockPos, Direction direction) {
        return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
    }

    default public int getDirectSignalTo(BlockPos blockPos) {
        int i = 0;
        if ((i = Math.max(i, this.getDirectSignal(blockPos.below(), Direction.DOWN))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.above(), Direction.UP))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    default public int getControlInputSignal(BlockPos blockPos, Direction direction, boolean bl) {
        BlockState blockState = this.getBlockState(blockPos);
        if (bl) {
            return DiodeBlock.isDiode(blockState) ? this.getDirectSignal(blockPos, direction) : 0;
        }
        if (blockState.is(Blocks.REDSTONE_BLOCK)) {
            return 15;
        }
        if (blockState.is(Blocks.REDSTONE_WIRE)) {
            return blockState.getValue(RedStoneWireBlock.POWER);
        }
        if (blockState.isSignalSource()) {
            return this.getDirectSignal(blockPos, direction);
        }
        return 0;
    }

    default public boolean hasSignal(BlockPos blockPos, Direction direction) {
        return this.getSignal(blockPos, direction) > 0;
    }

    default public int getSignal(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.getBlockState(blockPos);
        int i = blockState.getSignal(this, blockPos, direction);
        if (blockState.isRedstoneConductor(this, blockPos)) {
            return Math.max(i, this.getDirectSignalTo(blockPos));
        }
        return i;
    }

    default public boolean hasNeighborSignal(BlockPos blockPos) {
        if (this.getSignal(blockPos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignal(blockPos.east(), Direction.EAST) > 0;
    }

    default public int getBestNeighborSignal(BlockPos blockPos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getSignal(blockPos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }
}

