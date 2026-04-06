/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.Mth;

public record DistancePredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles horizontal, MinMaxBounds.Doubles absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::horizontal), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("absolute", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative)instance, DistancePredicate::new));

    public static DistancePredicate horizontal(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles);
    }

    public boolean matches(double d, double e, double f, double g, double h, double i) {
        float j = (float)(d - g);
        float k = (float)(e - h);
        float l = (float)(f - i);
        if (!(this.x.matches(Mth.abs(j)) && this.y.matches(Mth.abs(k)) && this.z.matches(Mth.abs(l)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr(j * j + l * l)) {
            return false;
        }
        return this.absolute.matchesSqr(j * j + k * k + l * l);
    }
}

