package com.ruinedportaloverhaul;

import com.ruinedportaloverhaul.block.ModBlocks;
import com.ruinedportaloverhaul.block.NetherConduitEvents;
import com.ruinedportaloverhaul.block.entity.ModBlockEntities;
import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.entity.ModEntities;
import com.ruinedportaloverhaul.item.GhastTearNecklaceEvents;
import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.item.ModItems;
import com.ruinedportaloverhaul.network.ModNetworking;
import com.ruinedportaloverhaul.raid.GoldRaidManager;
import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.sound.ModSounds;
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
        // Fix: initialize the optional config bridge before any gameplay systems so later registries stop baking hardcoded values.
        LOGGER.info("Initializing {}", MOD_ID);
        ModConfigManager.initialize();
        ModSounds.initialize();
        ModDataComponents.initialize();
        ModItems.initialize();
        GhastTearNecklaceEvents.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        NetherConduitEvents.initialize();
        ModEntities.initialize();
        ModAdvancementTriggers.initialize();
        ModStructures.initialize();
        ModWorldGen.initialize();
        ModNetworking.initialize();
        NetherDragonRituals.initialize();
        GoldRaidManager.initialize();
    }
}
