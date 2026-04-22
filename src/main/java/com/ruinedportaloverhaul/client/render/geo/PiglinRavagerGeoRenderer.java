package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinRavagerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinRavagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinRavagerGeoRenderer<R extends RavagerRenderState & GeoRenderState> extends GeoEntityRenderer<PiglinRavagerEntity, R> {
    public PiglinRavagerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinRavagerGeoModel());
    }
}
