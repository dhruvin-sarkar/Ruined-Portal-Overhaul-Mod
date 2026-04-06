/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class Raider
extends PatrollingMonster {
    protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive() && ItemStack.matches(itemEntity.getItem(), Raid.getOminousBannerInstance(itemEntity.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
    private static final int DEFAULT_WAVE = 0;
    private static final boolean DEFAULT_CAN_JOIN_RAID = false;
    protected @Nullable Raid raid;
    private int wave = 0;
    private boolean canJoinRaid = false;
    private int ticksOutsideRaid;

    protected Raider(EntityType<? extends Raider> entityType, Level level) {
        super((EntityType<? extends PatrollingMonster>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ObtainRaidLeaderBannerGoal(this, this));
        this.goalSelector.addGoal(3, new PathfindToRaidGoal<Raider>(this));
        this.goalSelector.addGoal(4, new RaiderMoveThroughVillageGoal(this, 1.05f, 1));
        this.goalSelector.addGoal(5, new RaiderCelebration(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CELEBRATING, false);
    }

    public abstract void applyRaidBuffs(ServerLevel var1, int var2, boolean var3);

    public boolean canJoinRaid() {
        return this.canJoinRaid;
    }

    public void setCanJoinRaid(boolean bl) {
        this.canJoinRaid = bl;
    }

    @Override
    public void aiStep() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.isAlive()) {
                Raid raid = this.getCurrentRaid();
                if (this.canJoinRaid()) {
                    if (raid == null) {
                        Raid raid2;
                        if (this.level().getGameTime() % 20L == 0L && (raid2 = serverLevel.getRaidAt(this.blockPosition())) != null && Raids.canJoinRaid(this)) {
                            raid2.joinRaid(serverLevel, raid2.getGroupsSpawned(), this, null, true);
                        }
                    } else {
                        LivingEntity livingEntity = this.getTarget();
                        if (livingEntity != null && (livingEntity.getType() == EntityType.PLAYER || livingEntity.getType() == EntityType.IRON_GOLEM)) {
                            this.noActionTime = 0;
                        }
                    }
                }
            }
        }
        super.aiStep();
    }

    @Override
    protected void updateNoActionTime() {
        this.noActionTime += 2;
    }

    @Override
    public void die(DamageSource damageSource) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Entity entity = damageSource.getEntity();
            Raid raid = this.getCurrentRaid();
            if (raid != null) {
                if (this.isPatrolLeader()) {
                    raid.removeLeader(this.getWave());
                }
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    raid.addHeroOfTheVillage(entity);
                }
                raid.removeFromRaid(serverLevel, this, false);
            }
        }
        super.die(damageSource);
    }

    @Override
    public boolean canJoinPatrol() {
        return !this.hasActiveRaid();
    }

    public void setCurrentRaid(@Nullable Raid raid) {
        this.raid = raid;
    }

    public @Nullable Raid getCurrentRaid() {
        return this.raid;
    }

    public boolean isCaptain() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
        boolean bl = !itemStack.isEmpty() && ItemStack.matches(itemStack, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
        boolean bl2 = this.isPatrolLeader();
        return bl && bl2;
    }

    public boolean hasRaid() {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return this.getCurrentRaid() != null || serverLevel.getRaidAt(this.blockPosition()) != null;
    }

    public boolean hasActiveRaid() {
        return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
    }

    public void setWave(int i) {
        this.wave = i;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isCelebrating() {
        return this.entityData.get(IS_CELEBRATING);
    }

    public void setCelebrating(boolean bl) {
        this.entityData.set(IS_CELEBRATING, bl);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        Level level;
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Wave", this.wave);
        valueOutput.putBoolean("CanJoinRaid", this.canJoinRaid);
        if (this.raid != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getRaids().getId(this.raid).ifPresent(i -> valueOutput.putInt("RaidId", i));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.wave = valueInput.getIntOr("Wave", 0);
        this.canJoinRaid = valueInput.getBooleanOr("CanJoinRaid", false);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            valueInput.getInt("RaidId").ifPresent(integer -> {
                this.raid = serverLevel.getRaids().get((int)integer);
                if (this.raid != null) {
                    this.raid.addWaveMob(serverLevel, this.wave, this, false);
                    if (this.isPatrolLeader()) {
                        this.raid.setLeader(this.wave, this);
                    }
                }
            });
        }
    }

    @Override
    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        boolean bl;
        ItemStack itemStack = itemEntity.getItem();
        boolean bl2 = bl = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
        if (this.hasActiveRaid() && !bl && ItemStack.matches(itemStack, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)))) {
            EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;
            ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
            double d = this.getDropChances().byEquipment(equipmentSlot);
            if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.spawnAtLocation(serverLevel, itemStack2);
            }
            this.onItemPickup(itemEntity);
            this.setItemSlot(equipmentSlot, itemStack);
            this.take(itemEntity, itemStack.getCount());
            itemEntity.discard();
            this.getCurrentRaid().setLeader(this.getWave(), this);
            this.setPatrolLeader(true);
        } else {
            super.pickUpItem(serverLevel, itemEntity);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        if (this.getCurrentRaid() == null) {
            return super.removeWhenFarAway(d);
        }
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
    }

    public int getTicksOutsideRaid() {
        return this.ticksOutsideRaid;
    }

    public void setTicksOutsideRaid(int i) {
        this.ticksOutsideRaid = i;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.hasActiveRaid()) {
            this.getCurrentRaid().updateBossbar();
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setCanJoinRaid(this.getType() != EntityType.WITCH || entitySpawnReason != EntitySpawnReason.NATURAL);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public abstract SoundEvent getCelebrateSound();

    public static class ObtainRaidLeaderBannerGoal<T extends Raider>
    extends Goal {
        private final T mob;
        private Int2LongOpenHashMap unreachableBannerCache = new Int2LongOpenHashMap();
        private @Nullable Path pathToBanner;
        private @Nullable ItemEntity pursuedBannerItemEntity;
        final /* synthetic */ Raider field_52512;

        public ObtainRaidLeaderBannerGoal(T raider2) {
            this.field_52512 = raider;
            this.mob = raider2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cannotPickUpBanner()) {
                return false;
            }
            Int2LongOpenHashMap int2LongOpenHashMap = new Int2LongOpenHashMap();
            double d = this.field_52512.getAttributeValue(Attributes.FOLLOW_RANGE);
            List<ItemEntity> list = ((Entity)this.mob).level().getEntitiesOfClass(ItemEntity.class, ((Entity)this.mob).getBoundingBox().inflate(d, 8.0, d), ALLOWED_ITEMS);
            for (ItemEntity itemEntity : list) {
                long l = this.unreachableBannerCache.getOrDefault(itemEntity.getId(), Long.MIN_VALUE);
                if (this.field_52512.level().getGameTime() < l) {
                    int2LongOpenHashMap.put(itemEntity.getId(), l);
                    continue;
                }
                Path path = ((Mob)this.mob).getNavigation().createPath(itemEntity, 1);
                if (path != null && path.canReach()) {
                    this.pathToBanner = path;
                    this.pursuedBannerItemEntity = itemEntity;
                    return true;
                }
                int2LongOpenHashMap.put(itemEntity.getId(), this.field_52512.level().getGameTime() + 600L);
            }
            this.unreachableBannerCache = int2LongOpenHashMap;
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.pursuedBannerItemEntity == null || this.pathToBanner == null) {
                return false;
            }
            if (this.pursuedBannerItemEntity.isRemoved()) {
                return false;
            }
            if (this.pathToBanner.isDone()) {
                return false;
            }
            return !this.cannotPickUpBanner();
        }

        private boolean cannotPickUpBanner() {
            if (!((Raider)this.mob).hasActiveRaid()) {
                return true;
            }
            if (((Raider)this.mob).getCurrentRaid().isOver()) {
                return true;
            }
            if (!((PatrollingMonster)this.mob).canBeLeader()) {
                return true;
            }
            if (ItemStack.matches(((LivingEntity)this.mob).getItemBySlot(EquipmentSlot.HEAD), Raid.getOminousBannerInstance(((Entity)this.mob).registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)))) {
                return true;
            }
            Raider raider = this.field_52512.raid.getLeader(((Raider)this.mob).getWave());
            return raider != null && raider.isAlive();
        }

        @Override
        public void start() {
            ((Mob)this.mob).getNavigation().moveTo(this.pathToBanner, (double)1.15f);
        }

        @Override
        public void stop() {
            this.pathToBanner = null;
            this.pursuedBannerItemEntity = null;
        }

        @Override
        public void tick() {
            if (this.pursuedBannerItemEntity != null && this.pursuedBannerItemEntity.closerThan((Entity)this.mob, 1.414)) {
                ((Raider)this.mob).pickUpItem(ObtainRaidLeaderBannerGoal.getServerLevel(this.field_52512.level()), this.pursuedBannerItemEntity);
            }
        }
    }

    static class RaiderMoveThroughVillageGoal
    extends Goal {
        private final Raider raider;
        private final double speedModifier;
        private BlockPos poiPos;
        private final List<BlockPos> visited = Lists.newArrayList();
        private final int distanceToPoi;
        private boolean stuck;

        public RaiderMoveThroughVillageGoal(Raider raider, double d, int i) {
            this.raider = raider;
            this.speedModifier = d;
            this.distanceToPoi = i;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.updateVisited();
            return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
        }

        private boolean isValidRaid() {
            return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
        }

        private boolean hasSuitablePoi() {
            ServerLevel serverLevel = (ServerLevel)this.raider.level();
            BlockPos blockPos = this.raider.blockPosition();
            Optional<BlockPos> optional = serverLevel.getPoiManager().getRandom(holder -> holder.is(PoiTypes.HOME), this::hasNotVisited, PoiManager.Occupancy.ANY, blockPos, 48, this.raider.random);
            if (optional.isEmpty()) {
                return false;
            }
            this.poiPos = optional.get().immutable();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.raider.getNavigation().isDone()) {
                return false;
            }
            return this.raider.getTarget() == null && !this.poiPos.closerToCenterThan(this.raider.position(), this.raider.getBbWidth() + (float)this.distanceToPoi) && !this.stuck;
        }

        @Override
        public void stop() {
            if (this.poiPos.closerToCenterThan(this.raider.position(), this.distanceToPoi)) {
                this.visited.add(this.poiPos);
            }
        }

        @Override
        public void start() {
            super.start();
            this.raider.setNoActionTime(0);
            this.raider.getNavigation().moveTo(this.poiPos.getX(), this.poiPos.getY(), this.poiPos.getZ(), this.speedModifier);
            this.stuck = false;
        }

        @Override
        public void tick() {
            if (this.raider.getNavigation().isDone()) {
                Vec3 vec3 = Vec3.atBottomCenterOf(this.poiPos);
                Vec3 vec32 = DefaultRandomPos.getPosTowards(this.raider, 16, 7, vec3, 0.3141592741012573);
                if (vec32 == null) {
                    vec32 = DefaultRandomPos.getPosTowards(this.raider, 8, 7, vec3, 1.5707963705062866);
                }
                if (vec32 == null) {
                    this.stuck = true;
                    return;
                }
                this.raider.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
            }
        }

        private boolean hasNotVisited(BlockPos blockPos) {
            for (BlockPos blockPos2 : this.visited) {
                if (!Objects.equals(blockPos, blockPos2)) continue;
                return false;
            }
            return true;
        }

        private void updateVisited() {
            if (this.visited.size() > 2) {
                this.visited.remove(0);
            }
        }
    }

    public class RaiderCelebration
    extends Goal {
        private final Raider mob;

        RaiderCelebration(Raider raider2) {
            this.mob = raider2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Raid raid = this.mob.getCurrentRaid();
            return this.mob.isAlive() && this.mob.getTarget() == null && raid != null && raid.isLoss();
        }

        @Override
        public void start() {
            this.mob.setCelebrating(true);
            super.start();
        }

        @Override
        public void stop() {
            this.mob.setCelebrating(false);
            super.stop();
        }

        @Override
        public void tick() {
            if (!this.mob.isSilent() && this.mob.random.nextInt(this.adjustedTickDelay(100)) == 0) {
                Raider.this.makeSound(Raider.this.getCelebrateSound());
            }
            if (!this.mob.isPassenger() && this.mob.random.nextInt(this.adjustedTickDelay(50)) == 0) {
                this.mob.getJumpControl().jump();
            }
            super.tick();
        }
    }

    protected static class HoldGroundAttackGoal
    extends Goal {
        private final Raider mob;
        private final float hostileRadiusSqr;
        public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight().ignoreInvisibilityTesting();

        public HoldGroundAttackGoal(AbstractIllager abstractIllager, float f) {
            this.mob = abstractIllager;
            this.hostileRadiusSqr = f * f;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.mob.getLastHurtByMob();
            return this.mob.getCurrentRaid() == null && this.mob.isPatrolling() && this.mob.getTarget() != null && !this.mob.isAggressive() && (livingEntity == null || livingEntity.getType() != EntityType.PLAYER);
        }

        @Override
        public void start() {
            super.start();
            this.mob.getNavigation().stop();
            List<Raider> list = HoldGroundAttackGoal.getServerLevel(this.mob).getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0));
            for (Raider raider : list) {
                raider.setTarget(this.mob.getTarget());
            }
        }

        @Override
        public void stop() {
            super.stop();
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity != null) {
                List<Raider> list = HoldGroundAttackGoal.getServerLevel(this.mob).getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0));
                for (Raider raider : list) {
                    raider.setTarget(livingEntity);
                    raider.setAggressive(true);
                }
                this.mob.setAggressive(true);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity == null) {
                return;
            }
            if (this.mob.distanceToSqr(livingEntity) > (double)this.hostileRadiusSqr) {
                this.mob.getLookControl().setLookAt(livingEntity, 30.0f, 30.0f);
                if (this.mob.random.nextInt(50) == 0) {
                    this.mob.playAmbientSound();
                }
            } else {
                this.mob.setAggressive(true);
            }
            super.tick();
        }
    }
}

