/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.BiPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DismountOrSkipMounting {
    public static <E extends LivingEntity> BehaviorControl<E> create(int i, BiPredicate<E, Entity> biPredicate) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.RIDE_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            Entity entity3;
            Entity entity = livingEntity.getVehicle();
            Entity entity2 = instance.tryGet(memoryAccessor).orElse(null);
            if (entity == null && entity2 == null) {
                return false;
            }
            Entity entity4 = entity3 = entity == null ? entity2 : entity;
            if (!DismountOrSkipMounting.isVehicleValid(livingEntity, entity3, i) || biPredicate.test(livingEntity, entity3)) {
                livingEntity.stopRiding();
                memoryAccessor.erase();
                return true;
            }
            return false;
        }));
    }

    private static boolean isVehicleValid(LivingEntity livingEntity, Entity entity, int i) {
        return entity.isAlive() && entity.closerThan(livingEntity, i) && entity.level() == livingEntity.level();
    }
}

