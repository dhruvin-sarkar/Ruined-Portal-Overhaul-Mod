package com.ruinedportaloverhaul.client.render.geo;

import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;

final class NetherConduitInnerGlowLayer<R extends BlockEntityRenderState & GeoRenderState>
    extends CustomBoneTextureGeoLayer<NetherConduitBlockEntity, Void, R> {
    private static final String INNER_BONE = "inner";
    private static final Identifier INNER_CORE_TEXTURE = Identifier.withDefaultNamespace("textures/block/netherrack.png");

    NetherConduitInnerGlowLayer(GeoRenderer<NetherConduitBlockEntity, Void, R> renderer) {
        super(renderer, INNER_BONE, INNER_CORE_TEXTURE);
    }

    @Override
    public void preRender(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        // Fix: GeckoLib's stock CustomBoneTextureGeoLayer hides the target bone before re-rendering it. The conduit needs an additive glow overlay, so the base inner core stays visible and this layer only adds emissive light when active.
    }

    @Override
    protected @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        // Fix: the first conduit pass changed spin speed but had no visible active glow. The renderer now gates an emissive inner-core pass from synced render-state data instead of relying on a missing vanilla glowmask texture.
        if (!renderState.getOrDefaultGeckolibData(RuinedPortalGeoRenderData.CONDUIT_ACTIVE, false)) {
            return null;
        }

        return AutoGlowingGeoLayer.EmissiveRenderType.getRenderType(texture, false, false, false);
    }
}
