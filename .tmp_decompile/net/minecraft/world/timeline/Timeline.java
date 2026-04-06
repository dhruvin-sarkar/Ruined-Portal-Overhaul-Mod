/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.timeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.AttributeTrack;
import net.minecraft.world.timeline.AttributeTrackSampler;

public class Timeline {
    public static final Codec<Holder<Timeline>> CODEC = RegistryFixedCodec.create(Registries.TIMELINE);
    private static final Codec<Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>>> TRACKS_CODEC = Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(AttributeTrack::createCodec));
    public static final Codec<Timeline> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("period_ticks").forGetter(timeline -> timeline.periodTicks), (App)TRACKS_CODEC.optionalFieldOf("tracks", (Object)Map.of()).forGetter(timeline -> timeline.tracks)).apply((Applicative)instance, Timeline::new)).validate(Timeline::validateInternal);
    public static final Codec<Timeline> NETWORK_CODEC = DIRECT_CODEC.xmap(Timeline::filterSyncableTracks, Timeline::filterSyncableTracks);
    private final Optional<Integer> periodTicks;
    private final Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>> tracks;

    private static Timeline filterSyncableTracks(Timeline timeline) {
        Map map = Map.copyOf((Map)Maps.filterKeys(timeline.tracks, EnvironmentAttribute::isSyncable));
        return new Timeline(timeline.periodTicks, map);
    }

    Timeline(Optional<Integer> optional, Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>> map) {
        this.periodTicks = optional;
        this.tracks = map;
    }

    private static DataResult<Timeline> validateInternal(Timeline timeline2) {
        if (timeline2.periodTicks.isEmpty()) {
            return DataResult.success((Object)timeline2);
        }
        int i = timeline2.periodTicks.get();
        DataResult dataResult = DataResult.success((Object)timeline2);
        for (AttributeTrack<?, ?> attributeTrack2 : timeline2.tracks.values()) {
            dataResult = dataResult.apply2stable((timeline, attributeTrack) -> timeline, AttributeTrack.validatePeriod(attributeTrack2, i));
        }
        return dataResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getCurrentTicks(Level level) {
        long l = this.getTotalTicks(level);
        if (this.periodTicks.isEmpty()) {
            return l;
        }
        return l % (long)this.periodTicks.get().intValue();
    }

    public long getTotalTicks(Level level) {
        return level.getDayTime();
    }

    public Optional<Integer> periodTicks() {
        return this.periodTicks;
    }

    public Set<EnvironmentAttribute<?>> attributes() {
        return this.tracks.keySet();
    }

    public <Value> AttributeTrackSampler<Value, ?> createTrackSampler(EnvironmentAttribute<Value> environmentAttribute, LongSupplier longSupplier) {
        AttributeTrack<?, ?> attributeTrack = this.tracks.get(environmentAttribute);
        if (attributeTrack == null) {
            throw new IllegalStateException("Timeline has no track for " + String.valueOf(environmentAttribute));
        }
        return attributeTrack.bakeSampler(environmentAttribute, this.periodTicks, longSupplier);
    }

    public static class Builder {
        private Optional<Integer> periodTicks = Optional.empty();
        private final ImmutableMap.Builder<EnvironmentAttribute<?>, AttributeTrack<?, ?>> tracks = ImmutableMap.builder();

        Builder() {
        }

        public Builder setPeriodTicks(int i) {
            this.periodTicks = Optional.of(i);
            return this;
        }

        public <Value, Argument> Builder addModifierTrack(EnvironmentAttribute<Value> environmentAttribute, AttributeModifier<Value, Argument> attributeModifier, Consumer<KeyframeTrack.Builder<Argument>> consumer) {
            environmentAttribute.type().checkAllowedModifier(attributeModifier);
            KeyframeTrack.Builder builder = new KeyframeTrack.Builder();
            consumer.accept(builder);
            this.tracks.put(environmentAttribute, new AttributeTrack<Value, Argument>(attributeModifier, builder.build()));
            return this;
        }

        public <Value> Builder addTrack(EnvironmentAttribute<Value> environmentAttribute, Consumer<KeyframeTrack.Builder<Value>> consumer) {
            return this.addModifierTrack(environmentAttribute, AttributeModifier.override(), consumer);
        }

        public Timeline build() {
            return new Timeline(this.periodTicks, (Map<EnvironmentAttribute<?>, AttributeTrack<?, ?>>)this.tracks.build());
        }
    }
}

