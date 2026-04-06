/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier location;
    private final int maxSupportedTextureSize;

    public SpriteLoader(Identifier identifier, int i) {
        this.location = identifier;
        this.maxSupportedTextureSize = i;
    }

    public static SpriteLoader create(TextureAtlas textureAtlas) {
        return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize());
    }

    private Preparations stitch(List<SpriteContents> list, int i, Executor executor) {
        try (Zone zone = Profiler.get().zone(() -> "stitch " + String.valueOf(this.location));){
            int m;
            int j = this.maxSupportedTextureSize;
            int k = Integer.MAX_VALUE;
            int l = 1 << i;
            for (SpriteContents spriteContents : list) {
                k = Math.min(k, Math.min(spriteContents.width(), spriteContents.height()));
                m = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
                if (m >= l) continue;
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[]{spriteContents.name(), spriteContents.width(), spriteContents.height(), Mth.log2(l), Mth.log2(m)});
                l = m;
            }
            int n = Math.min(k, l);
            int o = Mth.log2(n);
            if (o < i) {
                LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{this.location, i, o, n});
                m = o;
            } else {
                m = i;
            }
            Options options = Minecraft.getInstance().options;
            int p = m == 0 || options.textureFiltering().get() != TextureFilteringMethod.ANISOTROPIC ? 0 : options.maxAnisotropyBit().get();
            Stitcher<SpriteContents> stitcher = new Stitcher<SpriteContents>(j, j, m, p);
            for (SpriteContents spriteContents2 : list) {
                stitcher.registerSprite(spriteContents2);
            }
            try {
                stitcher.stitch();
            }
            catch (StitcherException stitcherException) {
                CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
                crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height())).collect(Collectors.joining(",")));
                crashReportCategory.setDetail("Max Texture Size", j);
                throw new ReportedException(crashReport);
            }
            int q = stitcher.getWidth();
            int r = stitcher.getHeight();
            Map<Identifier, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, q, r);
            TextureAtlasSprite textureAtlasSprite = map.get(MissingTextureAtlasSprite.getLocation());
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> map.values().forEach(textureAtlasSprite -> textureAtlasSprite.contents().increaseMipLevel(m)), executor);
            Preparations preparations = new Preparations(q, r, m, textureAtlasSprite, map, completableFuture);
            return preparations;
        }
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    private static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(SpriteResourceLoader spriteResourceLoader, List<SpriteSource.Loader> list2, Executor executor) {
        @Nullable List list22 = list2.stream().map(loader -> CompletableFuture.supplyAsync(() -> loader.get(spriteResourceLoader), executor)).toList();
        return Util.sequence(list22).thenApply(list -> list.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<Preparations> loadAndStitch(ResourceManager resourceManager, Identifier identifier, int i, Executor executor, Set<MetadataSectionType<?>> set) {
        SpriteResourceLoader spriteResourceLoader = SpriteResourceLoader.create(set);
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> SpriteSourceList.load(resourceManager, identifier).list(resourceManager), executor).thenCompose(list -> SpriteLoader.runSpriteSuppliers(spriteResourceLoader, list, executor))).thenApply(list -> this.stitch((List<SpriteContents>)list, i, executor));
    }

    private Map<Identifier, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int i, int j) {
        HashMap<Identifier, TextureAtlasSprite> map = new HashMap<Identifier, TextureAtlasSprite>();
        stitcher.gatherSprites((spriteContents, k, l, m) -> map.put(spriteContents.name(), new TextureAtlasSprite(this.location, (SpriteContents)spriteContents, i, j, k, l, m)));
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<Identifier, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
        public @Nullable TextureAtlasSprite getSprite(Identifier identifier) {
            return this.regions.get(identifier);
        }
    }
}

