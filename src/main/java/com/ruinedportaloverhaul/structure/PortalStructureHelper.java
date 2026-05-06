package com.ruinedportaloverhaul.structure;

import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.entity.ModEntities;
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
    private static final double MIDDLE_BLEND_START_RADIUS = INNER_RADIUS + 12.0;
    private static final int SHALLOW_WATER_DEPTH = 3;
    private static final int CLIFF_SLOPE_THRESHOLD = 5;

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
        PortalDungeonVariant variant,
        PortalFrameSpec frame,
        RandomSource random
    ) {
        // Fix: the inner zone used to assume one flat netherrack basin, which buried the sunken variant and prevented basalt-citadel material swaps. Variant-aware terrain targeting now keeps each portal core readable while preserving the baseline layout.
        int minX = Math.max(origin.getX() - INNER_RADIUS, Math.max(pieceBox.minX(), chunkBox.minX()));
        int maxX = Math.min(origin.getX() + INNER_RADIUS, Math.min(pieceBox.maxX(), chunkBox.maxX()));
        int minZ = Math.max(origin.getZ() - INNER_RADIUS, Math.max(pieceBox.minZ(), chunkBox.minZ()));
        int maxZ = Math.min(origin.getZ() + INNER_RADIUS, Math.min(pieceBox.maxZ(), chunkBox.maxZ()));
        long terrainSeed = origin.asLong() ^ 0x2D0AB1F4D7C35E91L;
        BlockState innerSurface = variant == PortalDungeonVariant.BASALT_CITADEL
            ? Blocks.BLACKSTONE.defaultBlockState()
            : Blocks.NETHERRACK.defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > INNER_RADIUS + 0.5) {
                    continue;
                }

                BlockPos top = variant == PortalDungeonVariant.SUNKEN_SANCTUM
                    ? sunkenInnerZoneTop(level, pieceBox, chunkBox, origin, x, z, terrainSeed)
                    : terrainTop(level, x, z);
                TerrainProfile profile = terrainProfile(level, x, z);
                if (profile.hasWater()) {
                    top = retargetTerrainTop(level, pieceBox, chunkBox, x, z, profile.waterSurfaceY(), cooledSupportBlock(profile, x, z, terrainSeed));
                }
                setColumn(level, pieceBox, chunkBox, top, innerSurface, 3);
                settleColumnLiquid(level, pieceBox, chunkBox, top, profile, 1.0, terrainSeed);
                stabilizeScarColumn(level, pieceBox, chunkBox, top, profile, innerSurface, 1.0);
                set(level, pieceBox, chunkBox, top.above(), Blocks.AIR.defaultBlockState());
            }
        }

        placeRitualPlatform(level, pieceBox, chunkBox, origin, variant, random);
        placeNetheritePedestals(level, pieceBox, chunkBox, origin);
        placePortalFrame(level, pieceBox, chunkBox, origin, frame, random);
        placeAnchor(level, pieceBox, chunkBox, origin);
        if (variant == PortalDungeonVariant.BASALT_CITADEL) {
            placeBasaltCitadelColumns(level, pieceBox, chunkBox, origin, frame);
        }
    }

    public static void buildMiddleZone(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalDungeonVariant variant,
        RandomSource random
    ) {
        // Fix: the middle ring previously generated only one surface profile, so the sunken bowl and added soul-sand identity could never appear. Variant-aware shaping now preserves the original scar while letting alternate variants read differently from spawn.
        int minX = Math.max(origin.getX() - MIDDLE_RADIUS, Math.max(pieceBox.minX(), chunkBox.minX()));
        int maxX = Math.min(origin.getX() + MIDDLE_RADIUS, Math.min(pieceBox.maxX(), chunkBox.maxX()));
        int minZ = Math.max(origin.getZ() - MIDDLE_RADIUS, Math.max(pieceBox.minZ(), chunkBox.minZ()));
        int maxZ = Math.min(origin.getZ() + MIDDLE_RADIUS, Math.min(pieceBox.maxZ(), chunkBox.maxZ()));
        long terrainSeed = origin.asLong() ^ 0x6E0F4D2A51B97C3DL;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= INNER_RADIUS || distance > MIDDLE_RADIUS + 0.5) {
                    continue;
                }

                BlockPos top = variant == PortalDungeonVariant.SUNKEN_SANCTUM
                    ? sunkenMiddleZoneTop(level, pieceBox, chunkBox, origin, x, z, distance, terrainSeed)
                    : sculptedTerrainTop(level, pieceBox, chunkBox, origin, x, z, distance, terrainSeed);
                TerrainProfile profile = terrainProfile(level, x, z);
                BlockState naturalSurface = profile.naturalSurface();
                BlockState surface = pickMiddleZoneSurface(variant, origin, x, z, distance, terrainSeed, naturalSurface);
                setColumn(level, pieceBox, chunkBox, top, surface, middleZoneSurfaceDepth(surface, distance));
                settleColumnLiquid(level, pieceBox, chunkBox, top, profile, 1.0 - clamp01((distance - INNER_RADIUS) / (MIDDLE_RADIUS - INNER_RADIUS)), terrainSeed);
                stabilizeScarColumn(level, pieceBox, chunkBox, top, profile, surface, 0.85);
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

        if (variant == PortalDungeonVariant.SUNKEN_SANCTUM) {
            placeSunkenSanctumArch(level, pieceBox, chunkBox, origin);
        }
    }

    public static void buildOuterScatter(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        long seed
    ) {
        // Fix: the Cloth Config toggle existed but generation ignored it, so pack authors could not disable the far outer scatter without removing the whole structure. This gate now leaves the dungeon intact while suppressing only sparse radius-52-to-136 corruption patches.
        if (!ModConfigManager.enableOuterZoneScatter()) {
            return;
        }

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
                BlockPos center = sculptedTerrainTop(level, pieceBox, chunkBox, origin, x, z, distance, seed);
                placeOuterPatch(level, pieceBox, chunkBox, origin, center, localRandom, gradient);
            }
        }
    }

    public static UndergroundLayout buildUnderground(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalDungeonVariant variant,
        RandomSource random
    ) {
        // Fix: underground generation previously had no hook for variant-specific pit hazards, so the basalt citadel could not add its moat without forking the whole cave system. The shared layout now accepts variant-only pit adornments and keeps the favored pit/carver logic intact.
        int chamberCenterY = Math.max(level.getMinY() + 14, origin.getY() - 33);
        int chamberFloorY = chamberCenterY - 6;
        BlockPos chamberCenter = new BlockPos(origin.getX(), chamberCenterY, origin.getZ());

        int pitBottomY = chamberCenterY + 6;
        carvePit(level, pieceBox, chunkBox, origin, pitBottomY);
        placePitLavaSeeps(level, pieceBox, chunkBox, origin, pitBottomY, random);
        if (variant == PortalDungeonVariant.BASALT_CITADEL) {
            placePitLavaMoat(level, pieceBox, chunkBox, origin, random);
        }
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

        List<TunnelPath> tunnels = new ArrayList<>();
        List<BlockPos> tunnelSpawners = new ArrayList<>();

        List<CaveNode> caveNodes = createCaveNodes(level, origin, chamberCenter, random);
        for (int i = 0; i < caveNodes.size(); i++) {
            CaveNode node = caveNodes.get(i);
            carveCaveNode(level, pieceBox, chunkBox, node, random);
            tunnelSpawners.add(node.center());
            if (i % 2 == 0 || node.band() >= 1 || random.nextFloat() < 0.35f) {
                BlockPos cache = caveCachePosition(node, random);
                prepareCaveCache(level, pieceBox, chunkBox, cache, node.band(), random);
                deepChests.add(cache);
            }

            double angleFromPit = Math.atan2(node.center().getZ() - origin.getZ(), node.center().getX() - origin.getX());
            if (i < 5) {
                BlockPos mouth = origin.offset(
                    (int) Math.round(Math.cos(angleFromPit) * (7.0 + random.nextDouble() * 3.0)),
                    -13 - random.nextInt(10),
                    (int) Math.round(Math.sin(angleFromPit) * (7.0 + random.nextDouble() * 3.0))
                );
                TunnelPath shaft = carveOrganicTunnel(level, pieceBox, chunkBox, mouth, node.center(), random);
                tunnels.add(shaft);
                tunnelSpawners.add(midpoint(shaft.start(), shaft.end()));
            } else {
                TunnelPath tunnel = carveOrganicTunnel(level, pieceBox, chunkBox, chamberCenter, node.center(), random);
                tunnels.add(tunnel);
                tunnelSpawners.add(midpoint(tunnel.start(), tunnel.end()));
            }

            if (random.nextFloat() < 0.88f) {
                BlockPos branchEnd = sideBranchEnd(node, origin, random);
                TunnelPath branch = carveOrganicTunnel(level, pieceBox, chunkBox, node.center(), branchEnd, random);
                carveSidePocket(level, pieceBox, chunkBox, branch.end(), node.band(), random);
                tunnels.add(branch);
                tunnelSpawners.add(branch.end());
            }
        }

        connectCaveGraph(level, pieceBox, chunkBox, caveNodes, tunnels, random);
        placeMasterworkRooms(level, pieceBox, chunkBox, origin, caveNodes, deepChests, tunnelSpawners, random);

        if (tunnelSpawners.isEmpty()) {
            tunnelSpawners.add(chamberCenter.offset(8, -5, 0));
        }
        return new UndergroundLayout(chamberCenter, chamberFloorY, altarCenter, deepChests, tunnelSpawners);
    }

    private static List<CaveNode> createCaveNodes(
        WorldGenLevel level,
        BlockPos origin,
        BlockPos chamberCenter,
        RandomSource random
    ) {
        int nodeCount = 14 + random.nextInt(5);
        List<CaveNode> nodes = new ArrayList<>(nodeCount + 6);
        for (int i = 0; i < nodeCount; i++) {
            int band = i % 3;
            int y = switch (band) {
                case 0 -> origin.getY() - 18 - random.nextInt(9);
                case 1 -> origin.getY() - 30 - random.nextInt(9);
                default -> origin.getY() - 42 - random.nextInt(14);
            };
            y = Math.max(level.getMinY() + 8, y);
            double angle = ((Math.PI * 2.0) / nodeCount) * i + random.nextDouble() * 0.75 - 0.35;
            double radius = switch (band) {
                case 0 -> 24.0 + random.nextDouble() * 34.0;
                case 1 -> 32.0 + random.nextDouble() * 40.0;
                default -> 38.0 + random.nextDouble() * 46.0;
            };
            BlockPos center = new BlockPos(
                origin.getX() + (int) Math.round(Math.cos(angle) * radius),
                y,
                origin.getZ() + (int) Math.round(Math.sin(angle) * radius)
            );
            int caveRadius = switch (band) {
                case 0 -> 5 + random.nextInt(4);
                case 1 -> 7 + random.nextInt(5);
                default -> 11 + random.nextInt(7);
            };
            int caveHeight = switch (band) {
                case 0 -> 3 + random.nextInt(3);
                case 1 -> 4 + random.nextInt(4);
                default -> 7 + random.nextInt(6);
            };
            nodes.add(new CaveNode(center, caveRadius, caveHeight, band));
        }
        nodes.add(new CaveNode(chamberCenter.offset(0, -10, 18), 9, 6, 1));
        nodes.add(new CaveNode(chamberCenter.offset(-15, -18, -12), 14, 10, 2));
        nodes.add(new CaveNode(chamberCenter.offset(23, -20, -16), 16, 11, 2));
        nodes.add(new CaveNode(chamberCenter.offset(4, -25, 28), 18, 12, 2));
        nodes.add(new CaveNode(chamberCenter.offset(-30, -24, 22), 15, 11, 2));
        nodes.add(new CaveNode(chamberCenter.offset(32, -14, 18), 12, 8, 1));
        return nodes;
    }

    private static void connectCaveGraph(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        List<CaveNode> caveNodes,
        List<TunnelPath> tunnels,
        RandomSource random
    ) {
        for (int i = 0; i < caveNodes.size(); i++) {
            CaveNode current = caveNodes.get(i);
            CaveNode next = caveNodes.get((i + 1) % caveNodes.size());
            if (random.nextFloat() < 0.90f) {
                tunnels.add(carveOrganicTunnel(level, pieceBox, chunkBox, current.center(), next.center(), random));
            }
            if (i + 3 < caveNodes.size() && random.nextFloat() < 0.46f) {
                CaveNode cross = caveNodes.get(i + 3);
                tunnels.add(carveOrganicTunnel(level, pieceBox, chunkBox, current.center(), cross.center(), random));
            }
        }
    }

    private static BlockPos caveCachePosition(CaveNode node, RandomSource random) {
        int usableRadius = Math.max(2, node.radius() - 2);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 1.5 + random.nextDouble() * usableRadius;
        return node.center().offset(
            (int) Math.round(Math.cos(angle) * distance),
            -node.height() + 1,
            (int) Math.round(Math.sin(angle) * distance)
        );
    }

    private static void prepareCaveCache(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos chestPos,
        int band,
        RandomSource random
    ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = chestPos.offset(dx, -1, dz);
                set(level, pieceBox, chunkBox, floor, Math.abs(dx) + Math.abs(dz) <= 1
                    ? StructureBlockPalette.ritualFloor(random)
                    : StructureBlockPalette.deepWall(random));
            }
        }
        set(level, pieceBox, chunkBox, chestPos, Blocks.AIR.defaultBlockState());
        set(level, pieceBox, chunkBox, chestPos.above(), Blocks.AIR.defaultBlockState());

        if (random.nextBoolean()) {
            set(level, pieceBox, chunkBox, chestPos.offset(2, 1, 0), Blocks.GLOWSTONE.defaultBlockState());
        } else {
            set(level, pieceBox, chunkBox, chestPos.offset(0, 1, 2), Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    private static BlockPos sideBranchEnd(CaveNode node, BlockPos origin, RandomSource random) {
        double fromOrigin = Math.atan2(node.center().getZ() - origin.getZ(), node.center().getX() - origin.getX());
        double angle = fromOrigin + (random.nextBoolean() ? 1.25 : -1.25) + (random.nextDouble() - 0.5) * 0.65;
        double length = 14.0 + random.nextDouble() * 24.0;
        int yDrift = switch (node.band()) {
            case 0 -> -3 + random.nextInt(6);
            case 1 -> -5 + random.nextInt(9);
            default -> -7 + random.nextInt(11);
        };
        return node.center().offset(
            (int) Math.round(Math.cos(angle) * length),
            yDrift,
            (int) Math.round(Math.sin(angle) * length)
        );
    }

    private static BlockPos midpoint(BlockPos first, BlockPos second) {
        return new BlockPos(
            (first.getX() + second.getX()) / 2,
            (first.getY() + second.getY()) / 2,
            (first.getZ() + second.getZ()) / 2
        );
    }

    private static void carveCaveNode(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        CaveNode node,
        RandomSource random
    ) {
        int radius = node.radius();
        int height = node.height();
        for (int dx = -radius - 2; dx <= radius + 2; dx++) {
            for (int dy = -height - 2; dy <= height + 2; dy++) {
                for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                    double horizontal = (dx * dx + dz * dz) / (double) (radius * radius);
                    double vertical = (dy * dy) / (double) Math.max(1, height * height);
                    double noise = coordinateNoise(node.center().asLong() ^ (long) dy * 1140071481932319845L, node.center().getX() + dx, node.center().getZ() + dz) * 0.30;
                    double normalized = horizontal + vertical + noise;
                    BlockPos pos = node.center().offset(dx, dy, dz);
                    if (normalized <= 0.92) {
                        carveNetherAir(level, pieceBox, chunkBox, pos);
                    } else if (normalized <= 1.24) {
                        set(level, pieceBox, chunkBox, pos, pickCaveWall(pos, node.band(), random));
                    }
                    if (dy <= -height + 1 && horizontal <= 1.08) {
                        set(level, pieceBox, chunkBox, pos, pickCaveFloor(node.band(), random));
                    }
                }
            }
        }

        placeCaveHazards(level, pieceBox, chunkBox, node, random);
        placeCaveLavaPool(level, pieceBox, chunkBox, node, random);
        placeCaveBasaltRibs(level, pieceBox, chunkBox, node, random);
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            placeCeilingGlowstone(level, pieceBox, chunkBox, node.center().offset(random.nextInt(radius * 2 + 1) - radius, height, random.nextInt(radius * 2 + 1) - radius), random);
        }
    }

    private static void carveSidePocket(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        int band,
        RandomSource random
    ) {
        carveCaveNode(level, pieceBox, chunkBox, new CaveNode(center, 4 + random.nextInt(4), 3 + random.nextInt(3), band), random);
    }

    private static void placeCaveHazards(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        CaveNode node,
        RandomSource random
    ) {
        for (int i = 0; i < 12 + node.radius(); i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = random.nextDouble() * Math.max(2, node.radius() - 1);
            BlockPos floor = node.center().offset(
                (int) Math.round(Math.cos(angle) * distance),
                -node.height(),
                (int) Math.round(Math.sin(angle) * distance)
            );
            BlockState state = random.nextFloat() < 0.42f
                ? Blocks.SOUL_SAND.defaultBlockState()
                : random.nextFloat() < 0.70f
                    ? Blocks.SOUL_SOIL.defaultBlockState()
                    : Blocks.MAGMA_BLOCK.defaultBlockState();
            set(level, pieceBox, chunkBox, floor, state);
        }
    }

    private static void placeCaveLavaPool(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        CaveNode node,
        RandomSource random
    ) {
        if (node.band() == 0 && random.nextFloat() < 0.55f) {
            return;
        }
        int radius = 2 + random.nextInt(node.band() == 2 ? 4 : 3);
        BlockPos center = node.center().offset(random.nextInt(5) - 2, -node.height(), random.nextInt(5) - 2);
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double normalized = (dx * dx + dz * dz) / (double) (radius * radius);
                BlockPos pos = center.offset(dx, 0, dz);
                if (normalized <= 1.0) {
                    set(level, pieceBox, chunkBox, pos.below(), pickLavaRimBlock(random));
                    set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                    set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
                } else if (normalized <= 1.45) {
                    set(level, pieceBox, chunkBox, pos, pickLavaRimBlock(random));
                }
            }
        }
    }

    private static void placeCaveBasaltRibs(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        CaveNode node,
        RandomSource random
    ) {
        int ribs = 3 + random.nextInt(4);
        for (int i = 0; i < ribs; i++) {
            double angle = ((Math.PI * 2.0) / ribs) * i + random.nextDouble() * 0.6;
            int x = (int) Math.round(Math.cos(angle) * (node.radius() - 1));
            int z = (int) Math.round(Math.sin(angle) * (node.radius() - 1));
            BlockPos base = node.center().offset(x, -node.height(), z);
            placeBasaltColumn(level, pieceBox, chunkBox, base, node.height() + random.nextInt(node.height() + 3));
        }
    }

    private static TunnelPath carveOrganicTunnel(
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
        double distance = Math.max(1.0, Math.sqrt(dx * dx + dy * dy + dz * dz));
        int steps = Math.max(14, (int) Math.ceil(distance * 1.22));
        long pathSeed = start.asLong() ^ Long.rotateLeft(end.asLong(), 17) ^ random.nextLong();
        double x = start.getX();
        double y = start.getY();
        double z = start.getZ();
        double dirX = dx / distance;
        double dirY = dy / distance;
        double dirZ = dz / distance;
        BlockPos last = start;
        double angle = Math.atan2(dz, dx);
        for (int step = 0; step <= steps; step++) {
            double t = step / (double) steps;
            double goalX = end.getX() - x;
            double goalY = end.getY() - y;
            double goalZ = end.getZ() - z;
            double goalDistance = Math.max(1.0, Math.sqrt(goalX * goalX + goalY * goalY + goalZ * goalZ));
            double noiseTime = step * 0.16;
            double noiseX = smoothNoise(pathSeed ^ 0x4D2C1A77B9E33F0AL, noiseTime, 0.0) * 2.0 - 1.0;
            double noiseY = (smoothNoise(pathSeed ^ 0x6A5F41C93B2D19E7L, noiseTime, 4.0) - 0.5) * 0.52;
            double noiseZ = smoothNoise(pathSeed ^ 0x19D36EF4A7B25C81L, noiseTime, 8.0) * 2.0 - 1.0;
            double finishPull = t > 0.75 ? (t - 0.75) / 0.25 : 0.0;
            double goalWeight = Math.min(0.96, 0.28 + t * 0.48 + finishPull * 0.32);
            double targetX = goalX / goalDistance * goalWeight + noiseX * (1.0 - goalWeight);
            double targetY = goalY / goalDistance * goalWeight + noiseY * (1.0 - goalWeight);
            double targetZ = goalZ / goalDistance * goalWeight + noiseZ * (1.0 - goalWeight);
            double targetLength = Math.max(0.001, Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ));
            targetX /= targetLength;
            targetY /= targetLength;
            targetZ /= targetLength;

            dirX = dirX * 0.82 + targetX * 0.18;
            dirY = dirY * 0.88 + targetY * 0.12;
            dirZ = dirZ * 0.82 + targetZ * 0.18;
            double dirLength = Math.max(0.001, Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ));
            dirX /= dirLength;
            dirY /= dirLength;
            dirZ /= dirLength;

            if (step > 0) {
                x += dirX * 1.15;
                y += dirY * 0.95;
                z += dirZ * 1.15;
            }

            double radiusNoise = smoothNoise(pathSeed ^ 0x52C7B3D9A1164E2FL, step * 0.13, 12.0);
            int radius = clamp((int) Math.round(2.0 + Math.sin(t * Math.PI) * 1.7 + radiusNoise * 3.0), 2, 6);
            if (step % 19 == 7 && radius < 6) {
                radius++;
            }
            BlockPos center = new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
            int shallowReferenceY = Math.max(start.getY(), end.getY());
            int depthBand = center.getY() <= shallowReferenceY - 18 || shallowReferenceY < 24 ? 2 : 1;
            int verticalRadius = clamp(1 + radius / 2 + (radiusNoise > 0.72 ? 1 : 0), 2, 5);
            carveTunnelSegment(level, pieceBox, chunkBox, center, radius, verticalRadius, depthBand, random);
            if (step > 6 && step % 16 == 8 && random.nextFloat() < 0.58f) {
                double sideX = -dirZ;
                double sideZ = dirX;
                BlockPos pocketCenter = center.offset(
                    (int) Math.round(sideX * (radius + 1.0)),
                    random.nextInt(3) - 1,
                    (int) Math.round(sideZ * (radius + 1.0))
                );
                carveCaveNode(
                    level,
                    pieceBox,
                    chunkBox,
                    new CaveNode(pocketCenter, 4 + random.nextInt(4), 3 + random.nextInt(3), pocketCenter.getY() <= start.getY() - 12 ? 2 : 1),
                    random
                );
            }
            if (step % 9 == 4) {
                set(level, pieceBox, chunkBox, center.offset(2, -1, 0), Blocks.SOUL_TORCH.defaultBlockState());
            }
            angle = Math.atan2(dirZ, dirX);
            if (step > 5 && step % 11 == 6 && random.nextFloat() < (depthBand == 2 ? 0.82f : 0.56f)) {
                placeTunnelLavaRun(level, pieceBox, chunkBox, center, angle, random);
            }
            if (step > 8 && step % 13 == 7 && random.nextFloat() < (depthBand == 2 ? 0.68f : 0.42f)) {
                placeTunnelCeilingLavaDrip(level, pieceBox, chunkBox, center, random);
            }
            last = center;
        }
        if (last.distSqr(end) > 16.0) {
            carveConnectorTunnel(level, pieceBox, chunkBox, last, end, random);
        } else {
            int endBand = end.getY() <= Math.max(start.getY(), end.getY()) - 18 || Math.max(start.getY(), end.getY()) < 24 ? 2 : 1;
            carveTunnelSegment(level, pieceBox, chunkBox, end, 3, 2, endBand, random);
        }
        return new TunnelPath(start, end);
    }

    private static BlockState pickCaveWall(BlockPos pos, int band, RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.04f) {
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        }
        if (roll < 0.08f) {
            return Blocks.NETHER_GOLD_ORE.defaultBlockState();
        }
        if (band == 2) {
            if (roll < 0.42f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.68f) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.78f) {
                return Blocks.MAGMA_BLOCK.defaultBlockState();
            }
        } else if (band == 1) {
            if (roll < 0.30f) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.44f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.54f) {
                return Blocks.SOUL_SOIL.defaultBlockState();
            }
        } else if (roll < 0.24f) {
            return Blocks.BASALT.defaultBlockState();
        }
        return corruptedWallBlock(pos);
    }

    private static BlockState pickCaveFloor(int band, RandomSource random) {
        float roll = random.nextFloat();
        if (band == 2 && roll < 0.28f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.20f) {
            return Blocks.SOUL_SAND.defaultBlockState();
        }
        if (roll < 0.38f) {
            return Blocks.SOUL_SOIL.defaultBlockState();
        }
        if (roll < 0.50f) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (roll < 0.68f) {
            return Blocks.BASALT.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    public static List<BlockPos> placePortalSpawners(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        UndergroundLayout layout
    ) {
        List<BlockPos> spawners = new ArrayList<>();
        EntityType<?>[] surfaceTypes = {
            EntityType.ZOMBIFIED_PIGLIN,
            ModEntities.PIGLIN_PILLAGER,
            EntityType.MAGMA_CUBE,
            ModEntities.PIGLIN_VINDICATOR,
            EntityType.ZOMBIFIED_PIGLIN,
            ModEntities.PIGLIN_PILLAGER,
            EntityType.MAGMA_CUBE,
            ModEntities.PIGLIN_VINDICATOR,
            EntityType.BLAZE,
            EntityType.ZOMBIFIED_PIGLIN
        };
        for (int i = 0; i < surfaceTypes.length; i++) {
            double angle = ((Math.PI * 2.0) / surfaceTypes.length) * i + 0.18;
            int radius = 21 + (i % 3) * 7;
            BlockPos surface = terrainTopIfColumnInside(level, pieceBox, chunkBox, origin.offset(
                (int) Math.round(Math.cos(angle) * radius),
                0,
                (int) Math.round(Math.sin(angle) * radius)
            ));
            if (surface == null) {
                continue;
            }
            EntityType<?> type = surfaceTypes[i];
            spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, surface.above(), type, type == EntityType.MAGMA_CUBE ? 8 : 5, 13, 25, 65, 34, type == EntityType.MAGMA_CUBE ? 34 : 28, type == EntityType.MAGMA_CUBE));
        }

        EntityType<?>[] chamberTypes = {
            EntityType.MAGMA_CUBE,
            EntityType.BLAZE,
            EntityType.WITHER_SKELETON,
            ModEntities.PIGLIN_BRUTE_PILLAGER,
            ModEntities.PIGLIN_PILLAGER,
            ModEntities.PIGLIN_VINDICATOR,
            EntityType.BLAZE,
            EntityType.WITHER_SKELETON
        };
        BlockPos[] chamberOffsets = {
            layout.chamberCenter().offset(6, -5, 0),
            layout.chamberCenter().offset(-7, -3, 4),
            layout.chamberCenter().offset(-4, -5, -8),
            layout.chamberCenter().offset(9, -4, 7),
            layout.chamberCenter().offset(-10, -4, -2),
            layout.chamberCenter().offset(2, -5, 10),
            layout.chamberCenter().offset(12, -2, -8),
            layout.chamberCenter().offset(-12, -5, 9)
        };
        for (int i = 0; i < chamberTypes.length; i++) {
            EntityType<?> type = chamberTypes[i];
            spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, chamberOffsets[i], type, type == EntityType.MAGMA_CUBE ? 8 : 5, 13, 24, 62, 32, type == EntityType.BLAZE ? 26 : 30, type == EntityType.MAGMA_CUBE));
        }

        List<BlockPos> tunnelSpawners = layout.tunnelSpawners();
        int placedTunnelSpawners = 0;
        for (BlockPos tunnelSpawner : tunnelSpawners) {
            if (placedTunnelSpawners >= 28) {
                break;
            }
            boolean lowerCave = tunnelSpawner.getY() <= origin.getY() - 30;
            boolean deepCave = tunnelSpawner.getY() <= origin.getY() - 42;
            EntityType<?> type = deepCave
                ? switch (placedTunnelSpawners % 9) {
                    case 0 -> EntityType.GHAST;
                    case 1 -> ModEntities.PIGLIN_BRUTE_PILLAGER;
                    case 2 -> EntityType.BLAZE;
                    case 3 -> EntityType.WITHER_SKELETON;
                    case 4 -> EntityType.MAGMA_CUBE;
                    case 5 -> ModEntities.PIGLIN_ILLUSIONER;
                    case 6 -> ModEntities.PIGLIN_BRUTE_PILLAGER;
                    case 7 -> ModEntities.PIGLIN_EVOKER;
                    default -> EntityType.BLAZE;
                }
                : lowerCave
                    ? switch (placedTunnelSpawners % 7) {
                        case 0 -> EntityType.BLAZE;
                        case 1 -> EntityType.WITHER_SKELETON;
                        case 2 -> EntityType.MAGMA_CUBE;
                        case 3 -> ModEntities.PIGLIN_BRUTE_PILLAGER;
                        case 4 -> ModEntities.PIGLIN_ILLUSIONER;
                        case 5 -> ModEntities.PIGLIN_VINDICATOR;
                        default -> EntityType.BLAZE;
                    }
                    : switch (placedTunnelSpawners % 6) {
                        case 0 -> EntityType.MAGMA_CUBE;
                        case 1 -> ModEntities.PIGLIN_PILLAGER;
                        case 2 -> ModEntities.PIGLIN_VINDICATOR;
                        case 3 -> EntityType.BLAZE;
                        case 4 -> EntityType.WITHER_SKELETON;
                        default -> EntityType.MAGMA_CUBE;
                    };
            int spawnCount = tunnelSpawnCount(type, deepCave);
            int spawnRange = type == EntityType.GHAST ? 18 : type == EntityType.MAGMA_CUBE ? 12 : deepCave ? 16 : 14;
            int maxNearby = type == EntityType.GHAST ? 8 : type == EntityType.MAGMA_CUBE ? 38 : type == EntityType.BLAZE ? 30 : deepCave ? 36 : 32;
            int minDelay = type == EntityType.GHAST ? 45 : type == EntityType.BLAZE ? 26 : type == EntityType.MAGMA_CUBE ? 22 : deepCave ? 28 : 30;
            int maxDelay = type == EntityType.GHAST ? 105 : type == EntityType.BLAZE ? 68 : type == EntityType.MAGMA_CUBE ? 62 : deepCave ? 74 : 78;
            int requiredPlayerRange = type == EntityType.GHAST ? 38 : deepCave ? 36 : lowerCave ? 35 : 34;
            BlockPos spawnerPos = type == EntityType.GHAST ? tunnelSpawner.above(2) : tunnelSpawner.above();
            spawners.add(placeConfiguredSpawner(level, pieceBox, chunkBox, spawnerPos, type, spawnCount, spawnRange, minDelay, maxDelay, requiredPlayerRange, maxNearby, type == EntityType.MAGMA_CUBE));
            placedTunnelSpawners++;
        }
        spawners.removeIf(pos -> pos == null);
        return spawners;
    }

    private static int tunnelSpawnCount(EntityType<?> type, boolean deepCave) {
        if (type == EntityType.GHAST) {
            return 1;
        }
        if (type == EntityType.MAGMA_CUBE) {
            return 8;
        }
        if (type == EntityType.BLAZE) {
            return deepCave ? 5 : 4;
        }
        if (type == ModEntities.PIGLIN_EVOKER) {
            return 2;
        }
        if (type == ModEntities.PIGLIN_ILLUSIONER) {
            return 4;
        }
        return deepCave ? 6 : 5;
    }

    public static BlockPos terrainTop(WorldGenLevel level, BlockPos pos) {
        return terrainTop(level, pos.getX(), pos.getZ());
    }

    public static BlockPos terrainTopIfColumnInside(WorldGenLevel level, BoundingBox pieceBox, BoundingBox chunkBox, BlockPos pos) {
        return isColumnInside(pieceBox, chunkBox, pos) ? terrainTop(level, pos) : null;
    }

    public static boolean isColumnInside(BoundingBox box, BlockPos pos) {
        return pos.getX() >= box.minX()
            && pos.getX() <= box.maxX()
            && pos.getZ() >= box.minZ()
            && pos.getZ() <= box.maxZ();
    }

    public static boolean isColumnInside(BoundingBox pieceBox, BoundingBox chunkBox, BlockPos pos) {
        return isColumnInside(pieceBox, pos) && isColumnInside(chunkBox, pos);
    }

    public static BlockPos surfaceCacheTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos target,
        long seed
    ) {
        if (!isColumnInside(pieceBox, chunkBox, target)) {
            return null;
        }

        TerrainProfile profile = terrainProfile(level, target.getX(), target.getZ());
        BlockPos top = profile.floorTop();
        if (profile.hasWater()) {
            top = retargetTerrainTop(level, pieceBox, chunkBox, target.getX(), target.getZ(), profile.waterSurfaceY(), cooledSupportBlock(profile, target.getX(), target.getZ(), seed));
            settleColumnLiquid(level, pieceBox, chunkBox, top, profile, 0.65, seed);
        }
        stabilizeScarColumn(level, pieceBox, chunkBox, top, profile, Blocks.BLACKSTONE.defaultBlockState(), 0.55);
        return top;
    }

    public static List<BlockPos> ritualPedestalPositions(BlockPos origin) {
        return List.of(
            origin.offset(0, 0, -6),
            origin.offset(0, 0, 6),
            origin.offset(6, 0, 0),
            origin.offset(-6, 0, 0)
        );
    }

    public static BlockPos terrainTop(WorldGenLevel level, int x, int z) {
        return terrainProfile(level, x, z).floorTop();
    }

    private static BlockPos sculptedTerrainTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int x,
        int z,
        double distance,
        long seed
    ) {
        TerrainProfile profile = terrainProfile(level, x, z);
        BlockPos naturalTop = profile.floorTop();
        if (profile.hasWater()) {
            int waterTarget = profile.waterDepth() <= SHALLOW_WATER_DEPTH
                ? profile.waterSurfaceY() - 1
                : profile.waterSurfaceY();
            return retargetTerrainTop(level, pieceBox, chunkBox, x, z, waterTarget, cooledSupportBlock(profile, x, z, seed));
        }
        if (distance <= INNER_RADIUS + 2.5) {
            return naturalTop;
        }

        double blend = Math.min(1.0, Math.max(0.0, (distance - INNER_RADIUS - 2.5) / 12.0));
        int offset = (int) Math.round(surfaceHeightOffset(seed, x, z) * blend);
        if (offset == 0) {
            return naturalTop;
        }

        int targetY = naturalTop.getY() + offset;
        return retargetTerrainTop(level, pieceBox, chunkBox, x, z, targetY, Blocks.NETHERRACK.defaultBlockState());
    }

    private static int surfaceHeightOffset(long seed, int x, int z) {
        double macroNoise = smoothNoise(seed ^ 0x3D9A27F14C8B55E1L, x / 18.0, z / 18.0) - 0.5;
        double localNoise = smoothNoise(seed ^ 0xA5170E47C19D9E33L, (x + 5) / 9.0, (z - 7) / 9.0) - 0.5;
        double microNoise = coordinateNoise(seed ^ 0x71D42B6F9E3518C2L, Math.floorDiv(x, 3), Math.floorDiv(z, 3)) - 0.5;
        int offset = (int) Math.round(macroNoise * 3.0 + localNoise * 2.0 + microNoise);
        double ledgeRoll = coordinateNoise(seed ^ 0x1F7E2D963AF4B105L, Math.floorDiv(x, 12), Math.floorDiv(z, 12));
        if (ledgeRoll < 0.055) {
            offset -= 1;
        } else if (ledgeRoll > 0.955) {
            offset += 1;
        }
        return clamp(offset, -3, 3);
    }

    private static BlockPos retargetTerrainTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        int x,
        int z,
        int requestedTargetY,
        BlockState raisedFill
    ) {
        BlockPos naturalTop = terrainTop(level, x, z);
        int targetY = clamp(requestedTargetY, pieceBox.minY() + 2, pieceBox.maxY() - 2);
        if (targetY > naturalTop.getY()) {
            for (int y = naturalTop.getY() + 1; y <= targetY; y++) {
                set(level, pieceBox, chunkBox, new BlockPos(x, y, z), raisedFill);
            }
        } else if (targetY < naturalTop.getY()) {
            for (int y = targetY + 1; y <= naturalTop.getY() + 1; y++) {
                set(level, pieceBox, chunkBox, new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
            }
        }
        return new BlockPos(x, targetY, z);
    }

    private static BlockPos sunkenInnerZoneTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int x,
        int z,
        long seed
    ) {
        int offset = clamp((int) Math.round(surfaceHeightOffset(seed, x, z) * 0.35), -1, 1);
        return retargetTerrainTop(level, pieceBox, chunkBox, x, z, origin.getY() + offset, Blocks.NETHERRACK.defaultBlockState());
    }

    private static BlockPos sunkenMiddleZoneTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int x,
        int z,
        double distance,
        long seed
    ) {
        TerrainProfile profile = terrainProfile(level, x, z);
        BlockPos naturalTop = profile.floorTop();
        if (profile.hasWater()) {
            return retargetTerrainTop(level, pieceBox, chunkBox, x, z, profile.waterSurfaceY(), cooledSupportBlock(profile, x, z, seed));
        }
        double normalized = clamp01((distance - INNER_RADIUS) / (MIDDLE_RADIUS - INNER_RADIUS));
        double bowlStrength = 1.0 - normalized;
        int bowlDepth = (int) Math.round(4.0 * bowlStrength * bowlStrength);
        int shaping = clamp((int) Math.round(surfaceHeightOffset(seed, x, z) * Math.max(0.15, normalized * 0.45)), -2, 2);
        int targetY = naturalTop.getY() - bowlDepth + shaping;
        return retargetTerrainTop(level, pieceBox, chunkBox, x, z, Math.min(targetY, naturalTop.getY()), Blocks.NETHERRACK.defaultBlockState());
    }

    private static BlockState pickMiddleZoneSurface(
        PortalDungeonVariant variant,
        BlockPos origin,
        int x,
        int z,
        double distance,
        long seed,
        BlockState naturalSurface
    ) {
        // Fix: the middle scar had collapsed back to one mostly netherrack material, erasing the intended north/south/east/west navigation language. The selector now restores cardinal material sectors while keeping netherrack as the common fallback and giving Sunken Sanctum its extra soul-sand weight.
        double edgeBlend = smoothStep(clamp01((distance - MIDDLE_BLEND_START_RADIUS) / (MIDDLE_RADIUS - MIDDLE_BLEND_START_RADIUS)));
        double nativeRoll = coordinateNoise(seed ^ 0x7312C844A51E9F2BL, Math.floorDiv(x, 4), Math.floorDiv(z, 4));
        if (nativeRoll < edgeBlend * 0.82) {
            return pickScorchedNativeSurface(naturalSurface, x, z, seed, edgeBlend);
        }

        int dx = x - origin.getX();
        int dz = z - origin.getZ();
        double materialRoll = coordinateNoise(seed ^ 0x0B6E29C14AF93D57L, Math.floorDiv(x, 3), Math.floorDiv(z, 3));
        double detailRoll = coordinateNoise(seed ^ 0x5C92E21D6F3A48B7L, x, z);
        boolean northOrSouth = Math.abs(dz) >= Math.abs(dx);
        boolean sunkenSoulBoost = variant == PortalDungeonVariant.SUNKEN_SANCTUM && (distance < INNER_RADIUS + 10.0 || materialRoll < 0.22);

        if (sunkenSoulBoost) {
            return detailRoll < 0.45 ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
        }
        if (northOrSouth && dz < 0) {
            if (materialRoll < 0.52) {
                return detailRoll < 0.58 ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState();
            }
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (northOrSouth) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (dx > 0) {
            if (materialRoll < 0.56) {
                return detailRoll < 0.75 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            }
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (materialRoll < 0.48) {
            return Blocks.CRIMSON_NYLIUM.defaultBlockState();
        }
        return Blocks.NETHERRACK.defaultBlockState();
    }

    private static int middleZoneSurfaceDepth(BlockState surface, double distance) {
        if (!isNetherScarSurface(surface)) {
            return 1;
        }
        return distance < MIDDLE_RADIUS * 0.65 ? 3 : 2;
    }

    private static BlockState pickScorchedNativeSurface(BlockState naturalSurface, int x, int z, long seed, double edgeBlend) {
        double roll = coordinateNoise(seed ^ 0x17C02C9538AF602DL, x, z);
        if (naturalSurface.is(Blocks.SAND) || naturalSurface.is(Blocks.RED_SAND)) {
            return roll < edgeBlend * 0.22 ? Blocks.GRAVEL.defaultBlockState() : naturalSurface;
        }
        if (naturalSurface.is(Blocks.GRAVEL) || naturalSurface.is(Blocks.CLAY) || naturalSurface.is(Blocks.MUD)) {
            return roll < 0.18 ? Blocks.SOUL_SOIL.defaultBlockState() : naturalSurface;
        }
        if (naturalSurface.is(Blocks.STONE) || naturalSurface.is(Blocks.ANDESITE) || naturalSurface.is(Blocks.DIORITE) || naturalSurface.is(Blocks.GRANITE)) {
            return roll < 0.24 ? Blocks.BLACKSTONE.defaultBlockState() : naturalSurface;
        }
        if (naturalSurface.is(Blocks.SNOW_BLOCK)) {
            return roll < 0.36 ? Blocks.GRAVEL.defaultBlockState() : naturalSurface;
        }
        if (naturalSurface.is(Blocks.GRASS_BLOCK) || naturalSurface.is(Blocks.DIRT) || naturalSurface.is(Blocks.PODZOL) || naturalSurface.is(Blocks.MYCELIUM)) {
            if (roll < 0.18) {
                return Blocks.SOUL_SOIL.defaultBlockState();
            }
            return roll < 0.72 ? Blocks.COARSE_DIRT.defaultBlockState() : naturalSurface;
        }
        return roll < 0.28 ? Blocks.COARSE_DIRT.defaultBlockState() : naturalSurface;
    }

    private static BlockState pickOuterBlendSurface(
        BlockPos origin,
        BlockPos column,
        BlockState naturalSurface,
        RandomSource random,
        double gradient
    ) {
        double artifactRoll = coordinateNoise(origin.asLong() ^ 0x697AD8C1021ECF31L, column.getX(), column.getZ());
        if (artifactRoll < 0.026 + gradient * 0.035) {
            return random.nextFloat() < 0.5f ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        double netherRoll = coordinateNoise(origin.asLong() ^ 0x6BC9730FA48C229DL, Math.floorDiv(column.getX(), 2), Math.floorDiv(column.getZ(), 2));
        if (netherRoll < 0.08 + gradient * 0.34) {
            if (gradient > 0.72 && random.nextFloat() < 0.22f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            return random.nextFloat() < 0.16f ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState();
        }
        return pickScorchedNativeSurface(naturalSurface, column.getX(), column.getZ(), origin.asLong(), 1.0 - gradient);
    }

    private static void placeRitualPlatform(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalDungeonVariant variant,
        RandomSource random
    ) {
        int radius = variant == PortalDungeonVariant.BASALT_CITADEL ? 7 : 5;
        int radiusSq = radius * radius + radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) {
                    continue;
                }
                BlockPos pos = new BlockPos(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                setColumn(level, pieceBox, chunkBox, pos, StructureBlockPalette.ritualFloor(random), 2);
                set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void placeNetheritePedestals(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin
    ) {
        for (BlockPos pedestal : ritualPedestalPositions(origin)) {
            set(level, pieceBox, chunkBox, pedestal.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            set(level, pieceBox, chunkBox, pedestal, Blocks.NETHERITE_BLOCK.defaultBlockState());
            set(level, pieceBox, chunkBox, pedestal.above(), Blocks.AIR.defaultBlockState());
            set(level, pieceBox, chunkBox, pedestal.above(2), Blocks.AIR.defaultBlockState());
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

    private static void placeBasaltCitadelColumns(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        PortalFrameSpec frame
    ) {
        int left = -frame.width() / 2;
        int right = left + frame.width() - 1;
        BlockPos[] columns = new BlockPos[] {
            origin.offset(left - 2, 1, -4),
            origin.offset(right + 2, 1, -4),
            origin.offset(left - 2, 1, 4),
            origin.offset(right + 2, 1, 4)
        };
        for (BlockPos base : columns) {
            set(level, pieceBox, chunkBox, base.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            set(level, pieceBox, chunkBox, base, Blocks.SMOOTH_BASALT.defaultBlockState());
            placeBasaltColumn(level, pieceBox, chunkBox, base.above(), 8);
        }
    }

    private static void placeMasterworkRooms(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        List<CaveNode> caveNodes,
        List<BlockPos> deepChests,
        List<BlockPos> tunnelSpawners,
        RandomSource random
    ) {
        if (caveNodes.isEmpty()) {
            return;
        }

        CaveNode deepest = caveNodes.stream()
            .min((first, second) -> Integer.compare(first.center().getY(), second.center().getY()))
            .orElse(caveNodes.getFirst());
        placeWitherShrine(level, pieceBox, chunkBox, deepest.center().offset(0, -deepest.height() + 2, 0), deepChests, random);

        for (int i = 0; i < caveNodes.size(); i++) {
            CaveNode node = caveNodes.get(i);
            if (node == deepest || node.band() < 1 || i % 3 != 1) {
                continue;
            }

            BlockPos roomCenter = node.center().offset(0, -Math.max(1, node.height() - 2), 0);
            switch ((i + node.band()) % 3) {
                case 0 -> placeGoldVault(level, pieceBox, chunkBox, roomCenter, deepChests, random);
                case 1 -> placeBlazeChamber(level, pieceBox, chunkBox, roomCenter, random);
                default -> placeAncientVault(level, pieceBox, chunkBox, roomCenter, deepChests, random);
            }
        }
    }

    private static void placeWitherShrine(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        List<BlockPos> deepChests,
        RandomSource random
    ) {
        carveRectRoom(level, pieceBox, chunkBox, center, 3, 4, 3, random);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                    set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), (Math.abs(dx) + Math.abs(dz)) % 2 == 0
                        ? Blocks.SOUL_SAND.defaultBlockState()
                        : Blocks.SOUL_SOIL.defaultBlockState());
                }
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos inset = center.relative(direction, 3).above(2);
            set(level, pieceBox, chunkBox, inset, Blocks.CHISELED_NETHER_BRICKS.defaultBlockState());
            set(level, pieceBox, chunkBox, inset.relative(direction.getClockWise()), Blocks.CRACKED_NETHER_BRICKS.defaultBlockState());
            set(level, pieceBox, chunkBox, inset.relative(direction.getCounterClockWise()), Blocks.CRACKED_NETHER_BRICKS.defaultBlockState());
        }
        set(level, pieceBox, chunkBox, center.above(), Blocks.SOUL_SAND.defaultBlockState());
        set(level, pieceBox, chunkBox, center.above(2), Blocks.SOUL_SAND.defaultBlockState());
        set(level, pieceBox, chunkBox, center.above(3), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
        set(level, pieceBox, chunkBox, center.offset(-2, 1, 0), Blocks.SOUL_LANTERN.defaultBlockState());
        set(level, pieceBox, chunkBox, center.offset(2, 1, 0), Blocks.SOUL_LANTERN.defaultBlockState());
        deepChests.add(center.offset(0, 1, 2));
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] corner : corners) {
            BlockPos spawner = center.offset(corner[0], 1, corner[1]);
            placeConfiguredSpawner(level, pieceBox, chunkBox, spawner, EntityType.WITHER_SKELETON, 4, 8, 30, 80, 32, 24, false);
        }
    }

    private static void placeGoldVault(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        List<BlockPos> deepChests,
        RandomSource random
    ) {
        carveRectRoom(level, pieceBox, chunkBox, center, 3, 3, 3, random);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), Math.abs(dx) <= 1 && Math.abs(dz) <= 1
                    ? Blocks.GOLD_BLOCK.defaultBlockState()
                    : Blocks.GILDED_BLACKSTONE.defaultBlockState());
                if ((Math.abs(dx) == 2 || Math.abs(dz) == 2) && random.nextFloat() < 0.28f) {
                    set(level, pieceBox, chunkBox, center.offset(dx, 2, dz), Blocks.NETHER_GOLD_ORE.defaultBlockState());
                }
            }
        }
        set(level, pieceBox, chunkBox, center.offset(-1, 3, -3), Blocks.IRON_CHAIN.defaultBlockState());
        set(level, pieceBox, chunkBox, center.offset(1, 3, -3), Blocks.IRON_CHAIN.defaultBlockState());
        deepChests.add(center.offset(0, 1, 0));
    }

    private static void placeBlazeChamber(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        RandomSource random
    ) {
        carveRectRoom(level, pieceBox, chunkBox, center, 4, 5, 4, random);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), edge
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.SOUL_SOIL.defaultBlockState());
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) == 2) {
                    set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), Blocks.BLACKSTONE.defaultBlockState());
                }
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            set(level, pieceBox, chunkBox, center.above(dy), dy == 1 ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState());
        }
        int[][] glowstone = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] cluster : glowstone) {
            set(level, pieceBox, chunkBox, center.offset(cluster[0], 5, cluster[1]), Blocks.GLOWSTONE.defaultBlockState());
        }
        BlockPos firstSpawner = center.offset(-3, 2, 0);
        BlockPos secondSpawner = center.offset(3, 2, 0);
        placeConfiguredSpawner(level, pieceBox, chunkBox, firstSpawner, EntityType.BLAZE, 4, 8, 24, 68, 30, 24, false);
        placeConfiguredSpawner(level, pieceBox, chunkBox, secondSpawner, EntityType.BLAZE, 4, 8, 24, 68, 30, 24, false);
    }

    private static void placeAncientVault(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        List<BlockPos> deepChests,
        RandomSource random
    ) {
        carveRectRoom(level, pieceBox, chunkBox, center, 3, 3, 3, random);
        for (int dx = -2; dx <= 2; dx++) {
            set(level, pieceBox, chunkBox, center.offset(dx, 1, -3), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            set(level, pieceBox, chunkBox, center.offset(dx, 2, -3), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        set(level, pieceBox, chunkBox, center.offset(0, 1, -3), Blocks.GRAVEL.defaultBlockState());
        set(level, pieceBox, chunkBox, center.offset(0, 2, -3), Blocks.GRAVEL.defaultBlockState());
        for (int dx = -2; dx <= 2; dx += 2) {
            set(level, pieceBox, chunkBox, center.offset(dx, 1, 2), Blocks.ANCIENT_DEBRIS.defaultBlockState());
            deepChests.add(center.offset(dx, 1, 0));
        }
        set(level, pieceBox, chunkBox, center.offset(2, 0, -2), Blocks.LAVA.defaultBlockState());
        deepChests.add(center.offset(0, 1, 1));
    }

    private static void carveRectRoom(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        int xRadius,
        int height,
        int zRadius,
        RandomSource random
    ) {
        for (int dx = -xRadius; dx <= xRadius; dx++) {
            for (int dy = 0; dy <= height; dy++) {
                for (int dz = -zRadius; dz <= zRadius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean shell = dy == 0 || dy == height || Math.abs(dx) == xRadius || Math.abs(dz) == zRadius;
                    set(level, pieceBox, chunkBox, pos, shell ? StructureBlockPalette.deepWall(random) : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void placeSunkenSanctumArch(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin
    ) {
        BlockPos rim = terrainTopIfColumnInside(level, pieceBox, chunkBox, origin.offset(0, 0, -20));
        if (rim == null) {
            return;
        }

        int baseY = rim.getY() + 1;
        BlockPos leftBase = new BlockPos(origin.getX() - 2, baseY, origin.getZ() - 20);
        BlockPos rightBase = new BlockPos(origin.getX() + 2, baseY, origin.getZ() - 20);
        for (int height = 0; height < 4; height++) {
            set(level, pieceBox, chunkBox, leftBase.above(height), height == 2 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
            set(level, pieceBox, chunkBox, rightBase.above(height), height == 1 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
        }

        int capY = baseY + 3;
        set(level, pieceBox, chunkBox, new BlockPos(origin.getX() - 1, capY, origin.getZ() - 20), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        set(level, pieceBox, chunkBox, new BlockPos(origin.getX(), capY, origin.getZ() - 20), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        set(level, pieceBox, chunkBox, new BlockPos(origin.getX() + 1, capY, origin.getZ() - 20), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
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
        BlockPos topCenter = terrainTopIfColumnInside(level, pieceBox, chunkBox, center);
        if (topCenter == null) {
            return;
        }
        TerrainProfile centerProfile = terrainProfile(level, topCenter.getX(), topCenter.getZ());
        boolean waterBasin = centerProfile.hasWater();
        int lavaY = waterBasin ? centerProfile.waterSurfaceY() : topCenter.getY();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.35;
                BlockPos columnTop = terrainTopIfColumnInside(level, pieceBox, chunkBox, topCenter.offset(dx, 0, dz));
                if (columnTop == null) {
                    continue;
                }
                TerrainProfile profile = terrainProfile(level, columnTop.getX(), columnTop.getZ());
                BlockPos pos = new BlockPos(columnTop.getX(), lavaY, columnTop.getZ());
                if (distance <= radius) {
                    if (waterBasin || profile.hasWater()) {
                        set(level, pieceBox, chunkBox, pos.below(), Blocks.BLACKSTONE.defaultBlockState());
                        set(level, pieceBox, chunkBox, pos, random.nextFloat() < 0.68f ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState());
                    } else {
                        set(level, pieceBox, chunkBox, pos.below(), Blocks.NETHERRACK.defaultBlockState());
                        set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                    }
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
            BlockPos top = terrainTopIfColumnInside(level, pieceBox, chunkBox, center.offset(dx, 0, dz));
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
        // Fix: outer scatter only placed netherrack, so the far scar lacked the promised crying-obsidian artifact flecks. Rare accent blocks now appear inside sparse patches without changing the low-density outer footprint.
        int radius = 1 + random.nextInt(gradient > 0.55 ? 4 : 3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
                    continue;
                }
                BlockPos column = center.offset(dx, 0, dz);
                if (!isColumnInside(pieceBox, chunkBox, column)) {
                    continue;
                }
                int columnDx = column.getX() - origin.getX();
                int columnDz = column.getZ() - origin.getZ();
                double distance = Math.sqrt(columnDx * columnDx + columnDz * columnDz);
                BlockPos top = sculptedTerrainTop(
                    level,
                    pieceBox,
                    chunkBox,
                    origin,
                    column.getX(),
                    column.getZ(),
                    distance,
                    origin.asLong() ^ 0x45BF38D6E0A47C91L
                );
                TerrainProfile profile = terrainProfile(level, column.getX(), column.getZ());
                BlockState naturalSurface = profile.naturalSurface();
                BlockState surface = pickOuterBlendSurface(origin, column, naturalSurface, random, gradient);
                setColumn(level, pieceBox, chunkBox, top, surface, isNetherScarSurface(surface) ? 2 : 1);
                settleColumnLiquid(level, pieceBox, chunkBox, top, profile, gradient, origin.asLong());
                stabilizeScarColumn(level, pieceBox, chunkBox, top, profile, surface, gradient);
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
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                BlockPos mouthTop = pitMouthTop(level, pieceBox, chunkBox, origin, dx, dz);
                int topY = Math.max(bottomY + 8, mouthTop.getY());
                for (int y = topY; y >= bottomY; y--) {
                    double t = (topY - y) / (double) Math.max(1, topY - bottomY);
                    double mouthFlare = t < 0.20 ? (1.0 - t / 0.20) * 3.1 : 0.0;
                    double radius = 7.2 - t * 1.45 + mouthFlare;
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
        placePitLedges(level, pieceBox, chunkBox, origin, bottomY);
        placePitSupportRibs(level, pieceBox, chunkBox, origin, bottomY);
    }

    private static BlockPos pitMouthTop(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int dx,
        int dz
    ) {
        int x = origin.getX() + dx;
        int z = origin.getZ() + dz;
        TerrainProfile profile = terrainProfile(level, x, z);
        if (!profile.hasWater()) {
            return profile.floorTop();
        }

        BlockPos top = retargetTerrainTop(level, pieceBox, chunkBox, x, z, profile.waterSurfaceY(), cooledSupportBlock(profile, x, z, origin.asLong()));
        settleColumnLiquid(level, pieceBox, chunkBox, top, profile, 0.95, origin.asLong());
        return top;
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
                BlockPos pos = pitMouthTop(level, pieceBox, chunkBox, origin, dx, dz);
                if (distance <= raggedRadius - 0.9) {
                    carveNetherAir(level, pieceBox, chunkBox, pos);
                    if (coordinateNoise(origin.asLong() ^ 0x0D1B54A32F5B827CL, pos.getX(), pos.getZ()) < 0.45) {
                        carveNetherAir(level, pieceBox, chunkBox, pos.below());
                    }
                } else if (distance <= raggedRadius + 3.7) {
                    double rubble = coordinateNoise(origin.asLong() ^ 0x3C27A21E158B572FL, pos.getX(), pos.getZ());
                    if (rubble < 0.62) {
                        setColumn(level, pieceBox, chunkBox, pos, pickPitRimBlock(pos), rubble < 0.32 ? 3 : 2);
                    }
                    if (rubble > 0.86 && distance <= raggedRadius + 1.6) {
                        carveNetherAir(level, pieceBox, chunkBox, pos.below());
                    }
                }
            }
        }
        softenPitRimBlend(level, pieceBox, chunkBox, origin);
    }

    private static void softenPitRimBlend(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin
    ) {
        for (int dx = -21; dx <= 21; dx++) {
            for (int dz = -21; dz <= 21; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance < 13.5 || distance > 20.5) {
                    continue;
                }
                BlockPos column = origin.offset(dx, 0, dz);
                if (!isColumnInside(pieceBox, chunkBox, column)) {
                    continue;
                }
                BlockPos top = terrainTop(level, column);
                BlockState naturalSurface = level.getBlockState(top);
                double blend = 1.0 - clamp01((distance - 13.5) / 7.0);
                double roll = coordinateNoise(origin.asLong() ^ 0x2383DB4FC627D5A9L, column.getX(), column.getZ());
                if (roll < blend * 0.58) {
                    BlockState surface = pickScorchedNativeSurface(naturalSurface, column.getX(), column.getZ(), origin.asLong(), blend);
                    setColumn(level, pieceBox, chunkBox, top, surface, 1);
                    stabilizeScarColumn(level, pieceBox, chunkBox, top, terrainProfile(level, column.getX(), column.getZ()), surface, blend);
                }
            }
        }
    }

    private static void placePitLedges(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int bottomY
    ) {
        int[] drops = {8, 17, 27, 36};
        for (int i = 0; i < drops.length; i++) {
            int ledgeY = origin.getY() - drops[i];
            if (ledgeY <= bottomY + 3) {
                continue;
            }
            double facing = (Math.PI * 0.5 * i) + coordinateNoise(origin.asLong() ^ i * 91815541L, origin.getX(), origin.getZ()) * 0.8;
            for (int step = -5; step <= 5; step++) {
                double angle = facing + step * 0.18;
                double radius = 7.3 + (step & 1) * 0.8;
                BlockPos ledge = new BlockPos(
                    origin.getX() + (int) Math.round(Math.cos(angle) * radius),
                    ledgeY,
                    origin.getZ() + (int) Math.round(Math.sin(angle) * radius)
                );
                set(level, pieceBox, chunkBox, ledge, step % 3 == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : pickPitRimBlock(ledge));
                set(level, pieceBox, chunkBox, ledge.above(), Blocks.AIR.defaultBlockState());
                if (step % 2 == 0) {
                    set(level, pieceBox, chunkBox, ledge.below(), Blocks.BLACKSTONE.defaultBlockState());
                }
            }
        }
    }

    private static void placePitSupportRibs(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        int bottomY
    ) {
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0 + coordinateNoise(origin.asLong() ^ 0x4B4F2D6B358479A1L, i, i * 7) * 0.18;
            int dx = (int) Math.round(Math.cos(angle) * 8.5);
            int dz = (int) Math.round(Math.sin(angle) * 8.5);
            BlockPos top = pitMouthTop(level, pieceBox, chunkBox, origin, dx, dz);
            int lowY = Math.max(bottomY + 5, top.getY() - 24);
            for (int y = top.getY() - 1; y >= lowY; y--) {
                BlockPos rib = new BlockPos(top.getX(), y, top.getZ());
                BlockState state = (y + i) % 5 == 0
                    ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                    : ((y + i) % 3 == 0 ? Blocks.BASALT.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
                set(level, pieceBox, chunkBox, rib, state);
                if ((y - lowY) % 7 == 0) {
                    set(level, pieceBox, chunkBox, rib.relative(Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(origin.asLong() ^ y ^ i))), Blocks.IRON_BARS.defaultBlockState());
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
        for (int i = 0; i < 12; i++) {
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

    private static void placePitLavaMoat(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos origin,
        RandomSource random
    ) {
        int moatY = origin.getY() - 10;
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.4 - 0.2;
                if (distance < 7.4 || distance > 10.1) {
                    continue;
                }
                BlockPos pos = new BlockPos(origin.getX() + dx, moatY, origin.getZ() + dz);
                set(level, pieceBox, chunkBox, pos.below(), pickLavaRimBlock(random));
                set(level, pieceBox, chunkBox, pos, Blocks.LAVA.defaultBlockState());
                set(level, pieceBox, chunkBox, pos.above(), Blocks.AIR.defaultBlockState());
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
                set(level, pieceBox, chunkBox, center.offset(dx, 0, dz), StructureBlockPalette.ritualFloor(RandomSource.create(center.asLong() ^ dx * 31L ^ dz * 17L)));
                set(level, pieceBox, chunkBox, center.offset(dx, -1, dz), StructureBlockPalette.deepWall(RandomSource.create(center.asLong() ^ dx * 57L ^ dz * 23L)));
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
        carveTunnelSegment(level, pieceBox, chunkBox, center, 3, 2, random);
    }

    private static void carveTunnelSegment(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        int radius,
        int verticalRadius,
        RandomSource random
    ) {
        carveTunnelSegment(level, pieceBox, chunkBox, center, radius, verticalRadius, 1, random);
    }

    private static void carveTunnelSegment(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos center,
        int radius,
        int verticalRadius,
        int materialBand,
        RandomSource random
    ) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double normalized = (dx * dx + dz * dz) / (double) (radius * radius)
                        + (dy * dy) / (double) Math.max(1, verticalRadius * verticalRadius + 1);
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (normalized <= 0.72) {
                        carveNetherAir(level, pieceBox, chunkBox, pos);
                    } else if (normalized <= 1.15) {
                        set(level, pieceBox, chunkBox, pos, pickTunnelWall(pos, materialBand, random));
                    }
                    if (dy == -verticalRadius && normalized <= 0.95) {
                        set(level, pieceBox, chunkBox, pos, pickCaveFloor(materialBand, random));
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
        return pickTunnelWall(pos, 1, random);
    }

    private static BlockState pickTunnelWall(BlockPos pos, int materialBand, RandomSource random) {
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
        if (materialBand >= 2) {
            if (roll < 0.44f) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            if (roll < 0.72f) {
                return Blocks.BASALT.defaultBlockState();
            }
            if (roll < 0.82f) {
                return Blocks.MAGMA_BLOCK.defaultBlockState();
            }
        } else if (roll < 0.28f) {
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
        return corruptedWallBlock(pos, random);
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
        return corruptedWallBlock(pos, RandomSource.create(pos.asLong()));
    }

    private static BlockState corruptedWallBlock(BlockPos pos, RandomSource random) {
        if (pos.getY() < 0) {
            return StructureBlockPalette.deepWall(random);
        }
        return StructureBlockPalette.netherWall(random);
    }

    private static BlockState pickPitRimBlock(BlockPos pos) {
        double roll = coordinateNoise(0x49F2E6C3A881D27CL, pos.getX(), pos.getZ());
        if (roll < 0.36) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.64) {
            return Blocks.BASALT.defaultBlockState();
        }
        if (roll < 0.82) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        return Blocks.SOUL_SOIL.defaultBlockState();
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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

    private static TerrainProfile terrainProfile(WorldGenLevel level, int x, int z) {
        int worldSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
        boolean waterAtSurface = level.getFluidState(new BlockPos(x, worldSurface, z)).is(FluidTags.WATER);
        int waterDepth = Math.max(0, worldSurface - oceanFloor);
        boolean hasWater = waterAtSurface || waterDepth > 1;
        BlockPos floorTop = new BlockPos(x, hasWater ? oceanFloor : worldSurface, z);
        BlockState naturalSurface = level.getBlockState(floorTop);
        int slope = columnSlope(level, x, z, floorTop.getY());
        return new TerrainProfile(floorTop, worldSurface, oceanFloor, waterDepth, hasWater, naturalSurface, slope);
    }

    private static int columnSlope(WorldGenLevel level, int x, int z, int centerY) {
        int slope = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int neighborX = x + direction.getStepX();
            int neighborZ = z + direction.getStepZ();
            int neighborSurface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, neighborX, neighborZ) - 1;
            slope = Math.max(slope, Math.abs(centerY - neighborSurface));
        }
        return slope;
    }

    private static BlockState cooledSupportBlock(TerrainProfile profile, int x, int z, long seed) {
        double roll = coordinateNoise(seed ^ 0x6732B4D8A19C0E37L, x, z);
        if (profile.waterDepth() > SHALLOW_WATER_DEPTH) {
            if (roll < 0.52) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            return roll < 0.86 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (roll < 0.45) {
            return Blocks.BASALT.defaultBlockState();
        }
        return roll < 0.82 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
    }

    private static void settleColumnLiquid(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos top,
        TerrainProfile profile,
        double intensity,
        long seed
    ) {
        if (!profile.hasWater()) {
            return;
        }

        BlockState cooledCap = cooledWaterCap(profile, top, intensity, seed);
        set(level, pieceBox, chunkBox, top, cooledCap);
        set(level, pieceBox, chunkBox, top.below(), underSurfaceBlock(cooledCap));
        int maxClearY = Math.min(top.getY() + 2, profile.waterSurfaceY() + 1);
        for (int y = top.getY() + 1; y <= maxClearY; y++) {
            BlockPos pos = new BlockPos(top.getX(), y, top.getZ());
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                set(level, pieceBox, chunkBox, pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static BlockState cooledWaterCap(TerrainProfile profile, BlockPos top, double intensity, long seed) {
        double roll = coordinateNoise(seed ^ 0x3485704B952F68F7L, top.getX(), top.getZ());
        if (profile.waterDepth() > SHALLOW_WATER_DEPTH && roll < 0.34 + intensity * 0.18) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (roll < 0.56) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        return roll < 0.86 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
    }

    private static void stabilizeScarColumn(
        WorldGenLevel level,
        BoundingBox pieceBox,
        BoundingBox chunkBox,
        BlockPos top,
        TerrainProfile profile,
        BlockState surface,
        double intensity
    ) {
        if (profile.slope() < CLIFF_SLOPE_THRESHOLD && !profile.hasWater()) {
            return;
        }

        BlockState support = isNetherScarSurface(surface) ? underSurfaceBlock(surface) : Blocks.BLACKSTONE.defaultBlockState();
        int maxDrop = profile.hasWater() ? Math.min(8, profile.waterDepth() + 2) : Math.min(9, profile.slope() + 1);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborColumn = top.relative(direction);
            if (!isColumnInside(pieceBox, chunkBox, neighborColumn)) {
                continue;
            }

            BlockPos neighborTop = terrainTop(level, neighborColumn);
            int drop = top.getY() - neighborTop.getY();
            if (drop <= 2) {
                continue;
            }

            int minY = Math.max(neighborTop.getY() + 1, top.getY() - maxDrop);
            for (int y = minY; y < top.getY(); y++) {
                BlockPos pos = new BlockPos(neighborColumn.getX(), y, neighborColumn.getZ());
                BlockState existing = level.getBlockState(pos);
                if (existing.isAir() || level.getFluidState(pos).is(FluidTags.WATER)) {
                    set(level, pieceBox, chunkBox, pos, support);
                }
            }
        }
    }

    private static boolean isNetherScarSurface(BlockState surface) {
        return surface.is(Blocks.NETHERRACK)
            || surface.is(Blocks.BLACKSTONE)
            || surface.is(Blocks.POLISHED_BLACKSTONE)
            || surface.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
            || surface.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
            || surface.is(Blocks.BASALT)
            || surface.is(Blocks.SMOOTH_BASALT)
            || surface.is(Blocks.SOUL_SAND)
            || surface.is(Blocks.SOUL_SOIL)
            || surface.is(Blocks.CRIMSON_NYLIUM)
            || surface.is(Blocks.MAGMA_BLOCK)
            || surface.is(Blocks.CRYING_OBSIDIAN);
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

    private record TerrainProfile(
        BlockPos floorTop,
        int waterSurfaceY,
        int oceanFloorY,
        int waterDepth,
        boolean hasWater,
        BlockState naturalSurface,
        int slope
    ) {
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

    private static double smoothNoise(long seed, double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;
        double sx = smoothStep(x - x0);
        double sz = smoothStep(z - z0);
        double north = lerp(coordinateNoise(seed, x0, z0), coordinateNoise(seed, x1, z0), sx);
        double south = lerp(coordinateNoise(seed, x0, z1), coordinateNoise(seed, x1, z1), sx);
        return lerp(north, south, sz);
    }

    private static double smoothStep(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
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

    private record CaveNode(BlockPos center, int radius, int height, int band) {
    }
}
