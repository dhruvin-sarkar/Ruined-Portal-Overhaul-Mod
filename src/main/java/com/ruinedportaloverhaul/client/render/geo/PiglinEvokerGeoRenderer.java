package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinEvokerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinEvokerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinEvokerGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinEvokerEntity, R> {
    public PiglinEvokerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinEvokerGeoModel());
    }
}
