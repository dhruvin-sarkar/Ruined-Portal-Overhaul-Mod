/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.fox.FoxModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class FoxHeldItemLayer
extends RenderLayer<FoxRenderState, FoxModel> {
    public FoxHeldItemLayer(RenderLayerParent<FoxRenderState, FoxModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, FoxRenderState foxRenderState, float f, float g) {
        ItemStackRenderState itemStackRenderState = foxRenderState.heldItem;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        boolean bl = foxRenderState.isSleeping;
        boolean bl2 = foxRenderState.isBaby;
        poseStack.pushPose();
        poseStack.translate(((FoxModel)this.getParentModel()).head.x / 16.0f, ((FoxModel)this.getParentModel()).head.y / 16.0f, ((FoxModel)this.getParentModel()).head.z / 16.0f);
        if (bl2) {
            float h = 0.75f;
            poseStack.scale(0.75f, 0.75f, 0.75f);
        }
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotation(foxRenderState.headRollAngle));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(g));
        if (foxRenderState.isBaby) {
            if (bl) {
                poseStack.translate(0.4f, 0.26f, 0.15f);
            } else {
                poseStack.translate(0.06f, 0.26f, -0.5f);
            }
        } else if (bl) {
            poseStack.translate(0.46f, 0.26f, 0.22f);
        } else {
            poseStack.translate(0.06f, 0.27f, -0.5f);
        }
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        if (bl) {
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
        itemStackRenderState.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, foxRenderState.outlineColor);
        poseStack.popPose();
    }
}

