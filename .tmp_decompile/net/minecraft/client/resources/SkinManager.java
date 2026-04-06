/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.hash.Hashing
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.SignatureState
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture$Type
 *  com.mojang.authlib.minecraft.MinecraftProfileTextures
 *  com.mojang.authlib.properties.Property
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SkinManager {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Services services;
    final SkinTextureDownloader skinTextureDownloader;
    private final LoadingCache<CacheKey, CompletableFuture<Optional<PlayerSkin>>> skinCache;
    private final TextureCache skinTextures;
    private final TextureCache capeTextures;
    private final TextureCache elytraTextures;

    public SkinManager(Path path, final Services services, SkinTextureDownloader skinTextureDownloader, final Executor executor) {
        this.services = services;
        this.skinTextureDownloader = skinTextureDownloader;
        this.skinTextures = new TextureCache(path, MinecraftProfileTexture.Type.SKIN);
        this.capeTextures = new TextureCache(path, MinecraftProfileTexture.Type.CAPE);
        this.elytraTextures = new TextureCache(path, MinecraftProfileTexture.Type.ELYTRA);
        this.skinCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(15L)).build((CacheLoader)new CacheLoader<CacheKey, CompletableFuture<Optional<PlayerSkin>>>(){

            public CompletableFuture<Optional<PlayerSkin>> load(CacheKey cacheKey) {
                return ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
                    Property property = cacheKey.packedTextures();
                    if (property == null) {
                        return MinecraftProfileTextures.EMPTY;
                    }
                    MinecraftProfileTextures minecraftProfileTextures = services.sessionService().unpackTextures(property);
                    if (minecraftProfileTextures.signatureState() == SignatureState.INVALID) {
                        LOGGER.warn("Profile contained invalid signature for textures property (profile id: {})", (Object)cacheKey.profileId());
                    }
                    return minecraftProfileTextures;
                }, Util.backgroundExecutor().forName("unpackSkinTextures")).thenComposeAsync(minecraftProfileTextures -> SkinManager.this.registerTextures(cacheKey.profileId(), (MinecraftProfileTextures)minecraftProfileTextures), executor)).handle((playerSkin, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Failed to load texture for profile {}", (Object)cacheKey.profileId, throwable);
                    }
                    return Optional.ofNullable(playerSkin);
                });
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((CacheKey)((Object)object));
            }
        });
    }

    public Supplier<PlayerSkin> createLookup(GameProfile gameProfile, boolean bl) {
        CompletableFuture<Optional<PlayerSkin>> completableFuture = this.get(gameProfile);
        PlayerSkin playerSkin2 = DefaultPlayerSkin.get(gameProfile);
        if (SharedConstants.DEBUG_DEFAULT_SKIN_OVERRIDE) {
            return () -> playerSkin2;
        }
        Optional optional = completableFuture.getNow(null);
        if (optional != null) {
            PlayerSkin playerSkin22 = optional.filter(playerSkin -> !bl || playerSkin.secure()).orElse(playerSkin2);
            return () -> playerSkin22;
        }
        return () -> completableFuture.getNow(Optional.empty()).filter(playerSkin -> !bl || playerSkin.secure()).orElse(playerSkin2);
    }

    public CompletableFuture<Optional<PlayerSkin>> get(GameProfile gameProfile) {
        if (SharedConstants.DEBUG_DEFAULT_SKIN_OVERRIDE) {
            PlayerSkin playerSkin = DefaultPlayerSkin.get(gameProfile);
            return CompletableFuture.completedFuture(Optional.of(playerSkin));
        }
        Property property = this.services.sessionService().getPackedTextures(gameProfile);
        return (CompletableFuture)this.skinCache.getUnchecked((Object)new CacheKey(gameProfile.id(), property));
    }

    CompletableFuture<PlayerSkin> registerTextures(UUID uUID, MinecraftProfileTextures minecraftProfileTextures) {
        PlayerModelType playerModelType;
        CompletableFuture<ClientAsset.Texture> completableFuture;
        MinecraftProfileTexture minecraftProfileTexture = minecraftProfileTextures.skin();
        if (minecraftProfileTexture != null) {
            completableFuture = this.skinTextures.getOrLoad(minecraftProfileTexture);
            playerModelType = PlayerModelType.byLegacyServicesName(minecraftProfileTexture.getMetadata("model"));
        } else {
            PlayerSkin playerSkin = DefaultPlayerSkin.get(uUID);
            completableFuture = CompletableFuture.completedFuture(playerSkin.body());
            playerModelType = playerSkin.model();
        }
        MinecraftProfileTexture minecraftProfileTexture2 = minecraftProfileTextures.cape();
        CompletableFuture<Object> completableFuture2 = minecraftProfileTexture2 != null ? this.capeTextures.getOrLoad(minecraftProfileTexture2) : CompletableFuture.completedFuture(null);
        MinecraftProfileTexture minecraftProfileTexture3 = minecraftProfileTextures.elytra();
        CompletableFuture<Object> completableFuture3 = minecraftProfileTexture3 != null ? this.elytraTextures.getOrLoad(minecraftProfileTexture3) : CompletableFuture.completedFuture(null);
        return CompletableFuture.allOf(completableFuture, completableFuture2, completableFuture3).thenApply(void_ -> new PlayerSkin((ClientAsset.Texture)completableFuture.join(), (ClientAsset.Texture)completableFuture2.join(), (ClientAsset.Texture)completableFuture3.join(), playerModelType, minecraftProfileTextures.signatureState() == SignatureState.SIGNED));
    }

    @Environment(value=EnvType.CLIENT)
    class TextureCache {
        private final Path root;
        private final MinecraftProfileTexture.Type type;
        private final Map<String, CompletableFuture<ClientAsset.Texture>> textures = new Object2ObjectOpenHashMap();

        TextureCache(Path path, MinecraftProfileTexture.Type type) {
            this.root = path;
            this.type = type;
        }

        public CompletableFuture<ClientAsset.Texture> getOrLoad(MinecraftProfileTexture minecraftProfileTexture) {
            String string = minecraftProfileTexture.getHash();
            CompletableFuture<ClientAsset.Texture> completableFuture = this.textures.get(string);
            if (completableFuture == null) {
                completableFuture = this.registerTexture(minecraftProfileTexture);
                this.textures.put(string, completableFuture);
            }
            return completableFuture;
        }

        private CompletableFuture<ClientAsset.Texture> registerTexture(MinecraftProfileTexture minecraftProfileTexture) {
            String string = Hashing.sha1().hashUnencodedChars((CharSequence)minecraftProfileTexture.getHash()).toString();
            Identifier identifier = this.getTextureLocation(string);
            Path path = this.root.resolve(string.length() > 2 ? string.substring(0, 2) : "xx").resolve(string);
            return SkinManager.this.skinTextureDownloader.downloadAndRegisterSkin(identifier, path, minecraftProfileTexture.getUrl(), this.type == MinecraftProfileTexture.Type.SKIN);
        }

        private Identifier getTextureLocation(String string) {
            String string2 = switch (this.type) {
                default -> throw new MatchException(null, null);
                case MinecraftProfileTexture.Type.SKIN -> "skins";
                case MinecraftProfileTexture.Type.CAPE -> "capes";
                case MinecraftProfileTexture.Type.ELYTRA -> "elytra";
            };
            return Identifier.withDefaultNamespace(string2 + "/" + string);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class CacheKey
    extends Record {
        final UUID profileId;
        private final @Nullable Property packedTextures;

        CacheKey(UUID uUID, @Nullable Property property) {
            this.profileId = uUID;
            this.packedTextures = property;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this, object);
        }

        public UUID profileId() {
            return this.profileId;
        }

        public @Nullable Property packedTextures() {
            return this.packedTextures;
        }
    }
}

