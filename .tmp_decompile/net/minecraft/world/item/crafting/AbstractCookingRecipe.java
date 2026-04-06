/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public abstract class AbstractCookingRecipe
extends SingleItemRecipe {
    private final CookingBookCategory category;
    private final float experience;
    private final int cookingTime;

    public AbstractCookingRecipe(String string, CookingBookCategory cookingBookCategory, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(string, ingredient, itemStack);
        this.category = cookingBookCategory;
        this.experience = f;
        this.cookingTime = i;
    }

    @Override
    public abstract RecipeSerializer<? extends AbstractCookingRecipe> getSerializer();

    @Override
    public abstract RecipeType<? extends AbstractCookingRecipe> getType();

    public float experience() {
        return this.experience;
    }

    public int cookingTime() {
        return this.cookingTime;
    }

    public CookingBookCategory category() {
        return this.category;
    }

    protected abstract Item furnaceIcon();

    @Override
    public List<RecipeDisplay> display() {
        return List.of((Object)new FurnaceRecipeDisplay(this.input().display(), SlotDisplay.AnyFuel.INSTANCE, new SlotDisplay.ItemStackSlotDisplay(this.result()), new SlotDisplay.ItemSlotDisplay(this.furnaceIcon()), this.cookingTime, this.experience));
    }

    @FunctionalInterface
    public static interface Factory<T extends AbstractCookingRecipe> {
        public T create(String var1, CookingBookCategory var2, Ingredient var3, ItemStack var4, float var5, int var6);
    }

    public static class Serializer<T extends AbstractCookingRecipe>
    implements RecipeSerializer<T> {
        private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(SingleItemRecipe::group), (App)CookingBookCategory.CODEC.fieldOf("category").orElse((Object)CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category), (App)Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input), (App)ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(SingleItemRecipe::result), (App)Codec.FLOAT.fieldOf("experience").orElse((Object)Float.valueOf(0.0f)).forGetter(AbstractCookingRecipe::experience), (App)Codec.INT.fieldOf("cookingtime").orElse((Object)i).forGetter(AbstractCookingRecipe::cookingTime)).apply((Applicative)instance, factory::create));
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SingleItemRecipe::group, CookingBookCategory.STREAM_CODEC, AbstractCookingRecipe::category, Ingredient.CONTENTS_STREAM_CODEC, SingleItemRecipe::input, ItemStack.STREAM_CODEC, SingleItemRecipe::result, ByteBufCodecs.FLOAT, AbstractCookingRecipe::experience, ByteBufCodecs.INT, AbstractCookingRecipe::cookingTime, factory::create);

        public Serializer(Factory<T> factory, int i) {
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }
}

