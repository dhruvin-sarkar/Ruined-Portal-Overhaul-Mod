/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class HopperBlockEntity
extends RandomizableContainerBlockEntity
implements Hopper {
    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;
    private static final int[][] CACHED_SLOTS = new int[54][];
    private static final int NO_COOLDOWN_TIME = -1;
    private static final Component DEFAULT_NAME = Component.translatable("container.hopper");
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;
    private Direction facing;

    public HopperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.HOPPER, blockPos, blockState);
        this.facing = blockState.getValue(HopperBlock.FACING);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(valueInput)) {
            ContainerHelper.loadAllItems(valueInput, this.items);
        }
        this.cooldownTime = valueInput.getIntOr("TransferCooldown", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.trySaveLootTable(valueOutput)) {
            ContainerHelper.saveAllItems(valueOutput, this.items);
        }
        valueOutput.putInt("TransferCooldown", this.cooldownTime);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.getItems(), i, j);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.unpackLootTable(null);
        this.getItems().set(i, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
    }

    @Override
    public void setBlockState(BlockState blockState) {
        super.setBlockState(blockState);
        this.facing = blockState.getValue(HopperBlock.FACING);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity) {
        --hopperBlockEntity.cooldownTime;
        hopperBlockEntity.tickedGameTime = level.getGameTime();
        if (!hopperBlockEntity.isOnCooldown()) {
            hopperBlockEntity.setCooldown(0);
            HopperBlockEntity.tryMoveItems(level, blockPos, blockState, hopperBlockEntity, () -> HopperBlockEntity.suckInItems(level, hopperBlockEntity));
        }
    }

    private static boolean tryMoveItems(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity, BooleanSupplier booleanSupplier) {
        if (level.isClientSide()) {
            return false;
        }
        if (!hopperBlockEntity.isOnCooldown() && blockState.getValue(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!hopperBlockEntity.isEmpty()) {
                bl = HopperBlockEntity.ejectItems(level, blockPos, hopperBlockEntity);
            }
            if (!hopperBlockEntity.inventoryFull()) {
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                hopperBlockEntity.setCooldown(8);
                HopperBlockEntity.setChanged(level, blockPos, blockState);
                return true;
            }
        }
        return false;
    }

    private boolean inventoryFull() {
        for (ItemStack itemStack : this.items) {
            if (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxStackSize()) continue;
            return false;
        }
        return true;
    }

    private static boolean ejectItems(Level level, BlockPos blockPos, HopperBlockEntity hopperBlockEntity) {
        Container container = HopperBlockEntity.getAttachedContainer(level, blockPos, hopperBlockEntity);
        if (container == null) {
            return false;
        }
        Direction direction = hopperBlockEntity.facing.getOpposite();
        if (HopperBlockEntity.isFullContainer(container, direction)) {
            return false;
        }
        for (int i = 0; i < hopperBlockEntity.getContainerSize(); ++i) {
            ItemStack itemStack = hopperBlockEntity.getItem(i);
            if (itemStack.isEmpty()) continue;
            int j = itemStack.getCount();
            ItemStack itemStack2 = HopperBlockEntity.addItem(hopperBlockEntity, container, hopperBlockEntity.removeItem(i, 1), direction);
            if (itemStack2.isEmpty()) {
                container.setChanged();
                return true;
            }
            itemStack.setCount(j);
            if (j != 1) continue;
            hopperBlockEntity.setItem(i, itemStack);
        }
        return false;
    }

    private static int[] getSlots(Container container, Direction direction) {
        if (container instanceof WorldlyContainer) {
            WorldlyContainer worldlyContainer = (WorldlyContainer)container;
            return worldlyContainer.getSlotsForFace(direction);
        }
        int i = container.getContainerSize();
        if (i < CACHED_SLOTS.length) {
            int[] is = CACHED_SLOTS[i];
            if (is != null) {
                return is;
            }
            int[] js = HopperBlockEntity.createFlatSlots(i);
            HopperBlockEntity.CACHED_SLOTS[i] = js;
            return js;
        }
        return HopperBlockEntity.createFlatSlots(i);
    }

    private static int[] createFlatSlots(int i) {
        int[] is = new int[i];
        for (int j = 0; j < is.length; ++j) {
            is[j] = j;
        }
        return is;
    }

    private static boolean isFullContainer(Container container, Direction direction) {
        int[] is;
        for (int i : is = HopperBlockEntity.getSlots(container, direction)) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.getCount() >= itemStack.getMaxStackSize()) continue;
            return false;
        }
        return true;
    }

    public static boolean suckInItems(Level level, Hopper hopper) {
        boolean bl;
        BlockState blockState;
        BlockPos blockPos = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY() + 1.0, hopper.getLevelZ());
        Container container = HopperBlockEntity.getSourceContainer(level, hopper, blockPos, blockState = level.getBlockState(blockPos));
        if (container != null) {
            Direction direction = Direction.DOWN;
            for (int i : HopperBlockEntity.getSlots(container, direction)) {
                if (!HopperBlockEntity.tryTakeInItemFromSlot(hopper, container, i, direction)) continue;
                return true;
            }
            return false;
        }
        boolean bl2 = bl = hopper.isGridAligned() && blockState.isCollisionShapeFullBlock(level, blockPos) && !blockState.is(BlockTags.DOES_NOT_BLOCK_HOPPERS);
        if (!bl) {
            for (ItemEntity itemEntity : HopperBlockEntity.getItemsAtAndAbove(level, hopper)) {
                if (!HopperBlockEntity.addItem(hopper, itemEntity)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
        ItemStack itemStack = container.getItem(i);
        if (!itemStack.isEmpty() && HopperBlockEntity.canTakeItemFromContainer(hopper, container, itemStack, i, direction)) {
            int j = itemStack.getCount();
            ItemStack itemStack2 = HopperBlockEntity.addItem(container, hopper, container.removeItem(i, 1), null);
            if (itemStack2.isEmpty()) {
                container.setChanged();
                return true;
            }
            itemStack.setCount(j);
            if (j == 1) {
                container.setItem(i, itemStack);
            }
        }
        return false;
    }

    public static boolean addItem(Container container, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getItem().copy();
        ItemStack itemStack2 = HopperBlockEntity.addItem(null, container, itemStack, null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.setItem(ItemStack.EMPTY);
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack2);
        }
        return bl;
    }

    /*
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static ItemStack addItem(@Nullable Container container, Container container2, ItemStack itemStack, @Nullable Direction direction) {
        if (container2 instanceof WorldlyContainer) {
            WorldlyContainer worldlyContainer = (WorldlyContainer)container2;
            if (direction != null) {
                int[] is = worldlyContainer.getSlotsForFace(direction);
                int i = 0;
                while (i < is.length) {
                    if (itemStack.isEmpty()) return itemStack;
                    itemStack = HopperBlockEntity.tryMoveInItem(container, container2, itemStack, is[i], direction);
                    ++i;
                }
                return itemStack;
            }
        }
        int j = container2.getContainerSize();
        int i = 0;
        while (i < j) {
            if (itemStack.isEmpty()) return itemStack;
            itemStack = HopperBlockEntity.tryMoveInItem(container, container2, itemStack, i, direction);
            ++i;
        }
        return itemStack;
    }

    private static boolean canPlaceItemInContainer(Container container, ItemStack itemStack, int i, @Nullable Direction direction) {
        WorldlyContainer worldlyContainer;
        if (!container.canPlaceItem(i, itemStack)) {
            return false;
        }
        return !(container instanceof WorldlyContainer) || (worldlyContainer = (WorldlyContainer)container).canPlaceItemThroughFace(i, itemStack, direction);
    }

    private static boolean canTakeItemFromContainer(Container container, Container container2, ItemStack itemStack, int i, Direction direction) {
        WorldlyContainer worldlyContainer;
        if (!container2.canTakeItem(container, i, itemStack)) {
            return false;
        }
        return !(container2 instanceof WorldlyContainer) || (worldlyContainer = (WorldlyContainer)container2).canTakeItemThroughFace(i, itemStack, direction);
    }

    private static ItemStack tryMoveInItem(@Nullable Container container, Container container2, ItemStack itemStack, int i, @Nullable Direction direction) {
        ItemStack itemStack2 = container2.getItem(i);
        if (HopperBlockEntity.canPlaceItemInContainer(container2, itemStack, i, direction)) {
            int k;
            boolean bl = false;
            boolean bl2 = container2.isEmpty();
            if (itemStack2.isEmpty()) {
                container2.setItem(i, itemStack);
                itemStack = ItemStack.EMPTY;
                bl = true;
            } else if (HopperBlockEntity.canMergeItems(itemStack2, itemStack)) {
                int j = itemStack.getMaxStackSize() - itemStack2.getCount();
                k = Math.min(itemStack.getCount(), j);
                itemStack.shrink(k);
                itemStack2.grow(k);
                boolean bl3 = bl = k > 0;
            }
            if (bl) {
                HopperBlockEntity hopperBlockEntity;
                if (bl2 && container2 instanceof HopperBlockEntity && !(hopperBlockEntity = (HopperBlockEntity)container2).isOnCustomCooldown()) {
                    k = 0;
                    if (container instanceof HopperBlockEntity) {
                        HopperBlockEntity hopperBlockEntity2 = (HopperBlockEntity)container;
                        if (hopperBlockEntity.tickedGameTime >= hopperBlockEntity2.tickedGameTime) {
                            k = 1;
                        }
                    }
                    hopperBlockEntity.setCooldown(8 - k);
                }
                container2.setChanged();
            }
        }
        return itemStack;
    }

    private static @Nullable Container getAttachedContainer(Level level, BlockPos blockPos, HopperBlockEntity hopperBlockEntity) {
        return HopperBlockEntity.getContainerAt(level, blockPos.relative(hopperBlockEntity.facing));
    }

    private static @Nullable Container getSourceContainer(Level level, Hopper hopper, BlockPos blockPos, BlockState blockState) {
        return HopperBlockEntity.getContainerAt(level, blockPos, blockState, hopper.getLevelX(), hopper.getLevelY() + 1.0, hopper.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level level, Hopper hopper) {
        AABB aABB = hopper.getSuckAabb().move(hopper.getLevelX() - 0.5, hopper.getLevelY() - 0.5, hopper.getLevelZ() - 0.5);
        return level.getEntitiesOfClass(ItemEntity.class, aABB, EntitySelector.ENTITY_STILL_ALIVE);
    }

    public static @Nullable Container getContainerAt(Level level, BlockPos blockPos) {
        return HopperBlockEntity.getContainerAt(level, blockPos, level.getBlockState(blockPos), (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    private static @Nullable Container getContainerAt(Level level, BlockPos blockPos, BlockState blockState, double d, double e, double f) {
        Container container = HopperBlockEntity.getBlockContainer(level, blockPos, blockState);
        if (container == null) {
            container = HopperBlockEntity.getEntityContainer(level, d, e, f);
        }
        return container;
    }

    private static @Nullable Container getBlockContainer(Level level, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity;
        Block block = blockState.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            return ((WorldlyContainerHolder)((Object)block)).getContainer(blockState, level, blockPos);
        }
        if (blockState.hasBlockEntity() && (blockEntity = level.getBlockEntity(blockPos)) instanceof Container) {
            Container container = (Container)((Object)blockEntity);
            if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
                container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
            }
            return container;
        }
        return null;
    }

    private static @Nullable Container getEntityContainer(Level level, double d, double e, double f) {
        List<Entity> list = level.getEntities((Entity)null, new AABB(d - 0.5, e - 0.5, f - 0.5, d + 0.5, e + 0.5, f + 0.5), EntitySelector.CONTAINER_ENTITY_SELECTOR);
        if (!list.isEmpty()) {
            return (Container)((Object)list.get(level.random.nextInt(list.size())));
        }
        return null;
    }

    private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getCount() <= itemStack.getMaxStackSize() && ItemStack.isSameItemSameComponents(itemStack, itemStack2);
    }

    @Override
    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5;
    }

    @Override
    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5;
    }

    @Override
    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5;
    }

    @Override
    public boolean isGridAligned() {
        return true;
    }

    private void setCooldown(int i) {
        this.cooldownTime = i;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public static void entityInside(Level level, BlockPos blockPos, BlockState blockState, Entity entity, HopperBlockEntity hopperBlockEntity) {
        ItemEntity itemEntity;
        if (entity instanceof ItemEntity && !(itemEntity = (ItemEntity)entity).getItem().isEmpty() && entity.getBoundingBox().move(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ()).intersects(hopperBlockEntity.getSuckAabb())) {
            HopperBlockEntity.tryMoveItems(level, blockPos, blockState, hopperBlockEntity, () -> HopperBlockEntity.addItem(hopperBlockEntity, itemEntity));
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new HopperMenu(i, inventory, this);
    }
}

