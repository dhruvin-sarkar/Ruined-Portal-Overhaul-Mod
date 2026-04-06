/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> BehaviorControl<E> create(TargetErasedCallback<E> targetErasedCallback) {
        return StopAttackingIfTargetInvalid.create((serverLevel, livingEntity) -> false, targetErasedCallback, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(StopAttackCondition stopAttackCondition) {
        return StopAttackingIfTargetInvalid.create(stopAttackCondition, (serverLevel, mob, livingEntity) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create() {
        return StopAttackingIfTargetInvalid.create((serverLevel, livingEntity) -> false, (serverLevel, mob, livingEntity) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(StopAttackCondition stopAttackCondition, TargetErasedCallback<E> targetErasedCallback, boolean bl) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
            LivingEntity livingEntity = (LivingEntity)instance.get(memoryAccessor);
            if (!mob.canAttack(livingEntity) || bl && StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget(mob, instance.tryGet(memoryAccessor2)) || !livingEntity.isAlive() || livingEntity.level() != mob.level() || stopAttackCondition.test(serverLevel, livingEntity)) {
                targetErasedCallback.accept(serverLevel, mob, livingEntity);
                memoryAccessor.erase();
                return true;
            }
            return true;
        }));
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
        return optional.isPresent() && livingEntity.level().getGameTime() - optional.get() > 200L;
    }

    @FunctionalInterface
    public static interface StopAttackCondition {
        public boolean test(ServerLevel var1, LivingEntity var2);
    }

    @FunctionalInterface
    public static interface TargetErasedCallback<E> {
        public void accept(ServerLevel var1, E var2, LivingEntity var3);
    }
}

