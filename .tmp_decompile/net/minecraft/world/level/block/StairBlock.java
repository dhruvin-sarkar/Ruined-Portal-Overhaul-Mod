/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.MatchException
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final MapCodec<StairBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockState.CODEC.fieldOf("base_state").forGetter(stairBlock -> stairBlock.baseState), StairBlock.propertiesCodec()).apply((Applicative)instance, StairBlock::new));
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_OUTER = Shapes.or(Block.column(16.0, 0.0, 8.0), Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0));
    private static final VoxelShape SHAPE_STRAIGHT = Shapes.or(SHAPE_OUTER, Shapes.rotate(SHAPE_OUTER, OctahedralGroup.BLOCK_ROT_Y_90));
    private static final VoxelShape SHAPE_INNER = Shapes.or(SHAPE_STRAIGHT, Shapes.rotate(SHAPE_STRAIGHT, OctahedralGroup.BLOCK_ROT_Y_90));
    private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_OUTER = Shapes.rotateHorizontal(SHAPE_OUTER);
    private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_STRAIGHT = Shapes.rotateHorizontal(SHAPE_STRAIGHT);
    private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_INNER = Shapes.rotateHorizontal(SHAPE_INNER);
    private static final Map<Direction, VoxelShape> SHAPE_TOP_OUTER = Shapes.rotateHorizontal(SHAPE_OUTER, OctahedralGroup.INVERT_Y);
    private static final Map<Direction, VoxelShape> SHAPE_TOP_STRAIGHT = Shapes.rotateHorizontal(SHAPE_STRAIGHT, OctahedralGroup.INVERT_Y);
    private static final Map<Direction, VoxelShape> SHAPE_TOP_INNER = Shapes.rotateHorizontal(SHAPE_INNER, OctahedralGroup.INVERT_Y);
    private final Block base;
    protected final BlockState baseState;

    public MapCodec<? extends StairBlock> codec() {
        return CODEC;
    }

    protected StairBlock(BlockState blockState, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(HALF, Half.BOTTOM)).setValue(SHAPE, StairsShape.STRAIGHT)).setValue(WATERLOGGED, false));
        this.base = blockState.getBlock();
        this.baseState = blockState;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        boolean bl = blockState.getValue(HALF) == Half.BOTTOM;
        Direction direction = blockState.getValue(FACING);
        return (switch (blockState.getValue(SHAPE)) {
            default -> throw new MatchException(null, null);
            case StairsShape.STRAIGHT -> {
                if (bl) {
                    yield SHAPE_BOTTOM_STRAIGHT;
                }
                yield SHAPE_TOP_STRAIGHT;
            }
            case StairsShape.INNER_RIGHT, StairsShape.INNER_LEFT -> {
                if (bl) {
                    yield SHAPE_BOTTOM_INNER;
                }
                yield SHAPE_TOP_INNER;
            }
            case StairsShape.OUTER_LEFT, StairsShape.OUTER_RIGHT -> bl ? SHAPE_BOTTOM_OUTER : SHAPE_TOP_OUTER;
        }).get(switch (blockState.getValue(SHAPE)) {
            default -> throw new MatchException(null, null);
            case StairsShape.STRAIGHT, StairsShape.OUTER_LEFT, StairsShape.INNER_RIGHT -> direction;
            case StairsShape.INNER_LEFT -> direction.getCounterClockWise();
            case StairsShape.OUTER_RIGHT -> direction.getClockWise();
        });
    }

    @Override
    public float getExplosionResistance() {
        return this.base.getExplosionResistance();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
        BlockState blockState = (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection())).setValue(HALF, direction == Direction.DOWN || direction != Direction.UP && blockPlaceContext.getClickLocation().y - (double)blockPos.getY() > 0.5 ? Half.TOP : Half.BOTTOM)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return (BlockState)blockState.setValue(SHAPE, StairBlock.getStairsShape(blockState, blockPlaceContext.getLevel(), blockPos));
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)blockState.setValue(SHAPE, StairBlock.getStairsShape(blockState, levelReader, blockPos));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    private static StairsShape getStairsShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        Direction direction3;
        Direction direction2;
        Direction direction = blockState.getValue(FACING);
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        if (StairBlock.isStairs(blockState2) && blockState.getValue(HALF) == blockState2.getValue(HALF) && (direction2 = blockState2.getValue(FACING)).getAxis() != blockState.getValue(FACING).getAxis() && StairBlock.canTakeShape(blockState, blockGetter, blockPos, direction2.getOpposite())) {
            if (direction2 == direction.getCounterClockWise()) {
                return StairsShape.OUTER_LEFT;
            }
            return StairsShape.OUTER_RIGHT;
        }
        BlockState blockState3 = blockGetter.getBlockState(blockPos.relative(direction.getOpposite()));
        if (StairBlock.isStairs(blockState3) && blockState.getValue(HALF) == blockState3.getValue(HALF) && (direction3 = blockState3.getValue(FACING)).getAxis() != blockState.getValue(FACING).getAxis() && StairBlock.canTakeShape(blockState, blockGetter, blockPos, direction3)) {
            if (direction3 == direction.getCounterClockWise()) {
                return StairsShape.INNER_LEFT;
            }
            return StairsShape.INNER_RIGHT;
        }
        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        return !StairBlock.isStairs(blockState2) || blockState2.getValue(FACING) != blockState.getValue(FACING) || blockState2.getValue(HALF) != blockState.getValue(HALF);
    }

    public static boolean isStairs(BlockState blockState) {
        return blockState.getBlock() instanceof StairBlock;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        Direction direction = blockState.getValue(FACING);
        StairsShape stairsShape = blockState.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT: {
                if (direction.getAxis() != Direction.Axis.Z) break;
                switch (stairsShape) {
                    case INNER_LEFT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                    }
                    case INNER_RIGHT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                    }
                    case OUTER_LEFT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                    }
                    case OUTER_RIGHT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                    }
                }
                return blockState.rotate(Rotation.CLOCKWISE_180);
            }
            case FRONT_BACK: {
                if (direction.getAxis() != Direction.Axis.X) break;
                switch (stairsShape) {
                    case INNER_LEFT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                    }
                    case INNER_RIGHT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                    }
                    case OUTER_LEFT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                    }
                    case OUTER_RIGHT: {
                        return (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                    }
                    case STRAIGHT: {
                        return blockState.rotate(Rotation.CLOCKWISE_180);
                    }
                }
                break;
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

