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
package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;

public class ThreeLayersFeatureSize
extends FeatureSize {
    public static final MapCodec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.intRange((int)0, (int)80).fieldOf("limit").orElse((Object)1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.limit), (App)Codec.intRange((int)0, (int)80).fieldOf("upper_limit").orElse((Object)1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperLimit), (App)Codec.intRange((int)0, (int)16).fieldOf("lower_size").orElse((Object)0).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.lowerSize), (App)Codec.intRange((int)0, (int)16).fieldOf("middle_size").orElse((Object)1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.middleSize), (App)Codec.intRange((int)0, (int)16).fieldOf("upper_size").orElse((Object)1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperSize), ThreeLayersFeatureSize.minClippedHeightCodec()).apply((Applicative)instance, ThreeLayersFeatureSize::new));
    private final int limit;
    private final int upperLimit;
    private final int lowerSize;
    private final int middleSize;
    private final int upperSize;

    public ThreeLayersFeatureSize(int i, int j, int k, int l, int m, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = i;
        this.upperLimit = j;
        this.lowerSize = k;
        this.middleSize = l;
        this.upperSize = m;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int i, int j) {
        if (j < this.limit) {
            return this.lowerSize;
        }
        if (j >= i - this.upperLimit) {
            return this.upperSize;
        }
        return this.middleSize;
    }
}

