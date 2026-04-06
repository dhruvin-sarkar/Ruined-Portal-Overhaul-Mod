/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static <T> int getTotalWeight(List<T> list, ToIntFunction<T> toIntFunction) {
        long l = 0L;
        for (T object : list) {
            l += (long)toIntFunction.applyAsInt(object);
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, int i, ToIntFunction<T> toIntFunction) {
        if (i < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (i == 0) {
            return Optional.empty();
        }
        int j = randomSource.nextInt(i);
        return WeightedRandom.getWeightedItem(list, j, toIntFunction);
    }

    public static <T> Optional<T> getWeightedItem(List<T> list, int i, ToIntFunction<T> toIntFunction) {
        for (T object : list) {
            if ((i -= toIntFunction.applyAsInt(object)) >= 0) continue;
            return Optional.of(object);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, ToIntFunction<T> toIntFunction) {
        return WeightedRandom.getRandomItem(randomSource, list, WeightedRandom.getTotalWeight(list, toIntFunction), toIntFunction);
    }
}

