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
import net.minecraft.client.model.animal.dolphin.DolphinModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class DolphinCarryingItemLayer
extends RenderLayer<DolphinRenderState, DolphinModel> {
    public DolphinCarryingItemLayer(RenderLayerParent<DolphinRenderState, DolphinModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, DolphinRenderState dolphinRenderState, float f, float g) {
        ItemStackRenderState itemStackRenderState = dolphinRenderState.heldItem;
        if (itemStackRenderState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        float h = 1.0f;
        float j = -1.0f;
        float k = Mth.abs(dolphinRenderState.xRot) / 60.0f;
        if (dolphinRenderState.xRot < 0.0f) {
            poseStack.translate(0.0f, 1.0f - k * 0.5f, -1.0f + k * 0.5f);
        } else {
            poseStack.translate(0.0f, 1.0f + k * 0.8f, -1.0f + k * 0.2f);
        }
        itemStackRenderState.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, dolphinRenderState.outlineColor);
        poseStack.popPose();
    }
}

