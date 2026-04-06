/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 */
package net.minecraft.world.entity.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Inventory
implements Container,
Nameable {
    public static final int POP_TIME_DURATION = 5;
    public static final int INVENTORY_SIZE = 36;
    public static final int SELECTION_SIZE = 9;
    public static final int SLOT_OFFHAND = 40;
    public static final int SLOT_BODY_ARMOR = 41;
    public static final int SLOT_SADDLE = 42;
    public static final int NOT_FOUND_INDEX = -1;
    public static final Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOT_MAPPING = new Int2ObjectArrayMap(Map.of((Object)EquipmentSlot.FEET.getIndex(36), (Object)EquipmentSlot.FEET, (Object)EquipmentSlot.LEGS.getIndex(36), (Object)EquipmentSlot.LEGS, (Object)EquipmentSlot.CHEST.getIndex(36), (Object)EquipmentSlot.CHEST, (Object)EquipmentSlot.HEAD.getIndex(36), (Object)EquipmentSlot.HEAD, (Object)40, (Object)EquipmentSlot.OFFHAND, (Object)41, (Object)EquipmentSlot.BODY, (Object)42, (Object)EquipmentSlot.SADDLE));
    private static final Component DEFAULT_NAME = Component.translatable("container.inventory");
    private final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
    private int selected;
    public final Player player;
    private final EntityEquipment equipment;
    private int timesChanged;

    public Inventory(Player player, EntityEquipment entityEquipment) {
        this.player = player;
        this.equipment = entityEquipment;
    }

    public int getSelectedSlot() {
        return this.selected;
    }

    public void setSelectedSlot(int i) {
        if (!Inventory.isHotbarSlot(i)) {
            throw new IllegalArgumentException("Invalid selected slot");
        }
        this.selected = i;
    }

    public ItemStack getSelectedItem() {
        return this.items.get(this.selected);
    }

    public ItemStack setSelectedItem(ItemStack itemStack) {
        return this.items.set(this.selected, itemStack);
    }

    public static int getSelectionSize() {
        return 9;
    }

    public NonNullList<ItemStack> getNonEquipmentItems() {
        return this.items;
    }

    private boolean hasRemainingSpaceForItem(ItemStack itemStack, ItemStack itemStack2) {
        return !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2) && itemStack.isStackable() && itemStack.getCount() < this.getMaxStackSize(itemStack);
    }

    public int getFreeSlot() {
        for (int i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public void addAndPickItem(ItemStack itemStack) {
        int i;
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        if (!this.items.get(this.selected).isEmpty() && (i = this.getFreeSlot()) != -1) {
            this.items.set(i, this.items.get(this.selected));
        }
        this.items.set(this.selected, itemStack);
    }

    public void pickSlot(int i) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        ItemStack itemStack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(i));
        this.items.set(i, itemStack);
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 0 && i < 9;
    }

    public int findSlotMatchingItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty() || !ItemStack.isSameItemSameComponents(itemStack, this.items.get(i))) continue;
            return i;
        }
        return -1;
    }

    public static boolean isUsableForCrafting(ItemStack itemStack) {
        return !itemStack.isDamaged() && !itemStack.isEnchanted() && !itemStack.has(DataComponents.CUSTOM_NAME);
    }

    public int findSlotMatchingCraftingIngredient(Holder<Item> holder, ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack2 = this.items.get(i);
            if (itemStack2.isEmpty() || !itemStack2.is(holder) || !Inventory.isUsableForCrafting(itemStack2) || !itemStack.isEmpty() && !ItemStack.isSameItemSameComponents(itemStack, itemStack2)) continue;
            return i;
        }
        return -1;
    }

    public int getSuitableHotbarSlot() {
        int j;
        int i;
        for (i = 0; i < 9; ++i) {
            j = (this.selected + i) % 9;
            if (!this.items.get(j).isEmpty()) continue;
            return j;
        }
        for (i = 0; i < 9; ++i) {
            j = (this.selected + i) % 9;
            if (this.items.get(j).isEnchanted()) continue;
            return j;
        }
        return this.selected;
    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int i, Container container) {
        int j = 0;
        boolean bl = i == 0;
        j += ContainerHelper.clearOrCountMatchingItems(this, predicate, i - j, bl);
        j += ContainerHelper.clearOrCountMatchingItems(container, predicate, i - j, bl);
        ItemStack itemStack = this.player.containerMenu.getCarried();
        j += ContainerHelper.clearOrCountMatchingItems(itemStack, predicate, i - j, bl);
        if (itemStack.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        return j;
    }

    private int addResource(ItemStack itemStack) {
        int i = this.getSlotWithRemainingSpace(itemStack);
        if (i == -1) {
            i = this.getFreeSlot();
        }
        if (i == -1) {
            return itemStack.getCount();
        }
        return this.addResource(i, itemStack);
    }

    private int addResource(int i, ItemStack itemStack) {
        int k;
        int l;
        int j = itemStack.getCount();
        ItemStack itemStack2 = this.getItem(i);
        if (itemStack2.isEmpty()) {
            itemStack2 = itemStack.copyWithCount(0);
            this.setItem(i, itemStack2);
        }
        if ((l = Math.min(j, k = this.getMaxStackSize(itemStack2) - itemStack2.getCount())) == 0) {
            return j;
        }
        itemStack2.grow(l);
        itemStack2.setPopTime(5);
        return j -= l;
    }

    public int getSlotWithRemainingSpace(ItemStack itemStack) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), itemStack)) {
            return this.selected;
        }
        if (this.hasRemainingSpaceForItem(this.getItem(40), itemStack)) {
            return 40;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            if (!this.hasRemainingSpaceForItem(this.items.get(i), itemStack)) continue;
            return i;
        }
        return -1;
    }

    public void tick() {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            itemStack.inventoryTick(this.player.level(), this.player, i == this.selected ? EquipmentSlot.MAINHAND : null);
        }
    }

    public boolean add(ItemStack itemStack) {
        return this.add(-1, itemStack);
    }

    public boolean add(int i, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        try {
            if (!itemStack.isDamaged()) {
                int j;
                do {
                    j = itemStack.getCount();
                    if (i == -1) {
                        itemStack.setCount(this.addResource(itemStack));
                        continue;
                    }
                    itemStack.setCount(this.addResource(i, itemStack));
                } while (!itemStack.isEmpty() && itemStack.getCount() < j);
                if (itemStack.getCount() == j && this.player.hasInfiniteMaterials()) {
                    itemStack.setCount(0);
                    return true;
                }
                return itemStack.getCount() < j;
            }
            if (i == -1) {
                i = this.getFreeSlot();
            }
            if (i >= 0) {
                this.items.set(i, itemStack.copyAndClear());
                this.items.get(i).setPopTime(5);
                return true;
            }
            if (this.player.hasInfiniteMaterials()) {
                itemStack.setCount(0);
                return true;
            }
            return false;
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
            crashReportCategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
            crashReportCategory.setDetail("Item data", itemStack.getDamageValue());
            crashReportCategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
            throw new ReportedException(crashReport);
        }
    }

    public void placeItemBackInInventory(ItemStack itemStack) {
        this.placeItemBackInInventory(itemStack, true);
    }

    public void placeItemBackInInventory(ItemStack itemStack, boolean bl) {
        while (!itemStack.isEmpty()) {
            Player player;
            int i = this.getSlotWithRemainingSpace(itemStack);
            if (i == -1) {
                i = this.getFreeSlot();
            }
            if (i == -1) {
                this.player.drop(itemStack, false);
                break;
            }
            int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
            if (!this.add(i, itemStack.split(j)) || !bl || !((player = this.player) instanceof ServerPlayer)) continue;
            ServerPlayer serverPlayer = (ServerPlayer)player;
            serverPlayer.connection.send(this.createInventoryUpdatePacket(i));
        }
    }

    public ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int i) {
        return new ClientboundSetPlayerInventoryPacket(i, this.getItem(i).copy());
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack;
        if (i < this.items.size()) {
            return ContainerHelper.removeItem(this.items, i, j);
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(i);
        if (equipmentSlot != null && !(itemStack = this.equipment.get(equipmentSlot)).isEmpty()) {
            return itemStack.split(j);
        }
        return ItemStack.EMPTY;
    }

    public void removeItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i) != itemStack) continue;
            this.items.set(i, ItemStack.EMPTY);
            return;
        }
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            ItemStack itemStack2 = this.equipment.get(equipmentSlot);
            if (itemStack2 != itemStack) continue;
            this.equipment.set(equipmentSlot, ItemStack.EMPTY);
            return;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        if (i < this.items.size()) {
            ItemStack itemStack = this.items.get(i);
            this.items.set(i, ItemStack.EMPTY);
            return itemStack;
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(i);
        if (equipmentSlot != null) {
            return this.equipment.set(equipmentSlot, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        EquipmentSlot equipmentSlot;
        if (i < this.items.size()) {
            this.items.set(i, itemStack);
        }
        if ((equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(i)) != null) {
            this.equipment.set(equipmentSlot, itemStack);
        }
    }

    public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty()) continue;
            typedOutputList.add(new ItemStackWithSlot(i, itemStack));
        }
    }

    public void load(ValueInput.TypedInputList<ItemStackWithSlot> typedInputList) {
        this.items.clear();
        for (ItemStackWithSlot itemStackWithSlot : typedInputList) {
            if (!itemStackWithSlot.isValidInContainer(this.items.size())) continue;
            this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + EQUIPMENT_SLOT_MAPPING.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            if (this.equipment.get(equipmentSlot).isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        if (i < this.items.size()) {
            return this.items.get(i);
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(i);
        if (equipmentSlot != null) {
            return this.equipment.get(equipmentSlot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName() {
        return DEFAULT_NAME;
    }

    public void dropAll() {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty()) continue;
            this.player.drop(itemStack, true, false);
            this.items.set(i, ItemStack.EMPTY);
        }
        this.equipment.dropAll(this.player);
    }

    @Override
    public void setChanged() {
        ++this.timesChanged;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public boolean contains(ItemStack itemStack) {
        for (ItemStack itemStack2 : this) {
            if (itemStack2.isEmpty() || !ItemStack.isSameItemSameComponents(itemStack2, itemStack)) continue;
            return true;
        }
        return false;
    }

    public boolean contains(TagKey<Item> tagKey) {
        for (ItemStack itemStack : this) {
            if (itemStack.isEmpty() || !itemStack.is(tagKey)) continue;
            return true;
        }
        return false;
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (ItemStack itemStack : this) {
            if (!predicate.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public void replaceWith(Inventory inventory) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, inventory.getItem(i));
        }
        this.setSelectedSlot(inventory.getSelectedSlot());
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.equipment.clear();
    }

    public void fillStackedContents(StackedItemContents stackedItemContents) {
        for (ItemStack itemStack : this.items) {
            stackedItemContents.accountSimpleStack(itemStack);
        }
    }

    public ItemStack removeFromSelected(boolean bl) {
        ItemStack itemStack = this.getSelectedItem();
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.removeItem(this.selected, bl ? itemStack.getCount() : 1);
    }
}

