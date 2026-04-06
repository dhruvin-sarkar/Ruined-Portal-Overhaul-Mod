/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record NumberRangeInput(int width, Component label, String labelFormat, RangeInfo rangeInfo) implements InputControl
{
    public static final MapCodec<NumberRangeInput> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(NumberRangeInput::width), (App)ComponentSerialization.CODEC.fieldOf("label").forGetter(NumberRangeInput::label), (App)Codec.STRING.optionalFieldOf("label_format", (Object)"options.generic_value").forGetter(NumberRangeInput::labelFormat), (App)RangeInfo.MAP_CODEC.forGetter(NumberRangeInput::rangeInfo)).apply((Applicative)instance, NumberRangeInput::new));

    public MapCodec<NumberRangeInput> mapCodec() {
        return MAP_CODEC;
    }

    public Component computeLabel(String string) {
        return Component.translatable(this.labelFormat, this.label, string);
    }

    public record RangeInfo(float start, float end, Optional<Float> initial, Optional<Float> step) {
        public static final MapCodec<RangeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("start").forGetter(RangeInfo::start), (App)Codec.FLOAT.fieldOf("end").forGetter(RangeInfo::end), (App)Codec.FLOAT.optionalFieldOf("initial").forGetter(RangeInfo::initial), (App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("step").forGetter(RangeInfo::step)).apply((Applicative)instance, RangeInfo::new)).validate(rangeInfo -> {
            if (rangeInfo.initial.isPresent()) {
                double d = rangeInfo.initial.get().floatValue();
                double e = Math.min(rangeInfo.start, rangeInfo.end);
                double f = Math.max(rangeInfo.start, rangeInfo.end);
                if (d < e || d > f) {
                    return DataResult.error(() -> "Initial value " + d + " is outside of range [" + e + ", " + f + "]");
                }
            }
            return DataResult.success((Object)rangeInfo);
        });

        public float computeScaledValue(float f) {
            float j;
            int k;
            float g = Mth.lerp(f, this.start, this.end);
            if (this.step.isEmpty()) {
                return g;
            }
            float h = this.step.get().floatValue();
            float i = this.initialScaledValue();
            float l = i + (float)(k = Math.round((j = g - i) / h)) * h;
            if (!this.isOutOfRange(l)) {
                return l;
            }
            int m = k - Mth.sign(k);
            return i + (float)m * h;
        }

        private boolean isOutOfRange(float f) {
            float g = this.scaledValueToSlider(f);
            return (double)g < 0.0 || (double)g > 1.0;
        }

        private float initialScaledValue() {
            if (this.initial.isPresent()) {
                return this.initial.get().floatValue();
            }
            return (this.start + this.end) / 2.0f;
        }

        public float initialSliderValue() {
            float f = this.initialScaledValue();
            return this.scaledValueToSlider(f);
        }

        private float scaledValueToSlider(float f) {
            if (this.start == this.end) {
                return 0.5f;
            }
            return Mth.inverseLerp(f, this.start, this.end);
        }
    }
}

