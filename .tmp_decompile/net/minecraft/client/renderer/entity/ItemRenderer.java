/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(value=EnvType.CLIENT)
public class ItemRenderer {
    public static final Identifier ENCHANTED_GLINT_ARMOR = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_armor.png");
    public static final Identifier ENCHANTED_GLINT_ITEM = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    public static final float SPECIAL_FOIL_UI_SCALE = 0.5f;
    public static final float SPECIAL_FOIL_FIRST_PERSON_SCALE = 0.75f;
    public static final float SPECIAL_FOIL_TEXTURE_SCALE = 0.0078125f;
    public static final int NO_TINT = -1;

    public static void renderItem(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
        VertexConsumer vertexConsumer;
        if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
            PoseStack.Pose pose = poseStack.last().copy();
            if (itemDisplayContext == ItemDisplayContext.GUI) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.5f);
            } else if (itemDisplayContext.firstPerson()) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.75f);
            }
            vertexConsumer = ItemRenderer.getSpecialFoilBuffer(multiBufferSource, renderType, pose);
        } else {
            vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, renderType, true, foilType != ItemStackRenderState.FoilType.NONE);
        }
        ItemRenderer.renderQuadList(poseStack, vertexConsumer, list, is, i, j);
    }

    private static VertexConsumer getSpecialFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create((VertexConsumer)new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(ItemRenderer.useTransparentGlint(renderType) ? RenderTypes.glintTranslucent() : RenderTypes.glint()), pose, 0.0078125f), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            if (ItemRenderer.useTransparentGlint(renderType)) {
                return VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderTypes.glintTranslucent()), multiBufferSource.getBuffer(renderType));
            }
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderTypes.glint() : RenderTypes.entityGlint()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    public static List<RenderType> getFoilRenderTypes(RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            if (ItemRenderer.useTransparentGlint(renderType)) {
                return List.of((Object)renderType, (Object)RenderTypes.glintTranslucent());
            }
            return List.of((Object)renderType, (Object)(bl ? RenderTypes.glint() : RenderTypes.entityGlint()));
        }
        return List.of((Object)renderType);
    }

    private static boolean useTransparentGlint(RenderType renderType) {
        return Minecraft.useShaderTransparency() && (renderType == Sheets.translucentItemSheet() || renderType == Sheets.translucentBlockItemSheet());
    }

    private static int getLayerColorSafe(int[] is, int i) {
        if (i < 0 || i >= is.length) {
            return -1;
        }
        return is[i];
    }

    private static void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, int[] is, int i, int j) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad bakedQuad : list) {
            float l;
            float h;
            float g;
            float f;
            if (bakedQuad.isTinted()) {
                int k = ItemRenderer.getLayerColorSafe(is, bakedQuad.tintIndex());
                f = (float)ARGB.alpha(k) / 255.0f;
                g = (float)ARGB.red(k) / 255.0f;
                h = (float)ARGB.green(k) / 255.0f;
                l = (float)ARGB.blue(k) / 255.0f;
            } else {
                f = 1.0f;
                g = 1.0f;
                h = 1.0f;
                l = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, g, h, l, f, i, j);
        }
    }
}

