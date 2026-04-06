/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMap
 *  it.unimi.dsi.fastutil.objects.Reference2FloatMaps
 *  it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.worldupdate;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.LegacyTagFixer;
import net.minecraft.world.level.chunk.storage.RecreatingSimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WorldUpgrader
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private static final String NEW_DIRECTORY_PREFIX = "new_";
    static final Component STATUS_UPGRADING_POI = Component.translatable("optimizeWorld.stage.upgrading.poi");
    static final Component STATUS_FINISHED_POI = Component.translatable("optimizeWorld.stage.finished.poi");
    static final Component STATUS_UPGRADING_ENTITIES = Component.translatable("optimizeWorld.stage.upgrading.entities");
    static final Component STATUS_FINISHED_ENTITIES = Component.translatable("optimizeWorld.stage.finished.entities");
    static final Component STATUS_UPGRADING_CHUNKS = Component.translatable("optimizeWorld.stage.upgrading.chunks");
    static final Component STATUS_FINISHED_CHUNKS = Component.translatable("optimizeWorld.stage.finished.chunks");
    final Registry<LevelStem> dimensions;
    final Set<ResourceKey<Level>> levels;
    final boolean eraseCache;
    final boolean recreateRegionFiles;
    final LevelStorageSource.LevelStorageAccess levelStorage;
    private final Thread thread;
    final DataFixer dataFixer;
    volatile boolean running = true;
    private volatile boolean finished;
    volatile float progress;
    volatile int totalChunks;
    volatile int totalFiles;
    volatile int converted;
    volatile int skipped;
    final Reference2FloatMap<ResourceKey<Level>> progressMap = Reference2FloatMaps.synchronize((Reference2FloatMap)new Reference2FloatOpenHashMap());
    volatile Component status = Component.translatable("optimizeWorld.stage.counting");
    static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, WorldData worldData, RegistryAccess registryAccess, boolean bl, boolean bl2) {
        this.dimensions = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        this.levels = (Set)this.dimensions.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
        this.eraseCache = bl;
        this.dataFixer = dataFixer;
        this.levelStorage = levelStorageAccess;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data"), dataFixer, registryAccess);
        this.recreateRegionFiles = bl2;
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = Component.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;
        try {
            this.thread.join();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void work() {
        long l = Util.getMillis();
        LOGGER.info("Upgrading entities");
        new EntityUpgrader(this).upgrade();
        LOGGER.info("Upgrading POIs");
        new PoiUpgrader(this).upgrade();
        LOGGER.info("Upgrading blocks");
        new ChunkUpgrader().upgrade();
        this.overworldDataStorage.saveAndJoin();
        l = Util.getMillis() - l;
        LOGGER.info("World optimizaton finished after {} seconds", (Object)(l / 1000L));
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public float dimensionProgress(ResourceKey<Level> resourceKey) {
        return this.progressMap.getFloat(resourceKey);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public Component getStatus() {
        return this.status;
    }

    @Override
    public void close() {
        this.overworldDataStorage.close();
    }

    static Path resolveRecreateDirectory(Path path) {
        return path.resolveSibling(NEW_DIRECTORY_PREFIX + path.getFileName().toString());
    }

    class EntityUpgrader
    extends SimpleRegionStorageUpgrader {
        EntityUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.ENTITY_CHUNK, "entities", STATUS_UPGRADING_ENTITIES, STATUS_FINISHED_ENTITIES);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
            return simpleRegionStorage.upgradeChunkTag(compoundTag, -1);
        }
    }

    class PoiUpgrader
    extends SimpleRegionStorageUpgrader {
        PoiUpgrader(WorldUpgrader worldUpgrader) {
            super(DataFixTypes.POI_CHUNK, "poi", STATUS_UPGRADING_POI, STATUS_FINISHED_POI);
        }

        @Override
        protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
            return simpleRegionStorage.upgradeChunkTag(compoundTag, 1945);
        }
    }

    class ChunkUpgrader
    extends AbstractUpgrader {
        ChunkUpgrader() {
            super(DataFixTypes.CHUNK, "chunk", "region", STATUS_UPGRADING_CHUNKS, STATUS_FINISHED_CHUNKS);
        }

        @Override
        protected boolean tryProcessOnePosition(SimpleRegionStorage simpleRegionStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
            CompoundTag compoundTag = simpleRegionStorage.read(chunkPos).join().orElse(null);
            if (compoundTag != null) {
                boolean bl;
                int i = NbtUtils.getDataVersion(compoundTag);
                ChunkGenerator chunkGenerator = WorldUpgrader.this.dimensions.getValueOrThrow(Registries.levelToLevelStem(resourceKey)).generator();
                CompoundTag compoundTag2 = simpleRegionStorage.upgradeChunkTag(compoundTag, -1, ChunkMap.getChunkDataFixContextTag(resourceKey, chunkGenerator.getTypeNameForDataFixer()));
                ChunkPos chunkPos2 = new ChunkPos(compoundTag2.getIntOr("xPos", 0), compoundTag2.getIntOr("zPos", 0));
                if (!chunkPos2.equals(chunkPos)) {
                    LOGGER.warn("Chunk {} has invalid position {}", (Object)chunkPos, (Object)chunkPos2);
                }
                boolean bl2 = bl = i < SharedConstants.getCurrentVersion().dataVersion().version();
                if (WorldUpgrader.this.eraseCache) {
                    bl = bl || compoundTag2.contains("Heightmaps");
                    compoundTag2.remove("Heightmaps");
                    bl = bl || compoundTag2.contains("isLightOn");
                    compoundTag2.remove("isLightOn");
                    ListTag listTag = compoundTag2.getListOrEmpty("sections");
                    for (int j = 0; j < listTag.size(); ++j) {
                        Optional<CompoundTag> optional = listTag.getCompound(j);
                        if (optional.isEmpty()) continue;
                        CompoundTag compoundTag3 = optional.get();
                        bl = bl || compoundTag3.contains("BlockLight");
                        compoundTag3.remove("BlockLight");
                        bl = bl || compoundTag3.contains("SkyLight");
                        compoundTag3.remove("SkyLight");
                    }
                }
                if (bl || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = simpleRegionStorage.write(chunkPos, compoundTag2);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            Supplier<LegacyTagFixer> supplier = LegacyStructureDataHandler.getLegacyTagFixer(regionStorageInfo.dimension(), () -> WorldUpgrader.this.overworldDataStorage, WorldUpgrader.this.dataFixer);
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage(regionStorageInfo.withTypeSuffix("source"), path, regionStorageInfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true, DataFixTypes.CHUNK, supplier) : new SimpleRegionStorage(regionStorageInfo, path, WorldUpgrader.this.dataFixer, true, DataFixTypes.CHUNK, supplier);
        }
    }

    abstract class SimpleRegionStorageUpgrader
    extends AbstractUpgrader {
        SimpleRegionStorageUpgrader(DataFixTypes dataFixTypes, String string, Component component, Component component2) {
            super(dataFixTypes, string, string, component, component2);
        }

        @Override
        protected SimpleRegionStorage createStorage(RegionStorageInfo regionStorageInfo, Path path) {
            return WorldUpgrader.this.recreateRegionFiles ? new RecreatingSimpleRegionStorage(regionStorageInfo.withTypeSuffix("source"), path, regionStorageInfo.withTypeSuffix("target"), WorldUpgrader.resolveRecreateDirectory(path), WorldUpgrader.this.dataFixer, true, this.dataFixType, LegacyTagFixer.EMPTY) : new SimpleRegionStorage(regionStorageInfo, path, WorldUpgrader.this.dataFixer, true, this.dataFixType);
        }

        @Override
        protected boolean tryProcessOnePosition(SimpleRegionStorage simpleRegionStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
            CompoundTag compoundTag = simpleRegionStorage.read(chunkPos).join().orElse(null);
            if (compoundTag != null) {
                boolean bl;
                int i = NbtUtils.getDataVersion(compoundTag);
                CompoundTag compoundTag2 = this.upgradeTag(simpleRegionStorage, compoundTag);
                boolean bl2 = bl = i < SharedConstants.getCurrentVersion().dataVersion().version();
                if (bl || WorldUpgrader.this.recreateRegionFiles) {
                    if (this.previousWriteFuture != null) {
                        this.previousWriteFuture.join();
                    }
                    this.previousWriteFuture = simpleRegionStorage.write(chunkPos, compoundTag2);
                    return true;
                }
            }
            return false;
        }

        protected abstract CompoundTag upgradeTag(SimpleRegionStorage var1, CompoundTag var2);
    }

    abstract class AbstractUpgrader {
        private final Component upgradingStatus;
        private final Component finishedStatus;
        private final String type;
        private final String folderName;
        protected @Nullable CompletableFuture<Void> previousWriteFuture;
        protected final DataFixTypes dataFixType;

        AbstractUpgrader(DataFixTypes dataFixTypes, String string, String string2, Component component, Component component2) {
            this.dataFixType = dataFixTypes;
            this.type = string;
            this.folderName = string2;
            this.upgradingStatus = component;
            this.finishedStatus = component2;
        }

        public void upgrade() {
            WorldUpgrader.this.totalFiles = 0;
            WorldUpgrader.this.totalChunks = 0;
            WorldUpgrader.this.converted = 0;
            WorldUpgrader.this.skipped = 0;
            List<DimensionToUpgrade> list = this.getDimensionsToUpgrade();
            if (WorldUpgrader.this.totalChunks == 0) {
                return;
            }
            float f = WorldUpgrader.this.totalFiles;
            WorldUpgrader.this.status = this.upgradingStatus;
            while (WorldUpgrader.this.running) {
                boolean bl = false;
                float g = 0.0f;
                for (DimensionToUpgrade dimensionToUpgrade : list) {
                    ResourceKey<Level> resourceKey = dimensionToUpgrade.dimensionKey;
                    ListIterator<FileToUpgrade> listIterator = dimensionToUpgrade.files;
                    SimpleRegionStorage simpleRegionStorage = dimensionToUpgrade.storage;
                    if (listIterator.hasNext()) {
                        FileToUpgrade fileToUpgrade = listIterator.next();
                        boolean bl2 = true;
                        for (ChunkPos chunkPos : fileToUpgrade.chunksToUpgrade) {
                            bl2 = bl2 && this.processOnePosition(resourceKey, simpleRegionStorage, chunkPos);
                            bl = true;
                        }
                        if (WorldUpgrader.this.recreateRegionFiles) {
                            if (bl2) {
                                this.onFileFinished(fileToUpgrade.file);
                            } else {
                                LOGGER.error("Failed to convert region file {}", (Object)fileToUpgrade.file.getPath());
                            }
                        }
                    }
                    float h = (float)listIterator.nextIndex() / f;
                    WorldUpgrader.this.progressMap.put(resourceKey, h);
                    g += h;
                }
                WorldUpgrader.this.progress = g;
                if (bl) continue;
                break;
            }
            WorldUpgrader.this.status = this.finishedStatus;
            for (DimensionToUpgrade dimensionToUpgrade2 : list) {
                try {
                    dimensionToUpgrade2.storage.close();
                }
                catch (Exception exception) {
                    LOGGER.error("Error upgrading chunk", (Throwable)exception);
                }
            }
        }

        private List<DimensionToUpgrade> getDimensionsToUpgrade() {
            ArrayList list = Lists.newArrayList();
            for (ResourceKey<Level> resourceKey : WorldUpgrader.this.levels) {
                RegionStorageInfo regionStorageInfo = new RegionStorageInfo(WorldUpgrader.this.levelStorage.getLevelId(), resourceKey, this.type);
                Path path = WorldUpgrader.this.levelStorage.getDimensionPath(resourceKey).resolve(this.folderName);
                SimpleRegionStorage simpleRegionStorage = this.createStorage(regionStorageInfo, path);
                ListIterator<FileToUpgrade> listIterator = this.getFilesToProcess(regionStorageInfo, path);
                list.add(new DimensionToUpgrade(resourceKey, simpleRegionStorage, listIterator));
            }
            return list;
        }

        protected abstract SimpleRegionStorage createStorage(RegionStorageInfo var1, Path var2);

        private ListIterator<FileToUpgrade> getFilesToProcess(RegionStorageInfo regionStorageInfo, Path path) {
            List<FileToUpgrade> list = AbstractUpgrader.getAllChunkPositions(regionStorageInfo, path);
            WorldUpgrader.this.totalFiles += list.size();
            WorldUpgrader.this.totalChunks += list.stream().mapToInt(fileToUpgrade -> fileToUpgrade.chunksToUpgrade.size()).sum();
            return list.listIterator();
        }

        private static List<FileToUpgrade> getAllChunkPositions(RegionStorageInfo regionStorageInfo, Path path) {
            File[] files = path.toFile().listFiles((file, string) -> string.endsWith(".mca"));
            if (files == null) {
                return List.of();
            }
            ArrayList list = Lists.newArrayList();
            for (File file2 : files) {
                Matcher matcher = REGEX.matcher(file2.getName());
                if (!matcher.matches()) continue;
                int i = Integer.parseInt(matcher.group(1)) << 5;
                int j = Integer.parseInt(matcher.group(2)) << 5;
                ArrayList list2 = Lists.newArrayList();
                try (RegionFile regionFile = new RegionFile(regionStorageInfo, file2.toPath(), path, true);){
                    for (int k = 0; k < 32; ++k) {
                        for (int l = 0; l < 32; ++l) {
                            ChunkPos chunkPos = new ChunkPos(k + i, l + j);
                            if (!regionFile.doesChunkExist(chunkPos)) continue;
                            list2.add(chunkPos);
                        }
                    }
                    if (list2.isEmpty()) continue;
                    list.add(new FileToUpgrade(regionFile, list2));
                }
                catch (Throwable throwable) {
                    LOGGER.error("Failed to read chunks from region file {}", (Object)file2.toPath(), (Object)throwable);
                }
            }
            return list;
        }

        private boolean processOnePosition(ResourceKey<Level> resourceKey, SimpleRegionStorage simpleRegionStorage, ChunkPos chunkPos) {
            boolean bl = false;
            try {
                bl = this.tryProcessOnePosition(simpleRegionStorage, chunkPos, resourceKey);
            }
            catch (CompletionException | ReportedException runtimeException) {
                Throwable throwable = runtimeException.getCause();
                if (throwable instanceof IOException) {
                    LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)throwable);
                }
                throw runtimeException;
            }
            if (bl) {
                ++WorldUpgrader.this.converted;
            } else {
                ++WorldUpgrader.this.skipped;
            }
            return bl;
        }

        protected abstract boolean tryProcessOnePosition(SimpleRegionStorage var1, ChunkPos var2, ResourceKey<Level> var3);

        private void onFileFinished(RegionFile regionFile) {
            if (!WorldUpgrader.this.recreateRegionFiles) {
                return;
            }
            if (this.previousWriteFuture != null) {
                this.previousWriteFuture.join();
            }
            Path path = regionFile.getPath();
            Path path2 = path.getParent();
            Path path3 = WorldUpgrader.resolveRecreateDirectory(path2).resolve(path.getFileName().toString());
            try {
                if (path3.toFile().exists()) {
                    Files.delete(path);
                    Files.move(path3, path, new CopyOption[0]);
                } else {
                    LOGGER.error("Failed to replace an old region file. New file {} does not exist.", (Object)path3);
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to replace an old region file", (Throwable)iOException);
            }
        }
    }

    static final class FileToUpgrade
    extends Record {
        final RegionFile file;
        final List<ChunkPos> chunksToUpgrade;

        FileToUpgrade(RegionFile regionFile, List<ChunkPos> list) {
            this.file = regionFile;
            this.chunksToUpgrade = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FileToUpgrade.class, "file;chunksToUpgrade", "file", "chunksToUpgrade"}, this, object);
        }

        public RegionFile file() {
            return this.file;
        }

        public List<ChunkPos> chunksToUpgrade() {
            return this.chunksToUpgrade;
        }
    }

    static final class DimensionToUpgrade
    extends Record {
        final ResourceKey<Level> dimensionKey;
        final SimpleRegionStorage storage;
        final ListIterator<FileToUpgrade> files;

        DimensionToUpgrade(ResourceKey<Level> resourceKey, SimpleRegionStorage simpleRegionStorage, ListIterator<FileToUpgrade> listIterator) {
            this.dimensionKey = resourceKey;
            this.storage = simpleRegionStorage;
            this.files = listIterator;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DimensionToUpgrade.class, "dimensionKey;storage;files", "dimensionKey", "storage", "files"}, this, object);
        }

        public ResourceKey<Level> dimensionKey() {
            return this.dimensionKey;
        }

        public SimpleRegionStorage storage() {
            return this.storage;
        }

        public ListIterator<FileToUpgrade> files() {
            return this.files;
        }
    }
}

