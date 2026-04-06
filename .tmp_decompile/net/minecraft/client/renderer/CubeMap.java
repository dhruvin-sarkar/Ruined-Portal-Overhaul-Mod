/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Environment(value=EnvType.CLIENT)
public class CubeMap
implements AutoCloseable {
    private static final int SIDES = 6;
    private final GpuBuffer vertexBuffer;
    private final CachedPerspectiveProjectionMatrixBuffer projectionMatrixUbo;
    private final Identifier location;

    public CubeMap(Identifier identifier) {
        this.location = identifier;
        this.projectionMatrixUbo = new CachedPerspectiveProjectionMatrixBuffer("cubemap", 0.05f, 10.0f);
        this.vertexBuffer = CubeMap.initializeVertices();
    }

    public void render(Minecraft minecraft, float f, float g) {
        RenderSystem.setProjectionMatrix(this.projectionMatrixUbo.getBuffer(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), 85.0f), ProjectionType.PERSPECTIVE);
        RenderPipeline renderPipeline = RenderPipelines.PANORAMA;
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView gpuTextureView = renderTarget.getColorTextureView();
        GpuTextureView gpuTextureView2 = renderTarget.getDepthTextureView();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(36);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.rotationX((float)Math.PI);
        matrix4fStack.rotateX(f * ((float)Math.PI / 180));
        matrix4fStack.rotateY(g * ((float)Math.PI / 180));
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)new Matrix4f((Matrix4fc)matrix4fStack), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        matrix4fStack.popMatrix();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Cubemap", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, this.vertexBuffer);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(this.location);
            renderPass.bindTexture("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
            renderPass.drawIndexed(0, 0, 36, 1);
        }
    }

    private static GpuBuffer initializeVertices() {
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 4 * 6);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferBuilder.addVertex(-1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, 1.0f);
            bufferBuilder.addVertex(-1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(-1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, 1.0f);
            bufferBuilder.addVertex(1.0f, -1.0f, -1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, 1.0f);
            bufferBuilder.addVertex(-1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, -1.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, 1.0f);
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Cube map vertex buffer", 32, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public void registerTextures(TextureManager textureManager) {
        textureManager.register(this.location, new CubeMapTexture(this.location));
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        this.projectionMatrixUbo.close();
    }
}

