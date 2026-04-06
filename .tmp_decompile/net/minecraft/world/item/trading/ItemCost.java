/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.trading;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemCost(Holder<Item> item, int count, DataComponentExactPredicate components, ItemStack itemStack) {
    public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Item.CODEC.fieldOf("id").forGetter(ItemCost::item), (App)ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse((Object)1).forGetter(ItemCost::count), (App)DataComponentExactPredicate.CODEC.optionalFieldOf("components", (Object)DataComponentExactPredicate.EMPTY).forGetter(ItemCost::components)).apply((Applicative)instance, ItemCost::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(Item.STREAM_CODEC, ItemCost::item, ByteBufCodecs.VAR_INT, ItemCost::count, DataComponentExactPredicate.STREAM_CODEC, ItemCost::components, ItemCost::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);

    public ItemCost(ItemLike itemLike) {
        this(itemLike, 1);
    }

    public ItemCost(ItemLike itemLike, int i) {
        this(itemLike.asItem().builtInRegistryHolder(), i, DataComponentExactPredicate.EMPTY);
    }

    public ItemCost(Holder<Item> holder, int i, DataComponentExactPredicate dataComponentExactPredicate) {
        this(holder, i, dataComponentExactPredicate, ItemCost.createStack(holder, i, dataComponentExactPredicate));
    }

    public ItemCost withComponents(UnaryOperator<DataComponentExactPredicate.Builder> unaryOperator) {
        return new ItemCost(this.item, this.count, ((DataComponentExactPredicate.Builder)unaryOperator.apply(DataComponentExactPredicate.builder())).build());
    }

    private static ItemStack createStack(Holder<Item> holder, int i, DataComponentExactPredicate dataComponentExactPredicate) {
        return new ItemStack(holder, i, dataComponentExactPredicate.asPatch());
    }

    public boolean test(ItemStack itemStack) {
        return itemStack.is(this.item) && this.components.test(itemStack);
    }
}

