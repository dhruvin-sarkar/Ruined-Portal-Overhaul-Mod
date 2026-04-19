package com.ruinedportaloverhaul.block.entity;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final Identifier NETHER_CONDUIT_ID = ModBlocks.NETHER_CONDUIT_ID;

    public static final BlockEntityType<NetherConduitBlockEntity> NETHER_CONDUIT = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        NETHER_CONDUIT_ID,
        FabricBlockEntityTypeBuilder.create(NetherConduitBlockEntity::new, ModBlocks.NETHER_CONDUIT).build()
    );

    private ModBlockEntities() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul block entities");
    }
}
