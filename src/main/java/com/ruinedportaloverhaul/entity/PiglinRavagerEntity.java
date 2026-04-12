package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinRavagerEntity extends Ravager {
    public PiglinRavagerEntity(EntityType<? extends PiglinRavagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Ravager.createAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 15.0);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource damageSource, float damageAmount) {
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
            damageAmount *= 0.5f;
        }
        return super.hurtServer(serverLevel, damageSource, damageAmount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.getRoarTick() == 10) {
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0))) {
                if (target != this) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this);
                }
            }
        }

        if (this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.03) {
            BlockPos origin = this.blockPosition();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = origin.relative(direction);
                BlockState state = serverLevel.getBlockState(pos);
                if (state.is(Blocks.NETHERRACK) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                    serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
        net.minecraft.world.level.ServerLevelAccessor levelAccessor,
        DifficultyInstance difficultyInstance,
        EntitySpawnReason spawnReason,
        net.minecraft.world.entity.SpawnGroupData spawnGroupData
    ) {
        net.minecraft.world.entity.SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficultyInstance, spawnReason, spawnGroupData);
        if (levelAccessor instanceof ServerLevel serverLevel) {
            Vindicator rider = ModEntities.PIGLIN_VINDICATOR.spawn(
                serverLevel,
                this.blockPosition().above(),
                EntitySpawnReason.MOB_SUMMONED
            );
            if (rider != null) {
                rider.startRiding(this, true);
            }
        }
        return data;
    }
}
