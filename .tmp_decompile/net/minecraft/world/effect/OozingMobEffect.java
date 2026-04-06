/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

class OozingMobEffect
extends MobEffect {
    private static final int RADIUS_TO_CHECK_SLIMES = 2;
    public static final int SLIME_SIZE = 2;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected OozingMobEffect(MobEffectCategory mobEffectCategory, int i, ToIntFunction<RandomSource> toIntFunction) {
        super(mobEffectCategory, i, ParticleTypes.ITEM_SLIME);
        this.spawnedCount = toIntFunction;
    }

    @VisibleForTesting
    protected static int numberOfSlimesToSpawn(int i, NearbySlimes nearbySlimes, int j) {
        if (i < 1) {
            return j;
        }
        return Mth.clamp(0, i - nearbySlimes.count(i), j);
    }

    @Override
    public void onMobRemoved(ServerLevel serverLevel, LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
        if (removalReason != Entity.RemovalReason.KILLED) {
            return;
        }
        int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());
        int k = serverLevel.getGameRules().get(GameRules.MAX_ENTITY_CRAMMING);
        int l = OozingMobEffect.numberOfSlimesToSpawn(k, NearbySlimes.closeTo(livingEntity), j);
        for (int m = 0; m < l; ++m) {
            this.spawnSlimeOffspring(livingEntity.level(), livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
        }
    }

    private void spawnSlimeOffspring(Level level, double d, double e, double f) {
        Slime slime = EntityType.SLIME.create(level, EntitySpawnReason.TRIGGERED);
        if (slime == null) {
            return;
        }
        slime.setSize(2, true);
        slime.snapTo(d, e, f, level.getRandom().nextFloat() * 360.0f, 0.0f);
        level.addFreshEntity(slime);
    }

    @FunctionalInterface
    protected static interface NearbySlimes {
        public int count(int var1);

        public static NearbySlimes closeTo(LivingEntity livingEntity) {
            return i -> {
                ArrayList list = new ArrayList();
                livingEntity.level().getEntities(EntityType.SLIME, livingEntity.getBoundingBox().inflate(2.0), slime -> slime != livingEntity, list, i);
                return list.size();
            };
        }
    }
}

