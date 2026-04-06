/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import org.jspecify.annotations.Nullable;

public class BasaltColumnsFeature
extends Feature<ColumnFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of((Object)Blocks.LAVA, (Object)Blocks.BEDROCK, (Object)Blocks.MAGMA_BLOCK, (Object)Blocks.SOUL_SAND, (Object)Blocks.NETHER_BRICKS, (Object)Blocks.NETHER_BRICK_FENCE, (Object)Blocks.NETHER_BRICK_STAIRS, (Object)Blocks.NETHER_WART, (Object)Blocks.CHEST, (Object)Blocks.SPAWNER);
    private static final int CLUSTERED_REACH = 5;
    private static final int CLUSTERED_SIZE = 50;
    private static final int UNCLUSTERED_REACH = 8;
    private static final int UNCLUSTERED_SIZE = 15;

    public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ColumnFeatureConfiguration> featurePlaceContext) {
        int i = featurePlaceContext.chunkGenerator().getSeaLevel();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        ColumnFeatureConfiguration columnFeatureConfiguration = featurePlaceContext.config();
        if (!BasaltColumnsFeature.canPlaceAt(worldGenLevel, i, blockPos.mutable())) {
            return false;
        }
        int j = columnFeatureConfiguration.height().sample(randomSource);
        boolean bl = randomSource.nextFloat() < 0.9f;
        int k = Math.min(j, bl ? 5 : 8);
        int l = bl ? 50 : 15;
        boolean bl2 = false;
        for (BlockPos blockPos2 : BlockPos.randomBetweenClosed(randomSource, l, blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k)) {
            int m = j - blockPos2.distManhattan(blockPos);
            if (m < 0) continue;
            bl2 |= this.placeColumn(worldGenLevel, i, blockPos2, m, columnFeatureConfiguration.reach().sample(randomSource));
        }
        return bl2;
    }

    private boolean placeColumn(LevelAccessor levelAccessor, int i, BlockPos blockPos, int j, int k) {
        boolean bl = false;
        block0: for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k)) {
            BlockPos blockPos3;
            int l = blockPos2.distManhattan(blockPos);
            BlockPos blockPos4 = blockPos3 = BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, blockPos2) ? BasaltColumnsFeature.findSurface(levelAccessor, i, blockPos2.mutable(), l) : BasaltColumnsFeature.findAir(levelAccessor, blockPos2.mutable(), l);
            if (blockPos3 == null) continue;
            BlockPos.MutableBlockPos mutableBlockPos = blockPos3.mutable();
            for (int m = j - l / 2; m >= 0; --m) {
                if (BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
                    this.setBlock(levelAccessor, mutableBlockPos, Blocks.BASALT.defaultBlockState());
                    mutableBlockPos.move(Direction.UP);
                    bl = true;
                    continue;
                }
                if (!levelAccessor.getBlockState(mutableBlockPos).is(Blocks.BASALT)) continue block0;
                mutableBlockPos.move(Direction.UP);
            }
        }
        return bl;
    }

    private static @Nullable BlockPos findSurface(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos, int j) {
        while (mutableBlockPos.getY() > levelAccessor.getMinY() + 1 && j > 0) {
            --j;
            if (BasaltColumnsFeature.canPlaceAt(levelAccessor, i, mutableBlockPos)) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean canPlaceAt(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        if (BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
            mutableBlockPos.move(Direction.UP);
            return !blockState.isAir() && !CANNOT_PLACE_ON.contains((Object)blockState.getBlock());
        }
        return false;
    }

    private static @Nullable BlockPos findAir(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, int i) {
        while (mutableBlockPos.getY() <= levelAccessor.getMaxY() && i > 0) {
            --i;
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
            if (CANNOT_PLACE_ON.contains((Object)blockState.getBlock())) {
                return null;
            }
            if (blockState.isAir()) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.UP);
        }
        return null;
    }

    private static boolean isAirOrLavaOcean(LevelAccessor levelAccessor, int i, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.isAir() || blockState.is(Blocks.LAVA) && blockPos.getY() <= i;
    }
}

