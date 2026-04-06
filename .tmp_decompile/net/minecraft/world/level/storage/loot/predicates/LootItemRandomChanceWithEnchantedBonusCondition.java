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
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public record LootItemRandomChanceWithEnchantedBonusCondition(float unenchantedChance, LevelBasedValue enchantedChance, Holder<Enchantment> enchantment) implements LootItemCondition
{
    public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("unenchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::unenchantedChance), (App)LevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantedChance), (App)Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantment)).apply((Applicative)instance, LootItemRandomChanceWithEnchantedBonusCondition::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext lootContext) {
        int n;
        Entity entity = lootContext.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            n = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingEntity);
        } else {
            n = 0;
        }
        int i = n;
        float f = i > 0 ? this.enchantedChance.calculate(i) : this.unenchantedChance;
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(HolderLookup.Provider provider, float f, float g) {
        HolderGetter registryLookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
        return () -> LootItemRandomChanceWithEnchantedBonusCondition.method_952(f, g, (HolderLookup.RegistryLookup)registryLookup);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    private static /* synthetic */ LootItemCondition method_952(float f, float g, HolderLookup.RegistryLookup registryLookup) {
        return new LootItemRandomChanceWithEnchantedBonusCondition(f, new LevelBasedValue.Linear(f + g, g), registryLookup.getOrThrow(Enchantments.LOOTING));
    }
}

