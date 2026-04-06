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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer serverPlayer, RecipeHolder<?> recipeHolder) {
        ((SimpleCriterionTrigger)this).trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(recipeHolder));
    }

    public static Criterion<TriggerInstance> unlocked(ResourceKey<Recipe<?>> resourceKey) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new TriggerInstance(Optional.empty(), resourceKey));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Recipe.KEY_CODEC.fieldOf("recipe").forGetter(TriggerInstance::recipe)).apply((Applicative)instance, TriggerInstance::new));

        public boolean matches(RecipeHolder<?> recipeHolder) {
            return this.recipe == recipeHolder.id();
        }
    }
}

