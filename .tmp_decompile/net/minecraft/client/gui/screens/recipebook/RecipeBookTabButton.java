/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

@Environment(value=EnvType.CLIENT)
public class RecipeBookTabButton
extends ImageButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/tab"), Identifier.withDefaultNamespace("recipe_book/tab_selected"));
    public static final int WIDTH = 35;
    public static final int HEIGHT = 27;
    private final RecipeBookComponent.TabInfo tabInfo;
    private static final float ANIMATION_TIME = 15.0f;
    private float animationTime;
    private boolean selected = false;

    public RecipeBookTabButton(int i, int j, RecipeBookComponent.TabInfo tabInfo, Button.OnPress onPress) {
        super(i, j, 35, 27, SPRITES, onPress);
        this.tabInfo = tabInfo;
    }

    public void startAnimation(ClientRecipeBook clientRecipeBook, boolean bl) {
        RecipeCollection.CraftableStatus craftableStatus = bl ? RecipeCollection.CraftableStatus.CRAFTABLE : RecipeCollection.CraftableStatus.ANY;
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.tabInfo.category());
        for (RecipeCollection recipeCollection : list) {
            for (RecipeDisplayEntry recipeDisplayEntry : recipeCollection.getSelectedRecipes(craftableStatus)) {
                if (!clientRecipeBook.willHighlight(recipeDisplayEntry.id())) continue;
                this.animationTime = 15.0f;
                return;
            }
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.animationTime > 0.0f) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.animationTime / 15.0f * (float)Math.PI);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12));
            guiGraphics.pose().scale(1.0f, g);
            guiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)));
        }
        Identifier identifier = this.sprites.get(true, this.selected);
        int k = this.getX();
        if (this.selected) {
            k -= 2;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, k, this.getY(), this.width, this.height);
        this.renderIcon(guiGraphics);
        if (this.animationTime > 0.0f) {
            guiGraphics.pose().popMatrix();
            this.animationTime -= f;
        }
    }

    @Override
    protected void handleCursor(GuiGraphics guiGraphics) {
        if (!this.selected) {
            super.handleCursor(guiGraphics);
        }
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        int i;
        int n = i = this.selected ? -2 : 0;
        if (this.tabInfo.secondaryIcon().isPresent()) {
            guiGraphics.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 3 + i, this.getY() + 5);
            guiGraphics.renderFakeItem(this.tabInfo.secondaryIcon().get(), this.getX() + 14 + i, this.getY() + 5);
        } else {
            guiGraphics.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 9 + i, this.getY() + 5);
        }
    }

    public ExtendedRecipeBookCategory getCategory() {
        return this.tabInfo.category();
    }

    public boolean updateVisibility(ClientRecipeBook clientRecipeBook) {
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.tabInfo.category());
        this.visible = false;
        for (RecipeCollection recipeCollection : list) {
            if (!recipeCollection.hasAnySelected()) continue;
            this.visible = true;
            break;
        }
        return this.visible;
    }

    public void select() {
        this.selected = true;
    }

    public void unselect() {
        this.selected = false;
    }
}

