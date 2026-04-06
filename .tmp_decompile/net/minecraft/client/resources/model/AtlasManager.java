/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class AtlasManager
implements PreparableReloadListener,
MaterialSet,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<AtlasConfig> KNOWN_ATLASES = List.of((Object[])new AtlasConfig[]{new AtlasConfig(Sheets.ARMOR_TRIMS_SHEET, AtlasIds.ARMOR_TRIMS, false), new AtlasConfig(Sheets.BANNER_SHEET, AtlasIds.BANNER_PATTERNS, false), new AtlasConfig(Sheets.BED_SHEET, AtlasIds.BEDS, false), new AtlasConfig(TextureAtlas.LOCATION_BLOCKS, AtlasIds.BLOCKS, true), new AtlasConfig(TextureAtlas.LOCATION_ITEMS, AtlasIds.ITEMS, false), new AtlasConfig(Sheets.CHEST_SHEET, AtlasIds.CHESTS, false), new AtlasConfig(Sheets.DECORATED_POT_SHEET, AtlasIds.DECORATED_POT, false), new AtlasConfig(Sheets.GUI_SHEET, AtlasIds.GUI, false, Set.of(GuiMetadataSection.TYPE)), new AtlasConfig(Sheets.MAP_DECORATIONS_SHEET, AtlasIds.MAP_DECORATIONS, false), new AtlasConfig(Sheets.PAINTINGS_SHEET, AtlasIds.PAINTINGS, false), new AtlasConfig(TextureAtlas.LOCATION_PARTICLES, AtlasIds.PARTICLES, false), new AtlasConfig(Sheets.SHIELD_SHEET, AtlasIds.SHIELD_PATTERNS, false), new AtlasConfig(Sheets.SHULKER_SHEET, AtlasIds.SHULKER_BOXES, false), new AtlasConfig(Sheets.SIGN_SHEET, AtlasIds.SIGNS, false), new AtlasConfig(Sheets.CELESTIAL_SHEET, AtlasIds.CELESTIALS, false)});
    public static final PreparableReloadListener.StateKey<PendingStitchResults> PENDING_STITCH = new PreparableReloadListener.StateKey();
    private final Map<Identifier, AtlasEntry> atlasByTexture = new HashMap<Identifier, AtlasEntry>();
    private final Map<Identifier, AtlasEntry> atlasById = new HashMap<Identifier, AtlasEntry>();
    private Map<Material, TextureAtlasSprite> materialLookup = Map.of();
    private int maxMipmapLevels;

    public AtlasManager(TextureManager textureManager, int i) {
        for (AtlasConfig atlasConfig : KNOWN_ATLASES) {
            TextureAtlas textureAtlas = new TextureAtlas(atlasConfig.textureId);
            textureManager.register(atlasConfig.textureId, textureAtlas);
            AtlasEntry atlasEntry = new AtlasEntry(textureAtlas, atlasConfig);
            this.atlasByTexture.put(atlasConfig.textureId, atlasEntry);
            this.atlasById.put(atlasConfig.definitionLocation, atlasEntry);
        }
        this.maxMipmapLevels = i;
    }

    public TextureAtlas getAtlasOrThrow(Identifier identifier) {
        AtlasEntry atlasEntry = this.atlasById.get(identifier);
        if (atlasEntry == null) {
            throw new IllegalArgumentException("Invalid atlas id: " + String.valueOf(identifier));
        }
        return atlasEntry.atlas();
    }

    public void forEach(BiConsumer<Identifier, TextureAtlas> biConsumer) {
        this.atlasById.forEach((? super K identifier, ? super V atlasEntry) -> biConsumer.accept((Identifier)identifier, atlasEntry.atlas));
    }

    public void updateMaxMipLevel(int i) {
        this.maxMipmapLevels = i;
    }

    @Override
    public void close() {
        this.materialLookup = Map.of();
        this.atlasById.values().forEach(AtlasEntry::close);
        this.atlasById.clear();
        this.atlasByTexture.clear();
    }

    @Override
    public TextureAtlasSprite get(Material material) {
        TextureAtlasSprite textureAtlasSprite = this.materialLookup.get(material);
        if (textureAtlasSprite != null) {
            return textureAtlasSprite;
        }
        Identifier identifier = material.atlasLocation();
        AtlasEntry atlasEntry = this.atlasByTexture.get(identifier);
        if (atlasEntry == null) {
            throw new IllegalArgumentException("Invalid atlas texture id: " + String.valueOf(identifier));
        }
        return atlasEntry.atlas().missingSprite();
    }

    @Override
    public void prepareSharedState(PreparableReloadListener.SharedState sharedState) {
        int i = this.atlasById.size();
        ArrayList<PendingStitch> list = new ArrayList<PendingStitch>(i);
        HashMap<Identifier, CompletableFuture<SpriteLoader.Preparations>> map = new HashMap<Identifier, CompletableFuture<SpriteLoader.Preparations>>(i);
        ArrayList list2 = new ArrayList(i);
        this.atlasById.forEach((? super K identifier, ? super V atlasEntry) -> {
            CompletableFuture<SpriteLoader.Preparations> completableFuture = new CompletableFuture<SpriteLoader.Preparations>();
            map.put((Identifier)identifier, completableFuture);
            list.add(new PendingStitch((AtlasEntry)atlasEntry, completableFuture));
            list2.add(completableFuture.thenCompose(SpriteLoader.Preparations::readyForUpload));
        });
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])list2.toArray(CompletableFuture[]::new));
        sharedState.set(PENDING_STITCH, new PendingStitchResults(list, map, completableFuture));
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        PendingStitchResults pendingStitchResults = sharedState.get(PENDING_STITCH);
        ResourceManager resourceManager = sharedState.resourceManager();
        pendingStitchResults.pendingStitches.forEach((? super T pendingStitch) -> pendingStitch.entry.scheduleLoad(resourceManager, executor, this.maxMipmapLevels).whenComplete((preparations, throwable) -> {
            if (preparations != null) {
                pendingStitch.preparations.complete((SpriteLoader.Preparations)((Object)((Object)preparations)));
            } else {
                pendingStitch.preparations.completeExceptionally((Throwable)throwable);
            }
        }));
        return ((CompletableFuture)pendingStitchResults.allReadyToUpload.thenCompose(preparationBarrier::wait)).thenAcceptAsync(object -> this.updateSpriteMaps(pendingStitchResults), executor2);
    }

    private void updateSpriteMaps(PendingStitchResults pendingStitchResults) {
        this.materialLookup = pendingStitchResults.joinAndUpload();
        HashMap map = new HashMap();
        this.materialLookup.forEach((? super K material, ? super V textureAtlasSprite) -> {
            TextureAtlasSprite textureAtlasSprite2;
            if (!material.texture().equals(MissingTextureAtlasSprite.getLocation()) && (textureAtlasSprite2 = map.putIfAbsent(material.texture(), textureAtlasSprite)) != null) {
                LOGGER.warn("Duplicate sprite {} from atlas {}, already defined in atlas {}. This will be rejected in a future version", new Object[]{material.texture(), material.atlasLocation(), textureAtlasSprite2.atlasLocation()});
            }
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static final class AtlasConfig
    extends Record {
        final Identifier textureId;
        final Identifier definitionLocation;
        final boolean createMipmaps;
        final Set<MetadataSectionType<?>> additionalMetadata;

        public AtlasConfig(Identifier identifier, Identifier identifier2, boolean bl) {
            this(identifier, identifier2, bl, Set.of());
        }

        public AtlasConfig(Identifier identifier, Identifier identifier2, boolean bl, Set<MetadataSectionType<?>> set) {
            this.textureId = identifier;
            this.definitionLocation = identifier2;
            this.createMipmaps = bl;
            this.additionalMetadata = set;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{AtlasConfig.class, "textureId;definitionLocation;createMipmaps;additionalMetadata", "textureId", "definitionLocation", "createMipmaps", "additionalMetadata"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AtlasConfig.class, "textureId;definitionLocation;createMipmaps;additionalMetadata", "textureId", "definitionLocation", "createMipmaps", "additionalMetadata"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AtlasConfig.class, "textureId;definitionLocation;createMipmaps;additionalMetadata", "textureId", "definitionLocation", "createMipmaps", "additionalMetadata"}, this, object);
        }

        public Identifier textureId() {
            return this.textureId;
        }

        public Identifier definitionLocation() {
            return this.definitionLocation;
        }

        public boolean createMipmaps() {
            return this.createMipmaps;
        }

        public Set<MetadataSectionType<?>> additionalMetadata() {
            return this.additionalMetadata;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class AtlasEntry
    extends Record
    implements AutoCloseable {
        final TextureAtlas atlas;
        final AtlasConfig config;

        AtlasEntry(TextureAtlas textureAtlas, AtlasConfig atlasConfig) {
            this.atlas = textureAtlas;
            this.config = atlasConfig;
        }

        @Override
        public void close() {
            this.atlas.clearTextureData();
        }

        CompletableFuture<SpriteLoader.Preparations> scheduleLoad(ResourceManager resourceManager, Executor executor, int i) {
            return SpriteLoader.create(this.atlas).loadAndStitch(resourceManager, this.config.definitionLocation, this.config.createMipmaps ? i : 0, executor, this.config.additionalMetadata);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{AtlasEntry.class, "atlas;config", "atlas", "config"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AtlasEntry.class, "atlas;config", "atlas", "config"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AtlasEntry.class, "atlas;config", "atlas", "config"}, this, object);
        }

        public TextureAtlas atlas() {
            return this.atlas;
        }

        public AtlasConfig config() {
            return this.config;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class PendingStitchResults {
        final List<PendingStitch> pendingStitches;
        private final Map<Identifier, CompletableFuture<SpriteLoader.Preparations>> stitchFuturesById;
        final CompletableFuture<?> allReadyToUpload;

        PendingStitchResults(List<PendingStitch> list, Map<Identifier, CompletableFuture<SpriteLoader.Preparations>> map, CompletableFuture<?> completableFuture) {
            this.pendingStitches = list;
            this.stitchFuturesById = map;
            this.allReadyToUpload = completableFuture;
        }

        public Map<Material, TextureAtlasSprite> joinAndUpload() {
            HashMap<Material, TextureAtlasSprite> map = new HashMap<Material, TextureAtlasSprite>();
            this.pendingStitches.forEach(pendingStitch -> pendingStitch.joinAndUpload(map));
            return map;
        }

        public CompletableFuture<SpriteLoader.Preparations> get(Identifier identifier) {
            return Objects.requireNonNull(this.stitchFuturesById.get(identifier));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class PendingStitch
    extends Record {
        final AtlasEntry entry;
        final CompletableFuture<SpriteLoader.Preparations> preparations;

        PendingStitch(AtlasEntry atlasEntry, CompletableFuture<SpriteLoader.Preparations> completableFuture) {
            this.entry = atlasEntry;
            this.preparations = completableFuture;
        }

        public void joinAndUpload(Map<Material, TextureAtlasSprite> map) {
            SpriteLoader.Preparations preparations = this.preparations.join();
            this.entry.atlas.upload(preparations);
            preparations.regions().forEach((identifier, textureAtlasSprite) -> map.put(new Material(this.entry.config.textureId, (Identifier)identifier), (TextureAtlasSprite)textureAtlasSprite));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingStitch.class, "entry;preparations", "entry", "preparations"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingStitch.class, "entry;preparations", "entry", "preparations"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingStitch.class, "entry;preparations", "entry", "preparations"}, this, object);
        }

        public AtlasEntry entry() {
            return this.entry;
        }

        public CompletableFuture<SpriteLoader.Preparations> preparations() {
            return this.preparations;
        }
    }
}

