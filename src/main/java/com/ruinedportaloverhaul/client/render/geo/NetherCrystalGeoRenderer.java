package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.NetherCrystalGeoModel;
import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class NetherCrystalGeoRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<NetherCrystalEntity, R> {
    private static final int CRIMSON_TINT = 0xFF8A0000;

    public NetherCrystalGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new NetherCrystalGeoModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public int getRenderColor(NetherCrystalEntity animatable, Void relatedObject, float partialTick) {
        return CRIMSON_TINT;
    }

    @Override
    public RenderType getRenderType(R renderState, Identifier texture) {
        return RenderTypes.entityCutoutNoCull(texture);
    }
}
