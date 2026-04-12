package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class PiglinBrutePillagerEntity extends Pillager {
    private static final float ARROW_DAMAGE = 7.0f;

    public PiglinBrutePillagerEntity(EntityType<? extends PiglinBrutePillagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
            .add(Attributes.MAX_HEALTH, 55.0)
            .add(Attributes.MOVEMENT_SPEED, 0.27)
            .add(Attributes.ATTACK_DAMAGE, 12.0);
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
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        Holder.Reference<Enchantment> enchantment = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.MULTISHOT);
        crossbow.enchant(enchantment, 1);
        this.setItemSlot(EquipmentSlot.MAINHAND, crossbow);
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack weapon = this.getMainHandItem();
        ItemStack ammo = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, distanceFactor, weapon);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.setBaseDamage(ARROW_DAMAGE);
        arrow.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(arrow);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    private final class CloseRangeMeleeGoal extends MeleeAttackGoal {
        private CloseRangeMeleeGoal() {
            super(PiglinBrutePillagerEntity.this, 1.0, false);
        }

        @Override
        public boolean canUse() {
            return PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange() && super.canContinueToUse();
        }
    }
}
