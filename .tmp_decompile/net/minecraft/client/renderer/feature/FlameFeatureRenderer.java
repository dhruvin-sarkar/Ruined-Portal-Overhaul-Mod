/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.ModelBakery;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class FlameFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, AtlasManager atlasManager) {
        for (SubmitNodeStorage.FlameSubmit flameSubmit : submitNodeCollection.getFlameSubmits()) {
            this.renderFlame(flameSubmit.pose(), bufferSource, flameSubmit.entityRenderState(), flameSubmit.rotation(), atlasManager);
        }
    }

    private void renderFlame(PoseStack.Pose pose, MultiBufferSource multiBufferSource, EntityRenderState entityRenderState, Quaternionf quaternionf, AtlasManager atlasManager) {
        TextureAtlasSprite textureAtlasSprite = atlasManager.get(ModelBakery.FIRE_0);
        TextureAtlasSprite textureAtlasSprite2 = atlasManager.get(ModelBakery.FIRE_1);
        float f = entityRenderState.boundingBoxWidth * 1.4f;
        pose.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = entityRenderState.boundingBoxHeight / f;
        float j = 0.0f;
        pose.rotate((Quaternionfc)quaternionf);
        pose.translate(0.0f, 0.0f, 0.3f - (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());
        while (i > 0.0f) {
            TextureAtlasSprite textureAtlasSprite3 = l % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
            float m = textureAtlasSprite3.getU0();
            float n = textureAtlasSprite3.getV0();
            float o = textureAtlasSprite3.getU1();
            float p = textureAtlasSprite3.getV1();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            FlameFeatureRenderer.fireVertex(pose, vertexConsumer, -g - 0.0f, 0.0f - j, k, o, p);
            FlameFeatureRenderer.fireVertex(pose, vertexConsumer, g - 0.0f, 0.0f - j, k, m, p);
            FlameFeatureRenderer.fireVertex(pose, vertexConsumer, g - 0.0f, 1.4f - j, k, m, n);
            FlameFeatureRenderer.fireVertex(pose, vertexConsumer, -g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k -= 0.03f;
            ++l;
        }
    }

    private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j) {
        vertexConsumer.addVertex(pose, f, g, h).setColor(-1).setUv(i, j).setUv1(0, 10).setLight(240).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}

