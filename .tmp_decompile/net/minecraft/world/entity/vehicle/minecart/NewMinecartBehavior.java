/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class NewMinecartBehavior
extends MinecartBehavior {
    public static final int POS_ROT_LERP_TICKS = 3;
    public static final double ON_RAIL_Y_OFFSET = 0.1;
    public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005;
    private @Nullable StepPartialTicks cacheIndexAlpha;
    private int cachedLerpDelay;
    private float cachedPartialTick;
    private int lerpDelay = 0;
    public final List<MinecartStep> lerpSteps = new LinkedList<MinecartStep>();
    public final List<MinecartStep> currentLerpSteps = new LinkedList<MinecartStep>();
    public double currentLerpStepsTotalWeight = 0.0;
    public MinecartStep oldLerp = MinecartStep.ZERO;

    public NewMinecartBehavior(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            this.lerpClientPositionAndRotation();
            boolean bl = BaseRailBlock.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));
            this.minecart.setOnRails(bl);
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        BlockPos blockPos = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (this.minecart.isFirstTick()) {
            this.minecart.setOnRails(BaseRailBlock.isRail(blockState));
            this.adjustToRails(blockPos, blockState, true);
        }
        this.minecart.applyGravity();
        this.minecart.moveAlongTrack(serverLevel);
    }

    private void lerpClientPositionAndRotation() {
        if (--this.lerpDelay <= 0) {
            this.setOldLerpValues();
            this.currentLerpSteps.clear();
            if (!this.lerpSteps.isEmpty()) {
                this.currentLerpSteps.addAll(this.lerpSteps);
                this.lerpSteps.clear();
                this.currentLerpStepsTotalWeight = 0.0;
                for (MinecartStep minecartStep : this.currentLerpSteps) {
                    this.currentLerpStepsTotalWeight += (double)minecartStep.weight;
                }
                int n = this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0 ? 0 : 3;
            }
        }
        if (this.cartHasPosRotLerp()) {
            this.setPos(this.getCartLerpPosition(1.0f));
            this.setDeltaMovement(this.getCartLerpMovements(1.0f));
            this.setXRot(this.getCartLerpXRot(1.0f));
            this.setYRot(this.getCartLerpYRot(1.0f));
        }
    }

    public void setOldLerpValues() {
        this.oldLerp = new MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0f);
    }

    public boolean cartHasPosRotLerp() {
        return !this.currentLerpSteps.isEmpty();
    }

    public float getCartLerpXRot(float f) {
        StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
        return Mth.rotLerp(stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.xRot, stepPartialTicks.currentStep.xRot);
    }

    public float getCartLerpYRot(float f) {
        StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
        return Mth.rotLerp(stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.yRot, stepPartialTicks.currentStep.yRot);
    }

    public Vec3 getCartLerpPosition(float f) {
        StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
        return Mth.lerp((double)stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.position, stepPartialTicks.currentStep.position);
    }

    public Vec3 getCartLerpMovements(float f) {
        StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
        return Mth.lerp((double)stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.movement, stepPartialTicks.currentStep.movement);
    }

    private StepPartialTicks getCurrentLerpStep(float f) {
        int j;
        if (f == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
            return this.cacheIndexAlpha;
        }
        float g = ((float)(3 - this.lerpDelay) + f) / 3.0f;
        float h = 0.0f;
        float i = 1.0f;
        boolean bl = false;
        for (j = 0; j < this.currentLerpSteps.size(); ++j) {
            float k = this.currentLerpSteps.get((int)j).weight;
            if (k <= 0.0f || !((double)(h += k) >= this.currentLerpStepsTotalWeight * (double)g)) continue;
            float l = h - k;
            i = (float)(((double)g * this.currentLerpStepsTotalWeight - (double)l) / (double)k);
            bl = true;
            break;
        }
        if (!bl) {
            j = this.currentLerpSteps.size() - 1;
        }
        MinecartStep minecartStep = this.currentLerpSteps.get(j);
        MinecartStep minecartStep2 = j > 0 ? this.currentLerpSteps.get(j - 1) : this.oldLerp;
        this.cacheIndexAlpha = new StepPartialTicks(i, minecartStep, minecartStep2);
        this.cachedLerpDelay = this.lerpDelay;
        this.cachedPartialTick = f;
        return this.cacheIndexAlpha;
    }

    public void adjustToRails(BlockPos blockPos, BlockState blockState, boolean bl) {
        boolean bl5;
        Vec3 vec310;
        Vec3 vec37;
        boolean bl2;
        if (!BaseRailBlock.isRail(blockState)) {
            return;
        }
        RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
        Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
        Vec3 vec3 = new Vec3((Vec3i)pair.getFirst()).scale(0.5);
        Vec3 vec32 = new Vec3((Vec3i)pair.getSecond()).scale(0.5);
        Vec3 vec33 = vec3.horizontal();
        Vec3 vec34 = vec32.horizontal();
        if (this.getDeltaMovement().length() > (double)1.0E-5f && this.getDeltaMovement().dot(vec33) < this.getDeltaMovement().dot(vec34) || this.isDecending(vec34, railShape)) {
            Vec3 vec35 = vec33;
            vec33 = vec34;
            vec34 = vec35;
        }
        float f = 180.0f - (float)(Math.atan2(vec33.z, vec33.x) * 180.0 / Math.PI);
        f += this.minecart.isFlipped() ? 180.0f : 0.0f;
        Vec3 vec36 = this.position();
        boolean bl3 = bl2 = vec3.x() != vec32.x() && vec3.z() != vec32.z();
        if (bl2) {
            vec37 = vec32.subtract(vec3);
            Vec3 vec38 = vec36.subtract(blockPos.getBottomCenter()).subtract(vec3);
            Vec3 vec39 = vec37.scale(vec37.dot(vec38) / vec37.dot(vec37));
            vec310 = blockPos.getBottomCenter().add(vec3).add(vec39);
            f = 180.0f - (float)(Math.atan2(vec39.z, vec39.x) * 180.0 / Math.PI);
            f += this.minecart.isFlipped() ? 180.0f : 0.0f;
        } else {
            boolean bl32 = vec3.subtract((Vec3)vec32).x != 0.0;
            boolean bl4 = vec3.subtract((Vec3)vec32).z != 0.0;
            vec310 = new Vec3(bl4 ? blockPos.getCenter().x : vec36.x, blockPos.getY(), bl32 ? blockPos.getCenter().z : vec36.z);
        }
        vec37 = vec310.subtract(vec36);
        this.setPos(vec36.add(vec37));
        float g = 0.0f;
        boolean bl4 = bl5 = vec3.y() != vec32.y();
        if (bl5) {
            Vec3 vec311 = blockPos.getBottomCenter().add(vec34);
            double d = vec311.distanceTo(this.position());
            this.setPos(this.position().add(0.0, d + 0.1, 0.0));
            g = this.minecart.isFlipped() ? 45.0f : -45.0f;
        } else {
            this.setPos(this.position().add(0.0, 0.1, 0.0));
        }
        this.setRotation(f, g);
        double e = vec36.distanceTo(this.position());
        if (e > 0.0) {
            this.lerpSteps.add(new MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), bl ? 0.0f : (float)e));
        }
    }

    private void setRotation(float f, float g) {
        double d = Math.abs(f - this.getYRot());
        if (d >= 175.0 && d <= 185.0) {
            this.minecart.setFlipped(!this.minecart.isFlipped());
            f -= 180.0f;
            g *= -1.0f;
        }
        g = Math.clamp((float)g, (float)-45.0f, (float)45.0f);
        this.setXRot(g % 360.0f);
        this.setYRot(f % 360.0f);
    }

    @Override
    public void moveAlongTrack(ServerLevel serverLevel) {
        TrackIteration trackIteration = new TrackIteration();
        while (trackIteration.shouldIterate() && this.minecart.isAlive()) {
            Vec3 vec32;
            Vec3 vec3 = this.getDeltaMovement();
            BlockPos blockPos = this.minecart.getCurrentBlockPosOrRailBelow();
            BlockState blockState = this.level().getBlockState(blockPos);
            boolean bl = BaseRailBlock.isRail(blockState);
            if (this.minecart.isOnRails() != bl) {
                this.minecart.setOnRails(bl);
                this.adjustToRails(blockPos, blockState, false);
            }
            if (bl) {
                this.minecart.resetFallDistance();
                this.minecart.setOldPosAndRot();
                if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart(serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockState.getValue(PoweredRailBlock.POWERED));
                }
                RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
                vec32 = this.calculateTrackSpeed(serverLevel, vec3.horizontal(), trackIteration, blockPos, blockState, railShape);
                trackIteration.movementLeft = trackIteration.firstIteration ? vec32.horizontalDistance() : (trackIteration.movementLeft += vec32.horizontalDistance() - vec3.horizontalDistance());
                this.setDeltaMovement(vec32);
                trackIteration.movementLeft = this.minecart.makeStepAlongTrack(blockPos, railShape, trackIteration.movementLeft);
            } else {
                this.minecart.comeOffTrack(serverLevel);
                trackIteration.movementLeft = 0.0;
            }
            Vec3 vec33 = this.position();
            vec32 = vec33.subtract(this.minecart.oldPosition());
            double d = vec32.length();
            if (d > (double)1.0E-5f) {
                if (vec32.horizontalDistanceSqr() > (double)1.0E-5f) {
                    float f = 180.0f - (float)(Math.atan2(vec32.z, vec32.x) * 180.0 / Math.PI);
                    float g = this.minecart.onGround() && !this.minecart.isOnRails() ? 0.0f : 90.0f - (float)(Math.atan2(vec32.horizontalDistance(), vec32.y) * 180.0 / Math.PI);
                    this.setRotation(f += this.minecart.isFlipped() ? 180.0f : 0.0f, g *= this.minecart.isFlipped() ? -1.0f : 1.0f);
                } else if (!this.minecart.isOnRails()) {
                    this.setXRot(this.minecart.onGround() ? 0.0f : Mth.rotLerp(0.2f, this.getXRot(), 0.0f));
                }
                this.lerpSteps.add(new MinecartStep(vec33, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)Math.min(d, this.getMaxSpeed(serverLevel))));
            } else if (vec3.horizontalDistanceSqr() > 0.0) {
                this.lerpSteps.add(new MinecartStep(vec33, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0f));
            }
            if (d > (double)1.0E-5f || trackIteration.firstIteration) {
                this.minecart.applyEffectsFromBlocks();
                this.minecart.applyEffectsFromBlocks();
            }
            trackIteration.firstIteration = false;
        }
    }

    private Vec3 calculateTrackSpeed(ServerLevel serverLevel, Vec3 vec3, TrackIteration trackIteration, BlockPos blockPos, BlockState blockState, RailShape railShape) {
        Vec3 vec33;
        Vec3 vec332;
        Vec3 vec32 = vec3;
        if (!trackIteration.hasGainedSlopeSpeed && (vec332 = this.calculateSlopeSpeed(vec32, railShape)).horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
            trackIteration.hasGainedSlopeSpeed = true;
            vec32 = vec332;
        }
        if (trackIteration.firstIteration && (vec332 = this.calculatePlayerInputSpeed(vec32)).horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
            trackIteration.hasHalted = true;
            vec32 = vec332;
        }
        if (!trackIteration.hasHalted && (vec332 = this.calculateHaltTrackSpeed(vec32, blockState)).horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
            trackIteration.hasHalted = true;
            vec32 = vec332;
        }
        if (trackIteration.firstIteration && (vec32 = this.minecart.applyNaturalSlowdown(vec32)).lengthSqr() > 0.0) {
            double d = Math.min(vec32.length(), this.minecart.getMaxSpeed(serverLevel));
            vec32 = vec32.normalize().scale(d);
        }
        if (!trackIteration.hasBoosted && (vec33 = this.calculateBoostTrackSpeed(vec32, blockPos, blockState)).horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
            trackIteration.hasBoosted = true;
            vec32 = vec33;
        }
        return vec32;
    }

    private Vec3 calculateSlopeSpeed(Vec3 vec3, RailShape railShape) {
        double d = Math.max(0.0078125, vec3.horizontalDistance() * 0.02);
        if (this.minecart.isInWater()) {
            d *= 0.2;
        }
        return switch (railShape) {
            case RailShape.ASCENDING_EAST -> vec3.add(-d, 0.0, 0.0);
            case RailShape.ASCENDING_WEST -> vec3.add(d, 0.0, 0.0);
            case RailShape.ASCENDING_NORTH -> vec3.add(0.0, 0.0, d);
            case RailShape.ASCENDING_SOUTH -> vec3.add(0.0, 0.0, -d);
            default -> vec3;
        };
    }

    private Vec3 calculatePlayerInputSpeed(Vec3 vec3) {
        Entity entity = this.minecart.getFirstPassenger();
        if (!(entity instanceof ServerPlayer)) {
            return vec3;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        Vec3 vec32 = serverPlayer.getLastClientMoveIntent();
        if (vec32.lengthSqr() > 0.0) {
            Vec3 vec33 = vec32.normalize();
            double d = vec3.horizontalDistanceSqr();
            if (vec33.lengthSqr() > 0.0 && d < 0.01) {
                return vec3.add(new Vec3(vec33.x, 0.0, vec33.z).normalize().scale(0.001));
            }
        }
        return vec3;
    }

    private Vec3 calculateHaltTrackSpeed(Vec3 vec3, BlockState blockState) {
        if (!blockState.is(Blocks.POWERED_RAIL) || blockState.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return vec3;
        }
        if (vec3.length() < 0.03) {
            return Vec3.ZERO;
        }
        return vec3.scale(0.5);
    }

    private Vec3 calculateBoostTrackSpeed(Vec3 vec3, BlockPos blockPos, BlockState blockState) {
        if (!blockState.is(Blocks.POWERED_RAIL) || !blockState.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return vec3;
        }
        if (vec3.length() > 0.01) {
            return vec3.normalize().scale(vec3.length() + 0.06);
        }
        Vec3 vec32 = this.minecart.getRedstoneDirection(blockPos);
        if (vec32.lengthSqr() <= 0.0) {
            return vec3;
        }
        return vec32.scale(vec3.length() + 0.2);
    }

    @Override
    public double stepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
        if (d < (double)1.0E-5f) {
            return 0.0;
        }
        Vec3 vec3 = this.position();
        Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        Vec3 vec32 = this.getDeltaMovement().horizontal();
        if (vec32.length() < (double)1.0E-5f) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        }
        boolean bl = vec3i.getY() != vec3i2.getY();
        Vec3 vec33 = new Vec3(vec3i2).scale(0.5).horizontal();
        Vec3 vec34 = new Vec3(vec3i).scale(0.5).horizontal();
        if (vec32.dot(vec34) < vec32.dot(vec33)) {
            vec34 = vec33;
        }
        Vec3 vec35 = blockPos.getBottomCenter().add(vec34).add(0.0, 0.1, 0.0).add(vec34.normalize().scale(1.0E-5f));
        if (bl && !this.isDecending(vec32, railShape)) {
            vec35 = vec35.add(0.0, 1.0, 0.0);
        }
        Vec3 vec36 = vec35.subtract(this.position()).normalize();
        vec32 = vec36.scale(vec32.length() / vec36.horizontalDistance());
        Vec3 vec37 = vec3.add(vec32.normalize().scale(d * (double)(bl ? Mth.SQRT_OF_TWO : 1.0f)));
        if (vec3.distanceToSqr(vec35) <= vec3.distanceToSqr(vec37)) {
            d = vec35.subtract(vec37).horizontalDistance();
            vec37 = vec35;
        } else {
            d = 0.0;
        }
        this.minecart.move(MoverType.SELF, vec37.subtract(vec3));
        BlockState blockState = this.level().getBlockState(BlockPos.containing(vec37));
        if (bl) {
            RailShape railShape2;
            if (BaseRailBlock.isRail(blockState) && this.restAtVShape(railShape, railShape2 = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty()))) {
                return 0.0;
            }
            double e = vec35.horizontal().distanceTo(this.position().horizontal());
            double f = vec35.y + (this.isDecending(vec32, railShape) ? e : -e);
            if (this.position().y < f) {
                this.setPos(this.position().x, f, this.position().z);
            }
        }
        if (this.position().distanceTo(vec3) < (double)1.0E-5f && vec37.distanceTo(vec3) > (double)1.0E-5f) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        }
        this.setDeltaMovement(vec32);
        return d;
    }

    private boolean restAtVShape(RailShape railShape, RailShape railShape2) {
        if (this.getDeltaMovement().lengthSqr() < 0.005 && railShape2.isSlope() && this.isDecending(this.getDeltaMovement(), railShape) && !this.isDecending(this.getDeltaMovement(), railShape2)) {
            this.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        return false;
    }

    @Override
    public double getMaxSpeed(ServerLevel serverLevel) {
        return (double)serverLevel.getGameRules().get(GameRules.MAX_MINECART_SPEED).intValue() * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
    }

    private boolean isDecending(Vec3 vec3, RailShape railShape) {
        return switch (railShape) {
            case RailShape.ASCENDING_EAST -> {
                if (vec3.x < 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_WEST -> {
                if (vec3.x > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_NORTH -> {
                if (vec3.z > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_SOUTH -> {
                if (vec3.z < 0.0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997 : 0.975;
    }

    @Override
    public boolean pushAndPickupEntities() {
        boolean bl = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2, 0.0, 0.2));
        if (this.minecart.horizontalCollision || this.minecart.verticalCollision) {
            boolean bl2 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7));
            return bl && !bl2;
        }
        return false;
    }

    public boolean pickupEntities(AABB aABB) {
        List<Entity> list;
        if (this.minecart.isRideable() && !this.minecart.isVehicle() && !(list = this.level().getEntities(this.minecart, aABB, EntitySelector.pushableBy(this.minecart))).isEmpty()) {
            for (Entity entity : list) {
                boolean bl;
                if (entity instanceof Player || entity instanceof IronGolem || entity instanceof AbstractMinecart || this.minecart.isVehicle() || entity.isPassenger() || !(bl = entity.startRiding(this.minecart))) continue;
                return true;
            }
        }
        return false;
    }

    public boolean pushEntities(AABB aABB) {
        boolean bl;
        block3: {
            block2: {
                bl = false;
                if (!this.minecart.isRideable()) break block2;
                List<Entity> list = this.level().getEntities(this.minecart, aABB, EntitySelector.pushableBy(this.minecart));
                if (list.isEmpty()) break block3;
                for (Entity entity : list) {
                    if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.minecart.isVehicle() && !entity.isPassenger()) continue;
                    entity.push(this.minecart);
                    bl = true;
                }
                break block3;
            }
            for (Entity entity2 : this.level().getEntities(this.minecart, aABB)) {
                if (this.minecart.hasPassenger(entity2) || !entity2.isPushable() || !(entity2 instanceof AbstractMinecart)) continue;
                entity2.push(this.minecart);
                bl = true;
            }
        }
        return bl;
    }

    public static final class MinecartStep
    extends Record {
        final Vec3 position;
        final Vec3 movement;
        final float yRot;
        final float xRot;
        final float weight;
        public static final StreamCodec<ByteBuf, MinecartStep> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, MinecartStep::position, Vec3.STREAM_CODEC, MinecartStep::movement, ByteBufCodecs.ROTATION_BYTE, MinecartStep::yRot, ByteBufCodecs.ROTATION_BYTE, MinecartStep::xRot, ByteBufCodecs.FLOAT, MinecartStep::weight, MinecartStep::new);
        public static MinecartStep ZERO = new MinecartStep(Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, 0.0f);

        public MinecartStep(Vec3 vec3, Vec3 vec32, float f, float g, float h) {
            this.position = vec3;
            this.movement = vec32;
            this.yRot = f;
            this.xRot = g;
            this.weight = h;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this, object);
        }

        public Vec3 position() {
            return this.position;
        }

        public Vec3 movement() {
            return this.movement;
        }

        public float yRot() {
            return this.yRot;
        }

        public float xRot() {
            return this.xRot;
        }

        public float weight() {
            return this.weight;
        }
    }

    static final class StepPartialTicks
    extends Record {
        final float partialTicksInStep;
        final MinecartStep currentStep;
        final MinecartStep previousStep;

        StepPartialTicks(float f, MinecartStep minecartStep, MinecartStep minecartStep2) {
            this.partialTicksInStep = f;
            this.currentStep = minecartStep;
            this.previousStep = minecartStep2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this, object);
        }

        public float partialTicksInStep() {
            return this.partialTicksInStep;
        }

        public MinecartStep currentStep() {
            return this.currentStep;
        }

        public MinecartStep previousStep() {
            return this.previousStep;
        }
    }

    static class TrackIteration {
        double movementLeft = 0.0;
        boolean firstIteration = true;
        boolean hasGainedSlopeSpeed = false;
        boolean hasHalted = false;
        boolean hasBoosted = false;

        TrackIteration() {
        }

        public boolean shouldIterate() {
            return this.firstIteration || this.movementLeft > (double)1.0E-5f;
        }
    }
}

