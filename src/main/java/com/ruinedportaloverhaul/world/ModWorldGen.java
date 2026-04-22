package com.ruinedportaloverhaul.world;

import com.ruinedportaloverhaul.config.ModConfigManager;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldGen {
    public static final ResourceKey<PlacedFeature> UNDERGROUND_NETHERRACK_BLOB = placedFeature("underground_netherrack_blob");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_SOUL_SAND_POCKET = placedFeature("underground_soul_sand_pocket");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_BLACKSTONE_VEIN = placedFeature("underground_blackstone_vein");

    private ModWorldGen() {
    }

    public static void initialize() {
        // Fix: the global lore spawns were always injected, so the startup gate now honors the worldgen config instead of forcing them on every pack.
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_NETHERRACK_BLOB
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_SOUL_SAND_POCKET
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_BLACKSTONE_VEIN
        );

        if (ModConfigManager.enableAmbientNetherSpawns()) {
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, EntityType.ZOMBIFIED_PIGLIN, 1, 1, 2);
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, EntityType.BLAZE, 1, 1, 1);
        }
    }

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ModStructures.id(path));
    }
}
