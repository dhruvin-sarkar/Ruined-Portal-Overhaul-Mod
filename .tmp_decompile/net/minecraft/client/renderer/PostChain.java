/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.UniformType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PostChain
implements AutoCloseable {
    public static final Identifier MAIN_TARGET_ID = Identifier.withDefaultNamespace("main");
    private final List<PostPass> passes;
    private final Map<Identifier, PostChainConfig.InternalTarget> internalTargets;
    private final Set<Identifier> externalTargets;
    private final Map<Identifier, RenderTarget> persistentTargets = new HashMap<Identifier, RenderTarget>();
    private final CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer;

    private PostChain(List<PostPass> list, Map<Identifier, PostChainConfig.InternalTarget> map, Set<Identifier> set, CachedOrthoProjectionMatrixBuffer cachedOrthoProjectionMatrixBuffer) {
        this.passes = list;
        this.internalTargets = map;
        this.externalTargets = set;
        this.projectionMatrixBuffer = cachedOrthoProjectionMatrixBuffer;
    }

    public static PostChain load(PostChainConfig postChainConfig, TextureManager textureManager, Set<Identifier> set, Identifier identifier2, CachedOrthoProjectionMatrixBuffer cachedOrthoProjectionMatrixBuffer) throws ShaderManager.CompilationException {
        Stream stream = postChainConfig.passes().stream().flatMap(PostChainConfig.Pass::referencedTargets);
        Set<Identifier> set2 = stream.filter(identifier -> !postChainConfig.internalTargets().containsKey(identifier)).collect(Collectors.toSet());
        Sets.SetView set3 = Sets.difference(set2, set);
        if (!set3.isEmpty()) {
            throw new ShaderManager.CompilationException("Referenced external targets are not available in this context: " + String.valueOf(set3));
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int i = 0; i < postChainConfig.passes().size(); ++i) {
            PostChainConfig.Pass pass = postChainConfig.passes().get(i);
            builder.add((Object)PostChain.createPass(textureManager, pass, identifier2.withSuffix("/" + i)));
        }
        return new PostChain((List<PostPass>)builder.build(), postChainConfig.internalTargets(), set2, cachedOrthoProjectionMatrixBuffer);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static PostPass createPass(TextureManager textureManager, PostChainConfig.Pass pass, Identifier identifier) throws ShaderManager.CompilationException {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET).withFragmentShader(pass.fragmentShaderId()).withVertexShader(pass.vertexShaderId()).withLocation(identifier);
        for (PostChainConfig.Input input : pass.inputs()) {
            builder.withSampler(input.samplerName() + "Sampler");
        }
        builder.withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER);
        for (String string2 : pass.uniforms().keySet()) {
            builder.withUniform(string2, UniformType.UNIFORM_BUFFER);
        }
        RenderPipeline renderPipeline = builder.build();
        ArrayList<PostPass.Input> list = new ArrayList<PostPass.Input>();
        Iterator<PostChainConfig.Input> iterator = pass.inputs().iterator();
        block9: while (true) {
            PostChainConfig.Input input;
            if (!iterator.hasNext()) {
                return new PostPass(renderPipeline, pass.outputTarget(), pass.uniforms(), list);
            }
            PostChainConfig.Input input2 = iterator.next();
            Objects.requireNonNull(input2);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PostChainConfig.TextureInput.class, PostChainConfig.TargetInput.class}, (Object)input, (int)n)) {
                case 0: {
                    boolean bl2;
                    Object object;
                    PostChainConfig.TextureInput textureInput = (PostChainConfig.TextureInput)input;
                    Object string2 = object = textureInput.samplerName();
                    Object identifier2 = object = textureInput.location();
                    boolean i = bl2 = textureInput.width();
                    boolean j = bl2 = textureInput.height();
                    boolean bl3 = bl2 = (boolean)textureInput.bilinear();
                    AbstractTexture abstractTexture = textureManager.getTexture(((Identifier)identifier2).withPath(string -> "textures/effect/" + string + ".png"));
                    list.add(new PostPass.TextureInput((String)string2, abstractTexture, i ? 1 : 0, j ? 1 : 0, bl3));
                    continue block9;
                }
                case 1: {
                    Object object = (PostChainConfig.TargetInput)input;
                    try {
                        boolean bl;
                        Object object2 = ((PostChainConfig.TargetInput)object).samplerName();
                        String string3 = object2;
                        Object identifier3 = object2 = ((PostChainConfig.TargetInput)object).targetId();
                        boolean bl2 = bl = ((PostChainConfig.TargetInput)object).useDepthBuffer();
                        boolean bl3 = bl = ((PostChainConfig.TargetInput)object).bilinear();
                        list.add(new PostPass.TargetInput(string3, (Identifier)identifier3, bl2, bl3));
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                    continue block9;
                }
            }
            break;
        }
        throw new MatchException(null, null);
    }

    public void addToFrame(FrameGraphBuilder frameGraphBuilder, int i, int j, TargetBundle targetBundle) {
        GpuBufferSlice gpuBufferSlice = this.projectionMatrixBuffer.getBuffer(i, j);
        HashMap<Identifier, ResourceHandle<RenderTarget>> map = new HashMap<Identifier, ResourceHandle<RenderTarget>>(this.internalTargets.size() + this.externalTargets.size());
        for (Identifier identifier : this.externalTargets) {
            map.put(identifier, targetBundle.getOrThrow(identifier));
        }
        for (Map.Entry entry : this.internalTargets.entrySet()) {
            Identifier identifier2 = (Identifier)entry.getKey();
            PostChainConfig.InternalTarget internalTarget = (PostChainConfig.InternalTarget)((Object)entry.getValue());
            RenderTargetDescriptor renderTargetDescriptor = new RenderTargetDescriptor(internalTarget.width().orElse(i), internalTarget.height().orElse(j), true, internalTarget.clearColor());
            if (internalTarget.persistent()) {
                RenderTarget renderTarget = this.getOrCreatePersistentTarget(identifier2, renderTargetDescriptor);
                map.put(identifier2, frameGraphBuilder.importExternal(identifier2.toString(), renderTarget));
                continue;
            }
            map.put(identifier2, frameGraphBuilder.createInternal(identifier2.toString(), renderTargetDescriptor));
        }
        for (PostPass postPass : this.passes) {
            postPass.addToFrame(frameGraphBuilder, map, gpuBufferSlice);
        }
        for (Identifier identifier : this.externalTargets) {
            targetBundle.replace(identifier, (ResourceHandle)map.get(identifier));
        }
    }

    @Deprecated
    public void process(RenderTarget renderTarget, GraphicsResourceAllocator graphicsResourceAllocator) {
        FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
        TargetBundle targetBundle = TargetBundle.of(MAIN_TARGET_ID, frameGraphBuilder.importExternal("main", renderTarget));
        this.addToFrame(frameGraphBuilder, renderTarget.width, renderTarget.height, targetBundle);
        frameGraphBuilder.execute(graphicsResourceAllocator);
    }

    private RenderTarget getOrCreatePersistentTarget(Identifier identifier, RenderTargetDescriptor renderTargetDescriptor) {
        RenderTarget renderTarget = this.persistentTargets.get(identifier);
        if (renderTarget == null || renderTarget.width != renderTargetDescriptor.width() || renderTarget.height != renderTargetDescriptor.height()) {
            if (renderTarget != null) {
                renderTarget.destroyBuffers();
            }
            renderTarget = renderTargetDescriptor.allocate();
            renderTargetDescriptor.prepare(renderTarget);
            this.persistentTargets.put(identifier, renderTarget);
        }
        return renderTarget;
    }

    @Override
    public void close() {
        this.persistentTargets.values().forEach(RenderTarget::destroyBuffers);
        this.persistentTargets.clear();
        for (PostPass postPass : this.passes) {
            postPass.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface TargetBundle {
        public static TargetBundle of(final Identifier identifier, final ResourceHandle<RenderTarget> resourceHandle) {
            return new TargetBundle(){
                private ResourceHandle<RenderTarget> handle;
                {
                    this.handle = resourceHandle;
                }

                @Override
                public void replace(Identifier identifier2, ResourceHandle<RenderTarget> resourceHandle2) {
                    if (!identifier2.equals(identifier)) {
                        throw new IllegalArgumentException("No target with id " + String.valueOf(identifier2));
                    }
                    this.handle = resourceHandle2;
                }

                @Override
                public @Nullable ResourceHandle<RenderTarget> get(Identifier identifier2) {
                    return identifier2.equals(identifier) ? this.handle : null;
                }
            };
        }

        public void replace(Identifier var1, ResourceHandle<RenderTarget> var2);

        public @Nullable ResourceHandle<RenderTarget> get(Identifier var1);

        default public ResourceHandle<RenderTarget> getOrThrow(Identifier identifier) {
            ResourceHandle<RenderTarget> resourceHandle = this.get(identifier);
            if (resourceHandle == null) {
                throw new IllegalArgumentException("Missing target with id " + String.valueOf(identifier));
            }
            return resourceHandle;
        }
    }
}

