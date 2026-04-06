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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
        List list = collection.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(list));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", (Object)List.of()).forGetter(TriggerInstance::victims)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> channeledLightning(EntityPredicate.Builder ... builders) {
            return CriteriaTriggers.CHANNELED_LIGHTNING.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(builders)));
        }

        public boolean matches(Collection<? extends LootContext> collection) {
            for (ContextAwarePredicate contextAwarePredicate : this.victims) {
                boolean bl = false;
                for (LootContext lootContext : collection) {
                    if (!contextAwarePredicate.matches(lootContext)) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntities(this.victims, "victims");
        }
    }
}

