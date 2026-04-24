package com.ruinedportaloverhaul.component;

import com.mojang.serialization.Codec;
import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    public static final DataComponentType<Long> LAST_NECKLACE_FIREBALL_TICK = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "last_necklace_fireball_tick"),
        DataComponentType.<Long>builder()
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG)
            .build()
    );
    public static final DataComponentType<Integer> NETHER_CONDUIT_LEVEL = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "nether_conduit_level"),
        DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build()
    );

    private ModDataComponents() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul data components");
    }
}
