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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public abstract class EnergySwirlLayer<S extends EntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public EnergySwirlLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S entityRenderState, float f, float g) {
        if (!this.isPowered(entityRenderState)) {
            return;
        }
        float h = ((EntityRenderState)entityRenderState).ageInTicks;
        M entityModel = this.model();
        submitNodeCollector.order(1).submitModel(entityModel, entityRenderState, poseStack, RenderTypes.energySwirl(this.getTextureLocation(), this.xOffset(h) % 1.0f, h * 0.01f % 1.0f), i, OverlayTexture.NO_OVERLAY, -8355712, (TextureAtlasSprite)null, ((EntityRenderState)entityRenderState).outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
    }

    protected abstract boolean isPowered(S var1);

    protected abstract float xOffset(float var1);

    protected abstract Identifier getTextureLocation();

    protected abstract M model();
}

