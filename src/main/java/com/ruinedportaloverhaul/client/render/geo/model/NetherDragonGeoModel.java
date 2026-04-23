package com.ruinedportaloverhaul.client.render.geo.model;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.entity.NetherDragonEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class NetherDragonGeoModel extends GeoModel<NetherDragonEntity> {
    private static final Identifier MODEL_RESOURCE = id("geo/entity/nether_dragon.geo.json");
    private static final Identifier TEXTURE_RESOURCE = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final Identifier ANIMATION_RESOURCE = id("animations/entity/nether_dragon.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL_RESOURCE;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public Identifier getAnimationResource(NetherDragonEntity animatable) {
        return ANIMATION_RESOURCE;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
