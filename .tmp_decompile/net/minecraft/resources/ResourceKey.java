/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.MapMaker
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class ResourceKey<T> {
    private static final ConcurrentMap<InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
    private final Identifier registryName;
    private final Identifier identifier;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
        return Identifier.CODEC.xmap(identifier -> ResourceKey.create(resourceKey, identifier), ResourceKey::identifier);
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> streamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
        return Identifier.STREAM_CODEC.map(identifier -> ResourceKey.create(resourceKey, identifier), ResourceKey::identifier);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, Identifier identifier) {
        return ResourceKey.create(resourceKey.identifier, identifier);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(Identifier identifier) {
        return ResourceKey.create(Registries.ROOT_REGISTRY_NAME, identifier);
    }

    private static <T> ResourceKey<T> create(Identifier identifier, Identifier identifier2) {
        return VALUES.computeIfAbsent(new InternKey(identifier, identifier2), internKey -> new ResourceKey(internKey.registry, internKey.identifier));
    }

    private ResourceKey(Identifier identifier, Identifier identifier2) {
        this.registryName = identifier;
        this.identifier = identifier2;
    }

    public String toString() {
        return "ResourceKey[" + String.valueOf(this.registryName) + " / " + String.valueOf(this.identifier) + "]";
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> resourceKey) {
        return this.registryName.equals(resourceKey.identifier());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
        return this.isFor(resourceKey) ? Optional.of(this) : Optional.empty();
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public Identifier registry() {
        return this.registryName;
    }

    public ResourceKey<Registry<T>> registryKey() {
        return ResourceKey.createRegistryKey(this.registryName);
    }

    static final class InternKey
    extends Record {
        final Identifier registry;
        final Identifier identifier;

        InternKey(Identifier identifier, Identifier identifier2) {
            this.registry = identifier;
            this.identifier = identifier2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this, object);
        }

        public Identifier registry() {
            return this.registry;
        }

        public Identifier identifier() {
            return this.identifier;
        }
    }
}

