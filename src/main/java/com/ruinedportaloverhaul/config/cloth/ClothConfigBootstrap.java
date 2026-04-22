package com.ruinedportaloverhaul.config.cloth;

import com.ruinedportaloverhaul.config.ModConfigManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class ClothConfigBootstrap {
    private static boolean initialized;

    private ClothConfigBootstrap() {
    }

    public static void initialize() {
        // Fix: optional config support now registers AutoConfig exactly once and exposes live getters instead of freezing defaults at startup.
        if (initialized) {
            return;
        }

        AutoConfig.register(ClothRuntimeConfig.class, GsonConfigSerializer::new);
        ModConfigManager.installRuntimeSource(() -> AutoConfig.getConfigHolder(ClothRuntimeConfig.class).getConfig());
        initialized = true;
    }
}
