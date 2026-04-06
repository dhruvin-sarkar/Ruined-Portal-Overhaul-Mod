/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jspecify.annotations.Nullable;

public class CommandStorage {
    private static final String ID_PREFIX = "command_storage_";
    private final Map<String, Container> namespaces = new HashMap<String, Container>();
    private final DimensionDataStorage storage;

    public CommandStorage(DimensionDataStorage dimensionDataStorage) {
        this.storage = dimensionDataStorage;
    }

    public CompoundTag get(Identifier identifier) {
        Container container = this.getContainer(identifier.getNamespace());
        if (container != null) {
            return container.get(identifier.getPath());
        }
        return new CompoundTag();
    }

    private @Nullable Container getContainer(String string) {
        Container container = this.namespaces.get(string);
        if (container != null) {
            return container;
        }
        Container container2 = this.storage.get(Container.type(string));
        if (container2 != null) {
            this.namespaces.put(string, container2);
        }
        return container2;
    }

    private Container getOrCreateContainer(String string) {
        Container container = this.namespaces.get(string);
        if (container != null) {
            return container;
        }
        Container container2 = this.storage.computeIfAbsent(Container.type(string));
        this.namespaces.put(string, container2);
        return container2;
    }

    public void set(Identifier identifier, CompoundTag compoundTag) {
        this.getOrCreateContainer(identifier.getNamespace()).put(identifier.getPath(), compoundTag);
    }

    public Stream<Identifier> keys() {
        return this.namespaces.entrySet().stream().flatMap(entry -> ((Container)entry.getValue()).getKeys((String)entry.getKey()));
    }

    static String createId(String string) {
        return ID_PREFIX + string;
    }

    static class Container
    extends SavedData {
        public static final Codec<Container> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.unboundedMap(ExtraCodecs.RESOURCE_PATH_CODEC, CompoundTag.CODEC).fieldOf("contents").forGetter(container -> container.storage)).apply((Applicative)instance, Container::new));
        private final Map<String, CompoundTag> storage;

        private Container(Map<String, CompoundTag> map) {
            this.storage = new HashMap<String, CompoundTag>(map);
        }

        private Container() {
            this(new HashMap<String, CompoundTag>());
        }

        public static SavedDataType<Container> type(String string) {
            return new SavedDataType<Container>(CommandStorage.createId(string), Container::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        }

        public CompoundTag get(String string) {
            CompoundTag compoundTag = this.storage.get(string);
            return compoundTag != null ? compoundTag : new CompoundTag();
        }

        public void put(String string, CompoundTag compoundTag) {
            if (compoundTag.isEmpty()) {
                this.storage.remove(string);
            } else {
                this.storage.put(string, compoundTag);
            }
            this.setDirty();
        }

        public Stream<Identifier> getKeys(String string) {
            return this.storage.keySet().stream().map(string2 -> Identifier.fromNamespaceAndPath(string, string2));
        }
    }
}

