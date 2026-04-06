/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem
extends Container {
    public ItemStack getTheItem();

    default public ItemStack splitTheItem(int i) {
        return this.getTheItem().split(i);
    }

    public void setTheItem(ItemStack var1);

    default public ItemStack removeTheItem() {
        return this.splitTheItem(this.getMaxStackSize());
    }

    @Override
    default public int getContainerSize() {
        return 1;
    }

    @Override
    default public boolean isEmpty() {
        return this.getTheItem().isEmpty();
    }

    @Override
    default public void clearContent() {
        this.removeTheItem();
    }

    @Override
    default public ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, this.getMaxStackSize());
    }

    @Override
    default public ItemStack getItem(int i) {
        return i == 0 ? this.getTheItem() : ItemStack.EMPTY;
    }

    @Override
    default public ItemStack removeItem(int i, int j) {
        if (i != 0) {
            return ItemStack.EMPTY;
        }
        return this.splitTheItem(j);
    }

    @Override
    default public void setItem(int i, ItemStack itemStack) {
        if (i == 0) {
            this.setTheItem(itemStack);
        }
    }

    public static interface BlockContainerSingleItem
    extends ContainerSingleItem {
        public BlockEntity getContainerBlockEntity();

        @Override
        default public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(this.getContainerBlockEntity(), player);
        }
    }
}

