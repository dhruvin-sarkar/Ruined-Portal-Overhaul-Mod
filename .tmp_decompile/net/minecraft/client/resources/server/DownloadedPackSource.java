/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hashing
 *  com.mojang.logging.LogUtils
 *  com.mojang.util.UndashedUuid
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.server;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.Unit;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DownloadedPackSource
implements AutoCloseable {
    private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final RepositorySource EMPTY_SOURCE = consumer -> {};
    private static final PackSelectionConfig DOWNLOADED_PACK_SELECTION = new PackSelectionConfig(true, Pack.Position.TOP, true);
    private static final PackLoadFeedback LOG_ONLY_FEEDBACK = new PackLoadFeedback(){

        @Override
        public void reportUpdate(UUID uUID, PackLoadFeedback.Update update) {
            LOGGER.debug("Downloaded pack {} changed state to {}", (Object)uUID, (Object)update);
        }

        @Override
        public void reportFinalResult(UUID uUID, PackLoadFeedback.FinalResult finalResult) {
            LOGGER.debug("Downloaded pack {} finished with state {}", (Object)uUID, (Object)finalResult);
        }
    };
    final Minecraft minecraft;
    private RepositorySource packSource = EMPTY_SOURCE;
    private @Nullable PackReloadConfig.Callbacks pendingReload;
    final ServerPackManager manager;
    private final DownloadQueue downloadQueue;
    private PackSource packType = PackSource.SERVER;
    PackLoadFeedback packFeedback = LOG_ONLY_FEEDBACK;
    private int packIdSerialNumber;

    public DownloadedPackSource(Minecraft minecraft, Path path, GameConfig.UserData userData) {
        this.minecraft = minecraft;
        try {
            this.downloadQueue = new DownloadQueue(path);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException("Failed to open download queue in directory " + String.valueOf(path), iOException);
        }
        Executor executor = minecraft::schedule;
        this.manager = new ServerPackManager(this.createDownloader(this.downloadQueue, executor, userData.user, userData.proxy), new PackLoadFeedback(){

            @Override
            public void reportUpdate(UUID uUID, PackLoadFeedback.Update update) {
                DownloadedPackSource.this.packFeedback.reportUpdate(uUID, update);
            }

            @Override
            public void reportFinalResult(UUID uUID, PackLoadFeedback.FinalResult finalResult) {
                DownloadedPackSource.this.packFeedback.reportFinalResult(uUID, finalResult);
            }
        }, this.createReloadConfig(), this.createUpdateScheduler(executor), ServerPackManager.PackPromptStatus.PENDING);
    }

    HttpUtil.DownloadProgressListener createDownloadNotifier(final int i) {
        return new HttpUtil.DownloadProgressListener(){
            private final SystemToast.SystemToastId toastId = new SystemToast.SystemToastId();
            private Component title = Component.empty();
            private @Nullable Component message = null;
            private int count;
            private int failCount;
            private OptionalLong totalBytes = OptionalLong.empty();

            private void updateToast() {
                DownloadedPackSource.this.minecraft.execute(() -> SystemToast.addOrUpdate(DownloadedPackSource.this.minecraft.getToastManager(), this.toastId, this.title, this.message));
            }

            private void updateProgress(long l) {
                this.message = this.totalBytes.isPresent() ? Component.translatable("download.pack.progress.percent", l * 100L / this.totalBytes.getAsLong()) : Component.translatable("download.pack.progress.bytes", Unit.humanReadable(l));
                this.updateToast();
            }

            @Override
            public void requestStart() {
                ++this.count;
                this.title = Component.translatable("download.pack.title", this.count, i);
                this.updateToast();
                LOGGER.debug("Starting pack {}/{} download", (Object)this.count, (Object)i);
            }

            @Override
            public void downloadStart(OptionalLong optionalLong) {
                LOGGER.debug("File size = {} bytes", (Object)optionalLong);
                this.totalBytes = optionalLong;
                this.updateProgress(0L);
            }

            @Override
            public void downloadedBytes(long l) {
                LOGGER.debug("Progress for pack {}: {} bytes", (Object)this.count, (Object)l);
                this.updateProgress(l);
            }

            @Override
            public void requestFinished(boolean bl) {
                if (!bl) {
                    LOGGER.info("Pack {} failed to download", (Object)this.count);
                    ++this.failCount;
                } else {
                    LOGGER.debug("Download ended for pack {}", (Object)this.count);
                }
                if (this.count == i) {
                    if (this.failCount > 0) {
                        this.title = Component.translatable("download.pack.failed", this.failCount, i);
                        this.message = null;
                        this.updateToast();
                    } else {
                        SystemToast.forceHide(DownloadedPackSource.this.minecraft.getToastManager(), this.toastId);
                    }
                }
            }
        };
    }

    private PackDownloader createDownloader(final DownloadQueue downloadQueue, final Executor executor, final User user, final Proxy proxy) {
        return new PackDownloader(){
            private static final int MAX_PACK_SIZE_BYTES = 0xFA00000;
            private static final HashFunction CACHE_HASHING_FUNCTION = Hashing.sha1();

            private Map<String, String> createDownloadHeaders() {
                WorldVersion worldVersion = SharedConstants.getCurrentVersion();
                return Map.of((Object)"X-Minecraft-Username", (Object)user.getName(), (Object)"X-Minecraft-UUID", (Object)UndashedUuid.toString((UUID)user.getProfileId()), (Object)"X-Minecraft-Version", (Object)worldVersion.name(), (Object)"X-Minecraft-Version-ID", (Object)worldVersion.id(), (Object)"X-Minecraft-Pack-Format", (Object)String.valueOf(worldVersion.packVersion(PackType.CLIENT_RESOURCES)), (Object)"User-Agent", (Object)("Minecraft Java/" + worldVersion.name()));
            }

            @Override
            public void download(Map<UUID, DownloadQueue.DownloadRequest> map, Consumer<DownloadQueue.BatchResult> consumer) {
                downloadQueue.downloadBatch(new DownloadQueue.BatchConfig(CACHE_HASHING_FUNCTION, 0xFA00000, this.createDownloadHeaders(), proxy, DownloadedPackSource.this.createDownloadNotifier(map.size())), map).thenAcceptAsync((Consumer)consumer, executor);
            }
        };
    }

    private Runnable createUpdateScheduler(final Executor executor) {
        return new Runnable(){
            private boolean scheduledInMainExecutor;
            private boolean hasUpdates;

            @Override
            public void run() {
                this.hasUpdates = true;
                if (!this.scheduledInMainExecutor) {
                    this.scheduledInMainExecutor = true;
                    executor.execute(this::runAllUpdates);
                }
            }

            private void runAllUpdates() {
                while (this.hasUpdates) {
                    this.hasUpdates = false;
                    DownloadedPackSource.this.manager.tick();
                }
                this.scheduledInMainExecutor = false;
            }
        };
    }

    private PackReloadConfig createReloadConfig() {
        return this::startReload;
    }

    private @Nullable List<Pack> loadRequestedPacks(List<PackReloadConfig.IdAndPath> list) {
        ArrayList<Pack> list2 = new ArrayList<Pack>(list.size());
        for (PackReloadConfig.IdAndPath idAndPath : Lists.reverse(list)) {
            PackFormat packFormat;
            FilePackResources.FileResourcesSupplier resourcesSupplier;
            String string = String.format(Locale.ROOT, "server/%08X/%s", this.packIdSerialNumber++, idAndPath.id());
            Path path = idAndPath.path();
            PackLocationInfo packLocationInfo = new PackLocationInfo(string, SERVER_NAME, this.packType, Optional.empty());
            Pack.Metadata metadata = Pack.readPackMetadata(packLocationInfo, resourcesSupplier = new FilePackResources.FileResourcesSupplier(path), packFormat = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES), PackType.CLIENT_RESOURCES);
            if (metadata == null) {
                LOGGER.warn("Invalid pack metadata in {}, ignoring all", (Object)path);
                return null;
            }
            list2.add(new Pack(packLocationInfo, resourcesSupplier, metadata, DOWNLOADED_PACK_SELECTION));
        }
        return list2;
    }

    public RepositorySource createRepositorySource() {
        return consumer -> this.packSource.loadPacks(consumer);
    }

    private static RepositorySource configureSource(List<Pack> list) {
        if (list.isEmpty()) {
            return EMPTY_SOURCE;
        }
        return list::forEach;
    }

    private void startReload(PackReloadConfig.Callbacks callbacks) {
        this.pendingReload = callbacks;
        List<PackReloadConfig.IdAndPath> list = callbacks.packsToLoad();
        List list2 = this.loadRequestedPacks(list);
        if (list2 == null) {
            callbacks.onFailure(false);
            List<PackReloadConfig.IdAndPath> list3 = callbacks.packsToLoad();
            list2 = this.loadRequestedPacks(list3);
            if (list2 == null) {
                LOGGER.warn("Double failure in loading server packs");
                list2 = List.of();
            }
        }
        this.packSource = DownloadedPackSource.configureSource(list2);
        this.minecraft.reloadResourcePacks();
    }

    public void onRecovery() {
        if (this.pendingReload != null) {
            this.pendingReload.onFailure(false);
            List list = this.loadRequestedPacks(this.pendingReload.packsToLoad());
            if (list == null) {
                LOGGER.warn("Double failure in loading server packs");
                list = List.of();
            }
            this.packSource = DownloadedPackSource.configureSource(list);
        }
    }

    public void onRecoveryFailure() {
        if (this.pendingReload != null) {
            this.pendingReload.onFailure(true);
            this.pendingReload = null;
            this.packSource = EMPTY_SOURCE;
        }
    }

    public void onReloadSuccess() {
        if (this.pendingReload != null) {
            this.pendingReload.onSuccess();
            this.pendingReload = null;
        }
    }

    private static @Nullable HashCode tryParseSha1Hash(@Nullable String string) {
        if (string != null && SHA1.matcher(string).matches()) {
            return HashCode.fromString((String)string.toLowerCase(Locale.ROOT));
        }
        return null;
    }

    public void pushPack(UUID uUID, URL uRL, @Nullable String string) {
        HashCode hashCode = DownloadedPackSource.tryParseSha1Hash(string);
        this.manager.pushPack(uUID, uRL, hashCode);
    }

    public void pushLocalPack(UUID uUID, Path path) {
        this.manager.pushLocalPack(uUID, path);
    }

    public void popPack(UUID uUID) {
        this.manager.popPack(uUID);
    }

    public void popAll() {
        this.manager.popAll();
    }

    private static PackLoadFeedback createPackResponseSender(final Connection connection) {
        return new PackLoadFeedback(){

            @Override
            public void reportUpdate(UUID uUID, PackLoadFeedback.Update update) {
                LOGGER.debug("Pack {} changed status to {}", (Object)uUID, (Object)update);
                ServerboundResourcePackPacket.Action action = switch (update) {
                    default -> throw new MatchException(null, null);
                    case PackLoadFeedback.Update.ACCEPTED -> ServerboundResourcePackPacket.Action.ACCEPTED;
                    case PackLoadFeedback.Update.DOWNLOADED -> ServerboundResourcePackPacket.Action.DOWNLOADED;
                };
                connection.send(new ServerboundResourcePackPacket(uUID, action));
            }

            @Override
            public void reportFinalResult(UUID uUID, PackLoadFeedback.FinalResult finalResult) {
                LOGGER.debug("Pack {} changed status to {}", (Object)uUID, (Object)finalResult);
                ServerboundResourcePackPacket.Action action = switch (finalResult) {
                    default -> throw new MatchException(null, null);
                    case PackLoadFeedback.FinalResult.APPLIED -> ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED;
                    case PackLoadFeedback.FinalResult.DOWNLOAD_FAILED -> ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD;
                    case PackLoadFeedback.FinalResult.DECLINED -> ServerboundResourcePackPacket.Action.DECLINED;
                    case PackLoadFeedback.FinalResult.DISCARDED -> ServerboundResourcePackPacket.Action.DISCARDED;
                    case PackLoadFeedback.FinalResult.ACTIVATION_FAILED -> ServerboundResourcePackPacket.Action.FAILED_RELOAD;
                };
                connection.send(new ServerboundResourcePackPacket(uUID, action));
            }
        };
    }

    public void configureForServerControl(Connection connection, ServerPackManager.PackPromptStatus packPromptStatus) {
        this.packType = PackSource.SERVER;
        this.packFeedback = DownloadedPackSource.createPackResponseSender(connection);
        switch (packPromptStatus) {
            case ALLOWED: {
                this.manager.allowServerPacks();
                break;
            }
            case DECLINED: {
                this.manager.rejectServerPacks();
                break;
            }
            case PENDING: {
                this.manager.resetPromptStatus();
            }
        }
    }

    public void configureForLocalWorld() {
        this.packType = PackSource.WORLD;
        this.packFeedback = LOG_ONLY_FEEDBACK;
        this.manager.allowServerPacks();
    }

    public void allowServerPacks() {
        this.manager.allowServerPacks();
    }

    public void rejectServerPacks() {
        this.manager.rejectServerPacks();
    }

    public CompletableFuture<Void> waitForPackFeedback(final UUID uUID) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        final PackLoadFeedback packLoadFeedback = this.packFeedback;
        this.packFeedback = new PackLoadFeedback(){

            @Override
            public void reportUpdate(UUID uUID2, PackLoadFeedback.Update update) {
                packLoadFeedback.reportUpdate(uUID2, update);
            }

            @Override
            public void reportFinalResult(UUID uUID2, PackLoadFeedback.FinalResult finalResult) {
                if (uUID.equals(uUID2)) {
                    DownloadedPackSource.this.packFeedback = packLoadFeedback;
                    if (finalResult == PackLoadFeedback.FinalResult.APPLIED) {
                        completableFuture.complete(null);
                    } else {
                        completableFuture.completeExceptionally(new IllegalStateException("Failed to apply pack " + String.valueOf(uUID2) + ", reason: " + String.valueOf((Object)finalResult)));
                    }
                }
                packLoadFeedback.reportFinalResult(uUID2, finalResult);
            }
        };
        return completableFuture;
    }

    public void cleanupAfterDisconnect() {
        this.manager.popAll();
        this.packFeedback = LOG_ONLY_FEEDBACK;
        this.manager.resetPromptStatus();
    }

    @Override
    public void close() throws IOException {
        this.downloadQueue.close();
    }
}

