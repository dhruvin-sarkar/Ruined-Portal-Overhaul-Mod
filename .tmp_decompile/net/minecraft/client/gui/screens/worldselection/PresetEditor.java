/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

@Environment(value=EnvType.CLIENT)
public interface PresetEditor {
    public static final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(Optional.of(WorldPresets.FLAT), (createWorldScreen, worldCreationContext) -> {
        ChunkGenerator chunkGenerator = worldCreationContext.selectedDimensions().overworld();
        RegistryAccess.Frozen registryAccess = worldCreationContext.worldgenLoadContext();
        HolderLookup.RegistryLookup holderGetter = registryAccess.lookupOrThrow(Registries.BIOME);
        HolderLookup.RegistryLookup holderGetter2 = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderLookup.RegistryLookup holderGetter3 = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);
        return new CreateFlatWorldScreen(createWorldScreen, flatLevelGeneratorSettings -> createWorldScreen.getUiState().updateDimensions(PresetEditor.flatWorldConfigurator(flatLevelGeneratorSettings)), chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault(holderGetter, holderGetter2, holderGetter3));
    }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (createWorldScreen, worldCreationContext) -> new CreateBuffetWorldScreen(createWorldScreen, worldCreationContext, holder -> createWorldScreen.getUiState().updateDimensions(PresetEditor.fixedBiomeConfigurator(holder))));

    public Screen createEditScreen(CreateWorldScreen var1, WorldCreationContext var2);

    public static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (frozen, worldDimensions) -> {
            FlatLevelSource chunkGenerator = new FlatLevelSource(flatLevelGeneratorSettings);
            return worldDimensions.replaceOverworldGenerator((HolderLookup.Provider)frozen, chunkGenerator);
        };
    }

    private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> holder) {
        return (frozen, worldDimensions) -> {
            HolderLookup.RegistryLookup registry = frozen.lookupOrThrow(Registries.NOISE_SETTINGS);
            Holder.Reference holder2 = registry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            FixedBiomeSource biomeSource = new FixedBiomeSource(holder);
            NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator((BiomeSource)biomeSource, holder2);
            return worldDimensions.replaceOverworldGenerator((HolderLookup.Provider)frozen, chunkGenerator);
        };
    }
}

