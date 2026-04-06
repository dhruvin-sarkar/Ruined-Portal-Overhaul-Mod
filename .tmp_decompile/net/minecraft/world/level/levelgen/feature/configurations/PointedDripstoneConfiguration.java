/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class PointedDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<PointedDripstoneConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_taller_dripstone").orElse((Object)Float.valueOf(0.2f)).forGetter(pointedDripstoneConfiguration -> Float.valueOf(pointedDripstoneConfiguration.chanceOfTallerDripstone)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_directional_spread").orElse((Object)Float.valueOf(0.7f)).forGetter(pointedDripstoneConfiguration -> Float.valueOf(pointedDripstoneConfiguration.chanceOfDirectionalSpread)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_spread_radius2").orElse((Object)Float.valueOf(0.5f)).forGetter(pointedDripstoneConfiguration -> Float.valueOf(pointedDripstoneConfiguration.chanceOfSpreadRadius2)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_spread_radius3").orElse((Object)Float.valueOf(0.5f)).forGetter(pointedDripstoneConfiguration -> Float.valueOf(pointedDripstoneConfiguration.chanceOfSpreadRadius3))).apply((Applicative)instance, PointedDripstoneConfiguration::new));
    public final float chanceOfTallerDripstone;
    public final float chanceOfDirectionalSpread;
    public final float chanceOfSpreadRadius2;
    public final float chanceOfSpreadRadius3;

    public PointedDripstoneConfiguration(float f, float g, float h, float i) {
        this.chanceOfTallerDripstone = f;
        this.chanceOfDirectionalSpread = g;
        this.chanceOfSpreadRadius2 = h;
        this.chanceOfSpreadRadius3 = i;
    }
}

