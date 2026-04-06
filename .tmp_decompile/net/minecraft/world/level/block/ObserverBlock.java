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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.ticks.LevelTicks;

public class ObserverBlock
extends DirectionalBlock {
    public static final MapCodec<ObserverBlock> CODEC = ObserverBlock.simpleCodec(ObserverBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MapCodec<ObserverBlock> codec() {
        return CODEC;
    }

    public ObserverBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.SOUTH)).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(POWERED).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, false), 2);
        } else {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, true), 2);
            serverLevel.scheduleTick(blockPos, this, 2);
        }
        this.updateNeighborsInFront(serverLevel, blockPos, blockState);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(FACING) == direction && !blockState.getValue(POWERED).booleanValue()) {
            this.startSignal(levelReader, scheduledTickAccess, blockPos);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    private void startSignal(LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos) {
        if (!levelReader.isClientSide() && !scheduledTickAccess.getBlockTicks().hasScheduledTick(blockPos, this)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 2);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction.getOpposite(), null);
        level.neighborChanged(blockPos2, this, orientation);
        level.updateNeighborsAtExceptFromFacing(blockPos2, this, direction, orientation);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(POWERED).booleanValue() && blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (!level.isClientSide() && blockState.getValue(POWERED).booleanValue() && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            BlockState blockState3 = (BlockState)blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState3, 18);
            this.updateNeighborsInFront(level, blockPos, blockState3);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (blockState.getValue(POWERED).booleanValue() && ((LevelTicks)serverLevel.getBlockTicks()).hasScheduledTick(blockPos, this)) {
            this.updateNeighborsInFront(serverLevel, blockPos, (BlockState)blockState.setValue(POWERED, false));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite().getOpposite());
    }
}

