/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record HitboxRenderState(double x0, double y0, double z0, double x1, double y1, double z1, float offsetX, float offsetY, float offsetZ, float red, float green, float blue) {
    public HitboxRenderState(double d, double e, double f, double g, double h, double i, float j, float k, float l) {
        this(d, e, f, g, h, i, 0.0f, 0.0f, 0.0f, j, k, l);
    }
}

