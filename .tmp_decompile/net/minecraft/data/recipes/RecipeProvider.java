/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jspecify.annotations.Nullable;

public abstract class RecipeProvider {
    protected final HolderLookup.Provider registries;
    private final HolderGetter<Item> items;
    protected final RecipeOutput output;
    private static final Map<BlockFamily.Variant, FamilyRecipeProvider> SHAPE_BUILDERS = ImmutableMap.builder().put((Object)BlockFamily.Variant.BUTTON, (recipeProvider, itemLike, itemLike2) -> recipeProvider.buttonBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CHISELED, (recipeProvider, itemLike, itemLike2) -> recipeProvider.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUT, (recipeProvider, itemLike, itemLike2) -> recipeProvider.cutBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.DOOR, (recipeProvider, itemLike, itemLike2) -> recipeProvider.doorBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUSTOM_FENCE, (recipeProvider, itemLike, itemLike2) -> recipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.FENCE, (recipeProvider, itemLike, itemLike2) -> recipeProvider.fenceBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.CUSTOM_FENCE_GATE, (recipeProvider, itemLike, itemLike2) -> recipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.FENCE_GATE, (recipeProvider, itemLike, itemLike2) -> recipeProvider.fenceGateBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.SIGN, (recipeProvider, itemLike, itemLike2) -> recipeProvider.signBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.SLAB, (recipeProvider, itemLike, itemLike2) -> recipeProvider.slabBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.STAIRS, (recipeProvider, itemLike, itemLike2) -> recipeProvider.stairBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.PRESSURE_PLATE, (recipeProvider, itemLike, itemLike2) -> recipeProvider.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.POLISHED, (recipeProvider, itemLike, itemLike2) -> recipeProvider.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.TRAPDOOR, (recipeProvider, itemLike, itemLike2) -> recipeProvider.trapdoorBuilder(itemLike, Ingredient.of(itemLike2))).put((Object)BlockFamily.Variant.WALL, (recipeProvider, itemLike, itemLike2) -> recipeProvider.wallBuilder(RecipeCategory.DECORATIONS, itemLike, Ingredient.of(itemLike2))).build();

    protected RecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
        this.registries = provider;
        this.items = provider.lookupOrThrow(Registries.ITEM);
        this.output = recipeOutput;
    }

    protected abstract void buildRecipes();

    protected void generateForEnabledBlockFamilies(FeatureFlagSet featureFlagSet) {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateRecipe).forEach(blockFamily -> this.generateRecipes((BlockFamily)blockFamily, featureFlagSet));
    }

    protected void oneToOneConversionRecipe(ItemLike itemLike, ItemLike itemLike2, @Nullable String string) {
        this.oneToOneConversionRecipe(itemLike, itemLike2, string, 1);
    }

    protected void oneToOneConversionRecipe(ItemLike itemLike, ItemLike itemLike2, @Nullable String string, int i) {
        this.shapeless(RecipeCategory.MISC, itemLike, i).requires(itemLike2).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output, RecipeProvider.getConversionRecipeName(itemLike, itemLike2));
    }

    protected void oreSmelting(List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string) {
        this.oreCooking(RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, list, recipeCategory, itemLike, f, i, string, "_from_smelting");
    }

    protected void oreBlasting(List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string) {
        this.oreCooking(RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, list, recipeCategory, itemLike, f, i, string, "_from_blasting");
    }

    private <T extends AbstractCookingRecipe> void oreCooking(RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, List<ItemLike> list, RecipeCategory recipeCategory, ItemLike itemLike, float f, int i, String string, String string2) {
        for (ItemLike itemLike2 : list) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike2), recipeCategory, itemLike, f, i, recipeSerializer, factory).group(string).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output, RecipeProvider.getItemName(itemLike) + string2 + "_" + RecipeProvider.getItemName(itemLike2));
        }
    }

    protected void netheriteSmithing(Item item, RecipeCategory recipeCategory, Item item2) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of((ItemLike)Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of((ItemLike)item), this.tag(ItemTags.NETHERITE_TOOL_MATERIALS), recipeCategory, item2).unlocks("has_netherite_ingot", this.has(ItemTags.NETHERITE_TOOL_MATERIALS)).save(this.output, RecipeProvider.getItemName(item2) + "_smithing");
    }

    protected void trimSmithing(Item item, ResourceKey<TrimPattern> resourceKey, ResourceKey<Recipe<?>> resourceKey2) {
        Holder.Reference<TrimPattern> reference = this.registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(resourceKey);
        SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of((ItemLike)item), this.tag(ItemTags.TRIMMABLE_ARMOR), this.tag(ItemTags.TRIM_MATERIALS), reference, RecipeCategory.MISC).unlocks("has_smithing_trim_template", this.has(item)).save(this.output, resourceKey2);
    }

    protected void twoByTwoPacker(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(recipeCategory, itemLike, 1).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void threeByThreePacker(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, String string) {
        this.shapeless(recipeCategory, itemLike).requires(itemLike2, 9).unlockedBy(string, (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void threeByThreePacker(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.threeByThreePacker(recipeCategory, itemLike, itemLike2, RecipeProvider.getHasName(itemLike2));
    }

    protected void planksFromLog(ItemLike itemLike, TagKey<Item> tagKey, int i) {
        this.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i).requires(tagKey).group("planks").unlockedBy("has_log", (Criterion)this.has(tagKey)).save(this.output);
    }

    protected void planksFromLogs(ItemLike itemLike, TagKey<Item> tagKey, int i) {
        this.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, i).requires(tagKey).group("planks").unlockedBy("has_logs", (Criterion)this.has(tagKey)).save(this.output);
    }

    protected void woodFromLogs(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").pattern("##").group("bark").unlockedBy("has_log", (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void woodenBoat(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.TRANSPORTATION, itemLike).define(Character.valueOf('#'), itemLike2).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", (Criterion)RecipeProvider.insideOf(Blocks.WATER)).save(this.output);
    }

    protected void chestBoat(ItemLike itemLike, ItemLike itemLike2) {
        this.shapeless(RecipeCategory.TRANSPORTATION, itemLike).requires(Blocks.CHEST).requires(itemLike2).group("chest_boat").unlockedBy("has_boat", (Criterion)this.has(ItemTags.BOATS)).save(this.output);
    }

    private RecipeBuilder buttonBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shapeless(RecipeCategory.REDSTONE, itemLike).requires(ingredient);
    }

    protected RecipeBuilder doorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(RecipeCategory.REDSTONE, itemLike, 3).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##").pattern("##");
    }

    private RecipeBuilder fenceBuilder(ItemLike itemLike, Ingredient ingredient) {
        int i = itemLike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item item = itemLike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return this.shaped(RecipeCategory.DECORATIONS, itemLike, i).define(Character.valueOf('W'), ingredient).define(Character.valueOf('#'), item).pattern("W#W").pattern("W#W");
    }

    private RecipeBuilder fenceGateBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(RecipeCategory.REDSTONE, itemLike).define(Character.valueOf('#'), Items.STICK).define(Character.valueOf('W'), ingredient).pattern("#W#").pattern("#W#");
    }

    protected void pressurePlate(ItemLike itemLike, ItemLike itemLike2) {
        this.pressurePlateBuilder(RecipeCategory.REDSTONE, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), this.has(itemLike2)).save(this.output);
    }

    private RecipeBuilder pressurePlateBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("##");
    }

    protected void slab(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.slabBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), this.has(itemLike2)).save(this.output);
    }

    protected void shelf(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike, 6).define(Character.valueOf('#'), itemLike2).pattern("###").pattern("   ").pattern("###").group("shelf").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected RecipeBuilder slabBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###");
    }

    protected RecipeBuilder stairBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("#  ").pattern("## ").pattern("###");
    }

    protected RecipeBuilder trapdoorBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(RecipeCategory.REDSTONE, itemLike, 2).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    private RecipeBuilder signBuilder(ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(RecipeCategory.DECORATIONS, itemLike, 3).group("sign").define(Character.valueOf('#'), ingredient).define(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    protected void hangingSign(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike, 6).group("hanging_sign").define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), Items.IRON_CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void colorItemWithDye(List<Item> list, List<Item> list2, String string, RecipeCategory recipeCategory) {
        this.colorWithDye(list, list2, null, string, recipeCategory);
    }

    protected void colorWithDye(List<Item> list, List<Item> list2, @Nullable Item item, String string, RecipeCategory recipeCategory) {
        for (int i = 0; i < list.size(); ++i) {
            Item item22 = list.get(i);
            Item item3 = list2.get(i);
            Stream<Item> stream = list2.stream().filter(item2 -> !item2.equals(item3));
            if (item != null) {
                stream = Stream.concat(stream, Stream.of(item));
            }
            this.shapeless(recipeCategory, item3).requires(item22).requires(Ingredient.of(stream)).group(string).unlockedBy("has_needed_dye", (Criterion)this.has(item22)).save(this.output, "dye_" + RecipeProvider.getItemName(item3));
        }
    }

    protected void carpet(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike, 3).define(Character.valueOf('#'), itemLike2).pattern("##").group("carpet").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void bedFromPlanksAndWool(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void banner(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void stainedGlassFromGlassAndDye(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", (Criterion)this.has(Blocks.GLASS)).save(this.output);
    }

    protected void dryGhast(ItemLike itemLike) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 1).define(Character.valueOf('#'), Items.GHAST_TEAR).define(Character.valueOf('X'), Items.SOUL_SAND).pattern("###").pattern("#X#").pattern("###").group("dry_ghast").unlockedBy(RecipeProvider.getHasName(Items.GHAST_TEAR), (Criterion)this.has(Items.GHAST_TEAR)).save(this.output);
    }

    protected void harness(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.COMBAT, itemLike).define(Character.valueOf('#'), itemLike2).define(Character.valueOf('G'), Items.GLASS).define(Character.valueOf('L'), Items.LEATHER).pattern("LLL").pattern("G#G").group("harness").unlockedBy("has_dried_ghast", (Criterion)this.has(Blocks.DRIED_GHAST)).save(this.output);
    }

    protected void stainedGlassPaneFromStainedGlass(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.DECORATIONS, itemLike, 16).define(Character.valueOf('#'), itemLike2).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void stainedGlassPaneFromGlassPaneAndDye(ItemLike itemLike, ItemLike itemLike2) {
        ((ShapedRecipeBuilder)this.shaped(RecipeCategory.DECORATIONS, itemLike, 8).define(Character.valueOf('#'), Blocks.GLASS_PANE).define(Character.valueOf('$'), itemLike2).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", (Criterion)this.has(Blocks.GLASS_PANE))).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output, RecipeProvider.getConversionRecipeName(itemLike, Blocks.GLASS_PANE));
    }

    protected void coloredTerracottaFromTerracottaAndDye(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).define(Character.valueOf('#'), Blocks.TERRACOTTA).define(Character.valueOf('X'), itemLike2).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", (Criterion)this.has(Blocks.TERRACOTTA)).save(this.output);
    }

    protected void concretePowder(ItemLike itemLike, ItemLike itemLike2) {
        ((ShapelessRecipeBuilder)this.shapeless(RecipeCategory.BUILDING_BLOCKS, itemLike, 8).requires(itemLike2).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", (Criterion)this.has(Blocks.SAND))).unlockedBy("has_gravel", (Criterion)this.has(Blocks.GRAVEL)).save(this.output);
    }

    protected void candle(ItemLike itemLike, ItemLike itemLike2) {
        this.shapeless(RecipeCategory.DECORATIONS, itemLike).requires(Blocks.CANDLE).requires(itemLike2).group("dyed_candle").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void wall(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.wallBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), this.has(itemLike2)).save(this.output);
    }

    private RecipeBuilder wallBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike, 6).define(Character.valueOf('#'), ingredient).pattern("###").pattern("###");
    }

    protected void polished(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.polishedBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), this.has(itemLike2)).save(this.output);
    }

    private RecipeBuilder polishedBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('S'), ingredient).pattern("SS").pattern("SS");
    }

    protected void cut(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.cutBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    private ShapedRecipeBuilder cutBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike, 4).define(Character.valueOf('#'), ingredient).pattern("##").pattern("##");
    }

    protected void chiseled(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.chiseledBuilder(recipeCategory, itemLike, Ingredient.of(itemLike2)).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void mosaicBuilder(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), itemLike2).pattern("#").pattern("#").unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipeCategory, ItemLike itemLike, Ingredient ingredient) {
        return this.shaped(recipeCategory, itemLike).define(Character.valueOf('#'), ingredient).pattern("#").pattern("#");
    }

    protected void stonecutterResultFromBase(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2) {
        this.stonecutterResultFromBase(recipeCategory, itemLike, itemLike2, 1);
    }

    protected void stonecutterResultFromBase(RecipeCategory recipeCategory, ItemLike itemLike, ItemLike itemLike2, int i) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemLike2), recipeCategory, itemLike, i).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output, RecipeProvider.getConversionRecipeName(itemLike, itemLike2) + "_stonecutting");
    }

    private void smeltingResultFromBase(ItemLike itemLike, ItemLike itemLike2) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemLike2), RecipeCategory.BUILDING_BLOCKS, itemLike, 0.1f, 200).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2)).save(this.output);
    }

    protected void nineBlockStorageRecipes(RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2) {
        this.nineBlockStorageRecipes(recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected void nineBlockStorageRecipesWithCustomPacking(RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        this.nineBlockStorageRecipes(recipeCategory, itemLike, recipeCategory2, itemLike2, string, string2, RecipeProvider.getSimpleRecipeName(itemLike), null);
    }

    protected void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, String string2) {
        this.nineBlockStorageRecipes(recipeCategory, itemLike, recipeCategory2, itemLike2, RecipeProvider.getSimpleRecipeName(itemLike2), null, string, string2);
    }

    private void nineBlockStorageRecipes(RecipeCategory recipeCategory, ItemLike itemLike, RecipeCategory recipeCategory2, ItemLike itemLike2, String string, @Nullable String string2, String string3, @Nullable String string4) {
        ((ShapelessRecipeBuilder)this.shapeless(recipeCategory, itemLike, 9).requires(itemLike2).group(string4).unlockedBy(RecipeProvider.getHasName(itemLike2), (Criterion)this.has(itemLike2))).save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse(string3)));
        ((ShapedRecipeBuilder)this.shaped(recipeCategory2, itemLike2).define(Character.valueOf('#'), itemLike).pattern("###").pattern("###").pattern("###").group(string2).unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)this.has(itemLike))).save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse(string)));
    }

    protected void copySmithingTemplate(ItemLike itemLike, ItemLike itemLike2) {
        this.shaped(RecipeCategory.MISC, itemLike, 2).define(Character.valueOf('#'), Items.DIAMOND).define(Character.valueOf('C'), itemLike2).define(Character.valueOf('S'), itemLike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)this.has(itemLike)).save(this.output);
    }

    protected void copySmithingTemplate(ItemLike itemLike, Ingredient ingredient) {
        this.shaped(RecipeCategory.MISC, itemLike, 2).define(Character.valueOf('#'), Items.DIAMOND).define(Character.valueOf('C'), ingredient).define(Character.valueOf('S'), itemLike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)this.has(itemLike)).save(this.output);
    }

    protected <T extends AbstractCookingRecipe> void cookRecipes(String string, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, int i) {
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.COD, Items.COOKED_COD, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.KELP, Items.DRIED_KELP, 0.1f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        this.simpleCookingRecipe(string, recipeSerializer, factory, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    private <T extends AbstractCookingRecipe> void simpleCookingRecipe(String string, RecipeSerializer<T> recipeSerializer, AbstractCookingRecipe.Factory<T> factory, int i, ItemLike itemLike, ItemLike itemLike2, float f) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), RecipeCategory.FOOD, itemLike2, f, i, recipeSerializer, factory).unlockedBy(RecipeProvider.getHasName(itemLike), (Criterion)this.has(itemLike)).save(this.output, RecipeProvider.getItemName(itemLike2) + "_from_" + string);
    }

    protected void waxRecipes(FeatureFlagSet featureFlagSet) {
        HoneycombItem.WAXABLES.get().forEach((block, block2) -> {
            if (!block2.requiredFeatures().isSubsetOf(featureFlagSet)) {
                return;
            }
            Pair pair = (Pair)HoneycombItem.WAXED_RECIPES.getOrDefault(block2, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)RecipeProvider.getItemName(block2)));
            RecipeCategory recipeCategory = (RecipeCategory)((Object)((Object)pair.getFirst()));
            String string = (String)pair.getSecond();
            this.shapeless(recipeCategory, (ItemLike)block2).requires((ItemLike)block).requires(Items.HONEYCOMB).group(string).unlockedBy(RecipeProvider.getHasName(block), (Criterion)this.has((ItemLike)block)).save(this.output, RecipeProvider.getConversionRecipeName(block2, Items.HONEYCOMB));
        });
    }

    protected void grate(Block block, Block block2) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, block, 4).define(Character.valueOf('M'), block2).pattern(" M ").pattern("M M").pattern(" M ").group(RecipeProvider.getItemName(block)).unlockedBy(RecipeProvider.getHasName(block2), (Criterion)this.has(block2)).save(this.output);
    }

    protected void copperBulb(Block block, Block block2) {
        ((ShapedRecipeBuilder)this.shaped(RecipeCategory.REDSTONE, block, 4).define(Character.valueOf('C'), block2).define(Character.valueOf('R'), Items.REDSTONE).define(Character.valueOf('B'), Items.BLAZE_ROD).pattern(" C ").pattern("CBC").pattern(" R ").unlockedBy(RecipeProvider.getHasName(block2), (Criterion)this.has(block2))).group(RecipeProvider.getItemName(block)).save(this.output);
    }

    protected void waxedChiseled(Block block, Block block2) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, block).define(Character.valueOf('M'), block2).pattern(" M ").pattern(" M ").group(RecipeProvider.getItemName(block)).unlockedBy(RecipeProvider.getHasName(block2), (Criterion)this.has(block2)).save(this.output);
    }

    protected void suspiciousStew(Item item, SuspiciousEffectHolder suspiciousEffectHolder) {
        ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW.builtInRegistryHolder(), 1, DataComponentPatch.builder().set(DataComponents.SUSPICIOUS_STEW_EFFECTS, suspiciousEffectHolder.getSuspiciousEffects()).build());
        this.shapeless(RecipeCategory.FOOD, itemStack).requires(Items.BOWL).requires(Items.BROWN_MUSHROOM).requires(Items.RED_MUSHROOM).requires(item).group("suspicious_stew").unlockedBy(RecipeProvider.getHasName(item), (Criterion)this.has(item)).save(this.output, RecipeProvider.getItemName(itemStack.getItem()) + "_from_" + RecipeProvider.getItemName(item));
    }

    protected void generateRecipes(BlockFamily blockFamily, FeatureFlagSet featureFlagSet) {
        blockFamily.getVariants().forEach((variant, block) -> {
            if (!block.requiredFeatures().isSubsetOf(featureFlagSet)) {
                return;
            }
            FamilyRecipeProvider familyRecipeProvider = SHAPE_BUILDERS.get(variant);
            Block itemLike = this.getBaseBlock(blockFamily, (BlockFamily.Variant)((Object)variant));
            if (familyRecipeProvider != null) {
                RecipeBuilder recipeBuilder = familyRecipeProvider.create(this, (ItemLike)block, itemLike);
                blockFamily.getRecipeGroupPrefix().ifPresent(string -> recipeBuilder.group(string + (String)(variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getRecipeGroup())));
                recipeBuilder.unlockedBy(blockFamily.getRecipeUnlockedBy().orElseGet(() -> RecipeProvider.getHasName(itemLike)), this.has(itemLike));
                recipeBuilder.save(this.output);
            }
            if (variant == BlockFamily.Variant.CRACKED) {
                this.smeltingResultFromBase((ItemLike)block, itemLike);
            }
        });
    }

    private Block getBaseBlock(BlockFamily blockFamily, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!blockFamily.getVariants().containsKey((Object)BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            }
            return blockFamily.get(BlockFamily.Variant.SLAB);
        }
        return blockFamily.getBaseBlock();
    }

    private static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return CriteriaTriggers.ENTER_BLOCK.createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
    }

    private Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints ints, ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(this.items, itemLike).withCount(ints));
    }

    protected Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(this.items, itemLike));
    }

    protected Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tagKey) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(this.items, tagKey));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder ... builders) {
        return RecipeProvider.inventoryTrigger((ItemPredicate[])Arrays.stream(builders).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate ... itemPredicates) {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of((Object[])itemPredicates)));
    }

    protected static String getHasName(ItemLike itemLike) {
        return "has_" + RecipeProvider.getItemName(itemLike);
    }

    protected static String getItemName(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
    }

    protected static String getSimpleRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike);
    }

    protected static String getConversionRecipeName(ItemLike itemLike, ItemLike itemLike2) {
        return RecipeProvider.getItemName(itemLike) + "_from_" + RecipeProvider.getItemName(itemLike2);
    }

    protected static String getSmeltingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_smelting";
    }

    protected static String getBlastingRecipeName(ItemLike itemLike) {
        return RecipeProvider.getItemName(itemLike) + "_from_blasting";
    }

    protected Ingredient tag(TagKey<Item> tagKey) {
        return Ingredient.of(this.items.getOrThrow(tagKey));
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike) {
        return ShapedRecipeBuilder.shaped(this.items, recipeCategory, itemLike);
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
        return ShapedRecipeBuilder.shaped(this.items, recipeCategory, itemLike, i);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemStack itemStack) {
        return ShapelessRecipeBuilder.shapeless(this.items, recipeCategory, itemStack);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike) {
        return ShapelessRecipeBuilder.shapeless(this.items, recipeCategory, itemLike);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
        return ShapelessRecipeBuilder.shapeless(this.items, recipeCategory, itemLike, i);
    }

    @FunctionalInterface
    static interface FamilyRecipeProvider {
        public RecipeBuilder create(RecipeProvider var1, ItemLike var2, ItemLike var3);
    }

    protected static abstract class Runner
    implements DataProvider {
        private final PackOutput packOutput;
        private final CompletableFuture<HolderLookup.Provider> registries;

        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
            this.packOutput = packOutput;
            this.registries = completableFuture;
        }

        @Override
        public final CompletableFuture<?> run(final CachedOutput cachedOutput) {
            return this.registries.thenCompose(provider -> {
                PackOutput.PathProvider pathProvider = this.packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
                PackOutput.PathProvider pathProvider2 = this.packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
                final HashSet set = Sets.newHashSet();
                final ArrayList list = new ArrayList();
                RecipeOutput recipeOutput = new RecipeOutput(){
                    final /* synthetic */ HolderLookup.Provider val$registries;
                    final /* synthetic */ PackOutput.PathProvider val$recipePathProvider;
                    final /* synthetic */ PackOutput.PathProvider val$advancementPathProvider;
                    {
                        this.val$registries = provider;
                        this.val$recipePathProvider = pathProvider;
                        this.val$advancementPathProvider = pathProvider2;
                    }

                    @Override
                    public void accept(ResourceKey<Recipe<?>> resourceKey, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                        if (!set.add(resourceKey)) {
                            throw new IllegalStateException("Duplicate recipe " + String.valueOf(resourceKey.identifier()));
                        }
                        this.saveRecipe(resourceKey, recipe);
                        if (advancementHolder != null) {
                            this.saveAdvancement(advancementHolder);
                        }
                    }

                    @Override
                    public Advancement.Builder advancement() {
                        return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                    }

                    @Override
                    public void includeRootAdvancement() {
                        AdvancementHolder advancementHolder = Advancement.Builder.recipeAdvancement().addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())).build(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                        this.saveAdvancement(advancementHolder);
                    }

                    private void saveRecipe(ResourceKey<Recipe<?>> resourceKey, Recipe<?> recipe) {
                        list.add(DataProvider.saveStable(cachedOutput, this.val$registries, Recipe.CODEC, recipe, this.val$recipePathProvider.json(resourceKey.identifier())));
                    }

                    private void saveAdvancement(AdvancementHolder advancementHolder) {
                        list.add(DataProvider.saveStable(cachedOutput, this.val$registries, Advancement.CODEC, advancementHolder.value(), this.val$advancementPathProvider.json(advancementHolder.id())));
                    }
                };
                this.createRecipeProvider((HolderLookup.Provider)provider, recipeOutput).buildRecipes();
                return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
            });
        }

        protected abstract RecipeProvider createRecipeProvider(HolderLookup.Provider var1, RecipeOutput var2);
    }
}

