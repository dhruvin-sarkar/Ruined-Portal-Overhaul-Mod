package com.ruinedportaloverhaul.client.render.geo.model;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

abstract class AbstractRuinedPortalEntityGeoModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    private final Identifier modelResource;
    private final Identifier textureResource;
    private final Identifier animationResource;

    protected AbstractRuinedPortalEntityGeoModel(String assetName) {
        super(Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, assetName), "head");
        this.modelResource = id("geo/entity/" + assetName + ".geo.json");
        this.textureResource = id("textures/entity/" + assetName + ".png");
        this.animationResource = id("animations/entity/" + assetName + ".animation.json");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.modelResource;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.textureResource;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return this.animationResource;
    }

    protected static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
