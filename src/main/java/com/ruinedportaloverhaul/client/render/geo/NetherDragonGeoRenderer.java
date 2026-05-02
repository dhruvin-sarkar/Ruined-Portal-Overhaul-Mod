package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.client.render.geo.model.NetherDragonGeoModel;
import com.ruinedportaloverhaul.entity.NetherDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class NetherDragonGeoRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<NetherDragonEntity, R> {
    private static final int BASE_TINT = 0xFF9A1C12;
    private static final int ENRAGED_TINT = 0xFFFF2A12;

    public NetherDragonGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new NetherDragonGeoModel());
        this.shadowRadius = 4.0f;
        this.withScale(2.25f, 2.25f);
    }

    @Override
    public void addRenderData(NetherDragonEntity animatable, Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(DataTickets.RENDER_COLOR, animatable.isEnragedPhase() ? ENRAGED_TINT : BASE_TINT);
    }

    @Override
    public RenderType getRenderType(R renderState, Identifier texture) {
        return RenderTypes.entityCutoutNoCull(texture);
    }
}
