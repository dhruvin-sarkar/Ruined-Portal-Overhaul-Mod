/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface MinecraftPlayerListService {
    public List<ServerPlayer> getPlayers();

    public @Nullable ServerPlayer getPlayer(UUID var1);

    default public CompletableFuture<Optional<NameAndId>> getUser(Optional<UUID> optional, Optional<String> optional2) {
        if (optional.isPresent()) {
            Optional<NameAndId> optional3 = this.getCachedUserById(optional.get());
            if (optional3.isPresent()) {
                return CompletableFuture.completedFuture(optional3);
            }
            return CompletableFuture.supplyAsync(() -> this.fetchUserById((UUID)optional.get()), Util.nonCriticalIoPool());
        }
        if (optional2.isPresent()) {
            return CompletableFuture.supplyAsync(() -> this.fetchUserByName((String)optional2.get()), Util.nonCriticalIoPool());
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public Optional<NameAndId> fetchUserByName(String var1);

    public Optional<NameAndId> fetchUserById(UUID var1);

    public Optional<NameAndId> getCachedUserById(UUID var1);

    public Optional<ServerPlayer> getPlayer(Optional<UUID> var1, Optional<String> var2);

    public List<ServerPlayer> getPlayersWithAddress(String var1);

    public @Nullable ServerPlayer getPlayerByName(String var1);

    public void remove(ServerPlayer var1, ClientInfo var2);
}

