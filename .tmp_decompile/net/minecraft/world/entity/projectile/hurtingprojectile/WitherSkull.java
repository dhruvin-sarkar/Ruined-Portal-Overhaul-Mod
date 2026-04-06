/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WitherSkull
extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_DANGEROUS = false;

    public WitherSkull(EntityType<? extends WitherSkull> entityType, Level level) {
        super((EntityType<? extends AbstractHurtingProjectile>)entityType, level);
    }

    public WitherSkull(Level level, LivingEntity livingEntity, Vec3 vec3) {
        super(EntityType.WITHER_SKULL, livingEntity, vec3, level);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73f : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        if (this.isDangerous() && WitherBoss.canDestroy(blockState)) {
            return Math.min(0.8f, f);
        }
        return f;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        boolean bl;
        LivingEntity livingEntity;
        super.onHitEntity(entityHitResult);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Entity entity = entityHitResult.getEntity();
        Entity entity2 = this.getOwner();
        if (entity2 instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity2;
            DamageSource damageSource = this.damageSources().witherSkull(this, livingEntity);
            bl = entity.hurtServer(serverLevel, damageSource, 8.0f);
            if (bl) {
                if (entity.isAlive()) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
                } else {
                    livingEntity.heal(5.0f);
                }
            }
        } else {
            bl = entity.hurtServer(serverLevel, this.damageSources().magic(), 5.0f);
        }
        if (bl && entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            int i = 0;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
                i = 10;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
                i = 40;
            }
            if (i > 0) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), 1.0f, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void setDangerous(boolean bl) {
        this.entityData.set(DATA_DANGEROUS, bl);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("dangerous", this.isDangerous());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setDangerous(valueInput.getBooleanOr("dangerous", false));
    }
}

