/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class Slime
extends Mob
implements Enemy {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 127;
    public static final int MAX_NATURAL_SIZE = 4;
    private static final boolean DEFAULT_WAS_ON_GROUND = false;
    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround = false;

    public Slime(EntityType<? extends Slime> entityType, Level level) {
        super((EntityType<? extends Mob>)entityType, level);
        this.fixupDimensions();
        this.moveControl = new SlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new SlimeKeepOnJumpingGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, (livingEntity, serverLevel) -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 1);
    }

    @VisibleForTesting
    public void setSize(int i, boolean bl) {
        int j = Mth.clamp(i, 1, 127);
        this.entityData.set(ID_SIZE, j);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(j * j);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2f + 0.1f * (float)j);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(j);
        if (bl) {
            this.setHealth(this.getMaxHealth());
        }
        this.xpReward = j;
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Size", this.getSize() - 1);
        valueOutput.putBoolean("wasOnGround", this.wasOnGround);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setSize(valueInput.getIntOr("Size", 0) + 1, false);
        super.readAdditionalSaveData(valueInput);
        this.wasOnGround = valueInput.getBooleanOr("wasOnGround", false);
    }

    public boolean isTiny() {
        return this.getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    public void tick() {
        this.oSquish = this.squish;
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        super.tick();
        if (this.onGround() && !this.wasOnGround) {
            float f = this.getDimensions(this.getPose()).width() * 2.0f;
            float g = f / 2.0f;
            int i = 0;
            while ((float)i < f * 16.0f) {
                float h = this.random.nextFloat() * ((float)Math.PI * 2);
                float j = this.random.nextFloat() * 0.5f + 0.5f;
                float k = Mth.sin(h) * g * j;
                float l = Mth.cos(h) * g * j;
                this.level().addParticle(this.getParticleType(), this.getX() + (double)k, this.getY(), this.getZ() + (double)l, 0.0, 0.0, 0.0);
                ++i;
            }
            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            this.targetSquish = -0.5f;
        } else if (!this.onGround() && this.wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.wasOnGround = this.onGround();
        this.decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6f;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.refreshDimensions();
        this.setPos(d, e, f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ID_SIZE.equals(entityDataAccessor)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public EntityType<? extends Slime> getType() {
        return super.getType();
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        int i = this.getSize();
        if (!this.level().isClientSide() && i > 1 && this.isDeadOrDying()) {
            float f = this.getDimensions(this.getPose()).width();
            float g = f / 2.0f;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);
            PlayerTeam playerTeam = this.getTeam();
            for (int l = 0; l < k; ++l) {
                float h = ((float)(l % 2) - 0.5f) * g;
                float m = ((float)(l / 2) - 0.5f) * g;
                this.convertTo(this.getType(), new ConversionParams(ConversionType.SPLIT_ON_DEATH, false, false, playerTeam), EntitySpawnReason.TRIGGERED, slime -> {
                    slime.setSize(j, true);
                    slime.snapTo(this.getX() + (double)h, this.getY() + 0.5, this.getZ() + (double)m, this.random.nextFloat() * 360.0f, 0.0f);
                });
            }
        }
        super.remove(removalReason);
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (entity instanceof IronGolem && this.isDealsDamage()) {
            this.dealDamage((LivingEntity)entity);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.isDealsDamage()) {
            this.dealDamage(player);
        }
    }

    protected void dealDamage(LivingEntity livingEntity) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            DamageSource damageSource;
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.isAlive() && this.isWithinMeleeAttackRange(livingEntity) && this.hasLineOfSight(livingEntity) && livingEntity.hurtServer(serverLevel, damageSource = this.damageSources().mobAttack(this), this.getAttackDamage())) {
                this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return new Vec3(0.0, (double)entityDimensions.height() - 0.015625 * (double)this.getSize() * (double)f, 0.0);
    }

    protected boolean isDealsDamage() {
        return !this.isTiny() && this.isEffectiveAi();
    }

    protected float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isTiny()) {
            return SoundEvents.SLIME_HURT_SMALL;
        }
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_DEATH_SMALL;
        }
        return SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        if (this.isTiny()) {
            return SoundEvents.SLIME_SQUISH_SMALL;
        }
        return SoundEvents.SLIME_SQUISH;
    }

    public static boolean checkSlimeSpawnRules(EntityType<Slime> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        if (levelAccessor.getDifficulty() != Difficulty.PEACEFUL) {
            boolean bl;
            if (EntitySpawnReason.isSpawner(entitySpawnReason)) {
                return Slime.checkMobSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
            }
            if (levelAccessor.getBiome(blockPos).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS) && blockPos.getY() > 50 && blockPos.getY() < 70) {
                float f = levelAccessor.environmentAttributes().getValue(EnvironmentAttributes.SURFACE_SLIME_SPAWN_CHANCE, blockPos).floatValue();
                if (randomSource.nextFloat() < f && levelAccessor.getMaxLocalRawBrightness(blockPos) <= randomSource.nextInt(8)) {
                    return Slime.checkMobSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
                }
            }
            if (!(levelAccessor instanceof WorldGenLevel)) {
                return false;
            }
            ChunkPos chunkPos = new ChunkPos(blockPos);
            boolean bl2 = bl = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel)levelAccessor).getSeed(), 987234911L).nextInt(10) == 0;
            if (randomSource.nextInt(10) == 0 && bl && blockPos.getY() < 40) {
                return Slime.checkMobSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
            }
        }
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f * (float)this.getSize();
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    public void jumpFromGround() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, this.getJumpPower(), vec3.z);
        this.needsSync = true;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        int i = randomSource.nextInt(3);
        if (i < 2 && randomSource.nextFloat() < 0.5f * difficultyInstance.getSpecialMultiplier()) {
            ++i;
        }
        int j = 1 << i;
        this.setSize(j, true);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    float getSoundPitch() {
        float f = this.isTiny() ? 1.4f : 0.8f;
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * f;
    }

    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getSize());
    }

    static class SlimeMoveControl
    extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final Slime slime;
        private boolean isAggressive;

        public SlimeMoveControl(Slime slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0f * slime.getYRot() / (float)Math.PI;
        }

        public void setDirection(float f, boolean bl) {
            this.yRot = f;
            this.isAggressive = bl;
        }

        public void setWantedMovement(double d) {
            this.speedModifier = d;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0f));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
                return;
            }
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.onGround()) {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpControl().jump();
                    if (this.slime.doPlayJumpSound()) {
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                    }
                } else {
                    this.slime.xxa = 0.0f;
                    this.slime.zza = 0.0f;
                    this.mob.setSpeed(0.0f);
                }
            } else {
                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
        }
    }

    static class SlimeFloatGoal
    extends Goal {
        private final Slime slime;

        public SlimeFloatGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setWantedMovement(1.2);
            }
        }
    }

    static class SlimeAttackGoal
    extends Goal {
        private final Slime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!this.slime.canAttack(livingEntity)) {
                return false;
            }
            return this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void start() {
            this.growTiredTimer = SlimeAttackGoal.reducedTickDelay(300);
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!this.slime.canAttack(livingEntity)) {
                return false;
            }
            return --this.growTiredTimer > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            LivingEntity livingEntity = this.slime.getTarget();
            if (livingEntity != null) {
                this.slime.lookAt(livingEntity, 10.0f, 10.0f);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
            }
        }
    }

    static class SlimeRandomDirectionGoal
    extends Goal {
        private final Slime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            MoveControl moveControl;
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            if ((moveControl = this.slime.getMoveControl()) instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setDirection(this.chosenDegrees, false);
            }
        }
    }

    static class SlimeKeepOnJumpingGoal
    extends Goal {
        private final Slime slime;

        public SlimeKeepOnJumpingGoal(Slime slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof SlimeMoveControl) {
                SlimeMoveControl slimeMoveControl = (SlimeMoveControl)moveControl;
                slimeMoveControl.setWantedMovement(1.0);
            }
        }
    }
}

