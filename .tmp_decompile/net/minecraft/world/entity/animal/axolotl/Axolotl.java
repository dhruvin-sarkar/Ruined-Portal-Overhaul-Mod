/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.BinaryAnimator;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.EasingType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Axolotl
extends Animal
implements Bucketable {
    public static final int TOTAL_PLAYDEAD_TIME = 200;
    private static final int POSE_ANIMATION_TICKS = 10;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.FOOD_TEMPTATIONS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT, (Object[])new MemoryModuleType[]{MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryModuleType.IS_PANICKING});
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    public static final double PLAYER_REGEN_DETECTION_RANGE = 20.0;
    public static final int RARE_VARIANT_CHANCE = 1200;
    private static final int AXOLOTL_TOTAL_AIR_SUPPLY = 6000;
    public static final String VARIANT_TAG = "Variant";
    private static final int REHYDRATE_AIR_SUPPLY = 1800;
    private static final int REGEN_BUFF_MAX_DURATION = 2400;
    private static final boolean DEFAULT_FROM_BUCKET = false;
    public final BinaryAnimator playingDeadAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator inWaterAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator onGroundAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    public final BinaryAnimator movingAnimator = new BinaryAnimator(10, EasingType.IN_OUT_SINE);
    private static final int REGEN_BUFF_BASE_DURATION = 100;

    public Axolotl(EntityType<? extends Axolotl> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.moveControl = new AxolotlMoveControl(this);
        this.lookControl = new AxolotlLookControl(this, 20);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_PLAYING_DEAD, false);
        builder.define(FROM_BUCKET, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store(VARIANT_TAG, Variant.LEGACY_CODEC, this.getVariant());
        valueOutput.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setVariant(valueInput.read(VARIANT_TAG, Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
        this.setFromBucket(valueInput.getBooleanOr("FromBucket", false));
    }

    @Override
    public void playAmbientSound() {
        if (this.isPlayingDead()) {
            return;
        }
        super.playAmbientSound();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        boolean bl = false;
        if (entitySpawnReason == EntitySpawnReason.BUCKET) {
            return spawnGroupData;
        }
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (spawnGroupData instanceof AxolotlGroupData) {
            if (((AxolotlGroupData)spawnGroupData).getGroupSize() >= 2) {
                bl = true;
            }
        } else {
            spawnGroupData = new AxolotlGroupData(Variant.getCommonSpawnVariant(randomSource), Variant.getCommonSpawnVariant(randomSource));
        }
        this.setVariant(((AxolotlGroupData)spawnGroupData).getVariant(randomSource));
        if (bl) {
            this.setAge(-24000);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public void baseTick() {
        Level level;
        int i = this.getAirSupply();
        super.baseTick();
        if (!this.isNoAi() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.handleAirSupply(serverLevel, i);
        }
        if (this.level().isClientSide()) {
            this.tickAnimations();
        }
    }

    private void tickAnimations() {
        AnimationState animationState = this.isPlayingDead() ? AnimationState.PLAYING_DEAD : (this.isInWater() ? AnimationState.IN_WATER : (this.onGround() ? AnimationState.ON_GROUND : AnimationState.IN_AIR));
        this.playingDeadAnimator.tick(animationState == AnimationState.PLAYING_DEAD);
        this.inWaterAnimator.tick(animationState == AnimationState.IN_WATER);
        this.onGroundAnimator.tick(animationState == AnimationState.ON_GROUND);
        boolean bl = this.walkAnimation.isMoving() || this.getXRot() != this.xRotO || this.getYRot() != this.yRotO;
        this.movingAnimator.tick(bl);
    }

    protected void handleAirSupply(ServerLevel serverLevel, int i) {
        if (this.isAlive() && !this.isInWaterOrRain()) {
            this.setAirSupply(i - 1);
            if (this.shouldTakeDrowningDamage()) {
                this.setAirSupply(0);
                this.hurtServer(serverLevel, this.damageSources().dryOut(), 2.0f);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    public void rehydrate() {
        int i = this.getAirSupply() + 1800;
        this.setAirSupply(Math.min(i, this.getMaxAirSupply()));
    }

    @Override
    public int getMaxAirSupply() {
        return 6000;
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT));
    }

    private void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, variant.getId());
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.AXOLOTL_VARIANT) {
            return Axolotl.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.AXOLOTL_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.AXOLOTL_VARIANT) {
            this.setVariant(Axolotl.castComponentValue(DataComponents.AXOLOTL_VARIANT, object));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    private static boolean useRareVariant(RandomSource randomSource) {
        return randomSource.nextInt(1200) == 0;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public void setPlayingDead(boolean bl) {
        this.entityData.set(DATA_PLAYING_DEAD, bl);
    }

    public boolean isPlayingDead() {
        return this.entityData.get(DATA_PLAYING_DEAD);
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean bl) {
        this.entityData.set(FROM_BUCKET, bl);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Axolotl axolotl = EntityType.AXOLOTL.create(serverLevel, EntitySpawnReason.BREEDING);
        if (axolotl != null) {
            Variant variant = Axolotl.useRareVariant(this.random) ? Variant.getRareSpawnVariant(this.random) : (this.random.nextBoolean() ? this.getVariant() : ((Axolotl)ageableMob).getVariant());
            axolotl.setVariant(variant);
            axolotl.setPersistenceRequired();
        }
        return axolotl;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.AXOLOTL_FOOD);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("axolotlBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.pop();
        profilerFiller.push("axolotlActivityUpdate");
        AxolotlAi.updateActivity(this);
        profilerFiller.pop();
        if (!this.isNoAi()) {
            Optional<Integer> optional = this.getBrain().getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
            this.setPlayingDead(optional.isPresent() && optional.get() > 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.AXOLOTL_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        float g = this.getHealth();
        if (!this.isNoAi() && this.level().random.nextInt(3) == 0 && ((float)this.level().random.nextInt(3) < f || g / this.getMaxHealth() < 0.5f) && f < g && this.isInWater() && (damageSource.getEntity() != null || damageSource.getDirectEntity() != null) && !this.isPlayingDead()) {
            this.brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, 200);
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return Bucketable.bucketMobPickup(player, interactionHand, this).orElse(super.mobInteract(player, interactionHand));
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
        itemStack.copyFrom(DataComponents.AXOLOTL_VARIANT, this);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> {
            compoundTag.putInt("Age", this.getAge());
            Brain<Axolotl> brain = this.getBrain();
            if (brain.hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
                compoundTag.putLong("HuntingCooldown", brain.getTimeUntilExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
            }
        });
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
        this.setAge(compoundTag.getIntOr("Age", 0));
        compoundTag.getLong("HuntingCooldown").ifPresentOrElse(long_ -> this.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, compoundTag.getLongOr("HuntingCooldown", 0L)), () -> this.getBrain().setMemory(MemoryModuleType.HAS_HUNTING_COOLDOWN, Optional.empty()));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.AXOLOTL_BUCKET);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return !this.isPlayingDead() && super.canBeSeenAsEnemy();
    }

    public static void onStopAttacking(ServerLevel serverLevel, Axolotl axolotl, LivingEntity livingEntity) {
        Entity entity;
        DamageSource damageSource;
        if (livingEntity.isDeadOrDying() && (damageSource = livingEntity.getLastDamageSource()) != null && (entity = damageSource.getEntity()) != null && entity.getType() == EntityType.PLAYER) {
            Player player = (Player)entity;
            List<Player> list = serverLevel.getEntitiesOfClass(Player.class, axolotl.getBoundingBox().inflate(20.0));
            if (list.contains(player)) {
                axolotl.applySupportingEffects(player);
            }
        }
    }

    public void applySupportingEffects(Player player) {
        MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.REGENERATION);
        if (mobEffectInstance == null || mobEffectInstance.endsWithin(2399)) {
            int i = mobEffectInstance != null ? mobEffectInstance.getDuration() : 0;
            int j = Math.min(2400, 100 + i);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, j, 0), this);
        }
        player.removeEffect(MobEffects.MINING_FATIGUE);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.AXOLOTL_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    protected Brain.Provider<Axolotl> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return AxolotlAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Axolotl> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void travelInWater(Vec3 vec3, double d, boolean bl, double e) {
        this.moveRelative(this.getSpeed(), vec3);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
    }

    @Override
    protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (itemStack.is(Items.TROPICAL_FISH_BUCKET)) {
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.WATER_BUCKET)));
        } else {
            super.usePlayerItem(player, interactionHand, itemStack);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    public static boolean checkAxolotlSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor serverLevelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return serverLevelAccessor.getBlockState(blockPos.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON);
    }

    static class AxolotlMoveControl
    extends SmoothSwimmingMoveControl {
        private final Axolotl axolotl;

        public AxolotlMoveControl(Axolotl axolotl) {
            super(axolotl, 85, 10, 0.1f, 0.5f, false);
            this.axolotl = axolotl;
        }

        @Override
        public void tick() {
            if (!this.axolotl.isPlayingDead()) {
                super.tick();
            }
        }
    }

    class AxolotlLookControl
    extends SmoothSwimmingLookControl {
        public AxolotlLookControl(Axolotl axolotl2, int i) {
            super(axolotl2, i);
        }

        @Override
        public void tick() {
            if (!Axolotl.this.isPlayingDead()) {
                super.tick();
            }
        }
    }

    public static enum Variant implements StringRepresentable
    {
        LUCY(0, "lucy", true),
        WILD(1, "wild", true),
        GOLD(2, "gold", true),
        CYAN(3, "cyan", true),
        BLUE(4, "blue", false);

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> LEGACY_CODEC;
        private final int id;
        private final String name;
        private final boolean common;

        private Variant(int j, String string2, boolean bl) {
            this.id = j;
            this.name = string2;
            this.common = bl;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Variant byId(int i) {
            return BY_ID.apply(i);
        }

        public static Variant getCommonSpawnVariant(RandomSource randomSource) {
            return Variant.getSpawnVariant(randomSource, true);
        }

        public static Variant getRareSpawnVariant(RandomSource randomSource) {
            return Variant.getSpawnVariant(randomSource, false);
        }

        private static Variant getSpawnVariant(RandomSource randomSource, boolean bl) {
            Variant[] variants = (Variant[])Arrays.stream(Variant.values()).filter(variant -> variant.common == bl).toArray(Variant[]::new);
            return Util.getRandom(variants, randomSource);
        }

        static {
            DEFAULT = LUCY;
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::getId);
        }
    }

    public static class AxolotlGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant[] types;

        public AxolotlGroupData(Variant ... variants) {
            super(false);
            this.types = variants;
        }

        public Variant getVariant(RandomSource randomSource) {
            return this.types[randomSource.nextInt(this.types.length)];
        }
    }

    public static enum AnimationState {
        PLAYING_DEAD,
        IN_WATER,
        ON_GROUND,
        IN_AIR;

    }
}

