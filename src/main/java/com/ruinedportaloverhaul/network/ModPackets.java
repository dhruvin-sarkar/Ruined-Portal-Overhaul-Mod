package com.ruinedportaloverhaul.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModPackets {
    private static boolean registered;

    private ModPackets() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        PayloadTypeRegistry.playS2C().register(DragonPhaseFlashPayload.TYPE, DragonPhaseFlashPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalAtmospherePayload.TYPE, PortalAtmospherePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NetherFireballPayload.TYPE, NetherFireballPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NetherFireballPayload.TYPE, (payload, context) ->
            context.player().level().getServer().execute(() -> NetherFireballHandler.handle(context.player(), payload))
        );
        registered = true;
    }
}
