/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityEquipment {
    public static final Codec<EntityEquipment> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).xmap(map -> {
        EnumMap<EquipmentSlot, ItemStack> enumMap = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        enumMap.putAll((Map<EquipmentSlot, ItemStack>)map);
        return new EntityEquipment(enumMap);
    }, entityEquipment -> {
        EnumMap<EquipmentSlot, ItemStack> map = new EnumMap<EquipmentSlot, ItemStack>(entityEquipment.items);
        map.values().removeIf(ItemStack::isEmpty);
        return map;
    });
    private final EnumMap<EquipmentSlot, ItemStack> items;

    private EntityEquipment(EnumMap<EquipmentSlot, ItemStack> enumMap) {
        this.items = enumMap;
    }

    public EntityEquipment() {
        this(new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class));
    }

    public ItemStack set(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        return (ItemStack)Objects.requireNonNullElse((Object)this.items.put(equipmentSlot, itemStack), (Object)ItemStack.EMPTY);
    }

    public ItemStack get(EquipmentSlot equipmentSlot) {
        return this.items.getOrDefault(equipmentSlot, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.items.values()) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void tick(Entity entity) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : this.items.entrySet()) {
            ItemStack itemStack = entry.getValue();
            if (itemStack.isEmpty()) continue;
            itemStack.inventoryTick(entity.level(), entity, entry.getKey());
        }
    }

    public void setAll(EntityEquipment entityEquipment) {
        this.items.clear();
        this.items.putAll(entityEquipment.items);
    }

    public void dropAll(LivingEntity livingEntity) {
        for (ItemStack itemStack : this.items.values()) {
            livingEntity.drop(itemStack, true, false);
        }
        this.clear();
    }

    public void clear() {
        this.items.replaceAll((equipmentSlot, itemStack) -> ItemStack.EMPTY);
    }
}

