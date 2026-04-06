/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public interface AttributeRange<Value> {
    public static final AttributeRange<Float> UNIT_FLOAT = AttributeRange.ofFloat(0.0f, 1.0f);
    public static final AttributeRange<Float> NON_NEGATIVE_FLOAT = AttributeRange.ofFloat(0.0f, Float.POSITIVE_INFINITY);

    public static <Value> AttributeRange<Value> any() {
        return new AttributeRange<Value>(){

            @Override
            public DataResult<Value> validate(Value object) {
                return DataResult.success(object);
            }

            @Override
            public Value sanitize(Value object) {
                return object;
            }
        };
    }

    public static AttributeRange<Float> ofFloat(final float f, final float g) {
        return new AttributeRange<Float>(){

            @Override
            public DataResult<Float> validate(Float float_) {
                if (float_.floatValue() >= f && float_.floatValue() <= g) {
                    return DataResult.success((Object)float_);
                }
                return DataResult.error(() -> float_ + " is not in range [" + f + "; " + g + "]");
            }

            @Override
            public Float sanitize(Float float_) {
                if (float_.floatValue() >= f && float_.floatValue() <= g) {
                    return float_;
                }
                return Float.valueOf(Mth.clamp(float_.floatValue(), f, g));
            }
        };
    }

    public DataResult<Value> validate(Value var1);

    public Value sanitize(Value var1);
}

