package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final Identifier GHAST_TEAR_NECKLACE_ID = id("ghast_tear_necklace");

    public static final GhastTearNecklaceItem GHAST_TEAR_NECKLACE = Registry.register(
        BuiltInRegistries.ITEM,
        GHAST_TEAR_NECKLACE_ID,
        new GhastTearNecklaceItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, GHAST_TEAR_NECKLACE_ID))
            .stacksTo(1)
            .fireResistant())
    );

    private ModItems() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul items");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
