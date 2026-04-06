/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MultifaceBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final MapCodec<MultifaceBlock> CODEC = MultifaceBlock.simpleCodec(MultifaceBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final Function<BlockState, VoxelShape> shapes;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    public MultifaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(MultifaceBlock.getDefaultMultifaceState(this.stateDefinition));
        this.shapes = this.makeShapes();
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
        return this.getShapeForEachState(blockState -> {
            VoxelShape voxelShape = Shapes.empty();
            for (Direction direction : DIRECTIONS) {
                if (!MultifaceBlock.hasFace(blockState, direction)) continue;
                voxelShape = Shapes.or(voxelShape, (VoxelShape)map.get(direction));
            }
            return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
        }, WATERLOGGED);
    }

    public static Set<Direction> availableFaces(BlockState blockState) {
        if (!(blockState.getBlock() instanceof MultifaceBlock)) {
            return Set.of();
        }
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            set.add(direction);
        }
        return set;
    }

    public static Set<Direction> unpack(byte b) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if ((b & (byte)(1 << direction.ordinal())) <= 0) continue;
            set.add(direction);
        }
        return set;
    }

    public static byte pack(Collection<Direction> collection) {
        byte b = 0;
        for (Direction direction : collection) {
            b = (byte)(b | 1 << direction.ordinal());
        }
        return b;
    }

    protected boolean isFaceSupported(Direction direction) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction direction : DIRECTIONS) {
            if (!this.isFaceSupported(direction)) continue;
            builder.add(MultifaceBlock.getFaceProperty(direction));
        }
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (!MultifaceBlock.hasAnyFace(blockState)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!MultifaceBlock.hasFace(blockState, direction) || MultifaceBlock.canAttachTo(levelReader, direction, blockPos2, blockState2)) {
            return blockState;
        }
        return MultifaceBlock.removeFace(blockState, MultifaceBlock.getFaceProperty(direction));
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        boolean bl = false;
        for (Direction direction : DIRECTIONS) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            if (!MultifaceBlock.canAttachTo(levelReader, blockPos, direction)) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return !blockPlaceContext.getItemInHand().is(this.asItem()) || MultifaceBlock.hasAnyVacantFace(blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        return Arrays.stream(blockPlaceContext.getNearestLookingDirections()).map(direction -> this.getStateForPlacement(blockState, level, blockPos, (Direction)direction)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean isValidStateForPlacement(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, Direction direction) {
        if (!this.isFaceSupported(direction) || blockState.is(this) && MultifaceBlock.hasFace(blockState, direction)) {
            return false;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        return MultifaceBlock.canAttachTo(blockGetter, direction, blockPos2, blockGetter.getBlockState(blockPos2));
    }

    public @Nullable BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.isValidStateForPlacement(blockGetter, blockState, blockPos, direction)) {
            return null;
        }
        BlockState blockState2 = blockState.is(this) ? blockState : (blockState.getFluidState().isSourceOfType(Fluids.WATER) ? (BlockState)this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true) : this.defaultBlockState());
        return (BlockState)blockState2.setValue(MultifaceBlock.getFaceProperty(direction), true);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        if (!this.canRotate) {
            return blockState;
        }
        return this.mapDirections(blockState, rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        if (mirror == Mirror.FRONT_BACK && !this.canMirrorX) {
            return blockState;
        }
        if (mirror == Mirror.LEFT_RIGHT && !this.canMirrorZ) {
            return blockState;
        }
        return this.mapDirections(blockState, mirror::mirror);
    }

    private BlockState mapDirections(BlockState blockState, Function<Direction, Direction> function) {
        BlockState blockState2 = blockState;
        for (Direction direction : DIRECTIONS) {
            if (!this.isFaceSupported(direction)) continue;
            blockState2 = (BlockState)blockState2.setValue(MultifaceBlock.getFaceProperty(function.apply(direction)), blockState.getValue(MultifaceBlock.getFaceProperty(direction)));
        }
        return blockState2;
    }

    public static boolean hasFace(BlockState blockState, Direction direction) {
        BooleanProperty booleanProperty = MultifaceBlock.getFaceProperty(direction);
        return blockState.getValueOrElse(booleanProperty, false);
    }

    public static boolean canAttachTo(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        return MultifaceBlock.canAttachTo(blockGetter, direction, blockPos2, blockState);
    }

    public static boolean canAttachTo(BlockGetter blockGetter, Direction direction, BlockPos blockPos, BlockState blockState) {
        return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction.getOpposite()) || Block.isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction.getOpposite());
    }

    private static BlockState removeFace(BlockState blockState, BooleanProperty booleanProperty) {
        BlockState blockState2 = (BlockState)blockState.setValue(booleanProperty, false);
        if (MultifaceBlock.hasAnyFace(blockState2)) {
            return blockState2;
        }
        return Blocks.AIR.defaultBlockState();
    }

    public static BooleanProperty getFaceProperty(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> stateDefinition) {
        BlockState blockState = (BlockState)stateDefinition.any().setValue(WATERLOGGED, false);
        for (BooleanProperty booleanProperty : PROPERTY_BY_DIRECTION.values()) {
            blockState = (BlockState)blockState.trySetValue(booleanProperty, false);
        }
        return blockState;
    }

    protected static boolean hasAnyFace(BlockState blockState) {
        for (Direction direction : DIRECTIONS) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            return true;
        }
        return false;
    }

    private static boolean hasAnyVacantFace(BlockState blockState) {
        for (Direction direction : DIRECTIONS) {
            if (MultifaceBlock.hasFace(blockState, direction)) continue;
            return true;
        }
        return false;
    }
}

