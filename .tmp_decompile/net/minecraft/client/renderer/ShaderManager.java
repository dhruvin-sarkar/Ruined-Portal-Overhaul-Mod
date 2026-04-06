/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.io.IOUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.IdentifierException;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ShaderManager
extends SimplePreparableReloadListener<Configs>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_LOG_LENGTH = 32768;
    public static final String SHADER_PATH = "shaders";
    private static final String SHADER_INCLUDE_PATH = "shaders/include/";
    private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
    final TextureManager textureManager;
    private final Consumer<Exception> recoveryHandler;
    private CompilationCache compilationCache = new CompilationCache(Configs.EMPTY);
    final CachedOrthoProjectionMatrixBuffer postChainProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("post", 0.1f, 1000.0f, false);

    public ShaderManager(TextureManager textureManager, Consumer<Exception> consumer) {
        this.textureManager = textureManager;
        this.recoveryHandler = consumer;
    }

    @Override
    protected Configs prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        Map<Identifier, Resource> map = resourceManager.listResources(SHADER_PATH, ShaderManager::isShader);
        for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
            Identifier identifier = entry.getKey();
            ShaderType shaderType = ShaderType.byLocation(identifier);
            if (shaderType == null) continue;
            ShaderManager.loadShader(identifier, entry.getValue(), shaderType, map, (ImmutableMap.Builder<ShaderSourceKey, String>)builder);
        }
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (Map.Entry<Identifier, Resource> entry2 : POST_CHAIN_ID_CONVERTER.listMatchingResources(resourceManager).entrySet()) {
            ShaderManager.loadPostChain(entry2.getKey(), entry2.getValue(), (ImmutableMap.Builder<Identifier, PostChainConfig>)builder2);
        }
        return new Configs((Map<ShaderSourceKey, String>)builder.build(), (Map<Identifier, PostChainConfig>)builder2.build());
    }

    private static void loadShader(Identifier identifier, Resource resource, ShaderType shaderType, Map<Identifier, Resource> map, ImmutableMap.Builder<ShaderSourceKey, String> builder) {
        Identifier identifier2 = shaderType.idConverter().fileToId(identifier);
        GlslPreprocessor glslPreprocessor = ShaderManager.createPreprocessor(map, identifier);
        try (BufferedReader reader = resource.openAsReader();){
            String string = IOUtils.toString((Reader)reader);
            builder.put((Object)new ShaderSourceKey(identifier2, shaderType), (Object)String.join((CharSequence)"", glslPreprocessor.process(string)));
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to load shader source at {}", (Object)identifier, (Object)iOException);
        }
    }

    private static GlslPreprocessor createPreprocessor(final Map<Identifier, Resource> map, Identifier identifier) {
        final Identifier identifier2 = identifier.withPath(FileUtil::getFullResourcePath);
        return new GlslPreprocessor(){
            private final Set<Identifier> importedLocations = new ObjectArraySet();

            @Override
            public @Nullable String applyImport(boolean bl, String string) {
                String string3;
                block11: {
                    Identifier identifier;
                    try {
                        identifier = bl ? identifier2.withPath(string2 -> FileUtil.normalizeResourcePath(string2 + string)) : Identifier.parse(string).withPrefix(ShaderManager.SHADER_INCLUDE_PATH);
                    }
                    catch (IdentifierException identifierException) {
                        LOGGER.error("Malformed GLSL import {}: {}", (Object)string, (Object)identifierException.getMessage());
                        return "#error " + identifierException.getMessage();
                    }
                    if (!this.importedLocations.add(identifier)) {
                        return null;
                    }
                    BufferedReader reader = ((Resource)map.get(identifier)).openAsReader();
                    try {
                        string3 = IOUtils.toString((Reader)reader);
                        if (reader == null) break block11;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (reader != null) {
                                try {
                                    ((Reader)reader).close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (IOException iOException) {
                            LOGGER.error("Could not open GLSL import {}: {}", (Object)identifier, (Object)iOException.getMessage());
                            return "#error " + iOException.getMessage();
                        }
                    }
                    ((Reader)reader).close();
                }
                return string3;
            }
        };
    }

    private static void loadPostChain(Identifier identifier, Resource resource, ImmutableMap.Builder<Identifier, PostChainConfig> builder) {
        Identifier identifier2 = POST_CHAIN_ID_CONVERTER.fileToId(identifier);
        try (BufferedReader reader = resource.openAsReader();){
            JsonElement jsonElement = StrictJsonParser.parse(reader);
            builder.put((Object)identifier2, (Object)((PostChainConfig)((Object)PostChainConfig.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonSyntaxException::new))));
        }
        catch (JsonParseException | IOException exception) {
            LOGGER.error("Failed to parse post chain at {}", (Object)identifier, (Object)exception);
        }
    }

    private static boolean isShader(Identifier identifier) {
        return ShaderType.byLocation(identifier) != null || identifier.getPath().endsWith(".glsl");
    }

    @Override
    protected void apply(Configs configs, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        CompilationCache compilationCache = new CompilationCache(configs);
        HashSet<RenderPipeline> set = new HashSet<RenderPipeline>(RenderPipelines.getStaticPipelines());
        ArrayList<Identifier> list = new ArrayList<Identifier>();
        GpuDevice gpuDevice = RenderSystem.getDevice();
        gpuDevice.clearPipelineCache();
        for (RenderPipeline renderPipeline : set) {
            CompiledRenderPipeline compiledRenderPipeline = gpuDevice.precompilePipeline(renderPipeline, compilationCache::getShaderSource);
            if (compiledRenderPipeline.isValid()) continue;
            list.add(renderPipeline.getLocation());
        }
        if (!list.isEmpty()) {
            gpuDevice.clearPipelineCache();
            throw new RuntimeException("Failed to load required shader programs:\n" + list.stream().map(identifier -> " - " + String.valueOf(identifier)).collect(Collectors.joining("\n")));
        }
        this.compilationCache.close();
        this.compilationCache = compilationCache;
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void tryTriggerRecovery(Exception exception) {
        if (this.compilationCache.triggeredRecovery) {
            return;
        }
        this.recoveryHandler.accept(exception);
        this.compilationCache.triggeredRecovery = true;
    }

    public @Nullable PostChain getPostChain(Identifier identifier, Set<Identifier> set) {
        try {
            return this.compilationCache.getOrLoadPostChain(identifier, set);
        }
        catch (CompilationException compilationException) {
            LOGGER.error("Failed to load post chain: {}", (Object)identifier, (Object)compilationException);
            this.compilationCache.postChains.put(identifier, Optional.empty());
            this.tryTriggerRecovery(compilationException);
            return null;
        }
    }

    @Override
    public void close() {
        this.compilationCache.close();
        this.postChainProjectionMatrixBuffer.close();
    }

    public @Nullable String getShader(Identifier identifier, ShaderType shaderType) {
        return this.compilationCache.getShaderSource(identifier, shaderType);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    class CompilationCache
    implements AutoCloseable {
        private final Configs configs;
        final Map<Identifier, Optional<PostChain>> postChains = new HashMap<Identifier, Optional<PostChain>>();
        boolean triggeredRecovery;

        CompilationCache(Configs configs) {
            this.configs = configs;
        }

        public @Nullable PostChain getOrLoadPostChain(Identifier identifier, Set<Identifier> set) throws CompilationException {
            Optional<PostChain> optional = this.postChains.get(identifier);
            if (optional != null) {
                return optional.orElse(null);
            }
            PostChain postChain = this.loadPostChain(identifier, set);
            this.postChains.put(identifier, Optional.of(postChain));
            return postChain;
        }

        private PostChain loadPostChain(Identifier identifier, Set<Identifier> set) throws CompilationException {
            PostChainConfig postChainConfig = this.configs.postChains.get(identifier);
            if (postChainConfig == null) {
                throw new CompilationException("Could not find post chain with id: " + String.valueOf(identifier));
            }
            return PostChain.load(postChainConfig, ShaderManager.this.textureManager, set, identifier, ShaderManager.this.postChainProjectionMatrixBuffer);
        }

        @Override
        public void close() {
            this.postChains.values().forEach(optional -> optional.ifPresent(PostChain::close));
            this.postChains.clear();
        }

        public @Nullable String getShaderSource(Identifier identifier, ShaderType shaderType) {
            return this.configs.shaderSources.get((Object)new ShaderSourceKey(identifier, shaderType));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Configs
    extends Record {
        final Map<ShaderSourceKey, String> shaderSources;
        final Map<Identifier, PostChainConfig> postChains;
        public static final Configs EMPTY = new Configs(Map.of(), Map.of());

        public Configs(Map<ShaderSourceKey, String> map, Map<Identifier, PostChainConfig> map2) {
            this.shaderSources = map;
            this.postChains = map2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this, object);
        }

        public Map<ShaderSourceKey, String> shaderSources() {
            return this.shaderSources;
        }

        public Map<Identifier, PostChainConfig> postChains() {
            return this.postChains;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ShaderSourceKey(Identifier id, ShaderType type) {
        public String toString() {
            return String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CompilationException
    extends Exception {
        public CompilationException(String string) {
            super(string);
        }
    }
}

