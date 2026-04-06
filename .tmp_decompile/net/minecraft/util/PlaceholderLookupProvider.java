/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RegistryContextSwapper;

public class PlaceholderLookupProvider
implements HolderGetter.Provider {
    final HolderLookup.Provider context;
    final UniversalLookup lookup = new UniversalLookup();
    final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<ResourceKey<Object>, Holder.Reference<Object>>();
    final Map<TagKey<Object>, HolderSet.Named<Object>> holderSets = new HashMap<TagKey<Object>, HolderSet.Named<Object>>();

    public PlaceholderLookupProvider(HolderLookup.Provider provider) {
        this.context = provider;
    }

    @Override
    public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        return Optional.of(this.lookup.castAsLookup());
    }

    public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
        return RegistryOps.create(dynamicOps, new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return PlaceholderLookupProvider.this.context.lookup(resourceKey).map(RegistryOps.RegistryInfo::fromRegistryLookup).or(() -> Optional.of(new RegistryOps.RegistryInfo(PlaceholderLookupProvider.this.lookup.castAsOwner(), PlaceholderLookupProvider.this.lookup.castAsLookup(), Lifecycle.experimental())));
            }
        });
    }

    public RegistryContextSwapper createSwapper() {
        return new RegistryContextSwapper(){

            @Override
            public <T> DataResult<T> swapTo(Codec<T> codec, T object2, HolderLookup.Provider provider) {
                return codec.encodeStart(PlaceholderLookupProvider.this.createSerializationContext(JavaOps.INSTANCE), object2).flatMap(object -> codec.parse(provider.createSerializationContext(JavaOps.INSTANCE), object));
            }
        };
    }

    public boolean hasRegisteredPlaceholders() {
        return !this.holders.isEmpty() || !this.holderSets.isEmpty();
    }

    class UniversalLookup
    implements HolderGetter<Object>,
    HolderOwner<Object> {
        UniversalLookup() {
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourceKey) {
            return Optional.of(this.getOrCreate(resourceKey));
        }

        @Override
        public Holder.Reference<Object> getOrThrow(ResourceKey<Object> resourceKey) {
            return this.getOrCreate(resourceKey);
        }

        private Holder.Reference<Object> getOrCreate(ResourceKey<Object> resourceKey2) {
            return PlaceholderLookupProvider.this.holders.computeIfAbsent(resourceKey2, resourceKey -> Holder.Reference.createStandAlone(this, resourceKey));
        }

        @Override
        public Optional<HolderSet.Named<Object>> get(TagKey<Object> tagKey) {
            return Optional.of(this.getOrCreate(tagKey));
        }

        @Override
        public HolderSet.Named<Object> getOrThrow(TagKey<Object> tagKey) {
            return this.getOrCreate(tagKey);
        }

        private HolderSet.Named<Object> getOrCreate(TagKey<Object> tagKey2) {
            return PlaceholderLookupProvider.this.holderSets.computeIfAbsent(tagKey2, tagKey -> HolderSet.emptyNamed(this, tagKey));
        }

        public <T> HolderGetter<T> castAsLookup() {
            return this;
        }

        public <T> HolderOwner<T> castAsOwner() {
            return this;
        }
    }
}

