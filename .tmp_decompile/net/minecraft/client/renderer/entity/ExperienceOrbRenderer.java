/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class ExperienceOrbRenderer
extends EntityRenderer<ExperienceOrb, ExperienceOrbRenderState> {
    private static final Identifier EXPERIENCE_ORB_LOCATION = Identifier.withDefaultNamespace("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderTypes.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public ExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    protected int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
        return Mth.clamp(super.getBlockLightLevel(experienceOrb, blockPos) + 7, 0, 15);
    }

    @Override
    public void submit(ExperienceOrbRenderState experienceOrbRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        int i = experienceOrbRenderState.icon;
        float f = (float)(i % 4 * 16 + 0) / 64.0f;
        float g = (float)(i % 4 * 16 + 16) / 64.0f;
        float h = (float)(i / 4 * 16 + 0) / 64.0f;
        float j = (float)(i / 4 * 16 + 16) / 64.0f;
        float k = 1.0f;
        float l = 0.5f;
        float m = 0.25f;
        float n = 255.0f;
        float o = experienceOrbRenderState.ageInTicks / 2.0f;
        int p = (int)((Mth.sin(o + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int q = 255;
        int r = (int)((Mth.sin(o + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        poseStack.translate(0.0f, 0.1f, 0.0f);
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        float s = 0.3f;
        poseStack.scale(0.3f, 0.3f, 0.3f);
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
            ExperienceOrbRenderer.vertex(vertexConsumer, pose, -0.5f, -0.25f, p, 255, r, f, j, experienceOrbRenderState.lightCoords);
            ExperienceOrbRenderer.vertex(vertexConsumer, pose, 0.5f, -0.25f, p, 255, r, g, j, experienceOrbRenderState.lightCoords);
            ExperienceOrbRenderer.vertex(vertexConsumer, pose, 0.5f, 0.75f, p, 255, r, g, h, experienceOrbRenderState.lightCoords);
            ExperienceOrbRenderer.vertex(vertexConsumer, pose, -0.5f, 0.75f, p, 255, r, f, h, experienceOrbRenderState.lightCoords);
        });
        poseStack.popPose();
        super.submit(experienceOrbRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, int i, int j, int k, float h, float l, int m) {
        vertexConsumer.addVertex(pose, f, g, 0.0f).setColor(i, j, k, 128).setUv(h, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(m).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public ExperienceOrbRenderState createRenderState() {
        return new ExperienceOrbRenderState();
    }

    @Override
    public void extractRenderState(ExperienceOrb experienceOrb, ExperienceOrbRenderState experienceOrbRenderState, float f) {
        super.extractRenderState(experienceOrb, experienceOrbRenderState, f);
        experienceOrbRenderState.icon = experienceOrb.getIcon();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

