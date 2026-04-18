package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class PiglinBrutePillagerEntity extends Pillager {
    private static final float ARROW_DAMAGE = 11.0f;

    public PiglinBrutePillagerEntity(EntityType<? extends PiglinBrutePillagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
            .add(Attributes.MAX_HEALTH, 88.0)
            .add(Attributes.MOVEMENT_SPEED, 0.29)
            .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new CloseRangeMeleeGoal());
    }

    private boolean isTargetWithinMeleeFallbackRange() {
        LivingEntity target = this.getTarget();
        return target != null && this.distanceToSqr(target) <= 9.0;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        float roll = randomSource.nextFloat();
        if (roll < 0.70f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, Items.GOLDEN_AXE));
        } else if (roll < 0.88f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, Items.GOLDEN_SWORD));
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, createCrossbow(randomSource));
            this.setItemSlot(EquipmentSlot.OFFHAND, createMeleeWeapon(randomSource, randomSource.nextBoolean() ? Items.GOLDEN_AXE : Items.GOLDEN_SWORD));
        }
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
    }

    private ItemStack createCrossbow(RandomSource randomSource) {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(enchantment(Enchantments.MULTISHOT), 1);
        crossbow.enchant(enchantment(Enchantments.QUICK_CHARGE), 2);
        if (randomSource.nextBoolean()) {
            crossbow.enchant(enchantment(Enchantments.PIERCING), 1);
        }
        return crossbow;
    }

    private ItemStack createMeleeWeapon(RandomSource randomSource, net.minecraft.world.item.Item item) {
        ItemStack weapon = new ItemStack(item);
        weapon.enchant(enchantment(Enchantments.SHARPNESS), 4 + randomSource.nextInt(2));
        if (randomSource.nextFloat() < 0.34f) {
            weapon.enchant(enchantment(Enchantments.KNOCKBACK), 1);
        }
        return weapon;
    }

    private Holder.Reference<Enchantment> enchantment(ResourceKey<Enchantment> key) {
        return this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack weapon = this.getMainHandItem();
        if (!weapon.is(Items.CROSSBOW)) {
            return;
        }
        ItemStack ammo = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, distanceFactor, weapon);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.setBaseDamage(ARROW_DAMAGE);
        arrow.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(arrow);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.playSound(SoundEvents.PIGLIN_BRUTE_ANGRY, 0.75f, 0.75f + this.getRandom().nextFloat() * 0.25f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_BRUTE_DEATH;
    }

    private final class CloseRangeMeleeGoal extends MeleeAttackGoal {
        private CloseRangeMeleeGoal() {
            super(PiglinBrutePillagerEntity.this, 1.0, false);
        }

        @Override
        public boolean canUse() {
            return (!PiglinBrutePillagerEntity.this.getMainHandItem().is(Items.CROSSBOW)
                || PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange())
                && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return (!PiglinBrutePillagerEntity.this.getMainHandItem().is(Items.CROSSBOW)
                || PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange())
                && super.canContinueToUse();
        }
    }
}
