/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector2i
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public class ScrollWheelHandler {
    private double accumulatedScrollX;
    private double accumulatedScrollY;

    public Vector2i onMouseScroll(double d, double e) {
        if (this.accumulatedScrollX != 0.0 && Math.signum(d) != Math.signum(this.accumulatedScrollX)) {
            this.accumulatedScrollX = 0.0;
        }
        if (this.accumulatedScrollY != 0.0 && Math.signum(e) != Math.signum(this.accumulatedScrollY)) {
            this.accumulatedScrollY = 0.0;
        }
        this.accumulatedScrollX += d;
        this.accumulatedScrollY += e;
        int i = (int)this.accumulatedScrollX;
        int j = (int)this.accumulatedScrollY;
        if (i == 0 && j == 0) {
            return new Vector2i(0, 0);
        }
        this.accumulatedScrollX -= (double)i;
        this.accumulatedScrollY -= (double)j;
        return new Vector2i(i, j);
    }

    public static int getNextScrollWheelSelection(double d, int i, int j) {
        int k = (int)Math.signum(d);
        i -= k;
        for (i = Math.max(-1, i); i < 0; i += j) {
        }
        while (i >= j) {
            i -= j;
        }
        return i;
    }
}

