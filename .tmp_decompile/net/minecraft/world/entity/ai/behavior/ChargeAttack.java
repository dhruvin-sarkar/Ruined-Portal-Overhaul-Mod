/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public class ChargeAttack
extends Behavior<Animal> {
    private final int timeBetweenAttacks;
    private final TargetingConditions chargeTargeting;
    private final float speed;
    private final float knockbackForce;
    private final double maxTargetDetectionDistance;
    private final double maxChargeDistance;
    private final SoundEvent chargeSound;
    private Vec3 chargeVelocityVector;
    private Vec3 startPosition;

    public ChargeAttack(int i, TargetingConditions targetingConditions, float f, float g, double d, double e, SoundEvent soundEvent) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.timeBetweenAttacks = i;
        this.chargeTargeting = targetingConditions;
        this.speed = f;
        this.knockbackForce = g;
        this.maxChargeDistance = d;
        this.maxTargetDetectionDistance = e;
        this.chargeSound = soundEvent;
        this.chargeVelocityVector = Vec3.ZERO;
        this.startPosition = Vec3.ZERO;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Animal animal) {
        return animal.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Animal animal, long l) {
        TamableAnimal tamableAnimal;
        Brain<Integer> brain = animal.getBrain();
        Optional<LivingEntity> optional = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isEmpty()) {
            return false;
        }
        LivingEntity livingEntity = optional.get();
        if (animal instanceof TamableAnimal && (tamableAnimal = (TamableAnimal)animal).isTame()) {
            return false;
        }
        if (animal.position().subtract(this.startPosition).lengthSqr() >= this.maxChargeDistance * this.maxChargeDistance) {
            return false;
        }
        if (livingEntity.position().subtract(animal.position()).lengthSqr() >= this.maxTargetDetectionDistance * this.maxTargetDetectionDistance) {
            return false;
        }
        if (!animal.hasLineOfSight(livingEntity)) {
            return false;
        }
        return !brain.hasMemoryValue(MemoryModuleType.CHARGE_COOLDOWN_TICKS);
    }

    @Override
    protected void start(ServerLevel serverLevel, Animal animal, long l) {
        Brain<?> brain = animal.getBrain();
        this.startPosition = animal.position();
        LivingEntity livingEntity = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        Vec3 vec3 = livingEntity.position().subtract(animal.position()).normalize();
        this.chargeVelocityVector = vec3.scale(this.speed);
        if (this.canStillUse(serverLevel, animal, l)) {
            animal.playSound(this.chargeSound);
        }
    }

    @Override
    protected void tick(ServerLevel serverLevel, Animal animal, long l) {
        Brain<?> brain = animal.getBrain();
        LivingEntity livingEntity2 = (LivingEntity)brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
        animal.lookAt(livingEntity2, 360.0f, 360.0f);
        animal.setDeltaMovement(this.chargeVelocityVector);
        ArrayList list = new ArrayList(1);
        serverLevel.getEntities(EntityTypeTest.forClass(LivingEntity.class), animal.getBoundingBox(), livingEntity -> this.chargeTargeting.test(serverLevel, animal, (LivingEntity)livingEntity), list, 1);
        if (!list.isEmpty()) {
            LivingEntity livingEntity22 = (LivingEntity)list.get(0);
            if (animal.hasPassenger(livingEntity22)) {
                return;
            }
            this.dealDamageToTarget(serverLevel, animal, livingEntity22);
            this.dealKnockBack(animal, livingEntity22);
            this.stop(serverLevel, animal, l);
        }
    }

    private void dealDamageToTarget(ServerLevel serverLevel, Animal animal, LivingEntity livingEntity) {
        float f;
        DamageSource damageSource = serverLevel.damageSources().mobAttack(animal);
        if (livingEntity.hurtServer(serverLevel, damageSource, f = (float)animal.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
        }
    }

    private void dealKnockBack(Animal animal, LivingEntity livingEntity) {
        int i = animal.hasEffect(MobEffects.SPEED) ? animal.getEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
        int j = animal.hasEffect(MobEffects.SLOWNESS) ? animal.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1 : 0;
        float f = 0.25f * (float)(i - j);
        float g = Mth.clamp(this.speed * (float)animal.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.2f, 2.0f) + f;
        animal.causeExtraKnockback(livingEntity, g * this.knockbackForce, animal.getDeltaMovement());
    }

    @Override
    protected void stop(ServerLevel serverLevel, Animal animal, long l) {
        animal.getBrain().setMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS, this.timeBetweenAttacks);
        animal.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Animal)livingEntity, l);
    }
}

