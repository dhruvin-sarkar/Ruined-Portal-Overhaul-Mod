/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.mutable.MutableFloat
 */
package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;

public record Enchantment(Component description, EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
    public static final int MAX_LEVEL = 255;
    public static final Codec<Enchantment> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description), (App)EnchantmentDefinition.CODEC.forGetter(Enchantment::definition), (App)RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.direct(new Holder[0])).forGetter(Enchantment::exclusiveSet), (App)EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", (Object)DataComponentMap.EMPTY).forGetter(Enchantment::effects)).apply((Applicative)instance, Enchantment::new));
    public static final Codec<Holder<Enchantment>> CODEC = RegistryFixedCodec.create(Registries.ENCHANTMENT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

    public static Cost constantCost(int i) {
        return new Cost(i, 0);
    }

    public static Cost dynamicCost(int i, int j) {
        return new Cost(i, j);
    }

    public static EnchantmentDefinition definition(HolderSet<Item> holderSet, HolderSet<Item> holderSet2, int i, int j, Cost cost, Cost cost2, int k, EquipmentSlotGroup ... equipmentSlotGroups) {
        return new EnchantmentDefinition(holderSet, Optional.of(holderSet2), i, j, cost, cost2, k, List.of((Object[])equipmentSlotGroups));
    }

    public static EnchantmentDefinition definition(HolderSet<Item> holderSet, int i, int j, Cost cost, Cost cost2, int k, EquipmentSlotGroup ... equipmentSlotGroups) {
        return new EnchantmentDefinition(holderSet, Optional.empty(), i, j, cost, cost2, k, List.of((Object[])equipmentSlotGroups));
    }

    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingEntity) {
        EnumMap map = Maps.newEnumMap(EquipmentSlot.class);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack;
            if (!this.matchingSlot(equipmentSlot) || (itemStack = livingEntity.getItemBySlot(equipmentSlot)).isEmpty()) continue;
            map.put(equipmentSlot, itemStack);
        }
        return map;
    }

    public HolderSet<Item> getSupportedItems() {
        return this.definition.supportedItems();
    }

    public boolean matchingSlot(EquipmentSlot equipmentSlot) {
        return this.definition.slots().stream().anyMatch(equipmentSlotGroup -> equipmentSlotGroup.test(equipmentSlot));
    }

    public boolean isPrimaryItem(ItemStack itemStack) {
        return this.isSupportedItem(itemStack) && (this.definition.primaryItems.isEmpty() || itemStack.is(this.definition.primaryItems.get()));
    }

    public boolean isSupportedItem(ItemStack itemStack) {
        return itemStack.is(this.definition.supportedItems);
    }

    public int getWeight() {
        return this.definition.weight();
    }

    public int getAnvilCost() {
        return this.definition.anvilCost();
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return this.definition.maxLevel();
    }

    public int getMinCost(int i) {
        return this.definition.minCost().calculate(i);
    }

    public int getMaxCost(int i) {
        return this.definition.maxCost().calculate(i);
    }

    public String toString() {
        return "Enchantment " + this.description.getString();
    }

    public static boolean areCompatible(Holder<Enchantment> holder, Holder<Enchantment> holder2) {
        return !holder.equals(holder2) && !holder.value().exclusiveSet.contains(holder2) && !holder2.value().exclusiveSet.contains(holder);
    }

    public static Component getFullname(Holder<Enchantment> holder, int i) {
        MutableComponent mutableComponent = holder.value().description.copy();
        mutableComponent = holder.is(EnchantmentTags.CURSE) ? ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.RED)) : ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
        if (i != 1 || holder.value().getMaxLevel() != 1) {
            mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + i));
        }
        return mutableComponent;
    }

    public boolean canEnchant(ItemStack itemStack) {
        return this.definition.supportedItems().contains(itemStack.getItemHolder());
    }

    public <T> List<T> getEffects(DataComponentType<List<T>> dataComponentType) {
        return this.effects.getOrDefault(dataComponentType, List.of());
    }

    public boolean isImmuneToDamage(ServerLevel serverLevel, int i, Entity entity, DamageSource damageSource) {
        LootContext lootContext = Enchantment.damageContext(serverLevel, i, entity, damageSource);
        for (ConditionalEffect conditionalEffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_IMMUNITY)) {
            if (!conditionalEffect.matches(lootContext)) continue;
            return true;
        }
        return false;
    }

    public void modifyDamageProtection(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        LootContext lootContext = Enchantment.damageContext(serverLevel, i, entity, damageSource);
        for (ConditionalEffect conditionalEffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_PROTECTION)) {
            if (!conditionalEffect.matches(lootContext)) continue;
            mutableFloat.setValue(((EnchantmentValueEffect)conditionalEffect.effect()).process(i, entity.getRandom(), mutableFloat.floatValue()));
        }
    }

    public void modifyDurabilityChange(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.ITEM_DAMAGE, serverLevel, i, itemStack, mutableFloat);
    }

    public void modifyAmmoCount(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.AMMO_USE, serverLevel, i, itemStack, mutableFloat);
    }

    public void modifyPiercingCount(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.PROJECTILE_PIERCING, serverLevel, i, itemStack, mutableFloat);
    }

    public void modifyBlockExperience(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.BLOCK_EXPERIENCE, serverLevel, i, itemStack, mutableFloat);
    }

    public void modifyMobExperience(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.MOB_EXPERIENCE, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyDurabilityToRepairFromXp(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.REPAIR_WITH_XP, serverLevel, i, itemStack, mutableFloat);
    }

    public void modifyTridentReturnToOwnerAcceleration(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyTridentSpinAttackStrength(RandomSource randomSource, int i, MutableFloat mutableFloat) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, randomSource, i, mutableFloat);
    }

    public void modifyFishingTimeReduction(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyFishingLuckBonus(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_LUCK_BONUS, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyDamage(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.DAMAGE, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
    }

    public void modifyFallBasedDamage(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
    }

    public void modifyKnockback(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.KNOCKBACK, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
    }

    public void modifyArmorEffectivness(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
    }

    public void doPostAttack(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, EnchantmentTarget enchantmentTarget, Entity entity, DamageSource damageSource) {
        for (TargetedConditionalEffect targetedConditionalEffect : this.getEffects(EnchantmentEffectComponents.POST_ATTACK)) {
            if (enchantmentTarget != targetedConditionalEffect.enchanted()) continue;
            Enchantment.doPostAttack(targetedConditionalEffect, serverLevel, i, enchantedItemInUse, entity, damageSource);
        }
    }

    public static void doPostAttack(TargetedConditionalEffect<EnchantmentEntityEffect> targetedConditionalEffect, ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, DamageSource damageSource) {
        if (targetedConditionalEffect.matches(Enchantment.damageContext(serverLevel, i, entity, damageSource))) {
            Entity entity2;
            switch (targetedConditionalEffect.affected()) {
                default: {
                    throw new MatchException(null, null);
                }
                case ATTACKER: {
                    Entity entity3 = damageSource.getEntity();
                    break;
                }
                case DAMAGING_ENTITY: {
                    Entity entity3 = damageSource.getDirectEntity();
                    break;
                }
                case VICTIM: {
                    Entity entity3 = entity2 = entity;
                }
            }
            if (entity2 != null) {
                targetedConditionalEffect.effect().apply(serverLevel, i, enchantedItemInUse, entity2, entity2.position());
            }
        }
    }

    public void doLunge(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity) {
        Enchantment.applyEffects(this.getEffects(EnchantmentEffectComponents.POST_PIERCING_ATTACK), Enchantment.entityContext(serverLevel, i, entity, entity.position()), enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, entity.position()));
    }

    public void modifyProjectileCount(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_COUNT, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyProjectileSpread(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_SPREAD, serverLevel, i, itemStack, entity, mutableFloat);
    }

    public void modifyCrossbowChargeTime(RandomSource randomSource, int i, MutableFloat mutableFloat) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, randomSource, i, mutableFloat);
    }

    public void modifyUnfilteredValue(DataComponentType<EnchantmentValueEffect> dataComponentType, RandomSource randomSource, int i, MutableFloat mutableFloat) {
        EnchantmentValueEffect enchantmentValueEffect = this.effects.get(dataComponentType);
        if (enchantmentValueEffect != null) {
            mutableFloat.setValue(enchantmentValueEffect.process(i, randomSource, mutableFloat.floatValue()));
        }
    }

    public void tick(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity) {
        Enchantment.applyEffects(this.getEffects(EnchantmentEffectComponents.TICK), Enchantment.entityContext(serverLevel, i, entity, entity.position()), enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, entity.position()));
    }

    public void onProjectileSpawned(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity) {
        Enchantment.applyEffects(this.getEffects(EnchantmentEffectComponents.PROJECTILE_SPAWNED), Enchantment.entityContext(serverLevel, i, entity, entity.position()), enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, entity.position()));
    }

    public void onHitBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, BlockState blockState) {
        Enchantment.applyEffects(this.getEffects(EnchantmentEffectComponents.HIT_BLOCK), Enchantment.blockHitContext(serverLevel, i, entity, vec3, blockState), enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, vec3));
    }

    private void modifyItemFilteredCount(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType, ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
        Enchantment.applyEffects(this.getEffects(dataComponentType), Enchantment.itemContext(serverLevel, i, itemStack), enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, serverLevel.getRandom(), mutableFloat.floatValue())));
    }

    private void modifyEntityFilteredValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType, ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
        Enchantment.applyEffects(this.getEffects(dataComponentType), Enchantment.entityContext(serverLevel, i, entity, entity.position()), enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, entity.getRandom(), mutableFloat.floatValue())));
    }

    private void modifyDamageFilteredValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType, ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
        Enchantment.applyEffects(this.getEffects(dataComponentType), Enchantment.damageContext(serverLevel, i, entity, damageSource), enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, entity.getRandom(), mutableFloat.floatValue())));
    }

    public static LootContext damageContext(ServerLevel serverLevel, int i, Entity entity, DamageSource damageSource) {
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).withParameter(LootContextParams.ORIGIN, entity.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity()).create(LootContextParamSets.ENCHANTED_DAMAGE);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    private static LootContext itemContext(ServerLevel serverLevel, int i, ItemStack itemStack) {
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.TOOL, itemStack).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).create(LootContextParamSets.ENCHANTED_ITEM);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    private static LootContext locationContext(ServerLevel serverLevel, int i, Entity entity, boolean bl) {
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).withParameter(LootContextParams.ORIGIN, entity.position()).withParameter(LootContextParams.ENCHANTMENT_ACTIVE, bl).create(LootContextParamSets.ENCHANTED_LOCATION);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    private static LootContext entityContext(ServerLevel serverLevel, int i, Entity entity, Vec3 vec3) {
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).withParameter(LootContextParams.ORIGIN, vec3).create(LootContextParamSets.ENCHANTED_ENTITY);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    private static LootContext blockHitContext(ServerLevel serverLevel, int i, Entity entity, Vec3 vec3, BlockState blockState) {
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).withParameter(LootContextParams.ORIGIN, vec3).withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.HIT_BLOCK);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    private static <T> void applyEffects(List<ConditionalEffect<T>> list, LootContext lootContext, Consumer<T> consumer) {
        for (ConditionalEffect<T> conditionalEffect : list) {
            if (!conditionalEffect.matches(lootContext)) continue;
            consumer.accept(conditionalEffect.effect());
        }
    }

    public void runLocationChangedEffects(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, LivingEntity livingEntity) {
        EquipmentSlot equipmentSlot = enchantedItemInUse.inSlot();
        if (equipmentSlot == null) {
            return;
        }
        Map<Enchantment, Set<EnchantmentLocationBasedEffect>> map = livingEntity.activeLocationDependentEnchantments(equipmentSlot);
        if (!this.matchingSlot(equipmentSlot)) {
            Set<EnchantmentLocationBasedEffect> set = map.remove((Object)this);
            if (set != null) {
                set.forEach(enchantmentLocationBasedEffect -> enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i));
            }
            return;
        }
        ObjectArraySet set = map.get((Object)this);
        for (ConditionalEffect conditionalEffect : this.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
            boolean bl;
            EnchantmentLocationBasedEffect enchantmentLocationBasedEffect2 = (EnchantmentLocationBasedEffect)conditionalEffect.effect();
            boolean bl2 = bl = set != null && set.contains(enchantmentLocationBasedEffect2);
            if (conditionalEffect.matches(Enchantment.locationContext(serverLevel, i, livingEntity, bl))) {
                if (!bl) {
                    if (set == null) {
                        set = new ObjectArraySet();
                        map.put(this, (Set<EnchantmentLocationBasedEffect>)set);
                    }
                    set.add((EnchantmentLocationBasedEffect)enchantmentLocationBasedEffect2);
                }
                enchantmentLocationBasedEffect2.onChangedBlock(serverLevel, i, enchantedItemInUse, livingEntity, livingEntity.position(), !bl);
                continue;
            }
            if (set == null || !set.remove(enchantmentLocationBasedEffect2)) continue;
            enchantmentLocationBasedEffect2.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i);
        }
        if (set != null && set.isEmpty()) {
            map.remove((Object)this);
        }
    }

    public void stopLocationBasedEffects(int i, EnchantedItemInUse enchantedItemInUse, LivingEntity livingEntity) {
        EquipmentSlot equipmentSlot = enchantedItemInUse.inSlot();
        if (equipmentSlot == null) {
            return;
        }
        Set<EnchantmentLocationBasedEffect> set = livingEntity.activeLocationDependentEnchantments(equipmentSlot).remove((Object)this);
        if (set == null) {
            return;
        }
        for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : set) {
            enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i);
        }
    }

    public static Builder enchantment(EnchantmentDefinition enchantmentDefinition) {
        return new Builder(enchantmentDefinition);
    }

    public static final class EnchantmentDefinition
    extends Record {
        final HolderSet<Item> supportedItems;
        final Optional<HolderSet<Item>> primaryItems;
        private final int weight;
        private final int maxLevel;
        private final Cost minCost;
        private final Cost maxCost;
        private final int anvilCost;
        private final List<EquipmentSlotGroup> slots;
        public static final MapCodec<EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("supported_items").forGetter(EnchantmentDefinition::supportedItems), (App)RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(EnchantmentDefinition::primaryItems), (App)ExtraCodecs.intRange(1, 1024).fieldOf("weight").forGetter(EnchantmentDefinition::weight), (App)ExtraCodecs.intRange(1, 255).fieldOf("max_level").forGetter(EnchantmentDefinition::maxLevel), (App)Cost.CODEC.fieldOf("min_cost").forGetter(EnchantmentDefinition::minCost), (App)Cost.CODEC.fieldOf("max_cost").forGetter(EnchantmentDefinition::maxCost), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anvil_cost").forGetter(EnchantmentDefinition::anvilCost), (App)EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(EnchantmentDefinition::slots)).apply((Applicative)instance, EnchantmentDefinition::new));

        public EnchantmentDefinition(HolderSet<Item> holderSet, Optional<HolderSet<Item>> optional, int i, int j, Cost cost, Cost cost2, int k, List<EquipmentSlotGroup> list) {
            this.supportedItems = holderSet;
            this.primaryItems = optional;
            this.weight = i;
            this.maxLevel = j;
            this.minCost = cost;
            this.maxCost = cost2;
            this.anvilCost = k;
            this.slots = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EnchantmentDefinition.class, "supportedItems;primaryItems;weight;maxLevel;minCost;maxCost;anvilCost;slots", "supportedItems", "primaryItems", "weight", "maxLevel", "minCost", "maxCost", "anvilCost", "slots"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EnchantmentDefinition.class, "supportedItems;primaryItems;weight;maxLevel;minCost;maxCost;anvilCost;slots", "supportedItems", "primaryItems", "weight", "maxLevel", "minCost", "maxCost", "anvilCost", "slots"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EnchantmentDefinition.class, "supportedItems;primaryItems;weight;maxLevel;minCost;maxCost;anvilCost;slots", "supportedItems", "primaryItems", "weight", "maxLevel", "minCost", "maxCost", "anvilCost", "slots"}, this, object);
        }

        public HolderSet<Item> supportedItems() {
            return this.supportedItems;
        }

        public Optional<HolderSet<Item>> primaryItems() {
            return this.primaryItems;
        }

        public int weight() {
            return this.weight;
        }

        public int maxLevel() {
            return this.maxLevel;
        }

        public Cost minCost() {
            return this.minCost;
        }

        public Cost maxCost() {
            return this.maxCost;
        }

        public int anvilCost() {
            return this.anvilCost;
        }

        public List<EquipmentSlotGroup> slots() {
            return this.slots;
        }
    }

    public record Cost(int base, int perLevelAboveFirst) {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("base").forGetter(Cost::base), (App)Codec.INT.fieldOf("per_level_above_first").forGetter(Cost::perLevelAboveFirst)).apply((Applicative)instance, Cost::new));

        public int calculate(int i) {
            return this.base + this.perLevelAboveFirst * (i - 1);
        }
    }

    public static class Builder {
        private final EnchantmentDefinition definition;
        private HolderSet<Enchantment> exclusiveSet = HolderSet.direct(new Holder[0]);
        private final Map<DataComponentType<?>, List<?>> effectLists = new HashMap();
        private final DataComponentMap.Builder effectMapBuilder = DataComponentMap.builder();

        public Builder(EnchantmentDefinition enchantmentDefinition) {
            this.definition = enchantmentDefinition;
        }

        public Builder exclusiveWith(HolderSet<Enchantment> holderSet) {
            this.exclusiveSet = holderSet;
            return this;
        }

        public <E> Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> dataComponentType, E object, LootItemCondition.Builder builder) {
            this.getEffectsList(dataComponentType).add(new ConditionalEffect<E>(object, Optional.of(builder.build())));
            return this;
        }

        public <E> Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> dataComponentType, E object) {
            this.getEffectsList(dataComponentType).add(new ConditionalEffect<E>(object, Optional.empty()));
            return this;
        }

        public <E> Builder withEffect(DataComponentType<List<TargetedConditionalEffect<E>>> dataComponentType, EnchantmentTarget enchantmentTarget, EnchantmentTarget enchantmentTarget2, E object, LootItemCondition.Builder builder) {
            this.getEffectsList(dataComponentType).add(new TargetedConditionalEffect<E>(enchantmentTarget, enchantmentTarget2, object, Optional.of(builder.build())));
            return this;
        }

        public <E> Builder withEffect(DataComponentType<List<TargetedConditionalEffect<E>>> dataComponentType, EnchantmentTarget enchantmentTarget, EnchantmentTarget enchantmentTarget2, E object) {
            this.getEffectsList(dataComponentType).add(new TargetedConditionalEffect<E>(enchantmentTarget, enchantmentTarget2, object, Optional.empty()));
            return this;
        }

        public Builder withEffect(DataComponentType<List<EnchantmentAttributeEffect>> dataComponentType, EnchantmentAttributeEffect enchantmentAttributeEffect) {
            this.getEffectsList(dataComponentType).add(enchantmentAttributeEffect);
            return this;
        }

        public <E> Builder withSpecialEffect(DataComponentType<E> dataComponentType, E object) {
            this.effectMapBuilder.set(dataComponentType, object);
            return this;
        }

        public Builder withEffect(DataComponentType<Unit> dataComponentType) {
            this.effectMapBuilder.set(dataComponentType, Unit.INSTANCE);
            return this;
        }

        private <E> List<E> getEffectsList(DataComponentType<List<E>> dataComponentType) {
            return this.effectLists.computeIfAbsent(dataComponentType, dataComponentType2 -> {
                ArrayList arrayList = new ArrayList();
                this.effectMapBuilder.set(dataComponentType, arrayList);
                return arrayList;
            });
        }

        public Enchantment build(Identifier identifier) {
            return new Enchantment(Component.translatable(Util.makeDescriptionId("enchantment", identifier)), this.definition, this.exclusiveSet, this.effectMapBuilder.build());
        }
    }
}

