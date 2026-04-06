/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.system.MemoryStack
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public class PostPass
implements AutoCloseable {
    private static final int UBO_SIZE_PER_SAMPLER = new Std140SizeCalculator().putVec2().get();
    private final String name;
    private final RenderPipeline pipeline;
    private final Identifier outputTargetId;
    private final Map<String, GpuBuffer> customUniforms = new HashMap<String, GpuBuffer>();
    private final MappableRingBuffer infoUbo;
    private final List<Input> inputs;

    public PostPass(RenderPipeline renderPipeline, Identifier identifier, Map<String, List<UniformValue>> map, List<Input> list) {
        this.pipeline = renderPipeline;
        this.name = renderPipeline.getLocation().toString();
        this.outputTargetId = identifier;
        this.inputs = list;
        for (Map.Entry<String, List<UniformValue>> entry : map.entrySet()) {
            List<UniformValue> list2 = entry.getValue();
            if (list2.isEmpty()) continue;
            Std140SizeCalculator std140SizeCalculator = new Std140SizeCalculator();
            for (UniformValue uniformValue : list2) {
                uniformValue.addSize(std140SizeCalculator);
            }
            int i = std140SizeCalculator.get();
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                Std140Builder std140Builder = Std140Builder.onStack(memoryStack, i);
                for (UniformValue uniformValue2 : list2) {
                    uniformValue2.writeTo(std140Builder);
                }
                this.customUniforms.put(entry.getKey(), RenderSystem.getDevice().createBuffer(() -> this.name + " / " + (String)entry.getKey(), 128, std140Builder.get()));
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
        this.infoUbo = new MappableRingBuffer(() -> this.name + " SamplerInfo", 130, (list.size() + 1) * UBO_SIZE_PER_SAMPLER);
    }

    public void addToFrame(FrameGraphBuilder frameGraphBuilder, Map<Identifier, ResourceHandle<RenderTarget>> map, GpuBufferSlice gpuBufferSlice) {
        FramePass framePass = frameGraphBuilder.addPass(this.name);
        for (Input input : this.inputs) {
            input.addToPass(framePass, map);
        }
        ResourceHandle resourceHandle2 = map.computeIfPresent(this.outputTargetId, (identifier, resourceHandle) -> framePass.readsAndWrites(resourceHandle));
        if (resourceHandle2 == null) {
            throw new IllegalStateException("Missing handle for target " + String.valueOf(this.outputTargetId));
        }
        framePass.executes(() -> {
            RenderTarget renderTarget = (RenderTarget)resourceHandle2.get();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(gpuBufferSlice, ProjectionType.ORTHOGRAPHIC);
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            SamplerCache samplerCache = RenderSystem.getSamplerCache();
            List list = this.inputs.stream().map(input -> new InputTexture(input.samplerName(), input.texture(map), samplerCache.getClampToEdge(input.bilinear() ? FilterMode.LINEAR : FilterMode.NEAREST))).toList();
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.infoUbo.currentBuffer(), false, true);){
                Iterator std140Builder = Std140Builder.intoBuffer(mappedView.data());
                ((Std140Builder)((Object)std140Builder)).putVec2(renderTarget.width, renderTarget.height);
                for (InputTexture inputTexture : list) {
                    ((Std140Builder)((Object)std140Builder)).putVec2(inputTexture.view.getWidth(0), inputTexture.view.getHeight(0));
                }
            }
            try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Post pass " + this.name, renderTarget.getColorTextureView(), OptionalInt.empty(), renderTarget.useDepth ? renderTarget.getDepthTextureView() : null, OptionalDouble.empty());){
                renderPass.setPipeline(this.pipeline);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("SamplerInfo", this.infoUbo.currentBuffer());
                for (Map.Entry entry : this.customUniforms.entrySet()) {
                    renderPass.setUniform((String)entry.getKey(), (GpuBuffer)entry.getValue());
                }
                for (InputTexture inputTexture : list) {
                    renderPass.bindTexture(inputTexture.samplerName() + "Sampler", inputTexture.view(), inputTexture.sampler());
                }
                renderPass.draw(0, 3);
            }
            this.infoUbo.rotate();
            RenderSystem.restoreProjectionMatrix();
            for (Input input2 : this.inputs) {
                input2.cleanup(map);
            }
        });
    }

    @Override
    public void close() {
        for (GpuBuffer gpuBuffer : this.customUniforms.values()) {
            gpuBuffer.close();
        }
        this.infoUbo.close();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Input {
        public void addToPass(FramePass var1, Map<Identifier, ResourceHandle<RenderTarget>> var2);

        default public void cleanup(Map<Identifier, ResourceHandle<RenderTarget>> map) {
        }

        public GpuTextureView texture(Map<Identifier, ResourceHandle<RenderTarget>> var1);

        public String samplerName();

        public boolean bilinear();
    }

    @Environment(value=EnvType.CLIENT)
    static final class InputTexture
    extends Record {
        private final String samplerName;
        final GpuTextureView view;
        private final GpuSampler sampler;

        InputTexture(String string, GpuTextureView gpuTextureView, GpuSampler gpuSampler) {
            this.samplerName = string;
            this.view = gpuTextureView;
            this.sampler = gpuSampler;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InputTexture.class, "samplerName;view;sampler", "samplerName", "view", "sampler"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InputTexture.class, "samplerName;view;sampler", "samplerName", "view", "sampler"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InputTexture.class, "samplerName;view;sampler", "samplerName", "view", "sampler"}, this, object);
        }

        public String samplerName() {
            return this.samplerName;
        }

        public GpuTextureView view() {
            return this.view;
        }

        public GpuSampler sampler() {
            return this.sampler;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TargetInput(String samplerName, Identifier targetId, boolean depthBuffer, boolean bilinear) implements Input
    {
        private ResourceHandle<RenderTarget> getHandle(Map<Identifier, ResourceHandle<RenderTarget>> map) {
            ResourceHandle<RenderTarget> resourceHandle = map.get(this.targetId);
            if (resourceHandle == null) {
                throw new IllegalStateException("Missing handle for target " + String.valueOf(this.targetId));
            }
            return resourceHandle;
        }

        @Override
        public void addToPass(FramePass framePass, Map<Identifier, ResourceHandle<RenderTarget>> map) {
            framePass.reads(this.getHandle(map));
        }

        @Override
        public GpuTextureView texture(Map<Identifier, ResourceHandle<RenderTarget>> map) {
            GpuTextureView gpuTextureView;
            ResourceHandle<RenderTarget> resourceHandle = this.getHandle(map);
            RenderTarget renderTarget = resourceHandle.get();
            GpuTextureView gpuTextureView2 = gpuTextureView = this.depthBuffer ? renderTarget.getDepthTextureView() : renderTarget.getColorTextureView();
            if (gpuTextureView == null) {
                throw new IllegalStateException("Missing " + (this.depthBuffer ? "depth" : "color") + "texture for target " + String.valueOf(this.targetId));
            }
            return gpuTextureView;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TextureInput(String samplerName, AbstractTexture texture, int width, int height, boolean bilinear) implements Input
    {
        @Override
        public void addToPass(FramePass framePass, Map<Identifier, ResourceHandle<RenderTarget>> map) {
        }

        @Override
        public GpuTextureView texture(Map<Identifier, ResourceHandle<RenderTarget>> map) {
            return this.texture.getTextureView();
        }
    }
}

