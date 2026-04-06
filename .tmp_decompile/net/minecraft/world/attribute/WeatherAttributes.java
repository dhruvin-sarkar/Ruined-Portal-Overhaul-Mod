/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.attribute;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.ColorModifier;
import net.minecraft.world.attribute.modifier.FloatModifier;
import net.minecraft.world.attribute.modifier.FloatWithAlpha;
import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.Timelines;

public class WeatherAttributes {
    public static final EnvironmentAttributeMap RAIN = EnvironmentAttributeMap.builder().modify(EnvironmentAttributes.SKY_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.6f, 0.75f)).modify(EnvironmentAttributes.FOG_COLOR, ColorModifier.MULTIPLY_RGB, ARGB.colorFromFloat(1.0f, 0.5f, 0.5f, 0.6f)).modify(EnvironmentAttributes.CLOUD_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.24f, 0.5f)).modify(EnvironmentAttributes.SKY_LIGHT_LEVEL, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(4.0f, 0.3125f)).modify(EnvironmentAttributes.SKY_LIGHT_COLOR, ColorModifier.ALPHA_BLEND, ARGB.color(0.3125f, Timelines.NIGHT_SKY_LIGHT_COLOR)).modify(EnvironmentAttributes.SKY_LIGHT_FACTOR, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(0.24f, 0.3125f)).set(EnvironmentAttributes.STAR_BRIGHTNESS, Float.valueOf(0.0f)).modify(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ColorModifier.MULTIPLY_ARGB, ARGB.colorFromFloat(1.0f, 0.5f, 0.5f, 0.6f)).set(EnvironmentAttributes.BEES_STAY_IN_HIVE, true).build();
    public static final EnvironmentAttributeMap THUNDER = EnvironmentAttributeMap.builder().modify(EnvironmentAttributes.SKY_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.24f, 0.94f)).modify(EnvironmentAttributes.FOG_COLOR, ColorModifier.MULTIPLY_RGB, ARGB.colorFromFloat(1.0f, 0.25f, 0.25f, 0.3f)).modify(EnvironmentAttributes.CLOUD_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.095f, 0.94f)).modify(EnvironmentAttributes.SKY_LIGHT_LEVEL, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(4.0f, 0.52734375f)).modify(EnvironmentAttributes.SKY_LIGHT_COLOR, ColorModifier.ALPHA_BLEND, ARGB.color(0.52734375f, Timelines.NIGHT_SKY_LIGHT_COLOR)).modify(EnvironmentAttributes.SKY_LIGHT_FACTOR, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(0.24f, 0.52734375f)).set(EnvironmentAttributes.STAR_BRIGHTNESS, Float.valueOf(0.0f)).modify(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ColorModifier.MULTIPLY_ARGB, ARGB.colorFromFloat(1.0f, 0.25f, 0.25f, 0.3f)).set(EnvironmentAttributes.BEES_STAY_IN_HIVE, true).build();
    private static final Set<EnvironmentAttribute<?>> WEATHER_ATTRIBUTES = Sets.union(RAIN.keySet(), THUNDER.keySet());

    public static void addBuiltinLayers(EnvironmentAttributeSystem.Builder builder, WeatherAccess weatherAccess) {
        for (EnvironmentAttribute<?> environmentAttribute : WEATHER_ATTRIBUTES) {
            WeatherAttributes.addLayer(builder, weatherAccess, environmentAttribute);
        }
    }

    private static <Value> void addLayer(EnvironmentAttributeSystem.Builder builder, WeatherAccess weatherAccess, EnvironmentAttribute<Value> environmentAttribute) {
        EnvironmentAttributeMap.Entry entry = RAIN.get(environmentAttribute);
        EnvironmentAttributeMap.Entry entry2 = THUNDER.get(environmentAttribute);
        builder.addTimeBasedLayer(environmentAttribute, (object, i) -> {
            Object object2;
            float f = weatherAccess.thunderLevel();
            float g = weatherAccess.rainLevel() - f;
            if (entry != null && g > 0.0f) {
                object2 = entry.applyModifier(object);
                object = environmentAttribute.type().stateChangeLerp().apply(g, object, object2);
            }
            if (entry2 != null && f > 0.0f) {
                object2 = entry2.applyModifier(object);
                object = environmentAttribute.type().stateChangeLerp().apply(f, object, object2);
            }
            return object;
        });
    }

    public static interface WeatherAccess {
        public static WeatherAccess from(final Level level) {
            return new WeatherAccess(){

                @Override
                public float rainLevel() {
                    return level.getRainLevel(1.0f);
                }

                @Override
                public float thunderLevel() {
                    return level.getThunderLevel(1.0f);
                }
            };
        }

        public float rainLevel();

        public float thunderLevel();
    }
}

