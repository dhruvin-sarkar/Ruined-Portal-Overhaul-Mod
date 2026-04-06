/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class ResetProfession {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.JOB_SITE)).apply((Applicative)instance, memoryAccessor -> (serverLevel, villager, l) -> {
            boolean bl;
            VillagerData villagerData = villager.getVillagerData();
            boolean bl2 = bl = !villagerData.profession().is(VillagerProfession.NONE) && !villagerData.profession().is(VillagerProfession.NITWIT);
            if (bl && villager.getVillagerXp() == 0 && villagerData.level() <= 1) {
                villager.setVillagerData(villager.getVillagerData().withProfession(serverLevel.registryAccess(), VillagerProfession.NONE));
                villager.refreshBrain(serverLevel);
                return true;
            }
            return false;
        }));
    }
}

