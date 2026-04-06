/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.LambdaMetafactory;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class BanlistService {
    private static final String BAN_SOURCE = "Management server";

    public static List<UserBanDto> get(MinecraftApi minecraftApi) {
        return minecraftApi.banListService().getUserBanEntries().stream().filter(userBanListEntry -> userBanListEntry.getUser() != null).map(UserBan::from).map(UserBanDto::from).toList();
    }

    public static List<UserBanDto> add(MinecraftApi minecraftApi, List<UserBanDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(userBanDto -> minecraftApi.playerListService().getUser(userBanDto.player().id(), userBanDto.player().name()).thenApply(optional -> optional.map(userBanDto::toUserBan))).toList();
        for (Optional optional : Util.sequence(list2).join()) {
            if (optional.isEmpty()) continue;
            UserBan userBan = (UserBan)((Object)optional.get());
            minecraftApi.banListService().addUserBan(userBan.toBanEntry(), clientInfo);
            ServerPlayer serverPlayer = minecraftApi.playerListService().getPlayer(((UserBan)((Object)optional.get())).player().id());
            if (serverPlayer == null) continue;
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
        }
        return BanlistService.get(minecraftApi);
    }

    public static List<UserBanDto> clear(MinecraftApi minecraftApi, ClientInfo clientInfo) {
        minecraftApi.banListService().clearUserBans(clientInfo);
        return BanlistService.get(minecraftApi);
    }

    public static List<UserBanDto> remove(MinecraftApi minecraftApi, List<PlayerDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(playerDto -> minecraftApi.playerListService().getUser(playerDto.id(), playerDto.name())).toList();
        for (Optional optional : Util.sequence(list2).join()) {
            if (optional.isEmpty()) continue;
            minecraftApi.banListService().removeUserBan((NameAndId)((Object)optional.get()), clientInfo);
        }
        return BanlistService.get(minecraftApi);
    }

    public static List<UserBanDto> set(MinecraftApi minecraftApi, List<UserBanDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(userBanDto -> minecraftApi.playerListService().getUser(userBanDto.player().id(), userBanDto.player().name()).thenApply(optional -> optional.map(userBanDto::toUserBan))).toList();
        Set set = Util.sequence(list2).join().stream().flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).collect(Collectors.toSet());
        Set set2 = minecraftApi.banListService().getUserBanEntries().stream().filter(userBanListEntry -> userBanListEntry.getUser() != null).map(UserBan::from).collect(Collectors.toSet());
        set2.stream().filter(userBan -> !set.contains(userBan)).forEach(userBan -> minecraftApi.banListService().removeUserBan(userBan.player(), clientInfo));
        set.stream().filter(userBan -> !set2.contains(userBan)).forEach(userBan -> {
            minecraftApi.banListService().addUserBan(userBan.toBanEntry(), clientInfo);
            ServerPlayer serverPlayer = minecraftApi.playerListService().getPlayer(userBan.player().id());
            if (serverPlayer != null) {
                serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
        });
        return BanlistService.get(minecraftApi);
    }

    record UserBan(NameAndId player, @Nullable String reason, String source, Optional<Instant> expires) {
        static UserBan from(UserBanListEntry userBanListEntry) {
            return new UserBan(Objects.requireNonNull((NameAndId)((Object)userBanListEntry.getUser())), userBanListEntry.getReason(), userBanListEntry.getSource(), Optional.ofNullable(userBanListEntry.getExpires()).map(Date::toInstant));
        }

        UserBanListEntry toBanEntry() {
            return new UserBanListEntry(new NameAndId(this.player().id(), this.player().name()), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }
    }

    public record UserBanDto(PlayerDto player, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<UserBanDto> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PlayerDto.CODEC.codec().fieldOf("player").forGetter(UserBanDto::player), (App)Codec.STRING.optionalFieldOf("reason").forGetter(UserBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(UserBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(UserBanDto::expires)).apply((Applicative)instance, UserBanDto::new));

        private static UserBanDto from(UserBan userBan) {
            return new UserBanDto(PlayerDto.from(userBan.player()), Optional.ofNullable(userBan.reason()), Optional.of(userBan.source()), userBan.expires());
        }

        public static UserBanDto from(UserBanListEntry userBanListEntry) {
            return UserBanDto.from(UserBan.from(userBanListEntry));
        }

        private UserBan toUserBan(NameAndId nameAndId) {
            return new UserBan(nameAndId, this.reason().orElse(null), this.source().orElse(BanlistService.BAN_SOURCE), this.expires());
        }
    }
}

