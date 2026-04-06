/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Llama
extends AbstractChestedHorse
implements RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.LLAMA.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.LLAMA.getHeight() - 0.8125f, -0.3f)).scale(0.5f);
    boolean didSpit;
    private @Nullable Llama caravanHead;
    private @Nullable Llama caravanTail;

    public Llama(EntityType<? extends Llama> entityType, Level level) {
        super((EntityType<? extends AbstractChestedHorse>)entityType, level);
        this.getNavigation().setRequiredPathLength(40.0f);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int i) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, i)));
    }

    private void setRandomStrength(RandomSource randomSource) {
        int i = randomSource.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + randomSource.nextInt(i));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("Variant", Variant.LEGACY_CODEC, this.getVariant());
        valueOutput.putInt("Strength", this.getStrength());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setStrength(valueInput.getIntOr("Strength", 0));
        super.readAdditionalSaveData(valueInput);
        this.setVariant(valueInput.read("Variant", Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1f));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, itemStack -> itemStack.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Llama.createBaseChestedHorseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STRENGTH_ID, 0);
        builder.define(DATA_VARIANT_ID, 0);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    private void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.LLAMA_VARIANT) {
            return Llama.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.LLAMA_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.LLAMA_VARIANT) {
            this.setVariant(Llama.castComponentValue(DataComponents.LLAMA_VARIANT, object));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack itemStack) {
        SoundEvent soundEvent;
        int i = 0;
        int j = 0;
        float f = 0.0f;
        boolean bl = false;
        if (itemStack.is(Items.WHEAT)) {
            i = 10;
            j = 3;
            f = 2.0f;
        } else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
            i = 90;
            j = 6;
            f = 10.0f;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
                bl = true;
                this.setInLove(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && i > 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide()) {
                this.ageUp(i);
                bl = true;
            }
        }
        if (!(j <= 0 || !bl && this.isTamed() || this.getTemper() >= this.getMaxTemper() || this.level().isClientSide())) {
            this.modifyTemper(j);
            bl = true;
        }
        if (bl && !this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return bl;
    }

    @Override
    public boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        Variant variant;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.setRandomStrength(randomSource);
        if (spawnGroupData instanceof LlamaGroupData) {
            variant = ((LlamaGroupData)spawnGroupData).variant;
        } else {
            variant = Util.getRandom(Variant.values(), randomSource);
            spawnGroupData = new LlamaGroupData(variant);
        }
        this.setVariant(variant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal animal) {
        return animal != this && animal instanceof Llama && this.canParent() && ((Llama)animal).canParent();
    }

    @Override
    public @Nullable Llama getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Llama llama = this.makeNewLlama();
        if (llama != null) {
            this.setOffspringAttributes(ageableMob, llama);
            Llama llama2 = (Llama)ageableMob;
            int i = this.random.nextInt(Math.max(this.getStrength(), llama2.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03f) {
                ++i;
            }
            llama.setStrength(i);
            llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama2.getVariant());
        }
        return llama;
    }

    protected @Nullable Llama makeNewLlama() {
        return EntityType.LLAMA.create(this.level(), EntitySpawnReason.BREEDING);
    }

    private void spit(LivingEntity livingEntity) {
        LlamaSpit llamaSpit = new LlamaSpit(this.level(), this);
        double d = livingEntity.getX() - this.getX();
        double e = livingEntity.getY(0.3333333333333333) - llamaSpit.getY();
        double f = livingEntity.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2f;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot(llamaSpit, serverLevel, ItemStack.EMPTY, d, e + g, f, 1.5f, 10.0f);
        }
        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.didSpit = true;
    }

    void setDidSpit(boolean bl) {
        this.didSpit = bl;
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        int i = this.calculateFallDamage(d, f);
        if (i <= 0) {
            return false;
        }
        if (d >= 6.0) {
            this.hurt(damageSource, i);
            this.propagateFallToPassengers(d, f, damageSource);
        }
        this.playBlockFallSound();
        return true;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(Llama llama) {
        this.caravanHead = llama;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    public @Nullable Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    public boolean supportQuadLeash() {
        return false;
    }

    @Override
    protected void followMommy(ServerLevel serverLevel) {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy(serverLevel);
        }
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.spit(livingEntity);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return Llama.getDefaultPassengerAttachmentPoint(this, entity, entityDimensions.attachments());
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static enum Variant implements StringRepresentable
    {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

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

        public int getId() {
            return this.id;
        }

        public static Variant byId(int i) {
            return BY_ID.apply(i);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            DEFAULT = CREAMY;
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::getId);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
        }
    }

    static class LlamaHurtByTargetGoal
    extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama llama) {
            super(llama, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            Mob mob = this.mob;
            if (mob instanceof Llama) {
                Llama llama = (Llama)mob;
                if (llama.didSpit) {
                    llama.setDidSpit(false);
                    return false;
                }
            }
            return super.canContinueToUse();
        }
    }

    static class LlamaAttackWolfGoal
    extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama llama) {
            super(llama, Wolf.class, 16, false, true, (livingEntity, serverLevel) -> !((Wolf)livingEntity).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        LlamaGroupData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}

