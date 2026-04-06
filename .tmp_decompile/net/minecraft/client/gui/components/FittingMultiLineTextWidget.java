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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class FittingMultiLineTextWidget
extends AbstractTextAreaWidget {
    private final Font font;
    private final MultiLineTextWidget multilineWidget;

    public FittingMultiLineTextWidget(int i, int j, int k, int l, Component component, Font font) {
        super(i, j, k, l, component);
        this.font = font;
        this.multilineWidget = new MultiLineTextWidget(component, font).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    @Override
    public void setWidth(int i) {
        super.setWidth(i);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    @Override
    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    public void minimizeHeight() {
        if (!this.showingScrollBar()) {
            this.setHeight(this.getInnerHeight() + this.totalInnerPadding());
        }
    }

    @Override
    protected double scrollRate() {
        return this.font.lineHeight;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    public boolean showingScrollBar() {
        return super.scrollbarVisible();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)this.getInnerLeft(), (float)this.getInnerTop());
        this.multilineWidget.render(guiGraphics, i, j, f);
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void setMessage(Component component) {
        super.setMessage(component);
        this.multilineWidget.setMessage(component);
    }
}

