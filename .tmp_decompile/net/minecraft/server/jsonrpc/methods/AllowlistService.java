/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.util.Util;

public class AllowlistService {
    public static List<PlayerDto> get(MinecraftApi minecraftApi) {
        return minecraftApi.allowListService().getEntries().stream().filter(userWhiteListEntry -> userWhiteListEntry.getUser() != null).map(userWhiteListEntry -> PlayerDto.from((NameAndId)((Object)((Object)userWhiteListEntry.getUser())))).toList();
    }

    public static List<PlayerDto> add(MinecraftApi minecraftApi, List<PlayerDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(playerDto -> minecraftApi.playerListService().getUser(playerDto.id(), playerDto.name())).toList();
        for (Optional optional : Util.sequence(list2).join()) {
            optional.ifPresent(nameAndId -> minecraftApi.allowListService().add(new UserWhiteListEntry((NameAndId)((Object)nameAndId)), clientInfo));
        }
        return AllowlistService.get(minecraftApi);
    }

    public static List<PlayerDto> clear(MinecraftApi minecraftApi, ClientInfo clientInfo) {
        minecraftApi.allowListService().clear(clientInfo);
        return AllowlistService.get(minecraftApi);
    }

    public static List<PlayerDto> remove(MinecraftApi minecraftApi, List<PlayerDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(playerDto -> minecraftApi.playerListService().getUser(playerDto.id(), playerDto.name())).toList();
        for (Optional optional : Util.sequence(list2).join()) {
            optional.ifPresent(nameAndId -> minecraftApi.allowListService().remove((NameAndId)((Object)nameAndId), clientInfo));
        }
        minecraftApi.allowListService().kickUnlistedPlayers(clientInfo);
        return AllowlistService.get(minecraftApi);
    }

    public static List<PlayerDto> set(MinecraftApi minecraftApi, List<PlayerDto> list, ClientInfo clientInfo) {
        List list2 = list.stream().map(playerDto -> minecraftApi.playerListService().getUser(playerDto.id(), playerDto.name())).toList();
        Set set = Util.sequence(list2).join().stream().flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).collect(Collectors.toSet());
        Set set2 = minecraftApi.allowListService().getEntries().stream().map(StoredUserEntry::getUser).collect(Collectors.toSet());
        set2.stream().filter(nameAndId -> !set.contains(nameAndId)).forEach(nameAndId -> minecraftApi.allowListService().remove((NameAndId)((Object)nameAndId), clientInfo));
        set.stream().filter(nameAndId -> !set2.contains(nameAndId)).forEach(nameAndId -> minecraftApi.allowListService().add(new UserWhiteListEntry((NameAndId)((Object)nameAndId)), clientInfo));
        minecraftApi.allowListService().kickUnlistedPlayers(clientInfo);
        return AllowlistService.get(minecraftApi);
    }
}

