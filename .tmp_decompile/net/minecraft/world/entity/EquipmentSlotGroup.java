/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public enum EquipmentSlotGroup implements StringRepresentable,
Iterable<EquipmentSlot>
{
    ANY(0, "any", equipmentSlot -> true),
    MAINHAND(1, "mainhand", EquipmentSlot.MAINHAND),
    OFFHAND(2, "offhand", EquipmentSlot.OFFHAND),
    HAND(3, "hand", equipmentSlot -> equipmentSlot.getType() == EquipmentSlot.Type.HAND),
    FEET(4, "feet", EquipmentSlot.FEET),
    LEGS(5, "legs", EquipmentSlot.LEGS),
    CHEST(6, "chest", EquipmentSlot.CHEST),
    HEAD(7, "head", EquipmentSlot.HEAD),
    ARMOR(8, "armor", EquipmentSlot::isArmor),
    BODY(9, "body", EquipmentSlot.BODY),
    SADDLE(10, "saddle", EquipmentSlot.SADDLE);

    public static final IntFunction<EquipmentSlotGroup> BY_ID;
    public static final Codec<EquipmentSlotGroup> CODEC;
    public static final StreamCodec<ByteBuf, EquipmentSlotGroup> STREAM_CODEC;
    private final int id;
    private final String key;
    private final Predicate<EquipmentSlot> predicate;
    private final List<EquipmentSlot> slots;

    private EquipmentSlotGroup(int j, String string2, Predicate<EquipmentSlot> predicate) {
        this.id = j;
        this.key = string2;
        this.predicate = predicate;
        this.slots = EquipmentSlot.VALUES.stream().filter(predicate).toList();
    }

    private EquipmentSlotGroup(int j, String string2, EquipmentSlot equipmentSlot) {
        this(j, string2, (EquipmentSlot equipmentSlot2) -> equipmentSlot2 == equipmentSlot);
    }

    public static EquipmentSlotGroup bySlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> MAINHAND;
            case EquipmentSlot.OFFHAND -> OFFHAND;
            case EquipmentSlot.FEET -> FEET;
            case EquipmentSlot.LEGS -> LEGS;
            case EquipmentSlot.CHEST -> CHEST;
            case EquipmentSlot.HEAD -> HEAD;
            case EquipmentSlot.BODY -> BODY;
            case EquipmentSlot.SADDLE -> SADDLE;
        };
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    public boolean test(EquipmentSlot equipmentSlot) {
        return this.predicate.test(equipmentSlot);
    }

    public List<EquipmentSlot> slots() {
        return this.slots;
    }

    @Override
    public Iterator<EquipmentSlot> iterator() {
        return this.slots.iterator();
    }

    static {
        BY_ID = ByIdMap.continuous(equipmentSlotGroup -> equipmentSlotGroup.id, EquipmentSlotGroup.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        CODEC = StringRepresentable.fromEnum(EquipmentSlotGroup::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, equipmentSlotGroup -> equipmentSlotGroup.id);
    }
}

