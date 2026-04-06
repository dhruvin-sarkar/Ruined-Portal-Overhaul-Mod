/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, i));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints levels) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::levels)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> enchantedItem() {
            return CriteriaTriggers.ENCHANTED_ITEM.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack itemStack, int i) {
            if (this.item.isPresent() && !this.item.get().test(itemStack)) {
                return false;
            }
            return this.levels.matches(i);
        }
    }
}

