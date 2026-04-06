/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinRenderCache {
    public static final RenderType DEFAULT_PLAYER_SKIN_RENDER_TYPE = PlayerSkinRenderCache.playerSkinRenderType(DefaultPlayerSkin.getDefaultSkin());
    public static final Duration CACHE_DURATION = Duration.ofMinutes(5L);
    private final LoadingCache<ResolvableProfile, CompletableFuture<Optional<RenderInfo>>> renderInfoCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION).build((CacheLoader)new CacheLoader<ResolvableProfile, CompletableFuture<Optional<RenderInfo>>>(){

        public CompletableFuture<Optional<RenderInfo>> load(ResolvableProfile resolvableProfile) {
            return resolvableProfile.resolveProfile(PlayerSkinRenderCache.this.profileResolver).thenCompose(gameProfile -> PlayerSkinRenderCache.this.skinManager.get((GameProfile)gameProfile).thenApply(optional -> optional.map(playerSkin -> new RenderInfo((GameProfile)gameProfile, (PlayerSkin)((Object)((Object)((Object)playerSkin))), resolvableProfile.skinPatch()))));
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((ResolvableProfile)object);
        }
    });
    private final LoadingCache<ResolvableProfile, RenderInfo> defaultSkinCache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_DURATION).build((CacheLoader)new CacheLoader<ResolvableProfile, RenderInfo>(){

        public RenderInfo load(ResolvableProfile resolvableProfile) {
            GameProfile gameProfile = resolvableProfile.partialProfile();
            return new RenderInfo(gameProfile, DefaultPlayerSkin.get(gameProfile), resolvableProfile.skinPatch());
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((ResolvableProfile)object);
        }
    });
    final TextureManager textureManager;
    final SkinManager skinManager;
    final ProfileResolver profileResolver;

    public PlayerSkinRenderCache(TextureManager textureManager, SkinManager skinManager, ProfileResolver profileResolver) {
        this.textureManager = textureManager;
        this.skinManager = skinManager;
        this.profileResolver = profileResolver;
    }

    public RenderInfo getOrDefault(ResolvableProfile resolvableProfile) {
        RenderInfo renderInfo = this.lookup(resolvableProfile).getNow(Optional.empty()).orElse(null);
        if (renderInfo != null) {
            return renderInfo;
        }
        return (RenderInfo)this.defaultSkinCache.getUnchecked((Object)resolvableProfile);
    }

    public Supplier<RenderInfo> createLookup(ResolvableProfile resolvableProfile) {
        RenderInfo renderInfo = (RenderInfo)this.defaultSkinCache.getUnchecked((Object)resolvableProfile);
        CompletableFuture completableFuture = (CompletableFuture)this.renderInfoCache.getUnchecked((Object)resolvableProfile);
        Optional optional = completableFuture.getNow(null);
        if (optional != null) {
            RenderInfo renderInfo2 = optional.orElse(renderInfo);
            return () -> renderInfo2;
        }
        return () -> completableFuture.getNow(Optional.empty()).orElse(renderInfo);
    }

    public CompletableFuture<Optional<RenderInfo>> lookup(ResolvableProfile resolvableProfile) {
        return (CompletableFuture)this.renderInfoCache.getUnchecked((Object)resolvableProfile);
    }

    static RenderType playerSkinRenderType(PlayerSkin playerSkin) {
        return SkullBlockRenderer.getPlayerSkinRenderType(playerSkin.body().texturePath());
    }

    @Environment(value=EnvType.CLIENT)
    public final class RenderInfo {
        private final GameProfile gameProfile;
        private final PlayerSkin playerSkin;
        private @Nullable RenderType itemRenderType;
        private @Nullable GpuTextureView textureView;
        private @Nullable GlyphRenderTypes glyphRenderTypes;

        public RenderInfo(GameProfile gameProfile, PlayerSkin playerSkin, PlayerSkin.Patch patch) {
            this.gameProfile = gameProfile;
            this.playerSkin = playerSkin.with(patch);
        }

        public GameProfile gameProfile() {
            return this.gameProfile;
        }

        public PlayerSkin playerSkin() {
            return this.playerSkin;
        }

        public RenderType renderType() {
            if (this.itemRenderType == null) {
                this.itemRenderType = PlayerSkinRenderCache.playerSkinRenderType(this.playerSkin);
            }
            return this.itemRenderType;
        }

        public GpuTextureView textureView() {
            if (this.textureView == null) {
                this.textureView = PlayerSkinRenderCache.this.textureManager.getTexture(this.playerSkin.body().texturePath()).getTextureView();
            }
            return this.textureView;
        }

        public GlyphRenderTypes glyphRenderTypes() {
            if (this.glyphRenderTypes == null) {
                this.glyphRenderTypes = GlyphRenderTypes.createForColorTexture(this.playerSkin.body().texturePath());
            }
            return this.glyphRenderTypes;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof RenderInfo)) return false;
            RenderInfo renderInfo = (RenderInfo)object;
            if (!this.gameProfile.equals((Object)renderInfo.gameProfile)) return false;
            if (!this.playerSkin.equals((Object)renderInfo.playerSkin)) return false;
            return true;
        }

        public int hashCode() {
            int i = 1;
            i = 31 * i + this.gameProfile.hashCode();
            i = 31 * i + this.playerSkin.hashCode();
            return i;
        }
    }
}

