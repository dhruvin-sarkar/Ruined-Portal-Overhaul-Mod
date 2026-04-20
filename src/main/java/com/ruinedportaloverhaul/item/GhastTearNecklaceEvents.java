package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class GhastTearNecklaceEvents {
    private static final String NECKLACE_SEEN_TAG = "rpo_ghast_tear_necklace_seen";

    private GhastTearNecklaceEvents() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(GhastTearNecklaceEvents::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.tickCount % GhastTearNecklaceItem.EFFECT_REFRESH_INTERVAL_TICKS != 0) {
                continue;
            }

            ItemStack necklace = GhastTearNecklaceItem.findCarriedNecklace(player);
            if (necklace.isEmpty()) {
                continue;
            }

            GhastTearNecklaceItem.applyPassiveEffects(player);
            if (!player.getTags().contains(NECKLACE_SEEN_TAG)) {
                player.addTag(NECKLACE_SEEN_TAG);
                ModAdvancementTriggers.trigger(ModAdvancementTriggers.GHAST_TEAR_NECKLACE_EQUIPPED, player);
            }
        }
    }
}
