/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import org.jspecify.annotations.Nullable;

public final class EnvironmentAttributeMap {
    public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of());
    public static final Codec<EnvironmentAttributeMap> CODEC = Codec.lazyInitialized(() -> Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(Entry::createCodec)).xmap(EnvironmentAttributeMap::new, environmentAttributeMap -> environmentAttributeMap.entries));
    public static final Codec<EnvironmentAttributeMap> NETWORK_CODEC = CODEC.xmap(EnvironmentAttributeMap::filterSyncable, EnvironmentAttributeMap::filterSyncable);
    public static final Codec<EnvironmentAttributeMap> CODEC_ONLY_POSITIONAL = CODEC.validate(environmentAttributeMap -> {
        List list = environmentAttributeMap.keySet().stream().filter(environmentAttribute -> !environmentAttribute.isPositional()).toList();
        if (!list.isEmpty()) {
            return DataResult.error(() -> "The following attributes cannot be positional: " + String.valueOf(list));
        }
        return DataResult.success((Object)environmentAttributeMap);
    });
    final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries;

    private static EnvironmentAttributeMap filterSyncable(EnvironmentAttributeMap environmentAttributeMap) {
        return new EnvironmentAttributeMap(Map.copyOf((Map)Maps.filterKeys(environmentAttributeMap.entries, EnvironmentAttribute::isSyncable)));
    }

    EnvironmentAttributeMap(Map<EnvironmentAttribute<?>, Entry<?, ?>> map) {
        this.entries = map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <Value> @Nullable Entry<Value, ?> get(EnvironmentAttribute<Value> environmentAttribute) {
        return this.entries.get(environmentAttribute);
    }

    public <Value> Value applyModifier(EnvironmentAttribute<Value> environmentAttribute, Value object) {
        Entry<Value, ?> entry = this.get(environmentAttribute);
        return entry != null ? entry.applyModifier(object) : object;
    }

    public boolean contains(EnvironmentAttribute<?> environmentAttribute) {
        return this.entries.containsKey(environmentAttribute);
    }

    public Set<EnvironmentAttribute<?>> keySet() {
        return this.entries.keySet();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EnvironmentAttributeMap)) return false;
        EnvironmentAttributeMap environmentAttributeMap = (EnvironmentAttributeMap)object;
        if (!this.entries.equals(environmentAttributeMap.entries)) return false;
        return true;
    }

    public int hashCode() {
        return this.entries.hashCode();
    }

    public String toString() {
        return this.entries.toString();
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries = new HashMap();

        Builder() {
        }

        public Builder putAll(EnvironmentAttributeMap environmentAttributeMap) {
            this.entries.putAll(environmentAttributeMap.entries);
            return this;
        }

        public <Value, Parameter> Builder modify(EnvironmentAttribute<Value> environmentAttribute, AttributeModifier<Value, Parameter> attributeModifier, Parameter object) {
            environmentAttribute.type().checkAllowedModifier(attributeModifier);
            this.entries.put(environmentAttribute, new Entry<Value, Parameter>(object, attributeModifier));
            return this;
        }

        public <Value> Builder set(EnvironmentAttribute<Value> environmentAttribute, Value object) {
            return this.modify(environmentAttribute, AttributeModifier.override(), object);
        }

        public EnvironmentAttributeMap build() {
            if (this.entries.isEmpty()) {
                return EMPTY;
            }
            return new EnvironmentAttributeMap(Map.copyOf(this.entries));
        }
    }

    public record Entry<Value, Argument>(Argument argument, AttributeModifier<Value, Argument> modifier) {
        private static <Value> Codec<Entry<Value, ?>> createCodec(EnvironmentAttribute<Value> environmentAttribute) {
            Codec codec = environmentAttribute.type().modifierCodec().dispatch("modifier", Entry::modifier, Util.memoize(attributeModifier -> Entry.createFullCodec(environmentAttribute, attributeModifier)));
            return Codec.either(environmentAttribute.valueCodec(), (Codec)codec).xmap(either -> (Entry)((Object)((Object)either.map(object -> new Entry(object, AttributeModifier.override()), entry -> entry))), entry -> {
                if (entry.modifier == AttributeModifier.override()) {
                    return Either.left(entry.argument());
                }
                return Either.right((Object)entry);
            });
        }

        private static <Value, Argument> MapCodec<Entry<Value, Argument>> createFullCodec(EnvironmentAttribute<Value> environmentAttribute, AttributeModifier<Value, Argument> attributeModifier) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)attributeModifier.argumentCodec(environmentAttribute).fieldOf("argument").forGetter(Entry::argument)).apply((Applicative)instance, object -> new Entry(object, attributeModifier)));
        }

        public Value applyModifier(Value object) {
            return this.modifier.apply(object, this.argument);
        }
    }
}

