/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public abstract sealed class ResolvableProfile
implements TooltipProvider {
    private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.mapEither(ExtraCodecs.STORED_GAME_PROFILE, Partial.MAP_CODEC).forGetter(ResolvableProfile::unpack), (App)PlayerSkin.Patch.MAP_CODEC.forGetter(ResolvableProfile::skinPatch)).apply((Applicative)instance, ResolvableProfile::create));
    public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(FULL_CODEC, ExtraCodecs.PLAYER_NAME, ResolvableProfile::createUnresolved);
    public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.either(ByteBufCodecs.GAME_PROFILE, Partial.STREAM_CODEC), ResolvableProfile::unpack, PlayerSkin.Patch.STREAM_CODEC, ResolvableProfile::skinPatch, ResolvableProfile::create);
    protected final GameProfile partialProfile;
    protected final PlayerSkin.Patch skinPatch;

    private static ResolvableProfile create(Either<GameProfile, Partial> either, PlayerSkin.Patch patch) {
        return (ResolvableProfile)either.map(gameProfile -> new Static((Either<GameProfile, Partial>)Either.left((Object)gameProfile), patch), partial -> {
            if (!partial.properties.isEmpty() || partial.id.isPresent() == partial.name.isPresent()) {
                return new Static((Either<GameProfile, Partial>)Either.right((Object)partial), patch);
            }
            return partial.name.map(string -> new Dynamic((Either<String, UUID>)Either.left((Object)string), patch)).orElseGet(() -> new Dynamic((Either<String, UUID>)Either.right((Object)partial.id.get()), patch));
        });
    }

    public static ResolvableProfile createResolved(GameProfile gameProfile) {
        return new Static((Either<GameProfile, Partial>)Either.left((Object)gameProfile), PlayerSkin.Patch.EMPTY);
    }

    public static ResolvableProfile createUnresolved(String string) {
        return new Dynamic((Either<String, UUID>)Either.left((Object)string), PlayerSkin.Patch.EMPTY);
    }

    public static ResolvableProfile createUnresolved(UUID uUID) {
        return new Dynamic((Either<String, UUID>)Either.right((Object)uUID), PlayerSkin.Patch.EMPTY);
    }

    protected abstract Either<GameProfile, Partial> unpack();

    protected ResolvableProfile(GameProfile gameProfile, PlayerSkin.Patch patch) {
        this.partialProfile = gameProfile;
        this.skinPatch = patch;
    }

    public abstract CompletableFuture<GameProfile> resolveProfile(ProfileResolver var1);

    public GameProfile partialProfile() {
        return this.partialProfile;
    }

    public PlayerSkin.Patch skinPatch() {
        return this.skinPatch;
    }

    static GameProfile createPartialProfile(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
        String string = optional.orElse("");
        UUID uUID = optional2.orElseGet(() -> optional.map(UUIDUtil::createOfflinePlayerUUID).orElse(Util.NIL_UUID));
        return new GameProfile(uUID, string, propertyMap);
    }

    public abstract Optional<String> name();

    public static final class Static
    extends ResolvableProfile {
        public static final Static EMPTY = new Static((Either<GameProfile, Partial>)Either.right((Object)((Object)Partial.EMPTY)), PlayerSkin.Patch.EMPTY);
        private final Either<GameProfile, Partial> contents;

        Static(Either<GameProfile, Partial> either, PlayerSkin.Patch patch) {
            super((GameProfile)either.map(gameProfile -> gameProfile, Partial::createProfile), patch);
            this.contents = either;
        }

        @Override
        public CompletableFuture<GameProfile> resolveProfile(ProfileResolver profileResolver) {
            return CompletableFuture.completedFuture(this.partialProfile);
        }

        @Override
        protected Either<GameProfile, Partial> unpack() {
            return this.contents;
        }

        @Override
        public Optional<String> name() {
            return (Optional)this.contents.map(gameProfile -> Optional.of(gameProfile.name()), partial -> partial.name);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Static)) return false;
            Static static_ = (Static)object;
            if (!this.contents.equals(static_.contents)) return false;
            if (!this.skinPatch.equals((Object)static_.skinPatch)) return false;
            return true;
        }

        public int hashCode() {
            int i = 31 + this.contents.hashCode();
            i = 31 * i + this.skinPatch.hashCode();
            return i;
        }

        @Override
        public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        }
    }

    public static final class Dynamic
    extends ResolvableProfile {
        private static final Component DYNAMIC_TOOLTIP = Component.translatable("component.profile.dynamic").withStyle(ChatFormatting.GRAY);
        private final Either<String, UUID> nameOrId;

        Dynamic(Either<String, UUID> either, PlayerSkin.Patch patch) {
            super(ResolvableProfile.createPartialProfile(either.left(), either.right(), PropertyMap.EMPTY), patch);
            this.nameOrId = either;
        }

        @Override
        public Optional<String> name() {
            return this.nameOrId.left();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Dynamic)) return false;
            Dynamic dynamic = (Dynamic)object;
            if (!this.nameOrId.equals(dynamic.nameOrId)) return false;
            if (!this.skinPatch.equals((Object)dynamic.skinPatch)) return false;
            return true;
        }

        public int hashCode() {
            int i = 31 + this.nameOrId.hashCode();
            i = 31 * i + this.skinPatch.hashCode();
            return i;
        }

        @Override
        protected Either<GameProfile, Partial> unpack() {
            return Either.right((Object)((Object)new Partial(this.nameOrId.left(), this.nameOrId.right(), PropertyMap.EMPTY)));
        }

        @Override
        public CompletableFuture<GameProfile> resolveProfile(ProfileResolver profileResolver) {
            return CompletableFuture.supplyAsync(() -> profileResolver.fetchByNameOrId(this.nameOrId).orElse(this.partialProfile), Util.nonCriticalIoPool());
        }

        @Override
        public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
            consumer.accept(DYNAMIC_TOOLTIP);
        }
    }

    protected static final class Partial
    extends Record {
        final Optional<String> name;
        final Optional<UUID> id;
        final PropertyMap properties;
        public static final Partial EMPTY = new Partial(Optional.empty(), Optional.empty(), PropertyMap.EMPTY);
        static final MapCodec<Partial> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(Partial::name), (App)UUIDUtil.CODEC.optionalFieldOf("id").forGetter(Partial::id), (App)ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", (Object)PropertyMap.EMPTY).forGetter(Partial::properties)).apply((Applicative)instance, Partial::new));
        public static final StreamCodec<ByteBuf, Partial> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.PLAYER_NAME.apply(ByteBufCodecs::optional), Partial::name, UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), Partial::id, ByteBufCodecs.GAME_PROFILE_PROPERTIES, Partial::properties, Partial::new);

        protected Partial(Optional<String> optional, Optional<UUID> optional2, PropertyMap propertyMap) {
            this.name = optional;
            this.id = optional2;
            this.properties = propertyMap;
        }

        private GameProfile createProfile() {
            return ResolvableProfile.createPartialProfile(this.name, this.id, this.properties);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Partial.class, "name;id;properties", "name", "id", "properties"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Partial.class, "name;id;properties", "name", "id", "properties"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Partial.class, "name;id;properties", "name", "id", "properties"}, this, object);
        }

        public Optional<String> name() {
            return this.name;
        }

        public Optional<UUID> id() {
            return this.id;
        }

        public PropertyMap properties() {
            return this.properties;
        }
    }
}

