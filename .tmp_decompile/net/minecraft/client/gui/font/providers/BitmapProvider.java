/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BitmapProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private final NativeImage image;
    private final CodepointMap<Glyph> glyphs;

    BitmapProvider(NativeImage nativeImage, CodepointMap<Glyph> codepointMap) {
        this.image = nativeImage;
        this.glyphs = codepointMap;
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Override
    public @Nullable UnbakedGlyph getGlyph(int i) {
        return this.glyphs.get(i);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable((IntSet)this.glyphs.keySet());
    }

    @Environment(value=EnvType.CLIENT)
    static final class Glyph
    extends Record
    implements UnbakedGlyph {
        final float scale;
        final NativeImage image;
        final int offsetX;
        final int offsetY;
        final int width;
        final int height;
        private final int advance;
        final int ascent;

        Glyph(float f, NativeImage nativeImage, int i, int j, int k, int l, int m, int n) {
            this.scale = f;
            this.image = nativeImage;
            this.offsetX = i;
            this.offsetY = j;
            this.width = k;
            this.height = l;
            this.advance = m;
            this.ascent = n;
        }

        @Override
        public GlyphInfo info() {
            return GlyphInfo.simple(this.advance);
        }

        @Override
        public BakedGlyph bake(UnbakedGlyph.Stitcher stitcher) {
            return stitcher.stitch(this.info(), new GlyphBitmap(){

                @Override
                public float getOversample() {
                    return 1.0f / scale;
                }

                @Override
                public int getPixelWidth() {
                    return width;
                }

                @Override
                public int getPixelHeight() {
                    return height;
                }

                @Override
                public float getBearingTop() {
                    return ascent;
                }

                @Override
                public void upload(int i, int j, GpuTexture gpuTexture) {
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, image, 0, 0, i, j, width, height, offsetX, offsetY);
                }

                @Override
                public boolean isColored() {
                    return image.format().components() > 1;
                }
            });
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this, object);
        }

        public float scale() {
            return this.scale;
        }

        public NativeImage image() {
            return this.image;
        }

        public int offsetX() {
            return this.offsetX;
        }

        public int offsetY() {
            return this.offsetY;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

        public int advance() {
            return this.advance;
        }

        public int ascent() {
            return this.ascent;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Definition(Identifier file, int height, int ascent, int[][] codepointGrid) implements GlyphProviderDefinition
    {
        private static final Codec<int[][]> CODEPOINT_GRID_CODEC = Codec.STRING.listOf().xmap(list -> {
            int i = list.size();
            int[][] is = new int[i][];
            for (int j = 0; j < i; ++j) {
                is[j] = ((String)list.get(j)).codePoints().toArray();
            }
            return is;
        }, is -> {
            ArrayList<String> list = new ArrayList<String>(((int[][])is).length);
            for (int[] js : is) {
                list.add(new String(js, 0, js.length));
            }
            return list;
        }).validate(Definition::validateDimensions);
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("file").forGetter(Definition::file), (App)Codec.INT.optionalFieldOf("height", (Object)8).forGetter(Definition::height), (App)Codec.INT.fieldOf("ascent").forGetter(Definition::ascent), (App)CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(Definition::codepointGrid)).apply((Applicative)instance, Definition::new)).validate(Definition::validate);

        private static DataResult<int[][]> validateDimensions(int[][] is) {
            int i = is.length;
            if (i == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            int[] js = is[0];
            int j = js.length;
            if (j == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            for (int k = 1; k < i; ++k) {
                int[] ks = is[k];
                if (ks.length == j) continue;
                return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + ks.length + " codepoints, expected: " + j + "), pad with \\u0000");
            }
            return DataResult.success((Object)is);
        }

        private static DataResult<Definition> validate(Definition definition) {
            if (definition.ascent > definition.height) {
                return DataResult.error(() -> "Ascent " + definition.ascent + " higher than height " + definition.height);
            }
            return DataResult.success((Object)definition);
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.BITMAP;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager resourceManager) throws IOException {
            Identifier identifier = this.file.withPrefix("textures/");
            try (InputStream inputStream = resourceManager.open(identifier);){
                NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
                int i2 = nativeImage.getWidth();
                int j = nativeImage.getHeight();
                int k = i2 / this.codepointGrid[0].length;
                int l = j / this.codepointGrid.length;
                float f = (float)this.height / (float)l;
                CodepointMap<Glyph> codepointMap = new CodepointMap<Glyph>(Glyph[]::new, i -> new Glyph[i][]);
                for (int m = 0; m < this.codepointGrid.length; ++m) {
                    int n = 0;
                    for (int o : this.codepointGrid[m]) {
                        int q;
                        Glyph glyph;
                        int p = n++;
                        if (o == 0 || (glyph = codepointMap.put(o, new Glyph(f, nativeImage, p * k, m * l, k, l, (int)(0.5 + (double)((float)(q = this.getActualGlyphWidth(nativeImage, k, l, p, m)) * f)) + 1, this.ascent))) == null) continue;
                        LOGGER.warn("Codepoint '{}' declared multiple times in {}", (Object)Integer.toHexString(o), (Object)identifier);
                    }
                }
                BitmapProvider bitmapProvider = new BitmapProvider(nativeImage, codepointMap);
                return bitmapProvider;
            }
        }

        private int getActualGlyphWidth(NativeImage nativeImage, int i, int j, int k, int l) {
            int m;
            for (m = i - 1; m >= 0; --m) {
                int n = k * i + m;
                for (int o = 0; o < j; ++o) {
                    int p = l * j + o;
                    if (nativeImage.getLuminanceOrAlpha(n, p) == 0) continue;
                    return m + 1;
                }
            }
            return m + 1;
        }
    }
}

