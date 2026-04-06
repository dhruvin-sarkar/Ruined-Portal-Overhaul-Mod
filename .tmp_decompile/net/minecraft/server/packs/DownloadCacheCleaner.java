/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;

public class DownloadCacheCleaner {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void vacuumCacheDir(Path path, int i) {
        try {
            List<PathAndTime> list = DownloadCacheCleaner.listFilesWithModificationTimes(path);
            int j = list.size() - i;
            if (j <= 0) {
                return;
            }
            list.sort(PathAndTime.NEWEST_FIRST);
            List<PathAndPriority> list2 = DownloadCacheCleaner.prioritizeFilesInDirs(list);
            Collections.reverse(list2);
            list2.sort(PathAndPriority.HIGHEST_PRIORITY_FIRST);
            HashSet<Path> set = new HashSet<Path>();
            for (int k = 0; k < j; ++k) {
                PathAndPriority pathAndPriority = list2.get(k);
                Path path2 = pathAndPriority.path;
                try {
                    Files.delete(path2);
                    if (pathAndPriority.removalPriority != 0) continue;
                    set.add(path2.getParent());
                    continue;
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete cache file {}", (Object)path2, (Object)iOException);
                }
            }
            set.remove(path);
            for (Path path3 : set) {
                try {
                    Files.delete(path3);
                }
                catch (DirectoryNotEmptyException path2) {
                }
                catch (IOException iOException2) {
                    LOGGER.warn("Failed to delete empty(?) cache directory {}", (Object)path3, (Object)iOException2);
                }
            }
        }
        catch (IOException | UncheckedIOException exception) {
            LOGGER.error("Failed to vacuum cache dir {}", (Object)path, (Object)exception);
        }
    }

    private static List<PathAndTime> listFilesWithModificationTimes(final Path path) throws IOException {
        try {
            final ArrayList<PathAndTime> list = new ArrayList<PathAndTime>();
            Files.walkFileTree(path, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) {
                    if (basicFileAttributes.isRegularFile() && !path2.getParent().equals(path)) {
                        FileTime fileTime = basicFileAttributes.lastModifiedTime();
                        list.add(new PathAndTime(path2, fileTime));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                    return this.visitFile((Path)object, basicFileAttributes);
                }
            });
            return list;
        }
        catch (NoSuchFileException noSuchFileException) {
            return List.of();
        }
    }

    private static List<PathAndPriority> prioritizeFilesInDirs(List<PathAndTime> list) {
        ArrayList<PathAndPriority> list2 = new ArrayList<PathAndPriority>();
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (PathAndTime pathAndTime : list) {
            int i = object2IntOpenHashMap.addTo((Object)pathAndTime.path.getParent(), 1);
            list2.add(new PathAndPriority(pathAndTime.path, i));
        }
        return list2;
    }

    static final class PathAndTime
    extends Record {
        final Path path;
        private final FileTime modifiedTime;
        public static final Comparator<PathAndTime> NEWEST_FIRST = Comparator.comparing(PathAndTime::modifiedTime).reversed();

        PathAndTime(Path path, FileTime fileTime) {
            this.path = path;
            this.modifiedTime = fileTime;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PathAndTime.class, "path;modifiedTime", "path", "modifiedTime"}, this, object);
        }

        public Path path() {
            return this.path;
        }

        public FileTime modifiedTime() {
            return this.modifiedTime;
        }
    }

    static final class PathAndPriority
    extends Record {
        final Path path;
        final int removalPriority;
        public static final Comparator<PathAndPriority> HIGHEST_PRIORITY_FIRST = Comparator.comparing(PathAndPriority::removalPriority).reversed();

        PathAndPriority(Path path, int i) {
            this.path = path;
            this.removalPriority = i;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PathAndPriority.class, "path;removalPriority", "path", "removalPriority"}, this, object);
        }

        public Path path() {
            return this.path;
        }

        public int removalPriority() {
            return this.removalPriority;
        }
    }
}

