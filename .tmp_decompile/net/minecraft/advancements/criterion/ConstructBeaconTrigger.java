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
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(i));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints level) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("level", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::level)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> constructedBeacon() {
            return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY));
        }

        public static Criterion<TriggerInstance> constructedBeacon(MinMaxBounds.Ints ints) {
            return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new TriggerInstance(Optional.empty(), ints));
        }

        public boolean matches(int i) {
            return this.level.matches(i);
        }
    }
}

