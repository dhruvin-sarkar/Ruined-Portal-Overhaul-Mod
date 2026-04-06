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
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class SpearMobsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(i));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Integer> count) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(TriggerInstance::count)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> spearMobs(int i) {
            return CriteriaTriggers.SPEAR_MOBS_TRIGGER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(i)));
        }

        public boolean matches(int i) {
            return this.count.isEmpty() || i >= this.count.get();
        }
    }
}

