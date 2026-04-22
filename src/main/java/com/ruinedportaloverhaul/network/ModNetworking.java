package com.ruinedportaloverhaul.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private static boolean initialized;

    private ModNetworking() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        PayloadTypeRegistry.playS2C().register(DragonPhaseFlashPayload.TYPE, DragonPhaseFlashPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalAtmospherePayload.TYPE, PortalAtmospherePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetherFireballPayload.TYPE, NetherFireballPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NetherFireballPayload.TYPE, (payload, context) ->
            NetherFireballHandler.handle(context.player())
        );
        initialized = true;
    }
}
