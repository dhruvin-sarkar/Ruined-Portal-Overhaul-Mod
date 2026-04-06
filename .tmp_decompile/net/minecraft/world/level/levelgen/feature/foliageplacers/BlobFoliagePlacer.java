/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P3
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class BlobFoliagePlacer
extends FoliagePlacer {
    public static final MapCodec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> BlobFoliagePlacer.blobParts(instance).apply((Applicative)instance, BlobFoliagePlacer::new));
    protected final int height;

    protected static <P extends BlobFoliagePlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider, Integer> blobParts(RecordCodecBuilder.Instance<P> instance) {
        return BlobFoliagePlacer.foliagePlacerParts(instance).and((App)Codec.intRange((int)0, (int)16).fieldOf("height").forGetter(blobFoliagePlacer -> blobFoliagePlacer.height));
    }

    public BlobFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int i) {
        super(intProvider, intProvider2);
        this.height = i;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        for (int m = l; m >= l - j; --m) {
            int n = Math.max(k + foliageAttachment.radiusOffset() - 1 - m / 2, 0);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, foliageAttachment.pos(), n, m, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        return i == l && k == l && (randomSource.nextInt(2) == 0 || j == 0);
    }
}

