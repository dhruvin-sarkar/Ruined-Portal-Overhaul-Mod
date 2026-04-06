/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

public interface ColorMapColorUtil {
    public static int get(double d, double e, int[] is, int i) {
        int k = (int)((1.0 - (e *= d)) * 255.0);
        int j = (int)((1.0 - d) * 255.0);
        int l = k << 8 | j;
        if (l >= is.length) {
            return i;
        }
        return is[l];
    }
}

