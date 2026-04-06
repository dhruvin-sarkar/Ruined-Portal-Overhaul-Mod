/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public interface EquipmentUser {
    public void setItemSlot(EquipmentSlot var1, ItemStack var2);

    public ItemStack getItemBySlot(EquipmentSlot var1);

    public void setDropChance(EquipmentSlot var1, float var2);

    default public void equip(EquipmentTable equipmentTable, LootParams lootParams) {
        this.equip(equipmentTable.lootTable(), lootParams, equipmentTable.slotDropChances());
    }

    default public void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, Map<EquipmentSlot, Float> map) {
        this.equip(resourceKey, lootParams, 0L, map);
    }

    default public void equip(ResourceKey<LootTable> resourceKey, LootParams lootParams, long l, Map<EquipmentSlot, Float> map) {
        LootTable lootTable = lootParams.getLevel().getServer().reloadableRegistries().getLootTable(resourceKey);
        if (lootTable == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> list = lootTable.getRandomItems(lootParams, l);
        ArrayList<EquipmentSlot> list2 = new ArrayList<EquipmentSlot>();
        for (ItemStack itemStack : list) {
            EquipmentSlot equipmentSlot = this.resolveSlot(itemStack, list2);
            if (equipmentSlot == null) continue;
            ItemStack itemStack2 = equipmentSlot.limit(itemStack);
            this.setItemSlot(equipmentSlot, itemStack2);
            Float float_ = map.get(equipmentSlot);
            if (float_ != null) {
                this.setDropChance(equipmentSlot, float_.floatValue());
            }
            list2.add(equipmentSlot);
        }
    }

    default public @Nullable EquipmentSlot resolveSlot(ItemStack itemStack, List<EquipmentSlot> list) {
        if (itemStack.isEmpty()) {
            return null;
        }
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            EquipmentSlot equipmentSlot = equippable.slot();
            if (!list.contains(equipmentSlot)) {
                return equipmentSlot;
            }
        } else if (!list.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}

