/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
    private static final int HIDE_TIMEOUT = 300;

    public static BehaviorControl<LivingEntity> create(int i, int j) {
        int k = i * 20;
        MutableInt mutableInt = new MutableInt(0);
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.HIDING_PLACE), instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            boolean bl;
            long m = (Long)instance.get(memoryAccessor2);
            boolean bl2 = bl = m + 300L <= l;
            if (mutableInt.intValue() > k || bl) {
                memoryAccessor2.erase();
                memoryAccessor.erase();
                livingEntity.getBrain().updateActivityFromSchedule(serverLevel.environmentAttributes(), serverLevel.getGameTime(), livingEntity.position());
                mutableInt.setValue(0);
                return true;
            }
            BlockPos blockPos = ((GlobalPos)((Object)((Object)((Object)((Object)instance.get(memoryAccessor)))))).pos();
            if (blockPos.closerThan(livingEntity.blockPosition(), j)) {
                mutableInt.increment();
            }
            return true;
        }));
    }
}

