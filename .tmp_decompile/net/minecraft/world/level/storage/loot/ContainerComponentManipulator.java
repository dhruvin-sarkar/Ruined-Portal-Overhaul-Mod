/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;

public interface ContainerComponentManipulator<T> {
    public DataComponentType<T> type();

    public T empty();

    public T setContents(T var1, Stream<ItemStack> var2);

    public Stream<ItemStack> getContents(T var1);

    default public void setContents(ItemStack itemStack, T object, Stream<ItemStack> stream) {
        T object2 = itemStack.getOrDefault(this.type(), object);
        T object3 = this.setContents(object2, stream);
        itemStack.set(this.type(), object3);
    }

    default public void setContents(ItemStack itemStack, Stream<ItemStack> stream) {
        this.setContents(itemStack, this.empty(), stream);
    }

    default public void modifyItems(ItemStack itemStack2, UnaryOperator<ItemStack> unaryOperator) {
        T object = itemStack2.get(this.type());
        if (object != null) {
            UnaryOperator unaryOperator2 = itemStack -> {
                if (itemStack.isEmpty()) {
                    return itemStack;
                }
                ItemStack itemStack2 = (ItemStack)unaryOperator.apply((ItemStack)itemStack);
                itemStack2.limitSize(itemStack2.getMaxStackSize());
                return itemStack2;
            };
            this.setContents(itemStack2, this.getContents(object).map(unaryOperator2));
        }
    }

    default public SlotCollection getSlots(ItemStack itemStack) {
        return () -> {
            T object = itemStack.get(this.type());
            if (object != null) {
                return this.getContents(object).filter(itemStack -> !itemStack.isEmpty());
            }
            return Stream.empty();
        };
    }
}

