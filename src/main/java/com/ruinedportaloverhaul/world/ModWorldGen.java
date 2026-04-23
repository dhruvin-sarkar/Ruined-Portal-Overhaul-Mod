package com.ruinedportaloverhaul.world;

import com.ruinedportaloverhaul.config.ModConfigManager;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Predicate;

public final class ModWorldGen {
    public static final ResourceKey<PlacedFeature> UNDERGROUND_NETHERRACK_BLOB = placedFeature("underground_netherrack_blob");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_SOUL_SAND_POCKET = placedFeature("underground_soul_sand_pocket");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_BLACKSTONE_VEIN = placedFeature("underground_blackstone_vein");
    private static final TagKey<Biome> TERRALITH_SKYLANDS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("terralith", "skylands"));
    private static final Predicate<BiomeSelectionContext> AMBIENT_CORRUPTION_SELECTOR =
        BiomeSelectors.foundInOverworld().and(context -> !isCompatExcludedBiome(context));

    private ModWorldGen() {
    }

    public static void initialize() {
        // Fix: the ambient corruption pass previously targeted every overworld biome, so Terralith sky islands and cave biomes could inherit surface corruption. The shared selector now keeps these additions on compatible surface biomes only.
        BiomeModifications.addFeature(
            AMBIENT_CORRUPTION_SELECTOR,
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_NETHERRACK_BLOB
        );
        BiomeModifications.addFeature(
            AMBIENT_CORRUPTION_SELECTOR,
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_SOUL_SAND_POCKET
        );
        BiomeModifications.addFeature(
            AMBIENT_CORRUPTION_SELECTOR,
            GenerationStep.Decoration.UNDERGROUND_ORES,
            UNDERGROUND_BLACKSTONE_VEIN
        );

        if (ModConfigManager.enableAmbientNetherSpawns()) {
            BiomeModifications.addSpawn(AMBIENT_CORRUPTION_SELECTOR, MobCategory.MONSTER, EntityType.ZOMBIFIED_PIGLIN, 1, 1, 2);
            BiomeModifications.addSpawn(AMBIENT_CORRUPTION_SELECTOR, MobCategory.MONSTER, EntityType.BLAZE, 1, 1, 1);
        }
    }

    // Fix: Terralith exposes overworld cave biomes under terralith:cave/... and floating skylands via its biome tag, so both are filtered out here before ambient corruption hooks are applied.
    private static boolean isCompatExcludedBiome(BiomeSelectionContext context) {
        return context.hasTag(TERRALITH_SKYLANDS) || isExcludedBiomeId(context.getBiomeKey().identifier());
    }

    public static boolean isCompatExcludedBiome(Holder<Biome> biome) {
        return biome.is(TERRALITH_SKYLANDS)
            || biome.unwrapKey().map(ResourceKey::identifier).map(ModWorldGen::isExcludedBiomeId).orElse(false);
    }

    private static boolean isExcludedBiomeId(Identifier biomeId) {
        return "terralith".equals(biomeId.getNamespace())
            && (biomeId.getPath().startsWith("skylands") || biomeId.getPath().startsWith("cave/"));
    }

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ModStructures.id(path));
    }
}
