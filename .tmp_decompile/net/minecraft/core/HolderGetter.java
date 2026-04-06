/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

public interface HolderGetter<T> {
    public Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    default public Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
        return this.get(resourceKey).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf(resourceKey)));
    }

    public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    default public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
        return this.get(tagKey).orElseThrow(() -> new IllegalStateException("Missing tag " + String.valueOf(tagKey)));
    }

    default public Optional<Holder<T>> getRandomElementOf(TagKey<T> tagKey, RandomSource randomSource) {
        return this.get(tagKey).flatMap(named -> named.getRandomElement(randomSource));
    }

    public static interface Provider {
        public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default public <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.lookup(resourceKey).orElseThrow(() -> new IllegalStateException("Registry " + String.valueOf(resourceKey.identifier()) + " not found"));
        }

        default public <T> Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
            return this.lookup(resourceKey.registryKey()).flatMap(holderGetter -> holderGetter.get(resourceKey));
        }

        default public <T> Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
            return (Holder.Reference)this.lookup(resourceKey.registryKey()).flatMap(holderGetter -> holderGetter.get(resourceKey)).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf(resourceKey)));
        }
    }
}

