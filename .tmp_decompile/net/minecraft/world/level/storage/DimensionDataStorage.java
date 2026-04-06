/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DimensionDataStorage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<SavedDataType<?>, Optional<SavedData>> cache = new HashMap();
    private final DataFixer fixerUpper;
    private final HolderLookup.Provider registries;
    private final Path dataFolder;
    private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture(null);

    public DimensionDataStorage(Path path, DataFixer dataFixer, HolderLookup.Provider provider) {
        this.fixerUpper = dataFixer;
        this.dataFolder = path;
        this.registries = provider;
    }

    private Path getDataFile(String string) {
        return this.dataFolder.resolve(string + ".dat");
    }

    public <T extends SavedData> T computeIfAbsent(SavedDataType<T> savedDataType) {
        T savedData = this.get(savedDataType);
        if (savedData != null) {
            return savedData;
        }
        SavedData savedData2 = (SavedData)savedDataType.constructor().get();
        this.set(savedDataType, savedData2);
        return (T)savedData2;
    }

    public <T extends SavedData> @Nullable T get(SavedDataType<T> savedDataType) {
        Optional<SavedData> optional = this.cache.get(savedDataType);
        if (optional == null) {
            optional = Optional.ofNullable(this.readSavedData(savedDataType));
            this.cache.put(savedDataType, optional);
        }
        return (T)((SavedData)optional.orElse(null));
    }

    private <T extends SavedData> @Nullable T readSavedData(SavedDataType<T> savedDataType) {
        try {
            Path path = this.getDataFile(savedDataType.id());
            if (Files.exists(path, new LinkOption[0])) {
                CompoundTag compoundTag = this.readTagFromDisk(savedDataType.id(), savedDataType.dataFixType(), SharedConstants.getCurrentVersion().dataVersion().version());
                RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
                return (T)((SavedData)savedDataType.codec().parse(registryOps, (Object)compoundTag.get("data")).resultOrPartial(string -> LOGGER.error("Failed to parse saved data for '{}': {}", (Object)savedDataType, string)).orElse(null));
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", savedDataType, (Object)exception);
        }
        return null;
    }

    public <T extends SavedData> void set(SavedDataType<T> savedDataType, T savedData) {
        this.cache.put(savedDataType, Optional.of(savedData));
        savedData.setDirty();
    }

    public CompoundTag readTagFromDisk(String string, DataFixTypes dataFixTypes, int i) throws IOException {
        try (InputStream inputStream = Files.newInputStream(this.getDataFile(string), new OpenOption[0]);){
            CompoundTag compoundTag;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(new FastBufferedInputStream(inputStream), 2);){
                CompoundTag compoundTag2;
                if (this.isGzip(pushbackInputStream)) {
                    compoundTag2 = NbtIo.readCompressed(pushbackInputStream, NbtAccounter.unlimitedHeap());
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        compoundTag2 = NbtIo.read(dataInputStream);
                    }
                }
                int j = NbtUtils.getDataVersion(compoundTag2, 1343);
                compoundTag = dataFixTypes.update(this.fixerUpper, compoundTag2, j, i);
            }
            return compoundTag;
        }
    }

    private boolean isGzip(PushbackInputStream pushbackInputStream) throws IOException {
        int j;
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = pushbackInputStream.read(bs, 0, 2);
        if (i == 2 && (j = (bs[1] & 0xFF) << 8 | bs[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (i != 0) {
            pushbackInputStream.unread(bs, 0, i);
        }
        return bl;
    }

    public CompletableFuture<?> scheduleSave() {
        Map<SavedDataType<?>, CompoundTag> map = this.collectDirtyTagsToSave();
        if (map.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        int i = Util.maxAllowedExecutorThreads();
        int j = map.size();
        this.pendingWriteFuture = j > i ? this.pendingWriteFuture.thenCompose(object -> {
            ArrayList list = new ArrayList(i);
            int k = Mth.positiveCeilDiv(j, i);
            for (List list2 : Iterables.partition(map.entrySet(), (int)k)) {
                list.add(CompletableFuture.runAsync(() -> {
                    for (Map.Entry entry : list2) {
                        this.tryWrite((SavedDataType)((Object)((Object)((Object)entry.getKey()))), (CompoundTag)entry.getValue());
                    }
                }, Util.ioPool()));
            }
            return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
        }) : this.pendingWriteFuture.thenCompose(object -> CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map(entry -> CompletableFuture.runAsync(() -> this.tryWrite((SavedDataType)((Object)((Object)((Object)((Object)entry.getKey())))), (CompoundTag)entry.getValue()), Util.ioPool())).toArray(CompletableFuture[]::new)));
        return this.pendingWriteFuture;
    }

    private Map<SavedDataType<?>, CompoundTag> collectDirtyTagsToSave() {
        Object2ObjectArrayMap map = new Object2ObjectArrayMap();
        RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
        this.cache.forEach((arg_0, arg_1) -> this.method_67444((Map)map, registryOps, arg_0, arg_1));
        return map;
    }

    private <T extends SavedData> CompoundTag encodeUnchecked(SavedDataType<T> savedDataType, SavedData savedData, RegistryOps<Tag> registryOps) {
        Codec<T> codec = savedDataType.codec();
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", (Tag)codec.encodeStart(registryOps, (Object)savedData).getOrThrow());
        NbtUtils.addCurrentDataVersion(compoundTag);
        return compoundTag;
    }

    private void tryWrite(SavedDataType<?> savedDataType, CompoundTag compoundTag) {
        Path path = this.getDataFile(savedDataType.id());
        try {
            NbtIo.writeCompressed(compoundTag, path);
        }
        catch (IOException iOException) {
            LOGGER.error("Could not save data to {}", (Object)path.getFileName(), (Object)iOException);
        }
    }

    public void saveAndJoin() {
        this.scheduleSave().join();
    }

    @Override
    public void close() {
        this.saveAndJoin();
    }

    private /* synthetic */ void method_67444(Map map, RegistryOps registryOps, SavedDataType savedDataType, Optional optional) {
        optional.filter(SavedData::isDirty).ifPresent(savedData -> {
            map.put(savedDataType, this.encodeUnchecked(savedDataType, (SavedData)savedData, registryOps));
            savedData.setDirty(false);
        });
    }
}

