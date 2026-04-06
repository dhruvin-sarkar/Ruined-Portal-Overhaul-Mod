/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.EasingType;
import net.minecraft.util.Keyframe;
import net.minecraft.util.KeyframeTrackSampler;
import net.minecraft.world.attribute.LerpFunction;

public record KeyframeTrack<T>(List<Keyframe<T>> keyframes, EasingType easingType) {
    public KeyframeTrack {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Track has no keyframes");
        }
    }

    public static <T> MapCodec<KeyframeTrack<T>> mapCodec(Codec<T> codec) {
        Codec codec2 = Keyframe.codec(codec).listOf().validate(KeyframeTrack::validateKeyframes);
        return RecordCodecBuilder.mapCodec((T instance) -> instance.group((App)codec2.fieldOf("keyframes").forGetter(KeyframeTrack::keyframes), (App)EasingType.CODEC.optionalFieldOf("ease", (Object)EasingType.LINEAR).forGetter(KeyframeTrack::easingType)).apply((Applicative)instance, KeyframeTrack::new));
    }

    static <T> DataResult<List<Keyframe<T>>> validateKeyframes(List<Keyframe<T>> list) {
        if (list.isEmpty()) {
            return DataResult.error(() -> "Keyframes must not be empty");
        }
        if (!Comparators.isInOrder(list, Comparator.comparingInt(Keyframe::ticks))) {
            return DataResult.error(() -> "Keyframes must be ordered by ticks field");
        }
        if (list.size() > 1) {
            int i = 0;
            int j = ((Keyframe)((Object)list.getLast())).ticks();
            for (Keyframe keyframe : list) {
                if (keyframe.ticks() == j) {
                    if (++i > 2) {
                        return DataResult.error(() -> "More than 2 keyframes on same tick: " + keyframe.ticks());
                    }
                } else {
                    i = 0;
                }
                j = keyframe.ticks();
            }
        }
        return DataResult.success(list);
    }

    public static DataResult<KeyframeTrack<?>> validatePeriod(KeyframeTrack<?> keyframeTrack, int i) {
        for (Keyframe<?> keyframe : keyframeTrack.keyframes()) {
            int j = keyframe.ticks();
            if (j >= 0 && j <= i) continue;
            return DataResult.error(() -> "Keyframe at tick " + keyframe.ticks() + " must be in range [0; " + i + "]");
        }
        return DataResult.success(keyframeTrack);
    }

    public KeyframeTrackSampler<T> bakeSampler(Optional<Integer> optional, LerpFunction<T> lerpFunction) {
        return new KeyframeTrackSampler<T>(this, optional, lerpFunction);
    }

    public static class Builder<T> {
        private final ImmutableList.Builder<Keyframe<T>> keyframes = ImmutableList.builder();
        private EasingType easing = EasingType.LINEAR;

        public Builder<T> addKeyframe(int i, T object) {
            this.keyframes.add(new Keyframe<T>(i, object));
            return this;
        }

        public Builder<T> setEasing(EasingType easingType) {
            this.easing = easingType;
            return this;
        }

        public KeyframeTrack<T> build() {
            List list = (List)KeyframeTrack.validateKeyframes(this.keyframes.build()).getOrThrow();
            return new KeyframeTrack(list, this.easing);
        }
    }
}

