/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class EnchantmentHelper {
    public static int getItemEnchantmentLevel(Holder<Enchantment> holder, ItemStack itemStack) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return itemEnchantments.getLevel(holder);
    }

    public static ItemEnchantments updateEnchantments(ItemStack itemStack, Consumer<ItemEnchantments.Mutable> consumer) {
        DataComponentType<ItemEnchantments> dataComponentType = EnchantmentHelper.getComponentType(itemStack);
        ItemEnchantments itemEnchantments = itemStack.get(dataComponentType);
        if (itemEnchantments == null) {
            return ItemEnchantments.EMPTY;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
        consumer.accept(mutable);
        ItemEnchantments itemEnchantments2 = mutable.toImmutable();
        itemStack.set(dataComponentType, itemEnchantments2);
        return itemEnchantments2;
    }

    public static boolean canStoreEnchantments(ItemStack itemStack) {
        return itemStack.has(EnchantmentHelper.getComponentType(itemStack));
    }

    public static void setEnchantments(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        itemStack.set(EnchantmentHelper.getComponentType(itemStack), itemEnchantments);
    }

    public static ItemEnchantments getEnchantmentsForCrafting(ItemStack itemStack) {
        return itemStack.getOrDefault(EnchantmentHelper.getComponentType(itemStack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> getComponentType(ItemStack itemStack) {
        return itemStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    public static boolean hasAnyEnchantments(ItemStack itemStack) {
        return !itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() || !itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static int processDurabilityChange(ServerLevel serverLevel, ItemStack itemStack, int i2) {
        MutableFloat mutableFloat = new MutableFloat((float)i2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyDurabilityChange(serverLevel, i, itemStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processAmmoUse(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2, int i2) {
        MutableFloat mutableFloat = new MutableFloat((float)i2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyAmmoCount(serverLevel, i, itemStack2, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processBlockExperience(ServerLevel serverLevel, ItemStack itemStack, int i2) {
        MutableFloat mutableFloat = new MutableFloat((float)i2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyBlockExperience(serverLevel, i, itemStack, mutableFloat));
        return mutableFloat.intValue();
    }

    public static int processMobExperience(ServerLevel serverLevel, @Nullable Entity entity, Entity entity2, int i2) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            MutableFloat mutableFloat = new MutableFloat((float)i2);
            EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).modifyMobExperience(serverLevel, i, enchantedItemInUse.itemStack(), entity2, mutableFloat));
            return mutableFloat.intValue();
        }
        return i2;
    }

    public static ItemStack createBook(EnchantmentInstance enchantmentInstance) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        itemStack.enchant(enchantmentInstance.enchantment(), enchantmentInstance.level());
        return itemStack;
    }

    private static void runIterationOnItem(ItemStack itemStack, EnchantmentVisitor enchantmentVisitor) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            enchantmentVisitor.accept((Holder)entry.getKey(), entry.getIntValue());
        }
    }

    private static void runIterationOnItem(ItemStack itemStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, EnchantmentInSlotVisitor enchantmentInSlotVisitor) {
        if (itemStack.isEmpty()) {
            return;
        }
        ItemEnchantments itemEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
        if (itemEnchantments == null || itemEnchantments.isEmpty()) {
            return;
        }
        EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            Holder holder = (Holder)entry.getKey();
            if (!((Enchantment)((Object)holder.value())).matchingSlot(equipmentSlot)) continue;
            enchantmentInSlotVisitor.accept(holder, entry.getIntValue(), enchantedItemInUse);
        }
    }

    private static void runIterationOnEquipment(LivingEntity livingEntity, EnchantmentInSlotVisitor enchantmentInSlotVisitor) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            EnchantmentHelper.runIterationOnItem(livingEntity.getItemBySlot(equipmentSlot), equipmentSlot, livingEntity, enchantmentInSlotVisitor);
        }
    }

    public static boolean isImmuneToDamage(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> mutableBoolean.setValue(mutableBoolean.isTrue() || ((Enchantment)((Object)((Object)holder.value()))).isImmuneToDamage(serverLevel, i, livingEntity, damageSource)));
        return mutableBoolean.isTrue();
    }

    public static float getDamageProtection(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).modifyDamageProtection(serverLevel, i, enchantedItemInUse.itemStack(), livingEntity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyDamage(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyFallBasedDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyFallBasedDamage(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyArmorEffectiveness(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyArmorEffectivness(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static float modifyKnockback(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyKnockback(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static void doPostAttackEffects(ServerLevel serverLevel, Entity entity, DamageSource damageSource) {
        Entity entity2 = damageSource.getEntity();
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, livingEntity.getWeaponItem());
        } else {
            EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, null);
        }
    }

    public static void doLungeEffects(ServerLevel serverLevel, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            EnchantmentHelper.runIterationOnItem(entity.getWeaponItem(), EquipmentSlot.MAINHAND, livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).doLunge(serverLevel, i, enchantedItemInUse, entity));
        }
    }

    public static void doPostAttackEffectsWithItemSource(ServerLevel serverLevel, Entity entity, DamageSource damageSource, @Nullable ItemStack itemStack) {
        EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverLevel, entity, damageSource, itemStack, null);
    }

    public static void doPostAttackEffectsWithItemSourceOnBreak(ServerLevel serverLevel, Entity entity, DamageSource damageSource, @Nullable ItemStack itemStack, @Nullable Consumer<Item> consumer) {
        LivingEntity livingEntity;
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).doPostAttack(serverLevel, i, enchantedItemInUse, EnchantmentTarget.VICTIM, entity, damageSource));
        }
        if (itemStack != null) {
            Entity entity2 = damageSource.getEntity();
            if (entity2 instanceof LivingEntity) {
                livingEntity = (LivingEntity)entity2;
                EnchantmentHelper.runIterationOnItem(itemStack, EquipmentSlot.MAINHAND, livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).doPostAttack(serverLevel, i, enchantedItemInUse, EnchantmentTarget.ATTACKER, entity, damageSource));
            } else if (consumer != null) {
                EnchantedItemInUse enchantedItemInUse2 = new EnchantedItemInUse(itemStack, null, null, consumer);
                EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).doPostAttack(serverLevel, i, enchantedItemInUse2, EnchantmentTarget.ATTACKER, entity, damageSource));
            }
        }
    }

    public static void runLocationChangedEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).runLocationChangedEffects(serverLevel, i, enchantedItemInUse, livingEntity));
    }

    public static void runLocationChangedEffects(ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        EnchantmentHelper.runIterationOnItem(itemStack, equipmentSlot, livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).runLocationChangedEffects(serverLevel, i, enchantedItemInUse, livingEntity));
    }

    public static void stopLocationBasedEffects(LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).stopLocationBasedEffects(i, enchantedItemInUse, livingEntity));
    }

    public static void stopLocationBasedEffects(ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        EnchantmentHelper.runIterationOnItem(itemStack, equipmentSlot, livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).stopLocationBasedEffects(i, enchantedItemInUse, livingEntity));
    }

    public static void tickEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> ((Enchantment)((Object)((Object)holder.value()))).tick(serverLevel, i, enchantedItemInUse, livingEntity));
    }

    public static int getEnchantmentLevel(Holder<Enchantment> holder, LivingEntity livingEntity) {
        Collection<ItemStack> iterable = holder.value().getSlotItems(livingEntity).values();
        int i = 0;
        for (ItemStack itemStack : iterable) {
            int j = EnchantmentHelper.getItemEnchantmentLevel(holder, itemStack);
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    public static int processProjectileCount(ServerLevel serverLevel, ItemStack itemStack, Entity entity, int i2) {
        MutableFloat mutableFloat = new MutableFloat((float)i2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyProjectileCount(serverLevel, i, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float processProjectileSpread(ServerLevel serverLevel, ItemStack itemStack, Entity entity, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyProjectileSpread(serverLevel, i, itemStack, entity, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getPiercingCount(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyPiercingCount(serverLevel, i, itemStack2, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static void onProjectileSpawned(ServerLevel serverLevel, ItemStack itemStack, Projectile projectile, Consumer<Item> consumer) {
        LivingEntity livingEntity;
        Entity entity = projectile.getOwner();
        LivingEntity livingEntity2 = entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null;
        EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, null, livingEntity2, consumer);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).onProjectileSpawned(serverLevel, i, enchantedItemInUse, projectile));
    }

    public static void onHitBlock(ServerLevel serverLevel, ItemStack itemStack, @Nullable LivingEntity livingEntity, Entity entity, @Nullable EquipmentSlot equipmentSlot, Vec3 vec3, BlockState blockState, Consumer<Item> consumer) {
        EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity, consumer);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).onHitBlock(serverLevel, i, enchantedItemInUse, entity, vec3, blockState));
    }

    public static int modifyDurabilityToRepairFromXp(ServerLevel serverLevel, ItemStack itemStack, int i2) {
        MutableFloat mutableFloat = new MutableFloat((float)i2);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyDurabilityToRepairFromXp(serverLevel, i, itemStack, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float processEquipmentDropChance(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        RandomSource randomSource = livingEntity.getRandom();
        EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> {
            LootContext lootContext = Enchantment.damageContext(serverLevel, i, livingEntity, damageSource);
            ((Enchantment)((Object)((Object)holder.value()))).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach(targetedConditionalEffect -> {
                if (targetedConditionalEffect.enchanted() == EnchantmentTarget.VICTIM && targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM && targetedConditionalEffect.matches(lootContext)) {
                    mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(i, randomSource, mutableFloat.floatValue()));
                }
            });
        });
        Entity entity = damageSource.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            EnchantmentHelper.runIterationOnEquipment(livingEntity2, (holder, i, enchantedItemInUse) -> {
                LootContext lootContext = Enchantment.damageContext(serverLevel, i, livingEntity, damageSource);
                ((Enchantment)((Object)((Object)holder.value()))).getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS).forEach(targetedConditionalEffect -> {
                    if (targetedConditionalEffect.enchanted() == EnchantmentTarget.ATTACKER && targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM && targetedConditionalEffect.matches(lootContext)) {
                        mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(i, randomSource, mutableFloat.floatValue()));
                    }
                });
            });
        }
        return mutableFloat.floatValue();
    }

    public static void forEachModifier(ItemStack itemStack, EquipmentSlotGroup equipmentSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(enchantmentAttributeEffect -> {
            if (((Enchantment)((Object)((Object)((Object)holder.value())))).definition().slots().contains(equipmentSlotGroup)) {
                biConsumer.accept(enchantmentAttributeEffect.attribute(), enchantmentAttributeEffect.getModifier(i, equipmentSlotGroup));
            }
        }));
    }

    public static void forEachModifier(ItemStack itemStack, EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(enchantmentAttributeEffect -> {
            if (((Enchantment)((Object)((Object)((Object)holder.value())))).matchingSlot(equipmentSlot)) {
                biConsumer.accept(enchantmentAttributeEffect.attribute(), enchantmentAttributeEffect.getModifier(i, equipmentSlot));
            }
        }));
    }

    public static int getFishingLuckBonus(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyFishingLuckBonus(serverLevel, i, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float getFishingTimeReduction(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyFishingTimeReduction(serverLevel, i, itemStack, entity, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static int getTridentReturnToOwnerAcceleration(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyTridentReturnToOwnerAcceleration(serverLevel, i, itemStack, entity, mutableFloat));
        return Math.max(0, mutableFloat.intValue());
    }

    public static float modifyCrossbowChargingTime(ItemStack itemStack, LivingEntity livingEntity, float f) {
        MutableFloat mutableFloat = new MutableFloat(f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyCrossbowChargeTime(livingEntity.getRandom(), i, mutableFloat));
        return Math.max(0.0f, mutableFloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack itemStack, LivingEntity livingEntity) {
        MutableFloat mutableFloat = new MutableFloat(0.0f);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> ((Enchantment)((Object)((Object)holder.value()))).modifyTridentSpinAttackStrength(livingEntity.getRandom(), i, mutableFloat));
        return mutableFloat.floatValue();
    }

    public static boolean hasTag(ItemStack itemStack, TagKey<Enchantment> tagKey) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            Holder holder = (Holder)entry.getKey();
            if (!holder.is(tagKey)) continue;
            return true;
        }
        return false;
    }

    public static boolean has(ItemStack itemStack, DataComponentType<?> dataComponentType) {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> {
            if (((Enchantment)((Object)((Object)holder.value()))).effects().has(dataComponentType)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.booleanValue();
    }

    public static <T> Optional<T> pickHighestLevel(ItemStack itemStack, DataComponentType<List<T>> dataComponentType) {
        Pair<List<T>, Integer> pair = EnchantmentHelper.getHighestLevel(itemStack, dataComponentType);
        if (pair != null) {
            List list = (List)pair.getFirst();
            int i = (Integer)pair.getSecond();
            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        }
        return Optional.empty();
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static <T> @Nullable Pair<T, Integer> getHighestLevel(ItemStack itemStack, DataComponentType<T> dataComponentType) {
        @Nullable MutableObject mutableObject = new MutableObject();
        EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> {
            Object object;
            if ((mutableObject.get() == null || (Integer)((Pair)mutableObject.get()).getSecond() < i) && (object = ((Enchantment)((Object)((Object)holder.value()))).effects().get(dataComponentType)) != null) {
                mutableObject.setValue((Object)Pair.of(object, (Object)i));
            }
        });
        return (Pair)mutableObject.get();
    }

    public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> dataComponentType, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        ArrayList<EnchantedItemInUse> list = new ArrayList<EnchantedItemInUse>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
            if (!predicate.test(itemStack)) continue;
            ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Holder holder = (Holder)entry.getKey();
                if (!((Enchantment)((Object)holder.value())).effects().has(dataComponentType) || !((Enchantment)((Object)holder.value())).matchingSlot(equipmentSlot)) continue;
                list.add(new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity));
            }
        }
        return Util.getRandomSafe(list, livingEntity.getRandom());
    }

    public static int getEnchantmentCost(RandomSource randomSource, int i, int j, ItemStack itemStack) {
        Enchantable enchantable = itemStack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) {
            return 0;
        }
        if (j > 15) {
            j = 15;
        }
        int k = randomSource.nextInt(8) + 1 + (j >> 1) + randomSource.nextInt(j + 1);
        if (i == 0) {
            return Math.max(k / 3, 1);
        }
        if (i == 1) {
            return k * 2 / 3 + 1;
        }
        return Math.max(k, j * 2);
    }

    public static ItemStack enchantItem(RandomSource randomSource, ItemStack itemStack, int i, RegistryAccess registryAccess, Optional<? extends HolderSet<Enchantment>> optional) {
        return EnchantmentHelper.enchantItem(randomSource, itemStack, i, optional.map(HolderSet::stream).orElseGet(() -> registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements().map(reference -> reference)));
    }

    public static ItemStack enchantItem(RandomSource randomSource, ItemStack itemStack, int i, Stream<Holder<Enchantment>> stream) {
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(randomSource, itemStack, i, stream);
        if (itemStack.is(Items.BOOK)) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentInstance enchantmentInstance : list) {
            itemStack.enchant(enchantmentInstance.enchantment(), enchantmentInstance.level());
        }
        return itemStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource randomSource, ItemStack itemStack, int i, Stream<Holder<Enchantment>> stream) {
        ArrayList list = Lists.newArrayList();
        Enchantable enchantable = itemStack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) {
            return list;
        }
        i += 1 + randomSource.nextInt(enchantable.value() / 4 + 1) + randomSource.nextInt(enchantable.value() / 4 + 1);
        float f = (randomSource.nextFloat() + randomSource.nextFloat() - 1.0f) * 0.15f;
        List<EnchantmentInstance> list2 = EnchantmentHelper.getAvailableEnchantmentResults(i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE), itemStack, stream);
        if (!list2.isEmpty()) {
            WeightedRandom.getRandomItem(randomSource, list2, EnchantmentInstance::weight).ifPresent(list::add);
            while (randomSource.nextInt(50) <= i) {
                if (!list.isEmpty()) {
                    EnchantmentHelper.filterCompatibleEnchantments(list2, (EnchantmentInstance)((Object)list.getLast()));
                }
                if (list2.isEmpty()) break;
                WeightedRandom.getRandomItem(randomSource, list2, EnchantmentInstance::weight).ifPresent(list::add);
                i /= 2;
            }
        }
        return list;
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentInstance) {
        list.removeIf(enchantmentInstance2 -> !Enchantment.areCompatible(enchantmentInstance.enchantment(), enchantmentInstance2.enchantment()));
    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Holder<Enchantment> holder) {
        for (Holder<Enchantment> holder2 : collection) {
            if (Enchantment.areCompatible(holder2, holder)) continue;
            return false;
        }
        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemStack, Stream<Holder<Enchantment>> stream) {
        ArrayList list = Lists.newArrayList();
        boolean bl = itemStack.is(Items.BOOK);
        stream.filter(holder -> ((Enchantment)((Object)((Object)holder.value()))).isPrimaryItem(itemStack) || bl).forEach(holder -> {
            Enchantment enchantment = (Enchantment)((Object)((Object)holder.value()));
            for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); --j) {
                if (i < enchantment.getMinCost(j) || i > enchantment.getMaxCost(j)) continue;
                list.add(new EnchantmentInstance((Holder<Enchantment>)holder, j));
                break;
            }
        });
        return list;
    }

    public static void enchantItemFromProvider(ItemStack itemStack, RegistryAccess registryAccess, ResourceKey<EnchantmentProvider> resourceKey, DifficultyInstance difficultyInstance, RandomSource randomSource) {
        EnchantmentProvider enchantmentProvider = registryAccess.lookupOrThrow(Registries.ENCHANTMENT_PROVIDER).getValue(resourceKey);
        if (enchantmentProvider != null) {
            EnchantmentHelper.updateEnchantments(itemStack, mutable -> enchantmentProvider.enchant(itemStack, (ItemEnchantments.Mutable)mutable, randomSource, difficultyInstance));
        }
    }

    @FunctionalInterface
    static interface EnchantmentVisitor {
        public void accept(Holder<Enchantment> var1, int var2);
    }

    @FunctionalInterface
    static interface EnchantmentInSlotVisitor {
        public void accept(Holder<Enchantment> var1, int var2, EnchantedItemInUse var3);
    }
}

