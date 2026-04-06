/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.Float2FloatFunction
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface BoundedFloatFunction<C> {
    public static final BoundedFloatFunction<Float> IDENTITY = BoundedFloatFunction.createUnlimited(f -> f);

    public float apply(C var1);

    public float minValue();

    public float maxValue();

    public static BoundedFloatFunction<Float> createUnlimited(final Float2FloatFunction float2FloatFunction) {
        return new BoundedFloatFunction<Float>(){

            @Override
            public float apply(Float float_) {
                return ((Float)float2FloatFunction.apply((Object)float_)).floatValue();
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default public <C2> BoundedFloatFunction<C2> comap(final Function<C2, C> function) {
        final BoundedFloatFunction boundedFloatFunction = this;
        return new BoundedFloatFunction<C2>(this){

            @Override
            public float apply(C2 object) {
                return boundedFloatFunction.apply(function.apply(object));
            }

            @Override
            public float minValue() {
                return boundedFloatFunction.minValue();
            }

            @Override
            public float maxValue() {
                return boundedFloatFunction.maxValue();
            }
        };
    }
}

