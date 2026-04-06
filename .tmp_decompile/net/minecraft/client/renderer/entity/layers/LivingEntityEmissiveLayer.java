/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@Environment(value=EnvType.CLIENT)
public class LivingEntityEmissiveLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final Function<S, Identifier> textureProvider;
    private final AlphaFunction<S> alphaFunction;
    private final M model;
    private final Function<Identifier, RenderType> bufferProvider;
    private final boolean alwaysVisible;

    public LivingEntityEmissiveLayer(RenderLayerParent<S, M> renderLayerParent, Function<S, Identifier> function, AlphaFunction<S> alphaFunction, M entityModel, Function<Identifier, RenderType> function2, boolean bl) {
        super(renderLayerParent);
        this.textureProvider = function;
        this.alphaFunction = alphaFunction;
        this.model = entityModel;
        this.bufferProvider = function2;
        this.alwaysVisible = bl;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S livingEntityRenderState, float f, float g) {
        if (((LivingEntityRenderState)livingEntityRenderState).isInvisible && !this.alwaysVisible) {
            return;
        }
        float h = this.alphaFunction.apply(livingEntityRenderState, ((LivingEntityRenderState)livingEntityRenderState).ageInTicks);
        if (h <= 1.0E-5f) {
            return;
        }
        int j = ARGB.white(h);
        RenderType renderType = this.bufferProvider.apply(this.textureProvider.apply(livingEntityRenderState));
        submitNodeCollector.order(1).submitModel(this.model, livingEntityRenderState, poseStack, renderType, i, LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0f), j, (TextureAtlasSprite)null, ((LivingEntityRenderState)livingEntityRenderState).outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface AlphaFunction<S extends LivingEntityRenderState> {
        public float apply(S var1, float var2);
    }
}

