package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class NetherDragonRituals {
    private static final double RITUAL_MESSAGE_RANGE_SQUARED = 64.0 * 64.0;

    private NetherDragonRituals() {
    }

    public static void onNetherCrystalPlaced(ServerLevel level, BlockPos pedestalPos, NetherCrystalEntity crystal) {
        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        portalRaidState.completedPortalForPedestal(pedestalPos).ifPresent(portalOrigin -> {
            PortalRaidState.RitualProgress progress = portalRaidState.markRitualCrystalPlaced(portalOrigin, pedestalPos);
            if (progress.newlyCompleted() && !portalRaidState.isDragonActive(portalOrigin)) {
                notifyNearbyPlayers(level, portalOrigin, Component.literal("The four offerings are set.").withStyle(ChatFormatting.DARK_RED));
            }
        });
    }

    private static void notifyNearbyPlayers(ServerLevel level, BlockPos portalOrigin, Component message) {
        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().distSqr(portalOrigin) <= RITUAL_MESSAGE_RANGE_SQUARED)) {
            player.displayClientMessage(message, true);
        }
    }
}
