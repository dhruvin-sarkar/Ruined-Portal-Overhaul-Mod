package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    private static final Identifier MAIN_TAB_ID = Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "main");
    private static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, MAIN_TAB_ID);

    public static final CreativeModeTab MAIN_TAB = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        MAIN_TAB_KEY,
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.ruined_portal_overhaul.main"))
            .icon(() -> new ItemStack(ModItems.PORTAL_SHARD))
            .displayItems((context, entries) -> addAll(entries))
            .build()
    );

    private ModCreativeTabs() {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> entries.accept(ModBlocks.NETHER_CONDUIT_ITEM));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(ModItems.CORRUPTED_NETHERITE_INGOT);
            entries.accept(ModItems.NETHER_DRAGON_SCALE);
            entries.accept(ModItems.NETHER_CRYSTAL);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(ModItems.PORTAL_SHARD);
            entries.accept(ModItems.GHAST_TEAR_NECKLACE);
            entries.accept(ModItems.MUSIC_DISC_NETHER_TIDE);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(ModItems.GHAST_TEAR_NECKLACE);
            entries.accept(ModItems.CORRUPTED_NETHERITE_HELMET);
            entries.accept(ModItems.CORRUPTED_NETHERITE_CHESTPLATE);
            entries.accept(ModItems.CORRUPTED_NETHERITE_LEGGINGS);
            entries.accept(ModItems.CORRUPTED_NETHERITE_BOOTS);
        });
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul creative inventory entries");
    }

    private static void addAll(CreativeModeTab.Output entries) {
        entries.accept(ModBlocks.NETHER_CONDUIT_ITEM);
        entries.accept(ModItems.GHAST_TEAR_NECKLACE);
        entries.accept(ModItems.NETHER_CRYSTAL);
        entries.accept(ModItems.PORTAL_SHARD);
        entries.accept(ModItems.CORRUPTED_NETHERITE_INGOT);
        entries.accept(ModItems.CORRUPTED_NETHERITE_HELMET);
        entries.accept(ModItems.CORRUPTED_NETHERITE_CHESTPLATE);
        entries.accept(ModItems.CORRUPTED_NETHERITE_LEGGINGS);
        entries.accept(ModItems.CORRUPTED_NETHERITE_BOOTS);
        entries.accept(ModItems.NETHER_DRAGON_SCALE);
        entries.accept(ModItems.MUSIC_DISC_NETHER_TIDE);
    }
}
