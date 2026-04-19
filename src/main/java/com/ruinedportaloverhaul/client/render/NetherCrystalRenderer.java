package com.ruinedportaloverhaul.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class NetherCrystalRenderer extends EntityRenderer<NetherCrystalEntity, EndCrystalRenderState> {
    private static final Identifier END_CRYSTAL_TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutNoCull(END_CRYSTAL_TEXTURE);
    private static final int CRIMSON_TINT = 0xFF8A0000;

    private final EndCrystalModel model;

    public NetherCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    @Override
    public void submit(EndCrystalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0.0f, -0.5f, 0.0f);
        collector.submitModel(
            this.model,
            state,
            poseStack,
            RENDER_TYPE,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            CRIMSON_TINT,
            null,
            state.outlineColor,
            null
        );
        poseStack.popPose();

        Vec3 beamOffset = state.beamOffset;
        if (beamOffset != null) {
            float crystalY = EndCrystalRenderer.getY(state.ageInTicks);
            float beamX = (float) beamOffset.x;
            float beamY = (float) beamOffset.y;
            float beamZ = (float) beamOffset.z;
            poseStack.translate(beamOffset);
            EnderDragonRenderer.submitCrystalBeams(
                -beamX,
                -beamY + crystalY,
                -beamZ,
                state.ageInTicks,
                poseStack,
                collector,
                state.lightCoords
            );
        }

        super.submit(state, poseStack, collector, cameraState);
    }

    @Override
    public EndCrystalRenderState createRenderState() {
        return new EndCrystalRenderState();
    }

    @Override
    public void extractRenderState(NetherCrystalEntity entity, EndCrystalRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.ageInTicks = entity.time + tickDelta;
        state.showsBottom = entity.showsBottom();

        BlockPos beamTarget = entity.getBeamTarget();
        state.beamOffset = beamTarget == null
            ? null
            : Vec3.atCenterOf(beamTarget).subtract(entity.getPosition(tickDelta));
    }

    @Override
    public boolean shouldRender(NetherCrystalEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.getBeamTarget() != null;
    }
}
