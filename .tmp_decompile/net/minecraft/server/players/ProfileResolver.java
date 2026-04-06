/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.datafixers.util.Either
 */
package net.minecraft.server.players;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.StringUtil;

public interface ProfileResolver {
    public Optional<GameProfile> fetchByName(String var1);

    public Optional<GameProfile> fetchById(UUID var1);

    default public Optional<GameProfile> fetchByNameOrId(Either<String, UUID> either) {
        return (Optional)either.map(this::fetchByName, this::fetchById);
    }

    public static class Cached
    implements ProfileResolver {
        private final LoadingCache<String, Optional<GameProfile>> profileCacheByName;
        final LoadingCache<UUID, Optional<GameProfile>> profileCacheById;

        public Cached(final MinecraftSessionService minecraftSessionService, final UserNameToIdResolver userNameToIdResolver) {
            this.profileCacheById = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<UUID, Optional<GameProfile>>(this){

                public Optional<GameProfile> load(UUID uUID) {
                    ProfileResult profileResult = minecraftSessionService.fetchProfile(uUID, true);
                    return Optional.ofNullable(profileResult).map(ProfileResult::profile);
                }

                public /* synthetic */ Object load(Object object) throws Exception {
                    return this.load((UUID)object);
                }
            });
            this.profileCacheByName = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<String, Optional<GameProfile>>(){

                public Optional<GameProfile> load(String string) {
                    return userNameToIdResolver.get(string).flatMap(nameAndId -> (Optional)profileCacheById.getUnchecked((Object)nameAndId.id()));
                }

                public /* synthetic */ Object load(Object object) throws Exception {
                    return this.load((String)object);
                }
            });
        }

        @Override
        public Optional<GameProfile> fetchByName(String string) {
            if (StringUtil.isValidPlayerName(string)) {
                return (Optional)this.profileCacheByName.getUnchecked((Object)string);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> fetchById(UUID uUID) {
            return (Optional)this.profileCacheById.getUnchecked((Object)uUID);
        }
    }
}

