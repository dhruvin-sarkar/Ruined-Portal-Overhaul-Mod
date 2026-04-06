/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.PlainTextRenderable;
import net.minecraft.client.gui.font.SingleSpriteSource;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class AtlasGlyphProvider {
    static final GlyphInfo GLYPH_INFO = GlyphInfo.simple(8.0f);
    final TextureAtlas atlas;
    final GlyphRenderTypes renderTypes;
    private final GlyphSource missingWrapper;
    private final Map<Identifier, GlyphSource> wrapperCache = new HashMap<Identifier, GlyphSource>();
    private final Function<Identifier, GlyphSource> spriteResolver;

    public AtlasGlyphProvider(TextureAtlas textureAtlas) {
        this.atlas = textureAtlas;
        this.renderTypes = GlyphRenderTypes.createForColorTexture(textureAtlas.location());
        TextureAtlasSprite textureAtlasSprite = textureAtlas.missingSprite();
        this.missingWrapper = this.createSprite(textureAtlasSprite);
        this.spriteResolver = identifier -> {
            TextureAtlasSprite textureAtlasSprite2 = textureAtlas.getSprite((Identifier)identifier);
            if (textureAtlasSprite2 == textureAtlasSprite) {
                return this.missingWrapper;
            }
            return this.createSprite(textureAtlasSprite2);
        };
    }

    public GlyphSource sourceForSprite(Identifier identifier) {
        return this.wrapperCache.computeIfAbsent(identifier, this.spriteResolver);
    }

    private GlyphSource createSprite(final TextureAtlasSprite textureAtlasSprite) {
        return new SingleSpriteSource(new BakedGlyph(){

            @Override
            public GlyphInfo info() {
                return GLYPH_INFO;
            }

            @Override
            public TextRenderable.Styled createGlyph(float f, float g, int i, int j, Style style, float h, float k) {
                return new Instance(AtlasGlyphProvider.this.renderTypes, AtlasGlyphProvider.this.atlas.getTextureView(), textureAtlasSprite, f, g, i, j, k, style);
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    record Instance(GlyphRenderTypes renderTypes, GpuTextureView textureView, TextureAtlasSprite sprite, float x, float y, int color, int shadowColor, float shadowOffset, Style style) implements PlainTextRenderable
    {
        @Override
        public void renderSprite(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, float f, float g, float h, int j) {
            float k = f + this.left();
            float l = f + this.right();
            float m = g + this.top();
            float n = g + this.bottom();
            vertexConsumer.addVertex((Matrix4fc)matrix4f, k, m, h).setUv(this.sprite.getU0(), this.sprite.getV0()).setColor(j).setLight(i);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, k, n, h).setUv(this.sprite.getU0(), this.sprite.getV1()).setColor(j).setLight(i);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, l, n, h).setUv(this.sprite.getU1(), this.sprite.getV1()).setColor(j).setLight(i);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, l, m, h).setUv(this.sprite.getU1(), this.sprite.getV0()).setColor(j).setLight(i);
        }

        @Override
        public RenderType renderType(Font.DisplayMode displayMode) {
            return this.renderTypes.select(displayMode);
        }

        @Override
        public RenderPipeline guiPipeline() {
            return this.renderTypes.guiPipeline();
        }
    }
}

