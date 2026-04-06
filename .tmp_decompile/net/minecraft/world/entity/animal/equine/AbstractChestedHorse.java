/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChestedHorse
extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_HAS_CHEST = false;
    private final EntityDimensions babyDimensions;

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
        this.canGallop = false;
        this.babyDimensions = entityType.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, entityType.getHeight() - 0.15625f, 0.0f)).scale(0.5f);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AbstractChestedHorse.generateMaxHealth(randomSource::nextInt));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return AbstractChestedHorse.createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175f).add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_ID_CHEST);
    }

    public void setChest(boolean bl) {
        this.entityData.set(DATA_ID_CHEST, bl);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? this.babyDimensions : super.getDefaultDimensions(pose);
    }

    @Override
    protected void dropEquipment(ServerLevel serverLevel) {
        super.dropEquipment(serverLevel);
        if (this.hasChest()) {
            this.spawnAtLocation(serverLevel, Blocks.CHEST);
            this.setChest(false);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList = valueOutput.list("Items", ItemStackWithSlot.CODEC);
            for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemStack = this.inventory.getItem(i);
                if (itemStack.isEmpty()) continue;
                typedOutputList.add(new ItemStackWithSlot(i, itemStack));
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setChest(valueInput.getBooleanOr("ChestedHorse", false));
        this.createInventory();
        if (this.hasChest()) {
            for (ItemStackWithSlot itemStackWithSlot : valueInput.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
                if (!itemStackWithSlot.isValidInContainer(this.inventory.getContainerSize())) continue;
                this.inventory.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
            }
        }
    }

    @Override
    public @Nullable SlotAccess getSlot(int i) {
        if (i == 499) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    if (itemStack.isEmpty()) {
                        if (AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(false);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    if (itemStack.is(Items.CHEST)) {
                        if (!AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(true);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    return false;
                }
            };
        }
        return super.getSlot(i);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
        if (this.isVehicle() || bl) {
            return super.mobInteract(player, interactionHand);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty()) {
            if (this.isFood(itemStack)) {
                return this.fedFood(player, itemStack);
            }
            if (!this.isTamed()) {
                this.makeMad();
                return InteractionResult.SUCCESS;
            }
            if (!this.hasChest() && itemStack.is(Items.CHEST)) {
                this.equipChest(player, itemStack);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, interactionHand);
    }

    private void equipChest(Player player, ItemStack itemStack) {
        this.setChest(true);
        this.playChestEquipsSound();
        itemStack.consume(1, player);
        this.createInventory();
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.41, 0.18, 0.73);
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}

