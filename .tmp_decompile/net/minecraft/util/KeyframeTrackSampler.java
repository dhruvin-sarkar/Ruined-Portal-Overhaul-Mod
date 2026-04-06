/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.util;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.EasingType;
import net.minecraft.util.Keyframe;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.world.attribute.LerpFunction;

public class KeyframeTrackSampler<T> {
    private final Optional<Integer> periodTicks;
    private final LerpFunction<T> lerp;
    private final List<Segment<T>> segments;

    KeyframeTrackSampler(KeyframeTrack<T> keyframeTrack, Optional<Integer> optional, LerpFunction<T> lerpFunction) {
        this.periodTicks = optional;
        this.lerp = lerpFunction;
        this.segments = KeyframeTrackSampler.bakeSegments(keyframeTrack, optional);
    }

    private static <T> List<Segment<T>> bakeSegments(KeyframeTrack<T> keyframeTrack, Optional<Integer> optional) {
        List<Keyframe<T>> list = keyframeTrack.keyframes();
        if (list.size() == 1) {
            Object object = ((Keyframe)((Object)list.getFirst())).value();
            return List.of(new Segment(EasingType.CONSTANT, object, 0, object, 0));
        }
        ArrayList<Segment<T>> list2 = new ArrayList<Segment<T>>();
        if (optional.isPresent()) {
            Keyframe keyframe = (Keyframe)((Object)list.getFirst());
            Keyframe keyframe2 = (Keyframe)((Object)list.getLast());
            list2.add(new Segment<T>(keyframeTrack, keyframe2, keyframe2.ticks() - optional.get(), keyframe, keyframe.ticks()));
            KeyframeTrackSampler.addSegmentsFromKeyframes(keyframeTrack, list, list2);
            list2.add(new Segment<T>(keyframeTrack, keyframe2, keyframe2.ticks(), keyframe, keyframe.ticks() + optional.get()));
        } else {
            KeyframeTrackSampler.addSegmentsFromKeyframes(keyframeTrack, list, list2);
        }
        return List.copyOf(list2);
    }

    private static <T> void addSegmentsFromKeyframes(KeyframeTrack<T> keyframeTrack, List<Keyframe<T>> list, List<Segment<T>> list2) {
        for (int i = 0; i < list.size() - 1; ++i) {
            Keyframe<T> keyframe = list.get(i);
            Keyframe<T> keyframe2 = list.get(i + 1);
            list2.add(new Segment<T>(keyframeTrack, keyframe, keyframe.ticks(), keyframe2, keyframe2.ticks()));
        }
    }

    public T sample(long l) {
        long m = this.loopTicks(l);
        Segment<T> segment = this.getSegmentAt(m);
        if (m <= (long)segment.fromTicks) {
            return segment.fromValue;
        }
        if (m >= (long)segment.toTicks) {
            return segment.toValue;
        }
        float f = (float)(m - (long)segment.fromTicks) / (float)(segment.toTicks - segment.fromTicks);
        float g = segment.easing.apply(f);
        return this.lerp.apply(g, segment.fromValue, segment.toValue);
    }

    private Segment<T> getSegmentAt(long l) {
        for (Segment<T> segment : this.segments) {
            if (l >= (long)segment.toTicks) continue;
            return segment;
        }
        return (Segment)((Object)this.segments.getLast());
    }

    private long loopTicks(long l) {
        if (this.periodTicks.isPresent()) {
            return Math.floorMod((long)l, (int)this.periodTicks.get());
        }
        return l;
    }

    static final class Segment<T>
    extends Record {
        final EasingType easing;
        final T fromValue;
        final int fromTicks;
        final T toValue;
        final int toTicks;

        public Segment(KeyframeTrack<T> keyframeTrack, Keyframe<T> keyframe, int i, Keyframe<T> keyframe2, int j) {
            this(keyframeTrack.easingType(), keyframe.value(), i, keyframe2.value(), j);
        }

        Segment(EasingType easingType, T object, int i, T object2, int j) {
            this.easing = easingType;
            this.fromValue = object;
            this.fromTicks = i;
            this.toValue = object2;
            this.toTicks = j;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this, object);
        }

        public EasingType easing() {
            return this.easing;
        }

        public T fromValue() {
            return this.fromValue;
        }

        public int fromTicks() {
            return this.fromTicks;
        }

        public T toValue() {
            return this.toValue;
        }

        public int toTicks() {
            return this.toTicks;
        }
    }
}

