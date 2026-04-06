/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
    public static final Codec<InclusiveRange<Integer>> INT = InclusiveRange.codec(Codec.INT);

    public InclusiveRange {
        if (comparable.compareTo(comparable2) > 0) {
            throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
        }
    }

    public InclusiveRange(T comparable) {
        this(comparable, comparable);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec) {
        return ExtraCodecs.intervalCodec(codec, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
    }

    public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec, T comparable, T comparable2) {
        return InclusiveRange.codec(codec).validate(inclusiveRange -> {
            if (inclusiveRange.minInclusive().compareTo(comparable) < 0) {
                return DataResult.error(() -> "Range limit too low, expected at least " + String.valueOf(comparable) + " [" + String.valueOf(inclusiveRange.minInclusive()) + "-" + String.valueOf(inclusiveRange.maxInclusive()) + "]");
            }
            if (inclusiveRange.maxInclusive().compareTo(comparable2) > 0) {
                return DataResult.error(() -> "Range limit too high, expected at most " + String.valueOf(comparable2) + " [" + String.valueOf(inclusiveRange.minInclusive()) + "-" + String.valueOf(inclusiveRange.maxInclusive()) + "]");
            }
            return DataResult.success((Object)inclusiveRange);
        });
    }

    public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T comparable, T comparable2) {
        if (comparable.compareTo(comparable2) <= 0) {
            return DataResult.success(new InclusiveRange<T>(comparable, comparable2));
        }
        return DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
    }

    public <S extends Comparable<S>> InclusiveRange<S> map(Function<? super T, ? extends S> function) {
        return new InclusiveRange<Comparable>((Comparable)function.apply(this.minInclusive), (Comparable)function.apply(this.maxInclusive));
    }

    public boolean isValueInRange(T comparable) {
        return comparable.compareTo(this.minInclusive) >= 0 && comparable.compareTo(this.maxInclusive) <= 0;
    }

    public boolean contains(InclusiveRange<T> inclusiveRange) {
        return inclusiveRange.minInclusive().compareTo(this.minInclusive) >= 0 && inclusiveRange.maxInclusive.compareTo(this.maxInclusive) <= 0;
    }

    public String toString() {
        return "[" + String.valueOf(this.minInclusive) + ", " + String.valueOf(this.maxInclusive) + "]";
    }
}

