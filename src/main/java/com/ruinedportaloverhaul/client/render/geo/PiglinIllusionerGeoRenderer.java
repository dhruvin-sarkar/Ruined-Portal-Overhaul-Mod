package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinIllusionerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinIllusionerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinIllusionerGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinIllusionerEntity, R> {
    public PiglinIllusionerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinIllusionerGeoModel());
    }
}
