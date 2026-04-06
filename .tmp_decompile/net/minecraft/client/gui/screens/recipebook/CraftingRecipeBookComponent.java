/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.recipebook;

import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Environment(value=EnvType.CLIENT)
public class CraftingRecipeBookComponent
extends RecipeBookComponent<AbstractCraftingMenu> {
    private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/filter_enabled"), Identifier.withDefaultNamespace("recipe_book/filter_disabled"), Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"), Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
    private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final List<RecipeBookComponent.TabInfo> TABS = List.of((Object)((Object)new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING)), (Object)((Object)new RecipeBookComponent.TabInfo(Items.IRON_AXE, Items.GOLDEN_SWORD, RecipeBookCategories.CRAFTING_EQUIPMENT)), (Object)((Object)new RecipeBookComponent.TabInfo(Items.BRICKS, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS)), (Object)((Object)new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC)), (Object)((Object)new RecipeBookComponent.TabInfo(Items.REDSTONE, RecipeBookCategories.CRAFTING_REDSTONE)));

    public CraftingRecipeBookComponent(AbstractCraftingMenu abstractCraftingMenu) {
        super(abstractCraftingMenu, TABS);
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return ((AbstractCraftingMenu)this.menu).getResultSlot() == slot || ((AbstractCraftingMenu)this.menu).getInputGridSlots().contains(slot);
    }

    private boolean canDisplay(RecipeDisplay recipeDisplay) {
        int i = ((AbstractCraftingMenu)this.menu).getGridWidth();
        int j = ((AbstractCraftingMenu)this.menu).getGridHeight();
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        Objects.requireNonNull(recipeDisplay2);
        RecipeDisplay recipeDisplay3 = recipeDisplay2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, (int)n)) {
            case 0 -> {
                ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                if (i >= shapedCraftingRecipeDisplay.width() && j >= shapedCraftingRecipeDisplay.height()) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                if (i * j >= shapelessCraftingRecipeDisplay.ingredients().size()) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
        ghostSlots.setResult(((AbstractCraftingMenu)this.menu).getResultSlot(), contextMap, recipeDisplay.result());
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        Objects.requireNonNull(recipeDisplay2);
        RecipeDisplay recipeDisplay3 = recipeDisplay2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, (int)n)) {
            case 0: {
                ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                List<Slot> list = ((AbstractCraftingMenu)this.menu).getInputGridSlots();
                PlaceRecipeHelper.placeRecipe(((AbstractCraftingMenu)this.menu).getGridWidth(), ((AbstractCraftingMenu)this.menu).getGridHeight(), shapedCraftingRecipeDisplay.width(), shapedCraftingRecipeDisplay.height(), shapedCraftingRecipeDisplay.ingredients(), (slotDisplay, i, j, k) -> {
                    Slot slot = (Slot)list.get(i);
                    ghostSlots.setInput(slot, contextMap, (SlotDisplay)slotDisplay);
                });
                break;
            }
            case 1: {
                ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                List<Slot> list2 = ((AbstractCraftingMenu)this.menu).getInputGridSlots();
                int i2 = Math.min(shapelessCraftingRecipeDisplay.ingredients().size(), list2.size());
                for (int j2 = 0; j2 < i2; ++j2) {
                    ghostSlots.setInput(list2.get(j2), contextMap, shapelessCraftingRecipeDisplay.ingredients().get(j2));
                }
                break;
            }
        }
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return FILTER_BUTTON_SPRITES;
    }

    @Override
    protected Component getRecipeFilterName() {
        return ONLY_CRAFTABLES_TOOLTIP;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
        recipeCollection.selectRecipes(stackedItemContents, this::canDisplay);
    }
}

