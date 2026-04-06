/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public interface ServerEntityGetter
extends EntityGetter {
    public ServerLevel getLevel();

    default public @Nullable Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity) {
        return this.getNearestEntity(this.players(), targetingConditions, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
    }

    default public @Nullable Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity, double d, double e, double f) {
        return this.getNearestEntity(this.players(), targetingConditions, livingEntity, d, e, f);
    }

    default public @Nullable Player getNearestPlayer(TargetingConditions targetingConditions, double d, double e, double f) {
        return this.getNearestEntity(this.players(), targetingConditions, null, d, e, f);
    }

    default public <T extends LivingEntity> @Nullable T getNearestEntity(Class<? extends T> class_, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity2, double d, double e, double f, AABB aABB) {
        return (T)this.getNearestEntity(this.getEntitiesOfClass(class_, aABB, livingEntity -> true), targetingConditions, livingEntity2, d, e, f);
    }

    default public @Nullable LivingEntity getNearestEntity(TagKey<EntityType<?>> tagKey, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity2, double d, double e, double f, AABB aABB) {
        double g = Double.MAX_VALUE;
        LivingEntity livingEntity22 = null;
        for (LivingEntity livingEntity3 : this.getEntitiesOfClass(LivingEntity.class, aABB, livingEntity -> livingEntity.getType().is(tagKey))) {
            double h;
            if (!targetingConditions.test(this.getLevel(), livingEntity2, livingEntity3) || !((h = livingEntity3.distanceToSqr(d, e, f)) < g)) continue;
            g = h;
            livingEntity22 = livingEntity3;
        }
        return livingEntity22;
    }

    default public <T extends LivingEntity> @Nullable T getNearestEntity(List<? extends T> list, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f) {
        double g = -1.0;
        LivingEntity livingEntity2 = null;
        for (LivingEntity livingEntity3 : list) {
            if (!targetingConditions.test(this.getLevel(), livingEntity, livingEntity3)) continue;
            double h = livingEntity3.distanceToSqr(d, e, f);
            if (g != -1.0 && !(h < g)) continue;
            g = h;
            livingEntity2 = livingEntity3;
        }
        return (T)livingEntity2;
    }

    default public List<Player> getNearbyPlayers(TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aABB) {
        ArrayList<Player> list = new ArrayList<Player>();
        for (Player player : this.players()) {
            if (!aABB.contains(player.getX(), player.getY(), player.getZ()) || !targetingConditions.test(this.getLevel(), livingEntity, player)) continue;
            list.add(player);
        }
        return list;
    }

    default public <T extends LivingEntity> List<T> getNearbyEntities(Class<T> class_, TargetingConditions targetingConditions, LivingEntity livingEntity2, AABB aABB) {
        List<LivingEntity> list = this.getEntitiesOfClass(class_, aABB, livingEntity -> true);
        ArrayList<LivingEntity> list2 = new ArrayList<LivingEntity>();
        for (LivingEntity livingEntity22 : list) {
            if (!targetingConditions.test(this.getLevel(), livingEntity2, livingEntity22)) continue;
            list2.add(livingEntity22);
        }
        return list2;
    }
}

