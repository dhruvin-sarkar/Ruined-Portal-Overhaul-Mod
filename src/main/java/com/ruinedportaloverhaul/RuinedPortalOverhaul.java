package com.ruinedportaloverhaul;

import com.ruinedportaloverhaul.entity.ModEntities;
import com.ruinedportaloverhaul.world.ModStructures;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuinedPortalOverhaul implements ModInitializer {
    public static final String MOD_ID = "ruined_portal_overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_ID);
        ModEntities.initialize();
        ModStructures.initialize();
    }
}
