package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.PiglinBrutePillagerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinBrutePillagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinBrutePillagerGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinBrutePillagerEntity, R> {
    public PiglinBrutePillagerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinBrutePillagerGeoModel());
    }
}
