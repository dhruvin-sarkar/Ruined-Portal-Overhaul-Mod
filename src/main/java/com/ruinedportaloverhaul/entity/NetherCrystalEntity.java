package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NetherCrystalEntity extends EndCrystal {
    public NetherCrystalEntity(EntityType<? extends NetherCrystalEntity> entityType, Level level) {
        super(entityType, level);
    }

    public NetherCrystalEntity(Level level, double x, double y, double z) {
        this(ModEntities.NETHER_CRYSTAL, level);
        this.setPos(x, y, z);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.NETHER_CRYSTAL);
    }
}
