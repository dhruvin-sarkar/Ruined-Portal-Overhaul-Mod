/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public record AttributeType<Value>(Codec<Value> valueCodec, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> modifierLibrary, Codec<AttributeModifier<Value, ?>> modifierCodec, LerpFunction<Value> keyframeLerp, LerpFunction<Value> stateChangeLerp, LerpFunction<Value> spatialLerp, LerpFunction<Value> partialTickLerp) {
    public static <Value> AttributeType<Value> ofInterpolated(Codec<Value> codec, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> map, LerpFunction<Value> lerpFunction) {
        return AttributeType.ofInterpolated(codec, map, lerpFunction, lerpFunction);
    }

    public static <Value> AttributeType<Value> ofInterpolated(Codec<Value> codec, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> map, LerpFunction<Value> lerpFunction, LerpFunction<Value> lerpFunction2) {
        return new AttributeType<Value>(codec, map, AttributeType.createModifierCodec(map), lerpFunction, lerpFunction, lerpFunction, lerpFunction2);
    }

    public static <Value> AttributeType<Value> ofNotInterpolated(Codec<Value> codec, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> map) {
        return new AttributeType<Value>(codec, map, AttributeType.createModifierCodec(map), LerpFunction.ofStep(1.0f), LerpFunction.ofStep(0.0f), LerpFunction.ofStep(0.5f), LerpFunction.ofStep(0.0f));
    }

    public static <Value> AttributeType<Value> ofNotInterpolated(Codec<Value> codec) {
        return AttributeType.ofNotInterpolated(codec, Map.of());
    }

    private static <Value> Codec<AttributeModifier<Value, ?>> createModifierCodec(Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> map) {
        ImmutableBiMap immutableBiMap = ImmutableBiMap.builder().put((Object)AttributeModifier.OperationId.OVERRIDE, AttributeModifier.override()).putAll(map).buildOrThrow();
        return ExtraCodecs.idResolverCodec(AttributeModifier.OperationId.CODEC, arg_0 -> ((ImmutableBiMap)immutableBiMap).get(arg_0), arg_0 -> ((ImmutableBiMap)immutableBiMap.inverse()).get(arg_0));
    }

    public void checkAllowedModifier(AttributeModifier<Value, ?> attributeModifier) {
        if (attributeModifier != AttributeModifier.override() && !this.modifierLibrary.containsValue(attributeModifier)) {
            throw new IllegalArgumentException("Modifier " + String.valueOf(attributeModifier) + " is not valid for " + String.valueOf((Object)this));
        }
    }

    public String toString() {
        return Util.getRegisteredName(BuiltInRegistries.ATTRIBUTE_TYPE, this);
    }
}

