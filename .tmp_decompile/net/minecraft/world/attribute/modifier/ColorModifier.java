/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public interface ColorModifier<Argument>
extends AttributeModifier<Integer, Argument> {
    public static final ColorModifier<Integer> ALPHA_BLEND = new ColorModifier<Integer>(){

        @Override
        public Integer apply(Integer integer, Integer integer2) {
            return ARGB.alphaBlend(integer, integer2);
        }

        @Override
        public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return ExtraCodecs.STRING_ARGB_COLOR;
        }

        @Override
        public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return LerpFunction.ofColor();
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (Integer)object2);
        }
    };
    public static final ColorModifier<Integer> ADD = ARGB::addRgb;
    public static final ColorModifier<Integer> SUBTRACT = ARGB::subtractRgb;
    public static final ColorModifier<Integer> MULTIPLY_RGB = ARGB::multiply;
    public static final ColorModifier<Integer> MULTIPLY_ARGB = ARGB::multiply;
    public static final ColorModifier<BlendToGray> BLEND_TO_GRAY = new ColorModifier<BlendToGray>(){

        @Override
        public Integer apply(Integer integer, BlendToGray blendToGray) {
            int i = ARGB.scaleRGB(ARGB.greyscale(integer), blendToGray.brightness);
            return ARGB.srgbLerp(blendToGray.factor, integer, i);
        }

        @Override
        public Codec<BlendToGray> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return BlendToGray.CODEC;
        }

        @Override
        public LerpFunction<BlendToGray> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return (f, blendToGray, blendToGray2) -> new BlendToGray(Mth.lerp(f, blendToGray.brightness, blendToGray2.brightness), Mth.lerp(f, blendToGray.factor, blendToGray2.factor));
        }

        @Override
        public /* synthetic */ Object apply(Object object, Object object2) {
            return this.apply((Integer)object, (BlendToGray)((Object)object2));
        }
    };

    @FunctionalInterface
    public static interface RgbModifier
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return ExtraCodecs.STRING_RGB_COLOR;
        }

        @Override
        default public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return LerpFunction.ofColor();
        }
    }

    @FunctionalInterface
    public static interface ArgbModifier
    extends ColorModifier<Integer> {
        @Override
        default public Codec<Integer> argumentCodec(EnvironmentAttribute<Integer> environmentAttribute) {
            return Codec.either(ExtraCodecs.STRING_ARGB_COLOR, ExtraCodecs.RGB_COLOR_CODEC).xmap(Either::unwrap, integer -> ARGB.alpha(integer) == 255 ? Either.right((Object)integer) : Either.left((Object)integer));
        }

        @Override
        default public LerpFunction<Integer> argumentKeyframeLerp(EnvironmentAttribute<Integer> environmentAttribute) {
            return LerpFunction.ofColor();
        }
    }

    public static final class BlendToGray
    extends Record {
        final float brightness;
        final float factor;
        public static final Codec<BlendToGray> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("brightness").forGetter(BlendToGray::brightness), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("factor").forGetter(BlendToGray::factor)).apply((Applicative)instance, BlendToGray::new));

        public BlendToGray(float f, float g) {
            this.brightness = f;
            this.factor = g;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlendToGray.class, "brightness;factor", "brightness", "factor"}, this, object);
        }

        public float brightness() {
            return this.brightness;
        }

        public float factor() {
            return this.factor;
        }
    }
}

