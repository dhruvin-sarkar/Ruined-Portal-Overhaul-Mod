/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<R extends Recipe<?>> {
    private static final int ITEM_NOT_FOUND = -1;
    private final Inventory inventory;
    private final CraftingMenuAccess<R> menu;
    private final boolean useMaxItems;
    private final int gridWidth;
    private final int gridHeight;
    private final List<Slot> inputGridSlots;
    private final List<Slot> slotsToClear;

    public static <I extends RecipeInput, R extends Recipe<I>> RecipeBookMenu.PostPlaceAction placeRecipe(CraftingMenuAccess<R> craftingMenuAccess, int i, int j, List<Slot> list, List<Slot> list2, Inventory inventory, RecipeHolder<R> recipeHolder, boolean bl, boolean bl2) {
        ServerPlaceRecipe<R> serverPlaceRecipe = new ServerPlaceRecipe<R>(craftingMenuAccess, inventory, bl, i, j, list, list2);
        if (!bl2 && !serverPlaceRecipe.testClearGrid()) {
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        StackedItemContents stackedItemContents = new StackedItemContents();
        inventory.fillStackedContents(stackedItemContents);
        craftingMenuAccess.fillCraftSlotsStackedContents(stackedItemContents);
        return serverPlaceRecipe.tryPlaceRecipe(recipeHolder, stackedItemContents);
    }

    private ServerPlaceRecipe(CraftingMenuAccess<R> craftingMenuAccess, Inventory inventory, boolean bl, int i, int j, List<Slot> list, List<Slot> list2) {
        this.menu = craftingMenuAccess;
        this.inventory = inventory;
        this.useMaxItems = bl;
        this.gridWidth = i;
        this.gridHeight = j;
        this.inputGridSlots = list;
        this.slotsToClear = list2;
    }

    private RecipeBookMenu.PostPlaceAction tryPlaceRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
        if (stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), null)) {
            this.placeRecipe(recipeHolder, stackedItemContents);
            this.inventory.setChanged();
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        this.clearGrid();
        this.inventory.setChanged();
        return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    private void clearGrid() {
        for (Slot slot : this.slotsToClear) {
            ItemStack itemStack = slot.getItem().copy();
            this.inventory.placeItemBackInInventory(itemStack, false);
            slot.set(itemStack);
        }
        this.menu.clearCraftingContent();
    }

    private void placeRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
        boolean bl = this.menu.recipeMatches(recipeHolder);
        int i = stackedItemContents.getBiggestCraftableStack((Recipe<?>)recipeHolder.value(), null);
        if (bl) {
            for (Slot slot : this.inputGridSlots) {
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty() || Math.min(i, itemStack.getMaxStackSize()) >= itemStack.getCount() + 1) continue;
                return;
            }
        }
        int j2 = this.calculateAmountToCraft(i, bl);
        ArrayList<Holder<Item>> list = new ArrayList<Holder<Item>>();
        if (!stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), j2, list::add)) {
            return;
        }
        int k2 = ServerPlaceRecipe.clampToMaxStackSize(j2, list);
        if (k2 != j2) {
            list.clear();
            if (!stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), k2, list::add)) {
                return;
            }
        }
        this.clearGrid();
        PlaceRecipeHelper.placeRecipe(this.gridWidth, this.gridHeight, recipeHolder.value(), recipeHolder.value().placementInfo().slotsToIngredientIndex(), (integer, j, k, l) -> {
            if (integer == -1) {
                return;
            }
            Slot slot = this.inputGridSlots.get(j);
            Holder holder = (Holder)list.get((int)integer);
            int m = k2;
            while (m > 0) {
                if ((m = this.moveItemToGrid(slot, holder, m)) != -1) continue;
                return;
            }
        });
    }

    private static int clampToMaxStackSize(int i, List<Holder<Item>> list) {
        for (Holder<Item> holder : list) {
            i = Math.min(i, holder.value().getDefaultMaxStackSize());
        }
        return i;
    }

    private int calculateAmountToCraft(int i, boolean bl) {
        if (this.useMaxItems) {
            return i;
        }
        if (bl) {
            int j = Integer.MAX_VALUE;
            for (Slot slot : this.inputGridSlots) {
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty() || j <= itemStack.getCount()) continue;
                j = itemStack.getCount();
            }
            if (j != Integer.MAX_VALUE) {
                ++j;
            }
            return j;
        }
        return 1;
    }

    private int moveItemToGrid(Slot slot, Holder<Item> holder, int i) {
        ItemStack itemStack = slot.getItem();
        int j = this.inventory.findSlotMatchingCraftingIngredient(holder, itemStack);
        if (j == -1) {
            return -1;
        }
        ItemStack itemStack2 = this.inventory.getItem(j);
        ItemStack itemStack3 = i < itemStack2.getCount() ? this.inventory.removeItem(j, i) : this.inventory.removeItemNoUpdate(j);
        int k = itemStack3.getCount();
        if (itemStack.isEmpty()) {
            slot.set(itemStack3);
        } else {
            itemStack.grow(k);
        }
        return i - k;
    }

    private boolean testClearGrid() {
        ArrayList list = Lists.newArrayList();
        int i = this.getAmountOfFreeSlotsInInventory();
        for (Slot slot : this.inputGridSlots) {
            ItemStack itemStack = slot.getItem().copy();
            if (itemStack.isEmpty()) continue;
            int j = this.inventory.getSlotWithRemainingSpace(itemStack);
            if (j == -1 && list.size() <= i) {
                for (ItemStack itemStack2 : list) {
                    if (!ItemStack.isSameItem(itemStack2, itemStack) || itemStack2.getCount() == itemStack2.getMaxStackSize() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) continue;
                    itemStack2.grow(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(itemStack);
                    continue;
                }
                return false;
            }
            if (j != -1) continue;
            return false;
        }
        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int i = 0;
        for (ItemStack itemStack : this.inventory.getNonEquipmentItems()) {
            if (!itemStack.isEmpty()) continue;
            ++i;
        }
        return i;
    }

    public static interface CraftingMenuAccess<T extends Recipe<?>> {
        public void fillCraftSlotsStackedContents(StackedItemContents var1);

        public void clearCraftingContent();

        public boolean recipeMatches(RecipeHolder<T> var1);
    }
}

