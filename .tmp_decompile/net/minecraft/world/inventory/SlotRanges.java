/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.SlotRange;
import org.jspecify.annotations.Nullable;

public class SlotRanges {
    private static final List<SlotRange> SLOTS = Util.make(new ArrayList(), arrayList -> {
        SlotRanges.addSingleSlot(arrayList, "contents", 0);
        SlotRanges.addSlotRange(arrayList, "container.", 0, 54);
        SlotRanges.addSlotRange(arrayList, "hotbar.", 0, 9);
        SlotRanges.addSlotRange(arrayList, "inventory.", 9, 27);
        SlotRanges.addSlotRange(arrayList, "enderchest.", 200, 27);
        SlotRanges.addSlotRange(arrayList, "villager.", 300, 8);
        SlotRanges.addSlotRange(arrayList, "horse.", 500, 15);
        int i = EquipmentSlot.MAINHAND.getIndex(98);
        int j = EquipmentSlot.OFFHAND.getIndex(98);
        SlotRanges.addSingleSlot(arrayList, "weapon", i);
        SlotRanges.addSingleSlot(arrayList, "weapon.mainhand", i);
        SlotRanges.addSingleSlot(arrayList, "weapon.offhand", j);
        SlotRanges.addSlots(arrayList, "weapon.*", i, j);
        i = EquipmentSlot.HEAD.getIndex(100);
        j = EquipmentSlot.CHEST.getIndex(100);
        int k = EquipmentSlot.LEGS.getIndex(100);
        int l = EquipmentSlot.FEET.getIndex(100);
        int m = EquipmentSlot.BODY.getIndex(105);
        SlotRanges.addSingleSlot(arrayList, "armor.head", i);
        SlotRanges.addSingleSlot(arrayList, "armor.chest", j);
        SlotRanges.addSingleSlot(arrayList, "armor.legs", k);
        SlotRanges.addSingleSlot(arrayList, "armor.feet", l);
        SlotRanges.addSingleSlot(arrayList, "armor.body", m);
        SlotRanges.addSlots(arrayList, "armor.*", i, j, k, l, m);
        SlotRanges.addSingleSlot(arrayList, "saddle", EquipmentSlot.SADDLE.getIndex(106));
        SlotRanges.addSingleSlot(arrayList, "horse.chest", 499);
        SlotRanges.addSingleSlot(arrayList, "player.cursor", 499);
        SlotRanges.addSlotRange(arrayList, "player.crafting.", 500, 4);
    });
    public static final Codec<SlotRange> CODEC = StringRepresentable.fromValues(() -> (SlotRange[])SLOTS.toArray(SlotRange[]::new));
    private static final Function<String, @Nullable SlotRange> NAME_LOOKUP = StringRepresentable.createNameLookup((StringRepresentable[])((SlotRange[])SLOTS.toArray(SlotRange[]::new)));

    private static SlotRange create(String string, int i) {
        return SlotRange.of(string, IntLists.singleton((int)i));
    }

    private static SlotRange create(String string, IntList intList) {
        return SlotRange.of(string, IntLists.unmodifiable((IntList)intList));
    }

    private static SlotRange create(String string, int ... is) {
        return SlotRange.of(string, IntList.of((int[])is));
    }

    private static void addSingleSlot(List<SlotRange> list, String string, int i) {
        list.add(SlotRanges.create(string, i));
    }

    private static void addSlotRange(List<SlotRange> list, String string, int i, int j) {
        IntArrayList intList = new IntArrayList(j);
        for (int k = 0; k < j; ++k) {
            int l = i + k;
            list.add(SlotRanges.create(string + k, l));
            intList.add(l);
        }
        list.add(SlotRanges.create(string + "*", (IntList)intList));
    }

    private static void addSlots(List<SlotRange> list, String string, int ... is) {
        list.add(SlotRanges.create(string, is));
    }

    public static @Nullable SlotRange nameToIds(String string) {
        return NAME_LOOKUP.apply(string);
    }

    public static Stream<String> allNames() {
        return SLOTS.stream().map(StringRepresentable::getSerializedName);
    }

    public static Stream<String> singleSlotNames() {
        return SLOTS.stream().filter(slotRange -> slotRange.size() == 1).map(StringRepresentable::getSerializedName);
    }
}

