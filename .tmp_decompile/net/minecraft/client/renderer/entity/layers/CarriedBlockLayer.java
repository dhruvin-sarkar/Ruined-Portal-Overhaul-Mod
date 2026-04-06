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
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class CarriedBlockLayer
extends RenderLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {
    public CarriedBlockLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, EndermanRenderState endermanRenderState, float f, float g) {
        BlockState blockState = endermanRenderState.carriedBlock;
        if (blockState == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.6875f, -0.75f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(20.0f));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(45.0f));
        poseStack.translate(0.25f, 0.1875f, 0.25f);
        float h = 0.5f;
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        submitNodeCollector.submitBlock(poseStack, blockState, i, OverlayTexture.NO_OVERLAY, endermanRenderState.outlineColor);
        poseStack.popPose();
    }
}

