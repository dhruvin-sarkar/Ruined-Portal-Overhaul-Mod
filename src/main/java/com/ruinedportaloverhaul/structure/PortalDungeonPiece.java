package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.block.NetherConduitChestPlacement;
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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
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
    private final PortalDungeonVariant variant;

    public PortalDungeonPiece(BlockPos center) {
        this(center, PortalDungeonVariant.CRIMSON_THRONE);
    }

    public PortalDungeonPiece(BlockPos center, PortalDungeonVariant variant) {
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
        this.variant = variant;
    }

    public PortalDungeonPiece(CompoundTag tag) {
        super(ModStructures.PORTAL_DUNGEON_PIECE, tag);
        this.centerX = tag.getIntOr("CenterX", 0);
        this.surfaceY = tag.getIntOr("SurfaceY", 0);
        this.centerZ = tag.getIntOr("CenterZ", 0);
        this.variant = PortalDungeonVariant.byId(tag.getIntOr("Variant", 0));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        // Fix: structure pieces previously forgot which portal variant they represented, so reloads could not preserve variant-specific geometry choices. The piece now writes the locked variant alongside its origin.
        tag.putInt("CenterX", this.centerX);
        tag.putInt("SurfaceY", this.surfaceY);
        tag.putInt("CenterZ", this.centerZ);
        tag.putInt("Variant", this.variant.id());
    }

    public BlockPos portalOrigin() {
        return new BlockPos(this.centerX, this.surfaceY, this.centerZ);
    }

    public PortalDungeonVariant variant() {
        return this.variant;
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
        // Fix: the generation pass previously assumed one hardcoded structure shape. The piece now exposes its locked variant to the helper pipeline so later slices can add geometry without touching lookup logic again.
        RandomSource random = RandomSource.create(this.seed());
        BlockPos origin = new BlockPos(this.centerX, this.surfaceY, this.centerZ);
        PortalStructureHelper.PortalFrameSpec frame = PortalStructureHelper.pickFrame(random);

        PortalStructureHelper.buildInnerZone(level, this.boundingBox, chunkBox, origin, this.variant, frame, random);
        PortalStructureHelper.buildMiddleZone(level, this.boundingBox, chunkBox, origin, this.variant, random);
        PortalStructureHelper.buildOuterScatter(level, this.boundingBox, chunkBox, origin, this.seed());
        PortalStructureHelper.UndergroundLayout underground = PortalStructureHelper.buildUnderground(level, this.boundingBox, chunkBox, origin, this.variant, random);

        PortalStructureHelper.placePortalSpawners(level, this.boundingBox, chunkBox, origin, underground);
        this.placeSurfaceChests(level, chunkBox, random, origin);
        this.placeDeepChests(level, chunkBox, random, origin, underground.deepChests());
    }

    private void placeSurfaceChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, BlockPos origin) {
        List<BlockPos> chests = new ArrayList<>();
        chests.add(origin.offset(9, 0, -6));
        chests.add(origin.offset(-10, 0, 7));
        chests.add(origin.offset(22, 0, 12));
        chests.add(origin.offset(-24, 0, -14));
        chests.add(origin.offset(36, 0, -7));
        chests.add(origin.offset(-38, 0, 18));

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

    private void placeDeepChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, BlockPos origin, List<BlockPos> chests) {
        BlockPos conduitChest = NetherConduitChestPlacement.useBossChest(origin)
            ? null
            : NetherConduitChestPlacement.pickDeepChest(origin, chests);
        for (BlockPos chestPos : chests) {
            if (!this.boundingBox.isInside(chestPos) || !chunkBox.isInside(chestPos)) {
                continue;
            }
            this.setBlockIfInside(level, chunkBox, chestPos.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            this.setBlockIfInside(level, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
            this.createChest(level, chunkBox, random, chestPos, DEPTH_LOOT, null);
            if (chestPos.equals(conduitChest) && level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
                NetherConduitChestPlacement.addNetherConduit(chest);
            }
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
