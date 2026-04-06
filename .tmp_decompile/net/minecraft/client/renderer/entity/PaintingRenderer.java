/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.Level;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class PaintingRenderer
extends EntityRenderer<Painting, PaintingRenderState> {
    private static final Identifier BACK_SPRITE_LOCATION = Identifier.withDefaultNamespace("back");
    private final TextureAtlas paintingsAtlas;

    public PaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.paintingsAtlas = context.getAtlas(AtlasIds.PAINTINGS);
    }

    @Override
    public void submit(PaintingRenderState paintingRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        PaintingVariant paintingVariant = paintingRenderState.variant;
        if (paintingVariant == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180 - paintingRenderState.direction.get2DDataValue() * 90));
        TextureAtlasSprite textureAtlasSprite = this.paintingsAtlas.getSprite(paintingVariant.assetId());
        TextureAtlasSprite textureAtlasSprite2 = this.paintingsAtlas.getSprite(BACK_SPRITE_LOCATION);
        this.renderPainting(poseStack, submitNodeCollector, RenderTypes.entitySolidZOffsetForward(textureAtlasSprite2.atlasLocation()), paintingRenderState.lightCoordsPerBlock, paintingVariant.width(), paintingVariant.height(), textureAtlasSprite, textureAtlasSprite2);
        poseStack.popPose();
        super.submit(paintingRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public PaintingRenderState createRenderState() {
        return new PaintingRenderState();
    }

    @Override
    public void extractRenderState(Painting painting, PaintingRenderState paintingRenderState, float f) {
        super.extractRenderState(painting, paintingRenderState, f);
        Direction direction = painting.getDirection();
        PaintingVariant paintingVariant = painting.getVariant().value();
        paintingRenderState.direction = direction;
        paintingRenderState.variant = paintingVariant;
        int i = paintingVariant.width();
        int j = paintingVariant.height();
        if (paintingRenderState.lightCoordsPerBlock.length != i * j) {
            paintingRenderState.lightCoordsPerBlock = new int[i * j];
        }
        float g = (float)(-i) / 2.0f;
        float h = (float)(-j) / 2.0f;
        Level level = painting.level();
        for (int k = 0; k < j; ++k) {
            for (int l = 0; l < i; ++l) {
                float m = (float)l + g + 0.5f;
                float n = (float)k + h + 0.5f;
                int o = painting.getBlockX();
                int p = Mth.floor(painting.getY() + (double)n);
                int q = painting.getBlockZ();
                switch (direction) {
                    case NORTH: {
                        o = Mth.floor(painting.getX() + (double)m);
                        break;
                    }
                    case WEST: {
                        q = Mth.floor(painting.getZ() - (double)m);
                        break;
                    }
                    case SOUTH: {
                        o = Mth.floor(painting.getX() - (double)m);
                        break;
                    }
                    case EAST: {
                        q = Mth.floor(painting.getZ() + (double)m);
                    }
                }
                paintingRenderState.lightCoordsPerBlock[l + k * i] = LevelRenderer.getLightColor(level, new BlockPos(o, p, q));
            }
        }
    }

    private void renderPainting(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int[] is, int i, int j, TextureAtlasSprite textureAtlasSprite, TextureAtlasSprite textureAtlasSprite2) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            float f = (float)(-i) / 2.0f;
            float g = (float)(-j) / 2.0f;
            float h = 0.03125f;
            float k = textureAtlasSprite2.getU0();
            float l = textureAtlasSprite2.getU1();
            float m = textureAtlasSprite2.getV0();
            float n = textureAtlasSprite2.getV1();
            float o = textureAtlasSprite2.getU0();
            float p = textureAtlasSprite2.getU1();
            float q = textureAtlasSprite2.getV0();
            float r = textureAtlasSprite2.getV(0.0625f);
            float s = textureAtlasSprite2.getU0();
            float t = textureAtlasSprite2.getU(0.0625f);
            float u = textureAtlasSprite2.getV0();
            float v = textureAtlasSprite2.getV1();
            double d = 1.0 / (double)i;
            double e = 1.0 / (double)j;
            for (int w = 0; w < i; ++w) {
                for (int x = 0; x < j; ++x) {
                    float y = f + (float)(w + 1);
                    float z = f + (float)w;
                    float aa = g + (float)(x + 1);
                    float ab = g + (float)x;
                    int ac = is[w + x * i];
                    float ad = textureAtlasSprite.getU((float)(d * (double)(i - w)));
                    float ae = textureAtlasSprite.getU((float)(d * (double)(i - (w + 1))));
                    float af = textureAtlasSprite.getV((float)(e * (double)(j - x)));
                    float ag = textureAtlasSprite.getV((float)(e * (double)(j - (x + 1))));
                    this.vertex(pose, vertexConsumer, y, ab, ae, af, -0.03125f, 0, 0, -1, ac);
                    this.vertex(pose, vertexConsumer, z, ab, ad, af, -0.03125f, 0, 0, -1, ac);
                    this.vertex(pose, vertexConsumer, z, aa, ad, ag, -0.03125f, 0, 0, -1, ac);
                    this.vertex(pose, vertexConsumer, y, aa, ae, ag, -0.03125f, 0, 0, -1, ac);
                    this.vertex(pose, vertexConsumer, y, aa, l, m, 0.03125f, 0, 0, 1, ac);
                    this.vertex(pose, vertexConsumer, z, aa, k, m, 0.03125f, 0, 0, 1, ac);
                    this.vertex(pose, vertexConsumer, z, ab, k, n, 0.03125f, 0, 0, 1, ac);
                    this.vertex(pose, vertexConsumer, y, ab, l, n, 0.03125f, 0, 0, 1, ac);
                    this.vertex(pose, vertexConsumer, y, aa, o, q, -0.03125f, 0, 1, 0, ac);
                    this.vertex(pose, vertexConsumer, z, aa, p, q, -0.03125f, 0, 1, 0, ac);
                    this.vertex(pose, vertexConsumer, z, aa, p, r, 0.03125f, 0, 1, 0, ac);
                    this.vertex(pose, vertexConsumer, y, aa, o, r, 0.03125f, 0, 1, 0, ac);
                    this.vertex(pose, vertexConsumer, y, ab, o, q, 0.03125f, 0, -1, 0, ac);
                    this.vertex(pose, vertexConsumer, z, ab, p, q, 0.03125f, 0, -1, 0, ac);
                    this.vertex(pose, vertexConsumer, z, ab, p, r, -0.03125f, 0, -1, 0, ac);
                    this.vertex(pose, vertexConsumer, y, ab, o, r, -0.03125f, 0, -1, 0, ac);
                    this.vertex(pose, vertexConsumer, y, aa, t, u, 0.03125f, -1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, y, ab, t, v, 0.03125f, -1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, y, ab, s, v, -0.03125f, -1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, y, aa, s, u, -0.03125f, -1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, z, aa, t, u, -0.03125f, 1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, z, ab, t, v, -0.03125f, 1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, z, ab, s, v, 0.03125f, 1, 0, 0, ac);
                    this.vertex(pose, vertexConsumer, z, aa, s, u, 0.03125f, 1, 0, 0, ac);
                }
            }
        });
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, int k, int l, int m, int n) {
        vertexConsumer.addVertex(pose, f, g, j).setColor(-1).setUv(h, i).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, k, l, m);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

