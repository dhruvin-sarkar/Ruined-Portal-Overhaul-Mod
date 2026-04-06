/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum EquipmentSlot implements StringRepresentable
{
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
    LEGS(Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
    CHEST(Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
    HEAD(Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
    BODY(Type.ANIMAL_ARMOR, 0, 1, 6, "body"),
    SADDLE(Type.SADDLE, 0, 1, 7, "saddle");

    public static final int NO_COUNT_LIMIT = 0;
    public static final List<EquipmentSlot> VALUES;
    public static final IntFunction<EquipmentSlot> BY_ID;
    public static final StringRepresentable.EnumCodec<EquipmentSlot> CODEC;
    public static final StreamCodec<ByteBuf, EquipmentSlot> STREAM_CODEC;
    private final Type type;
    private final int index;
    private final int countLimit;
    private final int id;
    private final String name;

    private EquipmentSlot(Type type, int j, int k, int l, String string2) {
        this.type = type;
        this.index = j;
        this.countLimit = k;
        this.id = l;
        this.name = string2;
    }

    private EquipmentSlot(Type type, int j, int k, String string2) {
        this(type, j, 0, k, string2);
    }

    public Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int i) {
        return i + this.index;
    }

    public ItemStack limit(ItemStack itemStack) {
        return this.countLimit > 0 ? itemStack.split(this.countLimit) : itemStack;
    }

    public int getId() {
        return this.id;
    }

    public int getFilterBit(int i) {
        return this.id + i;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == Type.HUMANOID_ARMOR || this.type == Type.ANIMAL_ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean canIncreaseExperience() {
        return this.type != Type.SADDLE;
    }

    public static EquipmentSlot byName(String string) {
        EquipmentSlot equipmentSlot = CODEC.byName(string);
        if (equipmentSlot != null) {
            return equipmentSlot;
        }
        throw new IllegalArgumentException("Invalid slot '" + string + "'");
    }

    static {
        VALUES = List.of((Object[])EquipmentSlot.values());
        BY_ID = ByIdMap.continuous(equipmentSlot -> equipmentSlot.id, EquipmentSlot.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, equipmentSlot -> equipmentSlot.id);
    }

    public static enum Type {
        HAND,
        HUMANOID_ARMOR,
        ANIMAL_ARMOR,
        SADDLE;

    }
}

