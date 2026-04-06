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
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class IronGolemFlowerLayer
extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, IronGolemRenderState ironGolemRenderState, float f, float g) {
        if (ironGolemRenderState.offerFlowerTick == 0) {
            return;
        }
        poseStack.pushPose();
        ModelPart modelPart = ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm();
        modelPart.translateAndRotate(poseStack);
        poseStack.translate(-1.1875f, 1.0625f, -0.9375f);
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float h = 0.5f;
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        submitNodeCollector.submitBlock(poseStack, Blocks.POPPY.defaultBlockState(), i, OverlayTexture.NO_OVERLAY, ironGolemRenderState.outlineColor);
        poseStack.popPose();
    }
}

