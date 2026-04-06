/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class PlayerItemInHandLayer<S extends AvatarRenderState, M extends EntityModel<S> & HeadedModel>
extends ItemInHandLayer<S, M> {
    private static final float X_ROT_MIN = -0.5235988f;
    private static final float X_ROT_MAX = 1.5707964f;

    public PlayerItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    protected void submitArmWithItem(S avatarRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
        InteractionHand interactionHand;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        InteractionHand interactionHand2 = interactionHand = humanoidArm == ((AvatarRenderState)avatarRenderState).mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (((AvatarRenderState)avatarRenderState).isUsingItem && ((AvatarRenderState)avatarRenderState).useItemHand == interactionHand && ((AvatarRenderState)avatarRenderState).attackTime < 1.0E-5f && !((AvatarRenderState)avatarRenderState).heldOnHead.isEmpty()) {
            this.renderItemHeldToEye(avatarRenderState, humanoidArm, poseStack, submitNodeCollector, i);
        } else {
            super.submitArmWithItem(avatarRenderState, itemStackRenderState, itemStack, humanoidArm, poseStack, submitNodeCollector, i);
        }
    }

    private void renderItemHeldToEye(S avatarRenderState, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
        poseStack.pushPose();
        ((Model)this.getParentModel()).root().translateAndRotate(poseStack);
        ModelPart modelPart = ((HeadedModel)this.getParentModel()).getHead();
        float f = modelPart.xRot;
        modelPart.xRot = Mth.clamp(modelPart.xRot, -0.5235988f, 1.5707964f);
        modelPart.translateAndRotate(poseStack);
        modelPart.xRot = f;
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((bl ? -2.5f : 2.5f) / 16.0f, -0.0625f, 0.0f);
        ((AvatarRenderState)avatarRenderState).heldOnHead.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, ((AvatarRenderState)avatarRenderState).outlineColor);
        poseStack.popPose();
    }
}

