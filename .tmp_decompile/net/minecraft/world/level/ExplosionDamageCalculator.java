/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class ExplosionDamageCalculator {
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.isAir() && fluidState.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Float.valueOf(Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance())));
    }

    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        return true;
    }

    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return true;
    }

    public float getKnockbackMultiplier(Entity entity) {
        return 1.0f;
    }

    public float getEntityDamageAmount(Explosion explosion, Entity entity, float f) {
        float g = explosion.radius() * 2.0f;
        Vec3 vec3 = explosion.center();
        double d = Math.sqrt(entity.distanceToSqr(vec3)) / (double)g;
        double e = (1.0 - d) * (double)f;
        return (float)((e * e + e) / 2.0 * 7.0 * (double)g + 1.0);
    }
}

