/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MapCloningRecipe
extends CustomRecipe {
    public MapCloningRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() < 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.has(DataComponents.MAP_ID)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            if (itemStack.is(Items.MAP)) {
                bl = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingInput.size(); ++j) {
            ItemStack itemStack2 = craftingInput.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.has(DataComponents.MAP_ID)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.MAP)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        return itemStack.copyWithCount(i + 1);
    }

    @Override
    public RecipeSerializer<MapCloningRecipe> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}

