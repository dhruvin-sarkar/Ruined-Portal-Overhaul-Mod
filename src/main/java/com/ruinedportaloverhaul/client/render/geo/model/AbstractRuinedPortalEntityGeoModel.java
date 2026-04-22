package com.ruinedportaloverhaul.client.render.geo.model;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.client.render.geo.RuinedPortalGeoRenderData;
import com.ruinedportaloverhaul.entity.TextureVariantMob;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

abstract class AbstractRuinedPortalEntityGeoModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    private final Identifier modelResource;
    private final Identifier[] textureResources;
    private final Identifier animationResource;

    protected AbstractRuinedPortalEntityGeoModel(String assetName) {
        this(assetName, 1);
    }

    protected AbstractRuinedPortalEntityGeoModel(String assetName, int textureVariantCount) {
        super(Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, assetName), "head");
        this.modelResource = id("geo/entity/" + assetName + ".geo.json");
        this.textureResources = createTextureResources(assetName, textureVariantCount);
        this.animationResource = id("animations/entity/" + assetName + ".animation.json");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.modelResource;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        int variant = renderState.getOrDefaultGeckolibData(RuinedPortalGeoRenderData.TEXTURE_VARIANT, 0);

        return this.textureResources[Math.floorMod(variant, this.textureResources.length)];
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return this.animationResource;
    }

    @Override
    public void addAdditionalStateData(T animatable, Object relatedObject, GeoRenderState renderState) {
        // Fix: the renderer previously had no per-entity texture state, so GeckoLib always fell back to a single PNG. The render state now captures each mob's synced visual variant before texture selection runs.
        if (animatable instanceof TextureVariantMob textureVariantMob) {
            renderState.addGeckolibData(RuinedPortalGeoRenderData.TEXTURE_VARIANT, textureVariantMob.getTextureVariant());
        }
    }

    protected static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }

    private static Identifier[] createTextureResources(String assetName, int textureVariantCount) {
        if (textureVariantCount <= 1) {
            return new Identifier[]{id("textures/entity/" + assetName + ".png")};
        }

        Identifier[] textures = new Identifier[textureVariantCount];

        for (int index = 0; index < textureVariantCount; index++) {
            textures[index] = id("textures/entity/" + assetName + "_" + index + ".png");
        }

        return textures;
    }
}
