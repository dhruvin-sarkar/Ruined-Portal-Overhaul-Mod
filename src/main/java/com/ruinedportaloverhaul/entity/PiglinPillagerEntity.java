package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class PiglinPillagerEntity extends Pillager {
    private static final float ARROW_DAMAGE = 5.0f;

    public PiglinPillagerEntity(EntityType<? extends PiglinPillagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        if (randomSource.nextFloat() < 0.25f) {
            Holder.Reference<Enchantment> enchantment = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.QUICK_CHARGE);
            crossbow.enchant(enchantment, 2);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, crossbow);
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
        arrow.shoot(dx, dy, dz, 1.6f, 0.9f);
        serverLevel.addFreshEntity(arrow);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }
}
