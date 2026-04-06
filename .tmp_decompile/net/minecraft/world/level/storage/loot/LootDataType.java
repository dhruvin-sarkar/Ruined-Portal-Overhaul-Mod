/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record LootDataType<T>(ResourceKey<Registry<T>> registryKey, Codec<T> codec, Validator<T> validator) {
    public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<LootItemCondition>(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, LootDataType.createSimpleValidator());
    public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<LootItemFunction>(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, LootDataType.createSimpleValidator());
    public static final LootDataType<LootTable> TABLE = new LootDataType<LootTable>(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, LootDataType.createLootTableValidator());

    public void runValidation(ValidationContext validationContext, ResourceKey<T> resourceKey, T object) {
        this.validator.run(validationContext, resourceKey, object);
    }

    public static Stream<LootDataType<?>> values() {
        return Stream.of(PREDICATE, MODIFIER, TABLE);
    }

    private static <T extends LootContextUser> Validator<T> createSimpleValidator() {
        return (validationContext, resourceKey, lootContextUser) -> lootContextUser.validate(validationContext.enterElement(new ProblemReporter.RootElementPathElement(resourceKey), resourceKey));
    }

    private static Validator<LootTable> createLootTableValidator() {
        return (validationContext, resourceKey, lootTable) -> lootTable.validate(validationContext.setContextKeySet(lootTable.getParamSet()).enterElement(new ProblemReporter.RootElementPathElement(resourceKey), resourceKey));
    }

    @FunctionalInterface
    public static interface Validator<T> {
        public void run(ValidationContext var1, ResourceKey<T> var2, T var3);
    }
}

