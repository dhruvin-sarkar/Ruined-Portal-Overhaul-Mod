/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.state;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class QuadParticleRenderState
implements SubmitNodeCollector.ParticleGroupRenderer,
ParticleGroupRenderState {
    private static final int INITIAL_PARTICLE_CAPACITY = 1024;
    private static final int FLOATS_PER_PARTICLE = 12;
    private static final int INTS_PER_PARTICLE = 2;
    private final Map<SingleQuadParticle.Layer, Storage> particles = new HashMap<SingleQuadParticle.Layer, Storage>();
    private int particleCount;

    public void add(SingleQuadParticle.Layer layer2, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, int r, int s) {
        this.particles.computeIfAbsent(layer2, layer -> new Storage()).add(f, g, h, i, j, k, l, m, n, o, p, q, r, s);
        ++this.particleCount;
    }

    @Override
    public void clear() {
        this.particles.values().forEach(Storage::clear);
        this.particleCount = 0;
    }

    @Override
    public @Nullable PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache particleBufferCache) {
        int i2 = this.particleCount * 4;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(i2 * DefaultVertexFormat.PARTICLE.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            HashMap<SingleQuadParticle.Layer, PreparedLayer> map = new HashMap<SingleQuadParticle.Layer, PreparedLayer>();
            int j2 = 0;
            for (Map.Entry<SingleQuadParticle.Layer, Storage> entry : this.particles.entrySet()) {
                entry.getValue().forEachParticle((f, g, h, i, j, k, l, m, n, o, p, q, r, s) -> this.renderRotatedQuad(bufferBuilder, f, g, h, i, j, k, l, m, n, o, p, q, r, s));
                if (entry.getValue().count() > 0) {
                    map.put(entry.getKey(), new PreparedLayer(j2, entry.getValue().count() * 6));
                }
                j2 += entry.getValue().count() * 4;
            }
            MeshData meshData = bufferBuilder.build();
            if (meshData != null) {
                particleBufferCache.write(meshData.vertexBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(meshData.drawState().indexCount());
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
                PreparedBuffers preparedBuffers = new PreparedBuffers(meshData.drawState().indexCount(), gpuBufferSlice, map);
                return preparedBuffers;
            }
            PreparedBuffers preparedBuffers = null;
            return preparedBuffers;
        }
    }

    @Override
    public void render(PreparedBuffers preparedBuffers, ParticleFeatureRenderer.ParticleBufferCache particleBufferCache, RenderPass renderPass, TextureManager textureManager, boolean bl) {
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        renderPass.setVertexBuffer(0, particleBufferCache.get());
        renderPass.setIndexBuffer(autoStorageIndexBuffer.getBuffer(preparedBuffers.indexCount), autoStorageIndexBuffer.type());
        renderPass.setUniform("DynamicTransforms", preparedBuffers.dynamicTransforms);
        for (Map.Entry<SingleQuadParticle.Layer, PreparedLayer> entry : preparedBuffers.layers.entrySet()) {
            if (bl != entry.getKey().translucent()) continue;
            renderPass.setPipeline(entry.getKey().pipeline());
            AbstractTexture abstractTexture = textureManager.getTexture(entry.getKey().textureAtlasLocation());
            renderPass.bindTexture("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
            renderPass.drawIndexed(entry.getValue().vertexOffset, 0, entry.getValue().indexCount, 1);
        }
    }

    protected void renderRotatedQuad(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, int r, int s) {
        Quaternionf quaternionf = new Quaternionf(i, j, k, l);
        this.renderVertex(vertexConsumer, quaternionf, f, g, h, 1.0f, -1.0f, m, o, q, r, s);
        this.renderVertex(vertexConsumer, quaternionf, f, g, h, 1.0f, 1.0f, m, o, p, r, s);
        this.renderVertex(vertexConsumer, quaternionf, f, g, h, -1.0f, 1.0f, m, n, p, r, s);
        this.renderVertex(vertexConsumer, quaternionf, f, g, h, -1.0f, -1.0f, m, n, q, r, s);
    }

    private void renderVertex(VertexConsumer vertexConsumer, Quaternionf quaternionf, float f, float g, float h, float i, float j, float k, float l, float m, int n, int o) {
        Vector3f vector3f = new Vector3f(i, j, 0.0f).rotate((Quaternionfc)quaternionf).mul(k).add(f, g, h);
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(l, m).setColor(n).setLight(o);
    }

    @Override
    public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (this.particleCount > 0) {
            submitNodeCollector.submitParticleGroup(this);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Storage {
        private int capacity = 1024;
        private float[] floatValues = new float[12288];
        private int[] intValues = new int[2048];
        private int currentParticleIndex;

        Storage() {
        }

        public void add(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, int r, int s) {
            if (this.currentParticleIndex >= this.capacity) {
                this.grow();
            }
            int t = this.currentParticleIndex * 12;
            this.floatValues[t++] = f;
            this.floatValues[t++] = g;
            this.floatValues[t++] = h;
            this.floatValues[t++] = i;
            this.floatValues[t++] = j;
            this.floatValues[t++] = k;
            this.floatValues[t++] = l;
            this.floatValues[t++] = m;
            this.floatValues[t++] = n;
            this.floatValues[t++] = o;
            this.floatValues[t++] = p;
            this.floatValues[t] = q;
            t = this.currentParticleIndex * 2;
            this.intValues[t++] = r;
            this.intValues[t] = s;
            ++this.currentParticleIndex;
        }

        public void forEachParticle(ParticleConsumer particleConsumer) {
            for (int i = 0; i < this.currentParticleIndex; ++i) {
                int j = i * 12;
                int k = i * 2;
                particleConsumer.consume(this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j++], this.floatValues[j], this.intValues[k++], this.intValues[k]);
            }
        }

        public void clear() {
            this.currentParticleIndex = 0;
        }

        private void grow() {
            this.capacity *= 2;
            this.floatValues = Arrays.copyOf(this.floatValues, this.capacity * 12);
            this.intValues = Arrays.copyOf(this.intValues, this.capacity * 2);
        }

        public int count() {
            return this.currentParticleIndex;
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ParticleConsumer {
        public void consume(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, int var13, int var14);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class PreparedLayer
    extends Record {
        final int vertexOffset;
        final int indexCount;

        public PreparedLayer(int i, int j) {
            this.vertexOffset = i;
            this.indexCount = j;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PreparedLayer.class, "vertexOffset;indexCount", "vertexOffset", "indexCount"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PreparedLayer.class, "vertexOffset;indexCount", "vertexOffset", "indexCount"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PreparedLayer.class, "vertexOffset;indexCount", "vertexOffset", "indexCount"}, this, object);
        }

        public int vertexOffset() {
            return this.vertexOffset;
        }

        public int indexCount() {
            return this.indexCount;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class PreparedBuffers
    extends Record {
        final int indexCount;
        final GpuBufferSlice dynamicTransforms;
        final Map<SingleQuadParticle.Layer, PreparedLayer> layers;

        public PreparedBuffers(int i, GpuBufferSlice gpuBufferSlice, Map<SingleQuadParticle.Layer, PreparedLayer> map) {
            this.indexCount = i;
            this.dynamicTransforms = gpuBufferSlice;
            this.layers = map;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PreparedBuffers.class, "indexCount;dynamicTransforms;layers", "indexCount", "dynamicTransforms", "layers"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PreparedBuffers.class, "indexCount;dynamicTransforms;layers", "indexCount", "dynamicTransforms", "layers"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PreparedBuffers.class, "indexCount;dynamicTransforms;layers", "indexCount", "dynamicTransforms", "layers"}, this, object);
        }

        public int indexCount() {
            return this.indexCount;
        }

        public GpuBufferSlice dynamicTransforms() {
            return this.dynamicTransforms;
        }

        public Map<SingleQuadParticle.Layer, PreparedLayer> layers() {
            return this.layers;
        }
    }
}

