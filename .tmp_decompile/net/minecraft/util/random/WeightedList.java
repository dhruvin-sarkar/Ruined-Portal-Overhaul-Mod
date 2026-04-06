/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import org.jspecify.annotations.Nullable;

public final class WeightedList<E> {
    private static final int FLAT_THRESHOLD = 64;
    private final int totalWeight;
    private final List<Weighted<E>> items;
    private final @Nullable Selector<E> selector;

    WeightedList(List<? extends Weighted<E>> list) {
        this.items = List.copyOf(list);
        this.totalWeight = WeightedRandom.getTotalWeight(list, Weighted::weight);
        this.selector = this.totalWeight == 0 ? null : (this.totalWeight < 64 ? new Flat<E>(this.items, this.totalWeight) : new Compact<E>(this.items));
    }

    public static <E> WeightedList<E> of() {
        return new WeightedList<E>(List.of());
    }

    public static <E> WeightedList<E> of(E object) {
        return new WeightedList<E>(List.of(new Weighted<E>(object, 1)));
    }

    @SafeVarargs
    public static <E> WeightedList<E> of(Weighted<E> ... weighteds) {
        return new WeightedList<E>(List.of(weighteds));
    }

    public static <E> WeightedList<E> of(List<Weighted<E>> list) {
        return new WeightedList<E>(list);
    }

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public <T> WeightedList<T> map(Function<E, T> function) {
        return new WeightedList<E>(Lists.transform(this.items, weighted -> weighted.map(function)));
    }

    public Optional<E> getRandom(RandomSource randomSource) {
        if (this.selector == null) {
            return Optional.empty();
        }
        int i = randomSource.nextInt(this.totalWeight);
        return Optional.of(this.selector.get(i));
    }

    public E getRandomOrThrow(RandomSource randomSource) {
        if (this.selector == null) {
            throw new IllegalStateException("Weighted list has no elements");
        }
        int i = randomSource.nextInt(this.totalWeight);
        return this.selector.get(i);
    }

    public List<Weighted<E>> unwrap() {
        return this.items;
    }

    public static <E> Codec<WeightedList<E>> codec(Codec<E> codec) {
        return Weighted.codec(codec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> codec(MapCodec<E> mapCodec) {
        return Weighted.codec(mapCodec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(Codec<E> codec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(codec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(MapCodec<E> mapCodec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(mapCodec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E, B extends ByteBuf> StreamCodec<B, WeightedList<E>> streamCodec(StreamCodec<B, E> streamCodec) {
        return Weighted.streamCodec(streamCodec).apply(ByteBufCodecs.list()).map(WeightedList::of, WeightedList::unwrap);
    }

    public boolean contains(E object) {
        for (Weighted<E> weighted : this.items) {
            if (!weighted.value().equals(object)) continue;
            return true;
        }
        return false;
    }

    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof WeightedList) {
            WeightedList weightedList = (WeightedList)object;
            return this.totalWeight == weightedList.totalWeight && Objects.equals(this.items, weightedList.items);
        }
        return false;
    }

    public int hashCode() {
        int i = this.totalWeight;
        i = 31 * i + this.items.hashCode();
        return i;
    }

    static interface Selector<E> {
        public E get(int var1);
    }

    static class Flat<E>
    implements Selector<E> {
        private final Object[] entries;

        Flat(List<Weighted<E>> list, int i) {
            this.entries = new Object[i];
            int j = 0;
            for (Weighted<E> weighted : list) {
                int k = weighted.weight();
                Arrays.fill(this.entries, j, j + k, weighted.value());
                j += k;
            }
        }

        @Override
        public E get(int i) {
            return (E)this.entries[i];
        }
    }

    static class Compact<E>
    implements Selector<E> {
        private final Weighted<?>[] entries;

        Compact(List<Weighted<E>> list) {
            this.entries = (Weighted[])list.toArray(Weighted[]::new);
        }

        @Override
        public E get(int i) {
            for (Weighted<?> weighted : this.entries) {
                if ((i -= weighted.weight()) >= 0) continue;
                return (E)weighted.value();
            }
            throw new IllegalStateException(i + " exceeded total weight");
        }
    }

    public static class Builder<E> {
        private final ImmutableList.Builder<Weighted<E>> result = ImmutableList.builder();

        public Builder<E> add(E object) {
            return this.add(object, 1);
        }

        public Builder<E> add(E object, int i) {
            this.result.add(new Weighted<E>(object, i));
            return this;
        }

        public WeightedList<E> build() {
            return new WeightedList(this.result.build());
        }
    }
}

