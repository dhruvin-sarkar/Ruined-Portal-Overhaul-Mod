/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.piston;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PistonHeadBlock
extends DirectionalBlock {
    public static final MapCodec<PistonHeadBlock> CODEC = PistonHeadBlock.simpleCodec(PistonHeadBlock::new);
    public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
    public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
    public static final int PLATFORM_THICKNESS = 4;
    private static final VoxelShape SHAPE_PLATFORM = Block.boxZ(16.0, 0.0, 4.0);
    private static final Map<Direction, VoxelShape> SHAPES_SHORT = Shapes.rotateAll(Shapes.or(SHAPE_PLATFORM, Block.boxZ(4.0, 4.0, 16.0)));
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Shapes.or(SHAPE_PLATFORM, Block.boxZ(4.0, 4.0, 20.0)));

    protected MapCodec<PistonHeadBlock> codec() {
        return CODEC;
    }

    public PistonHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(TYPE, PistonType.DEFAULT)).setValue(SHORT, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return (blockState.getValue(SHORT) != false ? SHAPES_SHORT : SHAPES).get(blockState.getValue(FACING));
    }

    private boolean isFittingBase(BlockState blockState, BlockState blockState2) {
        Block block = blockState.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
        return blockState2.is(block) && blockState2.getValue(PistonBaseBlock.EXTENDED) != false && blockState2.getValue(FACING) == blockState.getValue(FACING);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockPos blockPos2;
        if (!level.isClientSide() && player.preventsBlockDrops() && this.isFittingBase(blockState, level.getBlockState(blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite())))) {
            level.destroyBlock(blockPos2, false);
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        BlockPos blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
        if (this.isFittingBase(blockState, serverLevel.getBlockState(blockPos2))) {
            serverLevel.destroyBlock(blockPos2, true);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite()));
        return this.isFittingBase(blockState, blockState2) || blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == blockState.getValue(FACING);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (blockState.canSurvive(level, blockPos)) {
            level.neighborChanged(blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite()), block, ExperimentalRedstoneUtils.withFront(orientation, ((Direction)blockState.getValue(FACING)).getOpposite()));
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        return new ItemStack(blockState.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, SHORT);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

