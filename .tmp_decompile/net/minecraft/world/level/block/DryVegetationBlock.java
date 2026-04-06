/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryVegetationBlock
extends VegetationBlock {
    public static final MapCodec<DryVegetationBlock> CODEC = DryVegetationBlock.simpleCodec(DryVegetationBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

    public MapCodec<? extends DryVegetationBlock> codec() {
        return CODEC;
    }

    protected DryVegetationBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(BlockTags.DRY_VEGETATION_MAY_PLACE_ON);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        AmbientDesertBlockSoundsPlayer.playAmbientDeadBushSounds(level, blockPos, randomSource);
    }
}

