/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public class KilledByArrowTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, @Nullable ItemStack itemStack) {
        ArrayList list = Lists.newArrayList();
        HashSet set = Sets.newHashSet();
        for (Entity entity : collection) {
            set.add(entity.getType());
            list.add(EntityPredicate.createContext(serverPlayer, entity));
        }
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list, set.size(), itemStack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes, Optional<ItemPredicate> firedFromWeapon) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", (Object)List.of()).forGetter(TriggerInstance::victims), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("unique_entity_types", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::uniqueEntityTypes), (App)ItemPredicate.CODEC.optionalFieldOf("fired_from_weapon").forGetter(TriggerInstance::firedFromWeapon)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> crossbowKilled(HolderGetter<Item> holderGetter, EntityPredicate.Builder ... builders) {
            return CriteriaTriggers.KILLED_BY_ARROW.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(builders), MinMaxBounds.Ints.ANY, Optional.of(ItemPredicate.Builder.item().of(holderGetter, Items.CROSSBOW).build())));
        }

        public static Criterion<TriggerInstance> crossbowKilled(HolderGetter<Item> holderGetter, MinMaxBounds.Ints ints) {
            return CriteriaTriggers.KILLED_BY_ARROW.createCriterion(new TriggerInstance(Optional.empty(), List.of(), ints, Optional.of(ItemPredicate.Builder.item().of(holderGetter, Items.CROSSBOW).build())));
        }

        public boolean matches(Collection<LootContext> collection, int i, @Nullable ItemStack itemStack) {
            if (this.firedFromWeapon.isPresent() && (itemStack == null || !this.firedFromWeapon.get().test(itemStack))) {
                return false;
            }
            if (!this.victims.isEmpty()) {
                ArrayList list = Lists.newArrayList(collection);
                for (ContextAwarePredicate contextAwarePredicate : this.victims) {
                    boolean bl = false;
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext()) {
                        LootContext lootContext = (LootContext)iterator.next();
                        if (!contextAwarePredicate.matches(lootContext)) continue;
                        iterator.remove();
                        bl = true;
                        break;
                    }
                    if (bl) continue;
                    return false;
                }
            }
            return this.uniqueEntityTypes.matches(i);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntities(this.victims, "victims");
        }
    }
}

