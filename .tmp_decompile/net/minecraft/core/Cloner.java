/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.JavaOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public class Cloner<T> {
    private final Codec<T> directCodec;

    Cloner(Codec<T> codec) {
        this.directCodec = codec;
    }

    public T clone(T object, HolderLookup.Provider provider, HolderLookup.Provider provider2) {
        RegistryOps dynamicOps = provider.createSerializationContext(JavaOps.INSTANCE);
        RegistryOps dynamicOps2 = provider2.createSerializationContext(JavaOps.INSTANCE);
        Object object2 = this.directCodec.encodeStart(dynamicOps, object).getOrThrow(string -> new IllegalStateException("Failed to encode: " + string));
        return (T)this.directCodec.parse(dynamicOps2, object2).getOrThrow(string -> new IllegalStateException("Failed to decode: " + string));
    }

    public static class Factory {
        private final Map<ResourceKey<? extends Registry<?>>, Cloner<?>> codecs = new HashMap();

        public <T> Factory addCodec(ResourceKey<? extends Registry<? extends T>> resourceKey, Codec<T> codec) {
            this.codecs.put(resourceKey, new Cloner<T>(codec));
            return this;
        }

        public <T> @Nullable Cloner<T> cloner(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.codecs.get(resourceKey);
        }
    }
}

