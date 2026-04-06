/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class FireworkStarRecipe
extends CustomRecipe {
    private static final Map<Item, FireworkExplosion.Shape> SHAPE_BY_ITEM = Map.of((Object)Items.FIRE_CHARGE, (Object)FireworkExplosion.Shape.LARGE_BALL, (Object)Items.FEATHER, (Object)FireworkExplosion.Shape.BURST, (Object)Items.GOLD_NUGGET, (Object)FireworkExplosion.Shape.STAR, (Object)Items.SKELETON_SKULL, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.WITHER_SKELETON_SKULL, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.CREEPER_HEAD, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.PLAYER_HEAD, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.DRAGON_HEAD, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.ZOMBIE_HEAD, (Object)FireworkExplosion.Shape.CREEPER, (Object)Items.PIGLIN_HEAD, (Object)FireworkExplosion.Shape.CREEPER);
    private static final Ingredient TRAIL_INGREDIENT = Ingredient.of((ItemLike)Items.DIAMOND);
    private static final Ingredient TWINKLE_INGREDIENT = Ingredient.of((ItemLike)Items.GLOWSTONE_DUST);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of((ItemLike)Items.GUNPOWDER);

    public FireworkStarRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() < 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (SHAPE_BY_ITEM.containsKey(itemStack.getItem())) {
                if (bl3) {
                    return false;
                }
                bl3 = true;
                continue;
            }
            if (TWINKLE_INGREDIENT.test(itemStack)) {
                if (bl5) {
                    return false;
                }
                bl5 = true;
                continue;
            }
            if (TRAIL_INGREDIENT.test(itemStack)) {
                if (bl4) {
                    return false;
                }
                bl4 = true;
                continue;
            }
            if (GUNPOWDER_INGREDIENT.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (itemStack.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        FireworkExplosion.Shape shape = FireworkExplosion.Shape.SMALL_BALL;
        boolean bl = false;
        boolean bl2 = false;
        IntArrayList intList = new IntArrayList();
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            FireworkExplosion.Shape shape2 = SHAPE_BY_ITEM.get(itemStack.getItem());
            if (shape2 != null) {
                shape = shape2;
                continue;
            }
            if (TWINKLE_INGREDIENT.test(itemStack)) {
                bl = true;
                continue;
            }
            if (TRAIL_INGREDIENT.test(itemStack)) {
                bl2 = true;
                continue;
            }
            Item item = itemStack.getItem();
            if (!(item instanceof DyeItem)) continue;
            DyeItem dyeItem = (DyeItem)item;
            intList.add(dyeItem.getDyeColor().getFireworkColor());
        }
        ItemStack itemStack2 = new ItemStack(Items.FIREWORK_STAR);
        itemStack2.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(shape, (IntList)intList, IntList.of(), bl2, bl));
        return itemStack2;
    }

    @Override
    public RecipeSerializer<FireworkStarRecipe> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}

