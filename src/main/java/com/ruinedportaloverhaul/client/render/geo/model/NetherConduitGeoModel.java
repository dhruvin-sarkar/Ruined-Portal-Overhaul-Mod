package com.ruinedportaloverhaul.client.render.geo.model;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class NetherConduitGeoModel extends GeoModel<NetherConduitBlockEntity> {
    private static final Identifier MODEL_RESOURCE = id("geo/block/nether_conduit.geo.json");
    private static final Identifier TEXTURE_RESOURCE = Identifier.withDefaultNamespace("textures/block/obsidian.png");
    private static final Identifier ANIMATION_RESOURCE = id("animations/block/nether_conduit.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL_RESOURCE;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public Identifier getAnimationResource(NetherConduitBlockEntity animatable) {
        return ANIMATION_RESOURCE;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
