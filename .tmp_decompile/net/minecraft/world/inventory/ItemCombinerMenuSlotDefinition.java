/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.inventory;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<SlotDefinition> slots;
    private final SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<SlotDefinition> list, SlotDefinition slotDefinition) {
        if (list.isEmpty() || slotDefinition.equals((Object)SlotDefinition.EMPTY)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.slots = list;
        this.resultSlot = slotDefinition;
    }

    public static Builder create() {
        return new Builder();
    }

    public SlotDefinition getSlot(int i) {
        return this.slots.get(i);
    }

    public SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public static final class SlotDefinition
    extends Record {
        final int slotIndex;
        private final int x;
        private final int y;
        private final Predicate<ItemStack> mayPlace;
        static final SlotDefinition EMPTY = new SlotDefinition(0, 0, 0, itemStack -> true);

        public SlotDefinition(int i, int j, int k, Predicate<ItemStack> predicate) {
            this.slotIndex = i;
            this.x = j;
            this.y = k;
            this.mayPlace = predicate;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this, object);
        }

        public int slotIndex() {
            return this.slotIndex;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Predicate<ItemStack> mayPlace() {
            return this.mayPlace;
        }
    }

    public static class Builder {
        private final List<SlotDefinition> inputSlots = new ArrayList<SlotDefinition>();
        private SlotDefinition resultSlot = SlotDefinition.EMPTY;

        public Builder withSlot(int i, int j, int k, Predicate<ItemStack> predicate) {
            this.inputSlots.add(new SlotDefinition(i, j, k, predicate));
            return this;
        }

        public Builder withResultSlot(int i, int j, int k) {
            this.resultSlot = new SlotDefinition(i, j, k, itemStack -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            int i = this.inputSlots.size();
            for (int j = 0; j < i; ++j) {
                SlotDefinition slotDefinition = this.inputSlots.get(j);
                if (slotDefinition.slotIndex == j) continue;
                throw new IllegalArgumentException("Expected input slots to have continous indexes");
            }
            if (this.resultSlot.slotIndex != i) {
                throw new IllegalArgumentException("Expected result slot index to follow last input slot");
            }
            return new ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
        }
    }
}

