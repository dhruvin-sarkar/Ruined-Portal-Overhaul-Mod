/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

public final class ChargedProjectiles
implements TooltipProvider {
    public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
    public static final Codec<ChargedProjectiles> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectiles::new, chargedProjectiles -> chargedProjectiles.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ChargedProjectiles::new, chargedProjectiles -> chargedProjectiles.items);
    private final List<ItemStack> items;

    private ChargedProjectiles(List<ItemStack> list) {
        this.items = list;
    }

    public static ChargedProjectiles of(ItemStack itemStack) {
        return new ChargedProjectiles(List.of((Object)itemStack.copy()));
    }

    public static ChargedProjectiles of(List<ItemStack> list) {
        return new ChargedProjectiles(List.copyOf((Collection)Lists.transform(list, ItemStack::copy)));
    }

    public boolean contains(Item item) {
        for (ItemStack itemStack : this.items) {
            if (!itemStack.is(item)) continue;
            return true;
        }
        return false;
    }

    public List<ItemStack> getItems() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChargedProjectiles)) return false;
        ChargedProjectiles chargedProjectiles = (ChargedProjectiles)object;
        if (!ItemStack.listMatches(this.items, chargedProjectiles.items)) return false;
        return true;
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "ChargedProjectiles[items=" + String.valueOf(this.items) + "]";
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        ItemStack itemStack = null;
        int i = 0;
        for (ItemStack itemStack2 : this.items) {
            if (itemStack == null) {
                itemStack = itemStack2;
                i = 1;
                continue;
            }
            if (ItemStack.matches(itemStack, itemStack2)) {
                ++i;
                continue;
            }
            ChargedProjectiles.addProjectileTooltip(tooltipContext, consumer, itemStack, i);
            itemStack = itemStack2;
            i = 1;
        }
        if (itemStack != null) {
            ChargedProjectiles.addProjectileTooltip(tooltipContext, consumer, itemStack, i);
        }
    }

    private static void addProjectileTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, ItemStack itemStack, int i) {
        if (i == 1) {
            consumer.accept(Component.translatable("item.minecraft.crossbow.projectile.single", itemStack.getDisplayName()));
        } else {
            consumer.accept(Component.translatable("item.minecraft.crossbow.projectile.multiple", i, itemStack.getDisplayName()));
        }
        TooltipDisplay tooltipDisplay = itemStack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        itemStack.addDetailsToTooltip(tooltipContext, tooltipDisplay, null, TooltipFlag.NORMAL, component -> consumer.accept(Component.literal("  ").append((Component)component).withStyle(ChatFormatting.GRAY)));
    }
}

