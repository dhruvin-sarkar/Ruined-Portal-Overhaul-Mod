package com.ruinedportaloverhaul.structure;

import com.mojang.serialization.MapCodec;
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

    public PortalDungeonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Fix: generation previously returned a piece without checking the JSON biome predicate, so compat exclusions and vanilla ruined-portal biome tags could be bypassed. The center biome is now sampled first, validated, and then used for deterministic variant placement.
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

    @Override
    public StructureType<?> type() {
        return ModStructures.PORTAL_DUNGEON_TYPE;
    }
}
