package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class PiglinVindicatorEntity extends Vindicator {
    public PiglinVindicatorEntity(EntityType<? extends PiglinVindicatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vindicator.createAttributes()
            .add(Attributes.MAX_HEALTH, 35.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        ItemStack axe = new ItemStack(Items.GOLDEN_AXE);
        if (randomSource.nextFloat() < 0.20f) {
            Holder.Reference<Enchantment> enchantment = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS);
            axe.enchant(enchantment, 2);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, axe);
    }
}
