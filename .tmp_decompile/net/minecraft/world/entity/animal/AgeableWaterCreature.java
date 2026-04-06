/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class AgeableWaterCreature
extends AgeableMob {
    protected AgeableWaterCreature(EntityType<? extends AgeableWaterCreature> entityType, Level level) {
        super((EntityType<? extends AgeableMob>)entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public int getBaseExperienceReward(ServerLevel serverLevel) {
        return 1 + this.random.nextInt(3);
    }

    protected void handleAirSupply(int i) {
        if (this.isAlive() && !this.isInWater()) {
            this.setAirSupply(i - 1);
            if (this.shouldTakeDrowningDamage()) {
                this.setAirSupply(0);
                this.hurt(this.damageSources().drown(), 2.0f);
            }
        } else {
            this.setAirSupply(300);
        }
    }

    @Override
    public void baseTick() {
        int i = this.getAirSupply();
        super.baseTick();
        this.handleAirSupply(i);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public static boolean checkSurfaceAgeableWaterCreatureSpawnRules(EntityType<? extends AgeableWaterCreature> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        int i = levelAccessor.getSeaLevel();
        int j = i - 13;
        return blockPos.getY() >= j && blockPos.getY() <= i && levelAccessor.getFluidState(blockPos.below()).is(FluidTags.WATER) && levelAccessor.getBlockState(blockPos.above()).is(Blocks.WATER);
    }
}

