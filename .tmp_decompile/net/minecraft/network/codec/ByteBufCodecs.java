/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 */
package net.minecraft.network.codec;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface ByteBufCodecs {
    public static final int MAX_INITIAL_COLLECTION_SIZE = 65536;
    public static final StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf byteBuf) {
            return byteBuf.readBoolean();
        }

        @Override
        public void encode(ByteBuf byteBuf, Boolean boolean_) {
            byteBuf.writeBoolean(boolean_.booleanValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf byteBuf) {
            return byteBuf.readByte();
        }

        @Override
        public void encode(ByteBuf byteBuf, Byte byte_) {
            byteBuf.writeByte((int)byte_.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Float> ROTATION_BYTE = BYTE.map(Mth::unpackDegrees, Mth::packDegrees);
    public static final StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf byteBuf) {
            return byteBuf.readShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Short short_) {
            byteBuf.writeShort((int)short_.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeShort(integer.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return byteBuf.readInt();
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeInt(integer.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return VarInt.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            VarInt.write(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = VAR_INT.map(integer -> integer == 0 ? OptionalInt.empty() : OptionalInt.of(integer - 1), optionalInt -> optionalInt.isPresent() ? optionalInt.getAsInt() + 1 : 0);
    public static final StreamCodec<ByteBuf, Long> LONG = new StreamCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return byteBuf.readLong();
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            byteBuf.writeLong(long_.longValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf byteBuf) {
            return VarLong.read(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Long long_) {
            VarLong.write(byteBuf, long_);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf byteBuf) {
            return Float.valueOf(byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Float float_) {
            byteBuf.writeFloat(float_.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void encode(ByteBuf byteBuf, Double double_) {
            byteBuf.writeDouble(double_.doubleValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>(){

        @Override
        public byte[] decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readByteArray(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, byte[] bs) {
            FriendlyByteBuf.writeByteArray(byteBuf, bs);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, long[]> LONG_ARRAY = new StreamCodec<ByteBuf, long[]>(){

        @Override
        public long[] decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readLongArray(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, long[] ls) {
            FriendlyByteBuf.writeLongArray(byteBuf, ls);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (long[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = ByteBufCodecs.stringUtf8(Short.MAX_VALUE);
    public static final StreamCodec<ByteBuf, Tag> TAG = ByteBufCodecs.tagCodec(NbtAccounter::defaultQuota);
    public static final StreamCodec<ByteBuf, Tag> TRUSTED_TAG = ByteBufCodecs.tagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(NbtAccounter::defaultQuota);
    public static final StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>(){

        @Override
        public Optional<CompoundTag> decode(ByteBuf byteBuf) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt(byteBuf));
        }

        @Override
        public void encode(ByteBuf byteBuf, Optional<CompoundTag> optional) {
            FriendlyByteBuf.writeNbt(byteBuf, optional.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Vector3fc> VECTOR3F = new StreamCodec<ByteBuf, Vector3fc>(){

        @Override
        public Vector3fc decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readVector3f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Vector3fc vector3fc) {
            FriendlyByteBuf.writeVector3f(byteBuf, vector3fc);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3fc)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Quaternionfc> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionfc>(){

        @Override
        public Quaternionfc decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readQuaternion(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Quaternionfc quaternionfc) {
            FriendlyByteBuf.writeQuaternion(byteBuf, quaternionfc);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionfc)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> CONTAINER_ID = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readContainerId(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            FriendlyByteBuf.writeContainerId(byteBuf, integer);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>(){

        @Override
        public PropertyMap decode(ByteBuf byteBuf2) {
            int i = ByteBufCodecs.readCount(byteBuf2, 16);
            ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
            for (int j = 0; j < i; ++j) {
                String string = Utf8String.read(byteBuf2, 64);
                String string2 = Utf8String.read(byteBuf2, Short.MAX_VALUE);
                String string3 = FriendlyByteBuf.readNullable(byteBuf2, byteBuf -> Utf8String.read(byteBuf, 1024));
                Property property = new Property(string, string2, string3);
                builder.put((Object)property.name(), (Object)property);
            }
            return new PropertyMap((Multimap)builder.build());
        }

        @Override
        public void encode(ByteBuf byteBuf2, PropertyMap propertyMap) {
            ByteBufCodecs.writeCount(byteBuf2, propertyMap.size(), 16);
            for (Property property : propertyMap.values()) {
                Utf8String.write(byteBuf2, property.name(), 64);
                Utf8String.write(byteBuf2, property.value(), Short.MAX_VALUE);
                FriendlyByteBuf.writeNullable(byteBuf2, property.signature(), (byteBuf, string) -> Utf8String.write(byteBuf, string, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, String> PLAYER_NAME = ByteBufCodecs.stringUtf8(16);
    public static final StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = StreamCodec.composite(UUIDUtil.STREAM_CODEC, GameProfile::id, PLAYER_NAME, GameProfile::name, GAME_PROFILE_PROPERTIES, GameProfile::properties, GameProfile::new);
    public static final StreamCodec<ByteBuf, Integer> RGB_COLOR = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf byteBuf) {
            return ARGB.color(byteBuf.readByte() & 0xFF, byteBuf.readByte() & 0xFF, byteBuf.readByte() & 0xFF);
        }

        @Override
        public void encode(ByteBuf byteBuf, Integer integer) {
            byteBuf.writeByte(ARGB.red(integer));
            byteBuf.writeByte(ARGB.green(integer));
            byteBuf.writeByte(ARGB.blue(integer));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public static StreamCodec<ByteBuf, byte[]> byteArray(final int i) {
        return new StreamCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf byteBuf) {
                return FriendlyByteBuf.readByteArray(byteBuf, i);
            }

            @Override
            public void encode(ByteBuf byteBuf, byte[] bs) {
                if (bs.length > i) {
                    throw new EncoderException("ByteArray with size " + bs.length + " is bigger than allowed " + i);
                }
                FriendlyByteBuf.writeByteArray(byteBuf, bs);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, String> stringUtf8(final int i) {
        return new StreamCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf byteBuf) {
                return Utf8String.read(byteBuf, i);
            }

            @Override
            public void encode(ByteBuf byteBuf, String string) {
                Utf8String.write(byteBuf, string, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, Optional<Tag>> optionalTagCodec(final Supplier<NbtAccounter> supplier) {
        return new StreamCodec<ByteBuf, Optional<Tag>>(){

            @Override
            public Optional<Tag> decode(ByteBuf byteBuf) {
                return Optional.ofNullable(FriendlyByteBuf.readNbt(byteBuf, (NbtAccounter)supplier.get()));
            }

            @Override
            public void encode(ByteBuf byteBuf, Optional<Tag> optional) {
                FriendlyByteBuf.writeNbt(byteBuf, optional.orElse(null));
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> supplier) {
        return new StreamCodec<ByteBuf, Tag>(){

            @Override
            public Tag decode(ByteBuf byteBuf) {
                Tag tag = FriendlyByteBuf.readNbt(byteBuf, (NbtAccounter)supplier.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return tag;
            }

            @Override
            public void encode(ByteBuf byteBuf, Tag tag) {
                if (tag == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                FriendlyByteBuf.writeNbt(byteBuf, tag);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Tag)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> supplier) {
        return ByteBufCodecs.tagCodec(supplier).map(tag -> {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                return compoundTag;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf(tag));
        }, compoundTag -> compoundTag);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> codec) {
        return ByteBufCodecs.fromCodec(codec, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return ByteBufCodecs.fromCodec(codec, NbtAccounter::defaultQuota);
    }

    public static <T, B extends ByteBuf, V> StreamCodec.CodecOperation<B, T, V> fromCodec(final DynamicOps<T> dynamicOps, final Codec<V> codec) {
        return streamCodec -> new StreamCodec<B, V>(){

            @Override
            public V decode(B byteBuf) {
                Object object = streamCodec.decode(byteBuf);
                return codec.parse(dynamicOps, object).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(object)));
            }

            @Override
            public void encode(B byteBuf, V object) {
                Object object2 = codec.encodeStart(dynamicOps, object).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(object)));
                streamCodec.encode(byteBuf, object2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec, Supplier<NbtAccounter> supplier) {
        return ByteBufCodecs.tagCodec(supplier).apply(ByteBufCodecs.fromCodec(NbtOps.INSTANCE, codec));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec, NbtAccounter::defaultQuota);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> codec, Supplier<NbtAccounter> supplier) {
        final StreamCodec<ByteBuf, Tag> streamCodec = ByteBufCodecs.tagCodec(supplier);
        return new StreamCodec<RegistryFriendlyByteBuf, T>(){

            @Override
            public T decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                Tag tag = (Tag)streamCodec.decode(registryFriendlyByteBuf);
                RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return codec.parse(registryOps, (Object)tag).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(tag)));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, T object) {
                RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag tag = (Tag)codec.encodeStart(registryOps, object).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(object)));
                streamCodec.encode(registryFriendlyByteBuf, tag);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<? super B, V> streamCodec) {
        return new StreamCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B byteBuf) {
                if (byteBuf.readBoolean()) {
                    return Optional.of(streamCodec.decode(byteBuf));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B byteBuf, Optional<V> optional) {
                if (optional.isPresent()) {
                    byteBuf.writeBoolean(true);
                    streamCodec.encode(byteBuf, optional.get());
                } else {
                    byteBuf.writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCount(ByteBuf byteBuf, int i) {
        int j = VarInt.read(byteBuf);
        if (j > i) {
            throw new DecoderException(j + " elements exceeded max size of: " + i);
        }
        return j;
    }

    public static void writeCount(ByteBuf byteBuf, int i, int j) {
        if (i > j) {
            throw new EncoderException(i + " elements exceeded max size of: " + j);
        }
        VarInt.write(byteBuf, i);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intFunction, StreamCodec<? super B, V> streamCodec) {
        return ByteBufCodecs.collection(intFunction, streamCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(final IntFunction<C> intFunction, final StreamCodec<? super B, V> streamCodec, final int i) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B byteBuf) {
                int i2 = ByteBufCodecs.readCount(byteBuf, i);
                Collection collection = (Collection)intFunction.apply(Math.min(i2, 65536));
                for (int j = 0; j < i2; ++j) {
                    collection.add(streamCodec.decode(byteBuf));
                }
                return collection;
            }

            @Override
            public void encode(B byteBuf, C collection) {
                ByteBufCodecs.writeCount(byteBuf, collection.size(), i);
                for (Object object : collection) {
                    streamCodec.encode(byteBuf, object);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> intFunction) {
        return streamCodec -> ByteBufCodecs.collection(intFunction, streamCodec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return streamCodec -> ByteBufCodecs.collection(ArrayList::new, streamCodec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int i) {
        return streamCodec -> ByteBufCodecs.collection(ArrayList::new, streamCodec, i);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> intFunction, StreamCodec<? super B, K> streamCodec, StreamCodec<? super B, V> streamCodec2) {
        return ByteBufCodecs.map(intFunction, streamCodec, streamCodec2, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(final IntFunction<? extends M> intFunction, final StreamCodec<? super B, K> streamCodec, final StreamCodec<? super B, V> streamCodec2, final int i) {
        return new StreamCodec<B, M>(){

            @Override
            public void encode(B byteBuf, M map) {
                ByteBufCodecs.writeCount(byteBuf, map.size(), i);
                map.forEach((object, object2) -> {
                    streamCodec.encode(byteBuf, object);
                    streamCodec2.encode(byteBuf, object2);
                });
            }

            @Override
            public M decode(B byteBuf) {
                int i2 = ByteBufCodecs.readCount(byteBuf, i);
                Map map = (Map)intFunction.apply(Math.min(i2, 65536));
                for (int j = 0; j < i2; ++j) {
                    Object object = streamCodec.decode(byteBuf);
                    Object object2 = streamCodec2.decode(byteBuf);
                    map.put(object, object2);
                }
                return map;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> streamCodec, final StreamCodec<? super B, R> streamCodec2) {
        return new StreamCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B byteBuf) {
                if (byteBuf.readBoolean()) {
                    return Either.left(streamCodec.decode(byteBuf));
                }
                return Either.right(streamCodec2.decode(byteBuf));
            }

            @Override
            public void encode(B byteBuf, Either<L, R> either) {
                either.ifLeft(object -> {
                    byteBuf.writeBoolean(true);
                    streamCodec.encode(byteBuf, object);
                }).ifRight(object -> {
                    byteBuf.writeBoolean(false);
                    streamCodec2.encode(byteBuf, object);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)((Either)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, V> lengthPrefixed(final int i, final BiFunction<B, ByteBuf, B> biFunction) {
        return streamCodec -> new StreamCodec<B, V>(){

            @Override
            public V decode(B byteBuf) {
                int i2 = VarInt.read(byteBuf);
                if (i2 > i) {
                    throw new DecoderException("Buffer size " + i2 + " is larger than allowed limit of " + i);
                }
                int j = byteBuf.readerIndex();
                ByteBuf byteBuf2 = (ByteBuf)biFunction.apply(byteBuf, byteBuf.slice(j, i2));
                byteBuf.readerIndex(j + i2);
                return streamCodec.decode(byteBuf2);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void encode(B byteBuf, V object) {
                ByteBuf byteBuf2 = (ByteBuf)biFunction.apply(byteBuf, byteBuf.alloc().buffer());
                try {
                    streamCodec.encode(byteBuf2, object);
                    int i2 = byteBuf2.readableBytes();
                    if (i2 > i) {
                        throw new EncoderException("Buffer size " + i2 + " is  larger than allowed limit of " + i);
                    }
                    VarInt.write(byteBuf, i2);
                    byteBuf.writeBytes(byteBuf2);
                }
                finally {
                    byteBuf2.release();
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <V> StreamCodec.CodecOperation<ByteBuf, V, V> lengthPrefixed(int i) {
        return ByteBufCodecs.lengthPrefixed(i, (byteBuf, byteBuf2) -> byteBuf2);
    }

    public static <V> StreamCodec.CodecOperation<RegistryFriendlyByteBuf, V, V> registryFriendlyLengthPrefixed(int i) {
        return ByteBufCodecs.lengthPrefixed(i, (registryFriendlyByteBuf, byteBuf) -> new RegistryFriendlyByteBuf((ByteBuf)byteBuf, registryFriendlyByteBuf.registryAccess()));
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> intFunction, final ToIntFunction<T> toIntFunction) {
        return new StreamCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf byteBuf) {
                int i = VarInt.read(byteBuf);
                return intFunction.apply(i);
            }

            @Override
            public void encode(ByteBuf byteBuf, T object) {
                int i = toIntFunction.applyAsInt(object);
                VarInt.write(byteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> idMap) {
        return ByteBufCodecs.idMapper(idMap::byIdOrThrow, idMap::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(final ResourceKey<? extends Registry<T>> resourceKey, final Function<Registry<T>, IdMap<R>> function) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>(){

            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                return (IdMap)function.apply(registryFriendlyByteBuf.registryAccess().lookupOrThrow(resourceKey));
            }

            @Override
            public R decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int i = VarInt.read(registryFriendlyByteBuf);
                return this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(i);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, R object) {
                int i = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(object);
                VarInt.write(registryFriendlyByteBuf, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> resourceKey) {
        return ByteBufCodecs.registry(resourceKey, registry -> registry);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> resourceKey) {
        return ByteBufCodecs.registry(resourceKey, Registry::asHolderIdMap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(final ResourceKey<? extends Registry<T>> resourceKey, final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>(){
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                return registryFriendlyByteBuf.registryAccess().lookupOrThrow(resourceKey).asHolderIdMap();
            }

            @Override
            public Holder<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int i = VarInt.read(registryFriendlyByteBuf);
                if (i == 0) {
                    return Holder.direct(streamCodec.decode(registryFriendlyByteBuf));
                }
                return this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(i - 1);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, Holder<T> holder) {
                switch (holder.kind()) {
                    case REFERENCE: {
                        int i = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(holder);
                        VarInt.write(registryFriendlyByteBuf, i + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInt.write(registryFriendlyByteBuf, 0);
                        streamCodec.encode(registryFriendlyByteBuf, holder.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (Holder)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> resourceKey) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>(){
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec;
            {
                this.holderCodec = ByteBufCodecs.holderRegistry(resourceKey);
            }

            @Override
            public HolderSet<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int i = VarInt.read(registryFriendlyByteBuf) - 1;
                if (i == -1) {
                    HolderLookup.RegistryLookup registry = registryFriendlyByteBuf.registryAccess().lookupOrThrow(resourceKey);
                    return (HolderSet)registry.get(TagKey.create(resourceKey, (Identifier)Identifier.STREAM_CODEC.decode(registryFriendlyByteBuf))).orElseThrow();
                }
                ArrayList<Holder> list = new ArrayList<Holder>(Math.min(i, 65536));
                for (int j = 0; j < i; ++j) {
                    list.add((Holder)this.holderCodec.decode(registryFriendlyByteBuf));
                }
                return HolderSet.direct(list);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, HolderSet<T> holderSet) {
                Optional optional = holderSet.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(registryFriendlyByteBuf, 0);
                    Identifier.STREAM_CODEC.encode(registryFriendlyByteBuf, optional.get().location());
                } else {
                    VarInt.write(registryFriendlyByteBuf, holderSet.size() + 1);
                    for (Holder holder : holderSet) {
                        this.holderCodec.encode(registryFriendlyByteBuf, holder);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (HolderSet)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static StreamCodec<ByteBuf, JsonElement> lenientJson(final int i) {
        return new StreamCodec<ByteBuf, JsonElement>(){
            private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

            @Override
            public JsonElement decode(ByteBuf byteBuf) {
                String string = Utf8String.read(byteBuf, i);
                try {
                    return LenientJsonParser.parse(string);
                }
                catch (JsonSyntaxException jsonSyntaxException) {
                    throw new DecoderException("Failed to parse JSON", (Throwable)jsonSyntaxException);
                }
            }

            @Override
            public void encode(ByteBuf byteBuf, JsonElement jsonElement) {
                String string = GSON.toJson(jsonElement);
                Utf8String.write(byteBuf, string, i);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (JsonElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }
}

