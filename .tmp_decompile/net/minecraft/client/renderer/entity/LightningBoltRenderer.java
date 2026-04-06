/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class LightningBoltRenderer
extends EntityRenderer<LightningBolt, LightningBoltRenderState> {
    public LightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(LightningBoltRenderState lightningBoltRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        float[] fs = new float[8];
        float[] gs = new float[8];
        float f = 0.0f;
        float g = 0.0f;
        RandomSource randomSource = RandomSource.create(lightningBoltRenderState.seed);
        for (int i = 7; i >= 0; --i) {
            fs[i] = f;
            gs[i] = g;
            f += (float)(randomSource.nextInt(11) - 5);
            g += (float)(randomSource.nextInt(11) - 5);
        }
        float h = f;
        float j = g;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, vertexConsumer) -> {
            Matrix4f matrix4f = pose.pose();
            for (int i = 0; i < 4; ++i) {
                RandomSource randomSource = RandomSource.create(lightningBoltRenderState.seed);
                for (int j = 0; j < 3; ++j) {
                    int k = 7;
                    int l = 0;
                    if (j > 0) {
                        k = 7 - j;
                    }
                    if (j > 0) {
                        l = k - 2;
                    }
                    float h = fs[k] - h;
                    float m = gs[k] - j;
                    for (int n = k; n >= l; --n) {
                        float o = h;
                        float p = m;
                        if (j == 0) {
                            h += (float)(randomSource.nextInt(11) - 5);
                            m += (float)(randomSource.nextInt(11) - 5);
                        } else {
                            h += (float)(randomSource.nextInt(31) - 15);
                            m += (float)(randomSource.nextInt(31) - 15);
                        }
                        float q = 0.5f;
                        float r = 0.45f;
                        float s = 0.45f;
                        float t = 0.5f;
                        float u = 0.1f + (float)i * 0.2f;
                        if (j == 0) {
                            u *= (float)n * 0.1f + 1.0f;
                        }
                        float v = 0.1f + (float)i * 0.2f;
                        if (j == 0) {
                            v *= ((float)n - 1.0f) * 0.1f + 1.0f;
                        }
                        LightningBoltRenderer.quad(matrix4f, vertexConsumer, h, m, n, o, p, 0.45f, 0.45f, 0.5f, u, v, false, false, true, false);
                        LightningBoltRenderer.quad(matrix4f, vertexConsumer, h, m, n, o, p, 0.45f, 0.45f, 0.5f, u, v, true, false, true, true);
                        LightningBoltRenderer.quad(matrix4f, vertexConsumer, h, m, n, o, p, 0.45f, 0.45f, 0.5f, u, v, true, true, false, true);
                        LightningBoltRenderer.quad(matrix4f, vertexConsumer, h, m, n, o, p, 0.45f, 0.45f, 0.5f, u, v, false, true, false, false);
                    }
                }
            }
        });
    }

    private static void quad(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, int i, float h, float j, float k, float l, float m, float n, float o, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        vertexConsumer.addVertex((Matrix4fc)matrix4f, f + (bl ? o : -o), (float)(i * 16), g + (bl2 ? o : -o)).setColor(k, l, m, 0.3f);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, h + (bl ? n : -n), (float)((i + 1) * 16), j + (bl2 ? n : -n)).setColor(k, l, m, 0.3f);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, h + (bl3 ? n : -n), (float)((i + 1) * 16), j + (bl4 ? n : -n)).setColor(k, l, m, 0.3f);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, f + (bl3 ? o : -o), (float)(i * 16), g + (bl4 ? o : -o)).setColor(k, l, m, 0.3f);
    }

    @Override
    public LightningBoltRenderState createRenderState() {
        return new LightningBoltRenderState();
    }

    @Override
    public void extractRenderState(LightningBolt lightningBolt, LightningBoltRenderState lightningBoltRenderState, float f) {
        super.extractRenderState(lightningBolt, lightningBoltRenderState, f);
        lightningBoltRenderState.seed = lightningBolt.seed;
    }

    @Override
    protected boolean affectedByCulling(LightningBolt lightningBolt) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((LightningBolt)entity);
    }
}

