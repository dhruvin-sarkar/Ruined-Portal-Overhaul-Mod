/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

public final class Ingredient
implements StackedContents.IngredientInfo<Holder<Item>>,
Predicate<ItemStack> {
    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM).map(Ingredient::new, ingredient -> ingredient.values);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> OPTIONAL_CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM).map(holderSet -> holderSet.size() == 0 ? Optional.empty() : Optional.of(new Ingredient((HolderSet<Item>)holderSet)), optional -> optional.map(ingredient -> ingredient.values).orElse(HolderSet.direct(new Holder[0])));
    public static final Codec<HolderSet<Item>> NON_AIR_HOLDER_SET_CODEC = HolderSetCodec.create(Registries.ITEM, Item.CODEC, false);
    public static final Codec<Ingredient> CODEC = ExtraCodecs.nonEmptyHolderSet(NON_AIR_HOLDER_SET_CODEC).xmap(Ingredient::new, ingredient -> ingredient.values);
    private final HolderSet<Item> values;

    private Ingredient(HolderSet<Item> holderSet) {
        holderSet.unwrap().ifRight(list -> {
            if (list.isEmpty()) {
                throw new UnsupportedOperationException("Ingredients can't be empty");
            }
            if (list.contains(Items.AIR.builtInRegistryHolder())) {
                throw new UnsupportedOperationException("Ingredient can't contain air");
            }
        });
        this.values = holderSet;
    }

    public static boolean testOptionalIngredient(Optional<Ingredient> optional, ItemStack itemStack) {
        return optional.map(ingredient -> ingredient.test(itemStack)).orElseGet(itemStack::isEmpty);
    }

    @Deprecated
    public Stream<Holder<Item>> items() {
        return this.values.stream();
    }

    public boolean isEmpty() {
        return this.values.size() == 0;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(this.values);
    }

    @Override
    public boolean acceptsItem(Holder<Item> holder) {
        return this.values.contains(holder);
    }

    public boolean equals(Object object) {
        if (object instanceof Ingredient) {
            Ingredient ingredient = (Ingredient)object;
            return Objects.equals(this.values, ingredient.values);
        }
        return false;
    }

    public static Ingredient of(ItemLike itemLike) {
        return new Ingredient(HolderSet.direct(itemLike.asItem().builtInRegistryHolder()));
    }

    public static Ingredient of(ItemLike ... itemLikes) {
        return Ingredient.of(Arrays.stream(itemLikes));
    }

    public static Ingredient of(Stream<? extends ItemLike> stream) {
        return new Ingredient(HolderSet.direct(stream.map(itemLike -> itemLike.asItem().builtInRegistryHolder()).toList()));
    }

    public static Ingredient of(HolderSet<Item> holderSet) {
        return new Ingredient(holderSet);
    }

    public SlotDisplay display() {
        return (SlotDisplay)this.values.unwrap().map(SlotDisplay.TagSlotDisplay::new, list -> new SlotDisplay.Composite(list.stream().map(Ingredient::displayForSingleItem).toList()));
    }

    public static SlotDisplay optionalIngredientToDisplay(Optional<Ingredient> optional) {
        return optional.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE);
    }

    private static SlotDisplay displayForSingleItem(Holder<Item> holder) {
        SlotDisplay.ItemSlotDisplay slotDisplay = new SlotDisplay.ItemSlotDisplay(holder);
        ItemStack itemStack = holder.value().getCraftingRemainder();
        if (!itemStack.isEmpty()) {
            SlotDisplay.ItemStackSlotDisplay slotDisplay2 = new SlotDisplay.ItemStackSlotDisplay(itemStack);
            return new SlotDisplay.WithRemainder(slotDisplay, slotDisplay2);
        }
        return slotDisplay;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((ItemStack)object);
    }

    @Override
    public /* synthetic */ boolean acceptsItem(Object object) {
        return this.acceptsItem((Holder)object);
    }
}

