/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.color;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

@Environment(value=EnvType.CLIENT)
public class ColorLerper {
    public static final DyeColor[] MUSIC_NOTE_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.CYAN, DyeColor.GREEN, DyeColor.LIME, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.PINK, DyeColor.RED, DyeColor.MAGENTA};

    public static int getLerpedColor(Type type, float f) {
        int i = Mth.floor(f);
        int j = i / type.colorDuration;
        int k = type.colors.length;
        int l = j % k;
        int m = (j + 1) % k;
        float g = ((float)(i % type.colorDuration) + Mth.frac(f)) / (float)type.colorDuration;
        int n = type.getColor(type.colors[l]);
        int o = type.getColor(type.colors[m]);
        return ARGB.srgbLerp(g, n, o);
    }

    static int getModifiedColor(DyeColor dyeColor, float f) {
        if (dyeColor == DyeColor.WHITE) {
            return -1644826;
        }
        int i = dyeColor.getTextureDiffuseColor();
        return ARGB.color(255, Mth.floor((float)ARGB.red(i) * f), Mth.floor((float)ARGB.green(i) * f), Mth.floor((float)ARGB.blue(i) * f));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SHEEP(25, DyeColor.values(), 0.75f),
        MUSIC_NOTE(30, MUSIC_NOTE_COLORS, 1.25f);

        final int colorDuration;
        private final Map<DyeColor, Integer> colorByDye;
        final DyeColor[] colors;

        private Type(int j, DyeColor[] dyeColors, float f) {
            this.colorDuration = j;
            this.colorByDye = Maps.newHashMap(Arrays.stream(dyeColors).collect(Collectors.toMap(dyeColor -> dyeColor, dyeColor -> ColorLerper.getModifiedColor(dyeColor, f))));
            this.colors = dyeColors;
        }

        public final int getColor(DyeColor dyeColor) {
            return this.colorByDye.get(dyeColor);
        }
    }
}

