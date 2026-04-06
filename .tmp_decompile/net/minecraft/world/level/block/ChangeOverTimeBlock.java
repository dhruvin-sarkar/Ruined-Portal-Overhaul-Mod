/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    public static final int SCAN_DISTANCE = 4;

    public Optional<BlockState> getNext(BlockState var1);

    public float getChanceModifier();

    default public void changeOverTime(BlockState blockState2, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        float f = 0.05688889f;
        if (randomSource.nextFloat() < 0.05688889f) {
            this.getNextState(blockState2, serverLevel, blockPos, randomSource).ifPresent(blockState -> serverLevel.setBlockAndUpdate(blockPos, (BlockState)blockState));
        }
    }

    public T getAge();

    default public Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2;
        int l;
        int i = ((Enum)this.getAge()).ordinal();
        int j = 0;
        int k = 0;
        Iterator<BlockPos> iterator = BlockPos.withinManhattan(blockPos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (l = (blockPos2 = iterator.next()).distManhattan(blockPos)) <= 4) {
            Block block;
            if (blockPos2.equals(blockPos) || !((block = serverLevel.getBlockState(blockPos2).getBlock()) instanceof ChangeOverTimeBlock)) continue;
            ChangeOverTimeBlock changeOverTimeBlock = (ChangeOverTimeBlock)((Object)block);
            T enum_ = changeOverTimeBlock.getAge();
            if (this.getAge().getClass() != enum_.getClass()) continue;
            int m = ((Enum)enum_).ordinal();
            if (m < i) {
                return Optional.empty();
            }
            if (m > i) {
                ++k;
                continue;
            }
            ++j;
        }
        float f = (float)(k + 1) / (float)(k + j + 1);
        float g = f * f * this.getChanceModifier();
        if (randomSource.nextFloat() < g) {
            return this.getNext(blockState);
        }
        return Optional.empty();
    }
}

