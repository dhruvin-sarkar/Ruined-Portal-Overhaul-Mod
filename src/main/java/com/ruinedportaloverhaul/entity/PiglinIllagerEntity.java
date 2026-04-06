package com.ruinedportaloverhaul.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class PiglinIllagerEntity extends Pillager {
    public PiglinIllagerEntity(EntityType<? extends PiglinIllagerEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean causedByPlayer) {
        super.dropCustomDeathLoot(serverLevel, damageSource, causedByPlayer);
        this.spawnAtLocation(serverLevel, new ItemStack(Items.GOLD_NUGGET, 1 + this.random.nextInt(3)));
    }
}
