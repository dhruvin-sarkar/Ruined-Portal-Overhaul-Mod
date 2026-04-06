/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.DialogScreens;
import net.minecraft.client.gui.screens.dialog.WaitingForResponseScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundCustomReportDetailsPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class ClientCommonPacketListenerImpl
implements ClientCommonPacketListener {
    private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Minecraft minecraft;
    protected final Connection connection;
    protected final @Nullable ServerData serverData;
    protected @Nullable String serverBrand;
    protected final WorldSessionTelemetryManager telemetryManager;
    protected final @Nullable Screen postDisconnectScreen;
    protected boolean isTransferring;
    private final List<DeferredPacket> deferredPackets = new ArrayList<DeferredPacket>();
    protected final Map<Identifier, byte[]> serverCookies;
    protected Map<String, String> customReportDetails;
    private ServerLinks serverLinks;
    protected final Map<UUID, PlayerInfo> seenPlayers;
    protected boolean seenInsecureChatWarning;

    protected ClientCommonPacketListenerImpl(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        this.minecraft = minecraft;
        this.connection = connection;
        this.serverData = commonListenerCookie.serverData();
        this.serverBrand = commonListenerCookie.serverBrand();
        this.telemetryManager = commonListenerCookie.telemetryManager();
        this.postDisconnectScreen = commonListenerCookie.postDisconnectScreen();
        this.serverCookies = commonListenerCookie.serverCookies();
        this.customReportDetails = commonListenerCookie.customReportDetails();
        this.serverLinks = commonListenerCookie.serverLinks();
        this.seenPlayers = new HashMap<UUID, PlayerInfo>(commonListenerCookie.seenPlayers());
        this.seenInsecureChatWarning = commonListenerCookie.seenInsecureChatWarning();
    }

    public ServerLinks serverLinks() {
        return this.serverLinks;
    }

    @Override
    public void onPacketError(Packet packet, Exception exception) {
        LOGGER.error("Failed to handle packet {}, disconnecting", (Object)packet, (Object)exception);
        Optional<Path> optional = this.storeDisconnectionReport(packet, exception);
        Optional<URI> optional2 = this.serverLinks.findKnownType(ServerLinks.KnownLinkType.BUG_REPORT).map(ServerLinks.Entry::link);
        this.connection.disconnect(new DisconnectionDetails(Component.translatable("disconnect.packetError"), optional, optional2));
    }

    @Override
    public DisconnectionDetails createDisconnectionInfo(Component component, Throwable throwable) {
        Optional<Path> optional = this.storeDisconnectionReport(null, throwable);
        Optional<URI> optional2 = this.serverLinks.findKnownType(ServerLinks.KnownLinkType.BUG_REPORT).map(ServerLinks.Entry::link);
        return new DisconnectionDetails(component, optional, optional2);
    }

    private Optional<Path> storeDisconnectionReport(@Nullable Packet packet, Throwable throwable) {
        CrashReport crashReport = CrashReport.forThrowable(throwable, "Packet handling error");
        PacketUtils.fillCrashReport(crashReport, this, packet);
        Path path = this.minecraft.gameDirectory.toPath().resolve("debug");
        Path path2 = path.resolve("disconnect-" + Util.getFilenameFormattedDateTime() + "-client.txt");
        Optional<ServerLinks.Entry> optional = this.serverLinks.findKnownType(ServerLinks.KnownLinkType.BUG_REPORT);
        List list = optional.map(entry -> List.of((Object)("Server bug reporting link: " + String.valueOf(entry.link())))).orElse(List.of());
        if (crashReport.saveToFile(path2, ReportType.NETWORK_PROTOCOL_ERROR, list)) {
            return Optional.of(path2);
        }
        return Optional.empty();
    }

    @Override
    public boolean shouldHandleMessage(Packet<?> packet) {
        if (ClientCommonPacketListener.super.shouldHandleMessage(packet)) {
            return true;
        }
        return this.isTransferring && (packet instanceof ClientboundStoreCookiePacket || packet instanceof ClientboundTransferPacket);
    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket) {
        this.sendWhen(new ServerboundKeepAlivePacket(clientboundKeepAlivePacket.getId()), () -> !RenderSystem.isFrozenAtPollEvents(), Duration.ofMinutes(1L));
    }

    @Override
    public void handlePing(ClientboundPingPacket clientboundPingPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPingPacket, this, this.minecraft.packetProcessor());
        this.send(new ServerboundPongPacket(clientboundPingPacket.getId()));
    }

    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
        CustomPacketPayload customPacketPayload = clientboundCustomPayloadPacket.payload();
        if (customPacketPayload instanceof DiscardedPayload) {
            return;
        }
        PacketUtils.ensureRunningOnSameThread(clientboundCustomPayloadPacket, this, this.minecraft.packetProcessor());
        if (customPacketPayload instanceof BrandPayload) {
            BrandPayload brandPayload = (BrandPayload)customPacketPayload;
            this.serverBrand = brandPayload.brand();
            this.telemetryManager.onServerBrandReceived(brandPayload.brand());
        } else {
            this.handleCustomPayload(customPacketPayload);
        }
    }

    protected abstract void handleCustomPayload(CustomPacketPayload var1);

    @Override
    public void handleResourcePackPush(ClientboundResourcePackPushPacket clientboundResourcePackPushPacket) {
        ServerData.ServerPackStatus serverPackStatus;
        PacketUtils.ensureRunningOnSameThread(clientboundResourcePackPushPacket, this, this.minecraft.packetProcessor());
        UUID uUID = clientboundResourcePackPushPacket.id();
        URL uRL = ClientCommonPacketListenerImpl.parseResourcePackUrl(clientboundResourcePackPushPacket.url());
        if (uRL == null) {
            this.connection.send(new ServerboundResourcePackPacket(uUID, ServerboundResourcePackPacket.Action.INVALID_URL));
            return;
        }
        String string = clientboundResourcePackPushPacket.hash();
        boolean bl = clientboundResourcePackPushPacket.required();
        ServerData.ServerPackStatus serverPackStatus2 = serverPackStatus = this.serverData != null ? this.serverData.getResourcePackStatus() : ServerData.ServerPackStatus.PROMPT;
        if (serverPackStatus == ServerData.ServerPackStatus.PROMPT || bl && serverPackStatus == ServerData.ServerPackStatus.DISABLED) {
            this.minecraft.setScreen(this.addOrUpdatePackPrompt(uUID, uRL, string, bl, clientboundResourcePackPushPacket.prompt().orElse(null)));
        } else {
            this.minecraft.getDownloadedPackSource().pushPack(uUID, uRL, string);
        }
    }

    @Override
    public void handleResourcePackPop(ClientboundResourcePackPopPacket clientboundResourcePackPopPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundResourcePackPopPacket, this, this.minecraft.packetProcessor());
        clientboundResourcePackPopPacket.id().ifPresentOrElse(uUID -> this.minecraft.getDownloadedPackSource().popPack((UUID)uUID), () -> this.minecraft.getDownloadedPackSource().popAll());
    }

    static Component preparePackPrompt(Component component, @Nullable Component component2) {
        if (component2 == null) {
            return component;
        }
        return Component.translatable("multiplayer.texturePrompt.serverPrompt", component, component2);
    }

    private static @Nullable URL parseResourcePackUrl(String string) {
        try {
            URL uRL = new URL(string);
            String string2 = uRL.getProtocol();
            if ("http".equals(string2) || "https".equals(string2)) {
                return uRL;
            }
        }
        catch (MalformedURLException malformedURLException) {
            return null;
        }
        return null;
    }

    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket clientboundCookieRequestPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCookieRequestPacket, this, this.minecraft.packetProcessor());
        this.connection.send(new ServerboundCookieResponsePacket(clientboundCookieRequestPacket.key(), this.serverCookies.get(clientboundCookieRequestPacket.key())));
    }

    @Override
    public void handleStoreCookie(ClientboundStoreCookiePacket clientboundStoreCookiePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundStoreCookiePacket, this, this.minecraft.packetProcessor());
        this.serverCookies.put(clientboundStoreCookiePacket.key(), clientboundStoreCookiePacket.payload());
    }

    @Override
    public void handleCustomReportDetails(ClientboundCustomReportDetailsPacket clientboundCustomReportDetailsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCustomReportDetailsPacket, this, this.minecraft.packetProcessor());
        this.customReportDetails = clientboundCustomReportDetailsPacket.details();
    }

    @Override
    public void handleServerLinks(ClientboundServerLinksPacket clientboundServerLinksPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundServerLinksPacket, this, this.minecraft.packetProcessor());
        List<ServerLinks.UntrustedEntry> list = clientboundServerLinksPacket.links();
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)list.size());
        for (ServerLinks.UntrustedEntry untrustedEntry : list) {
            try {
                URI uRI = Util.parseAndValidateUntrustedUri(untrustedEntry.link());
                builder.add((Object)new ServerLinks.Entry(untrustedEntry.type(), uRI));
            }
            catch (Exception exception) {
                LOGGER.warn("Received invalid link for type {}:{}", new Object[]{untrustedEntry.type(), untrustedEntry.link(), exception});
            }
        }
        this.serverLinks = new ServerLinks((List<ServerLinks.Entry>)builder.build());
    }

    @Override
    public void handleShowDialog(ClientboundShowDialogPacket clientboundShowDialogPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundShowDialogPacket, this, this.minecraft.packetProcessor());
        this.showDialog(clientboundShowDialogPacket.dialog(), this.minecraft.screen);
    }

    protected abstract DialogConnectionAccess createDialogAccess();

    public void showDialog(Holder<Dialog> holder, @Nullable Screen screen) {
        this.showDialog(holder, this.createDialogAccess(), screen);
    }

    protected void showDialog(Holder<Dialog> holder, DialogConnectionAccess dialogConnectionAccess, @Nullable Screen screen) {
        Screen screen4;
        if (screen instanceof DialogScreen.WarningScreen) {
            Screen screen2;
            DialogScreen<Dialog> dialogScreen;
            DialogScreen.WarningScreen warningScreen = (DialogScreen.WarningScreen)screen;
            Screen screen22 = warningScreen.returnScreen();
            if (screen22 instanceof DialogScreen) {
                dialogScreen = (DialogScreen<Dialog>)screen22;
                screen2 = dialogScreen.previousScreen();
            } else {
                screen2 = screen22;
            }
            Screen screen3 = screen2;
            dialogScreen = DialogScreens.createFromData(holder.value(), screen3, dialogConnectionAccess);
            if (dialogScreen != null) {
                warningScreen.updateReturnScreen(dialogScreen);
            } else {
                LOGGER.warn("Failed to show dialog for data {}", holder);
            }
            return;
        }
        if (screen instanceof DialogScreen) {
            DialogScreen dialogScreen2 = (DialogScreen)screen;
            screen4 = dialogScreen2.previousScreen();
        } else if (screen instanceof WaitingForResponseScreen) {
            WaitingForResponseScreen waitingForResponseScreen = (WaitingForResponseScreen)screen;
            screen4 = waitingForResponseScreen.previousScreen();
        } else {
            screen4 = screen;
        }
        DialogScreen<Dialog> screen2 = DialogScreens.createFromData(holder.value(), screen4, dialogConnectionAccess);
        if (screen2 != null) {
            this.minecraft.setScreen(screen2);
        } else {
            LOGGER.warn("Failed to show dialog for data {}", holder);
        }
    }

    @Override
    public void handleClearDialog(ClientboundClearDialogPacket clientboundClearDialogPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundClearDialogPacket, this, this.minecraft.packetProcessor());
        this.clearDialog();
    }

    public void clearDialog() {
        Screen screen = this.minecraft.screen;
        if (screen instanceof DialogScreen.WarningScreen) {
            DialogScreen.WarningScreen warningScreen = (DialogScreen.WarningScreen)screen;
            Screen screen2 = warningScreen.returnScreen();
            if (screen2 instanceof DialogScreen) {
                DialogScreen dialogScreen = (DialogScreen)screen2;
                warningScreen.updateReturnScreen(dialogScreen.previousScreen());
            }
        } else {
            screen = this.minecraft.screen;
            if (screen instanceof DialogScreen) {
                DialogScreen dialogScreen2 = (DialogScreen)screen;
                this.minecraft.setScreen(dialogScreen2.previousScreen());
            }
        }
    }

    @Override
    public void handleTransfer(ClientboundTransferPacket clientboundTransferPacket) {
        this.isTransferring = true;
        PacketUtils.ensureRunningOnSameThread(clientboundTransferPacket, this, this.minecraft.packetProcessor());
        if (this.serverData == null) {
            throw new IllegalStateException("Cannot transfer to server from singleplayer");
        }
        this.connection.disconnect(Component.translatable("disconnect.transfer"));
        this.connection.setReadOnly();
        this.connection.handleDisconnection();
        ServerAddress serverAddress = new ServerAddress(clientboundTransferPacket.host(), clientboundTransferPacket.port());
        ConnectScreen.startConnecting((Screen)Objects.requireNonNullElseGet((Object)this.postDisconnectScreen, TitleScreen::new), this.minecraft, serverAddress, this.serverData, false, new TransferState(this.serverCookies, this.seenPlayers, this.seenInsecureChatWarning));
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket) {
        this.connection.disconnect(clientboundDisconnectPacket.reason());
    }

    protected void sendDeferredPackets() {
        Iterator<DeferredPacket> iterator = this.deferredPackets.iterator();
        while (iterator.hasNext()) {
            DeferredPacket deferredPacket = iterator.next();
            if (deferredPacket.sendCondition().getAsBoolean()) {
                this.send(deferredPacket.packet);
                iterator.remove();
                continue;
            }
            if (deferredPacket.expirationTime() > Util.getMillis()) continue;
            iterator.remove();
        }
    }

    public void send(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        this.telemetryManager.onDisconnect();
        this.minecraft.disconnect(this.createDisconnectScreen(disconnectionDetails), this.isTransferring);
        LOGGER.warn("Client disconnected with reason: {}", (Object)disconnectionDetails.reason().getString());
    }

    @Override
    public void fillListenerSpecificCrashDetails(CrashReport crashReport, CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Is Local", () -> String.valueOf(this.connection.isMemoryConnection()));
        crashReportCategory.setDetail("Server type", () -> this.serverData != null ? this.serverData.type().toString() : "<none>");
        crashReportCategory.setDetail("Server brand", () -> this.serverBrand);
        if (!this.customReportDetails.isEmpty()) {
            CrashReportCategory crashReportCategory2 = crashReport.addCategory("Custom Server Details");
            this.customReportDetails.forEach(crashReportCategory2::setDetail);
        }
    }

    protected Screen createDisconnectScreen(DisconnectionDetails disconnectionDetails) {
        Screen screen = (Screen)Objects.requireNonNullElseGet((Object)this.postDisconnectScreen, () -> this.serverData != null ? new JoinMultiplayerScreen(new TitleScreen()) : new TitleScreen());
        if (this.serverData != null && this.serverData.isRealm()) {
            return new DisconnectedScreen(screen, GENERIC_DISCONNECT_MESSAGE, disconnectionDetails, CommonComponents.GUI_BACK);
        }
        return new DisconnectedScreen(screen, GENERIC_DISCONNECT_MESSAGE, disconnectionDetails);
    }

    public @Nullable String serverBrand() {
        return this.serverBrand;
    }

    private void sendWhen(Packet<? extends ServerboundPacketListener> packet, BooleanSupplier booleanSupplier, Duration duration) {
        if (booleanSupplier.getAsBoolean()) {
            this.send(packet);
        } else {
            this.deferredPackets.add(new DeferredPacket(packet, booleanSupplier, Util.getMillis() + duration.toMillis()));
        }
    }

    private Screen addOrUpdatePackPrompt(UUID uUID, URL uRL, String string, boolean bl, @Nullable Component component) {
        Screen screen = this.minecraft.screen;
        if (screen instanceof PackConfirmScreen) {
            PackConfirmScreen packConfirmScreen = (PackConfirmScreen)screen;
            return packConfirmScreen.update(this.minecraft, uUID, uRL, string, bl, component);
        }
        return new PackConfirmScreen(this.minecraft, screen, List.of((Object)((Object)new PackConfirmScreen.PendingRequest(uUID, uRL, string))), bl, component);
    }

    @Environment(value=EnvType.CLIENT)
    static final class DeferredPacket
    extends Record {
        final Packet<? extends ServerboundPacketListener> packet;
        private final BooleanSupplier sendCondition;
        private final long expirationTime;

        DeferredPacket(Packet<? extends ServerboundPacketListener> packet, BooleanSupplier booleanSupplier, long l) {
            this.packet = packet;
            this.sendCondition = booleanSupplier;
            this.expirationTime = l;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DeferredPacket.class, "packet;sendCondition;expirationTime", "packet", "sendCondition", "expirationTime"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DeferredPacket.class, "packet;sendCondition;expirationTime", "packet", "sendCondition", "expirationTime"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DeferredPacket.class, "packet;sendCondition;expirationTime", "packet", "sendCondition", "expirationTime"}, this, object);
        }

        public Packet<? extends ServerboundPacketListener> packet() {
            return this.packet;
        }

        public BooleanSupplier sendCondition() {
            return this.sendCondition;
        }

        public long expirationTime() {
            return this.expirationTime;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PackConfirmScreen
    extends ConfirmScreen {
        private final List<PendingRequest> requests;
        private final @Nullable Screen parentScreen;

        PackConfirmScreen(@Nullable Minecraft minecraft, Screen screen, List<PendingRequest> list, @Nullable boolean bl, Component component) {
            super(bl2 -> {
                minecraft.setScreen(screen);
                DownloadedPackSource downloadedPackSource = minecraft.getDownloadedPackSource();
                if (bl2) {
                    if (clientCommonPacketListenerImpl.serverData != null) {
                        clientCommonPacketListenerImpl.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                    }
                    downloadedPackSource.allowServerPacks();
                } else {
                    downloadedPackSource.rejectServerPacks();
                    if (bl) {
                        clientCommonPacketListenerImpl.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                    } else if (clientCommonPacketListenerImpl.serverData != null) {
                        clientCommonPacketListenerImpl.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                    }
                }
                for (PendingRequest pendingRequest : list) {
                    downloadedPackSource.pushPack(pendingRequest.id, pendingRequest.url, pendingRequest.hash);
                }
                if (clientCommonPacketListenerImpl.serverData != null) {
                    ServerList.saveSingleServer(clientCommonPacketListenerImpl.serverData);
                }
            }, bl ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"), ClientCommonPacketListenerImpl.preparePackPrompt(bl ? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD) : Component.translatable("multiplayer.texturePrompt.line2"), component), bl ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, bl ? CommonComponents.GUI_DISCONNECT : CommonComponents.GUI_NO);
            this.requests = list;
            this.parentScreen = screen;
        }

        public PackConfirmScreen update(Minecraft minecraft, UUID uUID, URL uRL, String string, boolean bl, @Nullable Component component) {
            ImmutableList list = ImmutableList.builderWithExpectedSize((int)(this.requests.size() + 1)).addAll(this.requests).add((Object)new PendingRequest(uUID, uRL, string)).build();
            return new PackConfirmScreen(minecraft, this.parentScreen, (List<PendingRequest>)list, bl, component);
        }

        @Environment(value=EnvType.CLIENT)
        static final class PendingRequest
        extends Record {
            final UUID id;
            final URL url;
            final String hash;

            PendingRequest(UUID uUID, URL uRL, String string) {
                this.id = uUID;
                this.url = uRL;
                this.hash = string;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingRequest.class, "id;url;hash", "id", "url", "hash"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingRequest.class, "id;url;hash", "id", "url", "hash"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingRequest.class, "id;url;hash", "id", "url", "hash"}, this, object);
            }

            public UUID id() {
                return this.id;
            }

            public URL url() {
                return this.url;
            }

            public String hash() {
                return this.hash;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected abstract class CommonDialogAccess
    implements DialogConnectionAccess {
        protected CommonDialogAccess() {
        }

        @Override
        public void disconnect(Component component) {
            ClientCommonPacketListenerImpl.this.connection.disconnect(component);
            ClientCommonPacketListenerImpl.this.connection.handleDisconnection();
        }

        @Override
        public void openDialog(Holder<Dialog> holder, @Nullable Screen screen) {
            ClientCommonPacketListenerImpl.this.showDialog(holder, this, screen);
        }

        @Override
        public void sendCustomAction(Identifier identifier, Optional<Tag> optional) {
            ClientCommonPacketListenerImpl.this.send(new ServerboundCustomClickActionPacket(identifier, optional));
        }

        @Override
        public ServerLinks serverLinks() {
            return ClientCommonPacketListenerImpl.this.serverLinks();
        }
    }
}

