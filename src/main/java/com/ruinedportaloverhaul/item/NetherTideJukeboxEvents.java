package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.raid.PortalRaidState;
import com.ruinedportaloverhaul.world.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class NetherTideJukeboxEvents {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int PLAYER_SCAN_RADIUS = 8;
    private static final double PORTAL_FOOTPRINT_RADIUS_SQUARED = 64.0 * 64.0;

    private NetherTideJukeboxEvents() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(NetherTideJukeboxEvents::tick);
    }

    private static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        PortalRaidState state = PortalRaidState.get(server);
        if (state.completedPortalOrigins().isEmpty()) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension() != Level.OVERWORLD) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                spawnNearbyDiscParticles(level, player, state);
            }
        }
    }

    private static void spawnNearbyDiscParticles(ServerLevel level, ServerPlayer player, PortalRaidState state) {
        BlockPos playerPos = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
            playerPos.offset(-PLAYER_SCAN_RADIUS, -PLAYER_SCAN_RADIUS, -PLAYER_SCAN_RADIUS),
            playerPos.offset(PLAYER_SCAN_RADIUS, PLAYER_SCAN_RADIUS, PLAYER_SCAN_RADIUS)
        )) {
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.hasProperty(JukeboxBlock.HAS_RECORD) || !blockState.getValue(JukeboxBlock.HAS_RECORD)) {
                continue;
            }
            if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) {
                continue;
            }
            ItemStack disc = jukebox.getTheItem();
            if (!disc.is(ModItems.MUSIC_DISC_NETHER_TIDE) || !nearCompletedPortal(pos, state)) {
                continue;
            }
            level.sendParticles(ModParticles.NETHER_EMBER, pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5, 4, 0.35, 0.2, 0.35, 0.015);
        }
    }

    private static boolean nearCompletedPortal(BlockPos pos, PortalRaidState state) {
        for (BlockPos portalOrigin : state.completedPortalOrigins()) {
            if (horizontalDistanceSqr(portalOrigin, pos) <= PORTAL_FOOTPRINT_RADIUS_SQUARED) {
                return true;
            }
        }
        return false;
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }
}
