/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryMenu
extends AbstractCraftingMenu {
    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFTING_GRID_WIDTH = 2;
    private static final int CRAFTING_GRID_HEIGHT = 2;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final Identifier EMPTY_ARMOR_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    public static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    public static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    public static final Identifier EMPTY_ARMOR_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    public static final Identifier EMPTY_ARMOR_SLOT_SHIELD = Identifier.withDefaultNamespace("container/slot/shield");
    private static final Map<EquipmentSlot, Identifier> TEXTURE_EMPTY_SLOTS = Map.of((Object)EquipmentSlot.FEET, (Object)EMPTY_ARMOR_SLOT_BOOTS, (Object)EquipmentSlot.LEGS, (Object)EMPTY_ARMOR_SLOT_LEGGINGS, (Object)EquipmentSlot.CHEST, (Object)EMPTY_ARMOR_SLOT_CHESTPLATE, (Object)EquipmentSlot.HEAD, (Object)EMPTY_ARMOR_SLOT_HELMET);
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public final boolean active;
    private final Player owner;

    public InventoryMenu(Inventory inventory, boolean bl, final Player player) {
        super(null, 0, 2, 2);
        this.active = bl;
        this.owner = player;
        this.addResultSlot(player, 154, 28);
        this.addCraftingGridSlots(98, 18);
        for (int i = 0; i < 4; ++i) {
            EquipmentSlot equipmentSlot = SLOT_IDS[i];
            Identifier identifier = TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
            this.addSlot(new ArmorSlot(inventory, player, equipmentSlot, 39 - i, 8, 8 + i * 18, identifier));
        }
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addSlot(new Slot(this, inventory, 40, 77, 62){

            @Override
            public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
                player.onEquipItem(EquipmentSlot.OFFHAND, itemStack2, itemStack);
                super.setByPlayer(itemStack, itemStack2);
            }

            @Override
            public Identifier getNoItemIcon() {
                return EMPTY_ARMOR_SLOT_SHIELD;
            }
        });
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 36 && i < 45 || i == 45;
    }

    @Override
    public void slotsChanged(Container container) {
        Level level = this.owner.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            CraftingMenu.slotChangedCraftingGrid(this, serverLevel, this.owner, this.craftSlots, this.resultSlots, null);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultSlots.clearContent();
        if (player.level().isClientSide()) {
            return;
        }
        this.clearContainer(player, this.craftSlots);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(i);
        if (slot.hasItem()) {
            int j;
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
            if (i == 0) {
                if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (i >= 1 && i < 5 ? !this.moveItemStackTo(itemStack2, 9, 45, false) : (i >= 5 && i < 9 ? !this.moveItemStackTo(itemStack2, 9, 45, false) : (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !((Slot)this.slots.get(8 - equipmentSlot.getIndex())).hasItem() ? !this.moveItemStackTo(itemStack2, j = 8 - equipmentSlot.getIndex(), j + 1, false) : (equipmentSlot == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasItem() ? !this.moveItemStackTo(itemStack2, 45, 46, false) : (i >= 9 && i < 36 ? !this.moveItemStackTo(itemStack2, 36, 45, false) : (i >= 36 && i < 45 ? !this.moveItemStackTo(itemStack2, 9, 36, false) : !this.moveItemStackTo(itemStack2, 9, 45, false))))))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemStack);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
            if (i == 0) {
                player.drop(itemStack2, false);
            }
        }
        return itemStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override
    public Slot getResultSlot() {
        return (Slot)this.slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return this.slots.subList(1, 5);
    }

    public CraftingContainer getCraftSlots() {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return this.owner;
    }
}

