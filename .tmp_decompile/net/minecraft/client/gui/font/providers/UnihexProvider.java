/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.bytes.ByteList
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font.providers;

import com.google.common.annotations.VisibleForTesting;
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
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastBufferedInputStream;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class UnihexProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int GLYPH_HEIGHT = 16;
    private static final int DIGITS_PER_BYTE = 2;
    private static final int DIGITS_FOR_WIDTH_8 = 32;
    private static final int DIGITS_FOR_WIDTH_16 = 64;
    private static final int DIGITS_FOR_WIDTH_24 = 96;
    private static final int DIGITS_FOR_WIDTH_32 = 128;
    private final CodepointMap<Glyph> glyphs;

    UnihexProvider(CodepointMap<Glyph> codepointMap) {
        this.glyphs = codepointMap;
    }

    @Override
    public @Nullable UnbakedGlyph getGlyph(int i) {
        return this.glyphs.get(i);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    @VisibleForTesting
    static void unpackBitsToBytes(IntBuffer intBuffer, int i, int j, int k) {
        int l = 32 - j - 1;
        int m = 32 - k - 1;
        for (int n = l; n >= m; --n) {
            if (n >= 32 || n < 0) {
                intBuffer.put(0);
                continue;
            }
            boolean bl = (i >> n & 1) != 0;
            intBuffer.put(bl ? -1 : 0);
        }
    }

    static void unpackBitsToBytes(IntBuffer intBuffer, LineData lineData, int i, int j) {
        for (int k = 0; k < 16; ++k) {
            int l = lineData.line(k);
            UnihexProvider.unpackBitsToBytes(intBuffer, l, i, j);
        }
    }

    @VisibleForTesting
    static void readFromStream(InputStream inputStream, ReaderOutput readerOutput) throws IOException {
        int i = 0;
        ByteArrayList byteList = new ByteArrayList(128);
        while (true) {
            int l;
            boolean bl = UnihexProvider.copyUntil(inputStream, (ByteList)byteList, 58);
            int j = byteList.size();
            if (j == 0 && !bl) break;
            if (!bl || j != 4 && j != 5 && j != 6) {
                throw new IllegalArgumentException("Invalid entry at line " + i + ": expected 4, 5 or 6 hex digits followed by a colon");
            }
            int k = 0;
            for (l = 0; l < j; ++l) {
                k = k << 4 | UnihexProvider.decodeHex(i, byteList.getByte(l));
            }
            byteList.clear();
            UnihexProvider.copyUntil(inputStream, (ByteList)byteList, 10);
            l = byteList.size();
            LineData lineData = switch (l) {
                case 32 -> ByteContents.read(i, (ByteList)byteList);
                case 64 -> ShortContents.read(i, (ByteList)byteList);
                case 96 -> IntContents.read24(i, (ByteList)byteList);
                case 128 -> IntContents.read32(i, (ByteList)byteList);
                default -> throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line");
            };
            readerOutput.accept(k, lineData);
            ++i;
            byteList.clear();
        }
    }

    static int decodeHex(int i, ByteList byteList, int j) {
        return UnihexProvider.decodeHex(i, byteList.getByte(j));
    }

    private static int decodeHex(int i, byte b) {
        return switch (b) {
            case 48 -> 0;
            case 49 -> 1;
            case 50 -> 2;
            case 51 -> 3;
            case 52 -> 4;
            case 53 -> 5;
            case 54 -> 6;
            case 55 -> 7;
            case 56 -> 8;
            case 57 -> 9;
            case 65 -> 10;
            case 66 -> 11;
            case 67 -> 12;
            case 68 -> 13;
            case 69 -> 14;
            case 70 -> 15;
            default -> throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex digit, got " + (char)b);
        };
    }

    private static boolean copyUntil(InputStream inputStream, ByteList byteList, int i) throws IOException {
        int j;
        while ((j = inputStream.read()) != -1) {
            if (j == i) {
                return true;
            }
            byteList.add((byte)j);
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface LineData {
        public int line(int var1);

        public int bitWidth();

        default public int mask() {
            int i = 0;
            for (int j = 0; j < 16; ++j) {
                i |= this.line(j);
            }
            return i;
        }

        default public int calculateWidth() {
            int l;
            int k;
            int i = this.mask();
            int j = this.bitWidth();
            if (i == 0) {
                k = 0;
                l = j;
            } else {
                k = Integer.numberOfLeadingZeros(i);
                l = 32 - Integer.numberOfTrailingZeros(i) - 1;
            }
            return Dimensions.pack(k, l);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ByteContents(byte[] contents) implements LineData
    {
        @Override
        public int line(int i) {
            return this.contents[i] << 24;
        }

        static LineData read(int i, ByteList byteList) {
            byte[] bs = new byte[16];
            int j = 0;
            for (int k = 0; k < 16; ++k) {
                byte b;
                int l = UnihexProvider.decodeHex(i, byteList, j++);
                int m = UnihexProvider.decodeHex(i, byteList, j++);
                bs[k] = b = (byte)(l << 4 | m);
            }
            return new ByteContents(bs);
        }

        @Override
        public int bitWidth() {
            return 8;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ShortContents(short[] contents) implements LineData
    {
        @Override
        public int line(int i) {
            return this.contents[i] << 16;
        }

        static LineData read(int i, ByteList byteList) {
            short[] ss = new short[16];
            int j = 0;
            for (int k = 0; k < 16; ++k) {
                short s;
                int l = UnihexProvider.decodeHex(i, byteList, j++);
                int m = UnihexProvider.decodeHex(i, byteList, j++);
                int n = UnihexProvider.decodeHex(i, byteList, j++);
                int o = UnihexProvider.decodeHex(i, byteList, j++);
                ss[k] = s = (short)(l << 12 | m << 8 | n << 4 | o);
            }
            return new ShortContents(ss);
        }

        @Override
        public int bitWidth() {
            return 16;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record IntContents(int[] contents, int bitWidth) implements LineData
    {
        private static final int SIZE_24 = 24;

        @Override
        public int line(int i) {
            return this.contents[i];
        }

        static LineData read24(int i, ByteList byteList) {
            int[] is = new int[16];
            int j = 0;
            int k = 0;
            for (int l = 0; l < 16; ++l) {
                int m = UnihexProvider.decodeHex(i, byteList, k++);
                int n = UnihexProvider.decodeHex(i, byteList, k++);
                int o = UnihexProvider.decodeHex(i, byteList, k++);
                int p = UnihexProvider.decodeHex(i, byteList, k++);
                int q = UnihexProvider.decodeHex(i, byteList, k++);
                int r = UnihexProvider.decodeHex(i, byteList, k++);
                int s = m << 20 | n << 16 | o << 12 | p << 8 | q << 4 | r;
                is[l] = s << 8;
                j |= s;
            }
            return new IntContents(is, 24);
        }

        public static LineData read32(int i, ByteList byteList) {
            int[] is = new int[16];
            int j = 0;
            int k = 0;
            for (int l = 0; l < 16; ++l) {
                int u;
                int m = UnihexProvider.decodeHex(i, byteList, k++);
                int n = UnihexProvider.decodeHex(i, byteList, k++);
                int o = UnihexProvider.decodeHex(i, byteList, k++);
                int p = UnihexProvider.decodeHex(i, byteList, k++);
                int q = UnihexProvider.decodeHex(i, byteList, k++);
                int r = UnihexProvider.decodeHex(i, byteList, k++);
                int s = UnihexProvider.decodeHex(i, byteList, k++);
                int t = UnihexProvider.decodeHex(i, byteList, k++);
                is[l] = u = m << 28 | n << 24 | o << 20 | p << 16 | q << 12 | r << 8 | s << 4 | t;
                j |= u;
            }
            return new IntContents(is, 32);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ReaderOutput {
        public void accept(int var1, LineData var2);
    }

    @Environment(value=EnvType.CLIENT)
    static final class Glyph
    extends Record
    implements UnbakedGlyph {
        final LineData contents;
        final int left;
        final int right;

        Glyph(LineData lineData, int i, int j) {
            this.contents = lineData;
            this.left = i;
            this.right = j;
        }

        public int width() {
            return this.right - this.left + 1;
        }

        @Override
        public GlyphInfo info() {
            return new GlyphInfo(){

                @Override
                public float getAdvance() {
                    return this.width() / 2 + 1;
                }

                @Override
                public float getShadowOffset() {
                    return 0.5f;
                }

                @Override
                public float getBoldOffset() {
                    return 0.5f;
                }
            };
        }

        @Override
        public BakedGlyph bake(UnbakedGlyph.Stitcher stitcher) {
            return stitcher.stitch(this.info(), new GlyphBitmap(){

                @Override
                public float getOversample() {
                    return 2.0f;
                }

                @Override
                public int getPixelWidth() {
                    return this.width();
                }

                @Override
                public int getPixelHeight() {
                    return 16;
                }

                @Override
                public void upload(int i, int j, GpuTexture gpuTexture) {
                    IntBuffer intBuffer = MemoryUtil.memAllocInt((int)(this.width() * 16));
                    UnihexProvider.unpackBitsToBytes(intBuffer, contents, left, right);
                    intBuffer.rewind();
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, MemoryUtil.memByteBuffer((IntBuffer)intBuffer), NativeImage.Format.RGBA, 0, 0, i, j, this.width(), 16);
                    MemoryUtil.memFree((Buffer)intBuffer);
                }

                @Override
                public boolean isColored() {
                    return true;
                }
            });
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Glyph.class, "contents;left;right", "contents", "left", "right"}, this, object);
        }

        public LineData contents() {
            return this.contents;
        }

        public int left() {
            return this.left;
        }

        public int right() {
            return this.right;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Definition
    implements GlyphProviderDefinition {
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("hex_file").forGetter(definition -> definition.hexFile), (App)OverrideRange.CODEC.listOf().optionalFieldOf("size_overrides", (Object)List.of()).forGetter(definition -> definition.sizeOverrides)).apply((Applicative)instance, Definition::new));
        private final Identifier hexFile;
        private final List<OverrideRange> sizeOverrides;

        private Definition(Identifier identifier, List<OverrideRange> list) {
            this.hexFile = identifier;
            this.sizeOverrides = list;
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.UNIHEX;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager resourceManager) throws IOException {
            try (InputStream inputStream = resourceManager.open(this.hexFile);){
                UnihexProvider unihexProvider = this.loadData(inputStream);
                return unihexProvider;
            }
        }

        private UnihexProvider loadData(InputStream inputStream) throws IOException {
            CodepointMap<LineData> codepointMap = new CodepointMap<LineData>(LineData[]::new, i -> new LineData[i][]);
            ReaderOutput readerOutput = codepointMap::put;
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream);){
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String string = zipEntry.getName();
                    if (!string.endsWith(".hex")) continue;
                    LOGGER.info("Found {}, loading", (Object)string);
                    UnihexProvider.readFromStream(new FastBufferedInputStream(zipInputStream), readerOutput);
                }
                CodepointMap<Glyph> codepointMap2 = new CodepointMap<Glyph>(Glyph[]::new, i -> new Glyph[i][]);
                for (OverrideRange overrideRange : this.sizeOverrides) {
                    int i2 = overrideRange.from;
                    int j = overrideRange.to;
                    Dimensions dimensions = overrideRange.dimensions;
                    for (int k = i2; k <= j; ++k) {
                        LineData lineData2 = (LineData)codepointMap.remove(k);
                        if (lineData2 == null) continue;
                        codepointMap2.put(k, new Glyph(lineData2, dimensions.left, dimensions.right));
                    }
                }
                codepointMap.forEach((i, lineData) -> {
                    int j = lineData.calculateWidth();
                    int k = Dimensions.left(j);
                    int l = Dimensions.right(j);
                    codepointMap2.put(i, new Glyph((LineData)lineData, k, l));
                });
                UnihexProvider unihexProvider = new UnihexProvider(codepointMap2);
                return unihexProvider;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Dimensions
    extends Record {
        final int left;
        final int right;
        public static final MapCodec<Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("left").forGetter(Dimensions::left), (App)Codec.INT.fieldOf("right").forGetter(Dimensions::right)).apply((Applicative)instance, Dimensions::new));
        public static final Codec<Dimensions> CODEC = MAP_CODEC.codec();

        public Dimensions(int i, int j) {
            this.left = i;
            this.right = j;
        }

        public int pack() {
            return Dimensions.pack(this.left, this.right);
        }

        public static int pack(int i, int j) {
            return (i & 0xFF) << 8 | j & 0xFF;
        }

        public static int left(int i) {
            return (byte)(i >> 8);
        }

        public static int right(int i) {
            return (byte)i;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Dimensions.class, "left;right", "left", "right"}, this, object);
        }

        public int left() {
            return this.left;
        }

        public int right() {
            return this.right;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class OverrideRange
    extends Record {
        final int from;
        final int to;
        final Dimensions dimensions;
        private static final Codec<OverrideRange> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(OverrideRange::from), (App)ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(OverrideRange::to), (App)Dimensions.MAP_CODEC.forGetter(OverrideRange::dimensions)).apply((Applicative)instance, OverrideRange::new));
        public static final Codec<OverrideRange> CODEC = RAW_CODEC.validate(overrideRange -> {
            if (overrideRange.from >= overrideRange.to) {
                return DataResult.error(() -> "Invalid range: [" + overrideRange.from + ";" + overrideRange.to + "]");
            }
            return DataResult.success((Object)overrideRange);
        });

        private OverrideRange(int i, int j, Dimensions dimensions) {
            this.from = i;
            this.to = j;
            this.dimensions = dimensions;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{OverrideRange.class, "from;to;dimensions", "from", "to", "dimensions"}, this, object);
        }

        public int from() {
            return this.from;
        }

        public int to() {
            return this.to;
        }

        public Dimensions dimensions() {
            return this.dimensions;
        }
    }
}

