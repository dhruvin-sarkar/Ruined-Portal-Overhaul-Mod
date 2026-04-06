/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.rendertype;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class RenderSetup {
    final RenderPipeline pipeline;
    final Map<String, TextureBinding> textures;
    final TextureTransform textureTransform;
    final OutputTarget outputTarget;
    final OutlineProperty outlineProperty;
    final boolean useLightmap;
    final boolean useOverlay;
    final boolean affectsCrumbling;
    final boolean sortOnUpload;
    final int bufferSize;
    final LayeringTransform layeringTransform;

    RenderSetup(RenderPipeline renderPipeline, Map<String, TextureBinding> map, boolean bl, boolean bl2, LayeringTransform layeringTransform, OutputTarget outputTarget, TextureTransform textureTransform, OutlineProperty outlineProperty, boolean bl3, boolean bl4, int i) {
        this.pipeline = renderPipeline;
        this.textures = map;
        this.outputTarget = outputTarget;
        this.textureTransform = textureTransform;
        this.useLightmap = bl;
        this.useOverlay = bl2;
        this.outlineProperty = outlineProperty;
        this.layeringTransform = layeringTransform;
        this.affectsCrumbling = bl3;
        this.sortOnUpload = bl4;
        this.bufferSize = i;
    }

    public String toString() {
        return "RenderSetup[layeringTransform=" + String.valueOf(this.layeringTransform) + ", textureTransform=" + String.valueOf(this.textureTransform) + ", textures=" + String.valueOf(this.textures) + ", outlineProperty=" + String.valueOf((Object)this.outlineProperty) + ", useLightmap=" + this.useLightmap + ", useOverlay=" + this.useOverlay + "]";
    }

    public static RenderSetupBuilder builder(RenderPipeline renderPipeline) {
        return new RenderSetupBuilder(renderPipeline);
    }

    public Map<String, TextureAndSampler> getTextures() {
        if (this.textures.isEmpty() && !this.useOverlay && !this.useLightmap) {
            return Collections.emptyMap();
        }
        HashMap<String, TextureAndSampler> map = new HashMap<String, TextureAndSampler>();
        if (this.useOverlay) {
            map.put("Sampler1", new TextureAndSampler(Minecraft.getInstance().gameRenderer.overlayTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
        if (this.useLightmap) {
            map.put("Sampler2", new TextureAndSampler(Minecraft.getInstance().gameRenderer.lightTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Map.Entry<String, TextureBinding> entry : this.textures.entrySet()) {
            AbstractTexture abstractTexture = textureManager.getTexture(entry.getValue().location);
            GpuSampler gpuSampler = entry.getValue().sampler().get();
            map.put(entry.getKey(), new TextureAndSampler(abstractTexture.getTextureView(), gpuSampler != null ? gpuSampler : abstractTexture.getSampler()));
        }
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String string2) {
            this.name = string2;
        }

        public String toString() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class RenderSetupBuilder {
        private final RenderPipeline pipeline;
        private boolean useLightmap = false;
        private boolean useOverlay = false;
        private LayeringTransform layeringTransform = LayeringTransform.NO_LAYERING;
        private OutputTarget outputTarget = OutputTarget.MAIN_TARGET;
        private TextureTransform textureTransform = TextureTransform.DEFAULT_TEXTURING;
        private boolean affectsCrumbling = false;
        private boolean sortOnUpload = false;
        private int bufferSize = 1536;
        private OutlineProperty outlineProperty = OutlineProperty.NONE;
        private final Map<String, TextureBinding> textures = new HashMap<String, TextureBinding>();

        RenderSetupBuilder(RenderPipeline renderPipeline) {
            this.pipeline = renderPipeline;
        }

        public RenderSetupBuilder withTexture(String string, Identifier identifier) {
            this.textures.put(string, new TextureBinding(identifier, () -> null));
            return this;
        }

        public RenderSetupBuilder withTexture(String string, Identifier identifier, @Nullable Supplier<GpuSampler> supplier) {
            this.textures.put(string, new TextureBinding(identifier, (Supplier<GpuSampler>)Suppliers.memoize(() -> supplier == null ? null : (GpuSampler)supplier.get())));
            return this;
        }

        public RenderSetupBuilder useLightmap() {
            this.useLightmap = true;
            return this;
        }

        public RenderSetupBuilder useOverlay() {
            this.useOverlay = true;
            return this;
        }

        public RenderSetupBuilder affectsCrumbling() {
            this.affectsCrumbling = true;
            return this;
        }

        public RenderSetupBuilder sortOnUpload() {
            this.sortOnUpload = true;
            return this;
        }

        public RenderSetupBuilder bufferSize(int i) {
            this.bufferSize = i;
            return this;
        }

        public RenderSetupBuilder setLayeringTransform(LayeringTransform layeringTransform) {
            this.layeringTransform = layeringTransform;
            return this;
        }

        public RenderSetupBuilder setOutputTarget(OutputTarget outputTarget) {
            this.outputTarget = outputTarget;
            return this;
        }

        public RenderSetupBuilder setTextureTransform(TextureTransform textureTransform) {
            this.textureTransform = textureTransform;
            return this;
        }

        public RenderSetupBuilder setOutline(OutlineProperty outlineProperty) {
            this.outlineProperty = outlineProperty;
            return this;
        }

        public RenderSetup createRenderSetup() {
            return new RenderSetup(this.pipeline, this.textures, this.useLightmap, this.useOverlay, this.layeringTransform, this.outputTarget, this.textureTransform, this.outlineProperty, this.affectsCrumbling, this.sortOnUpload, this.bufferSize);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TextureAndSampler(GpuTextureView textureView, GpuSampler sampler) {
    }

    @Environment(value=EnvType.CLIENT)
    static final class TextureBinding
    extends Record {
        final Identifier location;
        private final Supplier<@Nullable GpuSampler> sampler;

        TextureBinding(Identifier identifier, Supplier<@Nullable GpuSampler> supplier) {
            this.location = identifier;
            this.sampler = supplier;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextureBinding.class, "location;sampler", "location", "sampler"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextureBinding.class, "location;sampler", "location", "sampler"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextureBinding.class, "location;sampler", "location", "sampler"}, this, object);
        }

        public Identifier location() {
            return this.location;
        }

        public Supplier<@Nullable GpuSampler> sampler() {
            return this.sampler;
        }
    }
}

