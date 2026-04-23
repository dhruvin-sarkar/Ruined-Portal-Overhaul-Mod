package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.config.ModConfigManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ServerLevelAccessor;

public final class PiglinDifficultyScaler {
    private static final net.minecraft.resources.Identifier SPAWN_HEALTH_SCALE_ID = ModEntities.id("hard_health_bonus");
    private static final net.minecraft.resources.Identifier SPAWN_ATTACK_SCALE_ID = ModEntities.id("hard_attack_bonus");

    private PiglinDifficultyScaler() {
    }

    public static SpawnGroupData applySpawnScaling(Mob mob, ServerLevelAccessor level, SpawnGroupData spawnData) {
        // Fix: this scaler used to hardcode a Hard-only bonus; it now composes live config multipliers with Easy/Normal/Hard scaling while preserving the original modifier ids for existing saves.
        double healthMultiplier = switch (level.getDifficulty()) {
            case EASY, PEACEFUL -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.25;
        } * ModConfigManager.mobHealthMultiplier();

        double damageMultiplier = switch (level.getDifficulty()) {
            case EASY, PEACEFUL -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.5;
        } * ModConfigManager.mobDamageMultiplier();

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && !maxHealth.hasModifier(SPAWN_HEALTH_SCALE_ID)) {
            maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
                SPAWN_HEALTH_SCALE_ID,
                maxHealth.getBaseValue() * (healthMultiplier - 1.0),
                AttributeModifier.Operation.ADD_VALUE
            ));
            mob.setHealth(mob.getMaxHealth());
        }

        AttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && !attackDamage.hasModifier(SPAWN_ATTACK_SCALE_ID)) {
            attackDamage.addOrReplacePermanentModifier(new AttributeModifier(
                SPAWN_ATTACK_SCALE_ID,
                attackDamage.getBaseValue() * (damageMultiplier - 1.0),
                AttributeModifier.Operation.ADD_VALUE
            ));
        }
        return spawnData;
    }
}
