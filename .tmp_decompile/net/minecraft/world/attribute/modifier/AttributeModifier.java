/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.BooleanModifier;
import net.minecraft.world.attribute.modifier.ColorModifier;
import net.minecraft.world.attribute.modifier.FloatModifier;

public interface AttributeModifier<Subject, Argument> {
    public static final Map<OperationId, AttributeModifier<Boolean, ?>> BOOLEAN_LIBRARY = Map.of((Object)OperationId.AND, (Object)BooleanModifier.AND, (Object)OperationId.NAND, (Object)BooleanModifier.NAND, (Object)OperationId.OR, (Object)BooleanModifier.OR, (Object)OperationId.NOR, (Object)BooleanModifier.NOR, (Object)OperationId.XOR, (Object)BooleanModifier.XOR, (Object)OperationId.XNOR, (Object)BooleanModifier.XNOR);
    public static final Map<OperationId, AttributeModifier<Float, ?>> FLOAT_LIBRARY = Map.of((Object)OperationId.ALPHA_BLEND, FloatModifier.ALPHA_BLEND, (Object)OperationId.ADD, FloatModifier.ADD, (Object)OperationId.SUBTRACT, FloatModifier.SUBTRACT, (Object)OperationId.MULTIPLY, FloatModifier.MULTIPLY, (Object)OperationId.MINIMUM, FloatModifier.MINIMUM, (Object)OperationId.MAXIMUM, FloatModifier.MAXIMUM);
    public static final Map<OperationId, AttributeModifier<Integer, ?>> RGB_COLOR_LIBRARY = Map.of((Object)OperationId.ALPHA_BLEND, ColorModifier.ALPHA_BLEND, (Object)OperationId.ADD, ColorModifier.ADD, (Object)OperationId.SUBTRACT, ColorModifier.SUBTRACT, (Object)OperationId.MULTIPLY, ColorModifier.MULTIPLY_RGB, (Object)OperationId.BLEND_TO_GRAY, ColorModifier.BLEND_TO_GRAY);
    public static final Map<OperationId, AttributeModifier<Integer, ?>> ARGB_COLOR_LIBRARY = Map.of((Object)OperationId.ALPHA_BLEND, ColorModifier.ALPHA_BLEND, (Object)OperationId.ADD, ColorModifier.ADD, (Object)OperationId.SUBTRACT, ColorModifier.SUBTRACT, (Object)OperationId.MULTIPLY, ColorModifier.MULTIPLY_ARGB, (Object)OperationId.BLEND_TO_GRAY, ColorModifier.BLEND_TO_GRAY);

    public static <Value> AttributeModifier<Value, Value> override() {
        return OverrideModifier.INSTANCE;
    }

    public Subject apply(Subject var1, Argument var2);

    public Codec<Argument> argumentCodec(EnvironmentAttribute<Subject> var1);

    public LerpFunction<Argument> argumentKeyframeLerp(EnvironmentAttribute<Subject> var1);

    public record OverrideModifier<Value>() implements AttributeModifier<Value, Value>
    {
        static final OverrideModifier<?> INSTANCE = new OverrideModifier();

        @Override
        public Value apply(Value object, Value object2) {
            return object2;
        }

        @Override
        public Codec<Value> argumentCodec(EnvironmentAttribute<Value> environmentAttribute) {
            return environmentAttribute.valueCodec();
        }

        @Override
        public LerpFunction<Value> argumentKeyframeLerp(EnvironmentAttribute<Value> environmentAttribute) {
            return environmentAttribute.type().keyframeLerp();
        }
    }

    public static enum OperationId implements StringRepresentable
    {
        OVERRIDE("override"),
        ALPHA_BLEND("alpha_blend"),
        ADD("add"),
        SUBTRACT("subtract"),
        MULTIPLY("multiply"),
        BLEND_TO_GRAY("blend_to_gray"),
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        AND("and"),
        NAND("nand"),
        OR("or"),
        NOR("nor"),
        XOR("xor"),
        XNOR("xnor");

        public static final Codec<OperationId> CODEC;
        private final String name;

        private OperationId(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(OperationId::values);
        }
    }
}

