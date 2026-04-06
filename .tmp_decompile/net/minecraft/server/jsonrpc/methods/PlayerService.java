/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.Message;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class PlayerService {
    private static final Component DEFAULT_KICK_MESSAGE = Component.translatable("multiplayer.disconnect.kicked");

    public static List<PlayerDto> get(MinecraftApi minecraftApi) {
        return minecraftApi.playerListService().getPlayers().stream().map(PlayerDto::from).toList();
    }

    public static List<PlayerDto> kick(MinecraftApi minecraftApi, List<KickDto> list, ClientInfo clientInfo) {
        ArrayList<PlayerDto> list2 = new ArrayList<PlayerDto>();
        for (KickDto kickDto : list) {
            ServerPlayer serverPlayer = PlayerService.getServerPlayer(minecraftApi, kickDto.player());
            if (serverPlayer == null) continue;
            minecraftApi.playerListService().remove(serverPlayer, clientInfo);
            serverPlayer.connection.disconnect(kickDto.message.flatMap(Message::asComponent).orElse(DEFAULT_KICK_MESSAGE));
            list2.add(kickDto.player());
        }
        return list2;
    }

    private static @Nullable ServerPlayer getServerPlayer(MinecraftApi minecraftApi, PlayerDto playerDto) {
        if (playerDto.id().isPresent()) {
            return minecraftApi.playerListService().getPlayer(playerDto.id().get());
        }
        if (playerDto.name().isPresent()) {
            return minecraftApi.playerListService().getPlayerByName(playerDto.name().get());
        }
        return null;
    }

    public static final class KickDto
    extends Record {
        private final PlayerDto player;
        final Optional<Message> message;
        public static final MapCodec<KickDto> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PlayerDto.CODEC.codec().fieldOf("player").forGetter(KickDto::player), (App)Message.CODEC.optionalFieldOf("message").forGetter(KickDto::message)).apply((Applicative)instance, KickDto::new));

        public KickDto(PlayerDto playerDto, Optional<Message> optional) {
            this.player = playerDto;
            this.message = optional;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this, object);
        }

        public PlayerDto player() {
            return this.player;
        }

        public Optional<Message> message() {
            return this.message;
        }
    }
}

