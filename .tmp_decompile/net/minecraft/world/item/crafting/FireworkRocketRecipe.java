/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe
extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of((ItemLike)Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of((ItemLike)Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of((ItemLike)Items.FIREWORK_STAR);

    public FireworkRocketRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() < 2) {
            return false;
        }
        boolean bl = false;
        int i = 0;
        for (int j = 0; j < craftingInput.size(); ++j) {
            ItemStack itemStack = craftingInput.getItem(j);
            if (itemStack.isEmpty()) continue;
            if (PAPER_INGREDIENT.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!(GUNPOWDER_INGREDIENT.test(itemStack) ? ++i > 3 : !STAR_INGREDIENT.test(itemStack))) continue;
            return false;
        }
        return bl && i >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ArrayList<FireworkExplosion> list = new ArrayList<FireworkExplosion>();
        int i = 0;
        for (int j = 0; j < craftingInput.size(); ++j) {
            FireworkExplosion fireworkExplosion;
            ItemStack itemStack = craftingInput.getItem(j);
            if (itemStack.isEmpty()) continue;
            if (GUNPOWDER_INGREDIENT.test(itemStack)) {
                ++i;
                continue;
            }
            if (!STAR_INGREDIENT.test(itemStack) || (fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION)) == null) continue;
            list.add(fireworkExplosion);
        }
        ItemStack itemStack2 = new ItemStack(Items.FIREWORK_ROCKET, 3);
        itemStack2.set(DataComponents.FIREWORKS, new Fireworks(i, list));
        return itemStack2;
    }

    @Override
    public RecipeSerializer<FireworkRocketRecipe> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}

