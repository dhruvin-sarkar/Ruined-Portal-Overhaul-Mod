/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.redstone;

import com.google.common.collect.Sets;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.jspecify.annotations.Nullable;

public class DefaultRedstoneWireEvaluator
extends RedstoneWireEvaluator {
    public DefaultRedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
        super(redStoneWireBlock);
    }

    @Override
    public void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
        int i = this.calculateTargetStrength(level, blockPos);
        if (blockState.getValue(RedStoneWireBlock.POWER) != i) {
            if (level.getBlockState(blockPos) == blockState) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(RedStoneWireBlock.POWER, i), 2);
            }
            HashSet set = Sets.newHashSet();
            set.add(blockPos);
            for (Direction direction : Direction.values()) {
                set.add(blockPos.relative(direction));
            }
            for (BlockPos blockPos2 : set) {
                level.updateNeighborsAt(blockPos2, this.wireBlock);
            }
        }
    }

    private int calculateTargetStrength(Level level, BlockPos blockPos) {
        int i = this.getBlockSignal(level, blockPos);
        if (i == 15) {
            return i;
        }
        return Math.max(i, this.getIncomingWireSignal(level, blockPos));
    }
}

