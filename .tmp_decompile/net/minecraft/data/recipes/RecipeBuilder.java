/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public interface RecipeBuilder {
    public static final Identifier ROOT_RECIPE_ADVANCEMENT = Identifier.withDefaultNamespace("recipes/root");

    public RecipeBuilder unlockedBy(String var1, Criterion<?> var2);

    public RecipeBuilder group(@Nullable String var1);

    public Item getResult();

    public void save(RecipeOutput var1, ResourceKey<Recipe<?>> var2);

    default public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, ResourceKey.create(Registries.RECIPE, RecipeBuilder.getDefaultRecipeId(this.getResult())));
    }

    default public void save(RecipeOutput recipeOutput, String string) {
        Identifier identifier = RecipeBuilder.getDefaultRecipeId(this.getResult());
        Identifier identifier2 = Identifier.parse(string);
        if (identifier2.equals(identifier)) {
            throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
        }
        this.save(recipeOutput, ResourceKey.create(Registries.RECIPE, identifier2));
    }

    public static Identifier getDefaultRecipeId(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
    }

    public static CraftingBookCategory determineBookCategory(RecipeCategory recipeCategory) {
        return switch (recipeCategory) {
            case RecipeCategory.BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case RecipeCategory.TOOLS, RecipeCategory.COMBAT -> CraftingBookCategory.EQUIPMENT;
            case RecipeCategory.REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC;
        };
    }
}

