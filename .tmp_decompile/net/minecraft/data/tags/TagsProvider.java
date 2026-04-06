/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;

public abstract class TagsProvider<T>
implements DataProvider {
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final CompletableFuture<Void> contentsDone = new CompletableFuture();
    private final CompletableFuture<TagLookup<T>> parentProvider;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    private final Map<Identifier, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this(packOutput, resourceKey, completableFuture, CompletableFuture.completedFuture(TagLookup.empty()));
    }

    protected TagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<T>> completableFuture2) {
        this.pathProvider = packOutput.createRegistryTagsPathProvider(resourceKey);
        this.registryKey = resourceKey;
        this.parentProvider = completableFuture2;
        this.lookupProvider = completableFuture;
    }

    @Override
    public final String getName() {
        return "Tags for " + String.valueOf(this.registryKey.identifier());
    }

    protected abstract void addTags(HolderLookup.Provider var1);

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        final class CombinedData<T>
        extends Record {
            final HolderLookup.Provider contents;
            final TagLookup<T> parent;

            CombinedData(HolderLookup.Provider provider, TagLookup<T> tagLookup) {
                this.contents = provider;
                this.parent = tagLookup;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this, object);
            }

            public HolderLookup.Provider contents() {
                return this.contents;
            }

            public TagLookup<T> parent() {
                return this.parent;
            }
        }
        return ((CompletableFuture)((CompletableFuture)this.createContentsProvider().thenApply(provider -> {
            this.contentsDone.complete(null);
            return provider;
        })).thenCombineAsync(this.parentProvider, (provider, tagLookup) -> new CombinedData((HolderLookup.Provider)provider, tagLookup), (Executor)Util.backgroundExecutor())).thenCompose(arg -> {
            HolderGetter registryLookup = arg.contents.lookupOrThrow(this.registryKey);
            Predicate<Identifier> predicate = arg_0 -> this.method_46832((HolderLookup.RegistryLookup)registryLookup, arg_0);
            Predicate<Identifier> predicate2 = identifier -> this.builders.containsKey(identifier) || arg.parent.contains(TagKey.create(this.registryKey, identifier));
            return CompletableFuture.allOf((CompletableFuture[])this.builders.entrySet().stream().map(entry -> {
                Identifier identifier = (Identifier)entry.getKey();
                TagBuilder tagBuilder = (TagBuilder)entry.getValue();
                List<TagEntry> list = tagBuilder.build();
                List list2 = list.stream().filter(tagEntry -> !tagEntry.verifyIfPresent(predicate, predicate2)).toList();
                if (!list2.isEmpty()) {
                    throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", identifier, list2.stream().map(Objects::toString).collect(Collectors.joining(","))));
                }
                Path path = this.pathProvider.json(identifier);
                return DataProvider.saveStable(cachedOutput, arg.contents, TagFile.CODEC, new TagFile(list, false), path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> tagKey) {
        return this.builders.computeIfAbsent(tagKey.location(), identifier -> TagBuilder.create());
    }

    public CompletableFuture<TagLookup<T>> contentsGetter() {
        return this.contentsDone.thenApply(void_ -> tagKey -> Optional.ofNullable(this.builders.get(tagKey.location())));
    }

    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return this.lookupProvider.thenApply(provider -> {
            this.builders.clear();
            this.addTags((HolderLookup.Provider)provider);
            return provider;
        });
    }

    private /* synthetic */ boolean method_46832(HolderLookup.RegistryLookup registryLookup, Identifier identifier) {
        return registryLookup.get(ResourceKey.create(this.registryKey, identifier)).isPresent();
    }

    @FunctionalInterface
    public static interface TagLookup<T>
    extends Function<TagKey<T>, Optional<TagBuilder>> {
        public static <T> TagLookup<T> empty() {
            return tagKey -> Optional.empty();
        }

        default public boolean contains(TagKey<T> tagKey) {
            return ((Optional)this.apply(tagKey)).isPresent();
        }
    }
}

