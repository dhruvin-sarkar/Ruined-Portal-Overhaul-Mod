package com.ruinedportaloverhaul.loot;

import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.entity.PiglinEvokerEntity;
import com.ruinedportaloverhaul.entity.PiglinIllusionerEntity;
import com.ruinedportaloverhaul.entity.PiglinRavagerEntity;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class ModLootEvents {
    private static final double EVOKER_NETHER_STAR_CHANCE = 0.05;
    private static final double RAVAGER_NETHER_STAR_CHANCE = 0.03;
    private static final double ILLUSIONER_NETHER_STAR_CHANCE = 0.01;

    private ModLootEvents() {
    }

    public static void initialize() {
        // Fix: Nether Star odds were hardcoded in loot JSON, so the runtime config slider could not affect raid drops.
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> addConfiguredNetherStarDrop(context, drops));
    }

    private static void addConfiguredNetherStarDrop(LootContext context, List<ItemStack> drops) {
        if (context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER) == null) {
            return;
        }

        Object rawEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(rawEntity instanceof Entity entity)) {
            return;
        }

        double baseChance = baseNetherStarChance(entity);
        if (baseChance <= 0.0) {
            return;
        }

        double chance = Math.clamp(baseChance * ModConfigManager.netherStarDropRate(), 0.0, 1.0);
        if (chance > 0.0 && context.getRandom().nextDouble() < chance) {
            drops.add(new ItemStack(Items.NETHER_STAR));
        }
    }

    private static double baseNetherStarChance(Entity entity) {
        if (entity instanceof PiglinEvokerEntity) {
            return EVOKER_NETHER_STAR_CHANCE;
        }
        if (entity instanceof PiglinRavagerEntity) {
            return RAVAGER_NETHER_STAR_CHANCE;
        }
        if (entity instanceof PiglinIllusionerEntity) {
            return ILLUSIONER_NETHER_STAR_CHANCE;
        }
        return 0.0;
    }
}
