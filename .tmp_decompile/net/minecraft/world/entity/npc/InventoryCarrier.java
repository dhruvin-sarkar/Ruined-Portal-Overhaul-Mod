/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface InventoryCarrier {
    public static final String TAG_INVENTORY = "Inventory";

    public SimpleContainer getInventory();

    public static void pickUpItem(ServerLevel serverLevel, Mob mob, InventoryCarrier inventoryCarrier, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (mob.wantsToPickUp(serverLevel, itemStack)) {
            SimpleContainer simpleContainer = inventoryCarrier.getInventory();
            boolean bl = simpleContainer.canAddItem(itemStack);
            if (!bl) {
                return;
            }
            mob.onItemPickup(itemEntity);
            int i = itemStack.getCount();
            ItemStack itemStack2 = simpleContainer.addItem(itemStack);
            mob.take(itemEntity, i - itemStack2.getCount());
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    default public void readInventoryFromTag(ValueInput valueInput) {
        valueInput.list(TAG_INVENTORY, ItemStack.CODEC).ifPresent(typedInputList -> this.getInventory().fromItemList((ValueInput.TypedInputList<ItemStack>)typedInputList));
    }

    default public void writeInventoryToTag(ValueOutput valueOutput) {
        this.getInventory().storeAsItemList(valueOutput.list(TAG_INVENTORY, ItemStack.CODEC));
    }
}

