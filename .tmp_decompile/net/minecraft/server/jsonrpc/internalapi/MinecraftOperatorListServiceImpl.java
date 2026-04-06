/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.internalapi;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftOperatorListService;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;

public class MinecraftOperatorListServiceImpl
implements MinecraftOperatorListService {
    private final MinecraftServer minecraftServer;
    private final JsonRpcLogger jsonrpcLogger;

    public MinecraftOperatorListServiceImpl(MinecraftServer minecraftServer, JsonRpcLogger jsonRpcLogger) {
        this.minecraftServer = minecraftServer;
        this.jsonrpcLogger = jsonRpcLogger;
    }

    @Override
    public Collection<ServerOpListEntry> getEntries() {
        return this.minecraftServer.getPlayerList().getOps().getEntries();
    }

    @Override
    public void op(NameAndId nameAndId, Optional<PermissionLevel> optional, Optional<Boolean> optional2, ClientInfo clientInfo) {
        this.jsonrpcLogger.log(clientInfo, "Op '{}'", new Object[]{nameAndId});
        this.minecraftServer.getPlayerList().op(nameAndId, optional.map(LevelBasedPermissionSet::forLevel), optional2);
    }

    @Override
    public void op(NameAndId nameAndId, ClientInfo clientInfo) {
        this.jsonrpcLogger.log(clientInfo, "Op '{}'", new Object[]{nameAndId});
        this.minecraftServer.getPlayerList().op(nameAndId);
    }

    @Override
    public void deop(NameAndId nameAndId, ClientInfo clientInfo) {
        this.jsonrpcLogger.log(clientInfo, "Deop '{}'", new Object[]{nameAndId});
        this.minecraftServer.getPlayerList().deop(nameAndId);
    }

    @Override
    public void clear(ClientInfo clientInfo) {
        this.jsonrpcLogger.log(clientInfo, "Clear operator list", new Object[0]);
        this.minecraftServer.getPlayerList().getOps().clear();
    }
}

