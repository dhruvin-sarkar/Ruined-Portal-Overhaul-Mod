/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Interner
 *  com.google.common.collect.Interners
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, Identifier location) {
    private static final Interner<TagKey<?>> VALUES = Interners.newWeakInterner();

    public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
        return Identifier.CODEC.xmap(identifier -> TagKey.create(resourceKey, identifier), TagKey::location);
    }

    public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> resourceKey) {
        return Codec.STRING.comapFlatMap(string -> string.startsWith("#") ? Identifier.read(string.substring(1)).map(identifier -> TagKey.create(resourceKey, identifier)) : DataResult.error(() -> "Not a tag id"), tagKey -> "#" + String.valueOf(tagKey.location));
    }

    public static <T> StreamCodec<ByteBuf, TagKey<T>> streamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
        return Identifier.STREAM_CODEC.map(identifier -> TagKey.create(resourceKey, identifier), TagKey::location);
    }

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, Identifier identifier) {
        return (TagKey)((Object)VALUES.intern(new TagKey<T>(resourceKey, identifier)));
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> resourceKey) {
        return this.registry == resourceKey;
    }

    public <E> Optional<TagKey<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
        return this.isFor(resourceKey) ? Optional.of(this) : Optional.empty();
    }

    public String toString() {
        return "TagKey[" + String.valueOf(this.registry.identifier()) + " / " + String.valueOf(this.location) + "]";
    }
}

