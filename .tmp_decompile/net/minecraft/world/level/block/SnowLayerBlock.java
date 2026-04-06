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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SnowLayerBlock
extends Block {
    public static final MapCodec<SnowLayerBlock> CODEC = SnowLayerBlock.simpleCodec(SnowLayerBlock::new);
    public static final int MAX_HEIGHT = 8;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    private static final VoxelShape[] SHAPES = Block.boxes(8, i -> Block.column(16.0, 0.0, i * 2));
    public static final int HEIGHT_IMPASSABLE = 5;

    public MapCodec<SnowLayerBlock> codec() {
        return CODEC;
    }

    protected SnowLayerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LAYERS, 1));
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        if (pathComputationType == PathComputationType.LAND) {
            return blockState.getValue(LAYERS) < 5;
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[blockState.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[blockState.getValue(LAYERS) - 1];
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPES[blockState.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[blockState.getValue(LAYERS)];
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getValue(LAYERS) == 8 ? 0.2f : 1.0f;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        if (blockState2.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
            return false;
        }
        if (blockState2.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
            return true;
        }
        return Block.isFaceFull(blockState2.getCollisionShape(levelReader, blockPos.below()), Direction.UP) || blockState2.is(this) && blockState2.getValue(LAYERS) == 8;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (!blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11) {
            SnowLayerBlock.dropResources(blockState, serverLevel, blockPos);
            serverLevel.removeBlock(blockPos, false);
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        int i = blockState.getValue(LAYERS);
        if (blockPlaceContext.getItemInHand().is(this.asItem()) && i < 8) {
            if (blockPlaceContext.replacingClickedOnBlock()) {
                return blockPlaceContext.getClickedFace() == Direction.UP;
            }
            return true;
        }
        return i == 1;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            int i = blockState.getValue(LAYERS);
            return (BlockState)blockState.setValue(LAYERS, Math.min(8, i + 1));
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }
}

