package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.world.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
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
                setColumn(level, pieceBox, chunkBox, top, Blocks.NETHERRACK.defaultBlockState(), 3);
                convertLocalWaterToLava(level, pieceBox, chunkBox, top);
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
                setColumn(level, pieceBox, chunkBox, top, Blocks.NETHERRACK.defaultBlockState(), distance < MIDDLE_RADIUS * 0.65 ? 3 : 2);
                convertLocalWaterToLava(level, pieceBox, chunkBox, top);
                if (random.nextFloat() < 0.08f) {
                    set(level, pieceBox, chunkBox, top.above(), Blocks.AIR.defaultBlockState());
                }
            }
        }

        int lavaPools = 9 + random.nextInt(5);
        for (int i = 0; i < lavaPools; i++) {
            placeContainedLavaPool(level, pieceBox, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 5, MIDDLE_RADIUS - 8), 5 + random.nextInt(4), random);
        }

        int basaltFormations = 7 + random.nextInt(6);
        for (int i = 0; i < basaltFormations; i++) {
            placeBasaltFormation(level, pieceBox, chunkBox, randomRingPos(origin, random, INNER_RADIUS + 4, MIDDLE_RADIUS - 3), random);
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
                if (roll > 0.045 + gradient * 0.18) {
                    continue;
                }

                RandomSource localRandom = RandomSource.create(seed ^ hashColumn(x, z));
                BlockPos center = terrainTop(level, x, z);
                placeOuterPatch(level, pieceBox, chunkBox, origin, center, localRandom, gradient);
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

        int pitBottomY = chamberCenterY + 6;
        carvePit(level, pieceBox, chunkBox, origin, pitBottomY);
        placePitLavaSeeps(level, pieceBox, chunkBox, origin, pitBottomY, random);
        carvePrimaryChamber(level, pieceBox, chunkBox, chamberCenter, random);
        placeChamberSpikes(level, pieceBox, chunkBox, chamberCenter, random);
        placeLargeLavaLake(level, pieceBox, chunkBox, new BlockPos(origin.getX(), chamberFloorY + 1, origin.getZ()), random);
        placeChamberLavaVents(level, pieceBox, chunkBox, chamberCenter, random);

        for (int i = 0; i < 8; i++) {
            placeCeilingGlowstone(level, pieceBox, chunkBox, chamberCenter.offset(random.nextInt(17) - 8, 7, random.nextInt(17) - 8), random);
        }
        for (int i = 0; i < 7; i++) {
            placeGlowstoneStalactite(level, pieceBox, chunkBox, chamberCenter.offset(random.nextInt(15) - 7, 7, random.nextInt(15) - 7), random);
        }

        BlockPos altarCenter = chamberCenter.offset(0, -4, -7);
        List<BlockPos> deepChests = placeAltar(level, pieceBox, chunkBox, altarCenter);

        int tunnelCount = 5 + random.nextInt(3);
        List<TunnelPath> tunnels = new ArrayList<>();
        List<BlockPos> tunnelSpawners = new ArrayList<>();
        for (int i = 0; i < tunnelCount; i++) {
            double angle = ((Math.PI * 2.0) / tunnelCount) * i + random.nextDouble() * 0.55;
            int length = 42 + random.nextInt(37);
            TunnelPath tunnel = carveTunnel(level, pieceBox, chunkBox, chamberCenter, angle, length, random);
            tunnels.add(tunnel);
            tunnelSpawners.add(tunnel.end());
            if (random.nextFloat() < 0.80f) {
                TunnelPath branch = carveTunnel(level, pieceBox, chunkBox, tunnel.end(), angle + (random.nextBoolean() ? 0.85 : -0.85), 20 + random.nextInt(20), random);
                tunnelSpawners.add(branch.end());
            }
        }

        connectTunnelNetwork(level, pieceBox, chunkBox, tunnels, random);

        if (tunnelSpawners.isEmpty()) {
            tunnelSpawners.add(chamberCenter.offset(8, -5, 0));
        }
        return new UndergroundLayout(chamberCenter, chamberFloorY, altarCenter, deepChests, tunnelSpawners);
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
        BlockPos thirdSurfaceSpawner = terrainTopIfColumnInside(level, chunkBox, origin.offset(10, 0, 27));
        spawners.add(firstSurfaceSpawner == null ? null : placeConfiguredSpawner(level, pieceBox, chunkBox, firstSurfaceSpawner.above(), EntityType.ZOMBIFIED_PIGLIN, 5, 10, 55, 110, 28, 18, false));
        spawners.add(secondSurfaceSpawner == null ? null : placeConfiguredSpawner(level, pieceBox, chunkBox, secondSurfaceSpawner.above(), EntityType.ZOMBIFIED_PIGLIN, 5, 10, 60, 120, 28, 18, false));
        spawners.add(thirdSurfaceSpawner == null ? null : placeConfiguredSpawner(level, pieceBox, chunkBox, thirdSurfaceSpawner.above(), EntityType.ZOMBIFIED_PIGLIN, 4, 10, 70, 125, 28, 16, false));
        spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, layout.chamberCenter().offset(6, -5, 0), EntityType.MAGMA_CUBE, 6, 10, 45, 90, 26, 24, true));
        spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, layout.chamberCenter().offset(-7, -3, 4), EntityType.BLAZE, 3, 11, 70, 130, 28, 18, false));
        spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, layout.chamberCenter().offset(-4, -5, -8), EntityType.WITHER_SKELETON, 3, 10, 75, 140, 26, 18, false));
        List<BlockPos> tunnelSpawners = layout.tunnelSpawners();
        int placedTunnelSpawners = 0;
        for (BlockPos tunnelSpawner : tunnelSpawners) {
            if (placedTunnelSpawners >= 5) {
                break;
            }
            EntityType<?> type = switch (placedTunnelSpawners % 3) {
                case 0 -> EntityType.WITHER_SKELETON;
                case 1 -> EntityType.BLAZE;
                default -> EntityType.MAGMA_CUBE;
            };
            int spawnCount = type == EntityType.MAGMA_CUBE ? 5 : type == EntityType.BLAZE ? 2 : 3;
            int spawnRange = type == EntityType.MAGMA_CUBE ? 9 : 10;
            int maxNearby = type == EntityType.MAGMA_CUBE ? 22 : type == EntityType.BLAZE ? 16 : 18;
            int minDelay = type == EntityType.BLAZE ? 80 : type == EntityType.MAGMA_CUBE ? 55 : 70;
            int maxDelay = type == EntityType.BLAZE ? 150 : type == EntityType.MAGMA_CUBE ? 110 : 130;
            spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, tunnelSpawner.above(), type, spawnCount, spawnRange, minDelay, maxDelay, 28, maxNearby, type == EntityType.MAGMA_CUBE));
            placedTunnelSpawners++;
        }
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
        BlockState state = random.nextFloat() < 0.18f
            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
            : Blocks.OBSIDIAN.defaultBlockState();
        set(level, pieceBox, chunkBox, pos, state);
    }

    private static void placeAnchor(WorldGenLevel level, BoundingBox pieceBox, BoundingBox chunkBox, BlockPos origin) {
        set(level, pieceBox, chunkBox, origin.offset(4, 1, 0), Blocks.NETHER_BRICK_FENCE.defaultBlockState());
        set(level, pieceBox, chunkBox, origin.offset(4, 1, 1), Blocks.CRIMSON_FENCE_GATE.defaultBlockState());
    }

    private static BlockState pickLavaRimBlock(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.55f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.80f) {
            return Blocks.BASALT.defaultBlockState();
        }
        if (roll < 0.95f) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        return Blocks.MAGMA_BLOCK.defaultBlockState();
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
                    set(level, pieceBox, chunkBox, pos, pickLavaRimBlock(random));
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                }
            }
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

    private static void placeBasaltFormation(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        int pillars = 4 + random.nextInt(6);
        for (int i = 0; i < pillars; i++) {
            int dx = random.nextInt(9) - 4;
            int dz = random.nextInt(9) - 4;
            if (dx * dx + dz * dz > 20) {
                continue;
            }
            BlockPos top = terrainTopIfColumnInside(level, chunkBox, center.offset(dx, 0, dz));
            if (top == null || level.getBlockState(top).is(Blocks.LAVA)) {
                continue;
            }
            set(level, pieceBox, chunkBox, top, Blocks.NETHERRACK.defaultBlockState());
            placeBasaltColumn(level, pieceBox, chunkBox, top.above(), 2 + random.nextInt(6));
        }
    }

    private static void placeOuterPatch(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
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
                if (convertLocalWaterToLava(level, pieceBox, chunkBox, top)) {
                    continue;
                }
                setColumn(level, pieceBox, chunkBox, top, Blocks.NETHERRACK.defaultBlockState(), 2);
            }
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
            double mouthFlare = t < 0.18 ? (1.0 - t / 0.18) * 2.8 : 0.0;
            double radius = 7.1 - t * 1.5 + mouthFlare;
            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    long layerSeed = origin.asLong() ^ (long) y * 341873128712L;
                    double noise = coordinateNoise(layerSeed, origin.getX() + dx, origin.getZ() + dz) * 2.4 - 1.2;
                    double distance = Math.sqrt(dx * dx + dz * dz) + noise;
                    BlockPos pos = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    if (distance <= radius) {
                        carveNetherAir(level, pieceBox, chunkBox, pos);
                    } else if (y < topY - 2 && distance <= radius + 1.9) {
                        set(level, pieceBox, chunkBox, pos, corruptedWallBlock(pos));
                    }
                }
            }
        }
        erodePitMouth(level, pieceBox, chunkBox, origin);
    }

    private static void erodePitMouth(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin
    ) {
        for (int dx = -15; dx <= 15; dx++) {
            for (int dz = -15; dz <= 15; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                double raggedRadius = 8.8 + coordinateNoise(origin.asLong() ^ 0x4F2C1A9D77B13E5BL, origin.getX() + dx, origin.getZ() + dz) * 3.8 - 1.9;
                BlockPos pos = origin.offset(dx, 0, dz);
                if (distance <= raggedRadius - 0.9) {
                    carveNetherAir(level, pieceBox, chunkBox, pos);
                    if (coordinateNoise(origin.asLong() ^ 0x0D1B54A32F5B827CL, pos.getX(), pos.getZ()) < 0.45) {
                        carveNetherAir(level, pieceBox, chunkBox, pos.below());
                    }
                } else if (distance <= raggedRadius + 3.7) {
                    double rubble = coordinateNoise(origin.asLong() ^ 0x3C27A21E158B572FL, pos.getX(), pos.getZ());
                    if (rubble < 0.62) {
                        setColumn(level, pieceBox, chunkBox, pos, Blocks.NETHERRACK.defaultBlockState(), rubble < 0.32 ? 3 : 2);
                    }
                    if (rubble > 0.86 && distance <= raggedRadius + 1.6) {
                        carveNetherAir(level, pieceBox, chunkBox, pos.below());
                    }
                }
            }
        }
    }

    private static void placePitLavaSeeps(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int bottomY,
        RandomSource random
    ) {
        int verticalRange = Math.max(1, origin.getY() - bottomY - 7);
        for (int i = 0; i < 22; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int y = origin.getY() - 8 - random.nextInt(verticalRange);
            double depth = (origin.getY() - y) / (double) Math.max(1, origin.getY() - bottomY);
            double radius = 8.0 - depth * 2.2 + random.nextDouble() * 1.5;
            BlockPos source = new BlockPos(
                origin.getX() + (int) Math.round(Math.cos(angle) * radius),
                y,
                origin.getZ() + (int) Math.round(Math.sin(angle) * radius)
            );
            set(level, pieceBox, chunkBox, source.below(), pickLavaRimBlock(random));
            set(level, pieceBox, chunkBox, source, Blocks.LAVA.defaultBlockState());
            set(level, pieceBox, chunkBox, source.above(), Blocks.AIR.defaultBlockState());
            set(level, pieceBox, chunkBox, source.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)), pickLavaRimBlock(random));
        }
    }

    private static void carvePrimaryChamber(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        for (int dx = -16; dx <= 16; dx++) {
            for (int dy = -9; dy <= 9; dy++) {
                for (int dz = -16; dz <= 16; dz++) {
                    double normalized = (dx * dx) / 196.0 + (dy * dy) / 64.0 + (dz * dz) / 196.0;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (normalized <= 0.86) {
                        carveNetherAir(level, pieceBox, chunkBox, pos);
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
        int radiusX = 13 + random.nextInt(4);
        int radiusZ = 11 + random.nextInt(4);
        for (int dx = -radiusX - 1; dx <= radiusX + 1; dx++) {
            for (int dz = -radiusZ - 1; dz <= radiusZ + 1; dz++) {
                double normalized = (dx * dx) / (double) (radiusX * radiusX) + (dz * dz) / (double) (radiusZ * radiusZ);
                BlockPos pos = center.offset(dx, 0, dz);
                if (normalized <= 1.0) {
                    set(level, pieceBox, chunkBox, pos.below(), pickLavaRimBlock(random));
                    set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                    if (random.nextFloat() < 0.12f) {
                        placeBasaltColumn(level, pieceBox, chunkBox, pos.above(), 2 + random.nextInt(5));
                    }
                } else if (normalized <= 1.35) {
                    set(level, pieceBox, chunkBox, pos, pickLavaRimBlock(random));
                }
            }
        }
    }

    private static void placeChamberLavaVents(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos chamberCenter,
        RandomSource random
    ) {
        for (int i = 0; i < 16; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 8.0 + random.nextDouble() * 7.0;
            BlockPos vent = chamberCenter.offset(
                (int) Math.round(Math.cos(angle) * radius),
                -5 + random.nextInt(3),
                (int) Math.round(Math.sin(angle) * radius)
            );
            set(level, pieceBox, chunkBox, vent.below(), pickLavaRimBlock(random));
            set(level, pieceBox, chunkBox, vent, Blocks.LAVA.defaultBlockState());
            set(level, pieceBox, chunkBox, vent.above(), Blocks.AIR.defaultBlockState());
            if (random.nextFloat() < 0.45f) {
                set(level, pieceBox, chunkBox, vent.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)), Blocks.MAGMA_BLOCK.defaultBlockState());
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
        for (int i = 0; i < 10; i++) {
            BlockPos ceiling = chamberCenter.offset(random.nextInt(23) - 11, 7, random.nextInt(23) - 11);
            placeNetherSpike(level, pieceBox, chunkBox, ceiling, Direction.DOWN, 3 + random.nextInt(6), random);
        }
        for (int i = 0; i < 9; i++) {
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
            if (Math.abs(x - chamberCenter.getX()) > 64 || Math.abs(z - chamberCenter.getZ()) > 64) {
                break;
            }
            int y = floorY + (int) Math.round(Math.sin(step * 0.25) * 1.5);
            BlockPos center = new BlockPos(x, y, z);
            carveTunnelSegment(level, pieceBox, chunkBox, center, random);
            if (step % 8 == 4) {
                set(level, pieceBox, chunkBox, center.offset(2, -1, 0), Blocks.SOUL_TORCH.defaultBlockState());
            }
            if (step > 6 && step % 9 == 5 && random.nextFloat() < 0.96f) {
                placeTunnelLavaRun(level, pieceBox, chunkBox, center, angle, random);
            }
            if (step > 10 && step % 14 == 7 && random.nextFloat() < 0.62f) {
                placeTunnelCeilingLavaDrip(level, pieceBox, chunkBox, center, random);
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
                        carveNetherAir(level, pieceBox, chunkBox, pos);
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
        int length = 10 + random.nextInt(11);
        for (int step = 0; step < length; step++) {
            int x = start.getX() + (int) Math.round(Math.cos(angle) * step);
            int z = start.getZ() + (int) Math.round(Math.sin(angle) * step);
            BlockPos channel = new BlockPos(x, start.getY() - 1, z);
            set(level, pieceBox, chunkBox, channel.below(), pickLavaRimBlock(random));
            set(level, pieceBox, chunkBox, channel, Blocks.LAVA.defaultBlockState());
            set(level, pieceBox, chunkBox, channel.above(), Blocks.AIR.defaultBlockState());
            if (step == 0 || step == length - 1) {
                set(level, pieceBox, chunkBox, channel.offset(1, 0, 0), pickLavaRimBlock(random));
                set(level, pieceBox, chunkBox, channel.offset(-1, 0, 0), pickLavaRimBlock(random));
                set(level, pieceBox, chunkBox, channel.offset(0, 0, 1), pickLavaRimBlock(random));
                set(level, pieceBox, chunkBox, channel.offset(0, 0, -1), pickLavaRimBlock(random));
            }
        }
    }

    private static void placeTunnelCeilingLavaDrip(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        BlockPos source = center.offset(random.nextInt(5) - 2, 3, random.nextInt(5) - 2);
        set(level, pieceBox, chunkBox, source.above(), pickLavaRimBlock(random));
        set(level, pieceBox, chunkBox, source, Blocks.LAVA.defaultBlockState());
        set(level, pieceBox, chunkBox, source.below(), Blocks.AIR.defaultBlockState());
        if (random.nextBoolean()) {
            set(level, pieceBox, chunkBox, source.below(2), Blocks.AIR.defaultBlockState());
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
            if (random.nextFloat() > 0.9f) {
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
            if (roll < 0.60f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.90f) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.95f) {
                return Blocks.NETHERRACK.defaultBlockState();
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

    private static void carveNetherAir(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos pos
    ) {
        set(level, pieceBox, chunkBox, pos, Blocks.AIR.defaultBlockState());
        netherizeNeighbors(level, pieceBox, chunkBox, pos);
    }

    private static void netherizeNeighbors(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos pos
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (!pieceBox.isInside(neighbor) || !chunkBox.isInside(neighbor)) {
                continue;
            }
            BlockState state = level.getBlockState(neighbor);
            if (isOverworldGeology(state)) {
                set(level, pieceBox, chunkBox, neighbor, pickUndergroundReplacement(neighbor));
            }
        }
    }

    private static BlockState pickUndergroundReplacement(BlockPos pos) {
        double roll = coordinateNoise(((long) pos.getY() * 3129871L) ^ 0x2A75C913EF52A41BL, pos.getX(), pos.getZ());
        if (pos.getY() < 16) {
            if (roll < 0.48) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.82) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.93) {
                return Blocks.MAGMA_BLOCK.defaultBlockState();
            }
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (pos.getY() < 32) {
            if (roll < 0.34) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.64) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.78) {
                return Blocks.SOUL_SOIL.defaultBlockState();
            }
        }
        return roll < 0.82 ? Blocks.NETHERRACK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
    }

    private static boolean isOverworldGeology(BlockState state) {
        return state.is(Blocks.STONE)
            || state.is(Blocks.DEEPSLATE)
            || state.is(Blocks.DIRT)
            || state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.COARSE_DIRT)
            || state.is(Blocks.ROOTED_DIRT)
            || state.is(Blocks.MUD)
            || state.is(Blocks.CLAY)
            || state.is(Blocks.GRAVEL)
            || state.is(Blocks.SAND)
            || state.is(Blocks.RED_SAND)
            || state.is(Blocks.GRANITE)
            || state.is(Blocks.DIORITE)
            || state.is(Blocks.ANDESITE)
            || state.is(Blocks.CALCITE)
            || state.is(Blocks.TUFF)
            || state.is(Blocks.DRIPSTONE_BLOCK)
            || state.is(Blocks.COAL_ORE)
            || state.is(Blocks.DEEPSLATE_COAL_ORE)
            || state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.COPPER_ORE)
            || state.is(Blocks.DEEPSLATE_COPPER_ORE)
            || state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.REDSTONE_ORE)
            || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
            || state.is(Blocks.LAPIS_ORE)
            || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
            || state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
            || state.is(Blocks.EMERALD_ORE)
            || state.is(Blocks.DEEPSLATE_EMERALD_ORE);
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
        BlockState subsurface = underSurfaceBlock(surface);
        for (int i = 1; i < depth; i++) {
            set(level, pieceBox, chunkBox, top.below(i), subsurface);
        }
    }

    private static BlockState underSurfaceBlock(BlockState surface) {
        if (surface.is(Blocks.BLACKSTONE)
            || surface.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
            || surface.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (surface.is(Blocks.BASALT) || surface.is(Blocks.SMOOTH_BASALT)) {
            return Blocks.BASALT.defaultBlockState();
        }
        if (surface.is(Blocks.SOUL_SAND) || surface.is(Blocks.SOUL_SOIL)) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    private static boolean convertLocalWaterToLava(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos top
    ) {
        boolean foundWater = level.getFluidState(top).is(FluidTags.WATER);
        for (int i = 1; i <= 8; i++) {
            BlockPos waterPos = top.above(i);
            if (!pieceBox.isInside(waterPos) || !chunkBox.isInside(waterPos)) {
                break;
            }
            if (level.getFluidState(waterPos).is(FluidTags.WATER)) {
                foundWater = true;
                set(level, pieceBox, chunkBox, waterPos, Blocks.AIR.defaultBlockState());
            } else if (foundWater || !level.getBlockState(waterPos).isAir()) {
                break;
            }
        }

        if (!foundWater) {
            return false;
        }

        set(level, pieceBox, chunkBox, top.below(), Blocks.NETHERRACK.defaultBlockState());
        set(level, pieceBox, chunkBox, top, Blocks.LAVA.defaultBlockState());
        return true;
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
        List<BlockPos> tunnelSpawners
    ) {
    }

    private record TunnelPath(BlockPos start, BlockPos end) {
    }
}
