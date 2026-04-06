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
import net.minecraft.util.Mth;

public record FloatTag(float value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 12;
    public static final FloatTag ZERO = new FloatTag(0.0f);
    public static final TagType<FloatTag> TYPE = new TagType.StaticSize<FloatTag>(){

        @Override
        public FloatTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return FloatTag.valueOf(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static float readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(12L);
            return dataInput.readFloat();
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public String getName() {
            return "FLOAT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Float";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };

    public static FloatTag valueOf(float f) {
        if (f == 0.0f) {
            return ZERO;
        }
        return new FloatTag(f);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeFloat(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 12;
    }

    @Override
    public byte getId() {
        return 5;
    }

    public TagType<FloatTag> getType() {
        return TYPE;
    }

    @Override
    public FloatTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitFloat(this);
    }

    @Override
    public long longValue() {
        return (long)this.value;
    }

    @Override
    public int intValue() {
        return Mth.floor(this.value);
    }

    @Override
    public short shortValue() {
        return (short)(Mth.floor(this.value) & 0xFFFF);
    }

    @Override
    public byte byteValue() {
        return (byte)(Mth.floor(this.value) & 0xFF);
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
        return Float.valueOf(this.value);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.value);
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitFloat(this);
        return stringTagVisitor.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

