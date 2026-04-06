/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ShadowFeatureRenderer {
    private static final RenderType SHADOW_RENDER_TYPE = RenderTypes.entityShadow(Identifier.withDefaultNamespace("textures/misc/shadow.png"));

    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(SHADOW_RENDER_TYPE);
        for (SubmitNodeStorage.ShadowSubmit shadowSubmit : submitNodeCollection.getShadowSubmits()) {
            for (EntityRenderState.ShadowPiece shadowPiece : shadowSubmit.pieces()) {
                AABB aABB = shadowPiece.shapeBelow().bounds();
                float f = shadowPiece.relativeX() + (float)aABB.minX;
                float g = shadowPiece.relativeX() + (float)aABB.maxX;
                float h = shadowPiece.relativeY() + (float)aABB.minY;
                float i = shadowPiece.relativeZ() + (float)aABB.minZ;
                float j = shadowPiece.relativeZ() + (float)aABB.maxZ;
                float k = shadowSubmit.radius();
                float l = -f / 2.0f / k + 0.5f;
                float m = -g / 2.0f / k + 0.5f;
                float n = -i / 2.0f / k + 0.5f;
                float o = -j / 2.0f / k + 0.5f;
                int p = ARGB.white(shadowPiece.alpha());
                ShadowFeatureRenderer.shadowVertex(shadowSubmit.pose(), vertexConsumer, p, f, h, i, l, n);
                ShadowFeatureRenderer.shadowVertex(shadowSubmit.pose(), vertexConsumer, p, f, h, j, l, o);
                ShadowFeatureRenderer.shadowVertex(shadowSubmit.pose(), vertexConsumer, p, g, h, j, m, o);
                ShadowFeatureRenderer.shadowVertex(shadowSubmit.pose(), vertexConsumer, p, g, h, i, m, n);
            }
        }
    }

    private static void shadowVertex(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, float f, float g, float h, float j, float k) {
        Vector3f vector3f = matrix4f.transformPosition(f, g, h, new Vector3f());
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), i, j, k, OverlayTexture.NO_OVERLAY, 0xF000F0, 0.0f, 1.0f, 0.0f);
    }
}

