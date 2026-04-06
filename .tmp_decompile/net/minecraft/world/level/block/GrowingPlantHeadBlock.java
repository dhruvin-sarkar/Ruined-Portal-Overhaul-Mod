/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantHeadBlock
extends GrowingPlantBlock
implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    public static final int MAX_AGE = 25;
    private final double growPerTickProbability;

    protected GrowingPlantHeadBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl, double d) {
        super(properties, direction, voxelShape, bl);
        this.growPerTickProbability = d;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    protected abstract MapCodec<? extends GrowingPlantHeadBlock> codec();

    @Override
    public BlockState getStateForPlacement(RandomSource randomSource) {
        return (BlockState)this.defaultBlockState().setValue(AGE, randomSource.nextInt(25));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGE) < 25;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2;
        if (blockState.getValue(AGE) < 25 && randomSource.nextDouble() < this.growPerTickProbability && this.canGrowInto(serverLevel.getBlockState(blockPos2 = blockPos.relative(this.growthDirection)))) {
            serverLevel.setBlockAndUpdate(blockPos2, this.getGrowIntoState(blockState, serverLevel.random));
        }
    }

    protected BlockState getGrowIntoState(BlockState blockState, RandomSource randomSource) {
        return (BlockState)blockState.cycle(AGE);
    }

    public BlockState getMaxAgeState(BlockState blockState) {
        return (BlockState)blockState.setValue(AGE, 25);
    }

    public boolean isMaxAge(BlockState blockState) {
        return blockState.getValue(AGE) == 25;
    }

    protected BlockState updateBodyAfterConvertedFromHead(BlockState blockState, BlockState blockState2) {
        return blockState2;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == this.growthDirection.getOpposite()) {
            if (!blockState.canSurvive(levelReader, blockPos)) {
                scheduledTickAccess.scheduleTick(blockPos, this, 1);
            } else {
                BlockState blockState3 = levelReader.getBlockState(blockPos.relative(this.growthDirection));
                if (blockState3.is(this) || blockState3.is(this.getBodyBlock())) {
                    return this.updateBodyAfterConvertedFromHead(blockState, this.getBodyBlock().defaultBlockState());
                }
            }
        }
        if (direction == this.growthDirection && (blockState2.is(this) || blockState2.is(this.getBodyBlock()))) {
            return this.updateBodyAfterConvertedFromHead(blockState, this.getBodyBlock().defaultBlockState());
        }
        if (this.scheduleFluidTicks) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return this.canGrowInto(levelReader.getBlockState(blockPos.relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.relative(this.growthDirection);
        int i = Math.min(blockState.getValue(AGE) + 1, 25);
        int j = this.getBlocksToGrowWhenBonemealed(randomSource);
        for (int k = 0; k < j && this.canGrowInto(serverLevel.getBlockState(blockPos2)); ++k) {
            serverLevel.setBlockAndUpdate(blockPos2, (BlockState)blockState.setValue(AGE, i));
            blockPos2 = blockPos2.relative(this.growthDirection);
            i = Math.min(i + 1, 25);
        }
    }

    protected abstract int getBlocksToGrowWhenBonemealed(RandomSource var1);

    protected abstract boolean canGrowInto(BlockState var1);

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}

