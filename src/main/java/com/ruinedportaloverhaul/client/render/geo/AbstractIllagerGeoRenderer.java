package com.ruinedportaloverhaul.client.render.geo;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;

abstract class AbstractIllagerGeoRenderer<T extends AbstractIllager & GeoAnimatable, R extends IllagerRenderState & GeoRenderState> extends GeoEntityRenderer<T, R> {
    protected AbstractIllagerGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.withRenderLayer(new ItemInHandGeoLayer<>(this, "rightArm", "leftArm"));
    }
}
