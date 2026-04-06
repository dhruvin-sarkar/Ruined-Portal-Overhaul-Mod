/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class EquipmentDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    public static final EquipmentDispenseItemBehavior INSTANCE = new EquipmentDispenseItemBehavior();

    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        return EquipmentDispenseItemBehavior.dispenseEquipment(blockSource, itemStack) ? itemStack : super.execute(blockSource, itemStack);
    }

    public static boolean dispenseEquipment(BlockSource blockSource, ItemStack itemStack) {
        BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
        List<LivingEntity> list = blockSource.level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), livingEntity -> livingEntity.canEquipWithDispenser(itemStack));
        if (list.isEmpty()) {
            return false;
        }
        LivingEntity livingEntity2 = (LivingEntity)list.getFirst();
        EquipmentSlot equipmentSlot = livingEntity2.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = itemStack.split(1);
        livingEntity2.setItemSlot(equipmentSlot, itemStack2);
        if (livingEntity2 instanceof Mob) {
            Mob mob = (Mob)livingEntity2;
            mob.setGuaranteedDrop(equipmentSlot);
            mob.setPersistenceRequired();
        }
        return true;
    }
}

