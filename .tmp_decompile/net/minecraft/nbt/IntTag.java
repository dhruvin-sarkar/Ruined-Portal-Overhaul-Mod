/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public record IntTag(int value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 12;
    public static final TagType<IntTag> TYPE = new TagType.StaticSize<IntTag>(){

        @Override
        public IntTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return IntTag.valueOf(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static int readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(12L);
            return dataInput.readInt();
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public String getName() {
            return "INT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };

    public static IntTag valueOf(int i) {
        if (i >= -128 && i <= 1024) {
            return Cache.cache[i - -128];
        }
        return new IntTag(i);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 12;
    }

    @Override
    public byte getId() {
        return 3;
    }

    public TagType<IntTag> getType() {
        return TYPE;
    }

    @Override
    public IntTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitInt(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return (short)(this.value & 0xFFFF);
    }

    @Override
    public byte byteValue() {
        return (byte)(this.value & 0xFF);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public Number box() {
        return this.value;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.value);
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitInt(this);
        return stringTagVisitor.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    static class Cache {
        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final IntTag[] cache = new IntTag[1153];

        private Cache() {
        }

        static {
            for (int i = 0; i < cache.length; ++i) {
                Cache.cache[i] = new IntTag(-128 + i);
            }
        }
    }
}

