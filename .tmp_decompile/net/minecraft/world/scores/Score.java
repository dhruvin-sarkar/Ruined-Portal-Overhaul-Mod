/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.scores;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import org.jspecify.annotations.Nullable;

public class Score
implements ReadOnlyScoreInfo {
    private int value;
    private boolean locked = true;
    private @Nullable Component display;
    private @Nullable NumberFormat numberFormat;

    public Score() {
    }

    public Score(Packed packed) {
        this.value = packed.value;
        this.locked = packed.locked;
        this.display = packed.display.orElse(null);
        this.numberFormat = packed.numberFormat.orElse(null);
    }

    public Packed pack() {
        return new Packed(this.value, this.locked, Optional.ofNullable(this.display), Optional.ofNullable(this.numberFormat));
    }

    @Override
    public int value() {
        return this.value;
    }

    public void value(int i) {
        this.value = i;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean bl) {
        this.locked = bl;
    }

    public @Nullable Component display() {
        return this.display;
    }

    public void display(@Nullable Component component) {
        this.display = component;
    }

    @Override
    public @Nullable NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public static final class Packed
    extends Record {
        final int value;
        final boolean locked;
        final Optional<Component> display;
        final Optional<NumberFormat> numberFormat;
        public static final MapCodec<Packed> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.optionalFieldOf("Score", (Object)0).forGetter(Packed::value), (App)Codec.BOOL.optionalFieldOf("Locked", (Object)false).forGetter(Packed::locked), (App)ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(Packed::display), (App)NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(Packed::numberFormat)).apply((Applicative)instance, Packed::new));

        public Packed(int i, boolean bl, Optional<Component> optional, Optional<NumberFormat> optional2) {
            this.value = i;
            this.locked = bl;
            this.display = optional;
            this.numberFormat = optional2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this, object);
        }

        public int value() {
            return this.value;
        }

        public boolean locked() {
            return this.locked;
        }

        public Optional<Component> display() {
            return this.display;
        }

        public Optional<NumberFormat> numberFormat() {
            return this.numberFormat;
        }
    }
}

