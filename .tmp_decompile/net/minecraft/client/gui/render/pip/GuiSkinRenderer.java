/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4fStack
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiSkinRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4fStack;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class GuiSkinRenderer
extends PictureInPictureRenderer<GuiSkinRenderState> {
    public GuiSkinRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiSkinRenderState> getRenderStateClass() {
        return GuiSkinRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiSkinRenderState guiSkinRenderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.PLAYER_SKIN);
        int i = Minecraft.getInstance().getWindow().getGuiScale();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        float f = guiSkinRenderState.scale() * (float)i;
        matrix4fStack.rotateAround((Quaternionfc)Axis.XP.rotationDegrees(guiSkinRenderState.rotationX()), 0.0f, f * -guiSkinRenderState.pivotY(), 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-guiSkinRenderState.rotationY()));
        poseStack.translate(0.0f, -1.6010001f, 0.0f);
        RenderType renderType = guiSkinRenderState.playerModel().renderType(guiSkinRenderState.texture());
        guiSkinRenderState.playerModel().renderToBuffer(poseStack, this.bufferSource.getBuffer(renderType), 0xF000F0, OverlayTexture.NO_OVERLAY);
        this.bufferSource.endBatch();
        matrix4fStack.popMatrix();
    }

    @Override
    protected String getTextureLabel() {
        return "player skin";
    }
}

