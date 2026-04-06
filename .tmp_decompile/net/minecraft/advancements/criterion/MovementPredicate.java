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

public record MovementPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles speed, MinMaxBounds.Doubles horizontalSpeed, MinMaxBounds.Doubles verticalSpeed, MinMaxBounds.Doubles fallDistance) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)).apply((Applicative)instance, MovementPredicate::new));

    public static MovementPredicate speed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate fallDistance(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles);
    }

    public boolean matches(double d, double e, double f, double g) {
        if (!(this.x.matches(d) && this.y.matches(e) && this.z.matches(f))) {
            return false;
        }
        double h = Mth.lengthSquared(d, e, f);
        if (!this.speed.matchesSqr(h)) {
            return false;
        }
        double i = Mth.lengthSquared(d, f);
        if (!this.horizontalSpeed.matchesSqr(i)) {
            return false;
        }
        double j = Math.abs(e);
        if (!this.verticalSpeed.matches(j)) {
            return false;
        }
        return this.fallDistance.matches(g);
    }
}

