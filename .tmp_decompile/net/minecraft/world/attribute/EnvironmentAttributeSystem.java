/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeLayer;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.attribute.WeatherAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttributeSystem
implements EnvironmentAttributeReader {
    private final Map<EnvironmentAttribute<?>, ValueSampler<?>> attributeSamplers = new Reference2ObjectOpenHashMap();

    EnvironmentAttributeSystem(Map<EnvironmentAttribute<?>, List<EnvironmentAttributeLayer<?>>> map) {
        map.forEach((environmentAttribute, list) -> this.attributeSamplers.put((EnvironmentAttribute<?>)environmentAttribute, this.bakeLayerSampler((EnvironmentAttribute)environmentAttribute, (List<? extends EnvironmentAttributeLayer<?>>)list)));
    }

    private <Value> ValueSampler<Value> bakeLayerSampler(EnvironmentAttribute<Value> environmentAttribute, List<? extends EnvironmentAttributeLayer<?>> list) {
        Object object;
        ArrayList list2 = new ArrayList(list);
        Value object2 = environmentAttribute.defaultValue();
        while (!list2.isEmpty() && (object = list2.getFirst()) instanceof EnvironmentAttributeLayer.Constant) {
            EnvironmentAttributeLayer.Constant constant = (EnvironmentAttributeLayer.Constant)object;
            object2 = constant.applyConstant(object2);
            list2.removeFirst();
        }
        boolean bl = list2.stream().anyMatch(environmentAttributeLayer -> environmentAttributeLayer instanceof EnvironmentAttributeLayer.Positional);
        return new ValueSampler<Value>(environmentAttribute, object2, List.copyOf(list2), bl);
    }

    public static Builder builder() {
        return new Builder();
    }

    static void addDefaultLayers(Builder builder, Level level) {
        RegistryAccess registryAccess = level.registryAccess();
        BiomeManager biomeManager = level.getBiomeManager();
        LongSupplier longSupplier = level::getDayTime;
        EnvironmentAttributeSystem.addDimensionLayer(builder, level.dimensionType());
        EnvironmentAttributeSystem.addBiomeLayer(builder, registryAccess.lookupOrThrow(Registries.BIOME), biomeManager);
        level.dimensionType().timelines().forEach(holder -> builder.addTimelineLayer((Holder<Timeline>)holder, longSupplier));
        if (level.canHaveWeather()) {
            WeatherAttributes.addBuiltinLayers(builder, WeatherAttributes.WeatherAccess.from(level));
        }
    }

    private static void addDimensionLayer(Builder builder, DimensionType dimensionType) {
        builder.addConstantLayer(dimensionType.attributes());
    }

    private static void addBiomeLayer(Builder builder, HolderLookup<Biome> holderLookup, BiomeManager biomeManager) {
        Stream stream = holderLookup.listElements().flatMap(reference -> ((Biome)reference.value()).getAttributes().keySet().stream()).distinct();
        stream.forEach(environmentAttribute -> EnvironmentAttributeSystem.addBiomeLayerForAttribute(builder, environmentAttribute, biomeManager));
    }

    private static <Value> void addBiomeLayerForAttribute(Builder builder, EnvironmentAttribute<Value> environmentAttribute, BiomeManager biomeManager) {
        builder.addPositionalLayer(environmentAttribute, (object, vec3, spatialAttributeInterpolator) -> {
            if (spatialAttributeInterpolator != null && environmentAttribute.isSpatiallyInterpolated()) {
                return spatialAttributeInterpolator.applyAttributeLayer(environmentAttribute, object);
            }
            Holder<Biome> holder = biomeManager.getNoiseBiomeAtPosition(vec3.x, vec3.y, vec3.z);
            return holder.value().getAttributes().applyModifier(environmentAttribute, object);
        });
    }

    public void invalidateTickCache() {
        this.attributeSamplers.values().forEach(ValueSampler::invalidateTickCache);
    }

    private <Value> @Nullable ValueSampler<Value> getValueSampler(EnvironmentAttribute<Value> environmentAttribute) {
        return this.attributeSamplers.get(environmentAttribute);
    }

    @Override
    public <Value> Value getDimensionValue(EnvironmentAttribute<Value> environmentAttribute) {
        if (SharedConstants.IS_RUNNING_IN_IDE && environmentAttribute.isPositional()) {
            throw new IllegalStateException("Position must always be provided for positional attribute " + String.valueOf(environmentAttribute));
        }
        ValueSampler<Value> valueSampler = this.getValueSampler(environmentAttribute);
        if (valueSampler == null) {
            return environmentAttribute.defaultValue();
        }
        return valueSampler.getDimensionValue();
    }

    @Override
    public <Value> Value getValue(EnvironmentAttribute<Value> environmentAttribute, Vec3 vec3, @Nullable SpatialAttributeInterpolator spatialAttributeInterpolator) {
        ValueSampler<Value> valueSampler = this.getValueSampler(environmentAttribute);
        if (valueSampler == null) {
            return environmentAttribute.defaultValue();
        }
        return valueSampler.getValue(vec3, spatialAttributeInterpolator);
    }

    @VisibleForTesting
    <Value> Value getConstantBaseValue(EnvironmentAttribute<Value> environmentAttribute) {
        ValueSampler<Value> valueSampler = this.getValueSampler(environmentAttribute);
        return valueSampler != null ? valueSampler.baseValue : environmentAttribute.defaultValue();
    }

    @VisibleForTesting
    boolean isAffectedByPosition(EnvironmentAttribute<?> environmentAttribute) {
        ValueSampler<?> valueSampler = this.getValueSampler(environmentAttribute);
        return valueSampler != null && valueSampler.isAffectedByPosition;
    }

    static class ValueSampler<Value> {
        private final EnvironmentAttribute<Value> attribute;
        final Value baseValue;
        private final List<EnvironmentAttributeLayer<Value>> layers;
        final boolean isAffectedByPosition;
        private @Nullable Value cachedTickValue;
        private int cacheTickId;

        ValueSampler(EnvironmentAttribute<Value> environmentAttribute, Value object, List<EnvironmentAttributeLayer<Value>> list, boolean bl) {
            this.attribute = environmentAttribute;
            this.baseValue = object;
            this.layers = list;
            this.isAffectedByPosition = bl;
        }

        public void invalidateTickCache() {
            this.cachedTickValue = null;
            ++this.cacheTickId;
        }

        public Value getDimensionValue() {
            if (this.cachedTickValue != null) {
                return this.cachedTickValue;
            }
            Value object = this.computeValueNotPositional();
            this.cachedTickValue = object;
            return object;
        }

        public Value getValue(Vec3 vec3, @Nullable SpatialAttributeInterpolator spatialAttributeInterpolator) {
            if (!this.isAffectedByPosition) {
                return this.getDimensionValue();
            }
            return this.computeValuePositional(vec3, spatialAttributeInterpolator);
        }

        private Value computeValuePositional(Vec3 vec3, @Nullable SpatialAttributeInterpolator spatialAttributeInterpolator) {
            Value object = this.baseValue;
            for (EnvironmentAttributeLayer<Value> environmentAttributeLayer : this.layers) {
                EnvironmentAttributeLayer<Value> environmentAttributeLayer2;
                Objects.requireNonNull(environmentAttributeLayer);
                int n = 0;
                object = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeLayer.Constant.class, EnvironmentAttributeLayer.TimeBased.class, EnvironmentAttributeLayer.Positional.class}, environmentAttributeLayer2, (int)n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeLayer.Constant constant = (EnvironmentAttributeLayer.Constant)environmentAttributeLayer2;
                        yield constant.applyConstant(object);
                    }
                    case 1 -> {
                        EnvironmentAttributeLayer.TimeBased timeBased = (EnvironmentAttributeLayer.TimeBased)environmentAttributeLayer2;
                        yield timeBased.applyTimeBased(object, this.cacheTickId);
                    }
                    case 2 -> {
                        EnvironmentAttributeLayer.Positional positional = (EnvironmentAttributeLayer.Positional)environmentAttributeLayer2;
                        yield positional.applyPositional(object, Objects.requireNonNull(vec3), spatialAttributeInterpolator);
                    }
                };
            }
            return this.attribute.sanitizeValue(object);
        }

        private Value computeValueNotPositional() {
            Value object = this.baseValue;
            for (EnvironmentAttributeLayer<Value> environmentAttributeLayer : this.layers) {
                EnvironmentAttributeLayer<Value> environmentAttributeLayer2;
                Objects.requireNonNull(environmentAttributeLayer);
                int n = 0;
                object = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeLayer.Constant.class, EnvironmentAttributeLayer.TimeBased.class, EnvironmentAttributeLayer.Positional.class}, environmentAttributeLayer2, (int)n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeLayer.Constant constant = (EnvironmentAttributeLayer.Constant)environmentAttributeLayer2;
                        yield constant.applyConstant(object);
                    }
                    case 1 -> {
                        EnvironmentAttributeLayer.TimeBased timeBased = (EnvironmentAttributeLayer.TimeBased)environmentAttributeLayer2;
                        yield timeBased.applyTimeBased(object, this.cacheTickId);
                    }
                    case 2 -> {
                        EnvironmentAttributeLayer.Positional positional = (EnvironmentAttributeLayer.Positional)environmentAttributeLayer2;
                        yield object;
                    }
                };
            }
            return this.attribute.sanitizeValue(object);
        }
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, List<EnvironmentAttributeLayer<?>>> layersByAttribute = new HashMap();

        Builder() {
        }

        public Builder addDefaultLayers(Level level) {
            EnvironmentAttributeSystem.addDefaultLayers(this, level);
            return this;
        }

        public Builder addConstantLayer(EnvironmentAttributeMap environmentAttributeMap) {
            for (EnvironmentAttribute<?> environmentAttribute : environmentAttributeMap.keySet()) {
                this.addConstantEntry(environmentAttribute, environmentAttributeMap);
            }
            return this;
        }

        private <Value> Builder addConstantEntry(EnvironmentAttribute<Value> environmentAttribute, EnvironmentAttributeMap environmentAttributeMap) {
            EnvironmentAttributeMap.Entry<Value, ?> entry = environmentAttributeMap.get(environmentAttribute);
            if (entry == null) {
                throw new IllegalArgumentException("Missing attribute " + String.valueOf(environmentAttribute));
            }
            return this.addConstantLayer(environmentAttribute, entry::applyModifier);
        }

        public <Value> Builder addConstantLayer(EnvironmentAttribute<Value> environmentAttribute, EnvironmentAttributeLayer.Constant<Value> constant) {
            return this.addLayer(environmentAttribute, constant);
        }

        public <Value> Builder addTimeBasedLayer(EnvironmentAttribute<Value> environmentAttribute, EnvironmentAttributeLayer.TimeBased<Value> timeBased) {
            return this.addLayer(environmentAttribute, timeBased);
        }

        public <Value> Builder addPositionalLayer(EnvironmentAttribute<Value> environmentAttribute, EnvironmentAttributeLayer.Positional<Value> positional) {
            return this.addLayer(environmentAttribute, positional);
        }

        private <Value> Builder addLayer(EnvironmentAttribute<Value> environmentAttribute2, EnvironmentAttributeLayer<Value> environmentAttributeLayer) {
            this.layersByAttribute.computeIfAbsent(environmentAttribute2, environmentAttribute -> new ArrayList()).add(environmentAttributeLayer);
            return this;
        }

        public Builder addTimelineLayer(Holder<Timeline> holder, LongSupplier longSupplier) {
            for (EnvironmentAttribute<?> environmentAttribute : holder.value().attributes()) {
                this.addTimelineLayerForAttribute(holder, environmentAttribute, longSupplier);
            }
            return this;
        }

        private <Value> void addTimelineLayerForAttribute(Holder<Timeline> holder, EnvironmentAttribute<Value> environmentAttribute, LongSupplier longSupplier) {
            this.addTimeBasedLayer(environmentAttribute, holder.value().createTrackSampler(environmentAttribute, longSupplier));
        }

        public EnvironmentAttributeSystem build() {
            return new EnvironmentAttributeSystem(this.layersByAttribute);
        }
    }
}

