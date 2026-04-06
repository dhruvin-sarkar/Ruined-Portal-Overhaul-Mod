/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.contextualbar;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(value=EnvType.CLIENT)
public interface ContextualBarRenderer {
    public static final int WIDTH = 182;
    public static final int HEIGHT = 5;
    public static final int MARGIN_BOTTOM = 24;
    public static final ContextualBarRenderer EMPTY = new ContextualBarRenderer(){

        @Override
        public void renderBackground(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        }

        @Override
        public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        }
    };

    default public int left(Window window) {
        return (window.getGuiScaledWidth() - 182) / 2;
    }

    default public int top(Window window) {
        return window.getGuiScaledHeight() - 24 - 5;
    }

    public void renderBackground(GuiGraphics var1, DeltaTracker var2);

    public void render(GuiGraphics var1, DeltaTracker var2);

    public static void renderExperienceLevel(GuiGraphics guiGraphics, Font font, int i) {
        MutableComponent component = Component.translatable("gui.experience.level", i);
        int j = (guiGraphics.guiWidth() - font.width(component)) / 2;
        int k = guiGraphics.guiHeight() - 24 - font.lineHeight - 2;
        guiGraphics.drawString(font, component, j + 1, k, -16777216, false);
        guiGraphics.drawString(font, component, j - 1, k, -16777216, false);
        guiGraphics.drawString(font, component, j, k + 1, -16777216, false);
        guiGraphics.drawString(font, component, j, k - 1, -16777216, false);
        guiGraphics.drawString(font, component, j, k, -8323296, false);
    }
}

