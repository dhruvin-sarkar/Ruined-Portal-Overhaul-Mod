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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EnchantTableRenderer
implements BlockEntityRenderer<EnchantingTableBlockEntity, EnchantTableRenderState> {
    public static final Material BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchanting_table_book");
    private final MaterialSet materials;
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    @Override
    public void extractRenderState(EnchantingTableBlockEntity enchantingTableBlockEntity, EnchantTableRenderState enchantTableRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        float g;
        BlockEntityRenderer.super.extractRenderState(enchantingTableBlockEntity, enchantTableRenderState, f, vec3, crumblingOverlay);
        enchantTableRenderState.flip = Mth.lerp(f, enchantingTableBlockEntity.oFlip, enchantingTableBlockEntity.flip);
        enchantTableRenderState.open = Mth.lerp(f, enchantingTableBlockEntity.oOpen, enchantingTableBlockEntity.open);
        enchantTableRenderState.time = (float)enchantingTableBlockEntity.time + f;
        for (g = enchantingTableBlockEntity.rot - enchantingTableBlockEntity.oRot; g >= (float)Math.PI; g -= (float)Math.PI * 2) {
        }
        while (g < (float)(-Math.PI)) {
            g += (float)Math.PI * 2;
        }
        enchantTableRenderState.yRot = enchantingTableBlockEntity.oRot + g * f;
    }

    @Override
    public void submit(EnchantTableRenderState enchantTableRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);
        poseStack.translate(0.0f, 0.1f + Mth.sin(enchantTableRenderState.time * 0.1f) * 0.01f, 0.0f);
        float f = enchantTableRenderState.yRot;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotation(-f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(80.0f));
        float g = Mth.frac(enchantTableRenderState.flip + 0.25f) * 1.6f - 0.3f;
        float h = Mth.frac(enchantTableRenderState.flip + 0.75f) * 1.6f - 0.3f;
        BookModel.State state = new BookModel.State(enchantTableRenderState.time, Mth.clamp(g, 0.0f, 1.0f), Mth.clamp(h, 0.0f, 1.0f), enchantTableRenderState.open);
        submitNodeCollector.submitModel(this.bookModel, state, poseStack, BOOK_TEXTURE.renderType(RenderTypes::entitySolid), enchantTableRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, this.materials.get(BOOK_TEXTURE), 0, enchantTableRenderState.breakProgress);
        poseStack.popPose();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

