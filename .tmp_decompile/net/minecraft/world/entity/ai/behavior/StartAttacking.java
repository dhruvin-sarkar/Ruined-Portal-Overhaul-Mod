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

public class StartAttacking {
    public static <E extends Mob> BehaviorControl<E> create(TargetFinder<E> targetFinder) {
        return StartAttacking.create((serverLevel, mob) -> true, targetFinder);
    }

    public static <E extends Mob> BehaviorControl<E> create(StartAttackingCondition<E> startAttackingCondition, TargetFinder<E> targetFinder) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
            if (!startAttackingCondition.test(serverLevel, mob)) {
                return false;
            }
            Optional<LivingEntity> optional = targetFinder.get(serverLevel, mob);
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity livingEntity = optional.get();
            if (!mob.canAttack(livingEntity)) {
                return false;
            }
            memoryAccessor.set(livingEntity);
            memoryAccessor2.erase();
            return true;
        }));
    }

    @FunctionalInterface
    public static interface StartAttackingCondition<E> {
        public boolean test(ServerLevel var1, E var2);
    }

    @FunctionalInterface
    public static interface TargetFinder<E> {
        public Optional<? extends LivingEntity> get(ServerLevel var1, E var2);
    }
}

