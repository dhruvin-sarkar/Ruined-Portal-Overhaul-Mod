/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int l = 0; l < inventory.getContainerSize(); ++l) {
            ItemStack itemStack2 = inventory.getItem(l);
            if (itemStack2.isEmpty()) {
                ++j;
                continue;
            }
            ++k;
            if (itemStack2.getCount() < itemStack2.getMaxStackSize()) continue;
            ++i;
        }
        this.trigger(serverPlayer, inventory, itemStack, i, j, k);
    }

    private void trigger(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack, int i, int j, int k) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(inventory, itemStack, i, j, k));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Slots slots, List<ItemPredicate> items) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Slots.CODEC.optionalFieldOf("slots", (Object)Slots.ANY).forGetter(TriggerInstance::slots), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("items", (Object)List.of()).forGetter(TriggerInstance::items)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasItems(ItemPredicate.Builder ... builders) {
            return TriggerInstance.hasItems((ItemPredicate[])Stream.of(builders).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
        }

        public static Criterion<TriggerInstance> hasItems(ItemPredicate ... itemPredicates) {
            return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Slots.ANY, List.of((Object[])itemPredicates)));
        }

        public static Criterion<TriggerInstance> hasItems(ItemLike ... itemLikes) {
            ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];
            for (int i = 0; i < itemLikes.length; ++i) {
                itemPredicates[i] = new ItemPredicate(Optional.of(HolderSet.direct(itemLikes[i].asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
            }
            return TriggerInstance.hasItems(itemPredicates);
        }

        public boolean matches(Inventory inventory, ItemStack itemStack, int i, int j, int k) {
            if (!this.slots.matches(i, j, k)) {
                return false;
            }
            if (this.items.isEmpty()) {
                return true;
            }
            if (this.items.size() == 1) {
                return !itemStack.isEmpty() && this.items.get(0).test(itemStack);
            }
            ObjectArrayList list = new ObjectArrayList(this.items);
            int l = inventory.getContainerSize();
            for (int m = 0; m < l; ++m) {
                if (list.isEmpty()) {
                    return true;
                }
                ItemStack itemStack2 = inventory.getItem(m);
                if (itemStack2.isEmpty()) continue;
                list.removeIf(itemPredicate -> itemPredicate.test(itemStack2));
            }
            return list.isEmpty();
        }

        public record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<Slots> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::occupied), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("full", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::full), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::empty)).apply((Applicative)instance, Slots::new));
            public static final Slots ANY = new Slots(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);

            public boolean matches(int i, int j, int k) {
                if (!this.full.matches(i)) {
                    return false;
                }
                if (!this.empty.matches(j)) {
                    return false;
                }
                return this.occupied.matches(k);
            }
        }
    }
}

