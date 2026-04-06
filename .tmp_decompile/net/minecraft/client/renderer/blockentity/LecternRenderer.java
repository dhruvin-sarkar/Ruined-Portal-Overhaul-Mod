/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LecternRenderer
implements BlockEntityRenderer<LecternBlockEntity, LecternRenderState> {
    private final MaterialSet materials;
    private final BookModel bookModel;
    private final BookModel.State bookState = new BookModel.State(0.0f, 0.1f, 0.9f, 1.2f);

    public LecternRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public LecternRenderState createRenderState() {
        return new LecternRenderState();
    }

    @Override
    public void extractRenderState(LecternBlockEntity lecternBlockEntity, LecternRenderState lecternRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(lecternBlockEntity, lecternRenderState, f, vec3, crumblingOverlay);
        lecternRenderState.hasBook = lecternBlockEntity.getBlockState().getValue(LecternBlock.HAS_BOOK);
        lecternRenderState.yRot = lecternBlockEntity.getBlockState().getValue(LecternBlock.FACING).getClockWise().toYRot();
    }

    @Override
    public void submit(LecternRenderState lecternRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!lecternRenderState.hasBook) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 1.0625f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-lecternRenderState.yRot));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(67.5f));
        poseStack.translate(0.0f, -0.125f, 0.0f);
        submitNodeCollector.submitModel(this.bookModel, this.bookState, poseStack, EnchantTableRenderer.BOOK_TEXTURE.renderType(RenderTypes::entitySolid), lecternRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, this.materials.get(EnchantTableRenderer.BOOK_TEXTURE), 0, lecternRenderState.breakProgress);
        poseStack.popPose();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

