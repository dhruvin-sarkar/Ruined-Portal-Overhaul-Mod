/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ambient;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Bat
extends AmbientCreature {
    public static final float FLAP_LENGTH_SECONDS = 0.5f;
    public static final float TICKS_PER_FLAP = 10.0f;
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Bat.class, EntityDataSerializers.BYTE);
    private static final int FLAG_RESTING = 1;
    private static final TargetingConditions BAT_RESTING_TARGETING = TargetingConditions.forNonCombat().range(4.0);
    private static final byte DEFAULT_FLAGS = 0;
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();
    private @Nullable BlockPos targetPosition;

    public Bat(EntityType<? extends Bat> entityType, Level level) {
        super((EntityType<? extends AmbientCreature>)entityType, level);
        if (!level.isClientSide()) {
            this.setResting(true);
        }
    }

    @Override
    public boolean isFlapping() {
        return !this.isResting() && (float)this.tickCount % 10.0f == 0.0f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_FLAGS, (byte)0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound() {
        if (this.isResting() && this.random.nextInt(4) != 0) {
            return null;
        }
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0);
    }

    public boolean isResting() {
        return (this.entityData.get(DATA_ID_FLAGS) & 1) != 0;
    }

    public void setResting(boolean bl) {
        byte b = this.entityData.get(DATA_ID_FLAGS);
        if (bl) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b | 1));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b & 0xFFFFFFFE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isResting()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), (double)Mth.floor(this.getY()) + 1.0 - (double)this.getBbHeight(), this.getZ());
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        this.setupAnimationStates();
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        BlockPos blockPos = this.blockPosition();
        BlockPos blockPos2 = blockPos.above();
        if (this.isResting()) {
            boolean bl = this.isSilent();
            if (serverLevel.getBlockState(blockPos2).isRedstoneConductor(serverLevel, blockPos)) {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = this.random.nextInt(360);
                }
                if (serverLevel.getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
                    this.setResting(false);
                    if (!bl) {
                        serverLevel.levelEvent(null, 1025, blockPos, 0);
                    }
                }
            } else {
                this.setResting(false);
                if (!bl) {
                    serverLevel.levelEvent(null, 1025, blockPos, 0);
                }
            }
        } else {
            if (!(this.targetPosition == null || serverLevel.isEmptyBlock(this.targetPosition) && this.targetPosition.getY() > serverLevel.getMinY())) {
                this.targetPosition = null;
            }
            if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0)) {
                this.targetPosition = BlockPos.containing(this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7), this.getY() + (double)this.random.nextInt(6) - 2.0, this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
            }
            double d = (double)this.targetPosition.getX() + 0.5 - this.getX();
            double e = (double)this.targetPosition.getY() + 0.1 - this.getY();
            double f = (double)this.targetPosition.getZ() + 0.5 - this.getZ();
            Vec3 vec3 = this.getDeltaMovement();
            Vec3 vec32 = vec3.add((Math.signum(d) * 0.5 - vec3.x) * (double)0.1f, (Math.signum(e) * (double)0.7f - vec3.y) * (double)0.1f, (Math.signum(f) * 0.5 - vec3.z) * (double)0.1f);
            this.setDeltaMovement(vec32);
            float g = (float)(Mth.atan2(vec32.z, vec32.x) * 57.2957763671875) - 90.0f;
            float h = Mth.wrapDegrees(g - this.getYRot());
            this.zza = 0.5f;
            this.setYRot(this.getYRot() + h);
            if (this.random.nextInt(100) == 0 && serverLevel.getBlockState(blockPos2).isRedstoneConductor(serverLevel, blockPos2)) {
                this.setResting(true);
            }
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        if (this.isResting()) {
            this.setResting(false);
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.entityData.set(DATA_ID_FLAGS, valueInput.getByteOr("BatFlags", (byte)0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putByte("BatFlags", this.entityData.get(DATA_ID_FLAGS));
    }

    public static boolean checkBatSpawnRules(EntityType<Bat> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        if (blockPos.getY() >= levelAccessor.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY()) {
            return false;
        }
        if (randomSource.nextBoolean()) {
            return false;
        }
        if (levelAccessor.getMaxLocalRawBrightness(blockPos) > randomSource.nextInt(4)) {
            return false;
        }
        if (!levelAccessor.getBlockState(blockPos.below()).is(BlockTags.BATS_SPAWNABLE_ON)) {
            return false;
        }
        return Bat.checkMobSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
    }

    private void setupAnimationStates() {
        if (this.isResting()) {
            this.flyAnimationState.stop();
            this.restAnimationState.startIfStopped(this.tickCount);
        } else {
            this.restAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        }
    }
}

