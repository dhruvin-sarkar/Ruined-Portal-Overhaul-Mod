/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.AtlasGlyphProvider;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.PlayerGlyphProvider;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FontManager
implements PreparableReloadListener,
AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FONTS_PATH = "fonts.json";
    public static final Identifier MISSING_FONT = Identifier.withDefaultNamespace("missing");
    private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    final FontSet missingFontSet;
    private final List<GlyphProvider> providersToClose = new ArrayList<GlyphProvider>();
    private final Map<Identifier, FontSet> fontSets = new HashMap<Identifier, FontSet>();
    private final TextureManager textureManager;
    private final CachedFontProvider anyGlyphs = new CachedFontProvider(false);
    private final CachedFontProvider nonFishyGlyphs = new CachedFontProvider(true);
    private final AtlasManager atlasManager;
    private final Map<Identifier, AtlasGlyphProvider> atlasProviders = new HashMap<Identifier, AtlasGlyphProvider>();
    final PlayerGlyphProvider playerProvider;

    public FontManager(TextureManager textureManager, AtlasManager atlasManager, PlayerSkinRenderCache playerSkinRenderCache) {
        this.textureManager = textureManager;
        this.atlasManager = atlasManager;
        this.missingFontSet = this.createFontSet(MISSING_FONT, List.of((Object)FontManager.createFallbackProvider()), Set.of());
        this.playerProvider = new PlayerGlyphProvider(playerSkinRenderCache);
    }

    private FontSet createFontSet(Identifier identifier, List<GlyphProvider.Conditional> list, Set<FontOption> set) {
        GlyphStitcher glyphStitcher = new GlyphStitcher(this.textureManager, identifier);
        FontSet fontSet = new FontSet(glyphStitcher);
        fontSet.reload(list, set);
        return fontSet;
    }

    private static GlyphProvider.Conditional createFallbackProvider() {
        return new GlyphProvider.Conditional(new AllMissingGlyphProvider(), FontOption.Filter.ALWAYS_PASS);
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        return ((CompletableFuture)this.prepare(sharedState.resourceManager(), executor).thenCompose(preparationBarrier::wait)).thenAcceptAsync(preparation -> this.apply((Preparation)((Object)preparation), Profiler.get()), executor2);
    }

    private CompletableFuture<Preparation> prepare(ResourceManager resourceManager, Executor executor) {
        ArrayList<CompletableFuture<UnresolvedBuilderBundle>> list2 = new ArrayList<CompletableFuture<UnresolvedBuilderBundle>>();
        for (Map.Entry<Identifier, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourceManager).entrySet()) {
            Identifier identifier = FONT_DEFINITIONS.fileToId(entry.getKey());
            list2.add(CompletableFuture.supplyAsync(() -> {
                List<Pair<BuilderId, GlyphProviderDefinition.Conditional>> list = FontManager.loadResourceStack((List)entry.getValue(), identifier);
                UnresolvedBuilderBundle unresolvedBuilderBundle = new UnresolvedBuilderBundle(identifier);
                for (Pair<BuilderId, GlyphProviderDefinition.Conditional> pair : list) {
                    BuilderId builderId = (BuilderId)((Object)((Object)pair.getFirst()));
                    FontOption.Filter filter = ((GlyphProviderDefinition.Conditional)((Object)((Object)pair.getSecond()))).filter();
                    ((GlyphProviderDefinition.Conditional)((Object)((Object)pair.getSecond()))).definition().unpack().ifLeft(loader -> {
                        CompletableFuture<Optional<GlyphProvider>> completableFuture = this.safeLoad(builderId, (GlyphProviderDefinition.Loader)loader, resourceManager, executor);
                        unresolvedBuilderBundle.add(builderId, filter, completableFuture);
                    }).ifRight(reference -> unresolvedBuilderBundle.add(builderId, filter, (GlyphProviderDefinition.Reference)((Object)((Object)reference))));
                }
                return unresolvedBuilderBundle;
            }, executor));
        }
        return Util.sequence(list2).thenCompose(list -> {
            List list2 = list.stream().flatMap(UnresolvedBuilderBundle::listBuilders).collect(Util.toMutableList());
            GlyphProvider.Conditional conditional = FontManager.createFallbackProvider();
            list2.add(CompletableFuture.completedFuture(Optional.of(conditional.provider())));
            return Util.sequence(list2).thenCompose(list22 -> {
                Map<Identifier, List<GlyphProvider.Conditional>> map = this.resolveProviders((List<UnresolvedBuilderBundle>)list);
                CompletableFuture[] completableFutures = (CompletableFuture[])map.values().stream().map(list -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading((List<GlyphProvider.Conditional>)list, conditional), executor)).toArray(CompletableFuture[]::new);
                return CompletableFuture.allOf(completableFutures).thenApply(void_ -> {
                    List list2 = list22.stream().flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).toList();
                    return new Preparation(map, list2);
                });
            });
        });
    }

    private CompletableFuture<Optional<GlyphProvider>> safeLoad(BuilderId builderId, GlyphProviderDefinition.Loader loader, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(loader.load(resourceManager));
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load builder {}, rejecting", (Object)builderId, (Object)exception);
                return Optional.empty();
            }
        }, executor);
    }

    private Map<Identifier, List<GlyphProvider.Conditional>> resolveProviders(List<UnresolvedBuilderBundle> list) {
        HashMap<Identifier, List<GlyphProvider.Conditional>> map = new HashMap<Identifier, List<GlyphProvider.Conditional>>();
        DependencySorter<Identifier, UnresolvedBuilderBundle> dependencySorter = new DependencySorter<Identifier, UnresolvedBuilderBundle>();
        list.forEach(unresolvedBuilderBundle -> dependencySorter.addEntry(unresolvedBuilderBundle.fontId, (UnresolvedBuilderBundle)unresolvedBuilderBundle));
        dependencySorter.orderByDependencies((identifier, unresolvedBuilderBundle) -> unresolvedBuilderBundle.resolve(map::get).ifPresent(list -> map.put((Identifier)identifier, (List<GlyphProvider.Conditional>)list)));
        return map;
    }

    private void finalizeProviderLoading(List<GlyphProvider.Conditional> list, GlyphProvider.Conditional conditional) {
        list.add(0, conditional);
        IntOpenHashSet intSet = new IntOpenHashSet();
        for (GlyphProvider.Conditional conditional2 : list) {
            intSet.addAll((IntCollection)conditional2.provider().getSupportedGlyphs());
        }
        intSet.forEach(i -> {
            GlyphProvider.Conditional conditional;
            if (i == 32) {
                return;
            }
            Iterator iterator = Lists.reverse((List)list).iterator();
            while (iterator.hasNext() && (conditional = (GlyphProvider.Conditional)iterator.next()).provider().getGlyph(i) == null) {
            }
        });
    }

    private static Set<FontOption> getFontOptions(Options options) {
        EnumSet<FontOption> set = EnumSet.noneOf(FontOption.class);
        if (options.forceUnicodeFont().get().booleanValue()) {
            set.add(FontOption.UNIFORM);
        }
        if (options.japaneseGlyphVariants().get().booleanValue()) {
            set.add(FontOption.JAPANESE_VARIANTS);
        }
        return set;
    }

    private void apply(Preparation preparation, ProfilerFiller profilerFiller) {
        profilerFiller.push("closing");
        this.anyGlyphs.invalidate();
        this.nonFishyGlyphs.invalidate();
        this.fontSets.values().forEach(FontSet::close);
        this.fontSets.clear();
        this.providersToClose.forEach(GlyphProvider::close);
        this.providersToClose.clear();
        Set<FontOption> set = FontManager.getFontOptions(Minecraft.getInstance().options);
        profilerFiller.popPush("reloading");
        preparation.fontSets().forEach((identifier, list) -> this.fontSets.put((Identifier)identifier, this.createFontSet((Identifier)identifier, Lists.reverse((List)list), set)));
        this.providersToClose.addAll(preparation.allProviders);
        profilerFiller.pop();
        if (!this.fontSets.containsKey(Minecraft.DEFAULT_FONT)) {
            throw new IllegalStateException("Default font failed to load");
        }
        this.atlasProviders.clear();
        this.atlasManager.forEach((identifier, textureAtlas) -> this.atlasProviders.put((Identifier)identifier, new AtlasGlyphProvider((TextureAtlas)textureAtlas)));
    }

    public void updateOptions(Options options) {
        Set<FontOption> set = FontManager.getFontOptions(options);
        for (FontSet fontSet : this.fontSets.values()) {
            fontSet.reload(set);
        }
    }

    private static List<Pair<BuilderId, GlyphProviderDefinition.Conditional>> loadResourceStack(List<Resource> list, Identifier identifier) {
        ArrayList<Pair<BuilderId, GlyphProviderDefinition.Conditional>> list2 = new ArrayList<Pair<BuilderId, GlyphProviderDefinition.Conditional>>();
        for (Resource resource : list) {
            try {
                BufferedReader reader = resource.openAsReader();
                try {
                    JsonElement jsonElement = (JsonElement)GSON.fromJson((Reader)reader, JsonElement.class);
                    FontDefinitionFile fontDefinitionFile = (FontDefinitionFile)((Object)FontDefinitionFile.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonParseException::new));
                    List<GlyphProviderDefinition.Conditional> list3 = fontDefinitionFile.providers;
                    for (int i = list3.size() - 1; i >= 0; --i) {
                        BuilderId builderId = new BuilderId(identifier, resource.sourcePackId(), i);
                        list2.add((Pair<BuilderId, GlyphProviderDefinition.Conditional>)Pair.of((Object)((Object)builderId), (Object)((Object)list3.get(i))));
                    }
                }
                finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", new Object[]{identifier, FONTS_PATH, resource.sourcePackId(), exception});
            }
        }
        return list2;
    }

    public Font createFont() {
        return new Font(this.anyGlyphs);
    }

    public Font createFontFilterFishy() {
        return new Font(this.nonFishyGlyphs);
    }

    FontSet getFontSetRaw(Identifier identifier) {
        return this.fontSets.getOrDefault(identifier, this.missingFontSet);
    }

    GlyphSource getSpriteFont(FontDescription.AtlasSprite atlasSprite) {
        AtlasGlyphProvider atlasGlyphProvider = this.atlasProviders.get(atlasSprite.atlasId());
        if (atlasGlyphProvider == null) {
            return this.missingFontSet.source(false);
        }
        return atlasGlyphProvider.sourceForSprite(atlasSprite.spriteId());
    }

    @Override
    public void close() {
        this.anyGlyphs.close();
        this.nonFishyGlyphs.close();
        this.fontSets.values().forEach(FontSet::close);
        this.providersToClose.forEach(GlyphProvider::close);
        this.missingFontSet.close();
    }

    @Environment(value=EnvType.CLIENT)
    class CachedFontProvider
    implements Font.Provider,
    AutoCloseable {
        private final boolean nonFishyOnly;
        private volatile @Nullable CachedEntry lastEntry;
        private volatile @Nullable EffectGlyph whiteGlyph;

        CachedFontProvider(boolean bl) {
            this.nonFishyOnly = bl;
        }

        public void invalidate() {
            this.lastEntry = null;
            this.whiteGlyph = null;
        }

        @Override
        public void close() {
            this.invalidate();
        }

        private GlyphSource getGlyphSource(FontDescription fontDescription) {
            FontDescription fontDescription2 = fontDescription;
            Objects.requireNonNull(fontDescription2);
            FontDescription fontDescription3 = fontDescription2;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FontDescription.Resource.class, FontDescription.AtlasSprite.class, FontDescription.PlayerSprite.class}, (Object)fontDescription3, (int)n)) {
                case 0 -> {
                    FontDescription.Resource resource = (FontDescription.Resource)fontDescription3;
                    yield FontManager.this.getFontSetRaw(resource.id()).source(this.nonFishyOnly);
                }
                case 1 -> {
                    FontDescription.AtlasSprite atlasSprite = (FontDescription.AtlasSprite)fontDescription3;
                    yield FontManager.this.getSpriteFont(atlasSprite);
                }
                case 2 -> {
                    FontDescription.PlayerSprite playerSprite = (FontDescription.PlayerSprite)fontDescription3;
                    yield FontManager.this.playerProvider.sourceForPlayer(playerSprite);
                }
                default -> FontManager.this.missingFontSet.source(this.nonFishyOnly);
            };
        }

        @Override
        public GlyphSource glyphs(FontDescription fontDescription) {
            CachedEntry cachedEntry = this.lastEntry;
            if (cachedEntry != null && fontDescription.equals(cachedEntry.description)) {
                return cachedEntry.source;
            }
            GlyphSource glyphSource = this.getGlyphSource(fontDescription);
            this.lastEntry = new CachedEntry(fontDescription, glyphSource);
            return glyphSource;
        }

        @Override
        public EffectGlyph effect() {
            EffectGlyph effectGlyph = this.whiteGlyph;
            if (effectGlyph == null) {
                this.whiteGlyph = effectGlyph = FontManager.this.getFontSetRaw(FontDescription.DEFAULT.id()).whiteGlyph();
            }
            return effectGlyph;
        }

        @Environment(value=EnvType.CLIENT)
        static final class CachedEntry
        extends Record {
            final FontDescription description;
            final GlyphSource source;

            CachedEntry(FontDescription fontDescription, GlyphSource glyphSource) {
                this.description = fontDescription;
                this.source = glyphSource;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{CachedEntry.class, "description;source", "description", "source"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CachedEntry.class, "description;source", "description", "source"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CachedEntry.class, "description;source", "description", "source"}, this, object);
            }

            public FontDescription description() {
                return this.description;
            }

            public GlyphSource source() {
                return this.source;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    record BuilderId(Identifier fontId, String pack, int index) {
        public String toString() {
            return "(" + String.valueOf(this.fontId) + ": builder #" + this.index + " from pack " + this.pack + ")";
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class Preparation
    extends Record {
        private final Map<Identifier, List<GlyphProvider.Conditional>> fontSets;
        final List<GlyphProvider> allProviders;

        Preparation(Map<Identifier, List<GlyphProvider.Conditional>> map, List<GlyphProvider> list) {
            this.fontSets = map;
            this.allProviders = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this, object);
        }

        public Map<Identifier, List<GlyphProvider.Conditional>> fontSets() {
            return this.fontSets;
        }

        public List<GlyphProvider> allProviders() {
            return this.allProviders;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class FontDefinitionFile
    extends Record {
        final List<GlyphProviderDefinition.Conditional> providers;
        public static final Codec<FontDefinitionFile> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GlyphProviderDefinition.Conditional.CODEC.listOf().fieldOf("providers").forGetter(FontDefinitionFile::providers)).apply((Applicative)instance, FontDefinitionFile::new));

        private FontDefinitionFile(List<GlyphProviderDefinition.Conditional> list) {
            this.providers = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this, object);
        }

        public List<GlyphProviderDefinition.Conditional> providers() {
            return this.providers;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class UnresolvedBuilderBundle
    extends Record
    implements DependencySorter.Entry<Identifier> {
        final Identifier fontId;
        private final List<BuilderResult> builders;
        private final Set<Identifier> dependencies;

        public UnresolvedBuilderBundle(Identifier identifier) {
            this(identifier, new ArrayList<BuilderResult>(), new HashSet<Identifier>());
        }

        private UnresolvedBuilderBundle(Identifier identifier, List<BuilderResult> list, Set<Identifier> set) {
            this.fontId = identifier;
            this.builders = list;
            this.dependencies = set;
        }

        public void add(BuilderId builderId, FontOption.Filter filter, GlyphProviderDefinition.Reference reference) {
            this.builders.add(new BuilderResult(builderId, filter, (Either<CompletableFuture<Optional<GlyphProvider>>, Identifier>)Either.right((Object)reference.id())));
            this.dependencies.add(reference.id());
        }

        public void add(BuilderId builderId, FontOption.Filter filter, CompletableFuture<Optional<GlyphProvider>> completableFuture) {
            this.builders.add(new BuilderResult(builderId, filter, (Either<CompletableFuture<Optional<GlyphProvider>>, Identifier>)Either.left(completableFuture)));
        }

        private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
            return this.builders.stream().flatMap(builderResult -> builderResult.result.left().stream());
        }

        public Optional<List<GlyphProvider.Conditional>> resolve(Function<Identifier, List<GlyphProvider.Conditional>> function) {
            ArrayList list = new ArrayList();
            for (BuilderResult builderResult : this.builders) {
                Optional<List<GlyphProvider.Conditional>> optional = builderResult.resolve(function);
                if (optional.isPresent()) {
                    list.addAll(optional.get());
                    continue;
                }
                return Optional.empty();
            }
            return Optional.of(list);
        }

        @Override
        public void visitRequiredDependencies(Consumer<Identifier> consumer) {
            this.dependencies.forEach(consumer);
        }

        @Override
        public void visitOptionalDependencies(Consumer<Identifier> consumer) {
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this, object);
        }

        public Identifier fontId() {
            return this.fontId;
        }

        public List<BuilderResult> builders() {
            return this.builders;
        }

        public Set<Identifier> dependencies() {
            return this.dependencies;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class BuilderResult
    extends Record {
        private final BuilderId id;
        private final FontOption.Filter filter;
        final Either<CompletableFuture<Optional<GlyphProvider>>, Identifier> result;

        BuilderResult(BuilderId builderId, FontOption.Filter filter, Either<CompletableFuture<Optional<GlyphProvider>>, Identifier> either) {
            this.id = builderId;
            this.filter = filter;
            this.result = either;
        }

        public Optional<List<GlyphProvider.Conditional>> resolve(Function<Identifier, @Nullable List<GlyphProvider.Conditional>> function) {
            return (Optional)this.result.map(completableFuture -> ((Optional)completableFuture.join()).map(glyphProvider -> List.of((Object)new GlyphProvider.Conditional((GlyphProvider)glyphProvider, this.filter))), identifier -> {
                List list = (List)function.apply((Identifier)identifier);
                if (list == null) {
                    LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", identifier, (Object)this.id);
                    return Optional.empty();
                }
                return Optional.of(list.stream().map(this::mergeFilters).toList());
            });
        }

        private GlyphProvider.Conditional mergeFilters(GlyphProvider.Conditional conditional) {
            return new GlyphProvider.Conditional(conditional.provider(), this.filter.merge(conditional.filter()));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this, object);
        }

        public BuilderId id() {
            return this.id;
        }

        public FontOption.Filter filter() {
            return this.filter;
        }

        public Either<CompletableFuture<Optional<GlyphProvider>>, Identifier> result() {
            return this.result;
        }
    }
}

