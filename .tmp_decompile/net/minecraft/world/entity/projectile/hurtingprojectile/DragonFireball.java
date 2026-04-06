/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.hurtingprojectile;

import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DragonFireball
extends AbstractHurtingProjectile {
    public static final float SPLASH_RANGE = 4.0f;

    public DragonFireball(EntityType<? extends DragonFireball> entityType, Level level) {
        super((EntityType<? extends AbstractHurtingProjectile>)entityType, level);
    }

    public DragonFireball(Level level, LivingEntity livingEntity, Vec3 vec3) {
        super(EntityType.DRAGON_FIREBALL, livingEntity, vec3, level);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.ownedBy(((EntityHitResult)hitResult).getEntity())) {
            return;
        }
        if (!this.level().isClientSide()) {
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloud.setOwner((LivingEntity)entity);
            }
            areaEffectCloud.setCustomParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f));
            areaEffectCloud.setRadius(3.0f);
            areaEffectCloud.setDuration(600);
            areaEffectCloud.setRadiusPerTick((7.0f - areaEffectCloud.getRadius()) / (float)areaEffectCloud.getDuration());
            areaEffectCloud.setPotionDurationScale(0.25f);
            areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
            if (!list.isEmpty()) {
                for (LivingEntity livingEntity : list) {
                    double d = this.distanceToSqr(livingEntity);
                    if (!(d < 16.0)) continue;
                    areaEffectCloud.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    break;
                }
            }
            this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
            this.level().addFreshEntity(areaEffectCloud);
            this.discard();
        }
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}

