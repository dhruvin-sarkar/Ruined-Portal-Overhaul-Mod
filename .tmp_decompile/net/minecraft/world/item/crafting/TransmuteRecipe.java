/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.TransmuteResult;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class TransmuteRecipe
implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final Ingredient input;
    final Ingredient material;
    final TransmuteResult result;
    private @Nullable PlacementInfo placementInfo;

    public TransmuteRecipe(String string, CraftingBookCategory craftingBookCategory, Ingredient ingredient, Ingredient ingredient2, TransmuteResult transmuteResult) {
        this.group = string;
        this.category = craftingBookCategory;
        this.input = ingredient;
        this.material = ingredient2;
        this.result = transmuteResult;
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (!bl && this.input.test(itemStack)) {
                if (this.result.isResultUnchanged(itemStack)) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!bl2 && this.material.test(itemStack)) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty() || !this.input.test(itemStack)) continue;
            return this.result.apply(itemStack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of((Object)new ShapelessCraftingRecipeDisplay(List.of((Object)this.input.display(), (Object)this.material.display()), this.result.display(), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public RecipeSerializer<TransmuteRecipe> getSerializer() {
        return RecipeSerializer.TRANSMUTE;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(List.of((Object)this.input, (Object)this.material));
        }
        return this.placementInfo;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    public static class Serializer
    implements RecipeSerializer<TransmuteRecipe> {
        private static final MapCodec<TransmuteRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(transmuteRecipe -> transmuteRecipe.group), (App)CraftingBookCategory.CODEC.fieldOf("category").orElse((Object)CraftingBookCategory.MISC).forGetter(transmuteRecipe -> transmuteRecipe.category), (App)Ingredient.CODEC.fieldOf("input").forGetter(transmuteRecipe -> transmuteRecipe.input), (App)Ingredient.CODEC.fieldOf("material").forGetter(transmuteRecipe -> transmuteRecipe.material), (App)TransmuteResult.CODEC.fieldOf("result").forGetter(transmuteRecipe -> transmuteRecipe.result)).apply((Applicative)instance, TransmuteRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, transmuteRecipe -> transmuteRecipe.group, CraftingBookCategory.STREAM_CODEC, transmuteRecipe -> transmuteRecipe.category, Ingredient.CONTENTS_STREAM_CODEC, transmuteRecipe -> transmuteRecipe.input, Ingredient.CONTENTS_STREAM_CODEC, transmuteRecipe -> transmuteRecipe.material, TransmuteResult.STREAM_CODEC, transmuteRecipe -> transmuteRecipe.result, TransmuteRecipe::new);

        @Override
        public MapCodec<TransmuteRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

