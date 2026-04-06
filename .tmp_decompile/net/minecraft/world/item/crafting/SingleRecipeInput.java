/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record SingleRecipeInput(ItemStack item) implements RecipeInput
{
    @Override
    public ItemStack getItem(int i) {
        if (i != 0) {
            throw new IllegalArgumentException("No item for index " + i);
        }
        return this.item;
    }

    @Override
    public int size() {
        return 1;
    }
}

