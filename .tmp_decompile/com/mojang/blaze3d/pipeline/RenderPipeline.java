/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class RenderPipeline {
    private final Identifier location;
    private final Identifier vertexShader;
    private final Identifier fragmentShader;
    private final ShaderDefines shaderDefines;
    private final List<String> samplers;
    private final List<UniformDescription> uniforms;
    private final DepthTestFunction depthTestFunction;
    private final PolygonMode polygonMode;
    private final boolean cull;
    private final LogicOp colorLogic;
    private final Optional<BlendFunction> blendFunction;
    private final boolean writeColor;
    private final boolean writeAlpha;
    private final boolean writeDepth;
    private final VertexFormat vertexFormat;
    private final VertexFormat.Mode vertexFormatMode;
    private final float depthBiasScaleFactor;
    private final float depthBiasConstant;
    private final int sortKey;
    private static int sortKeySeed;

    protected RenderPipeline(Identifier identifier, Identifier identifier2, Identifier identifier3, ShaderDefines shaderDefines, List<String> list, List<UniformDescription> list2, Optional<BlendFunction> optional, DepthTestFunction depthTestFunction, PolygonMode polygonMode, boolean bl, boolean bl2, boolean bl3, boolean bl4, LogicOp logicOp, VertexFormat vertexFormat, VertexFormat.Mode mode, float f, float g, int i) {
        this.location = identifier;
        this.vertexShader = identifier2;
        this.fragmentShader = identifier3;
        this.shaderDefines = shaderDefines;
        this.samplers = list;
        this.uniforms = list2;
        this.depthTestFunction = depthTestFunction;
        this.polygonMode = polygonMode;
        this.cull = bl;
        this.blendFunction = optional;
        this.writeColor = bl2;
        this.writeAlpha = bl3;
        this.writeDepth = bl4;
        this.colorLogic = logicOp;
        this.vertexFormat = vertexFormat;
        this.vertexFormatMode = mode;
        this.depthBiasScaleFactor = f;
        this.depthBiasConstant = g;
        this.sortKey = i;
    }

    public int getSortKey() {
        return SharedConstants.DEBUG_SHUFFLE_UI_RENDERING_ORDER ? super.hashCode() * (sortKeySeed + 1) : this.sortKey;
    }

    public static void updateSortKeySeed() {
        sortKeySeed = Math.round(100000.0f * (float)Math.random());
    }

    public String toString() {
        return this.location.toString();
    }

    public DepthTestFunction getDepthTestFunction() {
        return this.depthTestFunction;
    }

    public PolygonMode getPolygonMode() {
        return this.polygonMode;
    }

    public boolean isCull() {
        return this.cull;
    }

    public LogicOp getColorLogic() {
        return this.colorLogic;
    }

    public Optional<BlendFunction> getBlendFunction() {
        return this.blendFunction;
    }

    public boolean isWriteColor() {
        return this.writeColor;
    }

    public boolean isWriteAlpha() {
        return this.writeAlpha;
    }

    public boolean isWriteDepth() {
        return this.writeDepth;
    }

    public float getDepthBiasScaleFactor() {
        return this.depthBiasScaleFactor;
    }

    public float getDepthBiasConstant() {
        return this.depthBiasConstant;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public VertexFormat.Mode getVertexFormatMode() {
        return this.vertexFormatMode;
    }

    public Identifier getVertexShader() {
        return this.vertexShader;
    }

    public Identifier getFragmentShader() {
        return this.fragmentShader;
    }

    public ShaderDefines getShaderDefines() {
        return this.shaderDefines;
    }

    public List<String> getSamplers() {
        return this.samplers;
    }

    public List<UniformDescription> getUniforms() {
        return this.uniforms;
    }

    public boolean wantsDepthTexture() {
        return this.depthTestFunction != DepthTestFunction.NO_DEPTH_TEST || this.depthBiasConstant != 0.0f || this.depthBiasScaleFactor != 0.0f || this.writeDepth;
    }

    public static Builder builder(Snippet ... snippets) {
        Builder builder = new Builder();
        for (Snippet snippet : snippets) {
            builder.withSnippet(snippet);
        }
        return builder;
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public static class Builder {
        private static int nextPipelineSortKey;
        private Optional<Identifier> location = Optional.empty();
        private Optional<Identifier> fragmentShader = Optional.empty();
        private Optional<Identifier> vertexShader = Optional.empty();
        private Optional<ShaderDefines.Builder> definesBuilder = Optional.empty();
        private Optional<List<String>> samplers = Optional.empty();
        private Optional<List<UniformDescription>> uniforms = Optional.empty();
        private Optional<DepthTestFunction> depthTestFunction = Optional.empty();
        private Optional<PolygonMode> polygonMode = Optional.empty();
        private Optional<Boolean> cull = Optional.empty();
        private Optional<Boolean> writeColor = Optional.empty();
        private Optional<Boolean> writeAlpha = Optional.empty();
        private Optional<Boolean> writeDepth = Optional.empty();
        private Optional<LogicOp> colorLogic = Optional.empty();
        private Optional<BlendFunction> blendFunction = Optional.empty();
        private Optional<VertexFormat> vertexFormat = Optional.empty();
        private Optional<VertexFormat.Mode> vertexFormatMode = Optional.empty();
        private float depthBiasScaleFactor;
        private float depthBiasConstant;

        Builder() {
        }

        public Builder withLocation(String string) {
            this.location = Optional.of(Identifier.withDefaultNamespace(string));
            return this;
        }

        public Builder withLocation(Identifier identifier) {
            this.location = Optional.of(identifier);
            return this;
        }

        public Builder withFragmentShader(String string) {
            this.fragmentShader = Optional.of(Identifier.withDefaultNamespace(string));
            return this;
        }

        public Builder withFragmentShader(Identifier identifier) {
            this.fragmentShader = Optional.of(identifier);
            return this;
        }

        public Builder withVertexShader(String string) {
            this.vertexShader = Optional.of(Identifier.withDefaultNamespace(string));
            return this;
        }

        public Builder withVertexShader(Identifier identifier) {
            this.vertexShader = Optional.of(identifier);
            return this;
        }

        public Builder withShaderDefine(String string) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(string);
            return this;
        }

        public Builder withShaderDefine(String string, int i) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(string, i);
            return this;
        }

        public Builder withShaderDefine(String string, float f) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(string, f);
            return this;
        }

        public Builder withSampler(String string) {
            if (this.samplers.isEmpty()) {
                this.samplers = Optional.of(new ArrayList());
            }
            this.samplers.get().add(string);
            return this;
        }

        public Builder withUniform(String string, UniformType uniformType) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList());
            }
            if (uniformType == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Cannot use texel buffer without specifying texture format");
            }
            this.uniforms.get().add(new UniformDescription(string, uniformType));
            return this;
        }

        public Builder withUniform(String string, UniformType uniformType, TextureFormat textureFormat) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList());
            }
            if (uniformType != UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Only texel buffer can specify texture format");
            }
            this.uniforms.get().add(new UniformDescription(string, textureFormat));
            return this;
        }

        public Builder withDepthTestFunction(DepthTestFunction depthTestFunction) {
            this.depthTestFunction = Optional.of(depthTestFunction);
            return this;
        }

        public Builder withPolygonMode(PolygonMode polygonMode) {
            this.polygonMode = Optional.of(polygonMode);
            return this;
        }

        public Builder withCull(boolean bl) {
            this.cull = Optional.of(bl);
            return this;
        }

        public Builder withBlend(BlendFunction blendFunction) {
            this.blendFunction = Optional.of(blendFunction);
            return this;
        }

        public Builder withoutBlend() {
            this.blendFunction = Optional.empty();
            return this;
        }

        public Builder withColorWrite(boolean bl) {
            this.writeColor = Optional.of(bl);
            this.writeAlpha = Optional.of(bl);
            return this;
        }

        public Builder withColorWrite(boolean bl, boolean bl2) {
            this.writeColor = Optional.of(bl);
            this.writeAlpha = Optional.of(bl2);
            return this;
        }

        public Builder withDepthWrite(boolean bl) {
            this.writeDepth = Optional.of(bl);
            return this;
        }

        @Deprecated
        public Builder withColorLogic(LogicOp logicOp) {
            this.colorLogic = Optional.of(logicOp);
            return this;
        }

        public Builder withVertexFormat(VertexFormat vertexFormat, VertexFormat.Mode mode) {
            this.vertexFormat = Optional.of(vertexFormat);
            this.vertexFormatMode = Optional.of(mode);
            return this;
        }

        public Builder withDepthBias(float f, float g) {
            this.depthBiasScaleFactor = f;
            this.depthBiasConstant = g;
            return this;
        }

        void withSnippet(Snippet snippet) {
            if (snippet.vertexShader.isPresent()) {
                this.vertexShader = snippet.vertexShader;
            }
            if (snippet.fragmentShader.isPresent()) {
                this.fragmentShader = snippet.fragmentShader;
            }
            if (snippet.shaderDefines.isPresent()) {
                if (this.definesBuilder.isEmpty()) {
                    this.definesBuilder = Optional.of(ShaderDefines.builder());
                }
                ShaderDefines shaderDefines = snippet.shaderDefines.get();
                for (Map.Entry<String, String> entry : shaderDefines.values().entrySet()) {
                    this.definesBuilder.get().define(entry.getKey(), entry.getValue());
                }
                for (String string : shaderDefines.flags()) {
                    this.definesBuilder.get().define(string);
                }
            }
            snippet.samplers.ifPresent(list -> {
                if (this.samplers.isPresent()) {
                    this.samplers.get().addAll((Collection<String>)list);
                } else {
                    this.samplers = Optional.of(new ArrayList(list));
                }
            });
            snippet.uniforms.ifPresent(list -> {
                if (this.uniforms.isPresent()) {
                    this.uniforms.get().addAll((Collection<UniformDescription>)list);
                } else {
                    this.uniforms = Optional.of(new ArrayList(list));
                }
            });
            if (snippet.depthTestFunction.isPresent()) {
                this.depthTestFunction = snippet.depthTestFunction;
            }
            if (snippet.cull.isPresent()) {
                this.cull = snippet.cull;
            }
            if (snippet.writeColor.isPresent()) {
                this.writeColor = snippet.writeColor;
            }
            if (snippet.writeAlpha.isPresent()) {
                this.writeAlpha = snippet.writeAlpha;
            }
            if (snippet.writeDepth.isPresent()) {
                this.writeDepth = snippet.writeDepth;
            }
            if (snippet.colorLogic.isPresent()) {
                this.colorLogic = snippet.colorLogic;
            }
            if (snippet.blendFunction.isPresent()) {
                this.blendFunction = snippet.blendFunction;
            }
            if (snippet.vertexFormat.isPresent()) {
                this.vertexFormat = snippet.vertexFormat;
            }
            if (snippet.vertexFormatMode.isPresent()) {
                this.vertexFormatMode = snippet.vertexFormatMode;
            }
        }

        public Snippet buildSnippet() {
            return new Snippet(this.vertexShader, this.fragmentShader, this.definesBuilder.map(ShaderDefines.Builder::build), this.samplers.map(Collections::unmodifiableList), this.uniforms.map(Collections::unmodifiableList), this.blendFunction, this.depthTestFunction, this.polygonMode, this.cull, this.writeColor, this.writeAlpha, this.writeDepth, this.colorLogic, this.vertexFormat, this.vertexFormatMode);
        }

        public RenderPipeline build() {
            if (this.location.isEmpty()) {
                throw new IllegalStateException("Missing location");
            }
            if (this.vertexShader.isEmpty()) {
                throw new IllegalStateException("Missing vertex shader");
            }
            if (this.fragmentShader.isEmpty()) {
                throw new IllegalStateException("Missing fragment shader");
            }
            if (this.vertexFormat.isEmpty()) {
                throw new IllegalStateException("Missing vertex buffer format");
            }
            if (this.vertexFormatMode.isEmpty()) {
                throw new IllegalStateException("Missing vertex mode");
            }
            return new RenderPipeline(this.location.get(), this.vertexShader.get(), this.fragmentShader.get(), this.definesBuilder.orElse(ShaderDefines.builder()).build(), List.copyOf((Collection)this.samplers.orElse(new ArrayList())), this.uniforms.orElse(Collections.emptyList()), this.blendFunction, this.depthTestFunction.orElse(DepthTestFunction.LEQUAL_DEPTH_TEST), this.polygonMode.orElse(PolygonMode.FILL), this.cull.orElse(true), this.writeColor.orElse(true), this.writeAlpha.orElse(true), this.writeDepth.orElse(true), this.colorLogic.orElse(LogicOp.NONE), this.vertexFormat.get(), this.vertexFormatMode.get(), this.depthBiasScaleFactor, this.depthBiasConstant, nextPipelineSortKey++);
        }
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public static final class Snippet
    extends Record {
        final Optional<Identifier> vertexShader;
        final Optional<Identifier> fragmentShader;
        final Optional<ShaderDefines> shaderDefines;
        final Optional<List<String>> samplers;
        final Optional<List<UniformDescription>> uniforms;
        final Optional<BlendFunction> blendFunction;
        final Optional<DepthTestFunction> depthTestFunction;
        private final Optional<PolygonMode> polygonMode;
        final Optional<Boolean> cull;
        final Optional<Boolean> writeColor;
        final Optional<Boolean> writeAlpha;
        final Optional<Boolean> writeDepth;
        final Optional<LogicOp> colorLogic;
        final Optional<VertexFormat> vertexFormat;
        final Optional<VertexFormat.Mode> vertexFormatMode;

        public Snippet(Optional<Identifier> optional, Optional<Identifier> optional2, Optional<ShaderDefines> optional3, Optional<List<String>> optional4, Optional<List<UniformDescription>> optional5, Optional<BlendFunction> optional6, Optional<DepthTestFunction> optional7, Optional<PolygonMode> optional8, Optional<Boolean> optional9, Optional<Boolean> optional10, Optional<Boolean> optional11, Optional<Boolean> optional12, Optional<LogicOp> optional13, Optional<VertexFormat> optional14, Optional<VertexFormat.Mode> optional15) {
            this.vertexShader = optional;
            this.fragmentShader = optional2;
            this.shaderDefines = optional3;
            this.samplers = optional4;
            this.uniforms = optional5;
            this.blendFunction = optional6;
            this.depthTestFunction = optional7;
            this.polygonMode = optional8;
            this.cull = optional9;
            this.writeColor = optional10;
            this.writeAlpha = optional11;
            this.writeDepth = optional12;
            this.colorLogic = optional13;
            this.vertexFormat = optional14;
            this.vertexFormatMode = optional15;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Snippet.class, "vertexShader;fragmentShader;shaderDefines;samplers;uniforms;blendFunction;depthTestFunction;polygonMode;cull;writeColor;writeAlpha;writeDepth;colorLogic;vertexFormat;vertexFormatMode", "vertexShader", "fragmentShader", "shaderDefines", "samplers", "uniforms", "blendFunction", "depthTestFunction", "polygonMode", "cull", "writeColor", "writeAlpha", "writeDepth", "colorLogic", "vertexFormat", "vertexFormatMode"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Snippet.class, "vertexShader;fragmentShader;shaderDefines;samplers;uniforms;blendFunction;depthTestFunction;polygonMode;cull;writeColor;writeAlpha;writeDepth;colorLogic;vertexFormat;vertexFormatMode", "vertexShader", "fragmentShader", "shaderDefines", "samplers", "uniforms", "blendFunction", "depthTestFunction", "polygonMode", "cull", "writeColor", "writeAlpha", "writeDepth", "colorLogic", "vertexFormat", "vertexFormatMode"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Snippet.class, "vertexShader;fragmentShader;shaderDefines;samplers;uniforms;blendFunction;depthTestFunction;polygonMode;cull;writeColor;writeAlpha;writeDepth;colorLogic;vertexFormat;vertexFormatMode", "vertexShader", "fragmentShader", "shaderDefines", "samplers", "uniforms", "blendFunction", "depthTestFunction", "polygonMode", "cull", "writeColor", "writeAlpha", "writeDepth", "colorLogic", "vertexFormat", "vertexFormatMode"}, this, object);
        }

        public Optional<Identifier> vertexShader() {
            return this.vertexShader;
        }

        public Optional<Identifier> fragmentShader() {
            return this.fragmentShader;
        }

        public Optional<ShaderDefines> shaderDefines() {
            return this.shaderDefines;
        }

        public Optional<List<String>> samplers() {
            return this.samplers;
        }

        public Optional<List<UniformDescription>> uniforms() {
            return this.uniforms;
        }

        public Optional<BlendFunction> blendFunction() {
            return this.blendFunction;
        }

        public Optional<DepthTestFunction> depthTestFunction() {
            return this.depthTestFunction;
        }

        public Optional<PolygonMode> polygonMode() {
            return this.polygonMode;
        }

        public Optional<Boolean> cull() {
            return this.cull;
        }

        public Optional<Boolean> writeColor() {
            return this.writeColor;
        }

        public Optional<Boolean> writeAlpha() {
            return this.writeAlpha;
        }

        public Optional<Boolean> writeDepth() {
            return this.writeDepth;
        }

        public Optional<LogicOp> colorLogic() {
            return this.colorLogic;
        }

        public Optional<VertexFormat> vertexFormat() {
            return this.vertexFormat;
        }

        public Optional<VertexFormat.Mode> vertexFormatMode() {
            return this.vertexFormatMode;
        }
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public record UniformDescription(String name, UniformType type, @Nullable TextureFormat textureFormat) {
        public UniformDescription(String string, UniformType uniformType) {
            this(string, uniformType, null);
            if (uniformType == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Texel buffer needs a texture format");
            }
        }

        public UniformDescription(String string, TextureFormat textureFormat) {
            this(string, UniformType.TEXEL_BUFFER, textureFormat);
        }
    }
}

