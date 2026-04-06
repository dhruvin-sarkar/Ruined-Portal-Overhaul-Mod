/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_CLICKED_OUTSIDE = -999;
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;
    public static final int QUICKCRAFT_TYPE_CLONE = 2;
    public static final int QUICKCRAFT_HEADER_START = 0;
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
    public static final int QUICKCRAFT_HEADER_END = 2;
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
    public static final int SLOTS_PER_ROW = 9;
    public static final int SLOT_SIZE = 18;
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private ItemStack carried = ItemStack.EMPTY;
    private final NonNullList<RemoteSlot> remoteSlots = NonNullList.create();
    private final IntList remoteDataSlots = new IntArrayList();
    private RemoteSlot remoteCarried = RemoteSlot.PLACEHOLDER;
    private int stateId;
    private final @Nullable MenuType<?> menuType;
    public final int containerId;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    private @Nullable ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

    protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int i) {
        this.menuType = menuType;
        this.containerId = i;
    }

    protected void addInventoryHotbarSlots(Container container, int i, int j) {
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(container, k, i + k * 18, j));
        }
    }

    protected void addInventoryExtendedSlots(Container container, int i, int j) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(container, l + (k + 1) * 9, i + l * 18, j + k * 18));
            }
        }
    }

    protected void addStandardInventorySlots(Container container, int i, int j) {
        this.addInventoryExtendedSlots(container, i, j);
        int k = 4;
        int l = 58;
        this.addInventoryHotbarSlots(container, i, j + 58);
    }

    protected static boolean stillValid(ContainerLevelAccess containerLevelAccess, Player player, Block block) {
        return containerLevelAccess.evaluate((level, blockPos) -> {
            if (!level.getBlockState((BlockPos)blockPos).is(block)) {
                return false;
            }
            return player.isWithinBlockInteractionRange((BlockPos)blockPos, 4.0);
        }, true);
    }

    public MenuType<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.menuType;
    }

    protected static void checkContainerSize(Container container, int i) {
        int j = container.getContainerSize();
        if (j < i) {
            throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + i);
        }
    }

    protected static void checkContainerDataCount(ContainerData containerData, int i) {
        int j = containerData.getCount();
        if (j < i) {
            throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + i);
        }
    }

    public boolean isValidSlotIndex(int i) {
        return i == -1 || i == -999 || i < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        this.remoteSlots.add(this.synchronizer != null ? this.synchronizer.createSlot() : RemoteSlot.PLACEHOLDER);
        return slot;
    }

    protected DataSlot addDataSlot(DataSlot dataSlot) {
        this.dataSlots.add(dataSlot);
        this.remoteDataSlots.add(0);
        return dataSlot;
    }

    protected void addDataSlots(ContainerData containerData) {
        for (int i = 0; i < containerData.getCount(); ++i) {
            this.addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }

    public void addSlotListener(ContainerListener containerListener) {
        if (this.containerListeners.contains(containerListener)) {
            return;
        }
        this.containerListeners.add(containerListener);
        this.broadcastChanges();
    }

    public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
        this.synchronizer = containerSynchronizer;
        this.remoteCarried = containerSynchronizer.createSlot();
        this.remoteSlots.replaceAll(remoteSlot -> containerSynchronizer.createSlot());
        this.sendAllDataToRemote();
    }

    public void sendAllDataToRemote() {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(this.slots.size());
        int j = this.slots.size();
        for (int i = 0; i < j; ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            list.add(itemStack.copy());
            this.remoteSlots.get(i).force(itemStack);
        }
        ItemStack itemStack2 = this.getCarried();
        this.remoteCarried.force(itemStack2);
        int k = this.dataSlots.size();
        for (j = 0; j < k; ++j) {
            this.remoteDataSlots.set(j, this.dataSlots.get(j).get());
        }
        if (this.synchronizer != null) {
            this.synchronizer.sendInitialData(this, list, itemStack2.copy(), this.remoteDataSlots.toIntArray());
        }
    }

    public void removeSlotListener(ContainerListener containerListener) {
        this.containerListeners.remove(containerListener);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (Slot slot : this.slots) {
            nonNullList.add(slot.getItem());
        }
        return nonNullList;
    }

    public void broadcastChanges() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            com.google.common.base.Supplier supplier = Suppliers.memoize(itemStack::copy);
            this.triggerSlotListeners(i, itemStack, (Supplier<ItemStack>)supplier);
            this.synchronizeSlotToRemote(i, itemStack, (Supplier<ItemStack>)supplier);
        }
        this.synchronizeCarriedToRemote();
        for (i = 0; i < this.dataSlots.size(); ++i) {
            DataSlot dataSlot = this.dataSlots.get(i);
            int j = dataSlot.get();
            if (dataSlot.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(i, j);
            }
            this.synchronizeDataSlotToRemote(i, j);
        }
    }

    public void broadcastFullState() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            this.triggerSlotListeners(i, itemStack, itemStack::copy);
        }
        for (i = 0; i < this.dataSlots.size(); ++i) {
            DataSlot dataSlot = this.dataSlots.get(i);
            if (!dataSlot.checkAndClearUpdateFlag()) continue;
            this.updateDataSlotListeners(i, dataSlot.get());
        }
        this.sendAllDataToRemote();
    }

    private void updateDataSlotListeners(int i, int j) {
        for (ContainerListener containerListener : this.containerListeners) {
            containerListener.dataChanged(this, i, j);
        }
    }

    private void triggerSlotListeners(int i, ItemStack itemStack, Supplier<ItemStack> supplier) {
        ItemStack itemStack2 = this.lastSlots.get(i);
        if (!ItemStack.matches(itemStack2, itemStack)) {
            ItemStack itemStack3 = supplier.get();
            this.lastSlots.set(i, itemStack3);
            for (ContainerListener containerListener : this.containerListeners) {
                containerListener.slotChanged(this, i, itemStack3);
            }
        }
    }

    private void synchronizeSlotToRemote(int i, ItemStack itemStack, Supplier<ItemStack> supplier) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        RemoteSlot remoteSlot = this.remoteSlots.get(i);
        if (!remoteSlot.matches(itemStack)) {
            remoteSlot.force(itemStack);
            if (this.synchronizer != null) {
                this.synchronizer.sendSlotChange(this, i, supplier.get());
            }
        }
    }

    private void synchronizeDataSlotToRemote(int i, int j) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        int k = this.remoteDataSlots.getInt(i);
        if (k != j) {
            this.remoteDataSlots.set(i, j);
            if (this.synchronizer != null) {
                this.synchronizer.sendDataChange(this, i, j);
            }
        }
    }

    private void synchronizeCarriedToRemote() {
        if (this.suppressRemoteUpdates) {
            return;
        }
        ItemStack itemStack = this.getCarried();
        if (!this.remoteCarried.matches(itemStack)) {
            this.remoteCarried.force(itemStack);
            if (this.synchronizer != null) {
                this.synchronizer.sendCarriedChange(this, itemStack.copy());
            }
        }
    }

    public void setRemoteSlot(int i, ItemStack itemStack) {
        this.remoteSlots.get(i).force(itemStack);
    }

    public void setRemoteSlotUnsafe(int i, HashedStack hashedStack) {
        if (i < 0 || i >= this.remoteSlots.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)i, (Object)this.remoteSlots.size());
            return;
        }
        this.remoteSlots.get(i).receive(hashedStack);
    }

    public void setRemoteCarried(HashedStack hashedStack) {
        this.remoteCarried.receive(hashedStack);
    }

    public boolean clickMenuButton(Player player, int i) {
        return false;
    }

    public Slot getSlot(int i) {
        return this.slots.get(i);
    }

    public abstract ItemStack quickMoveStack(Player var1, int var2);

    public void setSelectedBundleItemIndex(int i, int j) {
        if (i >= 0 && i < this.slots.size()) {
            ItemStack itemStack = this.slots.get(i).getItem();
            BundleItem.toggleSelectedItem(itemStack, j);
        }
    }

    public void clicked(int i, int j, ClickType clickType, Player player) {
        try {
            this.doClick(i, j, clickType, player);
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
            crashReportCategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashReportCategory.setDetail("Slot Count", this.slots.size());
            crashReportCategory.setDetail("Slot", i);
            crashReportCategory.setDetail("Button", j);
            crashReportCategory.setDetail("Type", (Object)clickType);
            throw new ReportedException(crashReport);
        }
    }

    private void doClick(int i, int j, ClickType clickType, Player player) {
        block40: {
            block52: {
                int l;
                block51: {
                    block47: {
                        ItemStack itemStack3;
                        Slot slot;
                        ItemStack itemStack5;
                        Inventory inventory;
                        block50: {
                            block49: {
                                block48: {
                                    block45: {
                                        ClickAction clickAction;
                                        block46: {
                                            block44: {
                                                block38: {
                                                    block43: {
                                                        ItemStack itemStack4;
                                                        block42: {
                                                            block41: {
                                                                block39: {
                                                                    inventory = player.getInventory();
                                                                    if (clickType != ClickType.QUICK_CRAFT) break block38;
                                                                    int k = this.quickcraftStatus;
                                                                    this.quickcraftStatus = AbstractContainerMenu.getQuickcraftHeader(j);
                                                                    if (k == 1 && this.quickcraftStatus == 2 || k == this.quickcraftStatus) break block39;
                                                                    this.resetQuickCraft();
                                                                    break block40;
                                                                }
                                                                if (!this.getCarried().isEmpty()) break block41;
                                                                this.resetQuickCraft();
                                                                break block40;
                                                            }
                                                            if (this.quickcraftStatus != 0) break block42;
                                                            this.quickcraftType = AbstractContainerMenu.getQuickcraftType(j);
                                                            if (AbstractContainerMenu.isValidQuickcraftType(this.quickcraftType, player)) {
                                                                this.quickcraftStatus = 1;
                                                                this.quickcraftSlots.clear();
                                                            } else {
                                                                this.resetQuickCraft();
                                                            }
                                                            break block40;
                                                        }
                                                        if (this.quickcraftStatus != 1) break block43;
                                                        Slot slot2 = this.slots.get(i);
                                                        if (!AbstractContainerMenu.canItemQuickReplace(slot2, itemStack4 = this.getCarried(), true) || !slot2.mayPlace(itemStack4) || this.quickcraftType != 2 && itemStack4.getCount() <= this.quickcraftSlots.size() || !this.canDragTo(slot2)) break block40;
                                                        this.quickcraftSlots.add(slot2);
                                                        break block40;
                                                    }
                                                    if (this.quickcraftStatus == 2) {
                                                        if (!this.quickcraftSlots.isEmpty()) {
                                                            if (this.quickcraftSlots.size() == 1) {
                                                                int l2 = this.quickcraftSlots.iterator().next().index;
                                                                this.resetQuickCraft();
                                                                this.doClick(l2, this.quickcraftType, ClickType.PICKUP, player);
                                                                return;
                                                            }
                                                            ItemStack itemStack22 = this.getCarried().copy();
                                                            if (itemStack22.isEmpty()) {
                                                                this.resetQuickCraft();
                                                                return;
                                                            }
                                                            int m = this.getCarried().getCount();
                                                            for (Slot slot2 : this.quickcraftSlots) {
                                                                ItemStack itemStack32 = this.getCarried();
                                                                if (slot2 == null || !AbstractContainerMenu.canItemQuickReplace(slot2, itemStack32, true) || !slot2.mayPlace(itemStack32) || this.quickcraftType != 2 && itemStack32.getCount() < this.quickcraftSlots.size() || !this.canDragTo(slot2)) continue;
                                                                int n = slot2.hasItem() ? slot2.getItem().getCount() : 0;
                                                                int o = Math.min(itemStack22.getMaxStackSize(), slot2.getMaxStackSize(itemStack22));
                                                                int p = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemStack22) + n, o);
                                                                m -= p - n;
                                                                slot2.setByPlayer(itemStack22.copyWithCount(p));
                                                            }
                                                            itemStack22.setCount(m);
                                                            this.setCarried(itemStack22);
                                                        }
                                                        this.resetQuickCraft();
                                                    } else {
                                                        this.resetQuickCraft();
                                                    }
                                                    break block40;
                                                }
                                                if (this.quickcraftStatus == 0) break block44;
                                                this.resetQuickCraft();
                                                break block40;
                                            }
                                            if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE || j != 0 && j != 1) break block45;
                                            ClickAction clickAction2 = clickAction = j == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
                                            if (i != -999) break block46;
                                            if (this.getCarried().isEmpty()) break block40;
                                            if (clickAction == ClickAction.PRIMARY) {
                                                player.drop(this.getCarried(), true);
                                                this.setCarried(ItemStack.EMPTY);
                                            } else {
                                                player.drop(this.getCarried().split(1), true);
                                            }
                                            break block40;
                                        }
                                        if (clickType == ClickType.QUICK_MOVE) {
                                            if (i < 0) {
                                                return;
                                            }
                                            Slot slot3 = this.slots.get(i);
                                            if (!slot3.mayPickup(player)) {
                                                return;
                                            }
                                            ItemStack itemStack6 = this.quickMoveStack(player, i);
                                            while (!itemStack6.isEmpty() && ItemStack.isSameItem(slot3.getItem(), itemStack6)) {
                                                itemStack6 = this.quickMoveStack(player, i);
                                            }
                                        } else {
                                            if (i < 0) {
                                                return;
                                            }
                                            Slot slot4 = this.slots.get(i);
                                            ItemStack itemStack7 = slot4.getItem();
                                            ItemStack itemStack4 = this.getCarried();
                                            player.updateTutorialInventoryAction(itemStack4, slot4.getItem(), clickAction);
                                            if (!this.tryItemClickBehaviourOverride(player, clickAction, slot4, itemStack7, itemStack4)) {
                                                if (itemStack7.isEmpty()) {
                                                    if (!itemStack4.isEmpty()) {
                                                        int q = clickAction == ClickAction.PRIMARY ? itemStack4.getCount() : 1;
                                                        this.setCarried(slot4.safeInsert(itemStack4, q));
                                                    }
                                                } else if (slot4.mayPickup(player)) {
                                                    if (itemStack4.isEmpty()) {
                                                        int q = clickAction == ClickAction.PRIMARY ? itemStack7.getCount() : (itemStack7.getCount() + 1) / 2;
                                                        Optional<ItemStack> optional = slot4.tryRemove(q, Integer.MAX_VALUE, player);
                                                        optional.ifPresent(itemStack -> {
                                                            this.setCarried((ItemStack)itemStack);
                                                            slot4.onTake(player, (ItemStack)itemStack);
                                                        });
                                                    } else if (slot4.mayPlace(itemStack4)) {
                                                        if (ItemStack.isSameItemSameComponents(itemStack7, itemStack4)) {
                                                            int q = clickAction == ClickAction.PRIMARY ? itemStack4.getCount() : 1;
                                                            this.setCarried(slot4.safeInsert(itemStack4, q));
                                                        } else if (itemStack4.getCount() <= slot4.getMaxStackSize(itemStack4)) {
                                                            this.setCarried(itemStack7);
                                                            slot4.setByPlayer(itemStack4);
                                                        }
                                                    } else if (ItemStack.isSameItemSameComponents(itemStack7, itemStack4)) {
                                                        Optional<ItemStack> optional2 = slot4.tryRemove(itemStack7.getCount(), itemStack4.getMaxStackSize() - itemStack4.getCount(), player);
                                                        optional2.ifPresent(itemStack2 -> {
                                                            itemStack4.grow(itemStack2.getCount());
                                                            slot4.onTake(player, (ItemStack)itemStack2);
                                                        });
                                                    }
                                                }
                                            }
                                            slot4.setChanged();
                                        }
                                        break block40;
                                    }
                                    if (clickType != ClickType.SWAP || (j < 0 || j >= 9) && j != 40) break block47;
                                    itemStack5 = inventory.getItem(j);
                                    slot = this.slots.get(i);
                                    itemStack3 = slot.getItem();
                                    if (itemStack5.isEmpty() && itemStack3.isEmpty()) break block40;
                                    if (!itemStack5.isEmpty()) break block48;
                                    if (!slot.mayPickup(player)) break block40;
                                    inventory.setItem(j, itemStack3);
                                    slot.onSwapCraft(itemStack3.getCount());
                                    slot.setByPlayer(ItemStack.EMPTY);
                                    slot.onTake(player, itemStack3);
                                    break block40;
                                }
                                if (!itemStack3.isEmpty()) break block49;
                                if (!slot.mayPlace(itemStack5)) break block40;
                                int r = slot.getMaxStackSize(itemStack5);
                                if (itemStack5.getCount() > r) {
                                    slot.setByPlayer(itemStack5.split(r));
                                } else {
                                    inventory.setItem(j, ItemStack.EMPTY);
                                    slot.setByPlayer(itemStack5);
                                }
                                break block40;
                            }
                            if (!slot.mayPickup(player) || !slot.mayPlace(itemStack5)) break block40;
                            int r = slot.getMaxStackSize(itemStack5);
                            if (itemStack5.getCount() <= r) break block50;
                            slot.setByPlayer(itemStack5.split(r));
                            slot.onTake(player, itemStack3);
                            if (inventory.add(itemStack3)) break block40;
                            player.drop(itemStack3, true);
                            break block40;
                        }
                        inventory.setItem(j, itemStack3);
                        slot.setByPlayer(itemStack5);
                        slot.onTake(player, itemStack3);
                        break block40;
                    }
                    if (clickType != ClickType.CLONE || !player.hasInfiniteMaterials() || !this.getCarried().isEmpty() || i < 0) break block51;
                    Slot slot3 = this.slots.get(i);
                    if (!slot3.hasItem()) break block40;
                    ItemStack itemStack23 = slot3.getItem();
                    this.setCarried(itemStack23.copyWithCount(itemStack23.getMaxStackSize()));
                    break block40;
                }
                if (clickType != ClickType.THROW || !this.getCarried().isEmpty() || i < 0) break block52;
                Slot slot3 = this.slots.get(i);
                int n = l = j == 0 ? 1 : slot3.getItem().getCount();
                if (!player.canDropItems()) {
                    return;
                }
                ItemStack itemStack8 = slot3.safeTake(l, Integer.MAX_VALUE, player);
                player.drop(itemStack8, true);
                player.handleCreativeModeItemDrop(itemStack8);
                if (j != 1) break block40;
                while (!itemStack8.isEmpty() && ItemStack.isSameItem(slot3.getItem(), itemStack8)) {
                    if (!player.canDropItems()) {
                        return;
                    }
                    itemStack8 = slot3.safeTake(l, Integer.MAX_VALUE, player);
                    player.drop(itemStack8, true);
                    player.handleCreativeModeItemDrop(itemStack8);
                }
                break block40;
            }
            if (clickType == ClickType.PICKUP_ALL && i >= 0) {
                Slot slot3 = this.slots.get(i);
                ItemStack itemStack24 = this.getCarried();
                if (!(itemStack24.isEmpty() || slot3.hasItem() && slot3.mayPickup(player))) {
                    int m = j == 0 ? 0 : this.slots.size() - 1;
                    int r = j == 0 ? 1 : -1;
                    for (int q = 0; q < 2; ++q) {
                        for (int s = m; s >= 0 && s < this.slots.size() && itemStack24.getCount() < itemStack24.getMaxStackSize(); s += r) {
                            Slot slot4 = this.slots.get(s);
                            if (!slot4.hasItem() || !AbstractContainerMenu.canItemQuickReplace(slot4, itemStack24, true) || !slot4.mayPickup(player) || !this.canTakeItemForPickAll(itemStack24, slot4)) continue;
                            ItemStack itemStack6 = slot4.getItem();
                            if (q == 0 && itemStack6.getCount() == itemStack6.getMaxStackSize()) continue;
                            ItemStack itemStack7 = slot4.safeTake(itemStack6.getCount(), itemStack24.getMaxStackSize() - itemStack24.getCount(), player);
                            itemStack24.grow(itemStack7.getCount());
                        }
                    }
                }
            }
        }
    }

    private boolean tryItemClickBehaviourOverride(Player player, ClickAction clickAction, Slot slot, ItemStack itemStack, ItemStack itemStack2) {
        FeatureFlagSet featureFlagSet = player.level().enabledFeatures();
        if (itemStack2.isItemEnabled(featureFlagSet) && itemStack2.overrideStackedOnOther(slot, clickAction, player)) {
            return true;
        }
        return itemStack.isItemEnabled(featureFlagSet) && itemStack.overrideOtherStackedOnMe(itemStack2, slot, clickAction, player, this.createCarriedSlotAccess());
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return AbstractContainerMenu.this.getCarried();
            }

            @Override
            public boolean set(ItemStack itemStack) {
                AbstractContainerMenu.this.setCarried(itemStack);
                return true;
            }
        };
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return true;
    }

    public void removed(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ItemStack itemStack = this.getCarried();
        if (!itemStack.isEmpty()) {
            AbstractContainerMenu.dropOrPlaceInInventory(player, itemStack);
            this.setCarried(ItemStack.EMPTY);
        }
    }

    private static void dropOrPlaceInInventory(Player player, ItemStack itemStack) {
        ServerPlayer serverPlayer;
        boolean bl2;
        boolean bl = player.isRemoved() && player.getRemovalReason() != Entity.RemovalReason.CHANGED_DIMENSION;
        boolean bl3 = bl2 = player instanceof ServerPlayer && (serverPlayer = (ServerPlayer)player).hasDisconnected();
        if (bl || bl2) {
            player.drop(itemStack, false);
        } else if (player instanceof ServerPlayer) {
            player.getInventory().placeItemBackInInventory(itemStack);
        }
    }

    protected void clearContainer(Player player, Container container) {
        for (int i = 0; i < container.getContainerSize(); ++i) {
            AbstractContainerMenu.dropOrPlaceInInventory(player, container.removeItemNoUpdate(i));
        }
    }

    public void slotsChanged(Container container) {
        this.broadcastChanges();
    }

    public void setItem(int i, int j, ItemStack itemStack) {
        this.getSlot(i).set(itemStack);
        this.stateId = j;
    }

    public void initializeContents(int i, List<ItemStack> list, ItemStack itemStack) {
        for (int j = 0; j < list.size(); ++j) {
            this.getSlot(j).set(list.get(j));
        }
        this.carried = itemStack;
        this.stateId = i;
    }

    public void setData(int i, int j) {
        this.dataSlots.get(i).set(j);
    }

    public abstract boolean stillValid(Player var1);

    protected boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl) {
        int l;
        ItemStack itemStack2;
        Slot slot;
        boolean bl2 = false;
        int k = i;
        if (bl) {
            k = j - 1;
        }
        if (itemStack.isStackable()) {
            while (!itemStack.isEmpty() && (bl ? k >= i : k < j)) {
                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (!itemStack2.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
                    int m;
                    l = itemStack2.getCount() + itemStack.getCount();
                    if (l <= (m = slot.getMaxStackSize(itemStack2))) {
                        itemStack.setCount(0);
                        itemStack2.setCount(l);
                        slot.setChanged();
                        bl2 = true;
                    } else if (itemStack2.getCount() < m) {
                        itemStack.shrink(m - itemStack2.getCount());
                        itemStack2.setCount(m);
                        slot.setChanged();
                        bl2 = true;
                    }
                }
                if (bl) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        if (!itemStack.isEmpty()) {
            k = bl ? j - 1 : i;
            while (bl ? k >= i : k < j) {
                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (itemStack2.isEmpty() && slot.mayPlace(itemStack)) {
                    l = slot.getMaxStackSize(itemStack);
                    slot.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), l)));
                    slot.setChanged();
                    bl2 = true;
                    break;
                }
                if (bl) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        return bl2;
    }

    public static int getQuickcraftType(int i) {
        return i >> 2 & 3;
    }

    public static int getQuickcraftHeader(int i) {
        return i & 3;
    }

    public static int getQuickcraftMask(int i, int j) {
        return i & 3 | (j & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int i, Player player) {
        if (i == 0) {
            return true;
        }
        if (i == 1) {
            return true;
        }
        return i == 2 && player.hasInfiniteMaterials();
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = slot == null || !slot.hasItem();
        if (!bl2 && ItemStack.isSameItemSameComponents(itemStack, slot.getItem())) {
            return slot.getItem().getCount() + (bl ? 0 : itemStack.getCount()) <= itemStack.getMaxStackSize();
        }
        return bl2;
    }

    public static int getQuickCraftPlaceCount(Set<Slot> set, int i, ItemStack itemStack) {
        return switch (i) {
            case 0 -> Mth.floor((float)itemStack.getCount() / (float)set.size());
            case 1 -> 1;
            case 2 -> itemStack.getMaxStackSize();
            default -> itemStack.getCount();
        };
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((Object)blockEntity));
        }
        return 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container container) {
        if (container == null) {
            return 0;
        }
        float f = 0.0f;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty()) continue;
            f += (float)itemStack.getCount() / (float)container.getMaxStackSize(itemStack);
        }
        return Mth.lerpDiscrete(f /= (float)container.getContainerSize(), 0, 15);
    }

    public void setCarried(ItemStack itemStack) {
        this.carried = itemStack;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(AbstractContainerMenu abstractContainerMenu) {
        Slot slot;
        int i;
        HashBasedTable table = HashBasedTable.create();
        for (i = 0; i < abstractContainerMenu.slots.size(); ++i) {
            slot = abstractContainerMenu.slots.get(i);
            table.put((Object)slot.container, (Object)slot.getContainerSlot(), (Object)i);
        }
        for (i = 0; i < this.slots.size(); ++i) {
            slot = this.slots.get(i);
            Integer integer = (Integer)table.get((Object)slot.container, (Object)slot.getContainerSlot());
            if (integer == null) continue;
            this.lastSlots.set(i, abstractContainerMenu.lastSlots.get(integer));
            RemoteSlot remoteSlot = abstractContainerMenu.remoteSlots.get(integer);
            RemoteSlot remoteSlot2 = this.remoteSlots.get(i);
            if (!(remoteSlot instanceof RemoteSlot.Synchronized)) continue;
            RemoteSlot.Synchronized synchronized_ = (RemoteSlot.Synchronized)remoteSlot;
            if (!(remoteSlot2 instanceof RemoteSlot.Synchronized)) continue;
            RemoteSlot.Synchronized synchronized2 = (RemoteSlot.Synchronized)remoteSlot2;
            synchronized2.copyFrom(synchronized_);
        }
    }

    public OptionalInt findSlot(Container container, int i) {
        for (int j = 0; j < this.slots.size(); ++j) {
            Slot slot = this.slots.get(j);
            if (slot.container != container || i != slot.getContainerSlot()) continue;
            return OptionalInt.of(j);
        }
        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & Short.MAX_VALUE;
        return this.stateId;
    }
}

