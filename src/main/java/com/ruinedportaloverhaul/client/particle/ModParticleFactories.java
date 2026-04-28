package com.ruinedportaloverhaul.client.particle;

import com.ruinedportaloverhaul.world.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class ModParticleFactories {
    private ModParticleFactories() {
    }

    public static void initialize() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.NETHER_EMBER, NetherEmberParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CORRUPTION_RUNE, CorruptionRuneParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.DRAGON_BLOOD, DragonBloodParticle.Provider::new);
    }
}
