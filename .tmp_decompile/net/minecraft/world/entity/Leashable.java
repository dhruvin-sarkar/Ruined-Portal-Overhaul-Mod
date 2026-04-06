/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface Leashable {
    public static final String LEASH_TAG = "leash";
    public static final double LEASH_TOO_FAR_DIST = 12.0;
    public static final double LEASH_ELASTIC_DIST = 6.0;
    public static final double MAXIMUM_ALLOWED_LEASHED_DIST = 16.0;
    public static final Vec3 AXIS_SPECIFIC_ELASTICITY = new Vec3(0.8, 0.2, 0.8);
    public static final float SPRING_DAMPENING = 0.7f;
    public static final double TORSIONAL_ELASTICITY = 10.0;
    public static final double STIFFNESS = 0.11;
    public static final List<Vec3> ENTITY_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.5));
    public static final List<Vec3> LEASHER_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.0));
    public static final List<Vec3> SHARED_QUAD_ATTACHMENT_POINTS = ImmutableList.of((Object)new Vec3(-0.5, 0.5, 0.5), (Object)new Vec3(-0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, 0.5));

    public @Nullable LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canHaveALeashAttachedTo(Entity entity) {
        if (this == entity) {
            return false;
        }
        if (this.leashDistanceTo(entity) > this.leashSnapDistance()) {
            return false;
        }
        return this.canBeLeashed();
    }

    default public double leashDistanceTo(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(((Entity)((Object)this)).getBoundingBox().getCenter());
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setDelayedLeashHolderId(int i) {
        this.setLeashData(new LeashData(i));
        Leashable.dropLeash((Entity)((Object)this), false, false);
    }

    default public void readLeashData(ValueInput valueInput) {
        LeashData leashData = valueInput.read(LEASH_TAG, LeashData.CODEC).orElse(null);
        if (this.getLeashData() != null && leashData == null) {
            this.removeLeash();
        }
        this.setLeashData(leashData);
    }

    default public void writeLeashData(ValueOutput valueOutput, @Nullable LeashData leashData) {
        valueOutput.storeNullable(LEASH_TAG, LeashData.CODEC, leashData);
    }

    private static <E extends Entity> void restoreLeashFromSave(E entity, LeashData leashData) {
        Level level;
        if (leashData.delayedLeashInfo != null && (level = entity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Optional optional = leashData.delayedLeashInfo.left();
            Optional optional2 = leashData.delayedLeashInfo.right();
            if (optional.isPresent()) {
                Entity entity2 = serverLevel.getEntity((UUID)optional.get());
                if (entity2 != null) {
                    Leashable.setLeashedTo(entity, entity2, true);
                    return;
                }
            } else if (optional2.isPresent()) {
                Leashable.setLeashedTo(entity, LeashFenceKnotEntity.getOrCreateKnot(serverLevel, (BlockPos)optional2.get()), true);
                return;
            }
            if (entity.tickCount > 100) {
                entity.spawnAtLocation(serverLevel, Items.LEAD);
                ((Leashable)((Object)entity)).setLeashData(null);
            }
        }
    }

    default public void dropLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, true);
    }

    default public void removeLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, false);
    }

    default public void onLeashRemoved() {
    }

    private static <E extends Entity> void dropLeash(E entity, boolean bl, boolean bl2) {
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.leashHolder != null) {
            ((Leashable)((Object)entity)).setLeashData(null);
            ((Leashable)((Object)entity)).onLeashRemoved();
            Level level = entity.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (bl2) {
                    entity.spawnAtLocation(serverLevel, Items.LEAD);
                }
                if (bl) {
                    serverLevel.getChunkSource().sendToTrackingPlayers(entity, new ClientboundSetEntityLinkPacket(entity, null));
                }
                leashData.leashHolder.notifyLeasheeRemoved((Leashable)((Object)entity));
            }
        }
    }

    public static <E extends Entity> void tickLeash(ServerLevel serverLevel, E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.delayedLeashInfo != null) {
            Leashable.restoreLeashFromSave(entity, leashData);
        }
        if (leashData == null || leashData.leashHolder == null) {
            return;
        }
        if (!entity.canInteractWithLevel() || !leashData.leashHolder.canInteractWithLevel()) {
            if (serverLevel.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
                ((Leashable)((Object)entity)).dropLeash();
            } else {
                ((Leashable)((Object)entity)).removeLeash();
            }
        }
        if ((entity2 = ((Leashable)((Object)entity)).getLeashHolder()) != null && entity2.level() == entity.level()) {
            double d = ((Leashable)((Object)entity)).leashDistanceTo(entity2);
            ((Leashable)((Object)entity)).whenLeashedTo(entity2);
            if (d > ((Leashable)((Object)entity)).leashSnapDistance()) {
                serverLevel.playSound(null, entity2.getX(), entity2.getY(), entity2.getZ(), SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
                ((Leashable)((Object)entity)).leashTooFarBehaviour();
            } else if (d > ((Leashable)((Object)entity)).leashElasticDistance() - (double)entity2.getBbWidth() - (double)entity.getBbWidth() && ((Leashable)((Object)entity)).checkElasticInteractions(entity2, leashData)) {
                ((Leashable)((Object)entity)).onElasticLeashPull();
            } else {
                ((Leashable)((Object)entity)).closeRangeLeashBehaviour(entity2);
            }
            entity.setYRot((float)((double)entity.getYRot() - leashData.angularMomentum));
            leashData.angularMomentum *= (double)Leashable.angularFriction(entity);
        }
    }

    default public void onElasticLeashPull() {
        Entity entity = (Entity)((Object)this);
        entity.checkFallDistanceAccumulation();
    }

    default public double leashSnapDistance() {
        return 12.0;
    }

    default public double leashElasticDistance() {
        return 6.0;
    }

    public static <E extends Entity> float angularFriction(E entity) {
        if (entity.onGround()) {
            return entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91f;
        }
        if (entity.isInLiquid()) {
            return 0.8f;
        }
        return 0.91f;
    }

    default public void whenLeashedTo(Entity entity) {
        entity.notifyLeashHolder(this);
    }

    default public void leashTooFarBehaviour() {
        this.dropLeash();
    }

    default public void closeRangeLeashBehaviour(Entity entity) {
    }

    default public boolean checkElasticInteractions(Entity entity, LeashData leashData) {
        boolean bl = entity.supportQuadLeashAsHolder() && this.supportQuadLeash();
        List<Wrench> list = Leashable.computeElasticInteraction((Entity)((Object)this), entity, bl ? SHARED_QUAD_ATTACHMENT_POINTS : ENTITY_ATTACHMENT_POINT, bl ? SHARED_QUAD_ATTACHMENT_POINTS : LEASHER_ATTACHMENT_POINT);
        if (list.isEmpty()) {
            return false;
        }
        Wrench wrench = Wrench.accumulate(list).scale(bl ? 0.25 : 1.0);
        leashData.angularMomentum += 10.0 * wrench.torque();
        Vec3 vec3 = Leashable.getHolderMovement(entity).subtract(((Entity)((Object)this)).getKnownMovement());
        ((Entity)((Object)this)).addDeltaMovement(wrench.force().multiply(AXIS_SPECIFIC_ELASTICITY).add(vec3.scale(0.11)));
        return true;
    }

    private static Vec3 getHolderMovement(Entity entity) {
        Mob mob;
        if (entity instanceof Mob && (mob = (Mob)entity).isNoAi()) {
            return Vec3.ZERO;
        }
        return entity.getKnownMovement();
    }

    private static <E extends Entity> List<Wrench> computeElasticInteraction(E entity, Entity entity2, List<Vec3> list, List<Vec3> list2) {
        double d = ((Leashable)((Object)entity)).leashElasticDistance();
        Vec3 vec3 = Leashable.getHolderMovement(entity);
        float f = entity.getYRot() * ((float)Math.PI / 180);
        Vec3 vec32 = new Vec3(entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth());
        float g = entity2.getYRot() * ((float)Math.PI / 180);
        Vec3 vec33 = new Vec3(entity2.getBbWidth(), entity2.getBbHeight(), entity2.getBbWidth());
        ArrayList<Wrench> list3 = new ArrayList<Wrench>();
        for (int i = 0; i < list.size(); ++i) {
            Vec3 vec34 = list.get(i).multiply(vec32).yRot(-f);
            Vec3 vec35 = entity.position().add(vec34);
            Vec3 vec36 = list2.get(i).multiply(vec33).yRot(-g);
            Vec3 vec37 = entity2.position().add(vec36);
            Leashable.computeDampenedSpringInteraction(vec37, vec35, d, vec3, vec34).ifPresent(list3::add);
        }
        return list3;
    }

    private static Optional<Wrench> computeDampenedSpringInteraction(Vec3 vec3, Vec3 vec32, double d, Vec3 vec33, Vec3 vec34) {
        boolean bl;
        double e = vec32.distanceTo(vec3);
        if (e < d) {
            return Optional.empty();
        }
        Vec3 vec35 = vec3.subtract(vec32).normalize().scale(e - d);
        double f = Wrench.torqueFromForce(vec34, vec35);
        boolean bl2 = bl = vec33.dot(vec35) >= 0.0;
        if (bl) {
            vec35 = vec35.scale(0.3f);
        }
        return Optional.of(new Wrench(vec35, f));
    }

    default public boolean supportQuadLeash() {
        return false;
    }

    default public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets((Entity)((Object)this), 0.0, 0.5, 0.5, 0.5);
    }

    public static Vec3[] createQuadLeashOffsets(Entity entity, double d, double e, double f, double g) {
        float h = entity.getBbWidth();
        double i = d * (double)h;
        double j = e * (double)h;
        double k = f * (double)h;
        double l = g * (double)entity.getBbHeight();
        return new Vec3[]{new Vec3(-k, l, j + i), new Vec3(-k, l, -j + i), new Vec3(k, l, -j + i), new Vec3(k, l, j + i)};
    }

    default public Vec3 getLeashOffset(float f) {
        return this.getLeashOffset();
    }

    default public Vec3 getLeashOffset() {
        Entity entity = (Entity)((Object)this);
        return new Vec3(0.0, entity.getEyeHeight(), entity.getBbWidth() * 0.4f);
    }

    default public void setLeashedTo(Entity entity, boolean bl) {
        if (this == entity) {
            return;
        }
        Leashable.setLeashedTo((Entity)((Object)this), entity, bl);
    }

    private static <E extends Entity> void setLeashedTo(E entity, Entity entity2, boolean bl) {
        Level level;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            leashData = new LeashData(entity2);
            ((Leashable)((Object)entity)).setLeashData(leashData);
        } else {
            Entity entity3 = leashData.leashHolder;
            leashData.setLeashHolder(entity2);
            if (entity3 != null && entity3 != entity2) {
                entity3.notifyLeasheeRemoved((Leashable)((Object)entity));
            }
        }
        if (bl && (level = entity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getChunkSource().sendToTrackingPlayers(entity, new ClientboundSetEntityLinkPacket(entity, entity2));
        }
        if (entity.isPassenger()) {
            entity.stopRiding();
        }
    }

    default public @Nullable Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    private static <E extends Entity> @Nullable Entity getLeashHolder(E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            return null;
        }
        if (leashData.delayedLeashHolderId != 0 && entity.level().isClientSide() && (entity2 = entity.level().getEntity(leashData.delayedLeashHolderId)) instanceof Entity) {
            Entity entity22 = entity2;
            leashData.setLeashHolder(entity22);
        }
        return leashData.leashHolder;
    }

    public static List<Leashable> leashableLeashedTo(Entity entity) {
        return Leashable.leashableInArea(entity, leashable -> leashable.getLeashHolder() == entity);
    }

    public static List<Leashable> leashableInArea(Entity entity, Predicate<Leashable> predicate) {
        return Leashable.leashableInArea(entity.level(), entity.getBoundingBox().getCenter(), predicate);
    }

    public static List<Leashable> leashableInArea(Level level, Vec3 vec3, Predicate<Leashable> predicate) {
        double d = 32.0;
        AABB aABB = AABB.ofSize(vec3, 32.0, 32.0, 32.0);
        return level.getEntitiesOfClass(Entity.class, aABB, entity -> {
            Leashable leashable;
            return entity instanceof Leashable && predicate.test(leashable = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }

    public static final class LeashData {
        public static final Codec<LeashData> CODEC = Codec.xor((Codec)UUIDUtil.CODEC.fieldOf("UUID").codec(), BlockPos.CODEC).xmap(LeashData::new, leashData -> {
            Entity entity = leashData.leashHolder;
            if (entity instanceof LeashFenceKnotEntity) {
                LeashFenceKnotEntity leashFenceKnotEntity = (LeashFenceKnotEntity)entity;
                return Either.right((Object)leashFenceKnotEntity.getPos());
            }
            if (leashData.leashHolder != null) {
                return Either.left((Object)leashData.leashHolder.getUUID());
            }
            return Objects.requireNonNull(leashData.delayedLeashInfo, "Invalid LeashData had no attachment");
        });
        int delayedLeashHolderId;
        public @Nullable Entity leashHolder;
        public @Nullable Either<UUID, BlockPos> delayedLeashInfo;
        public double angularMomentum;

        private LeashData(Either<UUID, BlockPos> either) {
            this.delayedLeashInfo = either;
        }

        LeashData(Entity entity) {
            this.leashHolder = entity;
        }

        LeashData(int i) {
            this.delayedLeashHolderId = i;
        }

        public void setLeashHolder(Entity entity) {
            this.leashHolder = entity;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }

    public record Wrench(Vec3 force, double torque) {
        static Wrench ZERO = new Wrench(Vec3.ZERO, 0.0);

        static double torqueFromForce(Vec3 vec3, Vec3 vec32) {
            return vec3.z * vec32.x - vec3.x * vec32.z;
        }

        static Wrench accumulate(List<Wrench> list) {
            if (list.isEmpty()) {
                return ZERO;
            }
            double d = 0.0;
            double e = 0.0;
            double f = 0.0;
            double g = 0.0;
            for (Wrench wrench : list) {
                Vec3 vec3 = wrench.force;
                d += vec3.x;
                e += vec3.y;
                f += vec3.z;
                g += wrench.torque;
            }
            return new Wrench(new Vec3(d, e, f), g);
        }

        public Wrench scale(double d) {
            return new Wrench(this.force.scale(d), this.torque * d);
        }
    }
}

