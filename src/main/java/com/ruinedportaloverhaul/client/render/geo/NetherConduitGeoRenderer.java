package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import com.ruinedportaloverhaul.client.render.geo.model.NetherConduitGeoModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class NetherConduitGeoRenderer<R extends BlockEntityRenderState & GeoRenderState> extends GeoBlockRenderer<NetherConduitBlockEntity, R> {
    public NetherConduitGeoRenderer() {
        // Fix: GeckoLib 5 renders block entities through vanilla BlockEntityRenderState objects that are mixed into GeoRenderState at runtime. Keeping the renderer generic on that intersection preserves the real runtime state type while still satisfying javac's compile-time bound.
        super(new NetherConduitGeoModel());
    }
}
