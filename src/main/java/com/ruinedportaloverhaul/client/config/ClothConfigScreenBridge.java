package com.ruinedportaloverhaul.client.config;

import com.ruinedportaloverhaul.config.cloth.ClothConfigBootstrap;
import com.ruinedportaloverhaul.config.cloth.ClothRuntimeConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.Screen;

public final class ClothConfigScreenBridge {
    private ClothConfigScreenBridge() {
    }

    @SuppressWarnings({"deprecation", "removal"})
    public static Screen create(Screen parent) {
        ClothConfigBootstrap.initialize();
        return AutoConfig.getConfigScreen(ClothRuntimeConfig.class, parent).get();
    }
}
