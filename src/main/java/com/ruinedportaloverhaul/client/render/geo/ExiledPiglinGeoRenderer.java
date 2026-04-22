package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.ExiledPiglinGeoModel;
import com.ruinedportaloverhaul.entity.ExiledPiglinTraderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class ExiledPiglinGeoRenderer<R extends VillagerRenderState & GeoRenderState> extends GeoEntityRenderer<ExiledPiglinTraderEntity, R> {
    public ExiledPiglinGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new ExiledPiglinGeoModel());
    }
}
