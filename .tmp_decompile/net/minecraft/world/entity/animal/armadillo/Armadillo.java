/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.armadillo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.ArmadilloAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class Armadillo
extends Animal {
    public static final float BABY_SCALE = 0.6f;
    public static final float MAX_HEAD_ROTATION_EXTENT = 32.5f;
    public static final int SCARE_CHECK_INTERVAL = 80;
    private static final double SCARE_DISTANCE_HORIZONTAL = 7.0;
    private static final double SCARE_DISTANCE_VERTICAL = 2.0;
    private static final EntityDataAccessor<ArmadilloState> ARMADILLO_STATE = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.ARMADILLO_STATE);
    private long inStateTicks = 0L;
    public final AnimationState rollOutAnimationState = new AnimationState();
    public final AnimationState rollUpAnimationState = new AnimationState();
    public final AnimationState peekAnimationState = new AnimationState();
    private int scuteTime;
    private boolean peekReceivedClient = false;

    public Armadillo(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanFloat(true);
        this.scuteTime = this.pickNextScuteDropTime();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.ARMADILLO.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.14);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ARMADILLO_STATE, ArmadilloState.IDLE);
    }

    public boolean isScared() {
        return this.entityData.get(ARMADILLO_STATE) != ArmadilloState.IDLE;
    }

    public boolean shouldHideInShell() {
        return this.getState().shouldHideInShell(this.inStateTicks);
    }

    public boolean shouldSwitchToScaredState() {
        return this.getState() == ArmadilloState.ROLLING && this.inStateTicks > (long)ArmadilloState.ROLLING.animationDuration();
    }

    public ArmadilloState getState() {
        return this.entityData.get(ARMADILLO_STATE);
    }

    public void switchToState(ArmadilloState armadilloState) {
        this.entityData.set(ARMADILLO_STATE, armadilloState);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (ARMADILLO_STATE.equals(entityDataAccessor)) {
            this.inStateTicks = 0L;
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    protected Brain.Provider<Armadillo> brainProvider() {
        return ArmadilloAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return ArmadilloAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("armadilloBrain");
        this.brain.tick(serverLevel, this);
        profilerFiller.pop();
        profilerFiller.push("armadilloActivityUpdate");
        ArmadilloAi.updateActivity(this);
        profilerFiller.pop();
        if (this.isAlive() && --this.scuteTime <= 0 && this.shouldDropLoot(serverLevel)) {
            if (this.dropFromGiftLootTable(serverLevel, BuiltInLootTables.ARMADILLO_SHED, this::spawnAtLocation)) {
                this.playSound(SoundEvents.ARMADILLO_SCUTE_DROP, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.gameEvent(GameEvent.ENTITY_PLACE);
            }
            this.scuteTime = this.pickNextScuteDropTime();
        }
        super.customServerAiStep(serverLevel);
    }

    private int pickNextScuteDropTime() {
        return this.random.nextInt(20 * TimeUtil.SECONDS_PER_MINUTE * 5) + 20 * TimeUtil.SECONDS_PER_MINUTE * 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.isScared()) {
            this.clampHeadRotationToBody();
        }
        ++this.inStateTicks;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.6f : 1.0f;
    }

    private void setupAnimationStates() {
        switch (this.getState().ordinal()) {
            case 0: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.stop();
                this.peekAnimationState.stop();
                break;
            }
            case 3: {
                this.rollOutAnimationState.startIfStopped(this.tickCount);
                this.rollUpAnimationState.stop();
                this.peekAnimationState.stop();
                break;
            }
            case 1: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.startIfStopped(this.tickCount);
                this.peekAnimationState.stop();
                break;
            }
            case 2: {
                this.rollOutAnimationState.stop();
                this.rollUpAnimationState.stop();
                if (this.peekReceivedClient) {
                    this.peekAnimationState.stop();
                    this.peekReceivedClient = false;
                }
                if (this.inStateTicks == 0L) {
                    this.peekAnimationState.start(this.tickCount);
                    this.peekAnimationState.fastForward(ArmadilloState.SCARED.animationDuration(), 1.0f);
                    break;
                }
                this.peekAnimationState.startIfStopped(this.tickCount);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 64 && this.level().isClientSide()) {
            this.peekReceivedClient = true;
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMADILLO_PEEK, this.getSoundSource(), 1.0f, 1.0f, false);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.ARMADILLO_FOOD);
    }

    public static boolean checkArmadilloSpawnRules(EntityType<Armadillo> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.ARMADILLO_SPAWNABLE_ON) && Armadillo.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    public boolean isScaredBy(LivingEntity livingEntity) {
        if (!this.getBoundingBox().inflate(7.0, 2.0, 7.0).intersects(livingEntity.getBoundingBox())) {
            return false;
        }
        if (livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (this.getLastHurtByMob() == livingEntity) {
            return true;
        }
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (player.isSpectator()) {
                return false;
            }
            return player.isSprinting() || player.isPassenger();
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("state", ArmadilloState.CODEC, this.getState());
        valueOutput.putInt("scute_time", this.scuteTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.switchToState(valueInput.read("state", ArmadilloState.CODEC).orElse(ArmadilloState.IDLE));
        valueInput.getInt("scute_time").ifPresent(integer -> {
            this.scuteTime = integer;
        });
    }

    public void rollUp() {
        if (this.isScared()) {
            return;
        }
        this.stopInPlace();
        this.resetLove();
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.ARMADILLO_ROLL);
        this.switchToState(ArmadilloState.ROLLING);
    }

    public void rollOut() {
        if (!this.isScared()) {
            return;
        }
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.ARMADILLO_UNROLL_FINISH);
        this.switchToState(ArmadilloState.IDLE);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isScared()) {
            f = (f - 1.0f) / 2.0f;
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        super.actuallyHurt(serverLevel, damageSource, f);
        if (this.isNoAi() || this.isDeadOrDying()) {
            return;
        }
        if (damageSource.getEntity() instanceof LivingEntity) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, 80L);
            if (this.canStayRolledUp()) {
                this.rollUp();
            }
        } else if (damageSource.is(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES)) {
            this.rollOut();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.BRUSH) && this.brushOffScute(player, itemStack)) {
            itemStack.hurtAndBreak(16, (LivingEntity)player, interactionHand.asEquipmentSlot());
            return InteractionResult.SUCCESS;
        }
        if (this.isScared()) {
            return InteractionResult.FAIL;
        }
        return super.mobInteract(player, interactionHand);
    }

    public boolean brushOffScute(@Nullable Entity entity, ItemStack itemStack) {
        if (this.isBaby()) {
            return false;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.dropFromEntityInteractLootTable(serverLevel, BuiltInLootTables.ARMADILLO_BRUSH, entity, itemStack, this::spawnAtLocation);
            this.playSound(SoundEvents.ARMADILLO_BRUSH);
            this.gameEvent(GameEvent.ENTITY_INTERACT);
        }
        return true;
    }

    public boolean canStayRolledUp() {
        return !this.isPanicking() && !this.isInLiquid() && !this.isLeashed() && !this.isPassenger() && !this.isVehicle();
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.isScared();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScared()) {
            return null;
        }
        return SoundEvents.ARMADILLO_AMBIENT;
    }

    @Override
    protected void playEatingSound() {
        this.makeSound(SoundEvents.ARMADILLO_EAT);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMADILLO_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isScared()) {
            return SoundEvents.ARMADILLO_HURT_REDUCED;
        }
        return SoundEvents.ARMADILLO_HURT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.ARMADILLO_STEP, 0.15f, 1.0f);
    }

    @Override
    public int getMaxHeadYRot() {
        if (this.isScared()) {
            return 0;
        }
        return 32;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this){

            @Override
            public void clientTick() {
                if (!Armadillo.this.isScared()) {
                    super.clientTick();
                }
            }
        };
    }

    public static enum ArmadilloState implements StringRepresentable
    {
        IDLE("idle", false, 0, 0){

            @Override
            public boolean shouldHideInShell(long l) {
                return false;
            }
        }
        ,
        ROLLING("rolling", true, 10, 1){

            @Override
            public boolean shouldHideInShell(long l) {
                return l > 5L;
            }
        }
        ,
        SCARED("scared", true, 50, 2){

            @Override
            public boolean shouldHideInShell(long l) {
                return true;
            }
        }
        ,
        UNROLLING("unrolling", true, 30, 3){

            @Override
            public boolean shouldHideInShell(long l) {
                return l < 26L;
            }
        };

        static final Codec<ArmadilloState> CODEC;
        private static final IntFunction<ArmadilloState> BY_ID;
        public static final StreamCodec<ByteBuf, ArmadilloState> STREAM_CODEC;
        private final String name;
        private final boolean isThreatened;
        private final int animationDuration;
        private final int id;

        ArmadilloState(String string2, boolean bl, int j, int k) {
            this.name = string2;
            this.isThreatened = bl;
            this.animationDuration = j;
            this.id = k;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        private int id() {
            return this.id;
        }

        public abstract boolean shouldHideInShell(long var1);

        public boolean isThreatened() {
            return this.isThreatened;
        }

        public int animationDuration() {
            return this.animationDuration;
        }

        static {
            CODEC = StringRepresentable.fromEnum(ArmadilloState::values);
            BY_ID = ByIdMap.continuous(ArmadilloState::id, ArmadilloState.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ArmadilloState::id);
        }
    }
}

