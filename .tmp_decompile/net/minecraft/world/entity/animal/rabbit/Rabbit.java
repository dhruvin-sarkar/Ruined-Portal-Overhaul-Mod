/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.rabbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Rabbit
extends Animal {
    public static final double STROLL_SPEED_MOD = 0.6;
    public static final double BREED_SPEED_MOD = 0.8;
    public static final double FOLLOW_SPEED_MOD = 1.0;
    public static final double FLEE_SPEED_MOD = 2.2;
    public static final double ATTACK_SPEED_MOD = 1.4;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final int DEFAULT_MORE_CARROT_TICKS = 0;
    private static final Identifier KILLER_BUNNY = Identifier.withDefaultNamespace("killer_bunny");
    private static final int DEFAULT_ATTACK_POWER = 3;
    private static final int EVIL_ATTACK_POWER_INCREMENT = 5;
    private static final Identifier EVIL_ATTACK_POWER_MODIFIER = Identifier.withDefaultNamespace("evil");
    private static final int EVIL_ARMOR_VALUE = 8;
    private static final int MORE_CARROTS_DELAY = 40;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    int moreCarrotTicks = 0;

    public Rabbit(EntityType<? extends Rabbit> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(1, new RabbitPanicGoal(this, 2.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, itemStack -> itemStack.is(ItemTags.RABBIT_FOOD), false));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Player>(this, Player.class, 8.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Wolf>(this, Wolf.class, 10.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Monster>(this, Monster.class, 4.0f, 2.2, 2.2));
        this.goalSelector.addGoal(5, new RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override
    protected float getJumpPower() {
        Path path;
        float f = 0.3f;
        if (this.moveControl.getSpeedModifier() <= 0.6) {
            f = 0.2f;
        }
        if ((path = this.navigation.getPath()) != null && !path.isDone()) {
            Vec3 vec3 = path.getNextEntityPos(this);
            if (vec3.y > this.getY() + 0.5) {
                f = 0.5f;
            }
        }
        if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + 0.5) {
            f = 0.5f;
        }
        return super.getJumpPower(f / 0.42f);
    }

    @Override
    public void jumpFromGround() {
        double e;
        super.jumpFromGround();
        double d = this.moveControl.getSpeedModifier();
        if (d > 0.0 && (e = this.getDeltaMovement().horizontalDistanceSqr()) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)1);
        }
    }

    public float getJumpCompletion(float f) {
        if (this.jumpDuration == 0) {
            return 0.0f;
        }
        return ((float)this.jumpTicks + f) / (float)this.jumpDuration;
    }

    public void setSpeedModifier(double d) {
        this.getNavigation().setSpeedModifier(d);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), d);
    }

    @Override
    public void setJumping(boolean bl) {
        super.setJumping(bl);
        if (bl) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
        }
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE_ID, Variant.DEFAULT.id);
    }

    @Override
    public void customServerAiStep(ServerLevel serverLevel) {
        if (this.jumpDelayTicks > 0) {
            --this.jumpDelayTicks;
        }
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
        if (this.onGround()) {
            RabbitJumpControl rabbitJumpControl;
            LivingEntity livingEntity;
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }
            if (this.getVariant() == Variant.EVIL && this.jumpDelayTicks == 0 && (livingEntity = this.getTarget()) != null && this.distanceToSqr(livingEntity) < 16.0) {
                this.facePoint(livingEntity.getX(), livingEntity.getZ());
                this.moveControl.setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), this.moveControl.getSpeedModifier());
                this.startJumping();
                this.wasOnGround = true;
            }
            if (!(rabbitJumpControl = (RabbitJumpControl)this.jumpControl).wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (path != null && !path.isDone()) {
                        vec3 = path.getNextEntityPos(this);
                    }
                    this.facePoint(vec3.x, vec3.z);
                    this.startJumping();
                }
            } else if (!rabbitJumpControl.canJump()) {
                this.enableJumpControl();
            }
        }
        this.wasOnGround = this.onGround();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double d, double e) {
        this.setYRot((float)(Mth.atan2(e - this.getZ(), d - this.getX()) * 57.2957763671875) - 90.0f);
    }

    private void enableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(false);
    }

    private void setLandingDelay() {
        this.jumpDelayTicks = this.moveControl.getSpeedModifier() < 2.2 ? 10 : 1;
    }

    private void checkLandingDelay() {
        this.setLandingDelay();
        this.disableJumpControl();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("RabbitType", Variant.LEGACY_CODEC, this.getVariant());
        valueOutput.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setVariant(valueInput.read("RabbitType", Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
        this.moreCarrotTicks = valueInput.getIntOr("MoreCarrotTicks", 0);
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.RABBIT_JUMP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    public void playAttackSound() {
        if (this.getVariant() == Variant.EVIL) {
            this.playSound(SoundEvents.RABBIT_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    @Override
    public SoundSource getSoundSource() {
        return this.getVariant() == Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public @Nullable Rabbit getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        block2: {
            block3: {
                rabbit = EntityType.RABBIT.create(serverLevel, EntitySpawnReason.BREEDING);
                if (rabbit == null) break block2;
                variant = Rabbit.getRandomRabbitVariant(serverLevel, this.blockPosition());
                if (this.random.nextInt(20) == 0) break block3;
                if (!(ageableMob instanceof Rabbit)) ** GOTO lbl-1000
                rabbit2 = (Rabbit)ageableMob;
                if (this.random.nextBoolean()) {
                    variant = rabbit2.getVariant();
                } else lbl-1000:
                // 2 sources

                {
                    variant = this.getVariant();
                }
            }
            rabbit.setVariant(variant);
        }
        return rabbit;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.RABBIT_FOOD);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setVariant(Variant variant) {
        if (variant == Variant.EVIL) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.4, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Wolf>((Mob)this, Wolf.class, true));
            this.getAttribute(Attributes.ATTACK_DAMAGE).addOrUpdateTransientModifier(new AttributeModifier(EVIL_ATTACK_POWER_MODIFIER, 5.0, AttributeModifier.Operation.ADD_VALUE));
            if (!this.hasCustomName()) {
                this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
            }
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(EVIL_ATTACK_POWER_MODIFIER);
        }
        this.entityData.set(DATA_TYPE_ID, variant.id);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.RABBIT_VARIANT) {
            return Rabbit.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.RABBIT_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.RABBIT_VARIANT) {
            this.setVariant(Rabbit.castComponentValue(DataComponents.RABBIT_VARIANT, object));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        Variant variant = Rabbit.getRandomRabbitVariant(serverLevelAccessor, this.blockPosition());
        if (spawnGroupData instanceof RabbitGroupData) {
            variant = ((RabbitGroupData)spawnGroupData).variant;
        } else {
            spawnGroupData = new RabbitGroupData(variant);
        }
        this.setVariant(variant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    private static Variant getRandomRabbitVariant(LevelAccessor levelAccessor, BlockPos blockPos) {
        Holder<Biome> holder = levelAccessor.getBiome(blockPos);
        int i = levelAccessor.getRandom().nextInt(100);
        if (holder.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
            return i < 80 ? Variant.WHITE : Variant.WHITE_SPLOTCHED;
        }
        if (holder.is(BiomeTags.SPAWNS_GOLD_RABBITS)) {
            return Variant.GOLD;
        }
        return i < 50 ? Variant.BROWN : (i < 90 ? Variant.SALT : Variant.BLACK);
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && Rabbit.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    boolean wantsMoreFood() {
        return this.moreCarrotTicks <= 0;
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 1) {
            this.spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static class RabbitJumpControl
    extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean bl) {
            this.canJump = bl;
        }

        @Override
        public void tick() {
            if (this.jump) {
                this.rabbit.startJumping();
                this.jump = false;
            }
        }
    }

    static class RabbitMoveControl
    extends MoveControl {
        private final Rabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveControl(Rabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            if (this.rabbit.onGround() && !this.rabbit.jumping && !((RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
                this.rabbit.setSpeedModifier(0.0);
            } else if (this.hasWanted() || this.operation == MoveControl.Operation.JUMPING) {
                this.rabbit.setSpeedModifier(this.nextJumpSpeed);
            }
            super.tick();
        }

        @Override
        public void setWantedPosition(double d, double e, double f, double g) {
            if (this.rabbit.isInWater()) {
                g = 1.5;
            }
            super.setWantedPosition(d, e, f, g);
            if (g > 0.0) {
                this.nextJumpSpeed = g;
            }
        }
    }

    static class RabbitPanicGoal
    extends PanicGoal {
        private final Rabbit rabbit;

        public RabbitPanicGoal(Rabbit rabbit, double d) {
            super(rabbit, d);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.setSpeedModifier(this.speedModifier);
        }
    }

    static class RabbitAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Rabbit rabbit;

        public RabbitAvoidEntityGoal(Rabbit rabbit, Class<T> class_, float f, double d, double e) {
            super(rabbit, class_, f, d, e);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canUse() {
            return this.rabbit.getVariant() != Variant.EVIL && super.canUse();
        }
    }

    static class RaidGardenGoal
    extends MoveToBlockGoal {
        private final Rabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public RaidGardenGoal(Rabbit rabbit) {
            super(rabbit, 0.7f, 16);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canUse() {
            if (this.nextStartTick <= 0) {
                if (!RaidGardenGoal.getServerLevel(this.rabbit).getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                    return false;
                }
                this.canRaid = false;
                this.wantsToRaid = this.rabbit.wantsMoreFood();
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canRaid && super.canContinueToUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.getLookControl().setLookAt((double)this.blockPos.getX() + 0.5, this.blockPos.getY() + 1, (double)this.blockPos.getZ() + 0.5, 10.0f, this.rabbit.getMaxHeadXRot());
            if (this.isReachedTarget()) {
                Level level = this.rabbit.level();
                BlockPos blockPos = this.blockPos.above();
                BlockState blockState = level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (this.canRaid && block instanceof CarrotBlock) {
                    int i = blockState.getValue(CarrotBlock.AGE);
                    if (i == 0) {
                        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                        level.destroyBlock(blockPos, true, this.rabbit);
                    } else {
                        level.setBlock(blockPos, (BlockState)blockState.setValue(CarrotBlock.AGE, i - 1), 2);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(this.rabbit));
                        level.levelEvent(2001, blockPos, Block.getId(blockState));
                    }
                    this.rabbit.moreCarrotTicks = 40;
                }
                this.canRaid = false;
                this.nextStartTick = 10;
            }
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            if (blockState.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid && (blockState = levelReader.getBlockState(blockPos.above())).getBlock() instanceof CarrotBlock && ((CarrotBlock)blockState.getBlock()).isMaxAge(blockState)) {
                this.canRaid = true;
                return true;
            }
            return false;
        }
    }

    public static enum Variant implements StringRepresentable
    {
        BROWN(0, "brown"),
        WHITE(1, "white"),
        BLACK(2, "black"),
        WHITE_SPLOTCHED(3, "white_splotched"),
        GOLD(4, "gold"),
        SALT(5, "salt"),
        EVIL(99, "evil");

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> BY_ID;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> LEGACY_CODEC;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        final int id;
        private final String name;

        private Variant(int j, String string2) {
            this.id = j;
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int id() {
            return this.id;
        }

        public static Variant byId(int i) {
            return BY_ID.apply(i);
        }

        static {
            DEFAULT = BROWN;
            BY_ID = ByIdMap.sparse(Variant::id, Variant.values(), DEFAULT);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::id);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::id);
        }
    }

    public static class RabbitGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public RabbitGroupData(Variant variant) {
            super(1.0f);
            this.variant = variant;
        }
    }
}

