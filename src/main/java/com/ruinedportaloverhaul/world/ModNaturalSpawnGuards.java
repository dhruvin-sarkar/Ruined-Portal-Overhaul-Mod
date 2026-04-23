package com.ruinedportaloverhaul.world;

import com.ruinedportaloverhaul.raid.PortalRaidState;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ModNaturalSpawnGuards {
    private static final int COMPLETED_PORTAL_VERTICAL_RANGE = PortalStructureHelper.PIT_DEPTH + 80;
    private static final double COMPLETED_PORTAL_RADIUS_SQUARED =
        PortalStructureHelper.OUTER_RADIUS * PortalStructureHelper.OUTER_RADIUS;

    private ModNaturalSpawnGuards() {
    }

    public static boolean shouldSuppressNaturalSpawn(ServerLevel level, BlockPos spawnPos) {
        // Fix: portal ambient pressure is now owned entirely by the structure-local raid manager, so this guard only blocks new natural spawns inside completed portal territory instead of globally interfering with blaze or piglin spawns elsewhere in the overworld.
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return false;
        }

        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        for (BlockPos portalOrigin : portalRaidState.completedPortalOrigins()) {
            if (isInsideCompletedPortalSuppressionArea(spawnPos, portalOrigin)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideCompletedPortalSuppressionArea(BlockPos spawnPos, BlockPos portalOrigin) {
        return Math.abs(spawnPos.getY() - portalOrigin.getY()) <= COMPLETED_PORTAL_VERTICAL_RANGE
            && horizontalDistanceSqr(spawnPos, portalOrigin) <= COMPLETED_PORTAL_RADIUS_SQUARED;
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }
}
