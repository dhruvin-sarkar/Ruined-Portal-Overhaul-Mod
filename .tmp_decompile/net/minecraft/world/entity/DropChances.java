/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;

public record DropChances(Map<EquipmentSlot, Float> byEquipment) {
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085f;
    public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0f;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final DropChances DEFAULT = new DropChances(Util.makeEnumMap(EquipmentSlot.class, equipmentSlot -> Float.valueOf(0.085f)));
    public static final Codec<DropChances> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ExtraCodecs.NON_NEGATIVE_FLOAT).xmap(DropChances::toEnumMap, DropChances::filterDefaultValues).xmap(DropChances::new, DropChances::byEquipment);

    private static Map<EquipmentSlot, Float> filterDefaultValues(Map<EquipmentSlot, Float> map) {
        HashMap<EquipmentSlot, Float> map2 = new HashMap<EquipmentSlot, Float>(map);
        map2.values().removeIf(float_ -> float_.floatValue() == 0.085f);
        return map2;
    }

    private static Map<EquipmentSlot, Float> toEnumMap(Map<EquipmentSlot, Float> map) {
        return Util.makeEnumMap(EquipmentSlot.class, equipmentSlot -> map.getOrDefault(equipmentSlot, Float.valueOf(0.085f)));
    }

    public DropChances withGuaranteedDrop(EquipmentSlot equipmentSlot) {
        return this.withEquipmentChance(equipmentSlot, 2.0f);
    }

    public DropChances withEquipmentChance(EquipmentSlot equipmentSlot, float f) {
        if (f < 0.0f) {
            throw new IllegalArgumentException("Tried to set invalid equipment chance " + f + " for " + String.valueOf(equipmentSlot));
        }
        if (this.byEquipment(equipmentSlot) == f) {
            return this;
        }
        return new DropChances(Util.makeEnumMap(EquipmentSlot.class, equipmentSlot2 -> Float.valueOf(equipmentSlot2 == equipmentSlot ? f : this.byEquipment((EquipmentSlot)equipmentSlot2))));
    }

    public float byEquipment(EquipmentSlot equipmentSlot) {
        return this.byEquipment.getOrDefault(equipmentSlot, Float.valueOf(0.085f)).floatValue();
    }

    public boolean isPreserved(EquipmentSlot equipmentSlot) {
        return this.byEquipment(equipmentSlot) > 1.0f;
    }
}

