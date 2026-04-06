/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger
implements CriterionTrigger<TriggerInstance> {
    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance() implements CriterionTriggerInstance
    {
        public static final Codec<TriggerInstance> CODEC = MapCodec.unitCodec((Object)new TriggerInstance());

        @Override
        public void validate(CriterionValidator criterionValidator) {
        }
    }
}

