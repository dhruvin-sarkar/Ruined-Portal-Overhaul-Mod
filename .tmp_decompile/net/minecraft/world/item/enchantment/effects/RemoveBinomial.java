/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect
{
    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply((Applicative)instance, RemoveBinomial::new));

    @Override
    public float process(int i, RandomSource randomSource, float f) {
        float g = this.chance.calculate(i);
        int j = 0;
        if (f <= 128.0f || f * g < 20.0f || f * (1.0f - g) < 20.0f) {
            int k = 0;
            while ((float)k < f) {
                if (randomSource.nextFloat() < g) {
                    ++j;
                }
                ++k;
            }
        } else {
            double d = Math.floor(f * g);
            double e = Math.sqrt(f * g * (1.0f - g));
            j = (int)Math.round(d + randomSource.nextGaussian() * e);
            j = Math.clamp((long)j, (int)0, (int)((int)f));
        }
        return f - (float)j;
    }

    public MapCodec<RemoveBinomial> codec() {
        return CODEC;
    }
}

