/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class LeashFeatureRenderer {
    private static final int LEASH_RENDER_STEPS = 24;
    private static final float LEASH_WIDTH = 0.05f;

    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource) {
        for (SubmitNodeStorage.LeashSubmit leashSubmit : submitNodeCollection.getLeashSubmits()) {
            LeashFeatureRenderer.renderLeash(leashSubmit.pose(), bufferSource, leashSubmit.leashState());
        }
    }

    private static void renderLeash(Matrix4f matrix4f, MultiBufferSource multiBufferSource, EntityRenderState.LeashState leashState) {
        int l;
        float f = (float)(leashState.end.x - leashState.start.x);
        float g = (float)(leashState.end.y - leashState.start.y);
        float h = (float)(leashState.end.z - leashState.start.z);
        float i = Mth.invSqrt(f * f + h * h) * 0.05f / 2.0f;
        float j = h * i;
        float k = f * i;
        matrix4f.translate((float)leashState.offset.x, (float)leashState.offset.y, (float)leashState.offset.z);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.leash());
        for (l = 0; l <= 24; ++l) {
            LeashFeatureRenderer.addVertexPair(vertexConsumer, matrix4f, f, g, h, 0.05f, j, k, l, false, leashState);
        }
        for (l = 24; l >= 0; --l) {
            LeashFeatureRenderer.addVertexPair(vertexConsumer, matrix4f, f, g, h, 0.0f, j, k, l, true, leashState);
        }
    }

    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, float i, float j, float k, int l, boolean bl, EntityRenderState.LeashState leashState) {
        float m = (float)l / 24.0f;
        int n = (int)Mth.lerp(m, leashState.startBlockLight, leashState.endBlockLight);
        int o = (int)Mth.lerp(m, leashState.startSkyLight, leashState.endSkyLight);
        int p = LightTexture.pack(n, o);
        float q = l % 2 == (bl ? 1 : 0) ? 0.7f : 1.0f;
        float r = 0.5f * q;
        float s = 0.4f * q;
        float t = 0.3f * q;
        float u = f * m;
        float v = leashState.slack ? (g > 0.0f ? g * m * m : g - g * (1.0f - m) * (1.0f - m)) : g * m;
        float w = h * m;
        vertexConsumer.addVertex((Matrix4fc)matrix4f, u - j, v + i, w + k).setColor(r, s, t, 1.0f).setLight(p);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, u + j, v + 0.05f - i, w - k).setColor(r, s, t, 1.0f).setLight(p);
    }
}

