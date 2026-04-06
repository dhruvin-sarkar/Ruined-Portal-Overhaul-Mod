/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.ArabicShaping
 *  com.ibm.icu.text.ArabicShapingException
 *  com.ibm.icu.text.Bidi
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Font {
    private static final float EFFECT_DEPTH = 0.01f;
    private static final float OVER_EFFECT_DEPTH = 0.01f;
    private static final float UNDER_EFFECT_DEPTH = -0.01f;
    public static final float SHADOW_DEPTH = 0.03f;
    public final int lineHeight = 9;
    private final RandomSource random = RandomSource.create();
    final Provider provider;
    private final StringSplitter splitter;

    public Font(Provider provider) {
        this.provider = provider;
        this.splitter = new StringSplitter((i, style) -> this.getGlyphSource(style.getFont()).getGlyph(i).info().getAdvance(style.isBold()));
    }

    private GlyphSource getGlyphSource(FontDescription fontDescription) {
        return this.provider.glyphs(fontDescription);
    }

    public String bidirectionalShaping(String string) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(string), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicShapingException) {
            return string;
        }
    }

    public void drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        PreparedText preparedText = this.prepareText(string, f, g, i, bl, j);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, k));
    }

    public void drawInBatch(Component component, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        PreparedText preparedText = this.prepareText(component.getVisualOrderText(), f, g, i, bl, false, j);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, k));
    }

    public void drawInBatch(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int j, int k) {
        PreparedText preparedText = this.prepareText(formattedCharSequence, f, g, i, bl, false, j);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, k));
    }

    public void drawInBatch8xOutline(FormattedCharSequence formattedCharSequence, float f, float g, int i, int j, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k) {
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(0.0f, 0.0f, j, false, false);
        for (int l2 = -1; l2 <= 1; ++l2) {
            for (int m2 = -1; m2 <= 1; ++m2) {
                if (l2 == 0 && m2 == 0) continue;
                float[] fs = new float[]{f};
                int n = l2;
                int o = m2;
                formattedCharSequence.accept((l, style, m) -> {
                    boolean bl = style.isBold();
                    BakedGlyph bakedGlyph = this.getGlyph(m, style);
                    preparedTextBuilder.x = fs[0] + (float)n * bakedGlyph.info().getShadowOffset();
                    preparedTextBuilder.y = g + (float)o * bakedGlyph.info().getShadowOffset();
                    fs[0] = fs[0] + bakedGlyph.info().getAdvance(bl);
                    return preparedTextBuilder.accept(l, style.withColor(j), bakedGlyph);
                });
            }
        }
        GlyphVisitor glyphVisitor = GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, DisplayMode.NORMAL, k);
        for (TextRenderable.Styled styled : preparedTextBuilder.glyphs) {
            glyphVisitor.acceptGlyph(styled);
        }
        PreparedTextBuilder preparedTextBuilder2 = new PreparedTextBuilder(f, g, i, false, true);
        formattedCharSequence.accept(preparedTextBuilder2);
        preparedTextBuilder2.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, DisplayMode.POLYGON_OFFSET, k));
    }

    BakedGlyph getGlyph(int i, Style style) {
        GlyphSource glyphSource = this.getGlyphSource(style.getFont());
        BakedGlyph bakedGlyph = glyphSource.getGlyph(i);
        if (style.isObfuscated() && i != 32) {
            int j = Mth.ceil(bakedGlyph.info().getAdvance(false));
            bakedGlyph = glyphSource.getRandomGlyph(this.random, j);
        }
        return bakedGlyph;
    }

    public PreparedText prepareText(String string, float f, float g, int i, boolean bl, int j) {
        if (this.isBidirectional()) {
            string = this.bidirectionalShaping(string);
        }
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(f, g, i, j, bl, false);
        StringDecomposer.iterateFormatted(string, Style.EMPTY, (FormattedCharSink)preparedTextBuilder);
        return preparedTextBuilder;
    }

    public PreparedText prepareText(FormattedCharSequence formattedCharSequence, float f, float g, int i, boolean bl, boolean bl2, int j) {
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(f, g, i, j, bl, bl2);
        formattedCharSequence.accept(preparedTextBuilder);
        return preparedTextBuilder;
    }

    public int width(String string) {
        return Mth.ceil(this.splitter.stringWidth(string));
    }

    public int width(FormattedText formattedText) {
        return Mth.ceil(this.splitter.stringWidth(formattedText));
    }

    public int width(FormattedCharSequence formattedCharSequence) {
        return Mth.ceil(this.splitter.stringWidth(formattedCharSequence));
    }

    public String plainSubstrByWidth(String string, int i, boolean bl) {
        return bl ? this.splitter.plainTailByWidth(string, i, Style.EMPTY) : this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
    }

    public String plainSubstrByWidth(String string, int i) {
        return this.splitter.plainHeadByWidth(string, i, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText formattedText, int i) {
        return this.splitter.headByWidth(formattedText, i, Style.EMPTY);
    }

    public int wordWrapHeight(FormattedText formattedText, int i) {
        return 9 * this.splitter.splitLines(formattedText, i, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText formattedText, int i) {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(formattedText, i, Style.EMPTY));
    }

    public List<FormattedText> splitIgnoringLanguage(FormattedText formattedText, int i) {
        return this.splitter.splitLines(formattedText, i, Style.EMPTY);
    }

    public boolean isBidirectional() {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Provider {
        public GlyphSource glyphs(FontDescription var1);

        public EffectGlyph effect();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface PreparedText {
        public void visit(GlyphVisitor var1);

        public @Nullable ScreenRectangle bounds();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface GlyphVisitor {
        public static GlyphVisitor forMultiBufferSource(final MultiBufferSource multiBufferSource, final Matrix4f matrix4f, final DisplayMode displayMode, final int i) {
            return new GlyphVisitor(){

                @Override
                public void acceptGlyph(TextRenderable.Styled styled) {
                    this.render(styled);
                }

                @Override
                public void acceptEffect(TextRenderable textRenderable) {
                    this.render(textRenderable);
                }

                private void render(TextRenderable textRenderable) {
                    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(textRenderable.renderType(displayMode));
                    textRenderable.render(matrix4f, vertexConsumer, i, false);
                }
            };
        }

        default public void acceptGlyph(TextRenderable.Styled styled) {
        }

        default public void acceptEffect(TextRenderable textRenderable) {
        }

        default public void acceptEmptyArea(EmptyArea emptyArea) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DisplayMode {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;

    }

    @Environment(value=EnvType.CLIENT)
    class PreparedTextBuilder
    implements FormattedCharSink,
    PreparedText {
        private final boolean drawShadow;
        private final int color;
        private final int backgroundColor;
        private final boolean includeEmpty;
        float x;
        float y;
        private float left = Float.MAX_VALUE;
        private float top = Float.MAX_VALUE;
        private float right = -3.4028235E38f;
        private float bottom = -3.4028235E38f;
        private float backgroundLeft = Float.MAX_VALUE;
        private float backgroundTop = Float.MAX_VALUE;
        private float backgroundRight = -3.4028235E38f;
        private float backgroundBottom = -3.4028235E38f;
        final List<TextRenderable.Styled> glyphs = new ArrayList<TextRenderable.Styled>();
        private @Nullable List<TextRenderable> effects;
        private @Nullable List<EmptyArea> emptyAreas;

        public PreparedTextBuilder(float f, float g, int i, boolean bl, boolean bl2) {
            this(f, g, i, 0, bl, bl2);
        }

        public PreparedTextBuilder(float f, float g, int i, int j, boolean bl, boolean bl2) {
            this.x = f;
            this.y = g;
            this.drawShadow = bl;
            this.color = i;
            this.backgroundColor = j;
            this.includeEmpty = bl2;
            this.markBackground(f, g, 0.0f);
        }

        private void markSize(float f, float g, float h, float i) {
            this.left = Math.min(this.left, f);
            this.top = Math.min(this.top, g);
            this.right = Math.max(this.right, h);
            this.bottom = Math.max(this.bottom, i);
        }

        private void markBackground(float f, float g, float h) {
            if (ARGB.alpha(this.backgroundColor) == 0) {
                return;
            }
            this.backgroundLeft = Math.min(this.backgroundLeft, f - 1.0f);
            this.backgroundTop = Math.min(this.backgroundTop, g - 1.0f);
            this.backgroundRight = Math.max(this.backgroundRight, f + h);
            this.backgroundBottom = Math.max(this.backgroundBottom, g + 9.0f);
            this.markSize(this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom);
        }

        private void addGlyph(TextRenderable.Styled styled) {
            this.glyphs.add(styled);
            this.markSize(styled.left(), styled.top(), styled.right(), styled.bottom());
        }

        private void addEffect(TextRenderable textRenderable) {
            if (this.effects == null) {
                this.effects = new ArrayList<TextRenderable>();
            }
            this.effects.add(textRenderable);
            this.markSize(textRenderable.left(), textRenderable.top(), textRenderable.right(), textRenderable.bottom());
        }

        private void addEmptyGlyph(EmptyArea emptyArea) {
            if (this.emptyAreas == null) {
                this.emptyAreas = new ArrayList<EmptyArea>();
            }
            this.emptyAreas.add(emptyArea);
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            BakedGlyph bakedGlyph = Font.this.getGlyph(j, style);
            return this.accept(i, style, bakedGlyph);
        }

        public boolean accept(int i, Style style, BakedGlyph bakedGlyph) {
            float h;
            GlyphInfo glyphInfo = bakedGlyph.info();
            boolean bl = style.isBold();
            TextColor textColor = style.getColor();
            int j = this.getTextColor(textColor);
            int k = this.getShadowColor(style, j);
            float f = glyphInfo.getAdvance(bl);
            float g = i == 0 ? this.x - 1.0f : this.x;
            float l = bl ? glyphInfo.getBoldOffset() : 0.0f;
            TextRenderable.Styled styled = bakedGlyph.createGlyph(this.x, this.y, j, k, style, l, h = glyphInfo.getShadowOffset());
            if (styled != null) {
                this.addGlyph(styled);
            } else if (this.includeEmpty) {
                this.addEmptyGlyph(new EmptyArea(this.x, this.y, f, 7.0f, 9.0f, style));
            }
            this.markBackground(this.x, this.y, f);
            if (style.isStrikethrough()) {
                this.addEffect(Font.this.provider.effect().createEffect(g, this.y + 4.5f - 1.0f, this.x + f, this.y + 4.5f, 0.01f, j, k, h));
            }
            if (style.isUnderlined()) {
                this.addEffect(Font.this.provider.effect().createEffect(g, this.y + 9.0f - 1.0f, this.x + f, this.y + 9.0f, 0.01f, j, k, h));
            }
            this.x += f;
            return true;
        }

        @Override
        public void visit(GlyphVisitor glyphVisitor) {
            if (ARGB.alpha(this.backgroundColor) != 0) {
                glyphVisitor.acceptEffect(Font.this.provider.effect().createEffect(this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom, -0.01f, this.backgroundColor, 0, 0.0f));
            }
            for (TextRenderable.Styled styled : this.glyphs) {
                glyphVisitor.acceptGlyph(styled);
            }
            if (this.effects != null) {
                for (TextRenderable textRenderable : this.effects) {
                    glyphVisitor.acceptEffect(textRenderable);
                }
            }
            if (this.emptyAreas != null) {
                for (EmptyArea emptyArea : this.emptyAreas) {
                    glyphVisitor.acceptEmptyArea(emptyArea);
                }
            }
        }

        private int getTextColor(@Nullable TextColor textColor) {
            if (textColor != null) {
                int i = ARGB.alpha(this.color);
                int j = textColor.getValue();
                return ARGB.color(i, j);
            }
            return this.color;
        }

        private int getShadowColor(Style style, int i) {
            Integer integer = style.getShadowColor();
            if (integer != null) {
                float f = ARGB.alphaFloat(i);
                float g = ARGB.alphaFloat(integer);
                if (f != 1.0f) {
                    return ARGB.color(ARGB.as8BitChannel(f * g), (int)integer);
                }
                return integer;
            }
            if (this.drawShadow) {
                return ARGB.scaleRGB(i, 0.25f);
            }
            return 0;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            if (this.left >= this.right || this.top >= this.bottom) {
                return null;
            }
            int i = Mth.floor(this.left);
            int j = Mth.floor(this.top);
            int k = Mth.ceil(this.right);
            int l = Mth.ceil(this.bottom);
            return new ScreenRectangle(i, j, k - i, l - j);
        }
    }
}

