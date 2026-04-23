package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinRavagerEntity extends Ravager implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int hardWallRoarCooldown;

    public PiglinRavagerEntity(EntityType<? extends PiglinRavagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Ravager.createAttributes()
            .add(Attributes.MAX_HEALTH, 210.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.ATTACK_DAMAGE, 24.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            RuinedPortalGeoAnimations.walkIdleController(),
            RuinedPortalGeoAnimations.deathController(),
            RuinedPortalGeoAnimations.actionController()
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float damageAmount) {
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
            damageAmount *= 0.5f;
        }
        return super.hurtServer(serverLevel, damageSource, damageAmount);
    }

    @Override
    public void aiStep() {
        // Fix: the custom Hard wall-impact roar previously played sound/effects only, so it now triggers the GeckoLib roar animation too.
        super.aiStep();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.getRoarTick() == 10) {
            for (Player target : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0))) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
            }
        }

        if (this.hardWallRoarCooldown > 0) {
            this.hardWallRoarCooldown--;
        } else if (serverLevel.getDifficulty() == Difficulty.HARD && this.hasHardWallImpact(serverLevel)) {
            this.hardWallRoarCooldown = 80;
            this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_ROAR);
            serverLevel.playSound(null, this.blockPosition(), ModSounds.ENTITY_PIGLIN_RAVAGER_ROAR, SoundSource.HOSTILE, 1.4f, 0.75f);
            for (Player target : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(7.0))) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
            }
        }

        if (this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.03) {
            BlockPos origin = this.blockPosition();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = origin.relative(direction);
                BlockState state = serverLevel.getBlockState(pos);
                if (state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE)) {
                    serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private boolean hasHardWallImpact(ServerLevel serverLevel) {
        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() <= 0.08) {
            return false;
        }

        BlockPos origin = this.blockPosition();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = origin.relative(direction);
            BlockState state = serverLevel.getBlockState(pos);
            if (state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.CRYING_OBSIDIAN)
                || state.isFaceSturdy(serverLevel, pos, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_RAVAGER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.7f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.08f;
    }
}
