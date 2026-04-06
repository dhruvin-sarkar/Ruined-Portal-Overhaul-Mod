/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<PackEntry> fallbacks = Lists.newArrayList();
    private final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType packType, String string) {
        this.type = packType;
        this.namespace = string;
    }

    public void push(PackResources packResources) {
        this.pushInternal(packResources.packId(), packResources, null);
    }

    public void push(PackResources packResources, Predicate<Identifier> predicate) {
        this.pushInternal(packResources.packId(), packResources, predicate);
    }

    public void pushFilterOnly(String string, Predicate<Identifier> predicate) {
        this.pushInternal(string, null, predicate);
    }

    private void pushInternal(String string, @Nullable PackResources packResources, @Nullable Predicate<Identifier> predicate) {
        this.fallbacks.add(new PackEntry(string, packResources, predicate));
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of((Object)this.namespace);
    }

    @Override
    public Optional<Resource> getResource(Identifier identifier) {
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, identifier)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = this.createStackMetadataFinder(identifier, i);
                return Optional.of(FallbackResourceManager.createResource(packResources, identifier, ioSupplier, ioSupplier2));
            }
            if (!packEntry.isFiltered(identifier)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)identifier, (Object)packEntry.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(PackResources packResources, Identifier identifier, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2) {
        return new Resource(packResources, FallbackResourceManager.wrapForDebug(identifier, packResources, ioSupplier), ioSupplier2);
    }

    private static IoSupplier<InputStream> wrapForDebug(Identifier identifier, PackResources packResources, IoSupplier<InputStream> ioSupplier) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new LeakedResourceWarningInputStream((InputStream)ioSupplier.get(), identifier, packResources.packId());
        }
        return ioSupplier;
    }

    @Override
    public List<Resource> getResourceStack(Identifier identifier) {
        Identifier identifier2 = FallbackResourceManager.getMetadataLocation(identifier);
        ArrayList<Resource> list = new ArrayList<Resource>();
        boolean bl = false;
        String string = null;
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, identifier)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = bl ? ResourceMetadata.EMPTY_SUPPLIER : () -> {
                    IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, identifier2);
                    return ioSupplier != null ? FallbackResourceManager.parseMetadata(ioSupplier) : ResourceMetadata.EMPTY;
                };
                list.add(new Resource(packResources, ioSupplier, ioSupplier2));
            }
            if (packEntry.isFiltered(identifier)) {
                string = packEntry.name;
                break;
            }
            if (!packEntry.isFiltered(identifier2)) continue;
            bl = true;
        }
        if (list.isEmpty() && string != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)identifier, string);
        }
        return Lists.reverse(list);
    }

    private static boolean isMetadata(Identifier identifier) {
        return identifier.getPath().endsWith(".mcmeta");
    }

    private static Identifier getIdentifierFromMetadata(Identifier identifier) {
        String string = identifier.getPath().substring(0, identifier.getPath().length() - ".mcmeta".length());
        return identifier.withPath(string);
    }

    static Identifier getMetadataLocation(Identifier identifier) {
        return identifier.withPath(identifier.getPath() + ".mcmeta");
    }

    @Override
    public Map<Identifier, Resource> listResources(String string, Predicate<Identifier> predicate) {
        final class ResourceWithSourceAndIndex
        extends Record {
            final PackResources packResources;
            final IoSupplier<InputStream> resource;
            final int packIndex;

            ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> ioSupplier, int i) {
                this.packResources = packResources;
                this.resource = ioSupplier;
                this.packIndex = i;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this, object);
            }

            public PackResources packResources() {
                return this.packResources;
            }

            public IoSupplier<InputStream> resource() {
                return this.resource;
            }

            public int packIndex() {
                return this.packIndex;
            }
        }
        HashMap<Identifier, ResourceWithSourceAndIndex> map = new HashMap<Identifier, ResourceWithSourceAndIndex>();
        HashMap map2 = new HashMap();
        int i = this.fallbacks.size();
        for (int j = 0; j < i; ++j) {
            PackEntry packEntry = this.fallbacks.get(j);
            packEntry.filterAll(map.keySet());
            packEntry.filterAll(map2.keySet());
            PackResources packResources = packEntry.resources;
            if (packResources == null) continue;
            int k = j;
            packResources.listResources(this.type, this.namespace, string, (identifier, ioSupplier) -> {
                if (FallbackResourceManager.isMetadata(identifier)) {
                    if (predicate.test(FallbackResourceManager.getIdentifierFromMetadata(identifier))) {
                        map2.put(identifier, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, k));
                    }
                } else if (predicate.test((Identifier)identifier)) {
                    map.put((Identifier)identifier, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, k));
                }
            });
        }
        TreeMap map3 = Maps.newTreeMap();
        map.forEach((identifier, arg) -> {
            Identifier identifier2 = FallbackResourceManager.getMetadataLocation(identifier);
            ResourceWithSourceAndIndex lv = (ResourceWithSourceAndIndex)((Object)((Object)map2.get(identifier2)));
            IoSupplier<ResourceMetadata> ioSupplier = lv != null && lv.packIndex >= arg.packIndex ? FallbackResourceManager.convertToMetadata(lv.resource) : ResourceMetadata.EMPTY_SUPPLIER;
            map3.put(identifier, FallbackResourceManager.createResource(arg.packResources, identifier, arg.resource, ioSupplier));
        });
        return map3;
    }

    private IoSupplier<ResourceMetadata> createStackMetadataFinder(Identifier identifier, int i) {
        return () -> {
            Identifier identifier2 = FallbackResourceManager.getMetadataLocation(identifier);
            for (int j = this.fallbacks.size() - 1; j >= i; --j) {
                IoSupplier<InputStream> ioSupplier;
                PackEntry packEntry = this.fallbacks.get(j);
                PackResources packResources = packEntry.resources;
                if (packResources != null && (ioSupplier = packResources.getResource(this.type, identifier2)) != null) {
                    return FallbackResourceManager.parseMetadata(ioSupplier);
                }
                if (packEntry.isFiltered(identifier2)) break;
            }
            return ResourceMetadata.EMPTY;
        };
    }

    private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> ioSupplier) {
        return () -> FallbackResourceManager.parseMetadata(ioSupplier);
    }

    private static ResourceMetadata parseMetadata(IoSupplier<InputStream> ioSupplier) throws IOException {
        try (InputStream inputStream = ioSupplier.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(inputStream);
            return resourceMetadata;
        }
    }

    private static void applyPackFiltersToExistingResources(PackEntry packEntry, Map<Identifier, EntryStack> map) {
        for (EntryStack entryStack : map.values()) {
            if (packEntry.isFiltered(entryStack.fileLocation)) {
                entryStack.fileSources.clear();
                continue;
            }
            if (!packEntry.isFiltered(entryStack.metadataLocation())) continue;
            entryStack.metaSources.clear();
        }
    }

    private void listPackResources(PackEntry packEntry, String string, Predicate<Identifier> predicate, Map<Identifier, EntryStack> map) {
        PackResources packResources = packEntry.resources;
        if (packResources == null) {
            return;
        }
        packResources.listResources(this.type, this.namespace, string, (identifier, ioSupplier) -> {
            if (FallbackResourceManager.isMetadata(identifier)) {
                Identifier identifier2 = FallbackResourceManager.getIdentifierFromMetadata(identifier);
                if (!predicate.test(identifier2)) {
                    return;
                }
                map.computeIfAbsent(identifier2, (Function<Identifier, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.Identifier ), (Lnet/minecraft/resources/Identifier;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).metaSources.put(packResources, (IoSupplier<InputStream>)ioSupplier);
            } else {
                if (!predicate.test((Identifier)identifier)) {
                    return;
                }
                map.computeIfAbsent(identifier, (Function<Identifier, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.Identifier ), (Lnet/minecraft/resources/Identifier;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).fileSources.add(new ResourceWithSource(packResources, (IoSupplier<InputStream>)ioSupplier));
            }
        });
    }

    @Override
    public Map<Identifier, List<Resource>> listResourceStacks(String string, Predicate<Identifier> predicate) {
        HashMap map = Maps.newHashMap();
        for (PackEntry packEntry : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources(packEntry, map);
            this.listPackResources(packEntry, string, predicate, map);
        }
        TreeMap treeMap = Maps.newTreeMap();
        for (EntryStack entryStack : map.values()) {
            if (entryStack.fileSources.isEmpty()) continue;
            ArrayList<Resource> list = new ArrayList<Resource>();
            for (ResourceWithSource resourceWithSource : entryStack.fileSources) {
                PackResources packResources = resourceWithSource.source;
                IoSupplier<InputStream> ioSupplier = entryStack.metaSources.get(packResources);
                IoSupplier<ResourceMetadata> ioSupplier2 = ioSupplier != null ? FallbackResourceManager.convertToMetadata(ioSupplier) : ResourceMetadata.EMPTY_SUPPLIER;
                list.add(FallbackResourceManager.createResource(packResources, entryStack.fileLocation, resourceWithSource.resource, ioSupplier2));
            }
            treeMap.put(entryStack.fileLocation, list);
        }
        return treeMap;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
    }

    static final class PackEntry
    extends Record {
        final String name;
        final @Nullable PackResources resources;
        private final @Nullable Predicate<Identifier> filter;

        PackEntry(String string, @Nullable PackResources packResources, @Nullable Predicate<Identifier> predicate) {
            this.name = string;
            this.resources = packResources;
            this.filter = predicate;
        }

        public void filterAll(Collection<Identifier> collection) {
            if (this.filter != null) {
                collection.removeIf(this.filter);
            }
        }

        public boolean isFiltered(Identifier identifier) {
            return this.filter != null && this.filter.test(identifier);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public @Nullable PackResources resources() {
            return this.resources;
        }

        public @Nullable Predicate<Identifier> filter() {
            return this.filter;
        }
    }

    static final class EntryStack
    extends Record {
        final Identifier fileLocation;
        private final Identifier metadataLocation;
        final List<ResourceWithSource> fileSources;
        final Map<PackResources, IoSupplier<InputStream>> metaSources;

        EntryStack(Identifier identifier) {
            this(identifier, FallbackResourceManager.getMetadataLocation(identifier), new ArrayList<ResourceWithSource>(), (Map<PackResources, IoSupplier<InputStream>>)new Object2ObjectArrayMap());
        }

        private EntryStack(Identifier identifier, Identifier identifier2, List<ResourceWithSource> list, Map<PackResources, IoSupplier<InputStream>> map) {
            this.fileLocation = identifier;
            this.metadataLocation = identifier2;
            this.fileSources = list;
            this.metaSources = map;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this, object);
        }

        public Identifier fileLocation() {
            return this.fileLocation;
        }

        public Identifier metadataLocation() {
            return this.metadataLocation;
        }

        public List<ResourceWithSource> fileSources() {
            return this.fileSources;
        }

        public Map<PackResources, IoSupplier<InputStream>> metaSources() {
            return this.metaSources;
        }
    }

    static final class ResourceWithSource
    extends Record {
        final PackResources source;
        final IoSupplier<InputStream> resource;

        ResourceWithSource(PackResources packResources, IoSupplier<InputStream> ioSupplier) {
            this.source = packResources;
            this.resource = ioSupplier;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this, object);
        }

        public PackResources source() {
            return this.source;
        }

        public IoSupplier<InputStream> resource() {
            return this.resource;
        }
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final Supplier<String> message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream inputStream, Identifier identifier, String string) {
            super(inputStream);
            Exception exception = new Exception("Stacktrace");
            this.message = () -> {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                return "Leaked resource: '" + String.valueOf(identifier) + "' loaded from pack: '" + string + "'\n" + String.valueOf(stringWriter);
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.message.get());
            }
            super.finalize();
        }
    }
}

