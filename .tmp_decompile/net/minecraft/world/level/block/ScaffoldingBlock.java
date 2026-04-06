/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldingBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final MapCodec<ScaffoldingBlock> CODEC = ScaffoldingBlock.simpleCodec(ScaffoldingBlock::new);
    private static final int TICK_DELAY = 1;
    private static final VoxelShape SHAPE_STABLE = Shapes.or(Block.column(16.0, 14.0, 16.0), Shapes.rotateHorizontal(Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0)).values().stream().reduce(Shapes.empty(), Shapes::or));
    private static final VoxelShape SHAPE_UNSTABLE_BOTTOM = Block.column(16.0, 0.0, 2.0);
    private static final VoxelShape SHAPE_UNSTABLE = Shapes.or(SHAPE_STABLE, SHAPE_UNSTABLE_BOTTOM, Shapes.rotateHorizontal(Block.boxZ(16.0, 0.0, 2.0, 0.0, 2.0)).values().stream().reduce(Shapes.empty(), Shapes::or));
    private static final VoxelShape SHAPE_BELOW_BLOCK = Shapes.block().move(0.0, -1.0, 0.0).optimize();
    public static final int STABILITY_MAX_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;

    public MapCodec<ScaffoldingBlock> codec() {
        return CODEC;
    }

    protected ScaffoldingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(DISTANCE, 7)).setValue(WATERLOGGED, false)).setValue(BOTTOM, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, WATERLOGGED, BOTTOM);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!collisionContext.isHoldingItem(blockState.getBlock().asItem())) {
            return blockState.getValue(BOTTOM) != false ? SHAPE_UNSTABLE : SHAPE_STABLE;
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.block();
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext.getItemInHand().is(this.asItem());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        int i = ScaffoldingBlock.getDistance(level, blockPos);
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, level.getFluidState(blockPos).getType() == Fluids.WATER)).setValue(DISTANCE, i)).setValue(BOTTOM, this.isBottom(level, blockPos, i));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!level.isClientSide()) {
            level.scheduleTick(blockPos, this, 1);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (!levelReader.isClientSide()) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int i = ScaffoldingBlock.getDistance(serverLevel, blockPos);
        BlockState blockState2 = (BlockState)((BlockState)blockState.setValue(DISTANCE, i)).setValue(BOTTOM, this.isBottom(serverLevel, blockPos, i));
        if (blockState2.getValue(DISTANCE) == 7) {
            if (blockState.getValue(DISTANCE) == 7) {
                FallingBlockEntity.fall(serverLevel, blockPos, blockState2);
            } else {
                serverLevel.destroyBlock(blockPos, true);
            }
        } else if (blockState != blockState2) {
            serverLevel.setBlock(blockPos, blockState2, 3);
        }
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return ScaffoldingBlock.getDistance(levelReader, blockPos) < 7;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (collisionContext.isPlacement()) {
            return Shapes.empty();
        }
        if (!collisionContext.isAbove(Shapes.block(), blockPos, true) || collisionContext.isDescending()) {
            if (blockState.getValue(DISTANCE) != 0 && blockState.getValue(BOTTOM).booleanValue() && collisionContext.isAbove(SHAPE_BELOW_BLOCK, blockPos, true)) {
                return SHAPE_UNSTABLE_BOTTOM;
            }
            return Shapes.empty();
        }
        return SHAPE_STABLE;
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    private boolean isBottom(BlockGetter blockGetter, BlockPos blockPos, int i) {
        return i > 0 && !blockGetter.getBlockState(blockPos.below()).is(this);
    }

    public static int getDistance(BlockGetter blockGetter, BlockPos blockPos) {
        Direction direction;
        BlockState blockState2;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
        BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
        int i = 7;
        if (blockState.is(Blocks.SCAFFOLDING)) {
            i = blockState.getValue(DISTANCE);
        } else if (blockState.isFaceSturdy(blockGetter, mutableBlockPos, Direction.UP)) {
            return 0;
        }
        Iterator<Direction> iterator = Direction.Plane.HORIZONTAL.iterator();
        while (iterator.hasNext() && (!(blockState2 = blockGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, direction = iterator.next()))).is(Blocks.SCAFFOLDING) || (i = Math.min(i, blockState2.getValue(DISTANCE) + 1)) != 1)) {
        }
        return i;
    }
}

