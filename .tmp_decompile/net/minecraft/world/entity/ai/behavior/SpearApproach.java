/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.SpearAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jspecify.annotations.Nullable;

public class SpearApproach
extends Behavior<PathfinderMob> {
    double speedModifierWhenRepositioning;
    float approachDistanceSq;

    public SpearApproach(double d, float f) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        this.speedModifierWhenRepositioning = d;
        this.approachDistanceSq = f * f;
    }

    private boolean ableToAttack(PathfinderMob pathfinderMob) {
        return this.getTarget(pathfinderMob) != null && pathfinderMob.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return this.ableToAttack(pathfinderMob) && !pathfinderMob.isUsingItem();
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.setAggressive(true);
        pathfinderMob.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.APPROACH);
        super.start(serverLevel, pathfinderMob, l);
    }

    private @Nullable LivingEntity getTarget(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return this.ableToAttack(pathfinderMob) && this.farEnough(pathfinderMob);
    }

    private boolean farEnough(PathfinderMob pathfinderMob) {
        LivingEntity livingEntity = this.getTarget(pathfinderMob);
        double d = pathfinderMob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        return d > (double)this.approachDistanceSq;
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        LivingEntity livingEntity = this.getTarget(pathfinderMob);
        Entity entity = pathfinderMob.getRootVehicle();
        float f = 1.0f;
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            f = mob.chargeSpeedModifier();
        }
        pathfinderMob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
        pathfinderMob.getNavigation().moveTo(livingEntity, (double)f * this.speedModifierWhenRepositioning);
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.getNavigation().stop();
        pathfinderMob.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.CHARGING);
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

