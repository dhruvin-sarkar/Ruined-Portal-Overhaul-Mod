/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class RandomSpreadFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> RandomSpreadFoliagePlacer.foliagePlacerParts(instance).and(instance.group((App)IntProvider.codec(1, 512).fieldOf("foliage_height").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.foliageHeight), (App)Codec.intRange((int)0, (int)256).fieldOf("leaf_placement_attempts").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.leafPlacementAttempts))).apply((Applicative)instance, RandomSpreadFoliagePlacer::new));
    private final IntProvider foliageHeight;
    private final int leafPlacementAttempts;

    public RandomSpreadFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, int i) {
        super(intProvider, intProvider2);
        this.foliageHeight = intProvider3;
        this.leafPlacementAttempts = i;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        BlockPos blockPos = foliageAttachment.pos();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int m = 0; m < this.leafPlacementAttempts; ++m) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(k) - randomSource.nextInt(k), randomSource.nextInt(j) - randomSource.nextInt(j), randomSource.nextInt(k) - randomSource.nextInt(k));
            RandomSpreadFoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return this.foliageHeight.sample(randomSource);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        return false;
    }
}

