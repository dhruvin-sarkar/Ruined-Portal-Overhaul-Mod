/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.entity.boss.enderdragon;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import net.minecraft.util.Mth;

public class DragonFlightHistory {
    public static final int LENGTH = 64;
    private static final int MASK = 63;
    private final Sample[] samples = new Sample[64];
    private int head = -1;

    public DragonFlightHistory() {
        Arrays.fill((Object[])this.samples, (Object)new Sample(0.0, 0.0f));
    }

    public void copyFrom(DragonFlightHistory dragonFlightHistory) {
        System.arraycopy(dragonFlightHistory.samples, 0, this.samples, 0, 64);
        this.head = dragonFlightHistory.head;
    }

    public void record(double d, float f) {
        Sample sample = new Sample(d, f);
        if (this.head < 0) {
            Arrays.fill((Object[])this.samples, (Object)sample);
        }
        if (++this.head == 64) {
            this.head = 0;
        }
        this.samples[this.head] = sample;
    }

    public Sample get(int i) {
        return this.samples[this.head - i & 0x3F];
    }

    public Sample get(int i, float f) {
        Sample sample = this.get(i);
        Sample sample2 = this.get(i + 1);
        return new Sample(Mth.lerp((double)f, sample2.y, sample.y), Mth.rotLerp(f, sample2.yRot, sample.yRot));
    }

    public static final class Sample
    extends Record {
        final double y;
        final float yRot;

        public Sample(double d, float f) {
            this.y = d;
            this.yRot = f;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this, object);
        }

        public double y() {
            return this.y;
        }

        public float yRot() {
            return this.yRot;
        }
    }
}

