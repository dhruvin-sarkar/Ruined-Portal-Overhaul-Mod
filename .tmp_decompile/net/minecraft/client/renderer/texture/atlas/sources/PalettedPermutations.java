/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record PalettedPermutations(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations, String separator) implements SpriteSource
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_SEPARATOR = "_";
    public static final MapCodec<PalettedPermutations> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.list(Identifier.CODEC).fieldOf("textures").forGetter(PalettedPermutations::textures), (App)Identifier.CODEC.fieldOf("palette_key").forGetter(PalettedPermutations::paletteKey), (App)Codec.unboundedMap((Codec)Codec.STRING, Identifier.CODEC).fieldOf("permutations").forGetter(PalettedPermutations::permutations), (App)Codec.STRING.optionalFieldOf("separator", (Object)DEFAULT_SEPARATOR).forGetter(PalettedPermutations::separator)).apply((Applicative)instance, PalettedPermutations::new));

    public PalettedPermutations(List<Identifier> list, Identifier identifier, Map<String, Identifier> map) {
        this(list, identifier, map, DEFAULT_SEPARATOR);
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Supplier supplier = Suppliers.memoize(() -> PalettedPermutations.loadPaletteEntryFromImage(resourceManager, this.paletteKey));
        HashMap map = new HashMap();
        this.permutations.forEach((arg_0, arg_1) -> PalettedPermutations.method_48490(map, (java.util.function.Supplier)supplier, resourceManager, arg_0, arg_1));
        for (Identifier identifier : this.textures) {
            Identifier identifier2 = TEXTURE_ID_CONVERTER.idToFile(identifier);
            Optional<Resource> optional = resourceManager.getResource(identifier2);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", (Object)identifier2);
                continue;
            }
            LazyLoadedImage lazyLoadedImage = new LazyLoadedImage(identifier2, optional.get(), map.size());
            for (Map.Entry entry : map.entrySet()) {
                Identifier identifier3 = identifier.withSuffix(this.separator + (String)entry.getKey());
                output.add(identifier3, new PalettedSpriteSupplier(lazyLoadedImage, (java.util.function.Supplier)entry.getValue(), identifier3));
            }
        }
    }

    private static IntUnaryOperator createPaletteMapping(int[] is, int[] js) {
        if (js.length != is.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", (Object)is.length, (Object)js.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap(js.length);
        for (int i = 0; i < is.length; ++i) {
            int j = is[i];
            if (ARGB.alpha(j) == 0) continue;
            int2IntMap.put(ARGB.transparent(j), js[i]);
        }
        return arg_0 -> PalettedPermutations.method_48489((Int2IntMap)int2IntMap, arg_0);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static int[] loadPaletteEntryFromImage(ResourceManager resourceManager, Identifier identifier) {
        Optional<Resource> optional = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(identifier));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", (Object)identifier);
            throw new IllegalArgumentException();
        }
        try (InputStream inputStream = optional.get().open();){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int[] nArray = nativeImage.getPixels();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", (Object)identifier, (Object)exception);
            throw new IllegalArgumentException();
        }
    }

    public MapCodec<PalettedPermutations> codec() {
        return MAP_CODEC;
    }

    private static /* synthetic */ int method_48489(Int2IntMap int2IntMap, int i) {
        int j = ARGB.alpha(i);
        if (j == 0) {
            return i;
        }
        int k = ARGB.transparent(i);
        int l = int2IntMap.getOrDefault(k, ARGB.opaque(k));
        int m = ARGB.alpha(l);
        return ARGB.color(j * m / 255, l);
    }

    private static /* synthetic */ void method_48490(Map map, java.util.function.Supplier supplier, ResourceManager resourceManager, String string, Identifier identifier) {
        map.put(string, Suppliers.memoize(() -> PalettedPermutations.method_48491((java.util.function.Supplier)supplier, resourceManager, identifier)));
    }

    private static /* synthetic */ IntUnaryOperator method_48491(java.util.function.Supplier supplier, ResourceManager resourceManager, Identifier identifier) {
        return PalettedPermutations.createPaletteMapping((int[])supplier.get(), PalettedPermutations.loadPaletteEntryFromImage(resourceManager, identifier));
    }

    @Environment(value=EnvType.CLIENT)
    record PalettedSpriteSupplier(LazyLoadedImage baseImage, java.util.function.Supplier<IntUnaryOperator> palette, Identifier permutationLocation) implements SpriteSource.DiscardableLoader
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public @Nullable SpriteContents get(SpriteResourceLoader spriteResourceLoader) {
            try {
                NativeImage nativeImage = this.baseImage.get().mappedCopy(this.palette.get());
                SpriteContents spriteContents = new SpriteContents(this.permutationLocation, new FrameSize(nativeImage.getWidth(), nativeImage.getHeight()), nativeImage);
                return spriteContents;
            }
            catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("unable to apply palette to {}", (Object)this.permutationLocation, (Object)exception);
                SpriteContents spriteContents = null;
                return spriteContents;
            }
            finally {
                this.baseImage.release();
            }
        }

        @Override
        public void discard() {
            this.baseImage.release();
        }
    }
}

