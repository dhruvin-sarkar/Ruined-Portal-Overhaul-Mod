/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SnowGolem
extends AbstractGolem
implements Shearable,
RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);
    private static final byte PUMPKIN_FLAG = 16;
    private static final boolean DEFAULT_PUMPKIN = true;

    public SnowGolem(EntityType<? extends SnowGolem> entityType, Level level) {
        super((EntityType<? extends AbstractGolem>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0, 1.0000001E-5f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Mob>(this, Mob.class, 10, true, false, (livingEntity, serverLevel) -> livingEntity instanceof Enemy));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PUMPKIN_ID, (byte)16);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setPumpkin(valueInput.getBooleanOr("Pumpkin", true));
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (serverLevel.environmentAttributes().getValue(EnvironmentAttributes.SNOW_GOLEM_MELTS, this.position()).booleanValue()) {
                this.hurtServer(serverLevel, this.damageSources().onFire(), 1.0f);
            }
            if (!serverLevel.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                return;
            }
            BlockState blockState = Blocks.SNOW.defaultBlockState();
            for (int i = 0; i < 4; ++i) {
                int j = Mth.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25f));
                int k = Mth.floor(this.getY());
                int l = Mth.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25f));
                BlockPos blockPos = new BlockPos(j, k, l);
                if (!this.level().getBlockState(blockPos).isAir() || !blockState.canSurvive(this.level(), blockPos)) continue;
                this.level().setBlockAndUpdate(blockPos, blockState);
                this.level().gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(this, blockState));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        double d = livingEntity.getX() - this.getX();
        double e = livingEntity.getEyeY() - (double)1.1f;
        double g = livingEntity.getZ() - this.getZ();
        double h = Math.sqrt(d * d + g * g) * (double)0.2f;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ItemStack itemStack = new ItemStack(Items.SNOWBALL);
            Projectile.spawnProjectile(new Snowball(serverLevel, this, itemStack), serverLevel, itemStack, snowball -> snowball.shoot(d, e + h - snowball.getY(), g, 1.6f, 12.0f));
        }
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.SHEARS) && this.readyForShearing()) {
            Level level = this.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.shear(serverLevel, SoundSource.PLAYERS, itemStack);
                this.gameEvent(GameEvent.SHEAR, player);
                itemStack.hurtAndBreak(1, (LivingEntity)player, interactionHand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void shear(ServerLevel serverLevel2, SoundSource soundSource, ItemStack itemStack2) {
        serverLevel2.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, soundSource, 1.0f, 1.0f);
        this.setPumpkin(false);
        this.dropFromShearingLootTable(serverLevel2, BuiltInLootTables.SHEAR_SNOW_GOLEM, itemStack2, (serverLevel, itemStack) -> this.spawnAtLocation((ServerLevel)serverLevel, (ItemStack)itemStack, this.getEyeHeight()));
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && this.hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (this.entityData.get(DATA_PUMPKIN_ID) & 0x10) != 0;
    }

    public void setPumpkin(boolean bl) {
        byte b = this.entityData.get(DATA_PUMPKIN_ID);
        if (bl) {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(b | 0x10));
        } else {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(b & 0xFFFFFFEF));
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }
}

