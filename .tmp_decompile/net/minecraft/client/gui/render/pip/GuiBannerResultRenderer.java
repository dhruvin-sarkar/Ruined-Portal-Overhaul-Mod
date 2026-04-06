/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiBannerResultRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBakery;

@Environment(value=EnvType.CLIENT)
public class GuiBannerResultRenderer
extends PictureInPictureRenderer<GuiBannerResultRenderState> {
    private final MaterialSet materials;

    public GuiBannerResultRenderer(MultiBufferSource.BufferSource bufferSource, MaterialSet materialSet) {
        super(bufferSource);
        this.materials = materialSet;
    }

    @Override
    public Class<GuiBannerResultRenderState> getRenderStateClass() {
        return GuiBannerResultRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiBannerResultRenderState guiBannerResultRenderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        poseStack.translate(0.0f, 0.25f, 0.0f);
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
        BannerRenderer.submitPatterns(this.materials, poseStack, submitNodeStorage, 0xF000F0, OverlayTexture.NO_OVERLAY, guiBannerResultRenderState.flag(), Float.valueOf(0.0f), ModelBakery.BANNER_BASE, true, guiBannerResultRenderState.baseColor(), guiBannerResultRenderState.resultBannerPatterns(), false, null, 0);
        featureRenderDispatcher.renderAllFeatures();
    }

    @Override
    protected String getTextureLabel() {
        return "banner result";
    }
}

