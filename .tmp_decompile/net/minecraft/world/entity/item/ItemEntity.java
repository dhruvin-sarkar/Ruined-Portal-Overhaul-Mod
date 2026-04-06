/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.item;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemEntity
extends Entity
implements TraceableEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final float FLOAT_HEIGHT = 0.1f;
    public static final float EYE_HEIGHT = 0.2125f;
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = Short.MAX_VALUE;
    private static final int INFINITE_LIFETIME = Short.MIN_VALUE;
    private static final int DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_PICKUP_DELAY = 0;
    private int age = 0;
    private int pickupDelay = 0;
    private int health = 5;
    private @Nullable EntityReference<Entity> thrower;
    private @Nullable UUID target;
    public final float bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0f;

    public ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.setYRot(this.random.nextFloat() * 360.0f);
    }

    public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack) {
        this(level, d, e, f, itemStack, level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack, double g, double h, double i) {
        this((EntityType<? extends ItemEntity>)EntityType.ITEM, level);
        this.setPos(d, e, f);
        this.setDeltaMovement(g, h, i);
        this.setItem(itemStack);
    }

    @Override
    public boolean dampensVibrations() {
        return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Override
    public @Nullable Entity getOwner() {
        return EntityReference.getEntity(this.thrower, this.level());
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            this.thrower = itemEntity.thrower;
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        double d;
        int i;
        if (this.getItem().isEmpty()) {
            this.discard();
            return;
        }
        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE) {
            --this.pickupDelay;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 vec3 = this.getDeltaMovement();
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)0.1f) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)0.1f) {
            this.setUnderLavaMovement();
        } else {
            this.applyGravity();
        }
        if (this.level().isClientSide()) {
            this.noPhysics = false;
        } else {
            boolean bl = this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }
        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5f || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            float f = 0.98f;
            if (this.onGround()) {
                f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
            if (this.onGround()) {
                Vec3 vec32 = this.getDeltaMovement();
                if (vec32.y < 0.0) {
                    this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int n = i = bl ? 2 : 40;
        if (this.tickCount % i == 0 && !this.level().isClientSide() && this.isMergable()) {
            this.mergeWithNeighbours();
        }
        if (this.age != Short.MIN_VALUE) {
            ++this.age;
        }
        this.needsSync |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level().isClientSide() && (d = this.getDeltaMovement().subtract(vec3).lengthSqr()) > 0.01) {
            this.needsSync = true;
        }
        if (!this.level().isClientSide() && this.age >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999f);
    }

    private void setUnderwaterMovement() {
        this.setFluidMovement(0.99f);
    }

    private void setUnderLavaMovement() {
        this.setFluidMovement(0.95f);
    }

    private void setFluidMovement(double d) {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * d, vec3.y + (double)(vec3.y < (double)0.06f ? 5.0E-4f : 0.0f), vec3.z * d);
    }

    private void mergeWithNeighbours() {
        if (!this.isMergable()) {
            return;
        }
        List<ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), itemEntity -> itemEntity != this && itemEntity.isMergable());
        for (ItemEntity itemEntity2 : list) {
            if (!itemEntity2.isMergable()) continue;
            this.tryToMerge(itemEntity2);
            if (!this.isRemoved()) continue;
            break;
        }
    }

    private boolean isMergable() {
        ItemStack itemStack = this.getItem();
        return this.isAlive() && this.pickupDelay != Short.MAX_VALUE && this.age != Short.MIN_VALUE && this.age < 6000 && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity itemEntity) {
        ItemStack itemStack = this.getItem();
        ItemStack itemStack2 = itemEntity.getItem();
        if (!Objects.equals(this.target, itemEntity.target) || !ItemEntity.areMergable(itemStack, itemStack2)) {
            return;
        }
        if (itemStack2.getCount() < itemStack.getCount()) {
            ItemEntity.merge(this, itemStack, itemEntity, itemStack2);
        } else {
            ItemEntity.merge(itemEntity, itemStack2, this, itemStack);
        }
    }

    public static boolean areMergable(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(itemStack, itemStack2);
    }

    public static ItemStack merge(ItemStack itemStack, ItemStack itemStack2, int i) {
        int j = Math.min(Math.min(itemStack.getMaxStackSize(), i) - itemStack.getCount(), itemStack2.getCount());
        ItemStack itemStack3 = itemStack.copyWithCount(itemStack.getCount() + j);
        itemStack2.shrink(j);
        return itemStack3;
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2) {
        ItemStack itemStack3 = ItemEntity.merge(itemStack, itemStack2, 64);
        itemEntity.setItem(itemStack3);
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2) {
        ItemEntity.merge(itemEntity, itemStack, itemStack2);
        itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, itemEntity2.pickupDelay);
        itemEntity.age = Math.min(itemEntity.age, itemEntity2.age);
        if (itemStack2.isEmpty()) {
            itemEntity2.discard();
        }
    }

    @Override
    public boolean fireImmune() {
        return !this.getItem().canBeHurtBy(this.damageSources().inFire()) || super.fireImmune();
    }

    @Override
    protected boolean shouldPlayLavaHurtSound() {
        if (this.health <= 0) {
            return true;
        }
        return this.tickCount % 10 == 0;
    }

    @Override
    public final boolean hurtClient(DamageSource damageSource) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        return this.getItem().canBeHurtBy(damageSource);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        if (!serverLevel.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue() && damageSource.getEntity() instanceof Mob) {
            return false;
        }
        if (!this.getItem().canBeHurtBy(damageSource)) {
            return false;
        }
        this.markHurt();
        this.health = (int)((float)this.health - f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        if (this.health <= 0) {
            this.getItem().onDestroyed(this);
            this.discard();
        }
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        if (explosion.shouldAffectBlocklikeEntities()) {
            return super.ignoreExplosion(explosion);
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putShort("Health", (short)this.health);
        valueOutput.putShort("Age", (short)this.age);
        valueOutput.putShort("PickupDelay", (short)this.pickupDelay);
        EntityReference.store(this.thrower, valueOutput, "Thrower");
        valueOutput.storeNullable("Owner", UUIDUtil.CODEC, this.target);
        if (!this.getItem().isEmpty()) {
            valueOutput.store("Item", ItemStack.CODEC, this.getItem());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.health = valueInput.getShortOr("Health", (short)5);
        this.age = valueInput.getShortOr("Age", (short)0);
        this.pickupDelay = valueInput.getShortOr("PickupDelay", (short)0);
        this.target = valueInput.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.thrower = EntityReference.read(valueInput, "Thrower");
        this.setItem(valueInput.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        if (this.getItem().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Item item = itemStack.getItem();
        int i = itemStack.getCount();
        if (this.pickupDelay == 0 && (this.target == null || this.target.equals(player.getUUID())) && player.getInventory().add(itemStack)) {
            player.take(this, i);
            if (itemStack.isEmpty()) {
                this.discard();
                itemStack.setCount(i);
            }
            player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            player.onItemPickup(this);
        }
    }

    @Override
    public Component getName() {
        Component component = this.getCustomName();
        if (component != null) {
            return component;
        }
        return this.getItem().getItemName();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @Nullable Entity teleport(TeleportTransition teleportTransition) {
        Entity entity = super.teleport(teleportTransition);
        if (!this.level().isClientSide() && entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            itemEntity.mergeWithNeighbours();
        }
        return entity;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack itemStack) {
        this.getEntityData().set(DATA_ITEM, itemStack);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ITEM.equals(entityDataAccessor)) {
            this.getItem().setEntityRepresentation(this);
        }
    }

    public void setTarget(@Nullable UUID uUID) {
        this.target = uUID;
    }

    public void setThrower(Entity entity) {
        this.thrower = EntityReference.of(entity);
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = Short.MAX_VALUE;
    }

    public void setPickUpDelay(int i) {
        this.pickupDelay = i;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime() {
        this.age = Short.MIN_VALUE;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public static float getSpin(float f, float g) {
        return f / 20.0f + g;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0f - ItemEntity.getSpin((float)this.getAge() + 0.5f, this.bobOffs) / ((float)Math.PI * 2) * 360.0f;
    }

    @Override
    public @Nullable SlotAccess getSlot(int i) {
        if (i == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot(i);
    }
}

