/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.freetype.FT_Bitmap
 *  org.lwjgl.util.freetype.FT_Face
 *  org.lwjgl.util.freetype.FT_GlyphSlot
 *  org.lwjgl.util.freetype.FT_Vector
 *  org.lwjgl.util.freetype.FreeType
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@Environment(value=EnvType.CLIENT)
public class TrueTypeGlyphProvider
implements GlyphProvider {
    private @Nullable ByteBuffer fontMemory;
    private @Nullable FT_Face face;
    final float oversample;
    private final CodepointMap<GlyphEntry> glyphs = new CodepointMap(GlyphEntry[]::new, i -> new GlyphEntry[i][]);

    public TrueTypeGlyphProvider(ByteBuffer byteBuffer, FT_Face fT_Face, float f, float g, float h, float i2, String string) {
        this.fontMemory = byteBuffer;
        this.face = fT_Face;
        this.oversample = g;
        IntArraySet intSet = new IntArraySet();
        string.codePoints().forEach(arg_0 -> ((IntSet)intSet).add(arg_0));
        int j = Math.round(f * g);
        FreeType.FT_Set_Pixel_Sizes((FT_Face)fT_Face, (int)j, (int)j);
        float k = h * g;
        float l = -i2 * g;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int n;
            FT_Vector fT_Vector = FreeTypeUtil.setVector(FT_Vector.malloc((MemoryStack)memoryStack), k, l);
            FreeType.FT_Set_Transform((FT_Face)fT_Face, null, (FT_Vector)fT_Vector);
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            int m = (int)FreeType.FT_Get_First_Char((FT_Face)fT_Face, (IntBuffer)intBuffer);
            while ((n = intBuffer.get(0)) != 0) {
                if (!intSet.contains(m)) {
                    this.glyphs.put(m, new GlyphEntry(n));
                }
                m = (int)FreeType.FT_Get_Next_Char((FT_Face)fT_Face, (long)m, (IntBuffer)intBuffer);
            }
        }
    }

    @Override
    public @Nullable UnbakedGlyph getGlyph(int i) {
        GlyphEntry glyphEntry = this.glyphs.get(i);
        return glyphEntry != null ? this.getOrLoadGlyphInfo(i, glyphEntry) : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private UnbakedGlyph getOrLoadGlyphInfo(int i, GlyphEntry glyphEntry) {
        UnbakedGlyph unbakedGlyph = glyphEntry.glyph;
        if (unbakedGlyph == null) {
            FT_Face fT_Face;
            FT_Face fT_Face2 = fT_Face = this.validateFontOpen();
            synchronized (fT_Face2) {
                unbakedGlyph = glyphEntry.glyph;
                if (unbakedGlyph == null) {
                    glyphEntry.glyph = unbakedGlyph = this.loadGlyph(i, fT_Face, glyphEntry.index);
                }
            }
        }
        return unbakedGlyph;
    }

    private UnbakedGlyph loadGlyph(int i, FT_Face fT_Face, int j) {
        FT_GlyphSlot fT_GlyphSlot;
        int k = FreeType.FT_Load_Glyph((FT_Face)fT_Face, (int)j, (int)0x400008);
        if (k != 0) {
            FreeTypeUtil.assertError(k, String.format(Locale.ROOT, "Loading glyph U+%06X", i));
        }
        if ((fT_GlyphSlot = fT_Face.glyph()) == null) {
            throw new NullPointerException(String.format(Locale.ROOT, "Glyph U+%06X not initialized", i));
        }
        float f = FreeTypeUtil.x(fT_GlyphSlot.advance());
        FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
        int l = fT_GlyphSlot.bitmap_left();
        int m = fT_GlyphSlot.bitmap_top();
        int n = fT_Bitmap.width();
        int o = fT_Bitmap.rows();
        if (n <= 0 || o <= 0) {
            return new EmptyGlyph(f / this.oversample);
        }
        return new Glyph(l, m, n, o, f, j);
    }

    FT_Face validateFontOpen() {
        if (this.fontMemory == null || this.face == null) {
            throw new IllegalStateException("Provider already closed");
        }
        return this.face;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        if (this.face != null) {
            Object object = FreeTypeUtil.LIBRARY_LOCK;
            synchronized (object) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face((FT_Face)this.face), "Deleting face");
            }
            this.face = null;
        }
        MemoryUtil.memFree((Buffer)this.fontMemory);
        this.fontMemory = null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    @Environment(value=EnvType.CLIENT)
    static class GlyphEntry {
        final int index;
        volatile @Nullable UnbakedGlyph glyph;

        GlyphEntry(int i) {
            this.index = i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Glyph
    implements UnbakedGlyph {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final GlyphInfo info;
        final int index;

        Glyph(float f, float g, int i, int j, float h, int k) {
            this.width = i;
            this.height = j;
            this.info = GlyphInfo.simple(h / TrueTypeGlyphProvider.this.oversample);
            this.bearingX = f / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = g / TrueTypeGlyphProvider.this.oversample;
            this.index = k;
        }

        @Override
        public GlyphInfo info() {
            return this.info;
        }

        @Override
        public BakedGlyph bake(UnbakedGlyph.Stitcher stitcher) {
            return stitcher.stitch(this.info, new GlyphBitmap(){

                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TrueTypeGlyphProvider.this.oversample;
                }

                @Override
                public float getBearingLeft() {
                    return Glyph.this.bearingX;
                }

                @Override
                public float getBearingTop() {
                    return Glyph.this.bearingY;
                }

                @Override
                public void upload(int i, int j, GpuTexture gpuTexture) {
                    FT_Face fT_Face = TrueTypeGlyphProvider.this.validateFontOpen();
                    try (NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);){
                        if (nativeImage.copyFromFont(fT_Face, Glyph.this.index)) {
                            RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, nativeImage, 0, 0, i, j, Glyph.this.width, Glyph.this.height, 0, 0);
                        }
                    }
                }

                @Override
                public boolean isColored() {
                    return false;
                }
            });
        }
    }
}

