/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Checkbox
extends AbstractButton {
    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final OnValueChange onValueChange;
    private final MultiLineTextWidget textWidget;

    Checkbox(int i, int j, int k, Component component, Font font, boolean bl, OnValueChange onValueChange) {
        super(i, j, 0, 0, component);
        this.textWidget = new MultiLineTextWidget(component, font);
        this.textWidget.setMaxRows(2);
        this.width = this.adjustWidth(k, font);
        this.height = this.getAdjustedHeight(font);
        this.selected = bl;
        this.onValueChange = onValueChange;
    }

    public int adjustWidth(int i, Font font) {
        this.width = this.getAdjustedWidth(i, this.getMessage(), font);
        this.textWidget.setMaxWidth(this.width);
        return this.width;
    }

    private int getAdjustedWidth(int i, Component component, Font font) {
        return Math.min(Checkbox.getDefaultWidth(component, font), i);
    }

    private int getAdjustedHeight(Font font) {
        return Math.max(Checkbox.getBoxSize(font), this.textWidget.getHeight());
    }

    static int getDefaultWidth(Component component, Font font) {
        return Checkbox.getBoxSize(font) + 4 + font.width(component);
    }

    public static Builder builder(Component component, Font font) {
        return new Builder(component, font);
    }

    public static int getBoxSize(Font font) {
        return font.lineHeight + 8;
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable(this.selected ? "narration.checkbox.usage.focused.uncheck" : "narration.checkbox.usage.focused.check"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable(this.selected ? "narration.checkbox.usage.hovered.uncheck" : "narration.checkbox.usage.hovered.check"));
            }
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Identifier identifier = this.selected ? (this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE) : (this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE);
        int k = Checkbox.getBoxSize(font);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), k, k, ARGB.white(this.alpha));
        int l = this.getX() + k + 4;
        int m = this.getY() + k / 2 - this.textWidget.getHeight() / 2;
        this.textWidget.setPosition(l, m);
        this.textWidget.visitLines(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.notClickable(this.isHovered())));
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnValueChange {
        public static final OnValueChange NOP = (checkbox, bl) -> {};

        public void onValueChange(Checkbox var1, boolean var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int maxWidth;
        private int x = 0;
        private int y = 0;
        private OnValueChange onValueChange = OnValueChange.NOP;
        private boolean selected = false;
        private @Nullable OptionInstance<Boolean> option = null;
        private @Nullable Tooltip tooltip = null;

        Builder(Component component, Font font) {
            this.message = component;
            this.font = font;
            this.maxWidth = Checkbox.getDefaultWidth(component, font);
        }

        public Builder pos(int i, int j) {
            this.x = i;
            this.y = j;
            return this;
        }

        public Builder onValueChange(OnValueChange onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public Builder selected(boolean bl) {
            this.selected = bl;
            this.option = null;
            return this;
        }

        public Builder selected(OptionInstance<Boolean> optionInstance) {
            this.option = optionInstance;
            this.selected = optionInstance.get();
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder maxWidth(int i) {
            this.maxWidth = i;
            return this;
        }

        public Checkbox build() {
            OnValueChange onValueChange = this.option == null ? this.onValueChange : (checkbox, bl) -> {
                this.option.set(bl);
                this.onValueChange.onValueChange(checkbox, bl);
            };
            Checkbox checkbox2 = new Checkbox(this.x, this.y, this.maxWidth, this.message, this.font, this.selected, onValueChange);
            checkbox2.setTooltip(this.tooltip);
            return checkbox2;
        }
    }
}

