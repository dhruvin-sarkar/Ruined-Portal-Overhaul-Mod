package com.ruinedportaloverhaul.client;

import com.ruinedportaloverhaul.block.entity.ModBlockEntities;
import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import com.ruinedportaloverhaul.client.render.geo.ExiledPiglinGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.NetherConduitGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.NetherCrystalGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinBrutePillagerGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinEvokerGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinIllusionerGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinPillagerGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinRavagerGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinVexGeoRenderer;
import com.ruinedportaloverhaul.client.render.geo.PiglinVindicatorGeoRenderer;
import com.ruinedportaloverhaul.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public final class RuinedPortalOverhaulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Fix: block animations were missing a block-entity renderer hookup, so the conduit could never use GeckoLib on the client even after the animatable logic existed. The client bootstrap now registers the conduit renderer beside the mob renderers.
        PortalAtmosphereClient.initialize();
        NetherFireballKeybinds.initialize();
        BlockEntityRenderers.register(
            ModBlockEntities.NETHER_CONDUIT,
            context -> new NetherConduitGeoRenderer()
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_PILLAGER,
            context -> new PiglinPillagerGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_VINDICATOR,
            context -> new PiglinVindicatorGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_BRUTE_PILLAGER,
            context -> new PiglinBrutePillagerGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_ILLUSIONER,
            context -> new PiglinIllusionerGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_EVOKER,
            context -> new PiglinEvokerGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_RAVAGER,
            context -> new PiglinRavagerGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_VEX,
            context -> new PiglinVexGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.EXILED_PIGLIN,
            context -> new ExiledPiglinGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.NETHER_CRYSTAL,
            context -> new NetherCrystalGeoRenderer<>(context)
        );
        EntityRenderers.register(
            ModEntities.NETHER_DRAGON,
            EnderDragonRenderer::new
        );
    }
}
