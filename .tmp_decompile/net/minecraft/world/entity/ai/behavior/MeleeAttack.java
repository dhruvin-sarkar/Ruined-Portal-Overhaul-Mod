/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class MeleeAttack {
    public static <T extends Mob> OneShot<T> create(int i) {
        return MeleeAttack.create(mob -> true, i);
    }

    public static <T extends Mob> OneShot<T> create(Predicate<T> predicate, int i) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
            LivingEntity livingEntity = (LivingEntity)instance.get(memoryAccessor2);
            if (predicate.test(mob) && !MeleeAttack.isHoldingUsableNonMeleeWeapon(mob) && mob.isWithinMeleeAttackRange(livingEntity) && ((NearestVisibleLivingEntities)instance.get(memoryAccessor4)).contains(livingEntity)) {
                memoryAccessor.set(new EntityTracker(livingEntity, true));
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(serverLevel, livingEntity);
                memoryAccessor3.setWithExpiry(true, i);
                return true;
            }
            return false;
        }));
    }

    private static boolean isHoldingUsableNonMeleeWeapon(Mob mob) {
        return mob.isHolding(mob::canUseNonMeleeWeapon);
    }
}

