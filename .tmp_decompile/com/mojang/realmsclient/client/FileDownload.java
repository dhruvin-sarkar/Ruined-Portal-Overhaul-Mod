/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  com.google.common.io.Files
 *  com.mojang.logging.LogUtils
 *  java.net.http.HttpClient
 *  java.net.http.HttpRequest
 *  java.net.http.HttpRequest$Builder
 *  java.net.http.HttpResponse
 *  java.net.http.HttpResponse$BodyHandlers
 *  javax.annotation.CheckReturnValue
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.compress.archivers.tar.TarArchiveEntry
 *  org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 *  org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
 *  org.apache.commons.io.FileUtils
 *  org.apache.commons.io.IOUtils
 *  org.apache.commons.io.output.CountingOutputStream
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FileDownload {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile boolean cancelled;
    private volatile boolean finished;
    private volatile boolean error;
    private volatile boolean extracting;
    private volatile @Nullable File tempFile;
    private volatile File resourcePackPath;
    private volatile @Nullable CompletableFuture<?> pendingRequest;
    private @Nullable Thread currentThread;
    private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private <T> @Nullable T joinCancellableRequest(CompletableFuture<T> completableFuture) throws Throwable {
        this.pendingRequest = completableFuture;
        if (this.cancelled) {
            completableFuture.cancel(true);
            return null;
        }
        try {
            try {
                return completableFuture.join();
            }
            catch (CompletionException completionException) {
                throw completionException.getCause();
            }
        }
        catch (CancellationException cancellationException) {
            return null;
        }
    }

    private static HttpClient createClient() {
        return HttpClient.newBuilder().executor((Executor)Util.nonCriticalIoPool()).connectTimeout(Duration.ofMinutes(2L)).build();
    }

    private static HttpRequest.Builder createRequest(String string) {
        return HttpRequest.newBuilder((URI)URI.create(string)).timeout(Duration.ofMinutes(2L));
    }

    @CheckReturnValue
    public static OptionalLong contentLength(String string) {
        OptionalLong optionalLong;
        block8: {
            HttpClient httpClient = FileDownload.createClient();
            try {
                HttpResponse httpResponse = httpClient.send(FileDownload.createRequest(string).HEAD().build(), HttpResponse.BodyHandlers.discarding());
                optionalLong = httpResponse.headers().firstValueAsLong("Content-Length");
                if (httpClient == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (httpClient != null) {
                        try {
                            httpClient.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    LOGGER.error("Unable to get content length for download");
                    return OptionalLong.empty();
                }
            }
            httpClient.close();
        }
        return optionalLong;
    }

    public void download(WorldDownload worldDownload, String string, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, LevelStorageSource levelStorageSource) {
        if (this.currentThread != null) {
            return;
        }
        this.currentThread = new Thread(() -> {
            try (HttpClient httpClient = FileDownload.createClient();){
                try {
                    this.tempFile = File.createTempFile("backup", ".tar.gz");
                    this.download(downloadStatus, httpClient, worldDownload.downloadLink(), this.tempFile);
                    this.finishWorldDownload(string.trim(), this.tempFile, levelStorageSource, downloadStatus);
                }
                catch (Exception exception) {
                    LOGGER.error("Caught exception while downloading world", (Throwable)exception);
                    this.error = true;
                }
                finally {
                    this.pendingRequest = null;
                    if (this.tempFile != null) {
                        this.tempFile.delete();
                    }
                    this.tempFile = null;
                }
                if (this.error) {
                    return;
                }
                String string2 = worldDownload.resourcePackUrl();
                if (!string2.isEmpty() && !worldDownload.resourcePackHash().isEmpty()) {
                    try {
                        this.tempFile = File.createTempFile("resources", ".tar.gz");
                        this.download(downloadStatus, httpClient, string2, this.tempFile);
                        this.finishResourcePackDownload(downloadStatus, this.tempFile, worldDownload);
                    }
                    catch (Exception exception2) {
                        LOGGER.error("Caught exception while downloading resource pack", (Throwable)exception2);
                        this.error = true;
                    }
                    finally {
                        this.pendingRequest = null;
                        if (this.tempFile != null) {
                            this.tempFile.delete();
                        }
                        this.tempFile = null;
                    }
                }
                this.finished = true;
            }
        });
        this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        this.currentThread.start();
    }

    private void download(RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, HttpClient httpClient, String string, File file) throws IOException {
        HttpResponse httpResponse;
        HttpRequest httpRequest = FileDownload.createRequest(string).GET().build();
        try {
            httpResponse = (HttpResponse)this.joinCancellableRequest(httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()));
        }
        catch (Error error) {
            throw error;
        }
        catch (Throwable throwable) {
            LOGGER.error("Failed to download {}", (Object)string, (Object)throwable);
            this.error = true;
            return;
        }
        if (httpResponse == null || this.cancelled) {
            return;
        }
        if (httpResponse.statusCode() != 200) {
            this.error = true;
            return;
        }
        downloadStatus.totalBytes = httpResponse.headers().firstValueAsLong("Content-Length").orElse(0L);
        try (InputStream inputStream = (InputStream)httpResponse.body();
             FileOutputStream outputStream = new FileOutputStream(file);){
            inputStream.transferTo((OutputStream)((Object)new DownloadCountingOutputStream(outputStream, downloadStatus)));
        }
    }

    public void cancel() {
        if (this.tempFile != null) {
            this.tempFile.delete();
            this.tempFile = null;
        }
        this.cancelled = true;
        CompletableFuture<?> completableFuture = this.pendingRequest;
        if (completableFuture != null) {
            completableFuture.cancel(true);
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isError() {
        return this.error;
    }

    public boolean isExtracting() {
        return this.extracting;
    }

    public static String findAvailableFolderName(String string) {
        string = ((String)string).replaceAll("[\\./\"]", "_");
        for (String string2 : INVALID_FILE_NAMES) {
            if (!((String)string).equalsIgnoreCase(string2)) continue;
            string = "_" + (String)string + "_";
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void untarGzipArchive(String string, @Nullable File file, LevelStorageSource levelStorageSource) throws IOException {
        Object string3;
        Pattern pattern = Pattern.compile(".*-([0-9]+)$");
        int i = 1;
        for (char c : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            string = string.replace(c, '_');
        }
        if (StringUtils.isEmpty((CharSequence)string)) {
            string = "Realm";
        }
        string = FileDownload.findAvailableFolderName(string);
        try {
            Object object = levelStorageSource.findLevelCandidates().iterator();
            while (object.hasNext()) {
                LevelStorageSource.LevelDirectory levelDirectory = (LevelStorageSource.LevelDirectory)((Object)object.next());
                String string2 = levelDirectory.directoryName();
                if (!string2.toLowerCase(Locale.ROOT).startsWith(string.toLowerCase(Locale.ROOT))) continue;
                Matcher matcher = pattern.matcher(string2);
                if (matcher.matches()) {
                    int j = Integer.parseInt(matcher.group(1));
                    if (j <= i) continue;
                    i = j;
                    continue;
                }
                ++i;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error getting level list", (Throwable)exception);
            this.error = true;
            return;
        }
        if (!levelStorageSource.isNewLevelIdAcceptable(string) || i > 1) {
            string3 = string + (String)(i == 1 ? "" : "-" + i);
            if (!levelStorageSource.isNewLevelIdAcceptable((String)string3)) {
                boolean bl = false;
                while (!bl) {
                    if (!levelStorageSource.isNewLevelIdAcceptable((String)(string3 = string + (String)(++i == 1 ? "" : "-" + i)))) continue;
                    bl = true;
                }
            }
        } else {
            string3 = string;
        }
        TarArchiveInputStream tarArchiveInputStream = null;
        File file2 = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "saves");
        try {
            file2.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream((InputStream)new GzipCompressorInputStream((InputStream)new BufferedInputStream(new FileInputStream(file))));
            TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            while (tarArchiveEntry != null) {
                File file3 = new File(file2, tarArchiveEntry.getName().replace("world", (CharSequence)string3));
                if (tarArchiveEntry.isDirectory()) {
                    file3.mkdirs();
                } else {
                    file3.createNewFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file3);){
                        IOUtils.copy((InputStream)tarArchiveInputStream, (OutputStream)fileOutputStream);
                    }
                }
                tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            }
        }
        catch (Exception exception2) {
            LOGGER.error("Error extracting world", (Throwable)exception2);
            this.error = true;
        }
        finally {
            if (tarArchiveInputStream != null) {
                tarArchiveInputStream.close();
            }
            if (file != null) {
                file.delete();
            }
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.validateAndCreateAccess((String)string3);){
                levelStorageAccess.renameAndDropPlayer((String)string3);
            }
            catch (IOException | NbtException | ReportedNbtException exception2) {
                LOGGER.error("Failed to modify unpacked realms level {}", string3, (Object)exception2);
            }
            catch (ContentValidationException contentValidationException) {
                LOGGER.warn("Failed to download file", (Throwable)contentValidationException);
            }
            this.resourcePackPath = new File(file2, (String)string3 + File.separator + "resources.zip");
        }
    }

    private void finishWorldDownload(String string, File file, LevelStorageSource levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
        if (downloadStatus.bytesWritten >= downloadStatus.totalBytes && !this.cancelled && !this.error) {
            try {
                this.extracting = true;
                this.untarGzipArchive(string, file, levelStorageSource);
            }
            catch (IOException iOException) {
                LOGGER.error("Error extracting archive", (Throwable)iOException);
                this.error = true;
            }
        }
    }

    private void finishResourcePackDownload(RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, File file, WorldDownload worldDownload) {
        if (downloadStatus.bytesWritten >= downloadStatus.totalBytes && !this.cancelled) {
            try {
                String string = Hashing.sha1().hashBytes(Files.toByteArray((File)file)).toString();
                if (string.equals(worldDownload.resourcePackHash())) {
                    FileUtils.copyFile((File)file, (File)this.resourcePackPath);
                    this.finished = true;
                } else {
                    LOGGER.error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", (Object)worldDownload.resourcePackHash(), (Object)string);
                    FileUtils.deleteQuietly((File)file);
                    this.error = true;
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Error copying resourcepack file: {}", (Object)iOException.getMessage());
                this.error = true;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DownloadCountingOutputStream
    extends CountingOutputStream {
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

        public DownloadCountingOutputStream(OutputStream outputStream, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
            super(outputStream);
            this.downloadStatus = downloadStatus;
        }

        protected void afterWrite(int i) throws IOException {
            super.afterWrite(i);
            this.downloadStatus.bytesWritten = this.getByteCount();
        }
    }
}

