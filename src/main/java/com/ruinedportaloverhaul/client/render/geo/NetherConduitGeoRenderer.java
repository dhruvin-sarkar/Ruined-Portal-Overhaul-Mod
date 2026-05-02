package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import com.ruinedportaloverhaul.client.render.geo.model.NetherConduitGeoModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class NetherConduitGeoRenderer<R extends BlockEntityRenderState & GeoRenderState> extends GeoBlockRenderer<NetherConduitBlockEntity, R> {
    private static final int INACTIVE_TINT = 0xFF77706E;
    private static final int ACTIVE_BASE_TINT = 0xFFFF4A24;
    private static final int ACTIVE_LEVEL_TWO_TINT = 0xFFFF7A32;
    private static final int ACTIVE_LEVEL_THREE_TINT = 0xFFFFD06A;

    public NetherConduitGeoRenderer() {
        // Fix: GeckoLib 5 renders block entities through vanilla BlockEntityRenderState objects that are mixed into GeoRenderState at runtime. Keeping the renderer generic on that intersection preserves the real runtime state type while still satisfying javac's compile-time bound.
        super(new NetherConduitGeoModel());
        this.withRenderLayer(new NetherConduitInnerGlowLayer<>(this));
    }

    @Override
    public void addRenderData(NetherConduitBlockEntity animatable, Void relatedObject, R renderState, float partialTick) {
        // Fix: block-entity render layers do not receive the live conduit directly, so active state and upgrade level are copied into GeckoLib render-state tickets before the inner glow layer runs.
        renderState.addGeckolibData(RuinedPortalGeoRenderData.CONDUIT_ACTIVE, animatable.isActiveClientSide());
        renderState.addGeckolibData(RuinedPortalGeoRenderData.CONDUIT_LEVEL, animatable.conduitLevel());
    }

    @Override
    public int getRenderColor(NetherConduitBlockEntity animatable, Void relatedObject, float partialTick) {
        // Tint the shared conduit texture so inactive and upgraded states read clearly in-world.
        if (!animatable.isActiveClientSide()) {
            return INACTIVE_TINT;
        }

        return switch (animatable.conduitLevel()) {
            case 1 -> ACTIVE_LEVEL_TWO_TINT;
            case 2 -> ACTIVE_LEVEL_THREE_TINT;
            default -> ACTIVE_BASE_TINT;
        };
    }
}
