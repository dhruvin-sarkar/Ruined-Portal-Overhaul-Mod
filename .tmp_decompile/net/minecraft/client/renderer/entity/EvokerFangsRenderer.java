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
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.effects.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsRenderer
extends EntityRenderer<EvokerFangs, EvokerFangsRenderState> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel model;

    public EvokerFangsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EvokerFangsModel(context.bakeLayer(ModelLayers.EVOKER_FANGS));
    }

    @Override
    public void submit(EvokerFangsRenderState evokerFangsRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        float f = evokerFangsRenderState.biteProgress;
        if (f == 0.0f) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f - evokerFangsRenderState.yRot));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        submitNodeCollector.submitModel(this.model, evokerFangsRenderState, poseStack, this.model.renderType(TEXTURE_LOCATION), evokerFangsRenderState.lightCoords, OverlayTexture.NO_OVERLAY, evokerFangsRenderState.outlineColor, null);
        poseStack.popPose();
        super.submit(evokerFangsRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public EvokerFangsRenderState createRenderState() {
        return new EvokerFangsRenderState();
    }

    @Override
    public void extractRenderState(EvokerFangs evokerFangs, EvokerFangsRenderState evokerFangsRenderState, float f) {
        super.extractRenderState(evokerFangs, evokerFangsRenderState, f);
        evokerFangsRenderState.yRot = evokerFangs.getYRot();
        evokerFangsRenderState.biteProgress = evokerFangs.getAnimationProgress(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

