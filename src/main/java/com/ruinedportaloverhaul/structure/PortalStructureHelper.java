package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.world.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;

public final class PortalStructureHelper {
    public static final int INNER_RADIUS = 15;
    public static final int MIDDLE_RADIUS = 52;
    public static final int OUTER_RADIUS = 136;
    public static final int PIT_DEPTH = 45;

    private PortalStructureHelper() {
    }

    public static PortalFrameSpec pickFrame(RandomSource random) {
        boolean largeFrame = random.nextBoolean();
        return new PortalFrameSpec(largeFrame ? 6 : 4, largeFrame ? 7 : 5, Direction.Axis.X);
    }

    public static void buildInnerZone(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalFrameSpec frame,
        RandomSource random
    ) {
        int minX = Math.max(origin.getX() - INNER_RADIUS, Math.max(pieceBox.minX(), chunkBox.minX()));
        int maxX = Math.min(origin.getX() + INNER_RADIUS, Math.min(pieceBox.maxX(), chunkBox.maxX()));
        int minZ = Math.max(origin.getZ() - INNER_RADIUS, Math.max(pieceBox.minZ(), chunkBox.minZ()));
        int maxZ = Math.min(origin.getZ() + INNER_RADIUS, Math.min(pieceBox.maxZ(), chunkBox.maxZ()));

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > INNER_RADIUS + 0.5) {
                    continue;
                }

                BlockPos top = terrainTop(level, x, z);
                BlockState ground = random.nextFloat() < 0.16f
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.NETHERRACK.defaultBlockState();
                setColumn(level, pieceBox, chunkBox, top, ground, 3);
                set(level, pieceBox, chunkBox, top.above(), Blocks.AIR.defaultBlockState());
            }
        }

        placeRitualPlatform(level, pieceBox, chunkBox, origin, random);
        placePortalFrame(level, pieceBox, chunkBox, origin, frame, random);
        placeAnchor(level, pieceBox, chunkBox, origin);
    }

    public static void buildMiddleZone(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        RandomSource random
    ) {
        int minX = Math.max(origin.getX() - MIDDLE_RADIUS, Math.max(pieceBox.minX(), chunkBox.minX()));
        int maxX = Math.min(origin.getX() + MIDDLE_RADIUS, Math.min(pieceBox.maxX(), chunkBox.maxX()));
        int minZ = Math.max(origin.getZ() - MIDDLE_RADIUS, Math.max(pieceBox.minZ(), chunkBox.minZ()));
        int maxZ = Math.min(origin.getZ() + MIDDLE_RADIUS, Math.min(pieceBox.maxZ(), chunkBox.maxZ()));

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= INNER_RADIUS || distance > MIDDLE_RADIUS + 0.5) {
                    continue;
                }

                BlockPos top = terrainTop(level, x, z);
                setColumn(level, pieceBox, chunkBox, top, pickMiddleGround(random), distance < MIDDLE_RADIUS * 0.65 ? 3 : 2);
                if (random.nextFloat() < 0.08f) {
                    set(level, pieceBox, chunkBox, top.above(), Blocks.AIR.defaultBlockState());
                }
            }
        }

        int wartPatches = 4 + random.nextInt(5);
        for (int i = 0; i < wartPatches; i++) {
            placeWartPatch(level, pieceBox, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 3, MIDDLE_RADIUS - 4), random);
        }

        int lavaPools = 3 + random.nextInt(3);
        for (int i = 0; i < lavaPools; i++) {
            placeContainedLavaPool(level, pieceBox, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 5, MIDDLE_RADIUS - 8), 3 + random.nextInt(4), random);
        }

        int glowstoneClusters = 2 + random.nextInt(2);
        for (int i = 0; i < glowstoneClusters; i++) {
            placeGlowstoneGroundCluster(level, pieceBox, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 4, MIDDLE_RADIUS - 8), random);
        }

        int basaltColumns = 4 + random.nextInt(5);
        for (int i = 0; i < basaltColumns; i++) {
            BlockPos column = randomRingPos(origin, random, INNER_RADIUS + 2, MIDDLE_RADIUS - 2);
            BlockPos top = terrainTopIfColumnInside(level, chunkBox, column);
            if (top != null) {
                placeBasaltColumn(level, pieceBox, chunkBox, top, 2 + random.nextInt(4));
            }
        }

        int shroomlights = 1 + random.nextInt(2);
        for (int i = 0; i < shroomlights; i++) {
            BlockPos top = terrainTopIfColumnInside(level, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 6, MIDDLE_RADIUS - 5));
            if (top != null) {
                set(level, pieceBox, chunkBox, top, Blocks.SHROOMLIGHT.defaultBlockState());
            }
        }
    }

    public static void buildOuterScatter(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        long seed
    ) {
        int minX = Math.max(pieceBox.minX(), chunkBox.minX());
        int maxX = Math.min(pieceBox.maxX(), chunkBox.maxX());
        int minZ = Math.max(pieceBox.minZ(), chunkBox.minZ());
        int maxZ = Math.min(pieceBox.maxZ(), chunkBox.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= MIDDLE_RADIUS || distance > OUTER_RADIUS) {
                    continue;
                }

                double gradient = 1.0 - ((distance - MIDDLE_RADIUS) / (OUTER_RADIUS - MIDDLE_RADIUS));
                double roll = coordinateNoise(seed, x, z);
                if (roll > 0.035 + gradient * 0.13) {
                    continue;
                }

                RandomSource localRandom = RandomSource.create(seed ^ hashColumn(x, z));
                BlockPos center = terrainTop(level, x, z);
                placeOuterPatch(level, pieceBox, chunkBox, center, localRandom, gradient);
            }
        }
    }

    public static UndergroundLayout buildUnderground(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        RandomSource random
    ) {
        int chamberCenterY = Math.max(level.getMinY() + 14, origin.getY() - 33);
        int chamberFloorY = chamberCenterY - 6;
        BlockPos chamberCenter = new BlockPos(origin.getX(), chamberCenterY, origin.getZ());

        carvePit(level, pieceBox, chunkBox, origin, chamberCenterY + 6);
        carvePrimaryChamber(level, pieceBox, chunkBox, chamberCenter, random);
        placeChamberSpikes(level, pieceBox, chunkBox, chamberCenter, random);
        placeLargeLavaLake(level, pieceBox, chunkBox, new BlockPos(origin.getX(), chamberFloorY + 1, origin.getZ()), random);

        for (int i = 0; i < 4; i++) {
            placeCeilingGlowstone(level, pieceBox, chunkBox, chamberCenter.offset(random.nextInt(17) - 8, 7, random.nextInt(17) - 8), random);
        }
        for (int i = 0; i < 3; i++) {
            placeGlowstoneStalactite(level, pieceBox, chunkBox, chamberCenter.offset(random.nextInt(15) - 7, 7, random.nextInt(15) - 7), random);
        }

        BlockPos altarCenter = chamberCenter.offset(0, -4, -7);
        List<BlockPos> deepChests = placeAltar(level, pieceBox, chunkBox, altarCenter);

        int tunnelCount = 3 + random.nextInt(3);
        List<TunnelPath> tunnels = new ArrayList<>();
        for (int i = 0; i < tunnelCount; i++) {
            double angle = ((Math.PI * 2.0) / tunnelCount) * i + random.nextDouble() * 0.55;
            int length = 30 + random.nextInt(31);
            TunnelPath tunnel = carveTunnel(level, pieceBox, chunkBox, chamberCenter, angle, length, random);
            tunnels.add(tunnel);
            if (random.nextBoolean()) {
                carveTunnel(level, pieceBox, chunkBox, tunnel.end(), angle + (random.nextBoolean() ? 0.85 : -0.85), 14 + random.nextInt(16), random);
            }
        }

        connectTunnelNetwork(level, pieceBox, chunkBox, tunnels, random);

        BlockPos tunnelSpawner = tunnels.isEmpty()
            ? chamberCenter.offset(8, -5, 0)
            : tunnels.get(tunnels.size() - 1).end();
        return new UndergroundLayout(chamberCenter, chamberFloorY, altarCenter, deepChests, tunnelSpawner);
    }

    public static List<BlockPos> placePortalSpawners(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        UndergroundLayout layout
    ) {
        List<BlockPos> spawners = new ArrayList<>();
        BlockPos firstSurfaceSpawner = terrainTopIfColumnInside(level, chunkBox, origin.offset(24, 0, -14));
        BlockPos secondSurfaceSpawner = terrainTopIfColumnInside(level, chunkBox, origin.offset(-22, 0, 18));
        spawners.add(firstSurfaceSpawner == null ? null : placeConfiguredSpawner(level, pieceBox, chunkBox, firstSurfaceSpawner.above(), EntityType.ZOMBIFIED_PIGLIN, 2, 6, 200, 400, 24, 6, false));
        spawners.add(secondSurfaceSpawner == null ? null : placeConfiguredSpawner(level, pieceBox, chunkBox, secondSurfaceSpawner.above(), EntityType.ZOMBIFIED_PIGLIN, 2, 6, 200, 400, 24, 6, false));
        spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, layout.chamberCenter().offset(6, -5, 0), EntityType.MAGMA_CUBE, 3, 8, 150, 300, 16, 8, true));
        spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, layout.tunnelSpawner().above(), EntityType.WITHER_SKELETON, 1, 10, 400, 800, 20, 10, false));
        spawners.removeIf(pos -> pos == null);
        return spawners;
    }

    public static BlockPos terrainTop(WorldGenLevel level, BlockPos pos) {
        return terrainTop(level, pos.getX(), pos.getZ());
    }

    public static BlockPos terrainTopIfColumnInside(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos) {
        return isColumnInside(chunkBox, pos) ? terrainTop(level, pos) : null;
    }

    public static boolean isColumnInside(BoundingBox box, BlockPos pos) {
        return pos.getX() >= box.minX()
            && pos.getX() <= box.maxX()
            && pos.getZ() >= box.minZ()
            && pos.getZ() <= box.maxZ();
    }

    public static BlockPos terrainTop(WorldGenLevel level, int x, int z) {
        int worldSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;

        if (worldSurface - oceanFloor > 1) {
            return new BlockPos(x, oceanFloor, z);
        }
        return new BlockPos(x, worldSurface, z);
    }

    private static void placeRitualPlatform(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        RandomSource random
    ) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx * dx + dz * dz > 30) {
                    continue;
                }
                BlockPos pos = new BlockPos(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                setColumn(level, pieceBox, chunkBox, pos, random.nextInt(5) == 0 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void placePortalFrame(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalFrameSpec frame,
        RandomSource random
    ) {
        int baseY = origin.getY() + 1;
        int left = -frame.width() / 2;
        int right = left + frame.width() - 1;
        int topY = baseY + frame.height() - 1;

        for (int y = baseY; y <= topY; y++) {
            placeFrameBlock(level, pieceBox, chunkBox, origin.offset(left, y - origin.getY(), 0), random);
            placeFrameBlock(level, pieceBox, chunkBox, origin.offset(right, y - origin.getY(), 0), random);
        }

        for (int dx = left; dx <= right; dx++) {
            placeFrameBlock(level, pieceBox, chunkBox, origin.offset(dx, 1, 0), random);
            placeFrameBlock(level, pieceBox, chunkBox, origin.offset(dx, topY - origin.getY(), 0), random);
        }

        for (int dx = left + 1; dx < right; dx++) {
            for (int y = baseY + 1; y < topY; y++) {
                set(level, pieceBox, chunkBox, new BlockPos(origin.getX() + dx, y, origin.getZ()), Blocks.AIR.defaultBlockState());
            }
        }

        int chainCount = Math.min(4, frame.width() - 2);
        for (int i = 0; i < 4; i++) {
            int dx = left + 1 + Math.min(chainCount - 1, i * Math.max(1, chainCount - 1) / 3);
            int zOffset = frame.width() == 4 && i >= 2 ? (i == 2 ? -1 : 1) : 0;
            int length = 2 + (i & 1);
            for (int drop = 1; drop <= length; drop++) {
                set(level, pieceBox, chunkBox, new BlockPos(origin.getX() + dx, topY - drop, origin.getZ() + zOffset), Blocks.IRON_CHAIN.defaultBlockState());
            }
        }
    }

    private static void placeFrameBlock(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos pos,
        RandomSource random
    ) {
        BlockState state = random.nextFloat() < 0.4f
            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
            : Blocks.OBSIDIAN.defaultBlockState();
        set(level, pieceBox, chunkBox, pos, state);
    }

    private static void placeAnchor(WorldGenLevel level, BoundingBox pieceBox, BoundingBox chunkBox, BlockPos origin) {
        set(level, pieceBox, chunkBox, origin.offset(4, 1, 0), Blocks.NETHER_BRICK_FENCE.defaultBlockState());
        set(level, pieceBox, chunkBox, origin.offset(4, 1, 1), Blocks.CRIMSON_FENCE_GATE.defaultBlockState());
    }

    private static BlockState pickMiddleGround(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.70f) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (roll < 0.85f) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (roll < 0.95f) {
            return Blocks.SOUL_SAND.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    private static void placeWartPatch(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (random.nextFloat() < 0.18f) {
                    continue;
                }
                BlockPos top = terrainTopIfColumnInside(level, chunkBox, center.offset(dx, 0, dz));
                if (top == null) {
                    continue;
                }
                set(level, pieceBox, chunkBox, top, Blocks.SOUL_SAND.defaultBlockState());
                set(level, pieceBox, chunkBox, top.above(), Blocks.NETHER_WART.defaultBlockState());
            }
        }
    }

    private static void placeContainedLavaPool(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        int radius,
        RandomSource random
    ) {
        BlockPos topCenter = terrainTopIfColumnInside(level, chunkBox, center);
        if (topCenter == null) {
            return;
        }
        int lavaY = topCenter.getY();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.35;
                BlockPos columnTop = terrainTopIfColumnInside(level, chunkBox, topCenter.offset(dx, 0, dz));
                if (columnTop == null) {
                    continue;
                }
                BlockPos pos = new BlockPos(columnTop.getX(), lavaY, columnTop.getZ());
                if (distance <= radius) {
                    set(level, pieceBox, chunkBox, pos.below(), Blocks.NETHERRACK.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                } else if (distance <= radius + 1.5) {
                    set(level, pieceBox, chunkBox, pos, Blocks.MAGMA_BLOCK.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void placeGlowstoneGroundCluster(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        BlockPos top = terrainTopIfColumnInside(level, chunkBox, center);
        if (top == null) {
            return;
        }
        int blocks = 3 + random.nextInt(4);
        for (int i = 0; i < blocks; i++) {
            set(level, pieceBox, chunkBox, top.offset(random.nextInt(3) - 1, random.nextInt(2), random.nextInt(3) - 1), Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    private static void placeBasaltColumn(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos base,
        int height
    ) {
        for (int y = 0; y < height; y++) {
            set(level, pieceBox, chunkBox, base.above(y), Blocks.BASALT.defaultBlockState());
        }
    }

    private static void placeOuterPatch(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random,
        double gradient
    ) {
        int radius = 1 + random.nextInt(gradient > 0.55 ? 4 : 3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
                    continue;
                }
                BlockPos top = terrainTopIfColumnInside(level, chunkBox, center.offset(dx, 0, dz));
                if (top == null) {
                    continue;
                }
                float roll = random.nextFloat();
                if (roll < 0.70f) {
                    set(level, pieceBox, chunkBox, top, Blocks.NETHERRACK.defaultBlockState());
                } else if (roll < 0.84f) {
                    set(level, pieceBox, chunkBox, top, Blocks.SOUL_SAND.defaultBlockState());
                } else if (roll < 0.94f) {
                    set(level, pieceBox, chunkBox, top.above(), Blocks.DEAD_BUSH.defaultBlockState());
                } else if (roll < 0.985f) {
                    set(level, pieceBox, chunkBox, top, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                } else {
                    placeBonePillar(level, pieceBox, chunkBox, top.above(), 1 + random.nextInt(3));
                }
            }
        }
    }

    private static void placeBonePillar(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos base,
        int height
    ) {
        for (int y = 0; y < height; y++) {
            set(level, pieceBox, chunkBox, base.above(y), Blocks.BONE_BLOCK.defaultBlockState());
        }
    }

    private static void carvePit(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int bottomY
    ) {
        int topY = origin.getY();
        for (int y = topY; y >= bottomY; y--) {
            double t = (topY - y) / (double) Math.max(1, topY - bottomY);
            double radius = 9.5 - t * 3.0;
            for (int dx = -11; dx <= 11; dx++) {
                for (int dz = -11; dz <= 11; dz++) {
                    double noise = coordinateNoise(origin.asLong(), origin.getX() + dx, origin.getZ() + dz) * 1.6 - 0.8;
                    double distance = Math.sqrt(dx * dx + dz * dz) + noise;
                    BlockPos pos = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    if (distance <= radius) {
                        set(level, pieceBox, chunkBox, pos, Blocks.AIR.defaultBlockState());
                    } else if (distance <= radius + 1.4) {
                        set(level, pieceBox, chunkBox, pos, corruptedWallBlock(pos));
                    }
                }
            }
        }
    }

    private static void carvePrimaryChamber(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        for (int dx = -14; dx <= 14; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -14; dz <= 14; dz++) {
                    double normalized = (dx * dx) / 156.25 + (dy * dy) / 49.0 + (dz * dz) / 156.25;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (normalized <= 0.86) {
                        set(level, pieceBox, chunkBox, pos, Blocks.AIR.defaultBlockState());
                    } else if (normalized <= 1.10) {
                        set(level, pieceBox, chunkBox, pos, pickChamberWall(level, pos, random));
                    }
                    if (dy == -6 && normalized <= 0.96) {
                        set(level, pieceBox, chunkBox, pos, pickChamberFloor(random));
                    }
                }
            }
        }
    }

    private static void placeLargeLavaLake(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        int radiusX = 7 + random.nextInt(2);
        int radiusZ = 5 + random.nextInt(3);
        for (int dx = -radiusX - 1; dx <= radiusX + 1; dx++) {
            for (int dz = -radiusZ - 1; dz <= radiusZ + 1; dz++) {
                double normalized = (dx * dx) / (double) (radiusX * radiusX) + (dz * dz) / (double) (radiusZ * radiusZ);
                BlockPos pos = center.offset(dx, 0, dz);
                if (normalized <= 1.0) {
                    set(level, pieceBox, chunkBox, pos.below(), Blocks.NETHERRACK.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                    if (random.nextFloat() < 0.08f) {
                        placeBasaltColumn(level, pieceBox, chunkBox, pos.above(), 2 + random.nextInt(5));
                    }
                } else if (normalized <= 1.35) {
                    set(level, pieceBox, chunkBox, pos, Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
        }
    }

    private static void placeCeilingGlowstone(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos nearCeiling,
        RandomSource random
    ) {
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            set(level, pieceBox, chunkBox, nearCeiling.offset(random.nextInt(3) - 1, random.nextInt(2), random.nextInt(3) - 1), Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    private static void placeGlowstoneStalactite(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos ceiling,
        RandomSource random
    ) {
        int length = 4 + random.nextInt(5);
        for (int i = 0; i < length; i++) {
            set(level, pieceBox, chunkBox, ceiling.below(i), Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    private static void placeChamberSpikes(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos chamberCenter,
        RandomSource random
    ) {
        for (int i = 0; i < 7; i++) {
            BlockPos ceiling = chamberCenter.offset(random.nextInt(23) - 11, 7, random.nextInt(23) - 11);
            placeNetherSpike(level, pieceBox, chunkBox, ceiling, Direction.DOWN, 3 + random.nextInt(6), random);
        }
        for (int i = 0; i < 6; i++) {
            BlockPos floor = chamberCenter.offset(random.nextInt(21) - 10, -5, random.nextInt(21) - 10);
            placeNetherSpike(level, pieceBox, chunkBox, floor, Direction.UP, 2 + random.nextInt(5), random);
        }
    }

    private static void placeNetherSpike(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos base,
        Direction direction,
        int length,
        RandomSource random
    ) {
        for (int i = 0; i < length; i++) {
            BlockPos pos = base.relative(direction, i);
            BlockState state = random.nextFloat() < 0.65f
                ? Blocks.BASALT.defaultBlockState()
                : Blocks.BLACKSTONE.defaultBlockState();
            set(level, pieceBox, chunkBox, pos, state);
            if (i < length / 2 && random.nextFloat() < 0.35f) {
                set(level, pieceBox, chunkBox, pos.offset(1, 0, 0), Blocks.BLACKSTONE.defaultBlockState());
            }
            if (i < length / 2 && random.nextFloat() < 0.35f) {
                set(level, pieceBox, chunkBox, pos.offset(0, 0, 1), Blocks.BLACKSTONE.defaultBlockState());
            }
        }
    }

    private static List<BlockPos> placeAltar(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center
    ) {
        List<BlockPos> chests = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                set(level, pieceBox, chunkBox, center.offset(dx, -1, dz), Blocks.BLACKSTONE.defaultBlockState());
            }
        }
        set(level, pieceBox, chunkBox, center.above(), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] corner : corners) {
            BlockPos bar = center.offset(corner[0], 0, corner[1]);
            set(level, pieceBox, chunkBox, bar, Blocks.IRON_BARS.defaultBlockState());
            set(level, pieceBox, chunkBox, bar.above(), Blocks.SOUL_LANTERN.defaultBlockState());
        }
        chests.add(center.offset(-1, 1, 1));
        chests.add(center.offset(1, 1, 1));
        return chests;
    }

    private static TunnelPath carveTunnel(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos chamberCenter,
        double angle,
        int length,
        RandomSource random
    ) {
        int floorY = chamberCenter.getY() - 5;
        int startX = chamberCenter.getX() + (int) Math.round(Math.cos(angle) * 11.0);
        int startZ = chamberCenter.getZ() + (int) Math.round(Math.sin(angle) * 11.0);
        BlockPos last = new BlockPos(startX, floorY, startZ);

        for (int step = 0; step < length; step++) {
            int x = startX + (int) Math.round(Math.cos(angle) * step);
            int z = startZ + (int) Math.round(Math.sin(angle) * step);
            if (Math.abs(x - chamberCenter.getX()) > 50 || Math.abs(z - chamberCenter.getZ()) > 50) {
                break;
            }
            int y = floorY + (int) Math.round(Math.sin(step * 0.25) * 1.5);
            BlockPos center = new BlockPos(x, y, z);
            carveTunnelSegment(level, pieceBox, chunkBox, center, random);
            if (step % 8 == 4) {
                set(level, pieceBox, chunkBox, center.offset(2, -1, 0), Blocks.SOUL_TORCH.defaultBlockState());
            }
            if (step > 6 && step % 12 == 5 && random.nextFloat() < 0.45f) {
                placeTunnelLavaRun(level, pieceBox, chunkBox, center, angle, random);
            }
            last = center;
        }
        return new TunnelPath(new BlockPos(startX, floorY, startZ), last);
    }

    private static void carveTunnelSegment(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        int radius = 3;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double normalized = (dx * dx + dz * dz) / 9.0 + (dy * dy) / 5.0;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (normalized <= 0.72) {
                        set(level, pieceBox, chunkBox, pos, Blocks.AIR.defaultBlockState());
                    } else if (normalized <= 1.15) {
                        set(level, pieceBox, chunkBox, pos, pickTunnelWall(pos, random));
                    }
                }
            }
        }
    }

    private static void placeTunnelLavaRun(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos start,
        double angle,
        RandomSource random
    ) {
        int length = 4 + random.nextInt(5);
        for (int step = 0; step < length; step++) {
            int x = start.getX() + (int) Math.round(Math.cos(angle) * step);
            int z = start.getZ() + (int) Math.round(Math.sin(angle) * step);
            BlockPos channel = new BlockPos(x, start.getY() - 1, z);
            set(level, pieceBox, chunkBox, channel.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
            set(level, pieceBox, chunkBox, channel, Blocks.LAVA.defaultBlockState());
            set(level, pieceBox, chunkBox, channel.above(), Blocks.AIR.defaultBlockState());
            if (step == 0 || step == length - 1) {
                set(level, pieceBox, chunkBox, channel.offset(1, 0, 0), Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, pieceBox, chunkBox, channel.offset(-1, 0, 0), Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, pieceBox, chunkBox, channel.offset(0, 0, 1), Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, pieceBox, chunkBox, channel.offset(0, 0, -1), Blocks.MAGMA_BLOCK.defaultBlockState());
            }
        }
    }

    private static void connectTunnelNetwork(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        List<TunnelPath> tunnels,
        RandomSource random
    ) {
        if (tunnels.size() < 3) {
            return;
        }
        for (int i = 0; i < tunnels.size(); i++) {
            if (random.nextFloat() > 0.7f) {
                continue;
            }
            TunnelPath current = tunnels.get(i);
            TunnelPath next = tunnels.get((i + 1) % tunnels.size());
            carveConnectorTunnel(level, pieceBox, chunkBox, current.end(), next.end(), random);
        }
    }

    private static void carveConnectorTunnel(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos start,
        BlockPos end,
        RandomSource random
    ) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();
        int steps = Math.max(8, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz)));
        for (int step = 0; step <= steps; step++) {
            double t = step / (double) steps;
            int x = start.getX() + (int) Math.round(dx * t);
            int y = start.getY() + (int) Math.round(dy * t + Math.sin(t * Math.PI) * 2.0);
            int z = start.getZ() + (int) Math.round(dz * t);
            BlockPos center = new BlockPos(x, y, z);
            carveTunnelSegment(level, pieceBox, chunkBox, center, random);
            if (step % 10 == 5 && random.nextBoolean()) {
                set(level, pieceBox, chunkBox, center.offset(0, -1, 2), Blocks.SOUL_TORCH.defaultBlockState());
            }
        }
    }

    private static BlockState pickTunnelWall(BlockPos pos, RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.015f) {
            return Blocks.ANCIENT_DEBRIS.defaultBlockState();
        }
        if (roll < 0.08f) {
            return Blocks.NETHER_GOLD_ORE.defaultBlockState();
        }
        if (roll < 0.19f) {
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        }
        if (roll < 0.28f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (pos.getY() < 24 && roll < 0.55f) {
            return Blocks.BASALT.defaultBlockState();
        }
        if (pos.getY() < 16 && roll < 0.72f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    private static BlockState pickChamberWall(WorldGenLevel level, BlockPos pos, RandomSource random) {
        float roll = random.nextFloat();
        if (pos.getY() < 16) {
            if (roll < 0.55f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.85f) {
                return Blocks.BASALT.defaultBlockState();
            }
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (pos.getY() < 32) {
            if (roll < 0.35f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.62f) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.72f) {
                return Blocks.SOUL_SOIL.defaultBlockState();
            }
        }
        if (roll < 0.12f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.22f) {
            return Blocks.BASALT.defaultBlockState();
        }
        return corruptedWallBlock(pos);
    }

    private static BlockState pickChamberFloor(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.60f) {
            return Blocks.SOUL_SAND.defaultBlockState();
        }
        if (roll < 0.90f) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
        return Blocks.BASALT.defaultBlockState();
    }

    private static BlockState corruptedWallBlock(BlockPos pos) {
        if (pos.getY() < 0) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    private static BlockPos placeConfiguredSpawner(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos pos,
        EntityType<?> type,
        int spawnCount,
        int spawnRange,
        int minDelay,
        int maxDelay,
        int requiredPlayerRange,
        int maxNearbyEntities,
        boolean smallMagmaCube
    ) {
        if (!pieceBox.isInside(pos) || !chunkBox.isInside(pos)) {
            return null;
        }
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, level.getRandom());
            CompoundTag tag = spawner.saveWithFullMetadata(level.registryAccess());
            tag.putShort("SpawnCount", (short) spawnCount);
            tag.putShort("SpawnRange", (short) spawnRange);
            tag.putShort("MinSpawnDelay", (short) minDelay);
            tag.putShort("MaxSpawnDelay", (short) maxDelay);
            tag.putShort("RequiredPlayerRange", (short) requiredPlayerRange);
            tag.putShort("MaxNearbyEntities", (short) maxNearbyEntities);

            CompoundTag entity = new CompoundTag();
            entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
            if (smallMagmaCube) {
                entity.putInt("Size", 1);
            }
            CompoundTag spawnData = new CompoundTag();
            spawnData.put("entity", entity);
            tag.put("SpawnData", spawnData);
            spawner.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            spawner.setChanged();
        }
        return pos.immutable();
    }

    private static BlockPos randomRingPos(BlockPos origin, RandomSource random, int minRadius, int maxRadius) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
        return origin.offset((int) Math.round(Math.cos(angle) * radius), 0, (int) Math.round(Math.sin(angle) * radius));
    }

    private static void setColumn(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos top,
        BlockState surface,
        int depth
    ) {
        set(level, pieceBox, chunkBox, top, surface);
        for (int i = 1; i < depth; i++) {
            set(level, pieceBox, chunkBox, top.below(i), Blocks.NETHERRACK.defaultBlockState());
        }
    }

    private static void set(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos pos,
        BlockState state
    ) {
        if (pieceBox.isInside(pos) && chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static double coordinateNoise(long seed, int x, int z) {
        long hash = hashColumn(x, z) ^ seed;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        return (hash & 0x1FFFFFL) / (double) 0x1FFFFFL;
    }

    private static long hashColumn(int x, int z) {
        long hash = 1469598103934665603L;
        hash = (hash ^ x) * 1099511628211L;
        hash = (hash ^ z) * 1099511628211L;
        hash = (hash ^ ModStructures.PORTAL_DUNGEON_ID.hashCode()) * 1099511628211L;
        return hash;
    }

    public record PortalFrameSpec(int width, int height, Direction.Axis axis) {
    }

    public record UndergroundLayout(
        BlockPos chamberCenter,
        int chamberFloorY,
        BlockPos altarCenter,
        List<BlockPos> deepChests,
        BlockPos tunnelSpawner
    ) {
    }

    private record TunnelPath(BlockPos start, BlockPos end) {
    }
}
