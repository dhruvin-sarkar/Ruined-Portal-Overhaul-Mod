/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.StrictJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ElementLookup<T> elementLookup;
    private final String directory;

    public TagLoader(ElementLookup<T> elementLookup, String string) {
        this.elementLookup = elementLookup;
        this.directory = string;
    }

    public Map<Identifier, List<EntryWithSource>> load(ResourceManager resourceManager) {
        HashMap<Identifier, List<EntryWithSource>> map = new HashMap<Identifier, List<EntryWithSource>>();
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(this.directory);
        for (Map.Entry<Identifier, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            Identifier identifier2 = entry.getKey();
            Identifier identifier22 = fileToIdConverter.fileToId(identifier2);
            for (Resource resource : entry.getValue()) {
                try {
                    BufferedReader reader = resource.openAsReader();
                    try {
                        JsonElement jsonElement = StrictJsonParser.parse(reader);
                        List list = map.computeIfAbsent(identifier22, identifier -> new ArrayList());
                        TagFile tagFile = (TagFile)((Object)TagFile.CODEC.parse(new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement)).getOrThrow());
                        if (tagFile.replace()) {
                            list.clear();
                        }
                        String string = resource.sourcePackId();
                        tagFile.entries().forEach(tagEntry -> list.add(new EntryWithSource((TagEntry)tagEntry, string)));
                    }
                    finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                }
                catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{identifier22, identifier2, resource.sourcePackId(), exception});
                }
            }
        }
        return map;
    }

    private Either<List<EntryWithSource>, List<T>> tryBuildTag(TagEntry.Lookup<T> lookup, List<EntryWithSource> list) {
        LinkedHashSet sequencedSet = new LinkedHashSet();
        ArrayList<EntryWithSource> list2 = new ArrayList<EntryWithSource>();
        for (EntryWithSource entryWithSource : list) {
            if (entryWithSource.entry().build(lookup, sequencedSet::add)) continue;
            list2.add(entryWithSource);
        }
        return list2.isEmpty() ? Either.right((Object)List.copyOf(sequencedSet)) : Either.left(list2);
    }

    public Map<Identifier, List<T>> build(Map<Identifier, List<EntryWithSource>> map) {
        final HashMap map2 = new HashMap();
        TagEntry.Lookup lookup = new TagEntry.Lookup<T>(){

            @Override
            public @Nullable T element(Identifier identifier, boolean bl) {
                return TagLoader.this.elementLookup.get(identifier, bl).orElse(null);
            }

            @Override
            public @Nullable Collection<T> tag(Identifier identifier) {
                return (Collection)map2.get(identifier);
            }
        };
        DependencySorter<Identifier, SortingEntry> dependencySorter = new DependencySorter<Identifier, SortingEntry>();
        map.forEach((identifier, list) -> dependencySorter.addEntry((Identifier)identifier, new SortingEntry((List<EntryWithSource>)list)));
        dependencySorter.orderByDependencies((identifier, sortingEntry) -> this.tryBuildTag(lookup, sortingEntry.entries).ifLeft(list -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", identifier, (Object)list.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(list -> map2.put((Identifier)identifier, (List)list)));
        return map2;
    }

    public static <T> void loadTagsFromNetwork(TagNetworkSerialization.NetworkPayload networkPayload, WritableRegistry<T> writableRegistry) {
        networkPayload.resolve(writableRegistry).tags.forEach(writableRegistry::bindTag);
    }

    public static List<Registry.PendingTags<?>> loadTagsForExistingRegistries(ResourceManager resourceManager, RegistryAccess registryAccess) {
        return (List)registryAccess.registries().map(registryEntry -> TagLoader.loadPendingTags(resourceManager, registryEntry.value())).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).collect(Collectors.toUnmodifiableList());
    }

    public static <T> void loadTagsForRegistry(ResourceManager resourceManager, WritableRegistry<T> writableRegistry) {
        ResourceKey resourceKey = writableRegistry.key();
        TagLoader<Holder<T>> tagLoader = new TagLoader<Holder<T>>(ElementLookup.fromWritableRegistry(writableRegistry), Registries.tagsDirPath(resourceKey));
        tagLoader.build(tagLoader.load(resourceManager)).forEach((identifier, list) -> writableRegistry.bindTag(TagKey.create(resourceKey, identifier), (List)list));
    }

    private static <T> Map<TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends Registry<T>> resourceKey, Map<Identifier, List<Holder<T>>> map) {
        return (Map)map.entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> TagKey.create(resourceKey, (Identifier)entry.getKey()), Map.Entry::getValue));
    }

    private static <T> Optional<Registry.PendingTags<T>> loadPendingTags(ResourceManager resourceManager, Registry<T> registry) {
        ResourceKey<Registry<T>> resourceKey = registry.key();
        TagLoader<Holder<T>> tagLoader = new TagLoader<Holder<T>>(ElementLookup.fromFrozenRegistry(registry), Registries.tagsDirPath(resourceKey));
        LoadResult<T> loadResult = new LoadResult<T>(resourceKey, TagLoader.wrapTags(registry.key(), tagLoader.build(tagLoader.load(resourceManager))));
        return loadResult.tags().isEmpty() ? Optional.empty() : Optional.of(registry.prepareTagReload(loadResult));
    }

    public static List<HolderLookup.RegistryLookup<?>> buildUpdatedLookups(RegistryAccess.Frozen frozen, List<Registry.PendingTags<?>> list) {
        ArrayList list2 = new ArrayList();
        frozen.registries().forEach(registryEntry -> {
            Registry.PendingTags pendingTags = TagLoader.findTagsForRegistry(list, registryEntry.key());
            list2.add(pendingTags != null ? pendingTags.lookup() : registryEntry.value());
        });
        return list2;
    }

    private static @Nullable Registry.PendingTags<?> findTagsForRegistry(List<Registry.PendingTags<?>> list, ResourceKey<? extends Registry<?>> resourceKey) {
        for (Registry.PendingTags<?> pendingTags : list) {
            if (pendingTags.key() != resourceKey) continue;
            return pendingTags;
        }
        return null;
    }

    public static interface ElementLookup<T> {
        public Optional<? extends T> get(Identifier var1, boolean var2);

        public static <T> ElementLookup<? extends Holder<T>> fromFrozenRegistry(Registry<T> registry) {
            return (identifier, bl) -> registry.get(identifier);
        }

        public static <T> ElementLookup<Holder<T>> fromWritableRegistry(WritableRegistry<T> writableRegistry) {
            HolderGetter holderGetter = writableRegistry.createRegistrationLookup();
            return (identifier, bl) -> (bl ? holderGetter : writableRegistry).get(ResourceKey.create(writableRegistry.key(), identifier));
        }
    }

    public static final class EntryWithSource
    extends Record {
        final TagEntry entry;
        private final String source;

        public EntryWithSource(TagEntry tagEntry, String string) {
            this.entry = tagEntry;
            this.source = string;
        }

        public String toString() {
            return String.valueOf(this.entry) + " (from " + this.source + ")";
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this, object);
        }

        public TagEntry entry() {
            return this.entry;
        }

        public String source() {
            return this.source;
        }
    }

    public static final class LoadResult<T>
    extends Record {
        private final ResourceKey<? extends Registry<T>> key;
        final Map<TagKey<T>, List<Holder<T>>> tags;

        public LoadResult(ResourceKey<? extends Registry<T>> resourceKey, Map<TagKey<T>, List<Holder<T>>> map) {
            this.key = resourceKey;
            this.tags = map;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this, object);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Map<TagKey<T>, List<Holder<T>>> tags() {
            return this.tags;
        }
    }

    static final class SortingEntry
    extends Record
    implements DependencySorter.Entry<Identifier> {
        final List<EntryWithSource> entries;

        SortingEntry(List<EntryWithSource> list) {
            this.entries = list;
        }

        @Override
        public void visitRequiredDependencies(Consumer<Identifier> consumer) {
            this.entries.forEach(entryWithSource -> entryWithSource.entry.visitRequiredDependencies(consumer));
        }

        @Override
        public void visitOptionalDependencies(Consumer<Identifier> consumer) {
            this.entries.forEach(entryWithSource -> entryWithSource.entry.visitOptionalDependencies(consumer));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this, object);
        }

        public List<EntryWithSource> entries() {
            return this.entries;
        }
    }
}

