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
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class ItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public ItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S armedEntityRenderState, float f, float g) {
        this.submitArmWithItem(armedEntityRenderState, ((ArmedEntityRenderState)armedEntityRenderState).rightHandItemState, ((ArmedEntityRenderState)armedEntityRenderState).rightHandItemStack, HumanoidArm.RIGHT, poseStack, submitNodeCollector, i);
        this.submitArmWithItem(armedEntityRenderState, ((ArmedEntityRenderState)armedEntityRenderState).leftHandItemState, ((ArmedEntityRenderState)armedEntityRenderState).leftHandItemStack, HumanoidArm.LEFT, poseStack, submitNodeCollector, i);
    }

    protected void submitArmWithItem(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
        float f;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ((ArmedModel)this.getParentModel()).translateToHand(armedEntityRenderState, humanoidArm, poseStack);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((float)(bl ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        if (((ArmedEntityRenderState)armedEntityRenderState).attackTime > 0.0f && ((ArmedEntityRenderState)armedEntityRenderState).mainArm == humanoidArm && ((ArmedEntityRenderState)armedEntityRenderState).swingAnimationType == SwingAnimationType.STAB) {
            SpearAnimations.thirdPersonAttackItem(armedEntityRenderState, poseStack);
        }
        if ((f = ((ArmedEntityRenderState)armedEntityRenderState).ticksUsingItem(humanoidArm)) != 0.0f) {
            (humanoidArm == HumanoidArm.RIGHT ? ((ArmedEntityRenderState)armedEntityRenderState).rightArmPose : ((ArmedEntityRenderState)armedEntityRenderState).leftArmPose).animateUseItem(armedEntityRenderState, poseStack, f, humanoidArm, itemStack);
        }
        itemStackRenderState.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, ((ArmedEntityRenderState)armedEntityRenderState).outlineColor);
        poseStack.popPose();
    }
}

