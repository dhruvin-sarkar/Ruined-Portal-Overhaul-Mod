package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinPillagerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinPillagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinPillagerGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinPillagerEntity, R> {
    public PiglinPillagerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinPillagerGeoModel());
    }
}
