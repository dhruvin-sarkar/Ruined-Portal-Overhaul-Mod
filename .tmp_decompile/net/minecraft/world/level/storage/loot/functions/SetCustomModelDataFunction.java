/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction
extends LootItemConditionalFunction {
    private static final Codec<NumberProvider> COLOR_PROVIDER_CODEC = Codec.withAlternative(NumberProviders.CODEC, ExtraCodecs.RGB_COLOR_CODEC, ConstantValue::new);
    public static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCustomModelDataFunction.commonFields(instance).and(instance.group((App)ListOperation.StandAlone.codec(NumberProviders.CODEC, Integer.MAX_VALUE).optionalFieldOf("floats").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.floats), (App)ListOperation.StandAlone.codec(Codec.BOOL, Integer.MAX_VALUE).optionalFieldOf("flags").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.flags), (App)ListOperation.StandAlone.codec(Codec.STRING, Integer.MAX_VALUE).optionalFieldOf("strings").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.strings), (App)ListOperation.StandAlone.codec(COLOR_PROVIDER_CODEC, Integer.MAX_VALUE).optionalFieldOf("colors").forGetter(setCustomModelDataFunction -> setCustomModelDataFunction.colors))).apply((Applicative)instance, SetCustomModelDataFunction::new));
    private final Optional<ListOperation.StandAlone<NumberProvider>> floats;
    private final Optional<ListOperation.StandAlone<Boolean>> flags;
    private final Optional<ListOperation.StandAlone<String>> strings;
    private final Optional<ListOperation.StandAlone<NumberProvider>> colors;

    public SetCustomModelDataFunction(List<LootItemCondition> list, Optional<ListOperation.StandAlone<NumberProvider>> optional, Optional<ListOperation.StandAlone<Boolean>> optional2, Optional<ListOperation.StandAlone<String>> optional3, Optional<ListOperation.StandAlone<NumberProvider>> optional4) {
        super(list);
        this.floats = optional;
        this.flags = optional2;
        this.strings = optional3;
        this.colors = optional4;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Stream.concat(this.floats.stream(), this.colors.stream()).flatMap(standAlone -> standAlone.value().stream()).flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream()).collect(Collectors.toSet());
    }

    public LootItemFunctionType<SetCustomModelDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
    }

    private static <T> List<T> apply(Optional<ListOperation.StandAlone<T>> optional, List<T> list) {
        return optional.map(standAlone -> standAlone.apply(list)).orElse(list);
    }

    private static <T, E> List<E> apply(Optional<ListOperation.StandAlone<T>> optional, List<E> list, Function<T, E> function) {
        return optional.map(standAlone -> {
            List list2 = standAlone.value().stream().map(function).toList();
            return standAlone.operation().apply(list, list2);
        }).orElse(list);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        CustomModelData customModelData = itemStack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(SetCustomModelDataFunction.apply(this.floats, customModelData.floats(), numberProvider -> Float.valueOf(numberProvider.getFloat(lootContext))), SetCustomModelDataFunction.apply(this.flags, customModelData.flags()), SetCustomModelDataFunction.apply(this.strings, customModelData.strings()), SetCustomModelDataFunction.apply(this.colors, customModelData.colors(), numberProvider -> numberProvider.getInt(lootContext))));
        return itemStack;
    }
}

