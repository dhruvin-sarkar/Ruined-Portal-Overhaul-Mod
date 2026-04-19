package com.ruinedportaloverhaul.advancement;

import com.ruinedportaloverhaul.world.ModStructures;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancementTriggers {
    public static final PortalEventTrigger PORTAL_APPROACH = register("portal_approach");
    public static final PortalEventTrigger PIT_DESCENT = register("pit_descent");
    public static final PortalEventTrigger DEEP_STORM = register("deep_storm");
    public static final PortalEventTrigger AETHER_BOON = register("aether_boon");
    public static final PortalEventTrigger TERRITORY_TOTEM = register("territory_totem");
    public static final PortalEventTrigger RAID_STARTED = register("raid_started");
    public static final PortalEventTrigger RAID_COMPLETED = register("raid_completed");
    public static final PortalEventTrigger EXILED_TRADE = register("exiled_trade");
    public static final PortalEventTrigger NETHER_CONDUIT_LEVEL_2 = register("nether_conduit_level_2");
    public static final PortalEventTrigger GHAST_TEAR_NECKLACE_EQUIPPED = register("ghast_tear_necklace_equipped");
    public static final PortalEventTrigger NETHER_FIREBALL_USED = register("nether_fireball_used");
    public static final PortalEventTrigger NETHER_CRYSTAL_RITUAL_COMPLETE = register("nether_crystal_ritual_complete");
    public static final PortalEventTrigger NETHER_DRAGON_DEFEATED = register("nether_dragon_defeated");

    private ModAdvancementTriggers() {
    }

    public static void initialize() {
        // Loading this class registers the custom criteria above.
    }

    public static void trigger(PortalEventTrigger trigger, ServerPlayer player) {
        trigger.trigger(player);
    }

    private static PortalEventTrigger register(String path) {
        return CriteriaTriggers.register(ModStructures.id(path).toString(), new PortalEventTrigger());
    }
}
