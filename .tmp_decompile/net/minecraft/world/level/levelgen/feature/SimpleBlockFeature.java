/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature
extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> featurePlaceContext) {
        SimpleBlockConfiguration simpleBlockConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        BlockState blockState = simpleBlockConfiguration.toPlace().getState(featurePlaceContext.random(), blockPos);
        if (!blockState.canSurvive(worldGenLevel, blockPos)) return false;
        if (blockState.getBlock() instanceof DoublePlantBlock) {
            if (!worldGenLevel.isEmptyBlock(blockPos.above())) return false;
            DoublePlantBlock.placeAt(worldGenLevel, blockState, blockPos, 2);
        } else if (blockState.getBlock() instanceof MossyCarpetBlock) {
            MossyCarpetBlock.placeAt(worldGenLevel, blockPos, worldGenLevel.getRandom(), 2);
        } else {
            worldGenLevel.setBlock(blockPos, blockState, 2);
        }
        if (!simpleBlockConfiguration.scheduleTick()) return true;
        worldGenLevel.scheduleTick(blockPos, worldGenLevel.getBlockState(blockPos).getBlock(), 1);
        return true;
    }
}

