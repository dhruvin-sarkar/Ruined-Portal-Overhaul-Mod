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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpearRetreat
extends Behavior<PathfinderMob> {
    public static final int MIN_COOLDOWN_DISTANCE = 9;
    public static final int MAX_COOLDOWN_DISTANCE = 11;
    public static final int MAX_FLEEING_TIME = 100;
    double speedModifierWhenRepositioning;

    public SpearRetreat(double d) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 100);
        this.speedModifierWhenRepositioning = d;
    }

    private @Nullable LivingEntity getTarget(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private boolean ableToAttack(PathfinderMob pathfinderMob) {
        return this.getTarget(pathfinderMob) != null && pathfinderMob.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        double e;
        if (!this.ableToAttack(pathfinderMob) || pathfinderMob.isUsingItem()) {
            return false;
        }
        if (pathfinderMob.getBrain().getMemory(MemoryModuleType.SPEAR_STATUS).orElse(SpearAttack.SpearStatus.APPROACH) != SpearAttack.SpearStatus.RETREAT) {
            return false;
        }
        LivingEntity livingEntity = this.getTarget(pathfinderMob);
        double d = pathfinderMob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        int i = pathfinderMob.isPassenger() ? 2 : 0;
        Vec3 vec3 = LandRandomPos.getPosAway(pathfinderMob, Math.max(0.0, (double)(9 + i) - (e = Math.sqrt(d))), Math.max(1.0, (double)(11 + i) - e), 7, livingEntity.position());
        if (vec3 == null) {
            return false;
        }
        pathfinderMob.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_POSITION, vec3);
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.setAggressive(true);
        pathfinderMob.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, 0);
        super.start(serverLevel, pathfinderMob, l);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(100) < 100 && pathfinderMob.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION).isPresent() && !pathfinderMob.getNavigation().isDone() && this.ableToAttack(pathfinderMob);
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        float f;
        LivingEntity livingEntity = this.getTarget(pathfinderMob);
        Entity entity = pathfinderMob.getRootVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            f = mob.chargeSpeedModifier();
        } else {
            f = 1.0f;
        }
        float f2 = f;
        pathfinderMob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
        pathfinderMob.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, pathfinderMob.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(0) + 1);
        pathfinderMob.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION).ifPresent(vec3 -> pathfinderMob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, (double)f2 * this.speedModifierWhenRepositioning));
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.getNavigation().stop();
        pathfinderMob.setAggressive(false);
        pathfinderMob.stopUsingItem();
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_TIME);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_POSITION);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.SPEAR_STATUS);
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

