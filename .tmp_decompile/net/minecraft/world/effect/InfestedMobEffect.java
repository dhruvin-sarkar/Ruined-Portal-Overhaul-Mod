/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

class InfestedMobEffect
extends MobEffect {
    private final float chanceToSpawn;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected InfestedMobEffect(MobEffectCategory mobEffectCategory, int i, float f, ToIntFunction<RandomSource> toIntFunction) {
        super(mobEffectCategory, i, ParticleTypes.INFESTED);
        this.chanceToSpawn = f;
        this.spawnedCount = toIntFunction;
    }

    @Override
    public void onMobHurt(ServerLevel serverLevel, LivingEntity livingEntity, int i, DamageSource damageSource, float f) {
        if (livingEntity.getRandom().nextFloat() <= this.chanceToSpawn) {
            int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());
            for (int k = 0; k < j; ++k) {
                this.spawnSilverfish(serverLevel, livingEntity, livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getBbHeight() / 2.0, livingEntity.getZ());
            }
        }
    }

    private void spawnSilverfish(ServerLevel serverLevel, LivingEntity livingEntity, double d, double e, double f) {
        Silverfish silverfish = EntityType.SILVERFISH.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (silverfish == null) {
            return;
        }
        RandomSource randomSource = livingEntity.getRandom();
        float g = 1.5707964f;
        float h = Mth.randomBetween(randomSource, -1.5707964f, 1.5707964f);
        Vector3f vector3f = livingEntity.getLookAngle().toVector3f().mul(0.3f).mul(1.0f, 1.5f, 1.0f).rotateY(h);
        silverfish.snapTo(d, e, f, serverLevel.getRandom().nextFloat() * 360.0f, 0.0f);
        silverfish.setDeltaMovement(new Vec3((Vector3fc)vector3f));
        serverLevel.addFreshEntity(silverfish);
        silverfish.playSound(SoundEvents.SILVERFISH_HURT);
    }
}

