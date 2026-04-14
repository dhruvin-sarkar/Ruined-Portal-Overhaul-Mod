package com.ruinedportaloverhaul.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
    private static boolean initialized;

    private ModNetworking() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        PayloadTypeRegistry.playS2C().register(PortalAtmospherePayload.TYPE, PortalAtmospherePayload.CODEC);
        initialized = true;
    }
}
