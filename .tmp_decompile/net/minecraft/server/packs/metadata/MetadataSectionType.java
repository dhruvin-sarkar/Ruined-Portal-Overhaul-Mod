/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.server.packs.metadata;

import com.mojang.serialization.Codec;
import java.util.Optional;

public record MetadataSectionType<T>(String name, Codec<T> codec) {
    public WithValue<T> withValue(T object) {
        return new WithValue<T>(this, object);
    }

    public record WithValue<T>(MetadataSectionType<T> type, T value) {
        public <U> Optional<U> unwrapToType(MetadataSectionType<U> metadataSectionType) {
            return metadataSectionType == this.type ? Optional.of(this.value) : Optional.empty();
        }
    }
}

