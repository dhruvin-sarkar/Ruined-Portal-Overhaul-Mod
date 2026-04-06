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
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class SpriteIconButton
extends Button {
    protected final WidgetSprites sprite;
    protected final int spriteWidth;
    protected final int spriteHeight;

    SpriteIconButton(int i, int j, Component component, int k, int l, WidgetSprites widgetSprites, Button.OnPress onPress, @Nullable Component component2, @Nullable Button.CreateNarration createNarration) {
        super(0, 0, i, j, component, onPress, createNarration == null ? DEFAULT_NARRATION : createNarration);
        if (component2 != null) {
            this.setTooltip(Tooltip.create(component2));
        }
        this.spriteWidth = k;
        this.spriteHeight = l;
        this.sprite = widgetSprites;
    }

    protected void renderSprite(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite.get(this.isActive(), this.isHoveredOrFocused()), i, j, this.spriteWidth, this.spriteHeight, this.alpha);
    }

    public static Builder builder(Component component, Button.OnPress onPress, boolean bl) {
        return new Builder(component, onPress, bl);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        private final boolean iconOnly;
        private int width = 150;
        private int height = 20;
        private @Nullable WidgetSprites sprite;
        private int spriteWidth;
        private int spriteHeight;
        private @Nullable Component tooltip;
        private @Nullable Button.CreateNarration narration;

        public Builder(Component component, Button.OnPress onPress, boolean bl) {
            this.message = component;
            this.onPress = onPress;
            this.iconOnly = bl;
        }

        public Builder width(int i) {
            this.width = i;
            return this;
        }

        public Builder size(int i, int j) {
            this.width = i;
            this.height = j;
            return this;
        }

        public Builder sprite(Identifier identifier, int i, int j) {
            this.sprite = new WidgetSprites(identifier);
            this.spriteWidth = i;
            this.spriteHeight = j;
            return this;
        }

        public Builder sprite(WidgetSprites widgetSprites, int i, int j) {
            this.sprite = widgetSprites;
            this.spriteWidth = i;
            this.spriteHeight = j;
            return this;
        }

        public Builder withTootip() {
            this.tooltip = this.message;
            return this;
        }

        public Builder narration(Button.CreateNarration createNarration) {
            this.narration = createNarration;
            return this;
        }

        public SpriteIconButton build() {
            if (this.sprite == null) {
                throw new IllegalStateException("Sprite not set");
            }
            if (this.iconOnly) {
                return new CenteredIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.tooltip, this.narration);
            }
            return new TextAndIcon(this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.tooltip, this.narration);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextAndIcon
    extends SpriteIconButton {
        protected TextAndIcon(int i, int j, Component component, int k, int l, WidgetSprites widgetSprites, Button.OnPress onPress, @Nullable Component component2, @Nullable Button.CreateNarration createNarration) {
            super(i, j, component, k, l, widgetSprites, onPress, component2, createNarration);
        }

        @Override
        public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
            this.renderDefaultSprite(guiGraphics);
            int k = this.getX() + 2;
            int l = this.getX() + this.getWidth() - this.spriteWidth - 4;
            int m = this.getX() + this.getWidth() / 2;
            ActiveTextCollector activeTextCollector = guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE);
            activeTextCollector.acceptScrolling(this.getMessage(), m, k, l, this.getY(), this.getY() + this.getHeight());
            int n = this.getX() + this.getWidth() - this.spriteWidth - 2;
            int o = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            this.renderSprite(guiGraphics, n, o);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CenteredIcon
    extends SpriteIconButton {
        protected CenteredIcon(int i, int j, Component component, int k, int l, WidgetSprites widgetSprites, Button.OnPress onPress, @Nullable Component component2, @Nullable Button.CreateNarration createNarration) {
            super(i, j, component, k, l, widgetSprites, onPress, component2, createNarration);
        }

        @Override
        public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
            this.renderDefaultSprite(guiGraphics);
            int k = this.getX() + this.getWidth() / 2 - this.spriteWidth / 2;
            int l = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            this.renderSprite(guiGraphics, k, l);
        }
    }
}

