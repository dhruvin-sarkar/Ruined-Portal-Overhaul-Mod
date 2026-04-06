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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState>
extends EntityRenderer<T, S> {
    private final ArrowModel model;

    public ArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
    }

    @Override
    public void submit(S arrowRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(((ArrowRenderState)arrowRenderState).yRot - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(((ArrowRenderState)arrowRenderState).xRot));
        submitNodeCollector.submitModel(this.model, arrowRenderState, poseStack, RenderTypes.entityCutout(this.getTextureLocation(arrowRenderState)), ((ArrowRenderState)arrowRenderState).lightCoords, OverlayTexture.NO_OVERLAY, ((ArrowRenderState)arrowRenderState).outlineColor, null);
        poseStack.popPose();
        super.submit(arrowRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    protected abstract Identifier getTextureLocation(S var1);

    @Override
    public void extractRenderState(T abstractArrow, S arrowRenderState, float f) {
        super.extractRenderState(abstractArrow, arrowRenderState, f);
        ((ArrowRenderState)arrowRenderState).xRot = ((Entity)abstractArrow).getXRot(f);
        ((ArrowRenderState)arrowRenderState).yRot = ((Entity)abstractArrow).getYRot(f);
        ((ArrowRenderState)arrowRenderState).shake = (float)((AbstractArrow)abstractArrow).shakeTime - f;
    }
}

