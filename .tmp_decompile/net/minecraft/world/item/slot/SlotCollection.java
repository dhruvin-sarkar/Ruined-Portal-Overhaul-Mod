/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.slot;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;

public interface SlotCollection {
    public static final SlotCollection EMPTY = Stream::empty;

    public Stream<ItemStack> itemCopies();

    default public SlotCollection filter(Predicate<ItemStack> predicate) {
        return new Filtered(this, predicate);
    }

    default public SlotCollection flatMap(Function<ItemStack, ? extends SlotCollection> function) {
        return new FlatMapped(this, function);
    }

    default public SlotCollection limit(int i) {
        return new Limited(this, i);
    }

    public static SlotCollection of(SlotAccess slotAccess) {
        return () -> Stream.of(slotAccess.get().copy());
    }

    public static SlotCollection of(Collection<? extends SlotAccess> collection) {
        return switch (collection.size()) {
            case 0 -> EMPTY;
            case 1 -> SlotCollection.of(collection.iterator().next());
            default -> () -> collection.stream().map(SlotAccess::get).map(ItemStack::copy);
        };
    }

    public static SlotCollection concat(SlotCollection slotCollection, SlotCollection slotCollection2) {
        return () -> Stream.concat(slotCollection.itemCopies(), slotCollection2.itemCopies());
    }

    public static SlotCollection concat(List<? extends SlotCollection> list) {
        return switch (list.size()) {
            case 0 -> EMPTY;
            case 1 -> (SlotCollection)list.getFirst();
            case 2 -> SlotCollection.concat(list.get(0), list.get(1));
            default -> () -> list.stream().flatMap(SlotCollection::itemCopies);
        };
    }

    public record Filtered(SlotCollection slots, Predicate<ItemStack> filter) implements SlotCollection
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().filter(this.filter);
        }

        @Override
        public SlotCollection filter(Predicate<ItemStack> predicate) {
            return new Filtered(this.slots, this.filter.and(predicate));
        }
    }

    public record FlatMapped(SlotCollection slots, Function<ItemStack, ? extends SlotCollection> mapper) implements SlotCollection
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().map(this.mapper).flatMap(SlotCollection::itemCopies);
        }
    }

    public record Limited(SlotCollection slots, int limit) implements SlotCollection
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            return this.slots.itemCopies().limit(this.limit);
        }

        @Override
        public SlotCollection limit(int i) {
            return new Limited(this.slots, Math.min(this.limit, i));
        }
    }
}

