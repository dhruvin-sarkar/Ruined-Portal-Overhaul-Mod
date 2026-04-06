/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
enum AdvancementTabType {
    ABOVE(new Sprites(Identifier.withDefaultNamespace("advancements/tab_above_left_selected"), Identifier.withDefaultNamespace("advancements/tab_above_middle_selected"), Identifier.withDefaultNamespace("advancements/tab_above_right_selected")), new Sprites(Identifier.withDefaultNamespace("advancements/tab_above_left"), Identifier.withDefaultNamespace("advancements/tab_above_middle"), Identifier.withDefaultNamespace("advancements/tab_above_right")), 28, 32, 8),
    BELOW(new Sprites(Identifier.withDefaultNamespace("advancements/tab_below_left_selected"), Identifier.withDefaultNamespace("advancements/tab_below_middle_selected"), Identifier.withDefaultNamespace("advancements/tab_below_right_selected")), new Sprites(Identifier.withDefaultNamespace("advancements/tab_below_left"), Identifier.withDefaultNamespace("advancements/tab_below_middle"), Identifier.withDefaultNamespace("advancements/tab_below_right")), 28, 32, 8),
    LEFT(new Sprites(Identifier.withDefaultNamespace("advancements/tab_left_top_selected"), Identifier.withDefaultNamespace("advancements/tab_left_middle_selected"), Identifier.withDefaultNamespace("advancements/tab_left_bottom_selected")), new Sprites(Identifier.withDefaultNamespace("advancements/tab_left_top"), Identifier.withDefaultNamespace("advancements/tab_left_middle"), Identifier.withDefaultNamespace("advancements/tab_left_bottom")), 32, 28, 5),
    RIGHT(new Sprites(Identifier.withDefaultNamespace("advancements/tab_right_top_selected"), Identifier.withDefaultNamespace("advancements/tab_right_middle_selected"), Identifier.withDefaultNamespace("advancements/tab_right_bottom_selected")), new Sprites(Identifier.withDefaultNamespace("advancements/tab_right_top"), Identifier.withDefaultNamespace("advancements/tab_right_middle"), Identifier.withDefaultNamespace("advancements/tab_right_bottom")), 32, 28, 5);

    private final Sprites selectedSprites;
    private final Sprites unselectedSprites;
    private final int width;
    private final int height;
    private final int max;

    private AdvancementTabType(Sprites sprites, Sprites sprites2, int j, int k, int l) {
        this.selectedSprites = sprites;
        this.unselectedSprites = sprites2;
        this.width = j;
        this.height = k;
        this.max = l;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(GuiGraphics guiGraphics, int i, int j, boolean bl, int k) {
        Sprites sprites;
        Sprites sprites2 = sprites = bl ? this.selectedSprites : this.unselectedSprites;
        Identifier identifier = k == 0 ? sprites.first() : (k == this.max - 1 ? sprites.last() : sprites.middle());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, i, j, this.width, this.height);
    }

    public void drawIcon(GuiGraphics guiGraphics, int i, int j, int k, ItemStack itemStack) {
        int l = i + this.getX(k);
        int m = j + this.getY(k);
        switch (this.ordinal()) {
            case 0: {
                l += 6;
                m += 9;
                break;
            }
            case 1: {
                l += 6;
                m += 6;
                break;
            }
            case 2: {
                l += 10;
                m += 5;
                break;
            }
            case 3: {
                l += 6;
                m += 5;
            }
        }
        guiGraphics.renderFakeItem(itemStack, l, m);
    }

    public int getX(int i) {
        switch (this.ordinal()) {
            case 0: {
                return (this.width + 4) * i;
            }
            case 1: {
                return (this.width + 4) * i;
            }
            case 2: {
                return -this.width + 4;
            }
            case 3: {
                return 248;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public int getY(int i) {
        switch (this.ordinal()) {
            case 0: {
                return -this.height + 4;
            }
            case 1: {
                return 136;
            }
            case 2: {
                return this.height * i;
            }
            case 3: {
                return this.height * i;
            }
        }
        throw new UnsupportedOperationException("Don't know what this tab type is!" + String.valueOf((Object)this));
    }

    public boolean isMouseOver(int i, int j, int k, double d, double e) {
        int l = i + this.getX(k);
        int m = j + this.getY(k);
        return d > (double)l && d < (double)(l + this.width) && e > (double)m && e < (double)(m + this.height);
    }

    @Environment(value=EnvType.CLIENT)
    record Sprites(Identifier first, Identifier middle, Identifier last) {
    }
}

