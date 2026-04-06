/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3x2f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.render.state.pip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.ScreenArea;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface PictureInPictureRenderState
extends ScreenArea {
    public static final Matrix3x2f IDENTITY_POSE = new Matrix3x2f();

    public int x0();

    public int x1();

    public int y0();

    public int y1();

    public float scale();

    default public Matrix3x2f pose() {
        return IDENTITY_POSE;
    }

    public @Nullable ScreenRectangle scissorArea();

    public static @Nullable ScreenRectangle getBounds(int i, int j, int k, int l, @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = new ScreenRectangle(i, j, k - i, l - j);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}

