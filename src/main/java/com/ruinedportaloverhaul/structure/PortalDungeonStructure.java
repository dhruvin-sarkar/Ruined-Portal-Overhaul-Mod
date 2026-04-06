package com.ruinedportaloverhaul.structure;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import com.ruinedportaloverhaul.world.ModStructures;

public class PortalDungeonStructure extends Structure {
    public static final MapCodec<PortalDungeonStructure> CODEC = simpleCodec(PortalDungeonStructure::new);

    public PortalDungeonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int centerX = context.chunkPos().getMiddleBlockX();
        int centerZ = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator()
            .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()) - 1;
        int oceanFloorY = context.chunkGenerator()
            .getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState()) - 1;
        int baseY = surfaceY - oceanFloorY > 1 ? oceanFloorY : surfaceY;

        BlockPos center = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new PortalDungeonPiece(center))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.PORTAL_DUNGEON_TYPE;
    }
}
