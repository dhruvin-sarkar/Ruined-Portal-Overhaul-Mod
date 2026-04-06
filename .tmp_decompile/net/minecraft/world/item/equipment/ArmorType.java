/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public enum ArmorType implements StringRepresentable
{
    HELMET(EquipmentSlot.HEAD, 11, "helmet"),
    CHESTPLATE(EquipmentSlot.CHEST, 16, "chestplate"),
    LEGGINGS(EquipmentSlot.LEGS, 15, "leggings"),
    BOOTS(EquipmentSlot.FEET, 13, "boots"),
    BODY(EquipmentSlot.BODY, 16, "body");

    public static final Codec<ArmorType> CODEC;
    private final EquipmentSlot slot;
    private final String name;
    private final int unitDurability;

    private ArmorType(EquipmentSlot equipmentSlot, int j, String string2) {
        this.slot = equipmentSlot;
        this.name = string2;
        this.unitDurability = j;
    }

    public int getDurability(int i) {
        return this.unitDurability * i;
    }

    public EquipmentSlot getSlot() {
        return this.slot;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromValues(ArmorType::values);
    }
}

