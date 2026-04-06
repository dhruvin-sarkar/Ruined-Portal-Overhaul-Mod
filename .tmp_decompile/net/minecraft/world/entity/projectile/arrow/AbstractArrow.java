/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.arrow;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractArrow
extends Projectile {
    private static final double ARROW_BASE_DAMAGE = 2.0;
    private static final int SHAKE_TIME = 7;
    private static final float WATER_INERTIA = 0.6f;
    private static final float INERTIA = 0.99f;
    private static final short DEFAULT_LIFE = 0;
    private static final byte DEFAULT_SHAKE = 0;
    private static final boolean DEFAULT_IN_GROUND = false;
    private static final boolean DEFAULT_CRIT = false;
    private static final byte DEFAULT_PIERCE_LEVEL = 0;
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BOOLEAN);
    private static final int FLAG_CRIT = 1;
    private static final int FLAG_NOPHYSICS = 2;
    private @Nullable BlockState lastState;
    protected int inGroundTime;
    public Pickup pickup = Pickup.DISALLOWED;
    public int shakeTime = 0;
    private int life = 0;
    private double baseDamage = 2.0;
    private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
    private @Nullable IntOpenHashSet piercingIgnoreEntityIds;
    private @Nullable List<Entity> piercedAndKilledEntities;
    private ItemStack pickupItemStack = this.getDefaultPickupItem();
    private @Nullable ItemStack firedFromWeapon = null;

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, double d, double e, double f, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2) {
        this(entityType, level);
        this.pickupItemStack = itemStack.copy();
        this.applyComponentsFromItemStack(itemStack);
        Unit unit = itemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
        if (unit != null) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
        this.setPos(d, e, f);
        if (itemStack2 != null && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (itemStack2.isEmpty()) {
                throw new IllegalArgumentException("Invalid weapon firing an arrow");
            }
            this.firedFromWeapon = itemStack2.copy();
            int i = EnchantmentHelper.getPiercingCount(serverLevel, itemStack2, this.pickupItemStack);
            if (i > 0) {
                this.setPierceLevel((byte)i);
            }
        }
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2) {
        this(entityType, livingEntity.getX(), livingEntity.getEyeY() - (double)0.1f, livingEntity.getZ(), level, itemStack, itemStack2);
        this.setOwner(livingEntity);
    }

    public void setSoundEvent(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double e = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(e)) {
            e = 1.0;
        }
        return d < (e *= 64.0 * AbstractArrow.getViewScale()) * e;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte)0);
        builder.define(PIERCE_LEVEL, (byte)0);
        builder.define(IN_GROUND, false);
    }

    @Override
    public void shoot(double d, double e, double f, float g, float h) {
        super.shoot(d, e, f, g, h);
        this.life = 0;
    }

    @Override
    public void lerpMotion(Vec3 vec3) {
        super.lerpMotion(vec3);
        this.life = 0;
        if (this.isInGround() && vec3.lengthSqr() > 0.0) {
            this.setInGround(false);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (!this.firstTick && this.shakeTime <= 0 && entityDataAccessor.equals(IN_GROUND) && this.isInGround()) {
            this.shakeTime = 7;
        }
    }

    @Override
    public void tick() {
        VoxelShape voxelShape;
        boolean bl = !this.isNoPhysics();
        Vec3 vec3 = this.getDeltaMovement();
        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (!blockState.isAir() && bl && !(voxelShape = blockState.getCollisionShape(this.level(), blockPos)).isEmpty()) {
            Vec3 vec32 = this.position();
            for (AABB aABB : voxelShape.toAabbs()) {
                if (!aABB.move(blockPos).contains(vec32)) continue;
                this.setDeltaMovement(Vec3.ZERO);
                this.setInGround(true);
                break;
            }
        }
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }
        if (this.isInWaterOrRain()) {
            this.clearFire();
        }
        if (this.isInGround() && bl) {
            if (!this.level().isClientSide()) {
                if (this.lastState != blockState && this.shouldFall()) {
                    this.startFalling();
                } else {
                    this.tickDespawn();
                }
            }
            ++this.inGroundTime;
            if (this.isAlive()) {
                this.applyEffectsFromBlocks();
            }
            if (!this.level().isClientSide()) {
                this.setSharedFlagOnFire(this.getRemainingFireTicks() > 0);
            }
            return;
        }
        this.inGroundTime = 0;
        Vec3 vec33 = this.position();
        if (this.isInWater()) {
            this.applyInertia(this.getWaterInertia());
            this.addBubbleParticles(vec33);
        }
        if (this.isCritArrow()) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.CRIT, vec33.x + vec3.x * (double)i / 4.0, vec33.y + vec3.y * (double)i / 4.0, vec33.z + vec3.z * (double)i / 4.0, -vec3.x, -vec3.y + 0.2, -vec3.z);
            }
        }
        float f = !bl ? (float)(Mth.atan2(-vec3.x, -vec3.z) * 57.2957763671875) : (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875);
        float g = (float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * 57.2957763671875);
        this.setXRot(AbstractArrow.lerpRotation(this.getXRot(), g));
        this.setYRot(AbstractArrow.lerpRotation(this.getYRot(), f));
        this.checkLeftOwner();
        if (bl) {
            BlockHitResult blockHitResult = this.level().clipIncludingBorder(new ClipContext(vec33, vec33.add(vec3), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            this.stepMoveAndHit(blockHitResult);
        } else {
            this.setPos(vec33.add(vec3));
            this.applyEffectsFromBlocks();
        }
        if (!this.isInWater()) {
            this.applyInertia(0.99f);
        }
        if (bl && !this.isInGround()) {
            this.applyGravity();
        }
        super.tick();
    }

    private void stepMoveAndHit(BlockHitResult blockHitResult) {
        while (this.isAlive()) {
            Vec3 vec3 = this.position();
            ArrayList<EntityHitResult> arrayList = new ArrayList<EntityHitResult>(this.findHitEntities(vec3, blockHitResult.getLocation()));
            arrayList.sort(Comparator.comparingDouble(entityHitResult -> vec3.distanceToSqr(entityHitResult.getEntity().position())));
            EntityHitResult entityHitResult2 = arrayList.isEmpty() ? null : (EntityHitResult)arrayList.getFirst();
            Vec3 vec32 = ((HitResult)Objects.requireNonNullElse((Object)entityHitResult2, (Object)blockHitResult)).getLocation();
            this.setPos(vec32);
            this.applyEffectsFromBlocks(vec3, vec32);
            if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                this.handlePortal();
            }
            if (arrayList.isEmpty()) {
                if (!this.isAlive() || blockHitResult.getType() == HitResult.Type.MISS) break;
                this.hitTargetOrDeflectSelf(blockHitResult);
                this.needsSync = true;
                break;
            }
            if (!this.isAlive() || this.noPhysics) continue;
            ProjectileDeflection projectileDeflection = this.hitTargetsOrDeflectSelf(arrayList);
            this.needsSync = true;
            if (this.getPierceLevel() > 0 && projectileDeflection == ProjectileDeflection.NONE) continue;
            break;
        }
    }

    private ProjectileDeflection hitTargetsOrDeflectSelf(Collection<EntityHitResult> collection) {
        for (EntityHitResult entityHitResult : collection) {
            ProjectileDeflection projectileDeflection = this.hitTargetOrDeflectSelf(entityHitResult);
            if (this.isAlive() && projectileDeflection == ProjectileDeflection.NONE) continue;
            return projectileDeflection;
        }
        return ProjectileDeflection.NONE;
    }

    private void applyInertia(float f) {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.scale(f));
    }

    private void addBubbleParticles(Vec3 vec3) {
        Vec3 vec32 = this.getDeltaMovement();
        for (int i = 0; i < 4; ++i) {
            float f = 0.25f;
            this.level().addParticle(ParticleTypes.BUBBLE, vec3.x - vec32.x * 0.25, vec3.y - vec32.y * 0.25, vec3.z - vec32.z * 0.25, vec32.x, vec32.y, vec32.z);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    private boolean shouldFall() {
        return this.isInGround() && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
    }

    private void startFalling() {
        this.setInGround(false);
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    protected boolean isInGround() {
        return this.entityData.get(IN_GROUND);
    }

    protected void setInGround(boolean bl) {
        this.entityData.set(IN_GROUND, bl);
    }

    @Override
    public boolean isPushedByFluid() {
        return !this.isInGround();
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
        if (moverType != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }
    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }
        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }
    }

    @Override
    public void onItemBreak(Item item) {
        this.firedFromWeapon = null;
    }

    @Override
    public void onAboveBubbleColumn(boolean bl, BlockPos blockPos) {
        if (this.isInGround()) {
            return;
        }
        super.onAboveBubbleColumn(bl, blockPos);
    }

    @Override
    public void onInsideBubbleColumn(boolean bl) {
        if (this.isInGround()) {
            return;
        }
        super.onInsideBubbleColumn(bl);
    }

    @Override
    public void push(double d, double e, double f) {
        if (this.isInGround()) {
            return;
        }
        super.push(d, e, f);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Level level;
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        float f = (float)this.getDeltaMovement().length();
        double d = this.baseDamage;
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.damageSources().arrow(this, entity2 != null ? entity2 : this);
        if (this.getWeaponItem() != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            d = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, (float)d);
        }
        int i = Mth.ceil(Mth.clamp((double)f * d, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity((int)5);
            }
            if (this.piercingIgnoreEntityIds.size() < this.getPierceLevel() + 1) {
                this.piercingIgnoreEntityIds.add(entity.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritArrow()) {
            long l = this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(l + (long)i, Integer.MAX_VALUE);
        }
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            livingEntity.setLastHurtMob(entity);
        }
        boolean bl = entity.getType() == EntityType.ENDERMAN;
        int j = entity.getRemainingFireTicks();
        if (this.isOnFire() && !bl) {
            entity.igniteForSeconds(5.0f);
        }
        if (entity.hurtOrSimulate(damageSource, i)) {
            if (bl) {
                return;
            }
            if (entity instanceof LivingEntity) {
                ServerPlayer serverPlayer;
                LivingEntity livingEntity2 = (LivingEntity)entity;
                if (!this.level().isClientSide() && this.getPierceLevel() <= 0) {
                    livingEntity2.setArrowCount(livingEntity2.getArrowCount() + 1);
                }
                this.doKnockback(livingEntity2, damageSource);
                Level level2 = this.level();
                if (level2 instanceof ServerLevel) {
                    ServerLevel serverLevel2 = (ServerLevel)level2;
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel2, livingEntity2, damageSource, this.getWeaponItem());
                }
                this.doPostHurtEffects(livingEntity2);
                if (livingEntity2 instanceof Player && entity2 instanceof ServerPlayer) {
                    serverPlayer = (ServerPlayer)entity2;
                    if (!this.isSilent() && livingEntity2 != serverPlayer) {
                        serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND, 0.0f));
                    }
                }
                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingEntity2);
                }
                if (!this.level().isClientSide() && entity2 instanceof ServerPlayer) {
                    serverPlayer = (ServerPlayer)entity2;
                    if (this.piercedAndKilledEntities != null) {
                        CriteriaTriggers.KILLED_BY_ARROW.trigger(serverPlayer, this.piercedAndKilledEntities, this.firedFromWeapon);
                    } else if (!entity.isAlive()) {
                        CriteriaTriggers.KILLED_BY_ARROW.trigger(serverPlayer, List.of((Object)entity), this.firedFromWeapon);
                    }
                }
            }
            this.playSound(this.soundEvent, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(j);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.owner, false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            Level level3 = this.level();
            if (level3 instanceof ServerLevel) {
                ServerLevel serverLevel3 = (ServerLevel)level3;
                if (this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                    if (this.pickup == Pickup.ALLOWED) {
                        this.spawnAtLocation(serverLevel3, this.getPickupItem(), 0.1f);
                    }
                    this.discard();
                }
            }
        }
    }

    protected void doKnockback(LivingEntity livingEntity, DamageSource damageSource) {
        float f;
        Level level;
        if (this.firedFromWeapon != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f = EnchantmentHelper.modifyKnockback(serverLevel, this.firedFromWeapon, livingEntity, damageSource, 0.0f);
        } else {
            f = 0.0f;
        }
        double d = f;
        if (d > 0.0) {
            double e = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(d * 0.6 * e);
            if (vec3.lengthSqr() > 0.0) {
                livingEntity.push(vec3.x, 0.1, vec3.z);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.lastState = this.level().getBlockState(blockHitResult.getBlockPos());
        super.onHitBlock(blockHitResult);
        ItemStack itemStack = this.getWeaponItem();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (itemStack != null) {
                this.hitBlockEnchantmentEffects(serverLevel, blockHitResult, itemStack);
            }
        }
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec32 = new Vec3(Math.signum(vec3.x), Math.signum(vec3.y), Math.signum(vec3.z));
        Vec3 vec33 = vec32.scale(0.05f);
        this.setPos(this.position().subtract(vec33));
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(this.getHitGroundSoundEvent(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.setInGround(true);
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.resetPiercedEntities();
    }

    protected void hitBlockEnchantmentEffects(ServerLevel serverLevel, BlockHitResult blockHitResult, ItemStack itemStack) {
        LivingEntity livingEntity;
        Vec3 vec3 = blockHitResult.getBlockPos().clampLocationWithin(blockHitResult.getLocation());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock(serverLevel, itemStack, entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null, this, null, vec3, serverLevel.getBlockState(blockHitResult.getBlockPos()), item -> {
            this.firedFromWeapon = null;
        });
    }

    @Override
    public @Nullable ItemStack getWeaponItem() {
        return this.firedFromWeapon;
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(LivingEntity livingEntity) {
    }

    protected @Nullable EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, vec3, vec32, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity);
    }

    protected Collection<EntityHitResult> findHitEntities(Vec3 vec3, Vec3 vec32) {
        return ProjectileUtil.getManyEntityHitResult(this.level(), this, vec3, vec32, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity, false);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        Player player;
        Entity entity2;
        if (entity instanceof Player && (entity2 = this.getOwner()) instanceof Player && !(player = (Player)entity2).canHarmPlayer((Player)entity)) {
            return false;
        }
        return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putShort("life", (short)this.life);
        valueOutput.storeNullable("inBlockState", BlockState.CODEC, this.lastState);
        valueOutput.putByte("shake", (byte)this.shakeTime);
        valueOutput.putBoolean("inGround", this.isInGround());
        valueOutput.store("pickup", Pickup.LEGACY_CODEC, this.pickup);
        valueOutput.putDouble("damage", this.baseDamage);
        valueOutput.putBoolean("crit", this.isCritArrow());
        valueOutput.putByte("PierceLevel", this.getPierceLevel());
        valueOutput.store("SoundEvent", BuiltInRegistries.SOUND_EVENT.byNameCodec(), this.soundEvent);
        valueOutput.store("item", ItemStack.CODEC, this.pickupItemStack);
        valueOutput.storeNullable("weapon", ItemStack.CODEC, this.firedFromWeapon);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.life = valueInput.getShortOr("life", (short)0);
        this.lastState = valueInput.read("inBlockState", BlockState.CODEC).orElse(null);
        this.shakeTime = valueInput.getByteOr("shake", (byte)0) & 0xFF;
        this.setInGround(valueInput.getBooleanOr("inGround", false));
        this.baseDamage = valueInput.getDoubleOr("damage", 2.0);
        this.pickup = valueInput.read("pickup", Pickup.LEGACY_CODEC).orElse(Pickup.DISALLOWED);
        this.setCritArrow(valueInput.getBooleanOr("crit", false));
        this.setPierceLevel(valueInput.getByteOr("PierceLevel", (byte)0));
        this.soundEvent = valueInput.read("SoundEvent", BuiltInRegistries.SOUND_EVENT.byNameCodec()).orElse(this.getDefaultHitGroundSoundEvent());
        this.setPickupItemStack(valueInput.read("item", ItemStack.CODEC).orElse(this.getDefaultPickupItem()));
        this.firedFromWeapon = valueInput.read("weapon", ItemStack.CODEC).orElse(null);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        Pickup pickup;
        super.setOwner(entity);
        Entity entity2 = entity;
        int n = 0;
        block4: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Player.class, OminousItemSpawner.class}, (Object)entity2, (int)n)) {
                case 0: {
                    Player player = (Player)entity2;
                    if (this.pickup != Pickup.DISALLOWED) {
                        n = 1;
                        continue block4;
                    }
                    pickup = Pickup.ALLOWED;
                    break block4;
                }
                case 1: {
                    OminousItemSpawner ominousItemSpawner = (OminousItemSpawner)entity2;
                    pickup = Pickup.DISALLOWED;
                    break block4;
                }
                default: {
                    pickup = this.pickup;
                    break block4;
                }
            }
            break;
        }
        this.pickup = pickup;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide() || !this.isInGround() && !this.isNoPhysics() || this.shakeTime > 0) {
            return;
        }
        if (this.tryPickup(player)) {
            player.take(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(Player player) {
        return switch (this.pickup.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 1 -> player.getInventory().add(this.getPickupItem());
            case 2 -> player.hasInfiniteMaterials();
        };
    }

    protected ItemStack getPickupItem() {
        return this.pickupItemStack.copy();
    }

    protected abstract ItemStack getDefaultPickupItem();

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public ItemStack getPickupItemStackOrigin() {
        return this.pickupItemStack;
    }

    public void setBaseDamage(double d) {
        this.baseDamage = d;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    public void setCritArrow(boolean bl) {
        this.setFlag(1, bl);
    }

    private void setPierceLevel(byte b) {
        this.entityData.set(PIERCE_LEVEL, b);
    }

    private void setFlag(int i, boolean bl) {
        byte b = this.entityData.get(ID_FLAGS);
        if (bl) {
            this.entityData.set(ID_FLAGS, (byte)(b | i));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(b & ~i));
        }
    }

    protected void setPickupItemStack(ItemStack itemStack) {
        this.pickupItemStack = !itemStack.isEmpty() ? itemStack : this.getDefaultPickupItem();
    }

    public boolean isCritArrow() {
        byte b = this.entityData.get(ID_FLAGS);
        return (b & 1) != 0;
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    public void setBaseDamageFromMob(float f) {
        this.setBaseDamage((double)(f * 2.0f) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getWaterInertia() {
        return 0.6f;
    }

    public void setNoPhysics(boolean bl) {
        this.noPhysics = bl;
        this.setFlag(2, bl);
    }

    public boolean isNoPhysics() {
        if (!this.level().isClientSide()) {
            return this.noPhysics;
        }
        return (this.entityData.get(ID_FLAGS) & 2) != 0;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isInGround();
    }

    @Override
    public @Nullable SlotAccess getSlot(int i) {
        if (i == 0) {
            return SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack);
        }
        return super.getSlot(i);
    }

    @Override
    protected boolean shouldBounceOnWorldBorder() {
        return true;
    }

    public static enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static final Codec<Pickup> LEGACY_CODEC;

        public static Pickup byOrdinal(int i) {
            if (i < 0 || i > Pickup.values().length) {
                i = 0;
            }
            return Pickup.values()[i];
        }

        static {
            LEGACY_CODEC = Codec.BYTE.xmap(Pickup::byOrdinal, pickup -> (byte)pickup.ordinal());
        }
    }
}

