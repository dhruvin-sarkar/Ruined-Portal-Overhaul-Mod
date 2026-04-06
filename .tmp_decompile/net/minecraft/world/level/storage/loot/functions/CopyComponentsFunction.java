/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction
extends LootItemConditionalFunction {
    private static final Codec<LootContextArg<DataComponentGetter>> GETTER_CODEC = LootContextArg.createArgCodec(argCodecBuilder -> argCodecBuilder.anyEntity(DirectSource::new).anyBlockEntity(BlockEntitySource::new).anyItemStack(DirectSource::new));
    public static final MapCodec<CopyComponentsFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyComponentsFunction.commonFields(instance).and(instance.group((App)GETTER_CODEC.fieldOf("source").forGetter(copyComponentsFunction -> copyComponentsFunction.source), (App)DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter(copyComponentsFunction -> copyComponentsFunction.include), (App)DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter(copyComponentsFunction -> copyComponentsFunction.exclude))).apply((Applicative)instance, CopyComponentsFunction::new));
    private final LootContextArg<DataComponentGetter> source;
    private final Optional<List<DataComponentType<?>>> include;
    private final Optional<List<DataComponentType<?>>> exclude;
    private final Predicate<DataComponentType<?>> bakedPredicate;

    CopyComponentsFunction(List<LootItemCondition> list, LootContextArg<DataComponentGetter> lootContextArg, Optional<List<DataComponentType<?>>> optional, Optional<List<DataComponentType<?>>> optional2) {
        super(list);
        this.source = lootContextArg;
        this.include = optional.map((Function<List, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/List;)Ljava/util/List;)());
        this.exclude = optional2.map((Function<List, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/List;)Ljava/util/List;)());
        ArrayList list22 = new ArrayList(2);
        optional2.ifPresent(list2 -> list22.add(dataComponentType -> !list2.contains(dataComponentType)));
        optional.ifPresent(list2 -> list22.add(list2::contains));
        this.bakedPredicate = Util.allOf(list22);
    }

    public LootItemFunctionType<CopyComponentsFunction> getType() {
        return LootItemFunctions.COPY_COMPONENTS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        DataComponentGetter dataComponentGetter = this.source.get(lootContext);
        if (dataComponentGetter != null) {
            if (dataComponentGetter instanceof DataComponentMap) {
                DataComponentMap dataComponentMap = (DataComponentMap)dataComponentGetter;
                itemStack.applyComponents(dataComponentMap.filter(this.bakedPredicate));
            } else {
                Collection collection = this.exclude.orElse(List.of());
                this.include.map(Collection::stream).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE.listElements().map(Holder::value)).forEach(dataComponentType -> {
                    if (collection.contains(dataComponentType)) {
                        return;
                    }
                    TypedDataComponent typedDataComponent = dataComponentGetter.getTyped(dataComponentType);
                    if (typedDataComponent != null) {
                        itemStack.set(typedDataComponent);
                    }
                });
            }
        }
        return itemStack;
    }

    public static Builder copyComponentsFromEntity(ContextKey<? extends Entity> contextKey) {
        return new Builder(new DirectSource<Entity>(contextKey));
    }

    public static Builder copyComponentsFromBlockEntity(ContextKey<? extends BlockEntity> contextKey) {
        return new Builder(new BlockEntitySource(contextKey));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final LootContextArg<DataComponentGetter> source;
        private Optional<ImmutableList.Builder<DataComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<DataComponentType<?>>> exclude = Optional.empty();

        Builder(LootContextArg<DataComponentGetter> lootContextArg) {
            this.source = lootContextArg;
        }

        public Builder include(DataComponentType<?> dataComponentType) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }
            this.include.get().add(dataComponentType);
            return this;
        }

        public Builder exclude(DataComponentType<?> dataComponentType) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }
            this.exclude.get().add(dataComponentType);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyComponentsFunction(this.getConditions(), this.source, this.include.map(ImmutableList.Builder::build), this.exclude.map(ImmutableList.Builder::build));
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    record DirectSource<T extends DataComponentGetter>(ContextKey<? extends T> contextParam) implements LootContextArg.Getter<T, DataComponentGetter>
    {
        @Override
        public DataComponentGetter get(T dataComponentGetter) {
            return dataComponentGetter;
        }
    }

    record BlockEntitySource(ContextKey<? extends BlockEntity> contextParam) implements LootContextArg.Getter<BlockEntity, DataComponentGetter>
    {
        @Override
        public DataComponentGetter get(BlockEntity blockEntity) {
            return blockEntity.collectComponents();
        }
    }
}

