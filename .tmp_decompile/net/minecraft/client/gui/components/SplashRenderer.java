/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

@Environment(value=EnvType.CLIENT)
public class SplashRenderer {
    public static final SplashRenderer CHRISTMAS = new SplashRenderer(SplashManager.CHRISTMAS);
    public static final SplashRenderer NEW_YEAR = new SplashRenderer(SplashManager.NEW_YEAR);
    public static final SplashRenderer HALLOWEEN = new SplashRenderer(SplashManager.HALLOWEEN);
    private static final int WIDTH_OFFSET = 123;
    private static final int HEIGH_OFFSET = 69;
    private static final float TEXT_ANGLE = -0.34906584f;
    private final Component splash;

    public SplashRenderer(Component component) {
        this.splash = component;
    }

    public void render(GuiGraphics guiGraphics, int i, Font font, float f) {
        int j = font.width(this.splash);
        ActiveTextCollector activeTextCollector = guiGraphics.textRenderer();
        float g = 1.8f - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
        float h = g * 100.0f / (float)(j + 32);
        Matrix3x2f matrix3x2f = new Matrix3x2f(activeTextCollector.defaultParameters().pose()).translate((float)i / 2.0f + 123.0f, 69.0f).rotate(-0.34906584f).scale(h);
        ActiveTextCollector.Parameters parameters = activeTextCollector.defaultParameters().withOpacity(f).withPose((Matrix3x2fc)matrix3x2f);
        activeTextCollector.accept(TextAlignment.LEFT, -j / 2, -8, parameters, this.splash);
    }
}

