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
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class BakedSheetGlyph
implements BakedGlyph,
EffectGlyph {
    public static final float Z_FIGHTER = 0.001f;
    final GlyphInfo info;
    final GlyphRenderTypes renderTypes;
    final GpuTextureView textureView;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedSheetGlyph(GlyphInfo glyphInfo, GlyphRenderTypes glyphRenderTypes, GpuTextureView gpuTextureView, float f, float g, float h, float i, float j, float k, float l, float m) {
        this.info = glyphInfo;
        this.renderTypes = glyphRenderTypes;
        this.textureView = gpuTextureView;
        this.u0 = f;
        this.u1 = g;
        this.v0 = h;
        this.v1 = i;
        this.left = j;
        this.right = k;
        this.up = l;
        this.down = m;
    }

    float left(GlyphInstance glyphInstance) {
        return glyphInstance.x + this.left + (glyphInstance.style.isItalic() ? Math.min(this.shearTop(), this.shearBottom()) : 0.0f) - BakedSheetGlyph.extraThickness(glyphInstance.style.isBold());
    }

    float top(GlyphInstance glyphInstance) {
        return glyphInstance.y + this.up - BakedSheetGlyph.extraThickness(glyphInstance.style.isBold());
    }

    float right(GlyphInstance glyphInstance) {
        return glyphInstance.x + this.right + (glyphInstance.hasShadow() ? glyphInstance.shadowOffset : 0.0f) + (glyphInstance.style.isItalic() ? Math.max(this.shearTop(), this.shearBottom()) : 0.0f) + BakedSheetGlyph.extraThickness(glyphInstance.style.isBold());
    }

    float bottom(GlyphInstance glyphInstance) {
        return glyphInstance.y + this.down + (glyphInstance.hasShadow() ? glyphInstance.shadowOffset : 0.0f) + BakedSheetGlyph.extraThickness(glyphInstance.style.isBold());
    }

    void renderChar(GlyphInstance glyphInstance, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl) {
        float l;
        float h;
        Style style = glyphInstance.style();
        boolean bl2 = style.isItalic();
        float f = glyphInstance.x();
        float g = glyphInstance.y();
        int j = glyphInstance.color();
        boolean bl3 = style.isBold();
        float f2 = h = bl ? 0.0f : 0.001f;
        if (glyphInstance.hasShadow()) {
            int k = glyphInstance.shadowColor();
            this.render(bl2, f + glyphInstance.shadowOffset(), g + glyphInstance.shadowOffset(), 0.0f, matrix4f, vertexConsumer, k, bl3, i);
            if (bl3) {
                this.render(bl2, f + glyphInstance.boldOffset() + glyphInstance.shadowOffset(), g + glyphInstance.shadowOffset(), h, matrix4f, vertexConsumer, k, true, i);
            }
            l = bl ? 0.0f : 0.03f;
        } else {
            l = 0.0f;
        }
        this.render(bl2, f, g, l, matrix4f, vertexConsumer, j, bl3, i);
        if (bl3) {
            this.render(bl2, f + glyphInstance.boldOffset(), g, l + h, matrix4f, vertexConsumer, j, true, i);
        }
    }

    private void render(boolean bl, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl2, int j) {
        float k = f + this.left;
        float l = f + this.right;
        float m = g + this.up;
        float n = g + this.down;
        float o = bl ? this.shearTop() : 0.0f;
        float p = bl ? this.shearBottom() : 0.0f;
        float q = BakedSheetGlyph.extraThickness(bl2);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, k + o - q, m - q, h).setColor(i).setUv(this.u0, this.v0).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, k + p - q, n + q, h).setColor(i).setUv(this.u0, this.v1).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, l + p + q, n + q, h).setColor(i).setUv(this.u1, this.v1).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, l + o + q, m - q, h).setColor(i).setUv(this.u1, this.v0).setLight(j);
    }

    private static float extraThickness(boolean bl) {
        return bl ? 0.1f : 0.0f;
    }

    private float shearBottom() {
        return 1.0f - 0.25f * this.down;
    }

    private float shearTop() {
        return 1.0f - 0.25f * this.up;
    }

    void renderEffect(EffectInstance effectInstance, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl) {
        float f;
        float f2 = f = bl ? 0.0f : effectInstance.depth;
        if (effectInstance.hasShadow()) {
            this.buildEffect(effectInstance, effectInstance.shadowOffset(), f, effectInstance.shadowColor(), vertexConsumer, i, matrix4f);
            f += bl ? 0.0f : 0.03f;
        }
        this.buildEffect(effectInstance, 0.0f, f, effectInstance.color, vertexConsumer, i, matrix4f);
    }

    private void buildEffect(EffectInstance effectInstance, float f, float g, int i, VertexConsumer vertexConsumer, int j, Matrix4f matrix4f) {
        vertexConsumer.addVertex((Matrix4fc)matrix4f, effectInstance.x0 + f, effectInstance.y1 + f, g).setColor(i).setUv(this.u0, this.v0).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, effectInstance.x1 + f, effectInstance.y1 + f, g).setColor(i).setUv(this.u0, this.v1).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, effectInstance.x1 + f, effectInstance.y0 + f, g).setColor(i).setUv(this.u1, this.v1).setLight(j);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, effectInstance.x0 + f, effectInstance.y0 + f, g).setColor(i).setUv(this.u1, this.v0).setLight(j);
    }

    @Override
    public GlyphInfo info() {
        return this.info;
    }

    @Override
    public TextRenderable.Styled createGlyph(float f, float g, int i, int j, Style style, float h, float k) {
        return new GlyphInstance(f, g, i, j, this, style, h, k);
    }

    @Override
    public TextRenderable createEffect(float f, float g, float h, float i, float j, int k, int l, float m) {
        return new EffectInstance(this, f, g, h, i, j, k, l, m);
    }

    @Environment(value=EnvType.CLIENT)
    static final class GlyphInstance
    extends Record
    implements TextRenderable.Styled {
        final float x;
        final float y;
        private final int color;
        private final int shadowColor;
        private final BakedSheetGlyph glyph;
        final Style style;
        private final float boldOffset;
        final float shadowOffset;

        GlyphInstance(float f, float g, int i, int j, BakedSheetGlyph bakedSheetGlyph, Style style, float h, float k) {
            this.x = f;
            this.y = g;
            this.color = i;
            this.shadowColor = j;
            this.glyph = bakedSheetGlyph;
            this.style = style;
            this.boldOffset = h;
            this.shadowOffset = k;
        }

        @Override
        public float left() {
            return this.glyph.left(this);
        }

        @Override
        public float top() {
            return this.glyph.top(this);
        }

        @Override
        public float right() {
            return this.glyph.right(this);
        }

        @Override
        public float activeRight() {
            return this.x + this.glyph.info.getAdvance(this.style.isBold());
        }

        @Override
        public float bottom() {
            return this.glyph.bottom(this);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public void render(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl) {
            this.glyph.renderChar(this, matrix4f, vertexConsumer, i, bl);
        }

        @Override
        public RenderType renderType(Font.DisplayMode displayMode) {
            return this.glyph.renderTypes.select(displayMode);
        }

        @Override
        public GpuTextureView textureView() {
            return this.glyph.textureView;
        }

        @Override
        public RenderPipeline guiPipeline() {
            return this.glyph.renderTypes.guiPipeline();
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this, object);
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public int color() {
            return this.color;
        }

        public int shadowColor() {
            return this.shadowColor;
        }

        public BakedSheetGlyph glyph() {
            return this.glyph;
        }

        @Override
        public Style style() {
            return this.style;
        }

        public float boldOffset() {
            return this.boldOffset;
        }

        public float shadowOffset() {
            return this.shadowOffset;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class EffectInstance
    extends Record
    implements TextRenderable {
        private final BakedSheetGlyph glyph;
        final float x0;
        final float y0;
        final float x1;
        final float y1;
        final float depth;
        final int color;
        private final int shadowColor;
        private final float shadowOffset;

        EffectInstance(BakedSheetGlyph bakedSheetGlyph, float f, float g, float h, float i, float j, int k, int l, float m) {
            this.glyph = bakedSheetGlyph;
            this.x0 = f;
            this.y0 = g;
            this.x1 = h;
            this.y1 = i;
            this.depth = j;
            this.color = k;
            this.shadowColor = l;
            this.shadowOffset = m;
        }

        @Override
        public float left() {
            return this.x0;
        }

        @Override
        public float top() {
            return this.y0;
        }

        @Override
        public float right() {
            return this.x1 + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        @Override
        public float bottom() {
            return this.y1 + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public void render(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl) {
            this.glyph.renderEffect(this, matrix4f, vertexConsumer, i, false);
        }

        @Override
        public RenderType renderType(Font.DisplayMode displayMode) {
            return this.glyph.renderTypes.select(displayMode);
        }

        @Override
        public GpuTextureView textureView() {
            return this.glyph.textureView;
        }

        @Override
        public RenderPipeline guiPipeline() {
            return this.glyph.renderTypes.guiPipeline();
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EffectInstance.class, "glyph;x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "glyph", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EffectInstance.class, "glyph;x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "glyph", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EffectInstance.class, "glyph;x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "glyph", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this, object);
        }

        public BakedSheetGlyph glyph() {
            return this.glyph;
        }

        public float x0() {
            return this.x0;
        }

        public float y0() {
            return this.y0;
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float depth() {
            return this.depth;
        }

        public int color() {
            return this.color;
        }

        public int shadowColor() {
            return this.shadowColor;
        }

        public float shadowOffset() {
            return this.shadowOffset;
        }
    }
}

