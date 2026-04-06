/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.google.common.collect.UnmodifiableIterator
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMinecart
extends VehicleEntity {
    private static final Vec3 LOWERED_PASSENGER_ATTACHMENT = new Vec3(0.0, 0.0, 0.0);
    private static final EntityDataAccessor<Optional<BlockState>> DATA_ID_CUSTOM_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of((Object)Pose.STANDING, (Object)ImmutableList.of((Object)0, (Object)1, (Object)-1), (Object)Pose.CROUCHING, (Object)ImmutableList.of((Object)0, (Object)1, (Object)-1), (Object)Pose.SWIMMING, (Object)ImmutableList.of((Object)0, (Object)1));
    protected static final float WATER_SLOWDOWN_FACTOR = 0.95f;
    private static final boolean DEFAULT_FLIPPED_ROTATION = false;
    private boolean onRails;
    private boolean flipped = false;
    private final MinecartBehavior behavior;
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Maps.newEnumMap((Map)((Map)Util.make(() -> {
        Vec3i vec3i = Direction.WEST.getUnitVec3i();
        Vec3i vec3i2 = Direction.EAST.getUnitVec3i();
        Vec3i vec3i3 = Direction.NORTH.getUnitVec3i();
        Vec3i vec3i4 = Direction.SOUTH.getUnitVec3i();
        Vec3i vec3i5 = vec3i.below();
        Vec3i vec3i6 = vec3i2.below();
        Vec3i vec3i7 = vec3i3.below();
        Vec3i vec3i8 = vec3i4.below();
        return ImmutableMap.of((Object)RailShape.NORTH_SOUTH, (Object)Pair.of((Object)vec3i3, (Object)vec3i4), (Object)RailShape.EAST_WEST, (Object)Pair.of((Object)vec3i, (Object)vec3i2), (Object)RailShape.ASCENDING_EAST, (Object)Pair.of((Object)vec3i5, (Object)vec3i2), (Object)RailShape.ASCENDING_WEST, (Object)Pair.of((Object)vec3i, (Object)vec3i6), (Object)RailShape.ASCENDING_NORTH, (Object)Pair.of((Object)vec3i3, (Object)vec3i8), (Object)RailShape.ASCENDING_SOUTH, (Object)Pair.of((Object)vec3i7, (Object)vec3i4), (Object)RailShape.SOUTH_EAST, (Object)Pair.of((Object)vec3i4, (Object)vec3i2), (Object)RailShape.SOUTH_WEST, (Object)Pair.of((Object)vec3i4, (Object)vec3i), (Object)RailShape.NORTH_WEST, (Object)Pair.of((Object)vec3i3, (Object)vec3i), (Object)RailShape.NORTH_EAST, (Object)Pair.of((Object)vec3i3, (Object)vec3i2));
    })));

    protected AbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.behavior = AbstractMinecart.useExperimentalMovement(level) ? new NewMinecartBehavior(this) : new OldMinecartBehavior(this);
    }

    protected AbstractMinecart(EntityType<?> entityType, Level level, double d, double e, double f) {
        this(entityType, level);
        this.setInitialPos(d, e, f);
    }

    public void setInitialPos(double d, double e, double f) {
        this.setPos(d, e, f);
        this.xo = d;
        this.yo = e;
        this.zo = f;
    }

    public static <T extends AbstractMinecart> @Nullable T createMinecart(Level level, double d, double e, double f, EntityType<T> entityType, EntitySpawnReason entitySpawnReason, ItemStack itemStack, @Nullable Player player) {
        AbstractMinecart abstractMinecart = (AbstractMinecart)entityType.create(level, entitySpawnReason);
        if (abstractMinecart != null) {
            abstractMinecart.setInitialPos(d, e, f);
            EntityType.createDefaultStackConfig(level, itemStack, player).accept(abstractMinecart);
            MinecartBehavior minecartBehavior = abstractMinecart.getBehavior();
            if (minecartBehavior instanceof NewMinecartBehavior) {
                NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)minecartBehavior;
                BlockPos blockPos = abstractMinecart.getCurrentBlockPosOrRailBelow();
                BlockState blockState = level.getBlockState(blockPos);
                newMinecartBehavior.adjustToRails(blockPos, blockState, true);
            }
        }
        return (T)abstractMinecart;
    }

    public MinecartBehavior getBehavior() {
        return this.behavior;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_CUSTOM_DISPLAY_BLOCK, Optional.empty());
        builder.define(DATA_ID_DISPLAY_OFFSET, this.getDefaultDisplayOffset());
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return AbstractBoat.canVehicleCollide(this, entity);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        boolean bl;
        boolean bl2 = bl = entity instanceof Villager || entity instanceof WanderingTrader;
        if (bl) {
            return LOWERED_PASSENGER_ATTACHMENT;
        }
        return super.getPassengerAttachmentPoint(entity, entityDimensions, f);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(livingEntity);
        }
        int[][] is = DismountHelper.offsetsForDirection(direction);
        BlockPos blockPos2 = this.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        ImmutableList<Pose> immutableList = livingEntity.getDismountPoses();
        for (Pose pose : immutableList) {
            EntityDimensions entityDimensions = livingEntity.getDimensions(pose);
            float f = Math.min(entityDimensions.width(), 1.0f) / 2.0f;
            UnmodifiableIterator unmodifiableIterator = ((ImmutableList)POSE_DISMOUNT_HEIGHTS.get((Object)pose)).iterator();
            while (unmodifiableIterator.hasNext()) {
                int i = (Integer)unmodifiableIterator.next();
                for (int[] js : is) {
                    mutableBlockPos.set(blockPos2.getX() + js[0], blockPos2.getY() + i, blockPos2.getZ() + js[1]);
                    double d = this.level().getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level(), mutableBlockPos), () -> DismountHelper.nonClimbableShape(this.level(), (BlockPos)mutableBlockPos.below()));
                    if (!DismountHelper.isBlockFloorValid(d)) continue;
                    AABB aABB = new AABB(-f, 0.0, -f, f, entityDimensions.height(), f);
                    Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d);
                    if (!DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec3))) continue;
                    livingEntity.setPose(pose);
                    return vec3;
                }
            }
        }
        double e = this.getBoundingBox().maxY;
        mutableBlockPos.set((double)blockPos2.getX(), e, (double)blockPos2.getZ());
        for (Pose pose2 : immutableList) {
            int j;
            double h;
            double g = livingEntity.getDimensions(pose2).height();
            if (!(e + g <= (h = DismountHelper.findCeilingFrom(mutableBlockPos, j = Mth.ceil(e - (double)mutableBlockPos.getY() + g), blockPos -> this.level().getBlockState((BlockPos)blockPos).getCollisionShape(this.level(), (BlockPos)blockPos))))) continue;
            livingEntity.setPose(pose2);
            break;
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState blockState = this.level().getBlockState(this.blockPosition());
        if (blockState.is(BlockTags.RAILS)) {
            return 1.0f;
        }
        return super.getBlockSpeedFactor();
    }

    @Override
    public void animateHurt(float f) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    public static Pair<Vec3i, Vec3i> exits(RailShape railShape) {
        return EXITS.get(railShape);
    }

    @Override
    public Direction getMotionDirection() {
        return this.behavior.getMotionDirection();
    }

    @Override
    protected double getDefaultGravity() {
        return this.isInWater() ? 0.005 : 0.04;
    }

    @Override
    public void tick() {
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        this.checkBelowWorld();
        this.computeSpeed();
        this.handlePortal();
        this.behavior.tick();
        this.updateInWaterStateAndDoFluidPushing();
        if (this.isInLava()) {
            this.lavaIgnite();
            this.lavaHurt();
            this.fallDistance *= 0.5;
        }
        this.firstTick = false;
    }

    public boolean isFirstTick() {
        return this.firstTick;
    }

    public BlockPos getCurrentBlockPosOrRailBelow() {
        int i = Mth.floor(this.getX());
        int j = Mth.floor(this.getY());
        int k = Mth.floor(this.getZ());
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            double d = this.getY() - 0.1 - (double)1.0E-5f;
            if (this.level().getBlockState(BlockPos.containing(i, d, k)).is(BlockTags.RAILS)) {
                j = Mth.floor(d);
            }
        } else if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
            --j;
        }
        return new BlockPos(i, j, k);
    }

    protected double getMaxSpeed(ServerLevel serverLevel) {
        return this.behavior.getMaxSpeed(serverLevel);
    }

    public void activateMinecart(ServerLevel serverLevel, int i, int j, int k, boolean bl) {
    }

    @Override
    public void lerpPositionAndRotationStep(int i, double d, double e, double f, double g, double h) {
        super.lerpPositionAndRotationStep(i, d, e, f, g, h);
    }

    @Override
    public void applyGravity() {
        super.applyGravity();
    }

    @Override
    public void reapplyPosition() {
        super.reapplyPosition();
    }

    @Override
    public boolean updateInWaterStateAndDoFluidPushing() {
        return super.updateInWaterStateAndDoFluidPushing();
    }

    @Override
    public Vec3 getKnownMovement() {
        return this.behavior.getKnownMovement(super.getKnownMovement());
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.behavior.getInterpolation();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.behavior.lerpMotion(this.getDeltaMovement());
    }

    @Override
    public void lerpMotion(Vec3 vec3) {
        this.behavior.lerpMotion(vec3);
    }

    protected void moveAlongTrack(ServerLevel serverLevel) {
        this.behavior.moveAlongTrack(serverLevel);
    }

    protected void comeOffTrack(ServerLevel serverLevel) {
        double d = this.getMaxSpeed(serverLevel);
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp(vec3.x, -d, d), vec3.y, Mth.clamp(vec3.z, -d, d));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        }
    }

    protected double makeStepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
        return this.behavior.stepAlongTrack(blockPos, railShape, d);
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            Vec3 vec32 = this.position().add(vec3);
            super.move(moverType, vec3);
            boolean bl = this.behavior.pushAndPickupEntities();
            if (bl) {
                super.move(moverType, vec32.subtract(this.position()));
            }
            if (moverType.equals((Object)MoverType.PISTON)) {
                this.onRails = false;
            }
        } else {
            super.move(moverType, vec3);
            this.applyEffectsFromBlocks();
        }
    }

    @Override
    public void applyEffectsFromBlocks() {
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            super.applyEffectsFromBlocks();
        } else {
            this.applyEffectsFromBlocks(this.position(), this.position());
            this.clearMovementThisTick();
        }
    }

    @Override
    public boolean isOnRails() {
        return this.onRails;
    }

    public void setOnRails(boolean bl) {
        this.onRails = bl;
    }

    public boolean isFlipped() {
        return this.flipped;
    }

    public void setFlipped(boolean bl) {
        this.flipped = bl;
    }

    public Vec3 getRedstoneDirection(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (!blockState.is(Blocks.POWERED_RAIL) || !blockState.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return Vec3.ZERO;
        }
        RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
        if (railShape == RailShape.EAST_WEST) {
            if (this.isRedstoneConductor(blockPos.west())) {
                return new Vec3(1.0, 0.0, 0.0);
            }
            if (this.isRedstoneConductor(blockPos.east())) {
                return new Vec3(-1.0, 0.0, 0.0);
            }
        } else if (railShape == RailShape.NORTH_SOUTH) {
            if (this.isRedstoneConductor(blockPos.north())) {
                return new Vec3(0.0, 0.0, 1.0);
            }
            if (this.isRedstoneConductor(blockPos.south())) {
                return new Vec3(0.0, 0.0, -1.0);
            }
        }
        return Vec3.ZERO;
    }

    public boolean isRedstoneConductor(BlockPos blockPos) {
        return this.level().getBlockState(blockPos).isRedstoneConductor(this.level(), blockPos);
    }

    protected Vec3 applyNaturalSlowdown(Vec3 vec3) {
        double d = this.behavior.getSlowdownFactor();
        Vec3 vec32 = vec3.multiply(d, 0.0, d);
        if (this.isInWater()) {
            vec32 = vec32.scale(0.95f);
        }
        return vec32;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setCustomDisplayBlockState(valueInput.read("DisplayState", BlockState.CODEC));
        this.setDisplayOffset(valueInput.getIntOr("DisplayOffset", this.getDefaultDisplayOffset()));
        this.flipped = valueInput.getBooleanOr("FlippedRotation", false);
        this.firstTick = valueInput.getBooleanOr("HasTicked", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        this.getCustomDisplayBlockState().ifPresent(blockState -> valueOutput.store("DisplayState", BlockState.CODEC, blockState));
        int i = this.getDisplayOffset();
        if (i != this.getDefaultDisplayOffset()) {
            valueOutput.putInt("DisplayOffset", i);
        }
        valueOutput.putBoolean("FlippedRotation", this.flipped);
        valueOutput.putBoolean("HasTicked", this.firstTick);
    }

    @Override
    public void push(Entity entity) {
        double e;
        if (this.level().isClientSide()) {
            return;
        }
        if (entity.noPhysics || this.noPhysics) {
            return;
        }
        if (this.hasPassenger(entity)) {
            return;
        }
        double d = entity.getX() - this.getX();
        double f = d * d + (e = entity.getZ() - this.getZ()) * e;
        if (f >= (double)1.0E-4f) {
            f = Math.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.1f;
            e *= (double)0.1f;
            d *= 0.5;
            e *= 0.5;
            if (entity instanceof AbstractMinecart) {
                AbstractMinecart abstractMinecart = (AbstractMinecart)entity;
                this.pushOtherMinecart(abstractMinecart, d, e);
            } else {
                this.push(-d, 0.0, -e);
                entity.push(d / 4.0, 0.0, e / 4.0);
            }
        }
    }

    private void pushOtherMinecart(AbstractMinecart abstractMinecart, double d, double e) {
        double g;
        double f;
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            f = this.getDeltaMovement().x;
            g = this.getDeltaMovement().z;
        } else {
            f = abstractMinecart.getX() - this.getX();
            g = abstractMinecart.getZ() - this.getZ();
        }
        Vec3 vec3 = new Vec3(f, 0.0, g).normalize();
        Vec3 vec32 = new Vec3(Mth.cos(this.getYRot() * ((float)Math.PI / 180)), 0.0, Mth.sin(this.getYRot() * ((float)Math.PI / 180))).normalize();
        double h = Math.abs(vec3.dot(vec32));
        if (h < (double)0.8f && !AbstractMinecart.useExperimentalMovement(this.level())) {
            return;
        }
        Vec3 vec33 = this.getDeltaMovement();
        Vec3 vec34 = abstractMinecart.getDeltaMovement();
        if (abstractMinecart.isFurnace() && !this.isFurnace()) {
            this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
            this.push(vec34.x - d, 0.0, vec34.z - e);
            abstractMinecart.setDeltaMovement(vec34.multiply(0.95, 1.0, 0.95));
        } else if (!abstractMinecart.isFurnace() && this.isFurnace()) {
            abstractMinecart.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
            abstractMinecart.push(vec33.x + d, 0.0, vec33.z + e);
            this.setDeltaMovement(vec33.multiply(0.95, 1.0, 0.95));
        } else {
            double i = (vec34.x + vec33.x) / 2.0;
            double j = (vec34.z + vec33.z) / 2.0;
            this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
            this.push(i - d, 0.0, j - e);
            abstractMinecart.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
            abstractMinecart.push(i + d, 0.0, j + e);
        }
    }

    public BlockState getDisplayBlockState() {
        return this.getCustomDisplayBlockState().orElseGet(this::getDefaultDisplayBlockState);
    }

    private Optional<BlockState> getCustomDisplayBlockState() {
        return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY_BLOCK);
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    public int getDisplayOffset() {
        return this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
    }

    public int getDefaultDisplayOffset() {
        return 6;
    }

    public void setCustomDisplayBlockState(Optional<BlockState> optional) {
        this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY_BLOCK, optional);
    }

    public void setDisplayOffset(int i) {
        this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, i);
    }

    public static boolean useExperimentalMovement(Level level) {
        return level.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS);
    }

    @Override
    public abstract ItemStack getPickResult();

    public boolean isRideable() {
        return false;
    }

    public boolean isFurnace() {
        return false;
    }
}

