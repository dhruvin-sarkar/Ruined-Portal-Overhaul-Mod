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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class CrossedArmsItemLayer<S extends HoldingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public CrossedArmsItemLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S holdingEntityRenderState, float f, float g) {
        ItemStackRenderState itemStackRenderState = ((HoldingEntityRenderState)holdingEntityRenderState).heldItem;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.applyTranslation(holdingEntityRenderState, poseStack);
        itemStackRenderState.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, ((HoldingEntityRenderState)holdingEntityRenderState).outlineColor);
        poseStack.popPose();
    }

    protected void applyTranslation(S holdingEntityRenderState, PoseStack poseStack) {
        ((VillagerLikeModel)this.getParentModel()).translateToArms(holdingEntityRenderState, poseStack);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(0.75f));
        poseStack.scale(1.07f, 1.07f, 1.07f);
        poseStack.translate(0.0f, 0.13f, -0.34f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation((float)Math.PI));
    }
}

