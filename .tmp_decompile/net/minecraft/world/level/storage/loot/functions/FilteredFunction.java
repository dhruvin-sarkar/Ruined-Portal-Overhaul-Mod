/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction
extends LootItemConditionalFunction {
    public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredFunction.commonFields(instance).and(instance.group((App)ItemPredicate.CODEC.fieldOf("item_filter").forGetter(filteredFunction -> filteredFunction.filter), (App)LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_pass").forGetter(filteredFunction -> filteredFunction.onPass), (App)LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_fail").forGetter(filteredFunction -> filteredFunction.onFail))).apply((Applicative)instance, FilteredFunction::new));
    private final ItemPredicate filter;
    private final Optional<LootItemFunction> onPass;
    private final Optional<LootItemFunction> onFail;

    FilteredFunction(List<LootItemCondition> list, ItemPredicate itemPredicate, Optional<LootItemFunction> optional, Optional<LootItemFunction> optional2) {
        super(list);
        this.filter = itemPredicate;
        this.onPass = optional;
        this.onFail = optional2;
    }

    public LootItemFunctionType<FilteredFunction> getType() {
        return LootItemFunctions.FILTERED;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Optional<LootItemFunction> optional;
        Optional<LootItemFunction> optional2 = optional = this.filter.test(itemStack) ? this.onPass : this.onFail;
        if (optional.isPresent()) {
            return (ItemStack)optional.get().apply(itemStack, lootContext);
        }
        return itemStack;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        this.onPass.ifPresent(lootItemFunction -> lootItemFunction.validate(validationContext.forChild(new ProblemReporter.FieldPathElement("on_pass"))));
        this.onFail.ifPresent(lootItemFunction -> lootItemFunction.validate(validationContext.forChild(new ProblemReporter.FieldPathElement("on_fail"))));
    }

    public static Builder filtered(ItemPredicate itemPredicate) {
        return new Builder(itemPredicate);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ItemPredicate itemPredicate;
        private Optional<LootItemFunction> onPass = Optional.empty();
        private Optional<LootItemFunction> onFail = Optional.empty();

        Builder(ItemPredicate itemPredicate) {
            this.itemPredicate = itemPredicate;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder onPass(Optional<LootItemFunction> optional) {
            this.onPass = optional;
            return this;
        }

        public Builder onFail(Optional<LootItemFunction> optional) {
            this.onFail = optional;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new FilteredFunction(this.getConditions(), this.itemPredicate, this.onPass, this.onFail);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

