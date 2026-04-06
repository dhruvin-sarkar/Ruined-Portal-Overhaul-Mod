/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class Shulker
extends AbstractGolem
implements Enemy {
    private static final Identifier COVERED_ARMOR_MODIFIER_ID = Identifier.withDefaultNamespace("covered");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE);
    protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
    protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    private static final int TELEPORT_STEPS = 6;
    private static final byte NO_COLOR = 16;
    private static final byte DEFAULT_COLOR = 16;
    private static final int MAX_TELEPORT_DISTANCE = 8;
    private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
    private static final int OTHER_SHULKER_LIMIT = 5;
    private static final float PEEK_PER_TICK = 0.05f;
    private static final byte DEFAULT_PEEK = 0;
    private static final Direction DEFAULT_ATTACH_FACE = Direction.DOWN;
    static final Vector3f FORWARD = Util.make(() -> {
        Vec3i vec3i = Direction.SOUTH.getUnitVec3i();
        return new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    });
    private static final float MAX_SCALE = 3.0f;
    private float currentPeekAmountO;
    private float currentPeekAmount;
    private @Nullable BlockPos clientOldAttachPosition;
    private int clientSideTeleportInterpolation;
    private static final float MAX_LID_OPEN = 1.0f;

    public Shulker(EntityType<? extends Shulker> entityType, Level level) {
        super((EntityType<? extends AbstractGolem>)entityType, level);
        this.xpReward = 5;
        this.lookControl = new ShulkerLookControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(4, new ShulkerAttackGoal());
        this.goalSelector.addGoal(7, new ShulkerPeekGoal());
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new ShulkerNearestAttackGoal(this));
        this.targetSelector.addGoal(3, new ShulkerDefenseAttackGoal(this));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHULKER_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHULKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isClosed()) {
            return SoundEvents.SHULKER_HURT_CLOSED;
        }
        return SoundEvents.SHULKER_HURT;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACH_FACE_ID, DEFAULT_ATTACH_FACE);
        builder.define(DATA_PEEK_ID, (byte)0);
        builder.define(DATA_COLOR_ID, (byte)16);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new ShulkerBodyRotationControl(this);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setAttachFace(valueInput.read("AttachFace", Direction.LEGACY_ID_CODEC).orElse(DEFAULT_ATTACH_FACE));
        this.entityData.set(DATA_PEEK_ID, valueInput.getByteOr("Peek", (byte)0));
        this.entityData.set(DATA_COLOR_ID, valueInput.getByteOr("Color", (byte)16));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("AttachFace", Direction.LEGACY_ID_CODEC, this.getAttachFace());
        valueOutput.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
        valueOutput.putByte("Color", this.entityData.get(DATA_COLOR_ID));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level().isClientSide() || this.isPassenger() || this.canStayAt(this.blockPosition(), this.getAttachFace()))) {
            this.findNewAttachment();
        }
        if (this.updatePeekAmount()) {
            this.onPeekAmountChange();
        }
        if (this.level().isClientSide()) {
            if (this.clientSideTeleportInterpolation > 0) {
                --this.clientSideTeleportInterpolation;
            } else {
                this.clientOldAttachPosition = null;
            }
        }
    }

    private void findNewAttachment() {
        Direction direction = this.findAttachableSurface(this.blockPosition());
        if (direction != null) {
            this.setAttachFace(direction);
        } else {
            this.teleportSomewhere();
        }
    }

    @Override
    protected AABB makeBoundingBox(Vec3 vec3) {
        float f = Shulker.getPhysicalPeek(this.currentPeekAmount);
        Direction direction = this.getAttachFace().getOpposite();
        return Shulker.getProgressAabb(this.getScale(), direction, f, vec3);
    }

    private static float getPhysicalPeek(float f) {
        return 0.5f - Mth.sin((0.5f + f) * (float)Math.PI) * 0.5f;
    }

    private boolean updatePeekAmount() {
        this.currentPeekAmountO = this.currentPeekAmount;
        float f = (float)this.getRawPeekAmount() * 0.01f;
        if (this.currentPeekAmount == f) {
            return false;
        }
        this.currentPeekAmount = this.currentPeekAmount > f ? Mth.clamp(this.currentPeekAmount - 0.05f, f, 1.0f) : Mth.clamp(this.currentPeekAmount + 0.05f, 0.0f, f);
        return true;
    }

    private void onPeekAmountChange() {
        this.reapplyPosition();
        float f = Shulker.getPhysicalPeek(this.currentPeekAmount);
        float g = Shulker.getPhysicalPeek(this.currentPeekAmountO);
        Direction direction = this.getAttachFace().getOpposite();
        float h = (f - g) * this.getScale();
        if (h <= 0.0f) {
            return;
        }
        List<Entity> list = this.level().getEntities(this, Shulker.getProgressDeltaAabb(this.getScale(), direction, g, f, this.position()), EntitySelector.NO_SPECTATORS.and(entity -> !entity.isPassengerOfSameVehicle(this)));
        for (Entity entity2 : list) {
            if (entity2 instanceof Shulker || entity2.noPhysics) continue;
            entity2.move(MoverType.SHULKER, new Vec3(h * (float)direction.getStepX(), h * (float)direction.getStepY(), h * (float)direction.getStepZ()));
        }
    }

    public static AABB getProgressAabb(float f, Direction direction, float g, Vec3 vec3) {
        return Shulker.getProgressDeltaAabb(f, direction, -1.0f, g, vec3);
    }

    public static AABB getProgressDeltaAabb(float f, Direction direction, float g, float h, Vec3 vec3) {
        AABB aABB = new AABB((double)(-f) * 0.5, 0.0, (double)(-f) * 0.5, (double)f * 0.5, f, (double)f * 0.5);
        double d = Math.max(g, h);
        double e = Math.min(g, h);
        AABB aABB2 = aABB.expandTowards((double)direction.getStepX() * d * (double)f, (double)direction.getStepY() * d * (double)f, (double)direction.getStepZ() * d * (double)f).contract((double)(-direction.getStepX()) * (1.0 + e) * (double)f, (double)(-direction.getStepY()) * (1.0 + e) * (double)f, (double)(-direction.getStepZ()) * (1.0 + e) * (double)f);
        return aABB2.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl, boolean bl2) {
        if (this.level().isClientSide()) {
            this.clientOldAttachPosition = null;
            this.clientSideTeleportInterpolation = 0;
        }
        this.setAttachFace(Direction.DOWN);
        return super.startRiding(entity, bl, bl2);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (this.level().isClientSide()) {
            this.clientOldAttachPosition = this.blockPosition();
        }
        this.yBodyRotO = 0.0f;
        this.yBodyRot = 0.0f;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setYRot(0.0f);
        this.yHeadRot = this.getYRot();
        this.setOldPosAndRot();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (moverType == MoverType.SHULKER_BOX) {
            this.teleportSomewhere();
        } else {
            super.move(moverType, vec3);
        }
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 vec3) {
    }

    @Override
    public void setPos(double d, double e, double f) {
        BlockPos blockPos = this.blockPosition();
        if (this.isPassenger()) {
            super.setPos(d, e, f);
        } else {
            super.setPos((double)Mth.floor(d) + 0.5, Mth.floor(e + 0.5), (double)Mth.floor(f) + 0.5);
        }
        if (this.tickCount == 0) {
            return;
        }
        BlockPos blockPos2 = this.blockPosition();
        if (!blockPos2.equals(blockPos)) {
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.needsSync = true;
            if (this.level().isClientSide() && !this.isPassenger() && !blockPos2.equals(this.clientOldAttachPosition)) {
                this.clientOldAttachPosition = blockPos;
                this.clientSideTeleportInterpolation = 6;
                this.xOld = this.getX();
                this.yOld = this.getY();
                this.zOld = this.getZ();
            }
        }
    }

    protected @Nullable Direction findAttachableSurface(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (!this.canStayAt(blockPos, direction)) continue;
            return direction;
        }
        return null;
    }

    boolean canStayAt(BlockPos blockPos, Direction direction) {
        if (this.isPositionBlocked(blockPos)) {
            return false;
        }
        Direction direction2 = direction.getOpposite();
        if (!this.level().loadedAndEntityCanStandOnFace(blockPos.relative(direction), this, direction2)) {
            return false;
        }
        AABB aABB = Shulker.getProgressAabb(this.getScale(), direction2, 1.0f, blockPos.getBottomCenter()).deflate(1.0E-6);
        return this.level().noCollision(this, aABB);
    }

    private boolean isPositionBlocked(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        boolean bl = blockState.is(Blocks.MOVING_PISTON) && blockPos.equals(this.blockPosition());
        return !bl;
    }

    protected boolean teleportSomewhere() {
        if (this.isNoAi() || !this.isAlive()) {
            return false;
        }
        BlockPos blockPos = this.blockPosition();
        for (int i = 0; i < 5; ++i) {
            Direction direction;
            BlockPos blockPos2 = blockPos.offset(Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8));
            if (blockPos2.getY() <= this.level().getMinY() || !this.level().isEmptyBlock(blockPos2) || !this.level().getWorldBorder().isWithinBounds(blockPos2) || !this.level().noCollision(this, new AABB(blockPos2).deflate(1.0E-6)) || (direction = this.findAttachableSurface(blockPos2)) == null) continue;
            this.unRide();
            this.setAttachFace(direction);
            this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0f, 1.0f);
            this.setPos((double)blockPos2.getX() + 0.5, blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
            this.level().gameEvent(GameEvent.TELEPORT, blockPos, GameEvent.Context.of(this));
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.setTarget(null);
            return true;
        }
        return false;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        Entity entity;
        if (this.isClosed() && (entity = damageSource.getDirectEntity()) instanceof AbstractArrow) {
            return false;
        }
        if (super.hurtServer(serverLevel, damageSource, f)) {
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.teleportSomewhere();
            } else if (damageSource.is(DamageTypeTags.IS_PROJECTILE) && (entity = damageSource.getDirectEntity()) != null && entity.getType() == EntityType.SHULKER_BULLET) {
                this.hitByShulkerBullet();
            }
            return true;
        }
        return false;
    }

    private boolean isClosed() {
        return this.getRawPeekAmount() == 0;
    }

    private void hitByShulkerBullet() {
        Vec3 vec3 = this.position();
        AABB aABB = this.getBoundingBox();
        if (this.isClosed() || !this.teleportSomewhere()) {
            return;
        }
        int i = this.level().getEntities(EntityType.SHULKER, aABB.inflate(8.0), Entity::isAlive).size();
        float f = (float)(i - 1) / 5.0f;
        if (this.level().random.nextFloat() < f) {
            return;
        }
        Shulker shulker = EntityType.SHULKER.create(this.level(), EntitySpawnReason.BREEDING);
        if (shulker != null) {
            shulker.setVariant(this.getVariant());
            shulker.snapTo(vec3);
            this.level().addFreshEntity(shulker);
        }
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return this.isAlive();
    }

    public Direction getAttachFace() {
        return this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    private void setAttachFace(Direction direction) {
        this.entityData.set(DATA_ATTACH_FACE_ID, direction);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_ATTACH_FACE_ID.equals(entityDataAccessor)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID).byteValue();
    }

    void setRawPeekAmount(int i) {
        if (!this.level().isClientSide()) {
            this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
            if (i == 0) {
                this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                this.playSound(SoundEvents.SHULKER_CLOSE, 1.0f, 1.0f);
                this.gameEvent(GameEvent.CONTAINER_CLOSE);
            } else {
                this.playSound(SoundEvents.SHULKER_OPEN, 1.0f, 1.0f);
                this.gameEvent(GameEvent.CONTAINER_OPEN);
            }
        }
        this.entityData.set(DATA_PEEK_ID, (byte)i);
    }

    public float getClientPeekAmount(float f) {
        return Mth.lerp(f, this.currentPeekAmountO, this.currentPeekAmount);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.yBodyRot = 0.0f;
        this.yBodyRotO = 0.0f;
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override
    public void push(Entity entity) {
    }

    public @Nullable Vec3 getRenderPosition(float f) {
        if (this.clientOldAttachPosition == null || this.clientSideTeleportInterpolation <= 0) {
            return null;
        }
        double d = (double)((float)this.clientSideTeleportInterpolation - f) / 6.0;
        d *= d;
        BlockPos blockPos = this.blockPosition();
        double e = (double)(blockPos.getX() - this.clientOldAttachPosition.getX()) * (d *= (double)this.getScale());
        double g = (double)(blockPos.getY() - this.clientOldAttachPosition.getY()) * d;
        double h = (double)(blockPos.getZ() - this.clientOldAttachPosition.getZ()) * d;
        return new Vec3(-e, -g, -h);
    }

    @Override
    protected float sanitizeScale(float f) {
        return Math.min(f, 3.0f);
    }

    private void setVariant(Optional<DyeColor> optional) {
        this.entityData.set(DATA_COLOR_ID, optional.map(dyeColor -> (byte)dyeColor.getId()).orElse((byte)16));
    }

    public Optional<DyeColor> getVariant() {
        return Optional.ofNullable(this.getColor());
    }

    public @Nullable DyeColor getColor() {
        byte b = this.entityData.get(DATA_COLOR_ID);
        if (b == 16 || b > 15) {
            return null;
        }
        return DyeColor.byId(b);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.SHULKER_COLOR) {
            return Shulker.castComponentValue(dataComponentType, this.getColor());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.SHULKER_COLOR);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.SHULKER_COLOR) {
            this.setVariant(Optional.of(Shulker.castComponentValue(DataComponents.SHULKER_COLOR, object)));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    class ShulkerLookControl
    extends LookControl {
        public ShulkerLookControl(Mob mob) {
            super(mob);
        }

        @Override
        protected void clampHeadRotationToBody() {
        }

        @Override
        protected Optional<Float> getYRotD() {
            Direction direction = Shulker.this.getAttachFace().getOpposite();
            Vector3f vector3f = direction.getRotation().transform(new Vector3f((Vector3fc)FORWARD));
            Vec3i vec3i = direction.getUnitVec3i();
            Vector3f vector3f2 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            vector3f2.cross((Vector3fc)vector3f);
            double d = this.wantedX - this.mob.getX();
            double e = this.wantedY - this.mob.getEyeY();
            double f = this.wantedZ - this.mob.getZ();
            Vector3f vector3f3 = new Vector3f((float)d, (float)e, (float)f);
            float g = vector3f2.dot((Vector3fc)vector3f3);
            float h = vector3f.dot((Vector3fc)vector3f3);
            return Math.abs(g) > 1.0E-5f || Math.abs(h) > 1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2(-g, h) * 57.2957763671875))) : Optional.empty();
        }

        @Override
        protected Optional<Float> getXRotD() {
            return Optional.of(Float.valueOf(0.0f));
        }
    }

    class ShulkerAttackGoal
    extends Goal {
        private int attackTime;

        public ShulkerAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = Shulker.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            return Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void start() {
            this.attackTime = 20;
            Shulker.this.setRawPeekAmount(100);
        }

        @Override
        public void stop() {
            Shulker.this.setRawPeekAmount(0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            --this.attackTime;
            LivingEntity livingEntity = Shulker.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            Shulker.this.getLookControl().setLookAt(livingEntity, 180.0f, 180.0f);
            double d = Shulker.this.distanceToSqr(livingEntity);
            if (d < 400.0) {
                if (this.attackTime <= 0) {
                    this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
                    Shulker.this.level().addFreshEntity(new ShulkerBullet(Shulker.this.level(), Shulker.this, livingEntity, Shulker.this.getAttachFace().getAxis()));
                    Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0f, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2f + 1.0f);
                }
            } else {
                Shulker.this.setTarget(null);
            }
            super.tick();
        }
    }

    class ShulkerPeekGoal
    extends Goal {
        private int peekTime;

        ShulkerPeekGoal() {
        }

        @Override
        public boolean canUse() {
            return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(ShulkerPeekGoal.reducedTickDelay(40)) == 0 && Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
        }

        @Override
        public boolean canContinueToUse() {
            return Shulker.this.getTarget() == null && this.peekTime > 0;
        }

        @Override
        public void start() {
            this.peekTime = this.adjustedTickDelay(20 * (1 + Shulker.this.random.nextInt(3)));
            Shulker.this.setRawPeekAmount(30);
        }

        @Override
        public void stop() {
            if (Shulker.this.getTarget() == null) {
                Shulker.this.setRawPeekAmount(0);
            }
        }

        @Override
        public void tick() {
            --this.peekTime;
        }
    }

    class ShulkerNearestAttackGoal
    extends NearestAttackableTargetGoal<Player> {
        public ShulkerNearestAttackGoal(Shulker shulker2) {
            super((Mob)shulker2, Player.class, true);
        }

        @Override
        public boolean canUse() {
            if (Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double d) {
            Direction direction = ((Shulker)this.mob).getAttachFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, d, d);
            }
            if (direction.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate(d, d, 4.0);
            }
            return this.mob.getBoundingBox().inflate(d, 4.0, d);
        }
    }

    static class ShulkerDefenseAttackGoal
    extends NearestAttackableTargetGoal<LivingEntity> {
        public ShulkerDefenseAttackGoal(Shulker shulker) {
            super(shulker, LivingEntity.class, 10, true, false, (livingEntity, serverLevel) -> livingEntity instanceof Enemy);
        }

        @Override
        public boolean canUse() {
            if (this.mob.getTeam() == null) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double d) {
            Direction direction = ((Shulker)this.mob).getAttachFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, d, d);
            }
            if (direction.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate(d, d, 4.0);
            }
            return this.mob.getBoundingBox().inflate(d, 4.0, d);
        }
    }

    static class ShulkerBodyRotationControl
    extends BodyRotationControl {
        public ShulkerBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {
        }
    }
}

