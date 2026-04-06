/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.util.EasingType;
import net.minecraft.util.Mth;

public class BinaryAnimator {
    private final int animationLength;
    private final EasingType easing;
    private int ticks;
    private int ticksOld;

    public BinaryAnimator(int i, EasingType easingType) {
        this.animationLength = i;
        this.easing = easingType;
    }

    public BinaryAnimator(int i) {
        this(i, EasingType.LINEAR);
    }

    public void tick(boolean bl) {
        this.ticksOld = this.ticks;
        if (bl) {
            if (this.ticks < this.animationLength) {
                ++this.ticks;
            }
        } else if (this.ticks > 0) {
            --this.ticks;
        }
    }

    public float getFactor(float f) {
        float g = Mth.lerp(f, this.ticksOld, this.ticks) / (float)this.animationLength;
        return this.easing.apply(g);
    }
}

