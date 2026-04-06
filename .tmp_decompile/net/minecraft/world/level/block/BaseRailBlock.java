/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BaseRailBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_FLAT = Block.column(16.0, 0.0, 2.0);
    private static final VoxelShape SHAPE_SLOPE = Block.column(16.0, 0.0, 8.0);
    private final boolean isStraight;

    public static boolean isRail(Level level, BlockPos blockPos) {
        return BaseRailBlock.isRail(level.getBlockState(blockPos));
    }

    public static boolean isRail(BlockState blockState) {
        return blockState.is(BlockTags.RAILS) && blockState.getBlock() instanceof BaseRailBlock;
    }

    protected BaseRailBlock(boolean bl, BlockBehaviour.Properties properties) {
        super(properties);
        this.isStraight = bl;
    }

    protected abstract MapCodec<? extends BaseRailBlock> codec();

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(this.getShapeProperty()).isSlope() ? SHAPE_SLOPE : SHAPE_FLAT;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return BaseRailBlock.canSupportRigidBlock(levelReader, blockPos.below());
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        this.updateState(blockState, level, blockPos, bl);
    }

    protected BlockState updateState(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        blockState = this.updateDir(level, blockPos, blockState, true);
        if (this.isStraight) {
            level.neighborChanged(blockState, blockPos, this, null, bl);
        }
        return blockState;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.isClientSide() || !level.getBlockState(blockPos).is(this)) {
            return;
        }
        RailShape railShape = blockState.getValue(this.getShapeProperty());
        if (BaseRailBlock.shouldBeRemoved(blockPos, level, railShape)) {
            BaseRailBlock.dropResources(blockState, level, blockPos);
            level.removeBlock(blockPos, bl);
        } else {
            this.updateState(blockState, level, blockPos, block);
        }
    }

    private static boolean shouldBeRemoved(BlockPos blockPos, Level level, RailShape railShape) {
        if (!BaseRailBlock.canSupportRigidBlock(level, blockPos.below())) {
            return true;
        }
        switch (railShape) {
            case ASCENDING_EAST: {
                return !BaseRailBlock.canSupportRigidBlock(level, blockPos.east());
            }
            case ASCENDING_WEST: {
                return !BaseRailBlock.canSupportRigidBlock(level, blockPos.west());
            }
            case ASCENDING_NORTH: {
                return !BaseRailBlock.canSupportRigidBlock(level, blockPos.north());
            }
            case ASCENDING_SOUTH: {
                return !BaseRailBlock.canSupportRigidBlock(level, blockPos.south());
            }
        }
        return false;
    }

    protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
    }

    protected BlockState updateDir(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        if (level.isClientSide()) {
            return blockState;
        }
        RailShape railShape = blockState.getValue(this.getShapeProperty());
        return new RailState(level, blockPos, blockState).place(level.hasNeighborSignal(blockPos), bl, railShape).getState();
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (bl) {
            return;
        }
        if (blockState.getValue(this.getShapeProperty()).isSlope()) {
            serverLevel.updateNeighborsAt(blockPos.above(), this);
        }
        if (this.isStraight) {
            serverLevel.updateNeighborsAt(blockPos, this);
            serverLevel.updateNeighborsAt(blockPos.below(), this);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        BlockState blockState = super.defaultBlockState();
        Direction direction = blockPlaceContext.getHorizontalDirection();
        boolean bl2 = direction == Direction.EAST || direction == Direction.WEST;
        return (BlockState)((BlockState)blockState.setValue(this.getShapeProperty(), bl2 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH)).setValue(WATERLOGGED, bl);
    }

    public abstract Property<RailShape> getShapeProperty();

    protected RailShape rotate(RailShape railShape, Rotation rotation) {
        return switch (rotation) {
            case Rotation.CLOCKWISE_180 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case EAST_WEST: {
                        yield RailShape.EAST_WEST;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_WEST;
            }
            case Rotation.COUNTERCLOCKWISE_90 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.NORTH_WEST;
            }
            case Rotation.CLOCKWISE_90 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_EAST;
            }
            default -> railShape;
        };
    }

    protected RailShape mirror(RailShape railShape, Mirror mirror) {
        return switch (mirror) {
            case Mirror.LEFT_RIGHT -> {
                switch (railShape) {
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case NORTH_EAST: {
                        yield RailShape.SOUTH_EAST;
                    }
                }
                yield railShape;
            }
            case Mirror.FRONT_BACK -> {
                switch (railShape) {
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_EAST: {
                        yield RailShape.NORTH_WEST;
                    }
                }
                yield railShape;
            }
            default -> railShape;
        };
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }
}

