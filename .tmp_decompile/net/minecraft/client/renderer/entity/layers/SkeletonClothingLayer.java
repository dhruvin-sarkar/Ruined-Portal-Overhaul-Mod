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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public class SkeletonClothingLayer<S extends SkeletonRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final SkeletonModel<S> layerModel;
    private final Identifier clothesLocation;

    public SkeletonClothingLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, ModelLayerLocation modelLayerLocation, Identifier identifier) {
        super(renderLayerParent);
        this.clothesLocation = identifier;
        this.layerModel = new SkeletonModel(entityModelSet.bakeLayer(modelLayerLocation));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S skeletonRenderState, float f, float g) {
        SkeletonClothingLayer.coloredCutoutModelCopyLayerRender(this.layerModel, this.clothesLocation, poseStack, submitNodeCollector, i, skeletonRenderState, -1, 1);
    }
}

