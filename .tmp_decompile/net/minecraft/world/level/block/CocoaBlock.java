/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CocoaBlock
extends HorizontalDirectionalBlock
implements BonemealableBlock {
    public static final MapCodec<CocoaBlock> CODEC = CocoaBlock.simpleCodec(CocoaBlock::new);
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final List<Map<Direction, VoxelShape>> SHAPES = IntStream.rangeClosed(0, 2).mapToObj(i -> Shapes.rotateHorizontal(Block.column(4 + i * 2, 7 - i * 2, 12.0).move(0.0, 0.0, (double)(i - 5) / 16.0).optimize())).toList();

    public MapCodec<CocoaBlock> codec() {
        return CODEC;
    }

    public CocoaBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(AGE, 0));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGE) < 2;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int i;
        if (serverLevel.random.nextInt(5) == 0 && (i = blockState.getValue(AGE).intValue()) < 2) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(AGE, i + 1), 2);
        }
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.relative((Direction)blockState.getValue(FACING)));
        return blockState2.is(BlockTags.JUNGLE_LOGS);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(AGE)).get(blockState.getValue(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = this.defaultBlockState();
        Level levelReader = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal() || !(blockState = (BlockState)blockState.setValue(FACING, direction)).canSurvive(levelReader, blockPos)) continue;
            return blockState;
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == blockState.getValue(FACING) && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(AGE, blockState.getValue(AGE) + 1), 2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

