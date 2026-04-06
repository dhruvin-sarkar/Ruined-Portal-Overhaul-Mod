package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.world.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

public class PortalDungeonPiece extends StructurePiece {
    private static final int SURFACE_RADIUS = 15;
    private static final int PIT_RADIUS = 3;
    private static final int PIT_DEPTH = 25;
    private static final int SUPPORT_DEPTH = 4;

    private static final ResourceKey<LootTable> SURFACE_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ModStructures.id("chests/portal_dungeon_surface")
    );
    private static final ResourceKey<LootTable> DEPTH_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ModStructures.id("chests/portal_dungeon_depth")
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
                center.getY() - PIT_DEPTH - 2,
                center.getZ() - SURFACE_RADIUS,
                center.getX() + SURFACE_RADIUS,
                center.getY() + 8,
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
        RandomSource seededRandom = RandomSource.create(this.seed());

        List<BlockPos> lavaPools = this.chooseSurfacePools(seededRandom);
        List<BlockPos> surfaceChests = this.chooseSurfaceChests(seededRandom);
        List<BlockPos> deepChests = this.chooseDeepChests(seededRandom);
        List<TunnelDefinition> tunnels = this.chooseTunnels(seededRandom);

        this.generateSurfaceScar(level, chunkBox, seededRandom, lavaPools, surfaceChests);
        this.generatePit(level, chunkBox);
        this.generateBottomChamber(level, chunkBox);

        for (TunnelDefinition tunnel : tunnels) {
            this.generateTunnel(level, chunkBox, tunnel);
        }

        this.placeSurfacePools(level, chunkBox, lavaPools);
        this.placePortalFrame(level, chunkBox);
        this.placeSurfaceChests(level, chunkBox, seededRandom, surfaceChests);
        this.placeDeepChests(level, chunkBox, seededRandom, deepChests);
    }

    private void generateSurfaceScar(
        WorldGenLevel level,
        BoundingBox chunkBox,
        RandomSource random,
        List<BlockPos> lavaPools,
        List<BlockPos> surfaceChests
    ) {
        int minX = Math.max(this.boundingBox.minX(), chunkBox.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBox.maxX());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBox.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBox.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - this.centerX;
                int dz = z - this.centerZ;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance > SURFACE_RADIUS) {
                    continue;
                }

                double strength = 1.2 - (distance / (double) SURFACE_RADIUS);
                if (random.nextDouble() > strength) {
                    continue;
                }

                BlockPos topPos = this.findTerrainTop(level, x, z);
                int topY = topPos.getY();
                boolean protectedFeature = this.isPortalPedestal(x, z)
                    || this.containsAtColumn(lavaPools, x, z)
                    || this.containsAtColumn(surfaceChests, x, z);

                int fillDepth = distance < 6.0 ? 3 : distance < 11.0 ? 2 : 1;
                BlockState topState = this.pickSurfaceBlock(random, distance);

                for (int depth = 0; depth < fillDepth; depth++) {
                    BlockPos fillPos = new BlockPos(x, topY - depth, z);
                    this.setBlockIfInside(level, chunkBox, fillPos, depth == 0 ? topState : Blocks.NETHERRACK.defaultBlockState());
                }

                if (!protectedFeature) {
                    this.setBlockIfInside(level, chunkBox, topPos.above(), Blocks.AIR.defaultBlockState());

                    if (topState.is(Blocks.SOUL_SAND) && random.nextFloat() < 0.32f) {
                        this.setBlockIfInside(level, chunkBox, topPos.above(), Blocks.NETHER_WART.defaultBlockState());
                    }
                }
            }
        }
    }

    private void generatePit(WorldGenLevel level, BoundingBox chunkBox) {
        int minY = Math.max(level.getMinY() + 2, this.surfaceY - PIT_DEPTH);

        for (int y = this.surfaceY - 1; y >= minY; y--) {
            for (int x = this.centerX - PIT_RADIUS; x <= this.centerX + PIT_RADIUS; x++) {
                for (int z = this.centerZ - PIT_RADIUS; z <= this.centerZ + PIT_RADIUS; z++) {
                    boolean wall = x == this.centerX - PIT_RADIUS
                        || x == this.centerX + PIT_RADIUS
                        || z == this.centerZ - PIT_RADIUS
                        || z == this.centerZ + PIT_RADIUS;
                    boolean supportColumn = y >= this.surfaceY - SUPPORT_DEPTH
                        && x >= this.centerX - 1 && x <= this.centerX + 1
                        && z >= this.centerZ - 1 && z <= this.centerZ + 1;

                    BlockPos pos = new BlockPos(x, y, z);

                    if (wall) {
                        this.setBlockIfInside(level, chunkBox, pos, this.netherBrickFor(y, x, z));
                    } else if (supportColumn) {
                        this.setBlockIfInside(level, chunkBox, pos, Blocks.NETHERRACK.defaultBlockState());
                    } else {
                        this.setBlockIfInside(level, chunkBox, pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    private void generateBottomChamber(WorldGenLevel level, BoundingBox chunkBox) {
        int chamberFloor = this.surfaceY - PIT_DEPTH;

        for (int x = this.centerX - 4; x <= this.centerX + 4; x++) {
            for (int z = this.centerZ - 4; z <= this.centerZ + 4; z++) {
                for (int y = chamberFloor; y <= chamberFloor + 4; y++) {
                    boolean boundary = x == this.centerX - 4
                        || x == this.centerX + 4
                        || z == this.centerZ - 4
                        || z == this.centerZ + 4
                        || y == chamberFloor
                        || y == chamberFloor + 4;

                    BlockPos pos = new BlockPos(x, y, z);
                    this.setBlockIfInside(level, chunkBox, pos, boundary ? this.netherBrickFor(y, x, z) : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void generateTunnel(WorldGenLevel level, BoundingBox chunkBox, TunnelDefinition tunnel) {
        int stepX = tunnel.direction.getStepX();
        int stepZ = tunnel.direction.getStepZ();
        int perpX = stepZ;
        int perpZ = stepX;

        for (int step = 0; step < tunnel.length; step++) {
            int segmentCenterX = tunnel.startX + stepX * step;
            int segmentCenterZ = tunnel.startZ + stepZ * step;

            for (int lateral = -1; lateral <= 1; lateral++) {
                for (int y = tunnel.floorY; y <= tunnel.floorY + 3; y++) {
                    int x = segmentCenterX + perpX * lateral;
                    int z = segmentCenterZ + perpZ * lateral;
                    boolean boundary = lateral == -1 || lateral == 1 || y == tunnel.floorY || y == tunnel.floorY + 3;

                    BlockPos pos = new BlockPos(x, y, z);
                    this.setBlockIfInside(level, chunkBox, pos, boundary ? this.netherBrickFor(y, x, z) : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void placeSurfacePools(WorldGenLevel level, BoundingBox chunkBox, List<BlockPos> lavaPools) {
        for (BlockPos target : lavaPools) {
            BlockPos topPos = this.findTerrainTop(level, target.getX(), target.getZ());
            boolean submerged = !level.getFluidState(topPos.above()).isEmpty();

            for (int x = target.getX() - 1; x <= target.getX(); x++) {
                for (int z = target.getZ() - 1; z <= target.getZ(); z++) {
                    BlockPos localTop = this.findTerrainTop(level, x, z);
                    this.setBlockIfInside(level, chunkBox, localTop, Blocks.NETHERRACK.defaultBlockState());
                    this.setBlockIfInside(level, chunkBox, localTop.below(), Blocks.NETHERRACK.defaultBlockState());
                    this.setBlockIfInside(level, chunkBox, localTop.above(), Blocks.AIR.defaultBlockState());
                    this.setBlockIfInside(level, chunkBox, localTop, submerged ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.LAVA.defaultBlockState());
                }
            }
        }
    }

    private void placePortalFrame(WorldGenLevel level, BoundingBox chunkBox) {
        int baseY = this.surfaceY + 1;

        for (int y = 0; y <= 5; y++) {
            this.placePortalBlock(level, chunkBox, this.centerX - 1, baseY + y, this.centerZ, y);
            this.placePortalBlock(level, chunkBox, this.centerX + 1, baseY + y, this.centerZ, y);
        }

        for (int x = -1; x <= 1; x++) {
            this.placePortalBlock(level, chunkBox, this.centerX + x, baseY, this.centerZ, x + 10);
            if (x != 0) {
                this.placePortalBlock(level, chunkBox, this.centerX + x, baseY + 5, this.centerZ, x + 20);
            }
        }

        for (int x = this.centerX - 1; x <= this.centerX + 1; x++) {
            for (int z = this.centerZ - 1; z <= this.centerZ + 1; z++) {
                this.setBlockIfInside(level, chunkBox, new BlockPos(x, this.surfaceY, z), Blocks.NETHERRACK.defaultBlockState());
            }
        }
    }

    private void placePortalBlock(WorldGenLevel level, BoundingBox chunkBox, int x, int y, int z, int salt) {
        if ((salt == 21) || (salt == 11)) {
            return;
        }

        BlockState state = ((x + y + z + salt) & 1) == 0
            ? Blocks.OBSIDIAN.defaultBlockState()
            : Blocks.CRYING_OBSIDIAN.defaultBlockState();
        this.setBlockIfInside(level, chunkBox, new BlockPos(x, y, z), state);
    }

    private void placeSurfaceChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, List<BlockPos> chests) {
        for (BlockPos target : chests) {
            BlockPos topPos = this.findTerrainTop(level, target.getX(), target.getZ());
            BlockPos chestPos = topPos.above();

            this.setBlockIfInside(level, chunkBox, topPos, Blocks.NETHERRACK.defaultBlockState());
            this.setBlockIfInside(level, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
            this.createChest(level, chunkBox, random, chestPos, SURFACE_LOOT, null);
        }
    }

    private void placeDeepChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random, List<BlockPos> chests) {
        for (BlockPos chestPos : chests) {
            this.setBlockIfInside(level, chunkBox, chestPos.below(), Blocks.NETHER_BRICKS.defaultBlockState());
            this.setBlockIfInside(level, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
            this.createChest(level, chunkBox, random, chestPos, DEPTH_LOOT, null);
        }
    }

    private List<BlockPos> chooseSurfacePools(RandomSource random) {
        List<BlockPos> candidates = List.of(
            new BlockPos(this.centerX - 9, this.surfaceY, this.centerZ - 7),
            new BlockPos(this.centerX + 8, this.surfaceY, this.centerZ - 8),
            new BlockPos(this.centerX - 7, this.surfaceY, this.centerZ + 9),
            new BlockPos(this.centerX + 9, this.surfaceY, this.centerZ + 7)
        );

        return this.pickUniquePositions(random, candidates, 1 + random.nextInt(2));
    }

    private List<BlockPos> chooseSurfaceChests(RandomSource random) {
        List<BlockPos> candidates = List.of(
            new BlockPos(this.centerX - 12, this.surfaceY, this.centerZ - 2),
            new BlockPos(this.centerX + 11, this.surfaceY, this.centerZ - 4),
            new BlockPos(this.centerX - 4, this.surfaceY, this.centerZ + 12),
            new BlockPos(this.centerX + 8, this.surfaceY, this.centerZ + 10)
        );

        return this.pickUniquePositions(random, candidates, 2 + random.nextInt(2));
    }

    private List<BlockPos> chooseDeepChests(RandomSource random) {
        List<BlockPos> candidates = List.of(
            new BlockPos(this.centerX, this.surfaceY - PIT_DEPTH + 1, this.centerZ - 9),
            new BlockPos(this.centerX + 9, this.surfaceY - PIT_DEPTH + 1, this.centerZ),
            new BlockPos(this.centerX, this.surfaceY - PIT_DEPTH + 1, this.centerZ + 9),
            new BlockPos(this.centerX - 9, this.surfaceY - PIT_DEPTH + 1, this.centerZ)
        );

        return this.pickUniquePositions(random, candidates, 2);
    }

    private List<TunnelDefinition> chooseTunnels(RandomSource random) {
        int chamberFloor = this.surfaceY - PIT_DEPTH;
        List<TunnelDefinition> tunnels = new ArrayList<>();
        tunnels.add(new TunnelDefinition(this.centerX, chamberFloor, this.centerZ - 4, Direction.NORTH, 6 + random.nextInt(4)));
        tunnels.add(new TunnelDefinition(this.centerX + 4, chamberFloor, this.centerZ, Direction.EAST, 6 + random.nextInt(4)));
        tunnels.add(new TunnelDefinition(this.centerX, chamberFloor, this.centerZ + 4, Direction.SOUTH, 6 + random.nextInt(4)));
        tunnels.add(new TunnelDefinition(this.centerX - 4, chamberFloor, this.centerZ, Direction.WEST, 6 + random.nextInt(4)));
        return tunnels;
    }

    private List<BlockPos> pickUniquePositions(RandomSource random, List<BlockPos> candidates, int amount) {
        List<BlockPos> pool = new ArrayList<>(candidates);
        List<BlockPos> picked = new ArrayList<>();

        for (int i = 0; i < amount && !pool.isEmpty(); i++) {
            picked.add(pool.remove(random.nextInt(pool.size())));
        }

        return picked;
    }

    private boolean containsAtColumn(List<BlockPos> positions, int x, int z) {
        for (BlockPos pos : positions) {
            if (pos.getX() == x && pos.getZ() == z) {
                return true;
            }
        }

        return false;
    }

    private boolean isPortalPedestal(int x, int z) {
        return x >= this.centerX - 1 && x <= this.centerX + 1 && z >= this.centerZ - 1 && z <= this.centerZ + 1;
    }

    private BlockPos findTerrainTop(WorldGenLevel level, int x, int z) {
        int worldSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;

        if (worldSurface - oceanFloor > 1) {
            return new BlockPos(x, oceanFloor, z);
        }

        return new BlockPos(x, worldSurface, z);
    }

    private BlockState pickSurfaceBlock(RandomSource random, double distance) {
        if (distance < 6.5 && random.nextFloat() < 0.18f) {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
        if (random.nextFloat() < 0.18f) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (random.nextFloat() < 0.22f) {
            return Blocks.SOUL_SAND.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    private BlockState netherBrickFor(int y, int x, int z) {
        return ((x + z + y) & 3) == 0
            ? Blocks.CRACKED_NETHER_BRICKS.defaultBlockState()
            : Blocks.NETHER_BRICKS.defaultBlockState();
    }

    private void setBlockIfInside(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos, BlockState state) {
        if (chunkBox.isInside(pos)) {
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

    private record TunnelDefinition(int startX, int floorY, int startZ, Direction direction, int length) {
    }
}
