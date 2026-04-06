/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class IntegratedPlayerList
extends PlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private @Nullable CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer integratedServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage) {
        super(integratedServer, layeredRegistryAccess, playerDataStorage, integratedServer.notificationManager());
        this.setViewDistance(10);
    }

    @Override
    protected void save(ServerPlayer serverPlayer) {
        if (this.getServer().isSingleplayerOwner(serverPlayer.nameAndId())) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(serverPlayer.problemPath(), LOGGER);){
                TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, serverPlayer.registryAccess());
                serverPlayer.saveWithoutId(tagValueOutput);
                this.playerData = tagValueOutput.buildResult();
            }
        }
        super.save(serverPlayer);
    }

    @Override
    public Component canPlayerLogin(SocketAddress socketAddress, NameAndId nameAndId) {
        if (this.getServer().isSingleplayerOwner(nameAndId) && this.getPlayerByName(nameAndId.name()) != null) {
            return Component.translatable("multiplayer.disconnect.name_taken");
        }
        return super.canPlayerLogin(socketAddress, nameAndId);
    }

    @Override
    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }

    @Override
    public @Nullable CompoundTag getSingleplayerData() {
        return this.playerData;
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

