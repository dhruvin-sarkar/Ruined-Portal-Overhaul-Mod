/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class CompoundTag
implements Tag {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        Tag tag = (Tag)dynamic.convert((DynamicOps)NbtOps.INSTANCE).getValue();
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success((Object)(compoundTag == dynamic.getValue() ? compoundTag.copy() : compoundTag));
        }
        return DataResult.error(() -> "Not a compound tag: " + String.valueOf(tag));
    }, compoundTag -> new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.copy()));
    private static final int SELF_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public CompoundTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                CompoundTag compoundTag = 1.loadCompound(dataInput, nbtAccounter);
                return compoundTag;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static CompoundTag loadCompound(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            byte b;
            nbtAccounter.accountBytes(48L);
            HashMap map = Maps.newHashMap();
            while ((b = dataInput.readByte()) != 0) {
                Tag tag;
                String string = 1.readString(dataInput, nbtAccounter);
                if (map.put(string, tag = CompoundTag.readNamedTagData(TagTypes.getType(b), string, dataInput, nbtAccounter)) != null) continue;
                nbtAccounter.accountBytes(36L);
            }
            return new CompoundTag(map);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseCompound(dataInput, streamTagVisitor, nbtAccounter);
                return valueResult;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static StreamTagVisitor.ValueResult parseCompound(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            byte b;
            nbtAccounter.accountBytes(48L);
            block13: while ((b = dataInput.readByte()) != 0) {
                TagType<?> tagType = TagTypes.getType(b);
                switch (streamTagVisitor.visitEntry(tagType)) {
                    case HALT: {
                        return StreamTagVisitor.ValueResult.HALT;
                    }
                    case BREAK: {
                        StringTag.skipString(dataInput);
                        tagType.skip(dataInput, nbtAccounter);
                        break block13;
                    }
                    case SKIP: {
                        StringTag.skipString(dataInput);
                        tagType.skip(dataInput, nbtAccounter);
                        continue block13;
                    }
                    default: {
                        String string = 1.readString(dataInput, nbtAccounter);
                        switch (streamTagVisitor.visitEntry(tagType, string)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                            case BREAK: {
                                tagType.skip(dataInput, nbtAccounter);
                                break block13;
                            }
                            case SKIP: {
                                tagType.skip(dataInput, nbtAccounter);
                                continue block13;
                            }
                        }
                        nbtAccounter.accountBytes(36L);
                        switch (tagType.parse(dataInput, streamTagVisitor, nbtAccounter)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                        }
                        continue block13;
                    }
                }
            }
            if (b != 0) {
                while ((b = dataInput.readByte()) != 0) {
                    StringTag.skipString(dataInput);
                    TagTypes.getType(b).skip(dataInput, nbtAccounter);
                }
            }
            return streamTagVisitor.visitContainerEnd();
        }

        private static String readString(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            String string = dataInput.readUTF();
            nbtAccounter.accountBytes(28L);
            nbtAccounter.accountBytes(2L, string.length());
            return string;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                byte b;
                while ((b = dataInput.readByte()) != 0) {
                    StringTag.skipString(dataInput);
                    TagTypes.getType(b).skip(dataInput, nbtAccounter);
                }
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final Map<String, Tag> tags;

    CompoundTag(Map<String, Tag> map) {
        this.tags = map;
    }

    public CompoundTag() {
        this(new HashMap<String, Tag>());
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        for (String string : this.tags.keySet()) {
            Tag tag = this.tags.get(string);
            CompoundTag.writeNamedTag(string, tag, dataOutput);
        }
        dataOutput.writeByte(0);
    }

    @Override
    public int sizeInBytes() {
        int i = 48;
        for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
            i += 28 + 2 * entry.getKey().length();
            i += 36;
            i += entry.getValue().sizeInBytes();
        }
        return i;
    }

    public Set<String> keySet() {
        return this.tags.keySet();
    }

    public Set<Map.Entry<String, Tag>> entrySet() {
        return this.tags.entrySet();
    }

    public Collection<Tag> values() {
        return this.tags.values();
    }

    public void forEach(BiConsumer<String, Tag> biConsumer) {
        this.tags.forEach(biConsumer);
    }

    @Override
    public byte getId() {
        return 10;
    }

    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    public @Nullable Tag put(String string, Tag tag) {
        return this.tags.put(string, tag);
    }

    public void putByte(String string, byte b) {
        this.tags.put(string, ByteTag.valueOf(b));
    }

    public void putShort(String string, short s) {
        this.tags.put(string, ShortTag.valueOf(s));
    }

    public void putInt(String string, int i) {
        this.tags.put(string, IntTag.valueOf(i));
    }

    public void putLong(String string, long l) {
        this.tags.put(string, LongTag.valueOf(l));
    }

    public void putFloat(String string, float f) {
        this.tags.put(string, FloatTag.valueOf(f));
    }

    public void putDouble(String string, double d) {
        this.tags.put(string, DoubleTag.valueOf(d));
    }

    public void putString(String string, String string2) {
        this.tags.put(string, StringTag.valueOf(string2));
    }

    public void putByteArray(String string, byte[] bs) {
        this.tags.put(string, new ByteArrayTag(bs));
    }

    public void putIntArray(String string, int[] is) {
        this.tags.put(string, new IntArrayTag(is));
    }

    public void putLongArray(String string, long[] ls) {
        this.tags.put(string, new LongArrayTag(ls));
    }

    public void putBoolean(String string, boolean bl) {
        this.tags.put(string, ByteTag.valueOf(bl));
    }

    public @Nullable Tag get(String string) {
        return this.tags.get(string);
    }

    public boolean contains(String string) {
        return this.tags.containsKey(string);
    }

    private Optional<Tag> getOptional(String string) {
        return Optional.ofNullable(this.tags.get(string));
    }

    public Optional<Byte> getByte(String string) {
        return this.getOptional(string).flatMap(Tag::asByte);
    }

    public byte getByteOr(String string, byte b) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.byteValue();
        }
        return b;
    }

    public Optional<Short> getShort(String string) {
        return this.getOptional(string).flatMap(Tag::asShort);
    }

    public short getShortOr(String string, short s) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.shortValue();
        }
        return s;
    }

    public Optional<Integer> getInt(String string) {
        return this.getOptional(string).flatMap(Tag::asInt);
    }

    public int getIntOr(String string, int i) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.intValue();
        }
        return i;
    }

    public Optional<Long> getLong(String string) {
        return this.getOptional(string).flatMap(Tag::asLong);
    }

    public long getLongOr(String string, long l) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.longValue();
        }
        return l;
    }

    public Optional<Float> getFloat(String string) {
        return this.getOptional(string).flatMap(Tag::asFloat);
    }

    public float getFloatOr(String string, float f) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.floatValue();
        }
        return f;
    }

    public Optional<Double> getDouble(String string) {
        return this.getOptional(string).flatMap(Tag::asDouble);
    }

    public double getDoubleOr(String string, double d) {
        Tag tag = this.tags.get(string);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.doubleValue();
        }
        return d;
    }

    public Optional<String> getString(String string) {
        return this.getOptional(string).flatMap(Tag::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getStringOr(String string, String string2) {
        Tag tag = this.tags.get(string);
        if (!(tag instanceof StringTag)) return string2;
        StringTag stringTag = (StringTag)tag;
        try {
            String string3 = stringTag.value();
            return string3;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public Optional<byte[]> getByteArray(String string) {
        Tag tag = this.tags.get(string);
        if (tag instanceof ByteArrayTag) {
            ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
            return Optional.of(byteArrayTag.getAsByteArray());
        }
        return Optional.empty();
    }

    public Optional<int[]> getIntArray(String string) {
        Tag tag = this.tags.get(string);
        if (tag instanceof IntArrayTag) {
            IntArrayTag intArrayTag = (IntArrayTag)tag;
            return Optional.of(intArrayTag.getAsIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(String string) {
        Tag tag = this.tags.get(string);
        if (tag instanceof LongArrayTag) {
            LongArrayTag longArrayTag = (LongArrayTag)tag;
            return Optional.of(longArrayTag.getAsLongArray());
        }
        return Optional.empty();
    }

    public Optional<CompoundTag> getCompound(String string) {
        Tag tag = this.tags.get(string);
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return Optional.of(compoundTag);
        }
        return Optional.empty();
    }

    public CompoundTag getCompoundOrEmpty(String string) {
        return this.getCompound(string).orElseGet(CompoundTag::new);
    }

    public Optional<ListTag> getList(String string) {
        Tag tag = this.tags.get(string);
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            return Optional.of(listTag);
        }
        return Optional.empty();
    }

    public ListTag getListOrEmpty(String string) {
        return this.getList(string).orElseGet(ListTag::new);
    }

    public Optional<Boolean> getBoolean(String string) {
        return this.getOptional(string).flatMap(Tag::asBoolean);
    }

    public boolean getBooleanOr(String string, boolean bl) {
        return this.getByteOr(string, bl ? (byte)1 : 0) != 0;
    }

    public @Nullable Tag remove(String string) {
        return this.tags.remove(string);
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitCompound(this);
        return stringTagVisitor.build();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    protected CompoundTag shallowCopy() {
        return new CompoundTag(new HashMap<String, Tag>(this.tags));
    }

    @Override
    public CompoundTag copy() {
        HashMap<String, Tag> hashMap = new HashMap<String, Tag>();
        this.tags.forEach((? super K string, ? super V tag) -> hashMap.put((String)string, tag.copy()));
        return new CompoundTag(hashMap);
    }

    @Override
    public Optional<CompoundTag> asCompound() {
        return Optional.of(this);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)object).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        dataOutput.writeUTF(string);
        tag.write(dataOutput);
    }

    static Tag readNamedTagData(TagType<?> tagType, String string, DataInput dataInput, NbtAccounter nbtAccounter) {
        try {
            return tagType.load(dataInput, nbtAccounter);
        }
        catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag name", string);
            crashReportCategory.setDetail("Tag type", tagType.getName());
            throw new ReportedNbtException(crashReport);
        }
    }

    public CompoundTag merge(CompoundTag compoundTag) {
        for (String string : compoundTag.tags.keySet()) {
            Tag tag = compoundTag.tags.get(string);
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag)tag;
                Tag tag2 = this.tags.get(string);
                if (tag2 instanceof CompoundTag) {
                    CompoundTag compoundTag3 = (CompoundTag)tag2;
                    compoundTag3.merge(compoundTag2);
                    continue;
                }
            }
            this.put(string, tag.copy());
        }
        return this;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitCompound(this);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        block14: for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
            Tag tag = entry.getValue();
            TagType<?> tagType = tag.getType();
            StreamTagVisitor.EntryResult entryResult = streamTagVisitor.visitEntry(tagType);
            switch (entryResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            entryResult = streamTagVisitor.visitEntry(tagType, entry.getKey());
            switch (entryResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            StreamTagVisitor.ValueResult valueResult = tag.accept(streamTagVisitor);
            switch (valueResult) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
            }
        }
        return streamTagVisitor.visitContainerEnd();
    }

    public <T> void store(String string, Codec<T> codec, T object) {
        this.store(string, codec, NbtOps.INSTANCE, object);
    }

    public <T> void storeNullable(String string, Codec<T> codec, @Nullable T object) {
        if (object != null) {
            this.store(string, codec, object);
        }
    }

    public <T> void store(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps, T object) {
        this.put(string, (Tag)codec.encodeStart(dynamicOps, object).getOrThrow());
    }

    public <T> void storeNullable(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps, @Nullable T object) {
        if (object != null) {
            this.store(string, codec, dynamicOps, object);
        }
    }

    public <T> void store(MapCodec<T> mapCodec, T object) {
        this.store(mapCodec, NbtOps.INSTANCE, object);
    }

    public <T> void store(MapCodec<T> mapCodec, DynamicOps<Tag> dynamicOps, T object) {
        this.merge((CompoundTag)mapCodec.encoder().encodeStart(dynamicOps, object).getOrThrow());
    }

    public <T> Optional<T> read(String string, Codec<T> codec) {
        return this.read(string, codec, NbtOps.INSTANCE);
    }

    public <T> Optional<T> read(String string, Codec<T> codec, DynamicOps<Tag> dynamicOps) {
        Tag tag = this.get(string);
        if (tag == null) {
            return Optional.empty();
        }
        return codec.parse(dynamicOps, (Object)tag).resultOrPartial(string2 -> LOGGER.error("Failed to read field ({}={}): {}", new Object[]{string, tag, string2}));
    }

    public <T> Optional<T> read(MapCodec<T> mapCodec) {
        return this.read(mapCodec, NbtOps.INSTANCE);
    }

    public <T> Optional<T> read(MapCodec<T> mapCodec, DynamicOps<Tag> dynamicOps) {
        return mapCodec.decode(dynamicOps, (MapLike)dynamicOps.getMap((Object)this).getOrThrow()).resultOrPartial(string -> LOGGER.error("Failed to read value ({}): {}", (Object)this, string));
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

