package com.ruinedportaloverhaul;

import com.ruinedportaloverhaul.entity.ModEntities;
import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.network.ModNetworking;
import com.ruinedportaloverhaul.raid.GoldRaidManager;
import com.ruinedportaloverhaul.world.ModStructures;
import com.ruinedportaloverhaul.world.ModWorldGen;
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
        ModAdvancementTriggers.initialize();
        ModStructures.initialize();
        ModWorldGen.initialize();
        ModNetworking.initialize();
        GoldRaidManager.initialize();
    }
}
