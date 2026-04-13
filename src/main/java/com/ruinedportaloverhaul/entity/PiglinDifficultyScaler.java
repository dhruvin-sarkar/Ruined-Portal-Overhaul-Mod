package com.ruinedportaloverhaul.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ServerLevelAccessor;

public final class PiglinDifficultyScaler {
    private static final net.minecraft.resources.Identifier HARD_HEALTH_BONUS_ID = ModEntities.id("hard_health_bonus");

    private PiglinDifficultyScaler() {
    }

    public static SpawnGroupData applyHardHealth(Mob mob, ServerLevelAccessor level, SpawnGroupData spawnData) {
        if (level.getDifficulty() != Difficulty.HARD) {
            return spawnData;
        }

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null || maxHealth.hasModifier(HARD_HEALTH_BONUS_ID)) {
            return spawnData;
        }

        maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
            HARD_HEALTH_BONUS_ID,
            maxHealth.getBaseValue() * 0.25,
            AttributeModifier.Operation.ADD_VALUE
        ));
        mob.setHealth(mob.getMaxHealth());
        return spawnData;
    }
}
