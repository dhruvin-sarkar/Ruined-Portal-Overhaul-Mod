package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinVexGeoModel;
import com.ruinedportaloverhaul.entity.PiglinVexEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinVexGeoRenderer<R extends VexRenderState & GeoRenderState> extends GeoEntityRenderer<PiglinVexEntity, R> {
    public PiglinVexGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinVexGeoModel());
    }
}
