/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  java.lang.MatchException
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public enum BooleanModifier implements AttributeModifier<Boolean, Boolean>
{
    AND,
    NAND,
    OR,
    NOR,
    XOR,
    XNOR;


    @Override
    public Boolean apply(Boolean boolean_, Boolean boolean2) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> boolean2 != false && boolean_ != false;
            case 1 -> boolean2 == false || boolean_ == false;
            case 2 -> boolean2 != false || boolean_ != false;
            case 3 -> boolean2 == false && boolean_ == false;
            case 4 -> boolean2 ^ boolean_;
            case 5 -> boolean2 == boolean_;
        };
    }

    @Override
    public Codec<Boolean> argumentCodec(EnvironmentAttribute<Boolean> environmentAttribute) {
        return Codec.BOOL;
    }

    @Override
    public LerpFunction<Boolean> argumentKeyframeLerp(EnvironmentAttribute<Boolean> environmentAttribute) {
        return LerpFunction.ofConstant();
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((Boolean)object, (Boolean)object2);
    }
}

