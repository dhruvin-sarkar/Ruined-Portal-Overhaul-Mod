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
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class SnowGolemHeadLayer
extends RenderLayer<SnowGolemRenderState, SnowGolemModel> {
    private final BlockRenderDispatcher blockRenderer;

    public SnowGolemHeadLayer(RenderLayerParent<SnowGolemRenderState, SnowGolemModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, SnowGolemRenderState snowGolemRenderState, float f, float g) {
        if (!snowGolemRenderState.hasPumpkin) {
            return;
        }
        if (snowGolemRenderState.isInvisible && !snowGolemRenderState.appearsGlowing()) {
            return;
        }
        poseStack.pushPose();
        ((SnowGolemModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        float h = 0.625f;
        poseStack.translate(0.0f, -0.34375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        BlockState blockState = Blocks.CARVED_PUMPKIN.defaultBlockState();
        BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
        int j = LivingEntityRenderer.getOverlayCoords(snowGolemRenderState, 0.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        RenderType renderType = snowGolemRenderState.appearsGlowing() && snowGolemRenderState.isInvisible ? RenderTypes.outline(TextureAtlas.LOCATION_BLOCKS) : ItemBlockRenderTypes.getRenderType(blockState);
        submitNodeCollector.submitBlockModel(poseStack, renderType, blockStateModel, 0.0f, 0.0f, 0.0f, i, j, snowGolemRenderState.outlineColor);
        poseStack.popPose();
    }
}

