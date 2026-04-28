package com.ruinedportaloverhaul.world;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModParticles {
    public static final SimpleParticleType NETHER_EMBER = register("nether_ember");
    public static final SimpleParticleType CORRUPTION_RUNE = register("corruption_rune");
    public static final SimpleParticleType DRAGON_BLOOD = register("dragon_blood");

    private ModParticles() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul particles");
    }

    private static SimpleParticleType register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, FabricParticleTypes.simple());
    }
}
