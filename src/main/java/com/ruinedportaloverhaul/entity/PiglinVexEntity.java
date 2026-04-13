package com.ruinedportaloverhaul.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class PiglinVexEntity extends Vex {
    public PiglinVexEntity(EntityType<? extends PiglinVexEntity> entityType, Level level) {
        super(entityType, level);
        this.setLimitedLife(60 * 20);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes()
            .add(Attributes.MAX_HEALTH, 18.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
    }
}
