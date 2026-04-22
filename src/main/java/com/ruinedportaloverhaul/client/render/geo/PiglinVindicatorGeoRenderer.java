package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinVindicatorGeoModel;
import com.ruinedportaloverhaul.entity.PiglinVindicatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinVindicatorGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinVindicatorEntity, R> {
    public PiglinVindicatorGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinVindicatorGeoModel());
    }
}
