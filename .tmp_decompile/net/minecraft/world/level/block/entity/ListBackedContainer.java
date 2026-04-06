/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public interface ListBackedContainer
extends Container {
    public NonNullList<ItemStack> getItems();

    default public int count() {
        return (int)this.getItems().stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    default public int getContainerSize() {
        return this.getItems().size();
    }

    @Override
    default public void clearContent() {
        this.getItems().clear();
    }

    @Override
    default public boolean isEmpty() {
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    default public ItemStack getItem(int i) {
        return this.getItems().get(i);
    }

    @Override
    default public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = ContainerHelper.removeItem(this.getItems(), i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }
        return itemStack;
    }

    @Override
    default public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.removeItem(this.getItems(), i, this.getMaxStackSize());
    }

    @Override
    default public boolean canPlaceItem(int i, ItemStack itemStack) {
        return this.acceptsItemType(itemStack) && (this.getItem(i).isEmpty() || this.getItem(i).getCount() < this.getMaxStackSize(itemStack));
    }

    default public boolean acceptsItemType(ItemStack itemStack) {
        return true;
    }

    @Override
    default public void setItem(int i, ItemStack itemStack) {
        this.setItemNoUpdate(i, itemStack);
        this.setChanged();
    }

    default public void setItemNoUpdate(int i, ItemStack itemStack) {
        this.getItems().set(i, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
    }
}

