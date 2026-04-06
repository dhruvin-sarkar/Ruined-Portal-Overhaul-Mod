/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FontSet
implements AutoCloseable {
    private static final float LARGE_FORWARD_ADVANCE = 32.0f;
    private static final BakedGlyph INVISIBLE_MISSING_GLYPH = new BakedGlyph(){

        @Override
        public GlyphInfo info() {
            return SpecialGlyphs.MISSING;
        }

        @Override
        public @Nullable TextRenderable.Styled createGlyph(float f, float g, int i, int j, Style style, float h, float k) {
            return null;
        }
    };
    final GlyphStitcher stitcher;
    final UnbakedGlyph.Stitcher wrappedStitcher = new UnbakedGlyph.Stitcher(){

        @Override
        public BakedGlyph stitch(GlyphInfo glyphInfo, GlyphBitmap glyphBitmap) {
            return (BakedGlyph)Objects.requireNonNullElse((Object)FontSet.this.stitcher.stitch(glyphInfo, glyphBitmap), (Object)FontSet.this.missingGlyph);
        }

        @Override
        public BakedGlyph getMissing() {
            return FontSet.this.missingGlyph;
        }
    };
    private List<GlyphProvider.Conditional> allProviders = List.of();
    private List<GlyphProvider> activeProviders = List.of();
    private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap();
    private final CodepointMap<SelectedGlyphs> glyphCache = new CodepointMap(SelectedGlyphs[]::new, i -> new SelectedGlyphs[i][]);
    private final IntFunction<SelectedGlyphs> glyphGetter = this::computeGlyphInfo;
    BakedGlyph missingGlyph = INVISIBLE_MISSING_GLYPH;
    private final Supplier<BakedGlyph> missingGlyphGetter = () -> this.missingGlyph;
    private final SelectedGlyphs missingSelectedGlyphs = new SelectedGlyphs(this.missingGlyphGetter, this.missingGlyphGetter);
    private @Nullable EffectGlyph whiteGlyph;
    private final GlyphSource anyGlyphs = new Source(false);
    private final GlyphSource nonFishyGlyphs = new Source(true);

    public FontSet(GlyphStitcher glyphStitcher) {
        this.stitcher = glyphStitcher;
    }

    public void reload(List<GlyphProvider.Conditional> list, Set<FontOption> set) {
        this.allProviders = list;
        this.reload(set);
    }

    public void reload(Set<FontOption> set) {
        this.activeProviders = List.of();
        this.resetTextures();
        this.activeProviders = this.selectProviders(this.allProviders, set);
    }

    private void resetTextures() {
        this.stitcher.reset();
        this.glyphCache.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = Objects.requireNonNull(SpecialGlyphs.MISSING.bake(this.stitcher));
        this.whiteGlyph = SpecialGlyphs.WHITE.bake(this.stitcher);
    }

    private List<GlyphProvider> selectProviders(List<GlyphProvider.Conditional> list, Set<FontOption> set) {
        IntOpenHashSet intSet = new IntOpenHashSet();
        ArrayList<GlyphProvider> list2 = new ArrayList<GlyphProvider>();
        for (GlyphProvider.Conditional conditional : list) {
            if (!conditional.filter().apply(set)) continue;
            list2.add(conditional.provider());
            intSet.addAll((IntCollection)conditional.provider().getSupportedGlyphs());
        }
        HashSet set2 = Sets.newHashSet();
        intSet.forEach(i2 -> {
            for (GlyphProvider glyphProvider : list2) {
                UnbakedGlyph unbakedGlyph = glyphProvider.getGlyph(i2);
                if (unbakedGlyph == null) continue;
                set2.add(glyphProvider);
                if (unbakedGlyph.info() == SpecialGlyphs.MISSING) break;
                ((IntList)this.glyphsByWidth.computeIfAbsent(Mth.ceil(unbakedGlyph.info().getAdvance(false)), i -> new IntArrayList())).add(i2);
                break;
            }
        });
        return list2.stream().filter(set2::contains).toList();
    }

    @Override
    public void close() {
        this.stitcher.close();
    }

    private static boolean hasFishyAdvance(GlyphInfo glyphInfo) {
        float f = glyphInfo.getAdvance(false);
        if (f < 0.0f || f > 32.0f) {
            return true;
        }
        float g = glyphInfo.getAdvance(true);
        return g < 0.0f || g > 32.0f;
    }

    private SelectedGlyphs computeGlyphInfo(int i) {
        DelayedBake delayedBake = null;
        for (GlyphProvider glyphProvider : this.activeProviders) {
            UnbakedGlyph unbakedGlyph = glyphProvider.getGlyph(i);
            if (unbakedGlyph == null) continue;
            if (delayedBake == null) {
                delayedBake = new DelayedBake(unbakedGlyph);
            }
            if (FontSet.hasFishyAdvance(unbakedGlyph.info())) continue;
            if (delayedBake.unbaked == unbakedGlyph) {
                return new SelectedGlyphs(delayedBake, delayedBake);
            }
            return new SelectedGlyphs(delayedBake, new DelayedBake(unbakedGlyph));
        }
        if (delayedBake != null) {
            return new SelectedGlyphs(delayedBake, this.missingGlyphGetter);
        }
        return this.missingSelectedGlyphs;
    }

    SelectedGlyphs getGlyph(int i) {
        return this.glyphCache.computeIfAbsent(i, this.glyphGetter);
    }

    public BakedGlyph getRandomGlyph(RandomSource randomSource, int i) {
        IntList intList = (IntList)this.glyphsByWidth.get(i);
        if (intList != null && !intList.isEmpty()) {
            return this.getGlyph(intList.getInt(randomSource.nextInt(intList.size()))).nonFishy().get();
        }
        return this.missingGlyph;
    }

    public EffectGlyph whiteGlyph() {
        return Objects.requireNonNull(this.whiteGlyph);
    }

    public GlyphSource source(boolean bl) {
        return bl ? this.nonFishyGlyphs : this.anyGlyphs;
    }

    @Environment(value=EnvType.CLIENT)
    record SelectedGlyphs(Supplier<BakedGlyph> any, Supplier<BakedGlyph> nonFishy) {
        Supplier<BakedGlyph> select(boolean bl) {
            return bl ? this.nonFishy : this.any;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class Source
    implements GlyphSource {
        private final boolean filterFishyGlyphs;

        public Source(boolean bl) {
            this.filterFishyGlyphs = bl;
        }

        @Override
        public BakedGlyph getGlyph(int i) {
            return FontSet.this.getGlyph(i).select(this.filterFishyGlyphs).get();
        }

        @Override
        public BakedGlyph getRandomGlyph(RandomSource randomSource, int i) {
            return FontSet.this.getRandomGlyph(randomSource, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class DelayedBake
    implements Supplier<BakedGlyph> {
        final UnbakedGlyph unbaked;
        private @Nullable BakedGlyph baked;

        DelayedBake(UnbakedGlyph unbakedGlyph) {
            this.unbaked = unbakedGlyph;
        }

        @Override
        public BakedGlyph get() {
            if (this.baked == null) {
                this.baked = this.unbaked.bake(FontSet.this.wrappedStitcher);
            }
            return this.baked;
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }
}

