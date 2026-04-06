/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FilePackResources
extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final SharedZipFileAccess zipFileAccess;
    private final String prefix;

    FilePackResources(PackLocationInfo packLocationInfo, SharedZipFileAccess sharedZipFileAccess, String string) {
        super(packLocationInfo);
        this.zipFileAccess = sharedZipFileAccess;
        this.prefix = string;
    }

    private static String getPathFromLocation(PackType packType, Identifier identifier) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), identifier.getNamespace(), identifier.getPath());
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... strings) {
        return this.getResource(String.join((CharSequence)"/", strings));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, Identifier identifier) {
        return this.getResource(FilePackResources.getPathFromLocation(packType, identifier));
    }

    private String addPrefix(String string) {
        if (this.prefix.isEmpty()) {
            return string;
        }
        return this.prefix + "/" + string;
    }

    private @Nullable IoSupplier<InputStream> getResource(String string) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return null;
        }
        ZipEntry zipEntry = zipFile.getEntry(this.addPrefix(string));
        if (zipEntry == null) {
            return null;
        }
        return IoSupplier.create(zipFile, zipEntry);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return Set.of();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        HashSet set = Sets.newHashSet();
        String string = this.addPrefix(packType.getDirectory() + "/");
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            String string2 = zipEntry.getName();
            String string3 = FilePackResources.extractNamespace(string, string2);
            if (string3.isEmpty()) continue;
            if (Identifier.isValidNamespace(string3)) {
                set.add(string3);
                continue;
            }
            LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)string3, (Object)this.zipFileAccess.file);
        }
        return set;
    }

    @VisibleForTesting
    public static String extractNamespace(String string, String string2) {
        if (!string2.startsWith(string)) {
            return "";
        }
        int i = string.length();
        int j = string2.indexOf(47, i);
        if (j == -1) {
            return string2.substring(i);
        }
        return string2.substring(i, j);
    }

    @Override
    public void close() {
        this.zipFileAccess.close();
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
        if (zipFile == null) {
            return;
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        String string3 = this.addPrefix(packType.getDirectory() + "/" + string + "/");
        String string4 = string3 + string2 + "/";
        while (enumeration.hasMoreElements()) {
            String string5;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || !(string5 = zipEntry.getName()).startsWith(string4)) continue;
            String string6 = string5.substring(string3.length());
            Identifier identifier = Identifier.tryBuild(string, string6);
            if (identifier != null) {
                resourceOutput.accept(identifier, IoSupplier.create(zipFile, zipEntry));
                continue;
            }
            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)string, (Object)string6);
        }
    }

    static class SharedZipFileAccess
    implements AutoCloseable {
        final File file;
        private @Nullable ZipFile zipFile;
        private boolean failedToLoad;

        SharedZipFileAccess(File file) {
            this.file = file;
        }

        @Nullable ZipFile getOrCreateZipFile() {
            if (this.failedToLoad) {
                return null;
            }
            if (this.zipFile == null) {
                try {
                    this.zipFile = new ZipFile(this.file);
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to open pack {}", (Object)this.file, (Object)iOException);
                    this.failedToLoad = true;
                    return null;
                }
            }
            return this.zipFile;
        }

        @Override
        public void close() {
            if (this.zipFile != null) {
                IOUtils.closeQuietly((Closeable)this.zipFile);
                this.zipFile = null;
            }
        }

        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }
    }

    public static class FileResourcesSupplier
    implements Pack.ResourcesSupplier {
        private final File content;

        public FileResourcesSupplier(Path path) {
            this(path.toFile());
        }

        public FileResourcesSupplier(File file) {
            this.content = file;
        }

        @Override
        public PackResources openPrimary(PackLocationInfo packLocationInfo) {
            SharedZipFileAccess sharedZipFileAccess = new SharedZipFileAccess(this.content);
            return new FilePackResources(packLocationInfo, sharedZipFileAccess, "");
        }

        @Override
        public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
            SharedZipFileAccess sharedZipFileAccess = new SharedZipFileAccess(this.content);
            FilePackResources packResources = new FilePackResources(packLocationInfo, sharedZipFileAccess, "");
            List<String> list = metadata.overlays();
            if (list.isEmpty()) {
                return packResources;
            }
            ArrayList<PackResources> list2 = new ArrayList<PackResources>(list.size());
            for (String string : list) {
                list2.add(new FilePackResources(packLocationInfo, sharedZipFileAccess, string));
            }
            return new CompositePackResources(packResources, list2);
        }
    }
}

