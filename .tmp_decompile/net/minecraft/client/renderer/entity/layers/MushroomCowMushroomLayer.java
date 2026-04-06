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
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class MushroomCowMushroomLayer
extends RenderLayer<MushroomCowRenderState, CowModel> {
    private final BlockRenderDispatcher blockRenderer;

    public MushroomCowMushroomLayer(RenderLayerParent<MushroomCowRenderState, CowModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, MushroomCowRenderState mushroomCowRenderState, float f, float g) {
        boolean bl;
        if (mushroomCowRenderState.isBaby) {
            return;
        }
        boolean bl2 = bl = mushroomCowRenderState.appearsGlowing() && mushroomCowRenderState.isInvisible;
        if (mushroomCowRenderState.isInvisible && !bl) {
            return;
        }
        BlockState blockState = mushroomCowRenderState.variant.getBlockState();
        int j = LivingEntityRenderer.getOverlayCoords(mushroomCowRenderState, 0.0f);
        BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.submitMushroomBlock(poseStack, submitNodeCollector, i, bl, mushroomCowRenderState.outlineColor, blockState, j, blockStateModel);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(42.0f));
        poseStack.translate(0.1f, 0.0f, -0.6f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.submitMushroomBlock(poseStack, submitNodeCollector, i, bl, mushroomCowRenderState.outlineColor, blockState, j, blockStateModel);
        poseStack.popPose();
        poseStack.pushPose();
        ((CowModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0f, -0.7f, -0.2f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-78.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.submitMushroomBlock(poseStack, submitNodeCollector, i, bl, mushroomCowRenderState.outlineColor, blockState, j, blockStateModel);
        poseStack.popPose();
    }

    private void submitMushroomBlock(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, boolean bl, int j, BlockState blockState, int k, BlockStateModel blockStateModel) {
        if (bl) {
            submitNodeCollector.submitBlockModel(poseStack, RenderTypes.outline(TextureAtlas.LOCATION_BLOCKS), blockStateModel, 0.0f, 0.0f, 0.0f, i, k, j);
        } else {
            submitNodeCollector.submitBlock(poseStack, blockState, i, k, j);
        }
    }
}

