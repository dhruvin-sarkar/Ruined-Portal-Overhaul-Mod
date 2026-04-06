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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCraftedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, ResourceKey<Recipe<?>> resourceKey, List<ItemStack> list) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey, list));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipeId, List<ItemPredicate> ingredients) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Recipe.KEY_CODEC.fieldOf("recipe_id").forGetter(TriggerInstance::recipeId), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", (Object)List.of()).forGetter(TriggerInstance::ingredients)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> craftedItem(ResourceKey<Recipe<?>> resourceKey, List<ItemPredicate.Builder> list) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceKey, list.stream().map(ItemPredicate.Builder::build).toList()));
        }

        public static Criterion<TriggerInstance> craftedItem(ResourceKey<Recipe<?>> resourceKey) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceKey, List.of()));
        }

        public static Criterion<TriggerInstance> crafterCraftedItem(ResourceKey<Recipe<?>> resourceKey) {
            return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceKey, List.of()));
        }

        boolean matches(ResourceKey<Recipe<?>> resourceKey, List<ItemStack> list) {
            if (resourceKey != this.recipeId) {
                return false;
            }
            ArrayList<ItemStack> list2 = new ArrayList<ItemStack>(list);
            for (ItemPredicate itemPredicate : this.ingredients) {
                boolean bl = false;
                Iterator iterator = list2.iterator();
                while (iterator.hasNext()) {
                    if (!itemPredicate.test((ItemStack)iterator.next())) continue;
                    iterator.remove();
                    bl = true;
                    break;
                }
                if (bl) continue;
                return false;
            }
            return true;
        }
    }
}

