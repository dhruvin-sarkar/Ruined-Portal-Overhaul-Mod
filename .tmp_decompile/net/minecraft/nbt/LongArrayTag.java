/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class LongArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>(){

        @Override
        public LongArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new LongArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static long[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int i = dataInput.readInt();
            nbtAccounter.accountBytes(8L, i);
            long[] ls = new long[i];
            for (int j = 0; j < i; ++j) {
                ls[j] = dataInput.readLong();
            }
            return ls;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 8);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private long[] data;

    public LongArrayTag(long[] ls) {
        this.data = ls;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (long l : this.data) {
            dataOutput.writeLong(l);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 8 * this.data.length;
    }

    @Override
    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitLongArray(this);
        return stringTagVisitor.build();
    }

    @Override
    public LongArrayTag copy() {
        long[] ls = new long[this.data.length];
        System.arraycopy(this.data, 0, ls, 0, this.data.length);
        return new LongArrayTag(ls);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitLongArray(this);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public LongTag get(int i) {
        return LongTag.valueOf(this.data[i]);
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data[i] = numericTag.longValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data = ArrayUtils.add((long[])this.data, (int)i, (long)numericTag.longValue());
            return true;
        }
        return false;
    }

    @Override
    public LongTag remove(int i) {
        long l = this.data[i];
        this.data = ArrayUtils.remove((long[])this.data, (int)i);
        return LongTag.valueOf(l);
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public Optional<long[]> asLongArray() {
        return Optional.of(this.data);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag get(int i) {
        return this.get(i);
    }

    @Override
    public /* synthetic */ Tag remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

