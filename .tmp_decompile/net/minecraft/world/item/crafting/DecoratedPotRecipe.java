/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe
extends CustomRecipe {
    public DecoratedPotRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    private static ItemStack back(CraftingInput craftingInput) {
        return craftingInput.getItem(1, 0);
    }

    private static ItemStack left(CraftingInput craftingInput) {
        return craftingInput.getItem(0, 1);
    }

    private static ItemStack right(CraftingInput craftingInput) {
        return craftingInput.getItem(2, 1);
    }

    private static ItemStack front(CraftingInput craftingInput) {
        return craftingInput.getItem(1, 2);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.width() != 3 || craftingInput.height() != 3 || craftingInput.ingredientCount() != 4) {
            return false;
        }
        return DecoratedPotRecipe.back(craftingInput).is(ItemTags.DECORATED_POT_INGREDIENTS) && DecoratedPotRecipe.left(craftingInput).is(ItemTags.DECORATED_POT_INGREDIENTS) && DecoratedPotRecipe.right(craftingInput).is(ItemTags.DECORATED_POT_INGREDIENTS) && DecoratedPotRecipe.front(craftingInput).is(ItemTags.DECORATED_POT_INGREDIENTS);
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        PotDecorations potDecorations = new PotDecorations(DecoratedPotRecipe.back(craftingInput).getItem(), DecoratedPotRecipe.left(craftingInput).getItem(), DecoratedPotRecipe.right(craftingInput).getItem(), DecoratedPotRecipe.front(craftingInput).getItem());
        return DecoratedPotBlockEntity.createDecoratedPotItem(potDecorations);
    }

    @Override
    public RecipeSerializer<DecoratedPotRecipe> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}

