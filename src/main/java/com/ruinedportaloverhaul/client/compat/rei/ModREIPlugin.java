package com.ruinedportaloverhaul.client.compat.rei;

import com.ruinedportaloverhaul.block.ModBlocks;
import com.ruinedportaloverhaul.item.ModItems;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public final class ModREIPlugin implements REIClientPlugin {
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // Fix: the REI entrypoint is normally only invoked by REI, but the extra guard keeps optional compat inert in partial loader/test setups.
        if (!FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            return;
        }

        // REI already discovers the normal JSON crafting recipes, so this compat layer only adds the progression details that were previously missing from the recipe viewer.
        registry.add(netherConduitDisplay());
        registry.add(netherCrystalDisplay());
        registry.add(ghastTearNecklaceDisplay());
        registry.add(netherStarDisplay());
        registry.add(portalShardDisplay());
        registry.add(corruptedNetheriteDisplay());
        registry.add(netherTideDisplay());
    }

    private static DefaultInformationDisplay netherConduitDisplay() {
        return DefaultInformationDisplay.createFromEntry(
                EntryStacks.of(ModBlocks.NETHER_CONDUIT_ITEM),
                text("rei.ruined_portal_overhaul.nether_conduit.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.nether_conduit.line1"),
                text("rei.ruined_portal_overhaul.nether_conduit.line2"),
                text("rei.ruined_portal_overhaul.nether_conduit.line3"),
                text("rei.ruined_portal_overhaul.nether_conduit.line4")
            ));
    }

    private static DefaultInformationDisplay netherCrystalDisplay() {
        return DefaultInformationDisplay.createFromEntries(
                EntryIngredients.ofItems(List.of(
                    ModItems.NETHER_CRYSTAL,
                    ModBlocks.NETHER_CONDUIT_ITEM,
                    Items.NETHER_STAR,
                    Items.NETHERITE_INGOT,
                    Items.CRYING_OBSIDIAN
                )),
                text("rei.ruined_portal_overhaul.nether_crystal.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.nether_crystal.line1"),
                text("rei.ruined_portal_overhaul.nether_crystal.line2"),
                text("rei.ruined_portal_overhaul.nether_crystal.line3")
            ));
    }

    private static DefaultInformationDisplay ghastTearNecklaceDisplay() {
        return DefaultInformationDisplay.createFromEntries(
                EntryIngredients.ofItems(List.of(
                    ModItems.GHAST_TEAR_NECKLACE,
                    Items.GHAST_TEAR,
                    Items.NETHER_STAR,
                    Items.GOLD_INGOT
                )),
                text("rei.ruined_portal_overhaul.ghast_tear_necklace.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.ghast_tear_necklace.line1"),
                text("rei.ruined_portal_overhaul.ghast_tear_necklace.line2"),
                text("rei.ruined_portal_overhaul.ghast_tear_necklace.line3"),
                text("rei.ruined_portal_overhaul.ghast_tear_necklace.line4")
            ));
    }

    private static DefaultInformationDisplay netherStarDisplay() {
        return DefaultInformationDisplay.createFromEntry(
                EntryStacks.of(Items.NETHER_STAR),
                text("rei.ruined_portal_overhaul.nether_star.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.nether_star.line1"),
                text("rei.ruined_portal_overhaul.nether_star.line2"),
                text("rei.ruined_portal_overhaul.nether_star.line3"),
                text("rei.ruined_portal_overhaul.nether_star.line4"),
                text("rei.ruined_portal_overhaul.nether_star.line5")
            ));
    }

    private static DefaultInformationDisplay portalShardDisplay() {
        return DefaultInformationDisplay.createFromEntry(
                EntryStacks.of(ModItems.PORTAL_SHARD),
                text("rei.ruined_portal_overhaul.portal_shard.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.portal_shard.line1"),
                text("rei.ruined_portal_overhaul.portal_shard.line2"),
                text("rei.ruined_portal_overhaul.portal_shard.line3")
            ));
    }

    private static DefaultInformationDisplay corruptedNetheriteDisplay() {
        return DefaultInformationDisplay.createFromEntries(
                EntryIngredients.ofItems(List.of(
                    ModItems.CORRUPTED_NETHERITE_INGOT,
                    ModItems.CORRUPTED_NETHERITE_CHESTPLATE,
                    Items.NETHERITE_CHESTPLATE,
                    Items.ECHO_SHARD
                )),
                text("rei.ruined_portal_overhaul.corrupted_netherite.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.corrupted_netherite.line1"),
                text("rei.ruined_portal_overhaul.corrupted_netherite.line2"),
                text("rei.ruined_portal_overhaul.corrupted_netherite.line3"),
                text("rei.ruined_portal_overhaul.corrupted_netherite.line4")
            ));
    }

    private static DefaultInformationDisplay netherTideDisplay() {
        return DefaultInformationDisplay.createFromEntry(
                EntryStacks.of(ModItems.MUSIC_DISC_NETHER_TIDE),
                text("rei.ruined_portal_overhaul.nether_tide.title"))
            .lines(List.of(
                text("rei.ruined_portal_overhaul.nether_tide.line1"),
                text("rei.ruined_portal_overhaul.nether_tide.line2"),
                text("rei.ruined_portal_overhaul.nether_tide.line3")
            ));
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
