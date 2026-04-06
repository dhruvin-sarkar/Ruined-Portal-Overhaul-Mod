/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

@Environment(value=EnvType.CLIENT)
public class PlayerFaceRenderer {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;

    public static void draw(GuiGraphics guiGraphics, PlayerSkin playerSkin, int i, int j, int k) {
        PlayerFaceRenderer.draw(guiGraphics, playerSkin, i, j, k, -1);
    }

    public static void draw(GuiGraphics guiGraphics, PlayerSkin playerSkin, int i, int j, int k, int l) {
        PlayerFaceRenderer.draw(guiGraphics, playerSkin.body().texturePath(), i, j, k, true, false, l);
    }

    public static void draw(GuiGraphics guiGraphics, Identifier identifier, int i, int j, int k, boolean bl, boolean bl2, int l) {
        int m = 8 + (bl2 ? 8 : 0);
        int n = 8 * (bl2 ? -1 : 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, i, j, 8.0f, m, k, k, 8, n, 64, 64, l);
        if (bl) {
            PlayerFaceRenderer.drawHat(guiGraphics, identifier, i, j, k, bl2, l);
        }
    }

    private static void drawHat(GuiGraphics guiGraphics, Identifier identifier, int i, int j, int k, boolean bl, int l) {
        int m = 8 + (bl ? 8 : 0);
        int n = 8 * (bl ? -1 : 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, i, j, 40.0f, m, k, k, 8, n, 64, 64, l);
    }
}

