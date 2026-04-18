package com.ruinedportaloverhaul.block;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final Identifier NETHER_CONDUIT_ID = id("nether_conduit");

    public static final NetherConduitBlock NETHER_CONDUIT = Registry.register(
        BuiltInRegistries.BLOCK,
        NETHER_CONDUIT_ID,
        new NetherConduitBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, NETHER_CONDUIT_ID))
            .mapColor(MapColor.NETHER)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE)
            .noOcclusion())
    );

    public static final BlockItem NETHER_CONDUIT_ITEM = Registry.register(
        BuiltInRegistries.ITEM,
        NETHER_CONDUIT_ID,
        new BlockItem(
            NETHER_CONDUIT,
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, NETHER_CONDUIT_ID))
                .fireResistant()
        )
    );

    private ModBlocks() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul blocks");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
