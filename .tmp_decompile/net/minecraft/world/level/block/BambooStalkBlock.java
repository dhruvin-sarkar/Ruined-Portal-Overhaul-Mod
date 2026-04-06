/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BambooStalkBlock
extends Block
implements BonemealableBlock {
    public static final MapCodec<BambooStalkBlock> CODEC = BambooStalkBlock.simpleCodec(BambooStalkBlock::new);
    private static final VoxelShape SHAPE_SMALL = Block.column(6.0, 0.0, 16.0);
    private static final VoxelShape SHAPE_LARGE = Block.column(10.0, 0.0, 16.0);
    private static final VoxelShape SHAPE_COLLISION = Block.column(3.0, 0.0, 16.0);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    public static final int MAX_HEIGHT = 16;
    public static final int STAGE_GROWING = 0;
    public static final int STAGE_DONE_GROWING = 1;
    public static final int AGE_THIN_BAMBOO = 0;
    public static final int AGE_THICK_BAMBOO = 1;

    public MapCodec<BambooStalkBlock> codec() {
        return CODEC;
    }

    public BambooStalkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0)).setValue(LEAVES, BambooLeaves.NONE)).setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, LEAVES, STAGE);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getValue(LEAVES) == BambooLeaves.LARGE ? SHAPE_LARGE : SHAPE_SMALL;
        return voxelShape.move(blockState.getOffset(blockPos));
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION.move(blockState.getOffset(blockPos));
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        if (!fluidState.isEmpty()) {
            return null;
        }
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below());
        if (blockState.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockState.is(Blocks.BAMBOO_SAPLING)) {
                return (BlockState)this.defaultBlockState().setValue(AGE, 0);
            }
            if (blockState.is(Blocks.BAMBOO)) {
                int i = blockState.getValue(AGE) > 0 ? 1 : 0;
                return (BlockState)this.defaultBlockState().setValue(AGE, i);
            }
            BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
            if (blockState2.is(Blocks.BAMBOO)) {
                return (BlockState)this.defaultBlockState().setValue(AGE, blockState2.getValue(AGE));
            }
            return Blocks.BAMBOO_SAPLING.defaultBlockState();
        }
        return null;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(STAGE) == 0;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int i;
        if (blockState.getValue(STAGE) != 0) {
            return;
        }
        if (randomSource.nextInt(3) == 0 && serverLevel.isEmptyBlock(blockPos.above()) && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9 && (i = this.getHeightBelowUpToMax(serverLevel, blockPos) + 1) < 16) {
            this.growBamboo(blockState, serverLevel, blockPos, randomSource, i);
        }
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (!blockState.canSurvive(levelReader, blockPos)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        if (direction == Direction.UP && blockState2.is(Blocks.BAMBOO) && blockState2.getValue(AGE) > blockState.getValue(AGE)) {
            return (BlockState)blockState.cycle(AGE);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        int j;
        int i = this.getHeightAboveUpToMax(levelReader, blockPos);
        return i + (j = this.getHeightBelowUpToMax(levelReader, blockPos)) + 1 < 16 && levelReader.getBlockState(blockPos.above(i)).getValue(STAGE) != 1;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        int i = this.getHeightAboveUpToMax(serverLevel, blockPos);
        int j = this.getHeightBelowUpToMax(serverLevel, blockPos);
        int k = i + j + 1;
        int l = 1 + randomSource.nextInt(2);
        for (int m = 0; m < l; ++m) {
            BlockPos blockPos2 = blockPos.above(i);
            BlockState blockState2 = serverLevel.getBlockState(blockPos2);
            if (k >= 16 || blockState2.getValue(STAGE) == 1 || !serverLevel.isEmptyBlock(blockPos2.above())) {
                return;
            }
            this.growBamboo(blockState2, serverLevel, blockPos2, randomSource, k);
            ++i;
            ++k;
        }
    }

    protected void growBamboo(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource, int i) {
        BlockState blockState2 = level.getBlockState(blockPos.below());
        BlockPos blockPos2 = blockPos.below(2);
        BlockState blockState3 = level.getBlockState(blockPos2);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (i >= 1) {
            if (!blockState2.is(Blocks.BAMBOO) || blockState2.getValue(LEAVES) == BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.SMALL;
            } else if (blockState2.is(Blocks.BAMBOO) && blockState2.getValue(LEAVES) != BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.LARGE;
                if (blockState3.is(Blocks.BAMBOO)) {
                    level.setBlock(blockPos.below(), (BlockState)blockState2.setValue(LEAVES, BambooLeaves.SMALL), 3);
                    level.setBlock(blockPos2, (BlockState)blockState3.setValue(LEAVES, BambooLeaves.NONE), 3);
                }
            }
        }
        int j = blockState.getValue(AGE) == 1 || blockState3.is(Blocks.BAMBOO) ? 1 : 0;
        int k = i >= 11 && randomSource.nextFloat() < 0.25f || i == 15 ? 1 : 0;
        level.setBlock(blockPos.above(), (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, j)).setValue(LEAVES, bambooLeaves)).setValue(STAGE, k), 3);
    }

    protected int getHeightAboveUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i;
        for (i = 0; i < 16 && blockGetter.getBlockState(blockPos.above(i + 1)).is(Blocks.BAMBOO); ++i) {
        }
        return i;
    }

    protected int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i;
        for (i = 0; i < 16 && blockGetter.getBlockState(blockPos.below(i + 1)).is(Blocks.BAMBOO); ++i) {
        }
        return i;
    }
}

