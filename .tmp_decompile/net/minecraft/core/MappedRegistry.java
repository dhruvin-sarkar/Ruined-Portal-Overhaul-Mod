/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MappedRegistry<T>
implements WritableRegistry<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList(256);
    private final Reference2IntMap<T> toId = (Reference2IntMap)Util.make(new Reference2IntOpenHashMap(), reference2IntOpenHashMap -> reference2IntOpenHashMap.defaultReturnValue(-1));
    private final Map<Identifier, Holder.Reference<T>> byLocation = new HashMap<Identifier, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<ResourceKey<T>, Holder.Reference<T>>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<T, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap<ResourceKey<T>, RegistrationInfo>();
    private Lifecycle registryLifecycle;
    private final Map<TagKey<T>, HolderSet.Named<T>> frozenTags = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>();
    TagSet<T> allTags = TagSet.unbound();
    private boolean frozen;
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.getTags();
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
        this(resourceKey, lifecycle, false);
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
        this.key = resourceKey;
        this.registryLifecycle = lifecycle;
        if (bl) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<T, Holder.Reference<T>>();
        }
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    public String toString() {
        return "Registry[" + String.valueOf(this.key) + " (" + String.valueOf(this.registryLifecycle) + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> resourceKey) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf(resourceKey) + ")");
        }
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> resourceKey2, T object, RegistrationInfo registrationInfo) {
        Holder.Reference reference;
        this.validateWrite(resourceKey2);
        Objects.requireNonNull(resourceKey2);
        Objects.requireNonNull(object);
        if (this.byLocation.containsKey(resourceKey2.identifier())) {
            throw Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + String.valueOf(resourceKey2) + "' to registry"));
        }
        if (this.byValue.containsKey(object)) {
            throw Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + String.valueOf(object) + "' to registry"));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            reference = this.unregisteredIntrusiveHolders.remove(object);
            if (reference == null) {
                throw new AssertionError((Object)("Missing intrusive holder for " + String.valueOf(resourceKey2) + ":" + String.valueOf(object)));
            }
            reference.bindKey(resourceKey2);
        } else {
            reference = this.byKey.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this, resourceKey));
        }
        this.byKey.put(resourceKey2, reference);
        this.byLocation.put(resourceKey2.identifier(), reference);
        this.byValue.put(object, reference);
        int i = this.byId.size();
        this.byId.add((Object)reference);
        this.toId.put(object, i);
        this.registrationInfos.put(resourceKey2, registrationInfo);
        this.registryLifecycle = this.registryLifecycle.add(registrationInfo.lifecycle());
        return reference;
    }

    @Override
    public @Nullable Identifier getKey(T object) {
        Holder.Reference<T> reference = this.byValue.get(object);
        return reference != null ? reference.key().identifier() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T object) {
        return Optional.ofNullable(this.byValue.get(object)).map(Holder.Reference::key);
    }

    @Override
    public int getId(@Nullable T object) {
        return this.toId.getInt(object);
    }

    @Override
    public @Nullable T getValue(@Nullable ResourceKey<T> resourceKey) {
        return MappedRegistry.getValueFromNullable(this.byKey.get(resourceKey));
    }

    @Override
    public @Nullable T byId(int i) {
        if (i < 0 || i >= this.byId.size()) {
            return null;
        }
        return ((Holder.Reference)this.byId.get(i)).value();
    }

    @Override
    public Optional<Holder.Reference<T>> get(int i) {
        if (i < 0 || i >= this.byId.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable((Holder.Reference)this.byId.get(i));
    }

    @Override
    public Optional<Holder.Reference<T>> get(Identifier identifier) {
        return Optional.ofNullable(this.byLocation.get(identifier));
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.byKey.get(resourceKey));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return this.byId.isEmpty() ? Optional.empty() : Optional.of((Holder.Reference)this.byId.getFirst());
    }

    @Override
    public Holder<T> wrapAsHolder(T object) {
        Holder.Reference<T> reference = this.byValue.get(object);
        return reference != null ? reference : Holder.direct(object);
    }

    Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> resourceKey2) {
        return this.byKey.computeIfAbsent(resourceKey2, resourceKey -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.validateWrite((ResourceKey<T>)resourceKey);
            return Holder.Reference.createStandAlone(this, resourceKey);
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.registrationInfos.get(resourceKey));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform((Iterator)this.byId.iterator(), Holder::value);
    }

    @Override
    public @Nullable T getValue(@Nullable Identifier identifier) {
        Holder.Reference<T> reference = this.byLocation.get(identifier);
        return MappedRegistry.getValueFromNullable(reference);
    }

    private static <T> @Nullable T getValueFromNullable(@Nullable Holder.Reference<T> reference) {
        return reference != null ? (T)reference.value() : null;
    }

    @Override
    public Set<Identifier> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Util.mapValuesLazy(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.byId.stream();
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return this.allTags.getTags();
    }

    HolderSet.Named<T> getOrCreateTagForRegistration(TagKey<T> tagKey) {
        return this.frozenTags.computeIfAbsent(tagKey, this::createTag);
    }

    private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
        return new HolderSet.Named<T>(this, tagKey);
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
        return Util.getRandomSafe(this.byId, randomSource);
    }

    @Override
    public boolean containsKey(Identifier identifier) {
        return this.byLocation.containsKey(identifier);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourceKey) {
        return this.byKey.containsKey(resourceKey);
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        }
        this.frozen = true;
        this.byValue.forEach((? super K object, ? super V reference) -> reference.bindValue(object));
        List list = this.byKey.entrySet().stream().filter(entry -> !((Holder.Reference)entry.getValue()).isBound()).map(entry -> ((ResourceKey)entry.getKey()).identifier()).sorted().toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + String.valueOf(this.key()) + ": " + String.valueOf(list));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.unregisteredIntrusiveHolders.values()));
            }
            this.unregisteredIntrusiveHolders = null;
        }
        if (this.allTags.isBound()) {
            throw new IllegalStateException("Tags already present before freezing");
        }
        List list2 = this.frozenTags.entrySet().stream().filter(entry -> !((HolderSet.Named)entry.getValue()).isBound()).map(entry -> ((TagKey)((Object)((Object)entry.getKey()))).location()).sorted().toList();
        if (!list2.isEmpty()) {
            throw new IllegalStateException("Unbound tags in registry " + String.valueOf(this.key()) + ": " + String.valueOf(list2));
        }
        this.allTags = TagSet.fromMap(this.frozenTags);
        this.refreshTagsInHolders();
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T object2) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        this.validateWrite();
        return this.unregisteredIntrusiveHolders.computeIfAbsent(object2, object -> Holder.Reference.createIntrusive(this, object));
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
        return this.allTags.get(tagKey);
    }

    private Holder.Reference<T> validateAndUnwrapTagElement(TagKey<T> tagKey, Holder<T> holder) {
        if (!holder.canSerializeIn(this)) {
            throw new IllegalStateException("Can't create named set " + String.valueOf(tagKey) + " containing value " + String.valueOf(holder) + " from outside registry " + String.valueOf(this));
        }
        if (holder instanceof Holder.Reference) {
            Holder.Reference reference = (Holder.Reference)holder;
            return reference;
        }
        throw new IllegalStateException("Found direct holder " + String.valueOf(holder) + " value in tag " + String.valueOf(tagKey));
    }

    @Override
    public void bindTag(TagKey<T> tagKey, List<Holder<T>> list) {
        this.validateWrite();
        this.getOrCreateTagForRegistration(tagKey).bind(list);
    }

    void refreshTagsInHolders() {
        IdentityHashMap<Holder.Reference, List> map = new IdentityHashMap<Holder.Reference, List>();
        this.byKey.values().forEach(reference -> map.put((Holder.Reference)reference, new ArrayList()));
        this.allTags.forEach((? super TagKey<T> tagKey, ? super HolderSet.Named<T> named) -> {
            for (Holder holder : named) {
                Holder.Reference reference = this.validateAndUnwrapTagElement((TagKey<T>)((Object)tagKey), holder);
                ((List)map.get(reference)).add(tagKey);
            }
        });
        map.forEach(Holder.Reference::bindTags);
    }

    public void bindAllTagsToEmpty() {
        this.validateWrite();
        this.frozenTags.values().forEach(named -> named.bind(List.of()));
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>(){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                return Optional.of(this.getOrThrow(resourceKey));
            }

            @Override
            public Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
                return MappedRegistry.this.getOrCreateHolderOrThrow(resourceKey);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                return Optional.of(this.getOrThrow(tagKey));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
                return MappedRegistry.this.getOrCreateTagForRegistration(tagKey);
            }
        };
    }

    @Override
    public Registry.PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> loadResult) {
        if (!this.frozen) {
            throw new IllegalStateException("Invalid method used for tag loading");
        }
        ImmutableMap.Builder builder = ImmutableMap.builder();
        final HashMap map = new HashMap();
        loadResult.tags().forEach((? super K tagKey, ? super V list) -> {
            HolderSet.Named<T> named = this.frozenTags.get(tagKey);
            if (named == null) {
                named = this.createTag((TagKey<T>)((Object)tagKey));
            }
            builder.put((Object)tagKey, named);
            map.put(tagKey, List.copyOf((Collection)list));
        });
        final ImmutableMap immutableMap = builder.build();
        final HolderLookup.RegistryLookup.Delegate registryLookup = new HolderLookup.RegistryLookup.Delegate<T>(){

            @Override
            public HolderLookup.RegistryLookup<T> parent() {
                return MappedRegistry.this;
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                return Optional.ofNullable((HolderSet.Named)immutableMap.get(tagKey));
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return immutableMap.values().stream();
            }
        };
        return new Registry.PendingTags<T>(){

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return MappedRegistry.this.key();
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public HolderLookup.RegistryLookup<T> lookup() {
                return registryLookup;
            }

            @Override
            public void apply() {
                immutableMap.forEach((tagKey, named) -> {
                    List list = map.getOrDefault(tagKey, List.of());
                    named.bind(list);
                });
                MappedRegistry.this.allTags = TagSet.fromMap(immutableMap);
                MappedRegistry.this.refreshTagsInHolders();
            }
        };
    }

    static interface TagSet<T> {
        public static <T> TagSet<T> unbound() {
            return new TagSet<T>(){

                @Override
                public boolean isBound() {
                    return false;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                    throw new IllegalStateException("Tags not bound, trying to access " + String.valueOf(tagKey));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biConsumer) {
                    throw new IllegalStateException("Tags not bound");
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    throw new IllegalStateException("Tags not bound");
                }
            };
        }

        public static <T> TagSet<T> fromMap(final Map<TagKey<T>, HolderSet.Named<T>> map) {
            return new TagSet<T>(){

                @Override
                public boolean isBound() {
                    return true;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                    return Optional.ofNullable((HolderSet.Named)map.get((Object)tagKey));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biConsumer) {
                    map.forEach(biConsumer);
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    return map.values().stream();
                }
            };
        }

        public boolean isBound();

        public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

        public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> var1);

        public Stream<HolderSet.Named<T>> getTags();
    }
}
