package com.ruinedportaloverhaul.block.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class NetherConduitPowerTracker {
    private static final int POWER_EXPIRY_TICKS = 45;
    private static final Map<UUID, ConduitPower> ACTIVE_PLAYER_POWER = new HashMap<>();

    private NetherConduitPowerTracker() {
    }

    public static void grant(ServerPlayer player, long gameTime, int conduitLevel) {
        UUID playerId = player.getUUID();
        ConduitPower existing = ACTIVE_PLAYER_POWER.get(playerId);
        int level = existing == null || existing.expiresAt() <= gameTime
            ? conduitLevel
            : Math.max(existing.level(), conduitLevel);
        ACTIVE_PLAYER_POWER.put(playerId, new ConduitPower(gameTime + POWER_EXPIRY_TICKS, level));
    }

    public static int lavaBoostLevel(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return -1;
        }

        long gameTime = entity.level().getGameTime();
        ConduitPower power = ACTIVE_PLAYER_POWER.get(player.getUUID());
        if (power == null) {
            return -1;
        }
        if (power.expiresAt() < gameTime) {
            ACTIVE_PLAYER_POWER.remove(player.getUUID());
            return -1;
        }
        return power.level();
    }

    private record ConduitPower(long expiresAt, int level) {
    }
}
