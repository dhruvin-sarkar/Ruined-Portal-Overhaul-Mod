/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class SimpleCookingRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();
    private @Nullable String group;
    private final AbstractCookingRecipe.Factory<?> factory;

    private SimpleCookingRecipeBuilder(RecipeCategory recipeCategory, CookingBookCategory cookingBookCategory, ItemLike itemLike, Ingredient ingredient, float f, int i, AbstractCookingRecipe.Factory<?> factory) {
        this.category = recipeCategory;
        this.bookCategory = cookingBookCategory;
        this.result = itemLike.asItem();
        this.ingredient = ingredient;
        this.experience = f;
        this.cookingTime = i;
        this.factory = factory;
    }

    public static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory) {
        return new SimpleCookingRecipeBuilder(recipeCategory, SimpleCookingRecipeBuilder.determineRecipeCategory(recipeSerializer, itemLike), itemLike, ingredient, f, i, factory);
    }

    public static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
        return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, CampfireCookingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
        return new SimpleCookingRecipeBuilder(recipeCategory, SimpleCookingRecipeBuilder.determineBlastingRecipeCategory(itemLike), itemLike, ingredient, f, i, BlastingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
        return new SimpleCookingRecipeBuilder(recipeCategory, SimpleCookingRecipeBuilder.determineSmeltingRecipeCategory(itemLike), itemLike, ingredient, f, i, SmeltingRecipe::new);
    }

    public static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i) {
        return new SimpleCookingRecipeBuilder(recipeCategory, CookingBookCategory.FOOD, itemLike, ingredient, f, i, SmokingRecipe::new);
    }

    @Override
    public SimpleCookingRecipeBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }

    @Override
    public SimpleCookingRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey) {
        this.ensureValid(resourceKey);
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey)).rewards(AdvancementRewards.Builder.recipe(resourceKey)).requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        Object abstractCookingRecipe = this.factory.create((String)Objects.requireNonNullElse((Object)this.group, (Object)""), this.bookCategory, this.ingredient, new ItemStack(this.result), this.experience, this.cookingTime);
        recipeOutput.accept(resourceKey, (Recipe<?>)abstractCookingRecipe, builder.build(resourceKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike itemLike) {
        if (itemLike.asItem().components().has(DataComponents.FOOD)) {
            return CookingBookCategory.FOOD;
        }
        if (itemLike.asItem() instanceof BlockItem) {
            return CookingBookCategory.BLOCKS;
        }
        return CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineBlastingRecipeCategory(ItemLike itemLike) {
        if (itemLike.asItem() instanceof BlockItem) {
            return CookingBookCategory.BLOCKS;
        }
        return CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer, ItemLike itemLike) {
        if (recipeSerializer == RecipeSerializer.SMELTING_RECIPE) {
            return SimpleCookingRecipeBuilder.determineSmeltingRecipeCategory(itemLike);
        }
        if (recipeSerializer == RecipeSerializer.BLASTING_RECIPE) {
            return SimpleCookingRecipeBuilder.determineBlastingRecipeCategory(itemLike);
        }
        if (recipeSerializer == RecipeSerializer.SMOKING_RECIPE || recipeSerializer == RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
            return CookingBookCategory.FOOD;
        }
        throw new IllegalStateException("Unknown cooking recipe type");
    }

    private void ensureValid(ResourceKey<Recipe<?>> resourceKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(resourceKey.identifier()));
        }
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    public /* synthetic */ RecipeBuilder unlockedBy(String string, Criterion criterion) {
        return this.unlockedBy(string, criterion);
    }
}

