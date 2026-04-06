/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Phantom
extends Mob
implements Enemy {
    public static final float FLAP_DEGREES_PER_TICK = 7.448451f;
    public static final int TICKS_PER_FLAP = Mth.ceil(24.166098f);
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Phantom.class, EntityDataSerializers.INT);
    Vec3 moveTargetPoint = Vec3.ZERO;
    @Nullable BlockPos anchorPoint;
    AttackPhase attackPhase = AttackPhase.CIRCLE;

    public Phantom(EntityType<? extends Phantom> entityType, Level level) {
        super((EntityType<? extends Mob>)entityType, level);
        this.xpReward = 5;
        this.moveControl = new PhantomMoveControl(this);
        this.lookControl = new PhantomLookControl(this);
    }

    @Override
    public boolean isFlapping() {
        return (this.getUniqueFlapTickOffset() + this.tickCount) % TICKS_PER_FLAP == 0;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new PhantomBodyRotationControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PhantomAttackStrategyGoal());
        this.goalSelector.addGoal(2, new PhantomSweepAttackGoal());
        this.goalSelector.addGoal(3, new PhantomCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new PhantomAttackPlayerTargetGoal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 0);
    }

    public void setPhantomSize(int i) {
        this.entityData.set(ID_SIZE, Mth.clamp(i, 0, 64));
    }

    private void updatePhantomSizeInfo() {
        this.refreshDimensions();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6 + this.getPhantomSize());
    }

    public int getPhantomSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ID_SIZE.equals(entityDataAccessor)) {
            this.updatePhantomSizeInfo();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            float f = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            float g = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount + 1) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            if (f > 0.0f && g <= 0.0f) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, this.getSoundSource(), 0.95f + this.random.nextFloat() * 0.05f, 0.95f + this.random.nextFloat() * 0.05f, false);
            }
            float h = this.getBbWidth() * 1.48f;
            float i = Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * h;
            float j = Mth.sin(this.getYRot() * ((float)Math.PI / 180)) * h;
            float k = (0.3f + f * 0.45f) * this.getBbHeight() * 2.5f;
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)i, this.getY() + (double)k, this.getZ() + (double)j, 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)i, this.getY() + (double)k, this.getZ() - (double)j, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 vec3) {
        this.travelFlying(vec3, 0.2f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.anchorPoint = this.blockPosition().above(5);
        this.setPhantomSize(0);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.anchorPoint = valueInput.read("anchor_pos", BlockPos.CODEC).orElse(null);
        this.setPhantomSize(valueInput.getIntOr("size", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.storeNullable("anchor_pos", BlockPos.CODEC, this.anchorPoint);
        valueOutput.putInt("size", this.getPhantomSize());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        int i = this.getPhantomSize();
        EntityDimensions entityDimensions = super.getDefaultDimensions(pose);
        return entityDimensions.scale(1.0f + 0.15f * (float)i);
    }

    boolean canAttack(ServerLevel serverLevel, LivingEntity livingEntity, TargetingConditions targetingConditions) {
        return targetingConditions.test(serverLevel, this, livingEntity);
    }

    static enum AttackPhase {
        CIRCLE,
        SWOOP;

    }

    class PhantomMoveControl
    extends MoveControl {
        private float speed;

        public PhantomMoveControl(Mob mob) {
            super(mob);
            this.speed = 0.1f;
        }

        @Override
        public void tick() {
            if (Phantom.this.horizontalCollision) {
                Phantom.this.setYRot(Phantom.this.getYRot() + 180.0f);
                this.speed = 0.1f;
            }
            double d = Phantom.this.moveTargetPoint.x - Phantom.this.getX();
            double e = Phantom.this.moveTargetPoint.y - Phantom.this.getY();
            double f = Phantom.this.moveTargetPoint.z - Phantom.this.getZ();
            double g = Math.sqrt(d * d + f * f);
            if (Math.abs(g) > (double)1.0E-5f) {
                double h = 1.0 - Math.abs(e * (double)0.7f) / g;
                g = Math.sqrt((d *= h) * d + (f *= h) * f);
                double i = Math.sqrt(d * d + f * f + e * e);
                float j = Phantom.this.getYRot();
                float k = (float)Mth.atan2(f, d);
                float l = Mth.wrapDegrees(Phantom.this.getYRot() + 90.0f);
                float m = Mth.wrapDegrees(k * 57.295776f);
                Phantom.this.setYRot(Mth.approachDegrees(l, m, 4.0f) - 90.0f);
                Phantom.this.yBodyRot = Phantom.this.getYRot();
                this.speed = Mth.degreesDifferenceAbs(j, Phantom.this.getYRot()) < 3.0f ? Mth.approach(this.speed, 1.8f, 0.005f * (1.8f / this.speed)) : Mth.approach(this.speed, 0.2f, 0.025f);
                float n = (float)(-(Mth.atan2(-e, g) * 57.2957763671875));
                Phantom.this.setXRot(n);
                float o = Phantom.this.getYRot() + 90.0f;
                double p = (double)(this.speed * Mth.cos(o * ((float)Math.PI / 180))) * Math.abs(d / i);
                double q = (double)(this.speed * Mth.sin(o * ((float)Math.PI / 180))) * Math.abs(f / i);
                double r = (double)(this.speed * Mth.sin(n * ((float)Math.PI / 180))) * Math.abs(e / i);
                Vec3 vec3 = Phantom.this.getDeltaMovement();
                Phantom.this.setDeltaMovement(vec3.add(new Vec3(p, r, q).subtract(vec3).scale(0.2)));
            }
        }
    }

    static class PhantomLookControl
    extends LookControl {
        public PhantomLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }

    class PhantomBodyRotationControl
    extends BodyRotationControl {
        public PhantomBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {
            Phantom.this.yHeadRot = Phantom.this.yBodyRot;
            Phantom.this.yBodyRot = Phantom.this.getYRot();
        }
    }

    class PhantomAttackStrategyGoal
    extends Goal {
        private int nextSweepTick;

        PhantomAttackStrategyGoal() {
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity != null) {
                return Phantom.this.canAttack(PhantomAttackStrategyGoal.getServerLevel(Phantom.this.level()), livingEntity, TargetingConditions.DEFAULT);
            }
            return false;
        }

        @Override
        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            Phantom.this.attackPhase = AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        @Override
        public void stop() {
            if (Phantom.this.anchorPoint != null) {
                Phantom.this.anchorPoint = Phantom.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, Phantom.this.anchorPoint).above(10 + Phantom.this.random.nextInt(20));
            }
        }

        @Override
        public void tick() {
            if (Phantom.this.attackPhase == AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    Phantom.this.attackPhase = AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + Phantom.this.random.nextInt(4)) * 20);
                    Phantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0f, 0.95f + Phantom.this.random.nextFloat() * 0.1f);
                }
            }
        }

        private void setAnchorAboveTarget() {
            if (Phantom.this.anchorPoint == null) {
                return;
            }
            Phantom.this.anchorPoint = Phantom.this.getTarget().blockPosition().above(20 + Phantom.this.random.nextInt(20));
            if (Phantom.this.anchorPoint.getY() < Phantom.this.level().getSeaLevel()) {
                Phantom.this.anchorPoint = new BlockPos(Phantom.this.anchorPoint.getX(), Phantom.this.level().getSeaLevel() + 1, Phantom.this.anchorPoint.getZ());
            }
        }
    }

    class PhantomSweepAttackGoal
    extends PhantomMoveTargetGoal {
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        PhantomSweepAttackGoal() {
        }

        @Override
        public boolean canUse() {
            return Phantom.this.getTarget() != null && Phantom.this.attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!livingEntity.isAlive()) {
                return false;
            }
            if (livingEntity instanceof Player) {
                Player player = (Player)livingEntity;
                if (livingEntity.isSpectator() || player.isCreative()) {
                    return false;
                }
            }
            if (!this.canUse()) {
                return false;
            }
            if (Phantom.this.tickCount > this.catSearchTick) {
                this.catSearchTick = Phantom.this.tickCount + 20;
                List<Entity> list = Phantom.this.level().getEntitiesOfClass(Cat.class, Phantom.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
                for (Cat cat : list) {
                    cat.hiss();
                }
                this.isScaredOfCat = !list.isEmpty();
            }
            return !this.isScaredOfCat;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
            Phantom.this.setTarget(null);
            Phantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            Phantom.this.moveTargetPoint = new Vec3(livingEntity.getX(), livingEntity.getY(0.5), livingEntity.getZ());
            if (Phantom.this.getBoundingBox().inflate(0.2f).intersects(livingEntity.getBoundingBox())) {
                Phantom.this.doHurtTarget(PhantomSweepAttackGoal.getServerLevel(Phantom.this.level()), livingEntity);
                Phantom.this.attackPhase = AttackPhase.CIRCLE;
                if (!Phantom.this.isSilent()) {
                    Phantom.this.level().levelEvent(1039, Phantom.this.blockPosition(), 0);
                }
            } else if (Phantom.this.horizontalCollision || Phantom.this.hurtTime > 0) {
                Phantom.this.attackPhase = AttackPhase.CIRCLE;
            }
        }
    }

    class PhantomCircleAroundAnchorGoal
    extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        PhantomCircleAroundAnchorGoal() {
        }

        @Override
        public boolean canUse() {
            return Phantom.this.getTarget() == null || Phantom.this.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.distance = 5.0f + Phantom.this.random.nextFloat() * 10.0f;
            this.height = -4.0f + Phantom.this.random.nextFloat() * 9.0f;
            this.clockwise = Phantom.this.random.nextBoolean() ? 1.0f : -1.0f;
            this.selectNext();
        }

        @Override
        public void tick() {
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0f + Phantom.this.random.nextFloat() * 9.0f;
            }
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                this.distance += 1.0f;
                if (this.distance > 15.0f) {
                    this.distance = 5.0f;
                    this.clockwise = -this.clockwise;
                }
            }
            if (Phantom.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = Phantom.this.random.nextFloat() * 2.0f * (float)Math.PI;
                this.selectNext();
            }
            if (this.touchingTarget()) {
                this.selectNext();
            }
            if (Phantom.this.moveTargetPoint.y < Phantom.this.getY() && !Phantom.this.level().isEmptyBlock(Phantom.this.blockPosition().below(1))) {
                this.height = Math.max(1.0f, this.height);
                this.selectNext();
            }
            if (Phantom.this.moveTargetPoint.y > Phantom.this.getY() && !Phantom.this.level().isEmptyBlock(Phantom.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0f, this.height);
                this.selectNext();
            }
        }

        private void selectNext() {
            if (Phantom.this.anchorPoint == null) {
                Phantom.this.anchorPoint = Phantom.this.blockPosition();
            }
            this.angle += this.clockwise * 15.0f * ((float)Math.PI / 180);
            Phantom.this.moveTargetPoint = Vec3.atLowerCornerOf(Phantom.this.anchorPoint).add(this.distance * Mth.cos(this.angle), -4.0f + this.height, this.distance * Mth.sin(this.angle));
        }
    }

    class PhantomAttackPlayerTargetGoal
    extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);
        private int nextScanTick = PhantomAttackPlayerTargetGoal.reducedTickDelay(20);

        PhantomAttackPlayerTargetGoal() {
        }

        @Override
        public boolean canUse() {
            if (this.nextScanTick > 0) {
                --this.nextScanTick;
                return false;
            }
            this.nextScanTick = PhantomAttackPlayerTargetGoal.reducedTickDelay(60);
            ServerLevel serverLevel = PhantomAttackPlayerTargetGoal.getServerLevel(Phantom.this.level());
            List<Player> list = serverLevel.getNearbyPlayers(this.attackTargeting, Phantom.this, Phantom.this.getBoundingBox().inflate(16.0, 64.0, 16.0));
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(Entity::getY).reversed());
                for (Player player : list) {
                    if (!Phantom.this.canAttack(serverLevel, player, TargetingConditions.DEFAULT)) continue;
                    Phantom.this.setTarget(player);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = Phantom.this.getTarget();
            if (livingEntity != null) {
                return Phantom.this.canAttack(PhantomAttackPlayerTargetGoal.getServerLevel(Phantom.this.level()), livingEntity, TargetingConditions.DEFAULT);
            }
            return false;
        }
    }

    abstract class PhantomMoveTargetGoal
    extends Goal {
        public PhantomMoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return Phantom.this.moveTargetPoint.distanceToSqr(Phantom.this.getX(), Phantom.this.getY(), Phantom.this.getZ()) < 4.0;
        }
    }
}

