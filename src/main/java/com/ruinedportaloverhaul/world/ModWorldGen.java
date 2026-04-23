package com.ruinedportaloverhaul.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Predicate;

public final class ModWorldGen {
    public static final ResourceKey<PlacedFeature> UNDERGROUND_NETHERRACK_BLOB = placedFeature("underground_netherrack_blob");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_SOUL_SAND_POCKET = placedFeature("underground_soul_sand_pocket");
    public static final ResourceKey<PlacedFeature> UNDERGROUND_BLACKSTONE_VEIN = placedFeature("underground_blackstone_vein");
    private static final TagKey<Biome> TERRALITH_SKYLANDS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("terralith", "skylands"));
    private static final TagKey<Biome> TERRALITH_ALL_SKYLANDS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("terralith", "all_skylands"));
    private static final TagKey<Biome> TERRALITH_CAVES = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("terralith", "caves"));
    private static final Predicate<BiomeSelectionContext> AMBIENT_CORRUPTION_SELECTOR =
        BiomeSelectors.foundInOverworld().and(context -> !isCompatExcludedBiome(context));

    private ModWorldGen() {
    }

    public static void initialize() {
        // Fix: biome modifications are now limited to terrain corruption features; global mob-spawn injections were bleeding portal enemies into unrelated overworld biomes, so ambient combat pressure stays structure-local inside GoldRaidManager.
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
    }

    // Fix: Terralith exposes skyland and cave biome groups through tags that changed across releases, so all known tag names plus id fallbacks are filtered before ambient corruption hooks are applied.
    private static boolean isCompatExcludedBiome(BiomeSelectionContext context) {
        return context.hasTag(TERRALITH_SKYLANDS)
            || context.hasTag(TERRALITH_ALL_SKYLANDS)
            || context.hasTag(TERRALITH_CAVES)
            || isExcludedBiomeId(context.getBiomeKey().identifier());
    }

    public static boolean isCompatExcludedBiome(Holder<Biome> biome) {
        return biome.is(TERRALITH_SKYLANDS)
            || biome.is(TERRALITH_ALL_SKYLANDS)
            || biome.is(TERRALITH_CAVES)
            || biome.unwrapKey().map(ResourceKey::identifier).map(ModWorldGen::isExcludedBiomeId).orElse(false);
    }

    private static boolean isExcludedBiomeId(Identifier biomeId) {
        return "terralith".equals(biomeId.getNamespace())
            && (biomeId.getPath().startsWith("skylands")
                || biomeId.getPath().startsWith("cave/")
                || biomeId.getPath().endsWith("_caves"));
    }

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ModStructures.id(path));
    }
}
