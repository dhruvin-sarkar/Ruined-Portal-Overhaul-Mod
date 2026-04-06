/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufAllocator
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  io.netty.util.ByteProcessor
 *  io.netty.util.ReferenceCounted
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCounted;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.LpVec3;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class FriendlyByteBuf
extends ByteBuf {
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public FriendlyByteBuf(ByteBuf byteBuf) {
        this.source = byteBuf;
    }

    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<Tag> dynamicOps, Codec<T> codec) {
        return this.readWithCodec(dynamicOps, codec, NbtAccounter.unlimitedHeap());
    }

    @Deprecated
    public <T> T readWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, NbtAccounter nbtAccounter) {
        Tag tag = this.readNbt(nbtAccounter);
        return (T)codec.parse(dynamicOps, (Object)tag).getOrThrow(string -> new DecoderException("Failed to decode: " + string + " " + String.valueOf(tag)));
    }

    @Deprecated
    public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, T object) {
        Tag tag = (Tag)codec.encodeStart(dynamicOps, object).getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(object)));
        this.writeNbt(tag);
        return this;
    }

    public <T> T readLenientJsonWithCodec(Codec<T> codec) {
        JsonElement jsonElement = LenientJsonParser.parse(this.readUtf());
        DataResult dataResult = codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement);
        return (T)dataResult.getOrThrow(string -> new DecoderException("Failed to decode JSON: " + string));
    }

    public <T> void writeJsonWithCodec(Codec<T> codec, T object) {
        DataResult dataResult = codec.encodeStart((DynamicOps)JsonOps.INSTANCE, object);
        this.writeUtf(GSON.toJson((JsonElement)dataResult.getOrThrow(string -> new EncoderException("Failed to encode: " + string + " " + String.valueOf(object)))));
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> intFunction, int i) {
        return j -> {
            if (j > i) {
                throw new DecoderException("Value " + j + " is larger than limit " + i);
            }
            return intFunction.apply(j);
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> intFunction, StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        int i = this.readVarInt();
        Collection collection = (Collection)intFunction.apply(i);
        for (int j = 0; j < i; ++j) {
            collection.add(streamDecoder.decode(this));
        }
        return (C)collection;
    }

    public <T> void writeCollection(Collection<T> collection, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        this.writeVarInt(collection.size());
        for (T object : collection) {
            streamEncoder.encode(this, object);
        }
    }

    public <T> List<T> readList(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        return this.readCollection(Lists::newArrayListWithCapacity, streamDecoder);
    }

    public IntList readIntIdList() {
        int i = this.readVarInt();
        IntArrayList intList = new IntArrayList();
        for (int j = 0; j < i; ++j) {
            intList.add(this.readVarInt());
        }
        return intList;
    }

    public void writeIntIdList(IntList intList) {
        this.writeVarInt(intList.size());
        intList.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> intFunction, StreamDecoder<? super FriendlyByteBuf, K> streamDecoder, StreamDecoder<? super FriendlyByteBuf, V> streamDecoder2) {
        int i = this.readVarInt();
        Map map = (Map)intFunction.apply(i);
        for (int j = 0; j < i; ++j) {
            K object = streamDecoder.decode(this);
            V object2 = streamDecoder2.decode(this);
            map.put(object, object2);
        }
        return (M)map;
    }

    public <K, V> Map<K, V> readMap(StreamDecoder<? super FriendlyByteBuf, K> streamDecoder, StreamDecoder<? super FriendlyByteBuf, V> streamDecoder2) {
        return this.readMap(Maps::newHashMapWithExpectedSize, streamDecoder, streamDecoder2);
    }

    public <K, V> void writeMap(Map<K, V> map, StreamEncoder<? super FriendlyByteBuf, K> streamEncoder, StreamEncoder<? super FriendlyByteBuf, V> streamEncoder2) {
        this.writeVarInt(map.size());
        map.forEach((object, object2) -> {
            streamEncoder.encode(this, object);
            streamEncoder2.encode(this, object2);
        });
    }

    public void readWithCount(Consumer<FriendlyByteBuf> consumer) {
        int i = this.readVarInt();
        for (int j = 0; j < i; ++j) {
            consumer.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> class_) {
        Enum[] enums = (Enum[])class_.getEnumConstants();
        BitSet bitSet = new BitSet(enums.length);
        for (int i = 0; i < enums.length; ++i) {
            bitSet.set(i, enumSet.contains(enums[i]));
        }
        this.writeFixedBitSet(bitSet, enums.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> class_) {
        Enum[] enums = (Enum[])class_.getEnumConstants();
        BitSet bitSet = this.readFixedBitSet(enums.length);
        EnumSet<Enum> enumSet = EnumSet.noneOf(class_);
        for (int i = 0; i < enums.length; ++i) {
            if (!bitSet.get(i)) continue;
            enumSet.add(enums[i]);
        }
        return enumSet;
    }

    public <T> void writeOptional(Optional<T> optional, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        if (optional.isPresent()) {
            this.writeBoolean(true);
            streamEncoder.encode(this, optional.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        if (this.readBoolean()) {
            return Optional.of(streamDecoder.decode(this));
        }
        return Optional.empty();
    }

    public <L, R> void writeEither(Either<L, R> either, StreamEncoder<? super FriendlyByteBuf, L> streamEncoder, StreamEncoder<? super FriendlyByteBuf, R> streamEncoder2) {
        either.ifLeft(object -> {
            this.writeBoolean(true);
            streamEncoder.encode(this, object);
        }).ifRight(object -> {
            this.writeBoolean(false);
            streamEncoder2.encode(this, object);
        });
    }

    public <L, R> Either<L, R> readEither(StreamDecoder<? super FriendlyByteBuf, L> streamDecoder, StreamDecoder<? super FriendlyByteBuf, R> streamDecoder2) {
        if (this.readBoolean()) {
            return Either.left(streamDecoder.decode(this));
        }
        return Either.right(streamDecoder2.decode(this));
    }

    public <T> @Nullable T readNullable(StreamDecoder<? super FriendlyByteBuf, T> streamDecoder) {
        return FriendlyByteBuf.readNullable(this, streamDecoder);
    }

    public static <T, B extends ByteBuf> @Nullable T readNullable(B byteBuf, StreamDecoder<? super B, T> streamDecoder) {
        if (byteBuf.readBoolean()) {
            return streamDecoder.decode(byteBuf);
        }
        return null;
    }

    public <T> void writeNullable(@Nullable T object, StreamEncoder<? super FriendlyByteBuf, T> streamEncoder) {
        FriendlyByteBuf.writeNullable(this, object, streamEncoder);
    }

    public static <T, B extends ByteBuf> void writeNullable(B byteBuf, @Nullable T object, StreamEncoder<? super B, T> streamEncoder) {
        if (object != null) {
            byteBuf.writeBoolean(true);
            streamEncoder.encode(byteBuf, object);
        } else {
            byteBuf.writeBoolean(false);
        }
    }

    public byte[] readByteArray() {
        return FriendlyByteBuf.readByteArray(this);
    }

    public static byte[] readByteArray(ByteBuf byteBuf) {
        return FriendlyByteBuf.readByteArray(byteBuf, byteBuf.readableBytes());
    }

    public FriendlyByteBuf writeByteArray(byte[] bs) {
        FriendlyByteBuf.writeByteArray(this, bs);
        return this;
    }

    public static void writeByteArray(ByteBuf byteBuf, byte[] bs) {
        VarInt.write(byteBuf, bs.length);
        byteBuf.writeBytes(bs);
    }

    public byte[] readByteArray(int i) {
        return FriendlyByteBuf.readByteArray(this, i);
    }

    public static byte[] readByteArray(ByteBuf byteBuf, int i) {
        int j = VarInt.read(byteBuf);
        if (j > i) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
        }
        byte[] bs = new byte[j];
        byteBuf.readBytes(bs);
        return bs;
    }

    public FriendlyByteBuf writeVarIntArray(int[] is) {
        this.writeVarInt(is.length);
        for (int i : is) {
            this.writeVarInt(i);
        }
        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int i) {
        int j = this.readVarInt();
        if (j > i) {
            throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + i);
        }
        int[] is = new int[j];
        for (int k = 0; k < is.length; ++k) {
            is[k] = this.readVarInt();
        }
        return is;
    }

    public FriendlyByteBuf writeLongArray(long[] ls) {
        FriendlyByteBuf.writeLongArray(this, ls);
        return this;
    }

    public static void writeLongArray(ByteBuf byteBuf, long[] ls) {
        VarInt.write(byteBuf, ls.length);
        FriendlyByteBuf.writeFixedSizeLongArray(byteBuf, ls);
    }

    public FriendlyByteBuf writeFixedSizeLongArray(long[] ls) {
        FriendlyByteBuf.writeFixedSizeLongArray(this, ls);
        return this;
    }

    public static void writeFixedSizeLongArray(ByteBuf byteBuf, long[] ls) {
        for (long l : ls) {
            byteBuf.writeLong(l);
        }
    }

    public long[] readLongArray() {
        return FriendlyByteBuf.readLongArray(this);
    }

    public long[] readFixedSizeLongArray(long[] ls) {
        return FriendlyByteBuf.readFixedSizeLongArray(this, ls);
    }

    public static long[] readLongArray(ByteBuf byteBuf) {
        int j;
        int i = VarInt.read(byteBuf);
        if (i > (j = byteBuf.readableBytes() / 8)) {
            throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + j);
        }
        return FriendlyByteBuf.readFixedSizeLongArray(byteBuf, new long[i]);
    }

    public static long[] readFixedSizeLongArray(ByteBuf byteBuf, long[] ls) {
        for (int i = 0; i < ls.length; ++i) {
            ls[i] = byteBuf.readLong();
        }
        return ls;
    }

    public BlockPos readBlockPos() {
        return FriendlyByteBuf.readBlockPos(this);
    }

    public static BlockPos readBlockPos(ByteBuf byteBuf) {
        return BlockPos.of(byteBuf.readLong());
    }

    public FriendlyByteBuf writeBlockPos(BlockPos blockPos) {
        FriendlyByteBuf.writeBlockPos(this, blockPos);
        return this;
    }

    public static void writeBlockPos(ByteBuf byteBuf, BlockPos blockPos) {
        byteBuf.writeLong(blockPos.asLong());
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public FriendlyByteBuf writeChunkPos(ChunkPos chunkPos) {
        this.writeLong(chunkPos.toLong());
        return this;
    }

    public static ChunkPos readChunkPos(ByteBuf byteBuf) {
        return new ChunkPos(byteBuf.readLong());
    }

    public static void writeChunkPos(ByteBuf byteBuf, ChunkPos chunkPos) {
        byteBuf.writeLong(chunkPos.toLong());
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<Level> resourceKey = this.readResourceKey(Registries.DIMENSION);
        BlockPos blockPos = this.readBlockPos();
        return GlobalPos.of(resourceKey, blockPos);
    }

    public void writeGlobalPos(GlobalPos globalPos) {
        this.writeResourceKey(globalPos.dimension());
        this.writeBlockPos(globalPos.pos());
    }

    public Vector3f readVector3f() {
        return FriendlyByteBuf.readVector3f(this);
    }

    public static Vector3f readVector3f(ByteBuf byteBuf) {
        return new Vector3f(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        FriendlyByteBuf.writeVector3f(this, (Vector3fc)vector3f);
    }

    public static void writeVector3f(ByteBuf byteBuf, Vector3fc vector3fc) {
        byteBuf.writeFloat(vector3fc.x());
        byteBuf.writeFloat(vector3fc.y());
        byteBuf.writeFloat(vector3fc.z());
    }

    public Quaternionf readQuaternion() {
        return FriendlyByteBuf.readQuaternion(this);
    }

    public static Quaternionf readQuaternion(ByteBuf byteBuf) {
        return new Quaternionf(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
    }

    public void writeQuaternion(Quaternionf quaternionf) {
        FriendlyByteBuf.writeQuaternion(this, (Quaternionfc)quaternionf);
    }

    public static void writeQuaternion(ByteBuf byteBuf, Quaternionfc quaternionfc) {
        byteBuf.writeFloat(quaternionfc.x());
        byteBuf.writeFloat(quaternionfc.y());
        byteBuf.writeFloat(quaternionfc.z());
        byteBuf.writeFloat(quaternionfc.w());
    }

    public static Vec3 readVec3(ByteBuf byteBuf) {
        return new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
    }

    public Vec3 readVec3() {
        return FriendlyByteBuf.readVec3(this);
    }

    public static void writeVec3(ByteBuf byteBuf, Vec3 vec3) {
        byteBuf.writeDouble(vec3.x());
        byteBuf.writeDouble(vec3.y());
        byteBuf.writeDouble(vec3.z());
    }

    public void writeVec3(Vec3 vec3) {
        FriendlyByteBuf.writeVec3(this, vec3);
    }

    public Vec3 readLpVec3() {
        return LpVec3.read(this);
    }

    public void writeLpVec3(Vec3 vec3) {
        LpVec3.write(this, vec3);
    }

    public <T extends Enum<T>> T readEnum(Class<T> class_) {
        return (T)((Enum[])class_.getEnumConstants())[this.readVarInt()];
    }

    public FriendlyByteBuf writeEnum(Enum<?> enum_) {
        return this.writeVarInt(enum_.ordinal());
    }

    public <T> T readById(IntFunction<T> intFunction) {
        int i = this.readVarInt();
        return intFunction.apply(i);
    }

    public <T> FriendlyByteBuf writeById(ToIntFunction<T> toIntFunction, T object) {
        int i = toIntFunction.applyAsInt(object);
        return this.writeVarInt(i);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public FriendlyByteBuf writeUUID(UUID uUID) {
        FriendlyByteBuf.writeUUID(this, uUID);
        return this;
    }

    public static void writeUUID(ByteBuf byteBuf, UUID uUID) {
        byteBuf.writeLong(uUID.getMostSignificantBits());
        byteBuf.writeLong(uUID.getLeastSignificantBits());
    }

    public UUID readUUID() {
        return FriendlyByteBuf.readUUID(this);
    }

    public static UUID readUUID(ByteBuf byteBuf) {
        return new UUID(byteBuf.readLong(), byteBuf.readLong());
    }

    public FriendlyByteBuf writeVarInt(int i) {
        VarInt.write(this.source, i);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long l) {
        VarLong.write(this.source, l);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable Tag tag) {
        FriendlyByteBuf.writeNbt(this, tag);
        return this;
    }

    public static void writeNbt(ByteBuf byteBuf, @Nullable Tag tag) {
        if (tag == null) {
            tag = EndTag.INSTANCE;
        }
        try {
            NbtIo.writeAnyTag(tag, (DataOutput)new ByteBufOutputStream(byteBuf));
        }
        catch (IOException iOException) {
            throw new EncoderException((Throwable)iOException);
        }
    }

    public @Nullable CompoundTag readNbt() {
        return FriendlyByteBuf.readNbt(this);
    }

    public static @Nullable CompoundTag readNbt(ByteBuf byteBuf) {
        Tag tag = FriendlyByteBuf.readNbt(byteBuf, NbtAccounter.defaultQuota());
        if (tag == null || tag instanceof CompoundTag) {
            return (CompoundTag)tag;
        }
        throw new DecoderException("Not a compound tag: " + String.valueOf(tag));
    }

    public static @Nullable Tag readNbt(ByteBuf byteBuf, NbtAccounter nbtAccounter) {
        try {
            Tag tag = NbtIo.readAnyTag((DataInput)new ByteBufInputStream(byteBuf), nbtAccounter);
            if (tag.getId() == 0) {
                return null;
            }
            return tag;
        }
        catch (IOException iOException) {
            throw new EncoderException((Throwable)iOException);
        }
    }

    public @Nullable Tag readNbt(NbtAccounter nbtAccounter) {
        return FriendlyByteBuf.readNbt(this, nbtAccounter);
    }

    public String readUtf() {
        return this.readUtf(Short.MAX_VALUE);
    }

    public String readUtf(int i) {
        return Utf8String.read(this.source, i);
    }

    public FriendlyByteBuf writeUtf(String string) {
        return this.writeUtf(string, Short.MAX_VALUE);
    }

    public FriendlyByteBuf writeUtf(String string, int i) {
        Utf8String.write(this.source, string, i);
        return this;
    }

    public Identifier readIdentifier() {
        return Identifier.parse(this.readUtf(Short.MAX_VALUE));
    }

    public FriendlyByteBuf writeIdentifier(Identifier identifier) {
        this.writeUtf(identifier.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> resourceKey) {
        Identifier identifier = this.readIdentifier();
        return ResourceKey.create(resourceKey, identifier);
    }

    public void writeResourceKey(ResourceKey<?> resourceKey) {
        this.writeIdentifier(resourceKey.identifier());
    }

    public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
        Identifier identifier = this.readIdentifier();
        return ResourceKey.createRegistryKey(identifier);
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return Crypt.byteToPublicKey(this.readByteArray(512));
        }
        catch (CryptException cryptException) {
            throw new DecoderException("Malformed public key bytes", (Throwable)cryptException);
        }
    }

    public FriendlyByteBuf writePublicKey(PublicKey publicKey) {
        this.writeByteArray(publicKey.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos blockPos = this.readBlockPos();
        Direction direction = this.readEnum(Direction.class);
        float f = this.readFloat();
        float g = this.readFloat();
        float h = this.readFloat();
        boolean bl = this.readBoolean();
        boolean bl2 = this.readBoolean();
        return new BlockHitResult(new Vec3((double)blockPos.getX() + (double)f, (double)blockPos.getY() + (double)g, (double)blockPos.getZ() + (double)h), direction, blockPos, bl, bl2);
    }

    public void writeBlockHitResult(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        this.writeBlockPos(blockPos);
        this.writeEnum(blockHitResult.getDirection());
        Vec3 vec3 = blockHitResult.getLocation();
        this.writeFloat((float)(vec3.x - (double)blockPos.getX()));
        this.writeFloat((float)(vec3.y - (double)blockPos.getY()));
        this.writeFloat((float)(vec3.z - (double)blockPos.getZ()));
        this.writeBoolean(blockHitResult.isInside());
        this.writeBoolean(blockHitResult.isWorldBorderHit());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet bitSet) {
        this.writeLongArray(bitSet.toLongArray());
    }

    public BitSet readFixedBitSet(int i) {
        byte[] bs = new byte[Mth.positiveCeilDiv(i, 8)];
        this.readBytes(bs);
        return BitSet.valueOf(bs);
    }

    public void writeFixedBitSet(BitSet bitSet, int i) {
        if (bitSet.length() > i) {
            throw new EncoderException("BitSet is larger than expected size (" + bitSet.length() + ">" + i + ")");
        }
        byte[] bs = bitSet.toByteArray();
        this.writeBytes(Arrays.copyOf(bs, Mth.positiveCeilDiv(i, 8)));
    }

    public static int readContainerId(ByteBuf byteBuf) {
        return VarInt.read(byteBuf);
    }

    public int readContainerId() {
        return FriendlyByteBuf.readContainerId(this.source);
    }

    public static void writeContainerId(ByteBuf byteBuf, int i) {
        VarInt.write(byteBuf, i);
    }

    public void writeContainerId(int i) {
        FriendlyByteBuf.writeContainerId(this.source, i);
    }

    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    public int capacity() {
        return this.source.capacity();
    }

    public FriendlyByteBuf capacity(int i) {
        this.source.capacity(i);
        return this;
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    public ByteOrder order() {
        return this.source.order();
    }

    public ByteBuf order(ByteOrder byteOrder) {
        return this.source.order(byteOrder);
    }

    public ByteBuf unwrap() {
        return this.source;
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public FriendlyByteBuf readerIndex(int i) {
        this.source.readerIndex(i);
        return this;
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public FriendlyByteBuf writerIndex(int i) {
        this.source.writerIndex(i);
        return this;
    }

    public FriendlyByteBuf setIndex(int i, int j) {
        this.source.setIndex(i, j);
        return this;
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int i) {
        return this.source.isReadable(i);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int i) {
        return this.source.isWritable(i);
    }

    public FriendlyByteBuf clear() {
        this.source.clear();
        return this;
    }

    public FriendlyByteBuf markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public FriendlyByteBuf resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public FriendlyByteBuf markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public FriendlyByteBuf resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public FriendlyByteBuf discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public FriendlyByteBuf discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public FriendlyByteBuf ensureWritable(int i) {
        this.source.ensureWritable(i);
        return this;
    }

    public int ensureWritable(int i, boolean bl) {
        return this.source.ensureWritable(i, bl);
    }

    public boolean getBoolean(int i) {
        return this.source.getBoolean(i);
    }

    public byte getByte(int i) {
        return this.source.getByte(i);
    }

    public short getUnsignedByte(int i) {
        return this.source.getUnsignedByte(i);
    }

    public short getShort(int i) {
        return this.source.getShort(i);
    }

    public short getShortLE(int i) {
        return this.source.getShortLE(i);
    }

    public int getUnsignedShort(int i) {
        return this.source.getUnsignedShort(i);
    }

    public int getUnsignedShortLE(int i) {
        return this.source.getUnsignedShortLE(i);
    }

    public int getMedium(int i) {
        return this.source.getMedium(i);
    }

    public int getMediumLE(int i) {
        return this.source.getMediumLE(i);
    }

    public int getUnsignedMedium(int i) {
        return this.source.getUnsignedMedium(i);
    }

    public int getUnsignedMediumLE(int i) {
        return this.source.getUnsignedMediumLE(i);
    }

    public int getInt(int i) {
        return this.source.getInt(i);
    }

    public int getIntLE(int i) {
        return this.source.getIntLE(i);
    }

    public long getUnsignedInt(int i) {
        return this.source.getUnsignedInt(i);
    }

    public long getUnsignedIntLE(int i) {
        return this.source.getUnsignedIntLE(i);
    }

    public long getLong(int i) {
        return this.source.getLong(i);
    }

    public long getLongLE(int i) {
        return this.source.getLongLE(i);
    }

    public char getChar(int i) {
        return this.source.getChar(i);
    }

    public float getFloat(int i) {
        return this.source.getFloat(i);
    }

    public double getDouble(int i) {
        return this.source.getDouble(i);
    }

    public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf) {
        this.source.getBytes(i, byteBuf);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
        this.source.getBytes(i, byteBuf, j);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
        this.source.getBytes(i, byteBuf, j, k);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, byte[] bs) {
        this.source.getBytes(i, bs);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, byte[] bs, int j, int k) {
        this.source.getBytes(i, bs, j, k);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        this.source.getBytes(i, byteBuffer);
        return this;
    }

    public FriendlyByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
        this.source.getBytes(i, outputStream, j);
        return this;
    }

    public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int j) throws IOException {
        return this.source.getBytes(i, gatheringByteChannel, j);
    }

    public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return this.source.getBytes(i, fileChannel, l, j);
    }

    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return this.source.getCharSequence(i, j, charset);
    }

    public FriendlyByteBuf setBoolean(int i, boolean bl) {
        this.source.setBoolean(i, bl);
        return this;
    }

    public FriendlyByteBuf setByte(int i, int j) {
        this.source.setByte(i, j);
        return this;
    }

    public FriendlyByteBuf setShort(int i, int j) {
        this.source.setShort(i, j);
        return this;
    }

    public FriendlyByteBuf setShortLE(int i, int j) {
        this.source.setShortLE(i, j);
        return this;
    }

    public FriendlyByteBuf setMedium(int i, int j) {
        this.source.setMedium(i, j);
        return this;
    }

    public FriendlyByteBuf setMediumLE(int i, int j) {
        this.source.setMediumLE(i, j);
        return this;
    }

    public FriendlyByteBuf setInt(int i, int j) {
        this.source.setInt(i, j);
        return this;
    }

    public FriendlyByteBuf setIntLE(int i, int j) {
        this.source.setIntLE(i, j);
        return this;
    }

    public FriendlyByteBuf setLong(int i, long l) {
        this.source.setLong(i, l);
        return this;
    }

    public FriendlyByteBuf setLongLE(int i, long l) {
        this.source.setLongLE(i, l);
        return this;
    }

    public FriendlyByteBuf setChar(int i, int j) {
        this.source.setChar(i, j);
        return this;
    }

    public FriendlyByteBuf setFloat(int i, float f) {
        this.source.setFloat(i, f);
        return this;
    }

    public FriendlyByteBuf setDouble(int i, double d) {
        this.source.setDouble(i, d);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf) {
        this.source.setBytes(i, byteBuf);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        this.source.setBytes(i, byteBuf, j);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
        this.source.setBytes(i, byteBuf, j, k);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, byte[] bs) {
        this.source.setBytes(i, bs);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, byte[] bs, int j, int k) {
        this.source.setBytes(i, bs, j, k);
        return this;
    }

    public FriendlyByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        this.source.setBytes(i, byteBuffer);
        return this;
    }

    public int setBytes(int i, InputStream inputStream, int j) throws IOException {
        return this.source.setBytes(i, inputStream, j);
    }

    public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int j) throws IOException {
        return this.source.setBytes(i, scatteringByteChannel, j);
    }

    public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return this.source.setBytes(i, fileChannel, l, j);
    }

    public FriendlyByteBuf setZero(int i, int j) {
        this.source.setZero(i, j);
        return this;
    }

    public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
        return this.source.setCharSequence(i, charSequence, charset);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int i) {
        return this.source.readBytes(i);
    }

    public ByteBuf readSlice(int i) {
        return this.source.readSlice(i);
    }

    public ByteBuf readRetainedSlice(int i) {
        return this.source.readRetainedSlice(i);
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf) {
        this.source.readBytes(byteBuf);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf, int i) {
        this.source.readBytes(byteBuf, i);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
        this.source.readBytes(byteBuf, i, j);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] bs) {
        this.source.readBytes(bs);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] bs, int i, int j) {
        this.source.readBytes(bs, i, j);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuffer byteBuffer) {
        this.source.readBytes(byteBuffer);
        return this;
    }

    public FriendlyByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        this.source.readBytes(outputStream, i);
        return this;
    }

    public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return this.source.readBytes(gatheringByteChannel, i);
    }

    public CharSequence readCharSequence(int i, Charset charset) {
        return this.source.readCharSequence(i, charset);
    }

    public String readString(int i, Charset charset) {
        return this.source.readString(i, charset);
    }

    public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.source.readBytes(fileChannel, l, i);
    }

    public FriendlyByteBuf skipBytes(int i) {
        this.source.skipBytes(i);
        return this;
    }

    public FriendlyByteBuf writeBoolean(boolean bl) {
        this.source.writeBoolean(bl);
        return this;
    }

    public FriendlyByteBuf writeByte(int i) {
        this.source.writeByte(i);
        return this;
    }

    public FriendlyByteBuf writeShort(int i) {
        this.source.writeShort(i);
        return this;
    }

    public FriendlyByteBuf writeShortLE(int i) {
        this.source.writeShortLE(i);
        return this;
    }

    public FriendlyByteBuf writeMedium(int i) {
        this.source.writeMedium(i);
        return this;
    }

    public FriendlyByteBuf writeMediumLE(int i) {
        this.source.writeMediumLE(i);
        return this;
    }

    public FriendlyByteBuf writeInt(int i) {
        this.source.writeInt(i);
        return this;
    }

    public FriendlyByteBuf writeIntLE(int i) {
        this.source.writeIntLE(i);
        return this;
    }

    public FriendlyByteBuf writeLong(long l) {
        this.source.writeLong(l);
        return this;
    }

    public FriendlyByteBuf writeLongLE(long l) {
        this.source.writeLongLE(l);
        return this;
    }

    public FriendlyByteBuf writeChar(int i) {
        this.source.writeChar(i);
        return this;
    }

    public FriendlyByteBuf writeFloat(float f) {
        this.source.writeFloat(f);
        return this;
    }

    public FriendlyByteBuf writeDouble(double d) {
        this.source.writeDouble(d);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf) {
        this.source.writeBytes(byteBuf);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int i) {
        this.source.writeBytes(byteBuf, i);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
        this.source.writeBytes(byteBuf, i, j);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] bs) {
        this.source.writeBytes(bs);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] bs, int i, int j) {
        this.source.writeBytes(bs, i, j);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuffer byteBuffer) {
        this.source.writeBytes(byteBuffer);
        return this;
    }

    public int writeBytes(InputStream inputStream, int i) throws IOException {
        return this.source.writeBytes(inputStream, i);
    }

    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return this.source.writeBytes(scatteringByteChannel, i);
    }

    public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.source.writeBytes(fileChannel, l, i);
    }

    public FriendlyByteBuf writeZero(int i) {
        this.source.writeZero(i);
        return this;
    }

    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return this.source.writeCharSequence(charSequence, charset);
    }

    public int indexOf(int i, int j, byte b) {
        return this.source.indexOf(i, j, b);
    }

    public int bytesBefore(byte b) {
        return this.source.bytesBefore(b);
    }

    public int bytesBefore(int i, byte b) {
        return this.source.bytesBefore(i, b);
    }

    public int bytesBefore(int i, int j, byte b) {
        return this.source.bytesBefore(i, j, b);
    }

    public int forEachByte(ByteProcessor byteProcessor) {
        return this.source.forEachByte(byteProcessor);
    }

    public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
        return this.source.forEachByte(i, j, byteProcessor);
    }

    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.source.forEachByteDesc(byteProcessor);
    }

    public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
        return this.source.forEachByteDesc(i, j, byteProcessor);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int i, int j) {
        return this.source.copy(i, j);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int i, int j) {
        return this.source.slice(i, j);
    }

    public ByteBuf retainedSlice(int i, int j) {
        return this.source.retainedSlice(i, j);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int i, int j) {
        return this.source.nioBuffer(i, j);
    }

    public ByteBuffer internalNioBuffer(int i, int j) {
        return this.source.internalNioBuffer(i, j);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int i, int j) {
        return this.source.nioBuffers(i, j);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.source.toString(charset);
    }

    public String toString(int i, int j, Charset charset) {
        return this.source.toString(i, j, charset);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object object) {
        return this.source.equals(object);
    }

    public int compareTo(ByteBuf byteBuf) {
        return this.source.compareTo(byteBuf);
    }

    public String toString() {
        return this.source.toString();
    }

    public FriendlyByteBuf retain(int i) {
        this.source.retain(i);
        return this;
    }

    public FriendlyByteBuf retain() {
        this.source.retain();
        return this;
    }

    public FriendlyByteBuf touch() {
        this.source.touch();
        return this;
    }

    public FriendlyByteBuf touch(Object object) {
        this.source.touch(object);
        return this;
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int i) {
        return this.source.release(i);
    }

    public /* synthetic */ ByteBuf touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ByteBuf touch() {
        return this.touch();
    }

    public /* synthetic */ ByteBuf retain() {
        return this.retain();
    }

    public /* synthetic */ ByteBuf retain(int i) {
        return this.retain(i);
    }

    public /* synthetic */ ByteBuf writeZero(int i) {
        return this.writeZero(i);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return this.writeBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] bs, int i, int j) {
        return this.writeBytes(bs, i, j);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] bs) {
        return this.writeBytes(bs);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
        return this.writeBytes(byteBuf, i, j);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int i) {
        return this.writeBytes(byteBuf, i);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf) {
        return this.writeBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf writeDouble(double d) {
        return this.writeDouble(d);
    }

    public /* synthetic */ ByteBuf writeFloat(float f) {
        return this.writeFloat(f);
    }

    public /* synthetic */ ByteBuf writeChar(int i) {
        return this.writeChar(i);
    }

    public /* synthetic */ ByteBuf writeLongLE(long l) {
        return this.writeLongLE(l);
    }

    public /* synthetic */ ByteBuf writeLong(long l) {
        return this.writeLong(l);
    }

    public /* synthetic */ ByteBuf writeIntLE(int i) {
        return this.writeIntLE(i);
    }

    public /* synthetic */ ByteBuf writeInt(int i) {
        return this.writeInt(i);
    }

    public /* synthetic */ ByteBuf writeMediumLE(int i) {
        return this.writeMediumLE(i);
    }

    public /* synthetic */ ByteBuf writeMedium(int i) {
        return this.writeMedium(i);
    }

    public /* synthetic */ ByteBuf writeShortLE(int i) {
        return this.writeShortLE(i);
    }

    public /* synthetic */ ByteBuf writeShort(int i) {
        return this.writeShort(i);
    }

    public /* synthetic */ ByteBuf writeByte(int i) {
        return this.writeByte(i);
    }

    public /* synthetic */ ByteBuf writeBoolean(boolean bl) {
        return this.writeBoolean(bl);
    }

    public /* synthetic */ ByteBuf skipBytes(int i) {
        return this.skipBytes(i);
    }

    public /* synthetic */ ByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        return this.readBytes(outputStream, i);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuffer byteBuffer) {
        return this.readBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] bs, int i, int j) {
        return this.readBytes(bs, i, j);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] bs) {
        return this.readBytes(bs);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
        return this.readBytes(byteBuf, i, j);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int i) {
        return this.readBytes(byteBuf, i);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf) {
        return this.readBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf setZero(int i, int j) {
        return this.setZero(i, j);
    }

    public /* synthetic */ ByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        return this.setBytes(i, byteBuffer);
    }

    public /* synthetic */ ByteBuf setBytes(int i, byte[] bs, int j, int k) {
        return this.setBytes(i, bs, j, k);
    }

    public /* synthetic */ ByteBuf setBytes(int i, byte[] bs) {
        return this.setBytes(i, bs);
    }

    public /* synthetic */ ByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
        return this.setBytes(i, byteBuf, j, k);
    }

    public /* synthetic */ ByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        return this.setBytes(i, byteBuf, j);
    }

    public /* synthetic */ ByteBuf setBytes(int i, ByteBuf byteBuf) {
        return this.setBytes(i, byteBuf);
    }

    public /* synthetic */ ByteBuf setDouble(int i, double d) {
        return this.setDouble(i, d);
    }

    public /* synthetic */ ByteBuf setFloat(int i, float f) {
        return this.setFloat(i, f);
    }

    public /* synthetic */ ByteBuf setChar(int i, int j) {
        return this.setChar(i, j);
    }

    public /* synthetic */ ByteBuf setLongLE(int i, long l) {
        return this.setLongLE(i, l);
    }

    public /* synthetic */ ByteBuf setLong(int i, long l) {
        return this.setLong(i, l);
    }

    public /* synthetic */ ByteBuf setIntLE(int i, int j) {
        return this.setIntLE(i, j);
    }

    public /* synthetic */ ByteBuf setInt(int i, int j) {
        return this.setInt(i, j);
    }

    public /* synthetic */ ByteBuf setMediumLE(int i, int j) {
        return this.setMediumLE(i, j);
    }

    public /* synthetic */ ByteBuf setMedium(int i, int j) {
        return this.setMedium(i, j);
    }

    public /* synthetic */ ByteBuf setShortLE(int i, int j) {
        return this.setShortLE(i, j);
    }

    public /* synthetic */ ByteBuf setShort(int i, int j) {
        return this.setShort(i, j);
    }

    public /* synthetic */ ByteBuf setByte(int i, int j) {
        return this.setByte(i, j);
    }

    public /* synthetic */ ByteBuf setBoolean(int i, boolean bl) {
        return this.setBoolean(i, bl);
    }

    public /* synthetic */ ByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
        return this.getBytes(i, outputStream, j);
    }

    public /* synthetic */ ByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        return this.getBytes(i, byteBuffer);
    }

    public /* synthetic */ ByteBuf getBytes(int i, byte[] bs, int j, int k) {
        return this.getBytes(i, bs, j, k);
    }

    public /* synthetic */ ByteBuf getBytes(int i, byte[] bs) {
        return this.getBytes(i, bs);
    }

    public /* synthetic */ ByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
        return this.getBytes(i, byteBuf, j, k);
    }

    public /* synthetic */ ByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
        return this.getBytes(i, byteBuf, j);
    }

    public /* synthetic */ ByteBuf getBytes(int i, ByteBuf byteBuf) {
        return this.getBytes(i, byteBuf);
    }

    public /* synthetic */ ByteBuf ensureWritable(int i) {
        return this.ensureWritable(i);
    }

    public /* synthetic */ ByteBuf discardSomeReadBytes() {
        return this.discardSomeReadBytes();
    }

    public /* synthetic */ ByteBuf discardReadBytes() {
        return this.discardReadBytes();
    }

    public /* synthetic */ ByteBuf resetWriterIndex() {
        return this.resetWriterIndex();
    }

    public /* synthetic */ ByteBuf markWriterIndex() {
        return this.markWriterIndex();
    }

    public /* synthetic */ ByteBuf resetReaderIndex() {
        return this.resetReaderIndex();
    }

    public /* synthetic */ ByteBuf markReaderIndex() {
        return this.markReaderIndex();
    }

    public /* synthetic */ ByteBuf clear() {
        return this.clear();
    }

    public /* synthetic */ ByteBuf setIndex(int i, int j) {
        return this.setIndex(i, j);
    }

    public /* synthetic */ ByteBuf writerIndex(int i) {
        return this.writerIndex(i);
    }

    public /* synthetic */ ByteBuf readerIndex(int i) {
        return this.readerIndex(i);
    }

    public /* synthetic */ ByteBuf capacity(int i) {
        return this.capacity(i);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int i) {
        return this.retain(i);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

