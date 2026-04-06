/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.boat;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBoat
extends VehicleEntity
implements Leashable {
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.INT);
    public static final int PADDLE_LEFT = 0;
    public static final int PADDLE_RIGHT = 1;
    private static final int TIME_TO_EJECT = 60;
    private static final float PADDLE_SPEED = 0.3926991f;
    public static final double PADDLE_SOUND_TIME = 0.7853981852531433;
    public static final int BUBBLE_TIME = 60;
    private final float[] paddlePositions = new float[2];
    private float outOfControlTicks;
    private float deltaRotation;
    private final InterpolationHandler interpolation = new InterpolationHandler((Entity)this, 3);
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private Status status;
    private Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;
    private @Nullable Leashable.LeashData leashData;
    private final Supplier<Item> dropItem;

    public AbstractBoat(EntityType<? extends AbstractBoat> entityType, Level level, Supplier<Item> supplier) {
        super(entityType, level);
        this.dropItem = supplier;
        this.blocksBuilding = true;
    }

    public void setInitialPos(double d, double e, double f) {
        this.setPos(d, e, f);
        this.xo = d;
        this.yo = e;
        this.zo = f;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_PADDLE_LEFT, false);
        builder.define(DATA_ID_PADDLE_RIGHT, false);
        builder.define(DATA_ID_BUBBLE_TIME, 0);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return AbstractBoat.canVehicleCollide(this, entity);
    }

    public static boolean canVehicleCollide(Entity entity, Entity entity2) {
        return (entity2.canBeCollidedWith(entity) || entity2.isPushable()) && !entity.isPassengerOfSameVehicle(entity2);
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    protected abstract double rideHeight(EntityDimensions var1);

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        float g = this.getSinglePassengerXOffset();
        if (this.getPassengers().size() > 1) {
            int i = this.getPassengers().indexOf(entity);
            g = i == 0 ? 0.2f : -0.6f;
            if (entity instanceof Animal) {
                g += 0.2f;
            }
        }
        return new Vec3(0.0, this.rideHeight(entityDimensions), g).yRot(-this.getYRot() * ((float)Math.PI / 180));
    }

    @Override
    public void onAboveBubbleColumn(boolean bl, BlockPos blockPos) {
        if (this.level() instanceof ServerLevel) {
            this.isAboveBubbleColumn = true;
            this.bubbleColumnDirectionIsDown = bl;
            if (this.getBubbleTime() == 0) {
                this.setBubbleTime(60);
            }
        }
        if (!this.isUnderWater() && this.random.nextInt(100) == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat(), false);
            this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
        }
    }

    @Override
    public void push(Entity entity) {
        if (entity instanceof AbstractBoat) {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
                super.push(entity);
            }
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.push(entity);
        }
    }

    @Override
    public void animateHurt(float f) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        this.oldStatus = this.status;
        this.status = this.getStatus();
        this.outOfControlTicks = this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER ? (this.outOfControlTicks += 1.0f) : 0.0f;
        if (!this.level().isClientSide() && this.outOfControlTicks >= 60.0f) {
            this.ejectPassengers();
        }
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        super.tick();
        this.interpolation.interpolate();
        if (this.isLocalInstanceAuthoritative()) {
            if (!(this.getFirstPassenger() instanceof Player)) {
                this.setPaddleState(false, false);
            }
            this.floatBoat();
            if (this.level().isClientSide()) {
                this.controlBoat();
                this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.applyEffectsFromBlocks();
        this.applyEffectsFromBlocks();
        this.tickBubbleColumn();
        for (int i = 0; i <= 1; ++i) {
            if (this.getPaddleState(i)) {
                SoundEvent soundEvent;
                if (!this.isSilent() && (double)(this.paddlePositions[i] % ((float)Math.PI * 2)) <= 0.7853981852531433 && (double)((this.paddlePositions[i] + 0.3926991f) % ((float)Math.PI * 2)) >= 0.7853981852531433 && (soundEvent = this.getPaddleSound()) != null) {
                    Vec3 vec3 = this.getViewVector(1.0f);
                    double d = i == 1 ? -vec3.z : vec3.z;
                    double e = i == 1 ? vec3.x : -vec3.x;
                    this.level().playSound(null, this.getX() + d, this.getY(), this.getZ() + e, soundEvent, this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                }
                int n = i;
                this.paddlePositions[n] = this.paddlePositions[n] + 0.3926991f;
                continue;
            }
            this.paddlePositions[i] = 0.0f;
        }
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2f, -0.01f, 0.2f), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            boolean bl = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (bl && this.getPassengers().size() < this.getMaxPassengers() && !entity.isPassenger() && this.hasEnoughSpaceFor(entity) && entity instanceof LivingEntity && !entity.getType().is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS)) {
                    entity.startRiding(this);
                    continue;
                }
                this.push(entity);
            }
        }
    }

    private void tickBubbleColumn() {
        if (this.level().isClientSide()) {
            int i = this.getBubbleTime();
            this.bubbleMultiplier = i > 0 ? (this.bubbleMultiplier += 0.05f) : (this.bubbleMultiplier -= 0.1f);
            this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0f, 1.0f);
            this.bubbleAngleO = this.bubbleAngle;
            this.bubbleAngle = 10.0f * (float)Math.sin(0.5 * (double)this.tickCount) * this.bubbleMultiplier;
        } else {
            int i;
            if (!this.isAboveBubbleColumn) {
                this.setBubbleTime(0);
            }
            if ((i = this.getBubbleTime()) > 0) {
                this.setBubbleTime(--i);
                int j = 60 - i - 1;
                if (j > 0 && i == 0) {
                    this.setBubbleTime(0);
                    Vec3 vec3 = this.getDeltaMovement();
                    if (this.bubbleColumnDirectionIsDown) {
                        this.setDeltaMovement(vec3.add(0.0, -0.7, 0.0));
                        this.ejectPassengers();
                    } else {
                        this.setDeltaMovement(vec3.x, this.hasPassenger((Entity entity) -> entity instanceof Player) ? 2.7 : 0.6, vec3.z);
                    }
                }
                this.isAboveBubbleColumn = false;
            }
        }
    }

    protected @Nullable SoundEvent getPaddleSound() {
        return switch (this.getStatus().ordinal()) {
            case 0, 1, 2 -> SoundEvents.BOAT_PADDLE_WATER;
            case 3 -> SoundEvents.BOAT_PADDLE_LAND;
            default -> null;
        };
    }

    public void setPaddleState(boolean bl, boolean bl2) {
        this.entityData.set(DATA_ID_PADDLE_LEFT, bl);
        this.entityData.set(DATA_ID_PADDLE_RIGHT, bl2);
    }

    public float getRowingTime(int i, float f) {
        if (this.getPaddleState(i)) {
            return Mth.clampedLerp(f, this.paddlePositions[i] - 0.3926991f, this.paddlePositions[i]);
        }
        return 0.0f;
    }

    @Override
    public @Nullable Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.88f * this.getBbHeight(), 0.64f * this.getBbWidth());
    }

    @Override
    public boolean supportQuadLeash() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.0, 0.64, 0.382, 0.88);
    }

    private Status getStatus() {
        Status status = this.isUnderwater();
        if (status != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return status;
        }
        if (this.checkInWater()) {
            return Status.IN_WATER;
        }
        float f = this.getGroundFriction();
        if (f > 0.0f) {
            this.landFriction = f;
            return Status.ON_LAND;
        }
        return Status.IN_AIR;
    }

    public float getWaterLevelAbove() {
        AABB aABB = this.getBoundingBox();
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.maxY);
        int l = Mth.ceil(aABB.maxY - this.lastYd);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (int o = k; o < l; ++o) {
            float f = 0.0f;
            for (int p = i; p < j; ++p) {
                for (int q = m; q < n; ++q) {
                    mutableBlockPos.set(p, o, q);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (fluidState.is(FluidTags.WATER)) {
                        f = Math.max(f, fluidState.getHeight(this.level(), mutableBlockPos));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)mutableBlockPos.getY() + f;
        }
        return l + 1;
    }

    public float getGroundFriction() {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = new AABB(aABB.minX, aABB.minY - 0.001, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
        int i = Mth.floor(aABB2.minX) - 1;
        int j = Mth.ceil(aABB2.maxX) + 1;
        int k = Mth.floor(aABB2.minY) - 1;
        int l = Mth.ceil(aABB2.maxY) + 1;
        int m = Mth.floor(aABB2.minZ) - 1;
        int n = Mth.ceil(aABB2.maxZ) + 1;
        VoxelShape voxelShape = Shapes.create(aABB2);
        float f = 0.0f;
        int o = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int p = i; p < j; ++p) {
            for (int q = m; q < n; ++q) {
                int r = (p == i || p == j - 1 ? 1 : 0) + (q == m || q == n - 1 ? 1 : 0);
                if (r == 2) continue;
                for (int s = k; s < l; ++s) {
                    if (r > 0 && (s == k || s == l - 1)) continue;
                    mutableBlockPos.set(p, s, q);
                    BlockState blockState = this.level().getBlockState(mutableBlockPos);
                    if (blockState.getBlock() instanceof WaterlilyBlock || !Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level(), mutableBlockPos).move(mutableBlockPos), voxelShape, BooleanOp.AND)) continue;
                    f += blockState.getBlock().getFriction();
                    ++o;
                }
            }
        }
        return f / (float)o;
    }

    private boolean checkInWater() {
        AABB aABB = this.getBoundingBox();
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.minY + 0.001);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        boolean bl = false;
        this.waterLevel = -1.7976931348623157E308;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    mutableBlockPos.set(o, p, q);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (!fluidState.is(FluidTags.WATER)) continue;
                    float f = (float)p + fluidState.getHeight(this.level(), mutableBlockPos);
                    this.waterLevel = Math.max((double)f, this.waterLevel);
                    bl |= aABB.minY < (double)f;
                }
            }
        }
        return bl;
    }

    private @Nullable Status isUnderwater() {
        AABB aABB = this.getBoundingBox();
        double d = aABB.maxY + 0.001;
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.maxY);
        int l = Mth.ceil(d);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    mutableBlockPos.set(o, p, q);
                    FluidState fluidState = this.level().getFluidState(mutableBlockPos);
                    if (!fluidState.is(FluidTags.WATER) || !(d < (double)((float)mutableBlockPos.getY() + fluidState.getHeight(this.level(), mutableBlockPos)))) continue;
                    if (fluidState.isSource()) {
                        bl = true;
                        continue;
                    }
                    return Status.UNDER_FLOWING_WATER;
                }
            }
        }
        return bl ? Status.UNDER_WATER : null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    private void floatBoat() {
        double d = -this.getGravity();
        double e = 0.0;
        float f = 0.05f;
        if (this.oldStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = this.getY(1.0);
            double g = (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101;
            if (this.level().noCollision(this, this.getBoundingBox().move(0.0, g - this.getY(), 0.0))) {
                this.setPos(this.getX(), g, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                this.lastYd = 0.0;
            }
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                e = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
                f = 0.9f;
            } else if (this.status == Status.UNDER_FLOWING_WATER) {
                d = -7.0E-4;
                f = 0.9f;
            } else if (this.status == Status.UNDER_WATER) {
                e = 0.01f;
                f = 0.45f;
            } else if (this.status == Status.IN_AIR) {
                f = 0.9f;
            } else if (this.status == Status.ON_LAND) {
                f = this.landFriction;
                if (this.getControllingPassenger() instanceof Player) {
                    this.landFriction /= 2.0f;
                }
            }
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x * (double)f, vec3.y + d, vec3.z * (double)f);
            this.deltaRotation *= f;
            if (e > 0.0) {
                Vec3 vec32 = this.getDeltaMovement();
                this.setDeltaMovement(vec32.x, (vec32.y + e * (this.getDefaultGravity() / 0.65)) * 0.75, vec32.z);
            }
        }
    }

    private void controlBoat() {
        if (!this.isVehicle()) {
            return;
        }
        float f = 0.0f;
        if (this.inputLeft) {
            this.deltaRotation -= 1.0f;
        }
        if (this.inputRight) {
            this.deltaRotation += 1.0f;
        }
        if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            f += 0.005f;
        }
        this.setYRot(this.getYRot() + this.deltaRotation);
        if (this.inputUp) {
            f += 0.04f;
        }
        if (this.inputDown) {
            f -= 0.005f;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(Mth.sin(-this.getYRot() * ((float)Math.PI / 180)) * f, 0.0, Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * f));
        this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
    }

    protected float getSinglePassengerXOffset() {
        return 0.0f;
    }

    public boolean hasEnoughSpaceFor(Entity entity) {
        return entity.getBbWidth() < this.getBbWidth();
    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        if (entity.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            return;
        }
        entity.setYRot(entity.getYRot() + this.deltaRotation);
        entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
        this.clampRotation(entity);
        if (entity instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
            int i = entity.getId() % 2 == 0 ? 90 : 270;
            entity.setYBodyRot(((Animal)entity).yBodyRot + (float)i);
            entity.setYHeadRot(entity.getYHeadRot() + (float)i);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 vec3 = AbstractBoat.getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, livingEntity.getBbWidth(), livingEntity.getYRot());
        double d = this.getX() + vec3.x;
        double e = this.getZ() + vec3.z;
        BlockPos blockPos = BlockPos.containing(d, this.getBoundingBox().maxY, e);
        BlockPos blockPos2 = blockPos.below();
        if (!this.level().isWaterAt(blockPos2)) {
            double g;
            ArrayList list = Lists.newArrayList();
            double f = this.level().getBlockFloorHeight(blockPos);
            if (DismountHelper.isBlockFloorValid(f)) {
                list.add(new Vec3(d, (double)blockPos.getY() + f, e));
            }
            if (DismountHelper.isBlockFloorValid(g = this.level().getBlockFloorHeight(blockPos2))) {
                list.add(new Vec3(d, (double)blockPos2.getY() + g, e));
            }
            for (Pose pose : livingEntity.getDismountPoses()) {
                for (Vec3 vec32 : list) {
                    if (!DismountHelper.canDismountTo(this.level(), vec32, livingEntity, pose)) continue;
                    livingEntity.setPose(pose);
                    return vec32;
                }
            }
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    protected void clampRotation(Entity entity) {
        entity.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
        float g = Mth.clamp(f, -105.0f, 105.0f);
        entity.yRotO += g - f;
        entity.setYRot(entity.getYRot() + g - f);
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        this.clampRotation(entity);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        this.writeLeashData(valueOutput, this.leashData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.readLeashData(valueInput);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult = super.interact(player, interactionHand);
        if (interactionResult != InteractionResult.PASS) {
            return interactionResult;
        }
        if (!player.isSecondaryUseActive() && this.outOfControlTicks < 60.0f && (this.level().isClientSide() || player.startRiding(this))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide() && removalReason.shouldDestroy() && this.isLeashed()) {
            this.dropLeash();
        }
        super.remove(removalReason);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        this.lastYd = this.getDeltaMovement().y;
        if (this.isPassenger()) {
            return;
        }
        if (bl) {
            this.resetFallDistance();
        } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && d < 0.0) {
            this.fallDistance -= (double)((float)d);
        }
    }

    public boolean getPaddleState(int i) {
        return this.entityData.get(i == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) != false && this.getControllingPassenger() != null;
    }

    private void setBubbleTime(int i) {
        this.entityData.set(DATA_ID_BUBBLE_TIME, i);
    }

    private int getBubbleTime() {
        return this.entityData.get(DATA_ID_BUBBLE_TIME);
    }

    public float getBubbleAngle(float f) {
        return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        LivingEntity livingEntity;
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : super.getControllingPassenger();
    }

    public void setInput(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        this.inputLeft = bl;
        this.inputRight = bl2;
        this.inputUp = bl3;
        this.inputDown = bl4;
    }

    @Override
    public boolean isUnderWater() {
        return this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER;
    }

    @Override
    protected final Item getDropItem() {
        return this.dropItem.get();
    }

    @Override
    public final ItemStack getPickResult() {
        return new ItemStack(this.dropItem.get());
    }

    public static enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;

    }
}

