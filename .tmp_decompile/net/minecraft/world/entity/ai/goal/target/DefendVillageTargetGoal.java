/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class DefendVillageTargetGoal
extends TargetGoal {
    private final IronGolem golem;
    private @Nullable LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);

    public DefendVillageTargetGoal(IronGolem ironGolem) {
        super(ironGolem, false, true);
        this.golem = ironGolem;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        Player player2;
        AABB aABB = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
        ServerLevel serverLevel = DefendVillageTargetGoal.getServerLevel(this.golem);
        List<Villager> list = serverLevel.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, aABB);
        List<Player> list2 = serverLevel.getNearbyPlayers(this.attackTargeting, this.golem, aABB);
        for (LivingEntity livingEntity : list) {
            Villager villager = (Villager)livingEntity;
            for (Player player : list2) {
                int i = villager.getPlayerReputation(player);
                if (i > -100) continue;
                this.potentialTarget = player;
            }
        }
        if (this.potentialTarget == null) {
            return false;
        }
        LivingEntity livingEntity = this.potentialTarget;
        return !(livingEntity instanceof Player) || !(player2 = (Player)livingEntity).isSpectator() && !player2.isCreative();
    }

    @Override
    public void start() {
        this.golem.setTarget(this.potentialTarget);
        super.start();
    }
}

