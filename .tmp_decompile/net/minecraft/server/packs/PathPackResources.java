/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PathPackResources
extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Joiner PATH_JOINER = Joiner.on((String)"/");
    private final Path root;

    public PathPackResources(PackLocationInfo packLocationInfo, Path path) {
        super(packLocationInfo);
        this.root = path;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... strings) {
        FileUtil.validatePath(strings);
        Path path = FileUtil.resolvePath(this.root, List.of((Object[])strings));
        if (Files.exists(path, new LinkOption[0])) {
            return IoSupplier.create(path);
        }
        return null;
    }

    public static boolean validatePath(Path path) {
        if (!SharedConstants.DEBUG_VALIDATE_RESOURCE_PATH_CASE) {
            return true;
        }
        if (path.getFileSystem() != FileSystems.getDefault()) {
            return true;
        }
        try {
            return path.toRealPath(new LinkOption[0]).endsWith(path);
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to resolve real path for {}", (Object)path, (Object)iOException);
            return false;
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, Identifier identifier) {
        Path path = this.root.resolve(packType.getDirectory()).resolve(identifier.getNamespace());
        return PathPackResources.getResource(identifier, path);
    }

    public static @Nullable IoSupplier<InputStream> getResource(Identifier identifier, Path path) {
        return (IoSupplier)FileUtil.decomposePath(identifier.getPath()).mapOrElse(list -> {
            Path path2 = FileUtil.resolvePath(path, list);
            return PathPackResources.returnFileIfExists(path2);
        }, error -> {
            LOGGER.error("Invalid path {}: {}", (Object)identifier, (Object)error.message());
            return null;
        });
    }

    private static @Nullable IoSupplier<InputStream> returnFileIfExists(Path path) {
        if (Files.exists(path, new LinkOption[0]) && PathPackResources.validatePath(path)) {
            return IoSupplier.create(path);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        FileUtil.decomposePath(string2).ifSuccess(list -> {
            Path path = this.root.resolve(packType.getDirectory()).resolve(string);
            PathPackResources.listPath(string, path, list, resourceOutput);
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)string2, (Object)error.message()));
    }

    public static void listPath(String string, Path path, List<String> list, PackResources.ResourceOutput resourceOutput) {
        Path path22 = FileUtil.resolvePath(path, list);
        try (Stream<Path> stream2 = Files.find(path22, Integer.MAX_VALUE, PathPackResources::isRegularFile, new FileVisitOption[0]);){
            stream2.forEach(path2 -> {
                String string2 = PATH_JOINER.join((Iterable)path.relativize((Path)path2));
                Identifier identifier = Identifier.tryBuild(string, string2);
                if (identifier == null) {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", string, string2));
                } else {
                    resourceOutput.accept(identifier, IoSupplier.create(path2));
                }
            });
        }
        catch (NoSuchFileException | NotDirectoryException stream2) {
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path22, (Object)iOException);
        }
    }

    private static boolean isRegularFile(Path path, BasicFileAttributes basicFileAttributes) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return basicFileAttributes.isRegularFile() && !StringUtils.equalsIgnoreCase((CharSequence)path.getFileName().toString(), (CharSequence)".ds_store");
        }
        return basicFileAttributes.isRegularFile();
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        HashSet set = Sets.newHashSet();
        Path path = this.root.resolve(packType.getDirectory());
        try (DirectoryStream<Path> directoryStream2 = Files.newDirectoryStream(path);){
            for (Path path2 : directoryStream2) {
                String string = path2.getFileName().toString();
                if (Identifier.isValidNamespace(string)) {
                    set.add(string);
                    continue;
                }
                LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", (Object)string, (Object)this.root);
            }
        }
        catch (NoSuchFileException | NotDirectoryException directoryStream2) {
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to list path {}", (Object)path, (Object)iOException);
        }
        return set;
    }

    @Override
    public void close() {
    }

    public static class PathResourcesSupplier
    implements Pack.ResourcesSupplier {
        private final Path content;

        public PathResourcesSupplier(Path path) {
            this.content = path;
        }

        @Override
        public PackResources openPrimary(PackLocationInfo packLocationInfo) {
            return new PathPackResources(packLocationInfo, this.content);
        }

        @Override
        public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
            PackResources packResources = this.openPrimary(packLocationInfo);
            List<String> list = metadata.overlays();
            if (list.isEmpty()) {
                return packResources;
            }
            ArrayList<PackResources> list2 = new ArrayList<PackResources>(list.size());
            for (String string : list) {
                Path path = this.content.resolve(string);
                list2.add(new PathPackResources(packLocationInfo, path));
            }
            return new CompositePackResources(packResources, list2);
        }
    }
}

