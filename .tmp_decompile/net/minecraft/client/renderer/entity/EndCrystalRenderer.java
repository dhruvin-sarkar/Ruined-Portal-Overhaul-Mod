/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class EndCrystalRenderer
extends EntityRenderer<EndCrystal, EndCrystalRenderState> {
    private static final Identifier END_CRYSTAL_LOCATION = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private final EndCrystalModel model;

    public EndCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    @Override
    public void submit(EndCrystalRenderState endCrystalRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0.0f, -0.5f, 0.0f);
        submitNodeCollector.submitModel(this.model, endCrystalRenderState, poseStack, RENDER_TYPE, endCrystalRenderState.lightCoords, OverlayTexture.NO_OVERLAY, endCrystalRenderState.outlineColor, null);
        poseStack.popPose();
        Vec3 vec3 = endCrystalRenderState.beamOffset;
        if (vec3 != null) {
            float f = EndCrystalRenderer.getY(endCrystalRenderState.ageInTicks);
            float g = (float)vec3.x;
            float h = (float)vec3.y;
            float i = (float)vec3.z;
            poseStack.translate(vec3);
            EnderDragonRenderer.submitCrystalBeams(-g, -h + f, -i, endCrystalRenderState.ageInTicks, poseStack, submitNodeCollector, endCrystalRenderState.lightCoords);
        }
        super.submit(endCrystalRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    public static float getY(float f) {
        float g = Mth.sin(f * 0.2f) / 2.0f + 0.5f;
        g = (g * g + g) * 0.4f;
        return g - 1.4f;
    }

    @Override
    public EndCrystalRenderState createRenderState() {
        return new EndCrystalRenderState();
    }

    @Override
    public void extractRenderState(EndCrystal endCrystal, EndCrystalRenderState endCrystalRenderState, float f) {
        super.extractRenderState(endCrystal, endCrystalRenderState, f);
        endCrystalRenderState.ageInTicks = (float)endCrystal.time + f;
        endCrystalRenderState.showsBottom = endCrystal.showsBottom();
        BlockPos blockPos = endCrystal.getBeamTarget();
        endCrystalRenderState.beamOffset = blockPos != null ? Vec3.atCenterOf(blockPos).subtract(endCrystal.getPosition(f)) : null;
    }

    @Override
    public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double e, double f) {
        return super.shouldRender(endCrystal, frustum, d, e, f) || endCrystal.getBeamTarget() != null;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

