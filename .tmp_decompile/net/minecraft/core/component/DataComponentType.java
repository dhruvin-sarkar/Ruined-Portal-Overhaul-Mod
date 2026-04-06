/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface DataComponentType<T> {
    public static final Codec<DataComponentType<?>> CODEC = Codec.lazyInitialized(() -> BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentType<?>> STREAM_CODEC = StreamCodec.recursive(streamCodec -> ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE));
    public static final Codec<DataComponentType<?>> PERSISTENT_CODEC = CODEC.validate(dataComponentType -> dataComponentType.isTransient() ? DataResult.error(() -> "Encountered transient component " + String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey((DataComponentType<?>)dataComponentType))) : DataResult.success((Object)dataComponentType));
    public static final Codec<Map<DataComponentType<?>, Object>> VALUE_MAP_CODEC = Codec.dispatchedMap(PERSISTENT_CODEC, DataComponentType::codecOrThrow);

    public static <T> Builder<T> builder() {
        return new Builder();
    }

    public @Nullable Codec<T> codec();

    default public Codec<T> codecOrThrow() {
        Codec<T> codec = this.codec();
        if (codec == null) {
            throw new IllegalStateException(String.valueOf(this) + " is not a persistent component");
        }
        return codec;
    }

    default public boolean isTransient() {
        return this.codec() == null;
    }

    public boolean ignoreSwapAnimation();

    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    public static class Builder<T> {
        private @Nullable Codec<T> codec;
        private @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
        private boolean cacheEncoding;
        private boolean ignoreSwapAnimation;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.streamCodec = streamCodec;
            return this;
        }

        public Builder<T> cacheEncoding() {
            this.cacheEncoding = true;
            return this;
        }

        public DataComponentType<T> build() {
            StreamCodec streamCodec = (StreamCodec)Objects.requireNonNullElseGet(this.streamCodec, () -> ByteBufCodecs.fromCodecWithRegistries(Objects.requireNonNull(this.codec, "Missing Codec for component")));
            Codec<T> codec = this.cacheEncoding && this.codec != null ? DataComponents.ENCODER_CACHE.wrap(this.codec) : this.codec;
            return new SimpleType<T>(codec, streamCodec, this.ignoreSwapAnimation);
        }

        public Builder<T> ignoreSwapAnimation() {
            this.ignoreSwapAnimation = true;
            return this;
        }

        static class SimpleType<T>
        implements DataComponentType<T> {
            private final @Nullable Codec<T> codec;
            private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
            private final boolean ignoreSwapAnimation;

            SimpleType(@Nullable Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean bl) {
                this.codec = codec;
                this.streamCodec = streamCodec;
                this.ignoreSwapAnimation = bl;
            }

            @Override
            public boolean ignoreSwapAnimation() {
                return this.ignoreSwapAnimation;
            }

            @Override
            public @Nullable Codec<T> codec() {
                return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return this.streamCodec;
            }

            public String toString() {
                return Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, this);
            }
        }
    }
}

