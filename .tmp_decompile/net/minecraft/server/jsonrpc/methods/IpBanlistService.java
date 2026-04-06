/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.google.common.net.InetAddresses;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.Nullable;

public class IpBanlistService {
    private static final String BAN_SOURCE = "Management server";

    public static List<IpBanDto> get(MinecraftApi minecraftApi) {
        return minecraftApi.banListService().getIpBanEntries().stream().map(IpBan::from).map(IpBanDto::from).toList();
    }

    public static List<IpBanDto> add(MinecraftApi minecraftApi, List<IncomingIpBanDto> list, ClientInfo clientInfo) {
        list.stream().map(incomingIpBanDto -> IpBanlistService.banIp(minecraftApi, incomingIpBanDto, clientInfo)).flatMap(Collection::stream).forEach(serverPlayer -> serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
        return IpBanlistService.get(minecraftApi);
    }

    private static List<ServerPlayer> banIp(MinecraftApi minecraftApi, IncomingIpBanDto incomingIpBanDto, ClientInfo clientInfo) {
        Optional<ServerPlayer> optional;
        IpBan ipBan = incomingIpBanDto.toIpBan();
        if (ipBan != null) {
            return IpBanlistService.banIp(minecraftApi, ipBan, clientInfo);
        }
        if (incomingIpBanDto.player().isPresent() && (optional = minecraftApi.playerListService().getPlayer(incomingIpBanDto.player().get().id(), incomingIpBanDto.player().get().name())).isPresent()) {
            return IpBanlistService.banIp(minecraftApi, incomingIpBanDto.toIpBan(optional.get()), clientInfo);
        }
        return List.of();
    }

    private static List<ServerPlayer> banIp(MinecraftApi minecraftApi, IpBan ipBan, ClientInfo clientInfo) {
        minecraftApi.banListService().addIpBan(ipBan.toIpBanEntry(), clientInfo);
        return minecraftApi.playerListService().getPlayersWithAddress(ipBan.ip());
    }

    public static List<IpBanDto> clear(MinecraftApi minecraftApi, ClientInfo clientInfo) {
        minecraftApi.banListService().clearIpBans(clientInfo);
        return IpBanlistService.get(minecraftApi);
    }

    public static List<IpBanDto> remove(MinecraftApi minecraftApi, List<String> list, ClientInfo clientInfo) {
        list.forEach(string -> minecraftApi.banListService().removeIpBan((String)string, clientInfo));
        return IpBanlistService.get(minecraftApi);
    }

    public static List<IpBanDto> set(MinecraftApi minecraftApi, List<IpBanDto> list, ClientInfo clientInfo) {
        Set set = list.stream().filter(ipBanDto -> InetAddresses.isInetAddress((String)ipBanDto.ip())).map(IpBanDto::toIpBan).collect(Collectors.toSet());
        Set set2 = minecraftApi.banListService().getIpBanEntries().stream().map(IpBan::from).collect(Collectors.toSet());
        set2.stream().filter(ipBan -> !set.contains(ipBan)).forEach(ipBan -> minecraftApi.banListService().removeIpBan(ipBan.ip(), clientInfo));
        set.stream().filter(ipBan -> !set2.contains(ipBan)).forEach(ipBan -> minecraftApi.banListService().addIpBan(ipBan.toIpBanEntry(), clientInfo));
        set.stream().filter(ipBan -> !set2.contains(ipBan)).flatMap(ipBan -> minecraftApi.playerListService().getPlayersWithAddress(ipBan.ip()).stream()).forEach(serverPlayer -> serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
        return IpBanlistService.get(minecraftApi);
    }

    public record IncomingIpBanDto(Optional<PlayerDto> player, Optional<String> ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IncomingIpBanDto> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PlayerDto.CODEC.codec().optionalFieldOf("player").forGetter(IncomingIpBanDto::player), (App)Codec.STRING.optionalFieldOf("ip").forGetter(IncomingIpBanDto::ip), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IncomingIpBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IncomingIpBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IncomingIpBanDto::expires)).apply((Applicative)instance, IncomingIpBanDto::new));

        IpBan toIpBan(ServerPlayer serverPlayer) {
            return new IpBan(serverPlayer.getIpAddress(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }

        @Nullable IpBan toIpBan() {
            if (this.ip().isEmpty() || !InetAddresses.isInetAddress((String)this.ip().get())) {
                return null;
            }
            return new IpBan(this.ip().get(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }
    }

    record IpBan(String ip, @Nullable String reason, String source, Optional<Instant> expires) {
        static IpBan from(IpBanListEntry ipBanListEntry) {
            return new IpBan(Objects.requireNonNull((String)ipBanListEntry.getUser()), ipBanListEntry.getReason(), ipBanListEntry.getSource(), Optional.ofNullable(ipBanListEntry.getExpires()).map(Date::toInstant));
        }

        IpBanListEntry toIpBanEntry() {
            return new IpBanListEntry(this.ip(), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }
    }

    public record IpBanDto(String ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IpBanDto> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("ip").forGetter(IpBanDto::ip), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IpBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IpBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IpBanDto::expires)).apply((Applicative)instance, IpBanDto::new));

        private static IpBanDto from(IpBan ipBan) {
            return new IpBanDto(ipBan.ip(), Optional.ofNullable(ipBan.reason()), Optional.of(ipBan.source()), ipBan.expires());
        }

        public static IpBanDto from(IpBanListEntry ipBanListEntry) {
            return IpBanDto.from(IpBan.from(ipBanListEntry));
        }

        private IpBan toIpBan() {
            return new IpBan(this.ip(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }
    }
}

