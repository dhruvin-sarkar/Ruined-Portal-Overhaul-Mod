package com.ruinedportaloverhaul.structure;

import com.mojang.serialization.MapCodec;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.world.ModStructures;
import com.ruinedportaloverhaul.world.ModWorldGen;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class PortalDungeonStructure extends Structure {
    public static final MapCodec<PortalDungeonStructure> CODEC = simpleCodec(PortalDungeonStructure::new);
    private static final int MIN_CONFIGURABLE_SPACING = 32;

    public PortalDungeonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Fix: generation previously returned a piece without checking the JSON biome predicate, so compat exclusions and vanilla ruined-portal biome tags could be bypassed. The center biome is now sampled first, validated, and then used for deterministic variant placement.
        if (!passesConfiguredRarity(context)) {
            return Optional.empty();
        }

        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator()
            .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()) - 1;
        int oceanFloorY = context.chunkGenerator()
            .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState()) - 1;
        int baseY = surfaceY - oceanFloorY > 1 ? oceanFloorY : surfaceY;
        Holder<Biome> centerBiome = context.biomeSource().getNoiseBiome(
            QuartPos.fromBlock(centerX),
            QuartPos.fromBlock(baseY),
            QuartPos.fromBlock(centerZ),
            context.randomState().sampler()
        );
        if (!context.validBiome().test(centerBiome) || ModWorldGen.isCompatExcludedBiome(centerBiome)) {
            return Optional.empty();
        }

        PortalDungeonVariant variant = PortalDungeonVariant.selectForChunk(context.chunkPos());
        int centerY = baseY + variant.centerYOffset();

        BlockPos center = new BlockPos(centerX, centerY, centerZ);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new PortalDungeonPiece(center, variant))));
    }

    private static boolean passesConfiguredRarity(GenerationContext context) {
        // Fix: the config screen exposed structure rarity but the structure set was static JSON, so the value never affected generation. The JSON now supplies the minimum grid and this live deterministic thinning step raises average spacing for new chunks when admins choose rarer portals.
        int configuredSpacing = ModConfigManager.structureRarity();
        if (configuredSpacing <= MIN_CONFIGURABLE_SPACING) {
            return true;
        }

        double acceptanceChance = (MIN_CONFIGURABLE_SPACING * MIN_CONFIGURABLE_SPACING) / (double) (configuredSpacing * configuredSpacing);
        long mixed = mixRaritySeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        double roll = (mixed >>> 11) * 0x1.0p-53;
        return roll < acceptanceChance;
    }

    private static long mixRaritySeed(long seed, int chunkX, int chunkZ) {
        long value = seed ^ (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.PORTAL_DUNGEON_TYPE;
    }
}
