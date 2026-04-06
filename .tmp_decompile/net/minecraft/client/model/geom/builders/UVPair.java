/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.geom.builders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record UVPair(float u, float v) {
    public String toString() {
        return "(" + this.u + "," + this.v + ")";
    }

    public static long pack(float f, float g) {
        long l = (long)Float.floatToIntBits(f) & 0xFFFFFFFFL;
        long m = (long)Float.floatToIntBits(g) & 0xFFFFFFFFL;
        return l << 32 | m;
    }

    public static float unpackU(long l) {
        int i = (int)(l >> 32);
        return Float.intBitsToFloat(i);
    }

    public static float unpackV(long l) {
        return Float.intBitsToFloat((int)l);
    }
}

