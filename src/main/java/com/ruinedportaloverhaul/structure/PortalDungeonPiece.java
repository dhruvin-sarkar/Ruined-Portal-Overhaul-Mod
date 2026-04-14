package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.world.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

public class PortalDungeonPiece extends StructurePiece {
    private static final int SURFACE_RADIUS = PortalStructureHelper.OUTER_RADIUS;
    private static final int DEPTH = PortalStructureHelper.PIT_DEPTH;

    private static final ResourceKey<LootTable> SURFACE_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ModStructures.id("chests/portal_surface")
    );
    private static final ResourceKey<LootTable> DEPTH_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ModStructures.id("chests/portal_deep")
    );

    private final int centerX;
    private final int surfaceY;
    private final int centerZ;

    public PortalDungeonPiece(BlockPos center) {
        super(
            ModStructures.PORTAL_DUNGEON_PIECE,
            0,
            new BoundingBox(
                center.getX() - SURFACE_RADIUS,
                Math.max(3, center.getY() - DEPTH - 8),
                center.getZ() - SURFACE_RADIUS,
                center.getX() + SURFACE_RADIUS,
                center.getY() + 14,
                center.getZ() + SURFACE_RADIUS
            )
        );
        this.centerX = center.getX();
        this.surfaceY = center.getY();
        this.centerZ = center.getZ();
    }

    public PortalDungeonPiece(CompoundTag tag) {
        super(ModStructures.PORTAL_DUNGEON_PIECE, tag);
        this.centerX = tag.getIntOr("CenterX", 0);
        this.surfaceY = tag.getIntOr("SurfaceY", 0);
        this.centerZ = tag.getIntOr("CenterZ", 0);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CenterX", this.centerX);
        tag.putInt("SurfaceY", this.surfaceY);
        tag.putInt("CenterZ", this.centerZ);
    }

    public BlockPos portalOrigin() {
        return new BlockPos(this.centerX, this.surfaceY, this.centerZ);
    }

    @Override
    public void postProcess(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator chunkGenerator,
        RandomSource randomSource,
        BoundingBox chunkBox,
        ChunkPos chunkPos,
        BlockPos pivot
    ) {
        RandomSource random = RandomSource.create(this.seed());
        BlockPos origin = new BlockPos(this.centerX, this.surfaceY, this.centerZ);
        PortalStructureHelper.PortalFrameSpec frame = PortalStructureHelper.pickFrame(random);

        PortalStructureHelper.buildInnerZone(level, this.boundingBox, chunkBox, origin, frame, random);
        PortalStructureHelper.buildMiddleZone(level, this.boundingBox, chunkBox, origin, random);
        PortalStructureHelper.buildOuterScatter(level, this.boundingBox, chunkBox, origin, this.seed());
        PortalStructureHelper.UndergroundLayout underground = PortalStructureHelper.buildUnderground(level, this.boundingBox, chunkBox, origin, random);

        PortalStructureHelper.placePortalSpawners(level, this.boundingBox, chunkBox, origin, underground);
        this.placeSurfaceChests(level, chunkBox, random, origin);
        this.placeDeepChests(level, chunkBox, random, underground.deepChests());
    }

    private void placeSurfaceChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, BlockPos origin) {
        List<BlockPos> chests = new ArrayList<>();
        chests.add(origin.offset(9, 0, -6));
        chests.add(origin.offset(-10, 0, 7));

        for (BlockPos target : chests) {
            if (!PortalStructureHelper.isColumnInside(chunkBox, target)) {
                continue;
            }
            BlockPos top = PortalStructureHelper.terrainTop(level, target);
            BlockPos chestPos = top.above();
            this.setBlockIfInside(level, chunkBox, top, Blocks.NETHERRACK.defaultBlockState());
            this.setBlockIfInside(level, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
            this.createChest(level, chunkBox, random, chestPos, SURFACE_LOOT, null);
        }
    }

    private void placeDeepChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, List<BlockPos> chests) {
        for (BlockPos chestPos : chests) {
            this.setBlockIfInside(level, chunkBox, chestPos.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            this.setBlockIfInside(level, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
            this.createChest(level, chunkBox, random, chestPos, DEPTH_LOOT, null);
        }
    }

    private void setBlockIfInside(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (this.boundingBox.isInside(pos) && chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private long seed() {
        long seed = 341873128712L;
        seed = seed * 31L + this.centerX;
        seed = seed * 31L + this.surfaceY;
        seed = seed * 31L + this.centerZ;
        return seed;
    }
}
