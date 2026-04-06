/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsJoinInformation;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoConnectTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import com.mojang.realmsclient.util.task.ConnectTask;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GetServerDetailsTask
extends LongRunningTask {
    private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.connect.connecting");
    private final RealmsServer server;
    private final Screen lastScreen;

    public GetServerDetailsTask(Screen screen, RealmsServer realmsServer) {
        this.lastScreen = screen;
        this.server = realmsServer;
    }

    @Override
    public void run() {
        RealmsJoinInformation realmsJoinInformation;
        try {
            realmsJoinInformation = this.fetchServerAddress();
        }
        catch (CancellationException cancellationException) {
            LOGGER.info("User aborted connecting to realms");
            return;
        }
        catch (RealmsServiceException realmsServiceException) {
            switch (realmsServiceException.realmsError.errorCode()) {
                case 6002: {
                    GetServerDetailsTask.setScreen(new RealmsTermsScreen(this.lastScreen, this.server));
                    return;
                }
                case 6006: {
                    boolean bl = Minecraft.getInstance().isLocalPlayer(this.server.ownerUUID);
                    GetServerDetailsTask.setScreen(bl ? new RealmsBrokenWorldScreen(this.lastScreen, this.server.id, this.server.isMinigameActive()) : new RealmsGenericErrorScreen(Component.translatable("mco.brokenworld.nonowner.title"), Component.translatable("mco.brokenworld.nonowner.error"), this.lastScreen));
                    return;
                }
            }
            this.error(realmsServiceException);
            LOGGER.error("Couldn't connect to world", (Throwable)realmsServiceException);
            return;
        }
        catch (TimeoutException timeoutException) {
            this.error(Component.translatable("mco.errorMessage.connectionFailure"));
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't connect to world", (Throwable)exception);
            this.error(exception);
            return;
        }
        if (realmsJoinInformation.address() == null) {
            this.error(Component.translatable("mco.errorMessage.connectionFailure"));
            return;
        }
        boolean bl2 = realmsJoinInformation.resourcePackUrl() != null && realmsJoinInformation.resourcePackHash() != null;
        RealmsLongRunningMcoTaskScreen screen = bl2 ? this.resourcePackDownloadConfirmationScreen(realmsJoinInformation, GetServerDetailsTask.generatePackId(this.server), this::connectScreen) : this.connectScreen(realmsJoinInformation);
        GetServerDetailsTask.setScreen(screen);
    }

    private static UUID generatePackId(RealmsServer realmsServer) {
        if (realmsServer.minigameName != null) {
            return UUID.nameUUIDFromBytes(("minigame:" + realmsServer.minigameName).getBytes(StandardCharsets.UTF_8));
        }
        return UUID.nameUUIDFromBytes(("realms:" + (String)Objects.requireNonNullElse((Object)realmsServer.name, (Object)"") + ":" + realmsServer.activeSlot).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    private RealmsJoinInformation fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
        RealmsClient realmsClient = RealmsClient.getOrCreate();
        for (int i = 0; i < 40; ++i) {
            if (this.aborted()) {
                throw new CancellationException();
            }
            try {
                return realmsClient.join(this.server.id);
            }
            catch (RetryCallException retryCallException) {
                GetServerDetailsTask.pause(retryCallException.delaySeconds);
                continue;
            }
        }
        throw new TimeoutException();
    }

    public RealmsLongRunningMcoTaskScreen connectScreen(RealmsJoinInformation realmsJoinInformation) {
        return new RealmsLongRunningMcoConnectTaskScreen(this.lastScreen, realmsJoinInformation, (LongRunningTask)new ConnectTask(this.lastScreen, this.server, realmsJoinInformation));
    }

    private PopupScreen resourcePackDownloadConfirmationScreen(RealmsJoinInformation realmsJoinInformation, UUID uUID, Function<RealmsJoinInformation, Screen> function) {
        MutableComponent component = Component.translatable("mco.configure.world.resourcepack.question");
        return RealmsPopups.infoPopupScreen(this.lastScreen, component, popupScreen -> {
            GetServerDetailsTask.setScreen(new GenericMessageScreen(APPLYING_PACK_TEXT));
            ((CompletableFuture)this.scheduleResourcePackDownload(realmsJoinInformation, uUID).thenRun(() -> GetServerDetailsTask.setScreen((Screen)function.apply(realmsJoinInformation)))).exceptionally(throwable -> {
                Minecraft.getInstance().getDownloadedPackSource().cleanupAfterDisconnect();
                LOGGER.error("Failed to download resource pack from {}", (Object)realmsJoinInformation, throwable);
                GetServerDetailsTask.setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.download.resourcePack.fail"), this.lastScreen));
                return null;
            });
        });
    }

    private CompletableFuture<?> scheduleResourcePackDownload(RealmsJoinInformation realmsJoinInformation, UUID uUID) {
        try {
            if (realmsJoinInformation.resourcePackUrl() == null) {
                return CompletableFuture.failedFuture((Throwable)new IllegalStateException("resourcePackUrl was null"));
            }
            if (realmsJoinInformation.resourcePackHash() == null) {
                return CompletableFuture.failedFuture((Throwable)new IllegalStateException("resourcePackHash was null"));
            }
            DownloadedPackSource downloadedPackSource = Minecraft.getInstance().getDownloadedPackSource();
            CompletableFuture<Void> completableFuture = downloadedPackSource.waitForPackFeedback(uUID);
            downloadedPackSource.allowServerPacks();
            downloadedPackSource.pushPack(uUID, new URL(realmsJoinInformation.resourcePackUrl()), realmsJoinInformation.resourcePackHash());
            return completableFuture;
        }
        catch (Exception exception) {
            return CompletableFuture.failedFuture((Throwable)exception);
        }
    }
}

