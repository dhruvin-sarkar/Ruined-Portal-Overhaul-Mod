/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.decoration;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemFrame
extends HangingEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    public static final int NUM_ROTATIONS = 8;
    private static final float DEPTH = 0.0625f;
    private static final float WIDTH = 0.75f;
    private static final float HEIGHT = 0.75f;
    private static final byte DEFAULT_ROTATION = 0;
    private static final float DEFAULT_DROP_CHANCE = 1.0f;
    private static final boolean DEFAULT_INVISIBLE = false;
    private static final boolean DEFAULT_FIXED = false;
    private float dropChance = 1.0f;
    private boolean fixed = false;

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
        this.setInvisible(false);
    }

    public ItemFrame(Level level, BlockPos blockPos, Direction direction) {
        this(EntityType.ITEM_FRAME, level, blockPos, direction);
    }

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level, BlockPos blockPos, Direction direction) {
        super((EntityType<? extends HangingEntity>)entityType, level, blockPos);
        this.setDirection(direction);
        this.setInvisible(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ROTATION, 0);
    }

    @Override
    protected void setDirection(Direction direction) {
        Objects.requireNonNull(direction);
        super.setDirectionRaw(direction);
        if (direction.getAxis().isHorizontal()) {
            this.setXRot(0.0f);
            this.setYRot(direction.get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * direction.getAxisDirection().getStep());
            this.setYRot(0.0f);
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        super.recalculateBoundingBox();
        this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
        return this.createBoundingBox(blockPos, direction, this.hasFramedMap());
    }

    @Override
    protected AABB getPopBox() {
        return this.createBoundingBox(this.pos, this.getDirection(), false);
    }

    private AABB createBoundingBox(BlockPos blockPos, Direction direction, boolean bl) {
        float f = 0.46875f;
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        float g = bl ? 1.0f : 0.75f;
        float h = bl ? 1.0f : 0.75f;
        Direction.Axis axis = direction.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : (double)g;
        double e = axis == Direction.Axis.Y ? 0.0625 : (double)h;
        double i = axis == Direction.Axis.Z ? 0.0625 : (double)g;
        return AABB.ofSize(vec3, d, e, i);
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        }
        if (this.hasLevelCollision(this.getPopBox())) {
            return false;
        }
        BlockState blockState = this.level().getBlockState(this.pos.relative(this.getDirection().getOpposite()));
        if (!(blockState.isSolid() || this.getDirection().getAxis().isHorizontal() && DiodeBlock.isDiode(blockState))) {
            return false;
        }
        return this.canCoexist(true);
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.fixed) {
            super.move(moverType, vec3);
        }
    }

    @Override
    public void push(double d, double e, double f) {
        if (!this.fixed) {
            super.push(d, e, f);
        }
    }

    @Override
    public void kill(ServerLevel serverLevel) {
        this.removeFramedMap(this.getItem());
        super.kill(serverLevel);
    }

    private boolean shouldDamageDropItem(DamageSource damageSource) {
        return !damageSource.is(DamageTypeTags.IS_EXPLOSION) && !this.getItem().isEmpty();
    }

    private static boolean canHurtWhenFixed(DamageSource damageSource) {
        return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.isCreativePlayer();
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        if (this.fixed && !ItemFrame.canHurtWhenFixed(damageSource)) {
            return false;
        }
        return !this.isInvulnerableToBase(damageSource);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.fixed) {
            return ItemFrame.canHurtWhenFixed(damageSource) && super.hurtServer(serverLevel, damageSource, f);
        }
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        if (this.shouldDamageDropItem(damageSource)) {
            this.dropItem(serverLevel, damageSource.getEntity(), false);
            this.gameEvent(GameEvent.BLOCK_CHANGE, damageSource.getEntity());
            this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            return true;
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double e = 16.0;
        return d < (e *= 64.0 * ItemFrame.getViewScale()) * e;
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropItem(serverLevel, entity, true);
        this.gameEvent(GameEvent.BLOCK_CHANGE, entity);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ITEM_FRAME_BREAK;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ITEM_FRAME_PLACE;
    }

    private void dropItem(ServerLevel serverLevel, @Nullable Entity entity, boolean bl) {
        Player player;
        if (this.fixed) {
            return;
        }
        ItemStack itemStack = this.getItem();
        this.setItem(ItemStack.EMPTY);
        if (!serverLevel.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            if (entity == null) {
                this.removeFramedMap(itemStack);
            }
            return;
        }
        if (entity instanceof Player && (player = (Player)entity).hasInfiniteMaterials()) {
            this.removeFramedMap(itemStack);
            return;
        }
        if (bl) {
            this.spawnAtLocation(serverLevel, this.getFrameItemStack());
        }
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            this.removeFramedMap(itemStack);
            if (this.random.nextFloat() < this.dropChance) {
                this.spawnAtLocation(serverLevel, itemStack);
            }
        }
    }

    private void removeFramedMap(ItemStack itemStack) {
        MapItemSavedData mapItemSavedData;
        MapId mapId = this.getFramedMapId(itemStack);
        if (mapId != null && (mapItemSavedData = MapItem.getSavedData(mapId, this.level())) != null) {
            mapItemSavedData.removedFromFrame(this.pos, this.getId());
        }
        itemStack.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public @Nullable MapId getFramedMapId(ItemStack itemStack) {
        return itemStack.get(DataComponents.MAP_ID);
    }

    public boolean hasFramedMap() {
        return this.getItem().has(DataComponents.MAP_ID);
    }

    public void setItem(ItemStack itemStack) {
        this.setItem(itemStack, true);
    }

    public void setItem(ItemStack itemStack, boolean bl) {
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(1);
        }
        this.onItemChanged(itemStack);
        this.getEntityData().set(DATA_ITEM, itemStack);
        if (!itemStack.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if (bl && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public @Nullable SlotAccess getSlot(int i) {
        if (i == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot(i);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (entityDataAccessor.equals(DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }
    }

    private void onItemChanged(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getFrame() != this) {
            itemStack.setEntityRepresentation(this);
        }
        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int i) {
        this.setRotation(i, true);
    }

    private void setRotation(int i, boolean bl) {
        this.getEntityData().set(DATA_ROTATION, i % 8);
        if (bl && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        ItemStack itemStack = this.getItem();
        if (!itemStack.isEmpty()) {
            valueOutput.store("Item", ItemStack.CODEC, itemStack);
        }
        valueOutput.putByte("ItemRotation", (byte)this.getRotation());
        valueOutput.putFloat("ItemDropChance", this.dropChance);
        valueOutput.store("Facing", Direction.LEGACY_ID_CODEC, this.getDirection());
        valueOutput.putBoolean("Invisible", this.isInvisible());
        valueOutput.putBoolean("Fixed", this.fixed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        ItemStack itemStack = valueInput.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        ItemStack itemStack2 = this.getItem();
        if (!itemStack2.isEmpty() && !ItemStack.matches(itemStack, itemStack2)) {
            this.removeFramedMap(itemStack2);
        }
        this.setItem(itemStack, false);
        this.setRotation(valueInput.getByteOr("ItemRotation", (byte)0), false);
        this.dropChance = valueInput.getFloatOr("ItemDropChance", 1.0f);
        this.setDirection(valueInput.read("Facing", Direction.LEGACY_ID_CODEC).orElse(Direction.DOWN));
        this.setInvisible(valueInput.getBooleanOr("Invisible", false));
        this.fixed = valueInput.getBooleanOr("Fixed", false);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        boolean bl2;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl = !this.getItem().isEmpty();
        boolean bl3 = bl2 = !itemStack.isEmpty();
        if (this.fixed) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return bl || bl2 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!bl) {
            if (bl2 && !this.isRemoved()) {
                MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, this.level());
                if (mapItemSavedData != null && mapItemSavedData.isTrackedCountOverLimit(256)) {
                    return InteractionResult.FAIL;
                }
                this.setItem(itemStack);
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                itemStack.consume(1, player);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
        this.setRotation(this.getRotation() + 1);
        this.gameEvent(GameEvent.BLOCK_CHANGE, player);
        return InteractionResult.SUCCESS;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ITEM_FRAME_ROTATE_ITEM;
    }

    public int getAnalogOutput() {
        if (this.getItem().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return this.getFrameItemStack();
        }
        return itemStack.copy();
    }

    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        Direction direction = this.getDirection();
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + this.getRotation() * 45 + i);
    }
}

