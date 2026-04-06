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
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class ByteArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>(){

        @Override
        public ByteArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new ByteArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static byte[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int i = dataInput.readInt();
            nbtAccounter.accountBytes(1L, i);
            byte[] bs = new byte[i];
            dataInput.readFully(bs);
            return bs;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] bs) {
        this.data = bs;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        dataOutput.write(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 24 + 1 * this.data.length;
    }

    @Override
    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitByteArray(this);
        return stringTagVisitor.build();
    }

    @Override
    public Tag copy() {
        byte[] bs = new byte[this.data.length];
        System.arraycopy(this.data, 0, bs, 0, this.data.length);
        return new ByteArrayTag(bs);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitByteArray(this);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public ByteTag get(int i) {
        return ByteTag.valueOf(this.data[i]);
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data[i] = numericTag.byteValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data = ArrayUtils.add((byte[])this.data, (int)i, (byte)numericTag.byteValue());
            return true;
        }
        return false;
    }

    @Override
    public ByteTag remove(int i) {
        byte b = this.data[i];
        this.data = ArrayUtils.remove((byte[])this.data, (int)i);
        return ByteTag.valueOf(b);
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    public Optional<byte[]> asByteArray() {
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
}

