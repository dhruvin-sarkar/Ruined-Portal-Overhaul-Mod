package com.ruinedportaloverhaul.client.config;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.lang.reflect.InvocationTargetException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!ModConfigManager.isClothConfigAvailable()) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }

        return parent -> openConfigScreen(parent);
    }

    private Screen openConfigScreen(Screen parent) {
        try {
            Class<?> screenBridge = Class.forName("com.ruinedportaloverhaul.client.config.ClothConfigScreenBridge");
            return (Screen) screenBridge.getMethod("create", Screen.class).invoke(null, parent);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            RuinedPortalOverhaul.LOGGER.warn("Failed to open the optional Cloth Config screen.", exception);
            return parent;
        }
    }
}
