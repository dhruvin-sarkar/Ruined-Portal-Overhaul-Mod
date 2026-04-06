/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public interface CraftingRecipe
extends Recipe<CraftingInput> {
    @Override
    default public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer();

    public CraftingBookCategory category();

    default public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        return CraftingRecipe.defaultCraftingReminder(craftingInput);
    }

    public static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            Item item = craftingInput.getItem(i).getItem();
            nonNullList.set(i, item.getCraftingRemainder());
        }
        return nonNullList;
    }

    @Override
    default public RecipeBookCategory recipeBookCategory() {
        return switch (this.category()) {
            default -> throw new MatchException(null, null);
            case CraftingBookCategory.BUILDING -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
            case CraftingBookCategory.EQUIPMENT -> RecipeBookCategories.CRAFTING_EQUIPMENT;
            case CraftingBookCategory.REDSTONE -> RecipeBookCategories.CRAFTING_REDSTONE;
            case CraftingBookCategory.MISC -> RecipeBookCategories.CRAFTING_MISC;
        };
    }
}

