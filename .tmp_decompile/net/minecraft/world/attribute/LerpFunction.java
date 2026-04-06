/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public interface LerpFunction<T> {
    public static LerpFunction<Float> ofFloat() {
        return Mth::lerp;
    }

    public static LerpFunction<Float> ofDegrees(float f) {
        return (g, float_, float2) -> {
            float h = Mth.wrapDegrees(float2.floatValue() - float_.floatValue());
            if (Math.abs(h) >= f) {
                return float2;
            }
            return Float.valueOf(float_.floatValue() + g * h);
        };
    }

    public static <T> LerpFunction<T> ofConstant() {
        return (f, object, object2) -> object;
    }

    public static <T> LerpFunction<T> ofStep(float f) {
        return (g, object, object2) -> g >= f ? object2 : object;
    }

    public static LerpFunction<Integer> ofColor() {
        return ARGB::srgbLerp;
    }

    public T apply(float var1, T var2, T var3);
}

