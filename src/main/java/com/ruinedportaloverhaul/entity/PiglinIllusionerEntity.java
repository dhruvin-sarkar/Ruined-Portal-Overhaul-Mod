package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class PiglinIllusionerEntity extends Illusioner {
    private static final int BLINDNESS_COOLDOWN_TICKS = 180;
    private static final float ARROW_DAMAGE = 8.0f;

    private int blindnessCooldown;

    public PiglinIllusionerEntity(EntityType<? extends PiglinIllusionerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Illusioner.createAttributes()
            .add(Attributes.MAX_HEALTH, 54.0)
            .add(Attributes.MOVEMENT_SPEED, 0.31);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        ItemStack bow = new ItemStack(Items.BOW);
        Holder.Reference<Enchantment> flame = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.FLAME);
        bow.enchant(flame, 1);
        Holder.Reference<Enchantment> power = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.POWER);
        bow.enchant(power, 3);
        if (randomSource.nextBoolean()) {
            Holder.Reference<Enchantment> punch = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PUNCH);
            bow.enchant(punch, 1);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        if (this.blindnessCooldown > 0) {
            this.blindnessCooldown--;
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && this.hasLineOfSight(target) && this.distanceToSqr(target) < 144.0) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 180, 0), this);
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.HOSTILE, 0.65f, 0.58f);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.PIGLIN_ANGRY, SoundSource.HOSTILE, 0.55f, 0.85f);
            this.blindnessCooldown = BLINDNESS_COOLDOWN_TICKS;
        }
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
        arrow.igniteForSeconds(4.0f);
        arrow.shoot(dx, dy, dz, 1.6f, 0.9f);
        serverLevel.addFreshEntity(arrow);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.55f, 1.45f);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.PIGLIN_ANGRY, SoundSource.HOSTILE, 0.45f, 1.10f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }
}
