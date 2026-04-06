/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class TrapezoidFloat
extends FloatProvider {
    public static final MapCodec<TrapezoidFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("min").forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.min)), (App)Codec.FLOAT.fieldOf("max").forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.max)), (App)Codec.FLOAT.fieldOf("plateau").forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.plateau))).apply((Applicative)instance, TrapezoidFloat::new)).validate(trapezoidFloat -> {
        if (trapezoidFloat.max < trapezoidFloat.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]");
        }
        if (trapezoidFloat.plateau > trapezoidFloat.max - trapezoidFloat.min) {
            return DataResult.error(() -> "Plateau can at most be the full span: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]");
        }
        return DataResult.success((Object)trapezoidFloat);
    });
    private final float min;
    private final float max;
    private final float plateau;

    public static TrapezoidFloat of(float f, float g, float h) {
        return new TrapezoidFloat(f, g, h);
    }

    private TrapezoidFloat(float f, float g, float h) {
        this.min = f;
        this.max = g;
        this.plateau = h;
    }

    @Override
    public float sample(RandomSource randomSource) {
        float f = this.max - this.min;
        float g = (f - this.plateau) / 2.0f;
        float h = f - g;
        return this.min + randomSource.nextFloat() * h + randomSource.nextFloat() * g;
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.TRAPEZOID;
    }

    public String toString() {
        return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
    }
}

