/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

public class DedicatedPlayerList
extends PlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DedicatedPlayerList(DedicatedServer dedicatedServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage) {
        super(dedicatedServer, layeredRegistryAccess, playerDataStorage, dedicatedServer.notificationManager());
        this.setViewDistance(dedicatedServer.viewDistance());
        this.setSimulationDistance(dedicatedServer.simulationDistance());
        this.loadUserBanList();
        this.saveUserBanList();
        this.loadIpBanList();
        this.saveIpBanList();
        this.loadOps();
        this.loadWhiteList();
        this.saveOps();
        if (!this.getWhiteList().getFile().exists()) {
            this.saveWhiteList();
        }
    }

    @Override
    public void reloadWhiteList() {
        this.loadWhiteList();
    }

    private void saveIpBanList() {
        try {
            this.getIpBans().save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to save ip banlist: ", (Throwable)iOException);
        }
    }

    private void saveUserBanList() {
        try {
            this.getBans().save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to save user banlist: ", (Throwable)iOException);
        }
    }

    private void loadIpBanList() {
        try {
            this.getIpBans().load();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to load ip banlist: ", (Throwable)iOException);
        }
    }

    private void loadUserBanList() {
        try {
            this.getBans().load();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to load user banlist: ", (Throwable)iOException);
        }
    }

    private void loadOps() {
        try {
            this.getOps().load();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load operators list: ", (Throwable)exception);
        }
    }

    private void saveOps() {
        try {
            this.getOps().save();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to save operators list: ", (Throwable)exception);
        }
    }

    private void loadWhiteList() {
        try {
            this.getWhiteList().load();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load white-list: ", (Throwable)exception);
        }
    }

    private void saveWhiteList() {
        try {
            this.getWhiteList().save();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to save white-list: ", (Throwable)exception);
        }
    }

    @Override
    public boolean isWhiteListed(NameAndId nameAndId) {
        return !this.isUsingWhitelist() || this.isOp(nameAndId) || this.getWhiteList().isWhiteListed(nameAndId);
    }

    @Override
    public DedicatedServer getServer() {
        return (DedicatedServer)super.getServer();
    }

    @Override
    public boolean canBypassPlayerLimit(NameAndId nameAndId) {
        return this.getOps().canBypassPlayerLimit(nameAndId);
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

