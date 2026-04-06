/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SkeletonHorse
extends AbstractHorse {
    private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
    private static final int TRAP_MAX_LIFE = 18000;
    private static final boolean DEFAULT_IS_TRAP = false;
    private static final int DEFAULT_TRAP_TIME = 0;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.SKELETON_HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.SKELETON_HORSE.getHeight() - 0.03125f, 0.0f)).scale(0.5f);
    private boolean isTrap = false;
    private int trapTime = 0;

    public SkeletonHorse(EntityType<? extends SkeletonHorse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SkeletonHorse.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2f);
    }

    public static boolean checkSkeletonHorseSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        if (EntitySpawnReason.isSpawner(entitySpawnReason)) {
            return EntitySpawnReason.ignoresLightRequirements(entitySpawnReason) || SkeletonHorse.isBrightEnoughToSpawn(levelAccessor, blockPos);
        }
        return Animal.checkAnimalSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(SkeletonHorse.generateJumpStrength(randomSource::nextDouble));
    }

    @Override
    protected void addBehaviourGoals() {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isEyeInFluid(FluidTags.WATER)) {
            return SoundEvents.SKELETON_HORSE_AMBIENT_WATER;
        }
        return SoundEvents.SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HORSE_HURT;
    }

    @Override
    protected SoundEvent getSwimSound() {
        if (this.onGround()) {
            if (this.isVehicle()) {
                ++this.gallopSoundCounter;
                if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                    return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
                }
                if (this.gallopSoundCounter <= 5) {
                    return SoundEvents.SKELETON_HORSE_STEP_WATER;
                }
            } else {
                return SoundEvents.SKELETON_HORSE_STEP_WATER;
            }
        }
        return SoundEvents.SKELETON_HORSE_SWIM;
    }

    @Override
    protected void playSwimSound(float f) {
        if (this.onGround()) {
            super.playSwimSound(0.3f);
        } else {
            super.playSwimSound(Math.min(0.1f, f * 25.0f));
        }
    }

    @Override
    protected void playJumpSound() {
        if (this.isInWater()) {
            this.playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4f, 1.0f);
        } else {
            super.playJumpSound();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isTrap() && this.trapTime++ >= 18000) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("SkeletonTrap", this.isTrap());
        valueOutput.putInt("SkeletonTrapTime", this.trapTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setTrap(valueInput.getBooleanOr("SkeletonTrap", false));
        this.trapTime = valueInput.getIntOr("SkeletonTrapTime", 0);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.96f;
    }

    public boolean isTrap() {
        return this.isTrap;
    }

    public void setTrap(boolean bl) {
        if (bl == this.isTrap) {
            return;
        }
        this.isTrap = bl;
        if (bl) {
            this.goalSelector.addGoal(1, this.skeletonTrapGoal);
        } else {
            this.goalSelector.removeGoal(this.skeletonTrapGoal);
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.SKELETON_HORSE.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (!this.isTamed()) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }
}

