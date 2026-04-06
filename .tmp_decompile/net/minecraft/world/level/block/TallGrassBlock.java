/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TallGrassBlock
extends VegetationBlock
implements BonemealableBlock {
    public static final MapCodec<TallGrassBlock> CODEC = TallGrassBlock.simpleCodec(TallGrassBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

    public MapCodec<TallGrassBlock> codec() {
        return CODEC;
    }

    protected TallGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return TallGrassBlock.getGrownBlock(blockState).defaultBlockState().canSurvive(levelReader, blockPos) && levelReader.isEmptyBlock(blockPos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        DoublePlantBlock.placeAt(serverLevel, TallGrassBlock.getGrownBlock(blockState).defaultBlockState(), blockPos, 2);
    }

    private static DoublePlantBlock getGrownBlock(BlockState blockState) {
        return (DoublePlantBlock)(blockState.is(Blocks.FERN) ? Blocks.LARGE_FERN : Blocks.TALL_GRASS);
    }
}

