/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;
import org.jspecify.annotations.Nullable;

public final class ListTag
extends AbstractList<Tag>
implements CollectionTag {
    private static final String WRAPPER_MARKER = "";
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public ListTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                ListTag listTag = 1.loadList(dataInput, nbtAccounter);
                return listTag;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        private static ListTag loadList(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(36L);
            byte b = dataInput.readByte();
            int i = 1.readListCount(dataInput);
            if (b == 0 && i > 0) {
                throw new NbtFormatException("Missing type on ListTag");
            }
            nbtAccounter.accountBytes(4L, i);
            TagType<?> tagType = TagTypes.getType(b);
            ListTag listTag = new ListTag(new ArrayList<Tag>(i));
            for (int j = 0; j < i; ++j) {
                listTag.addAndUnwrap((Tag)tagType.load(dataInput, nbtAccounter));
            }
            return listTag;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseList(dataInput, streamTagVisitor, nbtAccounter);
                return valueResult;
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        /*
         * Exception decompiling
         */
        private static StreamTagVisitor.ValueResult parseList(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[SWITCH], 8[CASE]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        private static int readListCount(DataInput dataInput) throws IOException {
            int i = dataInput.readInt();
            if (i < 0) {
                throw new NbtFormatException("ListTag length cannot be negative: " + i);
            }
            return i;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.pushDepth();
            try {
                TagType<?> tagType = TagTypes.getType(dataInput.readByte());
                int i = dataInput.readInt();
                tagType.skip(dataInput, i, nbtAccounter);
            }
            finally {
                nbtAccounter.popDepth();
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final List<Tag> list;

    public ListTag() {
        this(new ArrayList<Tag>());
    }

    ListTag(List<Tag> list) {
        this.list = list;
    }

    private static Tag tryUnwrap(CompoundTag compoundTag) {
        Tag tag;
        if (compoundTag.size() == 1 && (tag = compoundTag.get(WRAPPER_MARKER)) != null) {
            return tag;
        }
        return compoundTag;
    }

    private static boolean isWrapper(CompoundTag compoundTag) {
        return compoundTag.size() == 1 && compoundTag.contains(WRAPPER_MARKER);
    }

    private static Tag wrapIfNeeded(byte b, Tag tag) {
        CompoundTag compoundTag;
        if (b != 10) {
            return tag;
        }
        if (tag instanceof CompoundTag && !ListTag.isWrapper(compoundTag = (CompoundTag)tag)) {
            return compoundTag;
        }
        return ListTag.wrapElement(tag);
    }

    private static CompoundTag wrapElement(Tag tag) {
        return new CompoundTag(Map.of((Object)WRAPPER_MARKER, (Object)tag));
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        byte b = this.identifyRawElementType();
        dataOutput.writeByte(b);
        dataOutput.writeInt(this.list.size());
        for (Tag tag : this.list) {
            ListTag.wrapIfNeeded(b, tag).write(dataOutput);
        }
    }

    @VisibleForTesting
    byte identifyRawElementType() {
        byte b = 0;
        for (Tag tag : this.list) {
            byte c = tag.getId();
            if (b == 0) {
                b = c;
                continue;
            }
            if (b == c) continue;
            return 10;
        }
        return b;
    }

    public void addAndUnwrap(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            this.add(ListTag.tryUnwrap(compoundTag));
        } else {
            this.add(tag);
        }
    }

    @Override
    public int sizeInBytes() {
        int i = 36;
        i += 4 * this.list.size();
        for (Tag tag : this.list) {
            i += tag.sizeInBytes();
        }
        return i;
    }

    @Override
    public byte getId() {
        return 9;
    }

    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitList(this);
        return stringTagVisitor.build();
    }

    @Override
    public Tag remove(int i) {
        return this.list.remove(i);
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public Optional<CompoundTag> getCompound(int i) {
        Tag tag = this.getNullable(i);
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return Optional.of(compoundTag);
        }
        return Optional.empty();
    }

    public CompoundTag getCompoundOrEmpty(int i) {
        return this.getCompound(i).orElseGet(CompoundTag::new);
    }

    public Optional<ListTag> getList(int i) {
        Tag tag = this.getNullable(i);
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            return Optional.of(listTag);
        }
        return Optional.empty();
    }

    public ListTag getListOrEmpty(int i) {
        return this.getList(i).orElseGet(ListTag::new);
    }

    public Optional<Short> getShort(int i) {
        return this.getOptional(i).flatMap(Tag::asShort);
    }

    public short getShortOr(int i, short s) {
        Tag tag = this.getNullable(i);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.shortValue();
        }
        return s;
    }

    public Optional<Integer> getInt(int i) {
        return this.getOptional(i).flatMap(Tag::asInt);
    }

    public int getIntOr(int i, int j) {
        Tag tag = this.getNullable(i);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.intValue();
        }
        return j;
    }

    public Optional<int[]> getIntArray(int i) {
        Tag tag = this.getNullable(i);
        if (tag instanceof IntArrayTag) {
            IntArrayTag intArrayTag = (IntArrayTag)tag;
            return Optional.of(intArrayTag.getAsIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(int i) {
        Tag tag = this.getNullable(i);
        if (tag instanceof LongArrayTag) {
            LongArrayTag longArrayTag = (LongArrayTag)tag;
            return Optional.of(longArrayTag.getAsLongArray());
        }
        return Optional.empty();
    }

    public Optional<Double> getDouble(int i) {
        return this.getOptional(i).flatMap(Tag::asDouble);
    }

    public double getDoubleOr(int i, double d) {
        Tag tag = this.getNullable(i);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.doubleValue();
        }
        return d;
    }

    public Optional<Float> getFloat(int i) {
        return this.getOptional(i).flatMap(Tag::asFloat);
    }

    public float getFloatOr(int i, float f) {
        Tag tag = this.getNullable(i);
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag.floatValue();
        }
        return f;
    }

    public Optional<String> getString(int i) {
        return this.getOptional(i).flatMap(Tag::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getStringOr(int i, String string) {
        Tag tag = this.getNullable(i);
        if (!(tag instanceof StringTag)) return string;
        StringTag stringTag = (StringTag)tag;
        try {
            String string2 = stringTag.value();
            return string2;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private @Nullable Tag getNullable(int i) {
        return i >= 0 && i < this.list.size() ? this.list.get(i) : null;
    }

    private Optional<Tag> getOptional(int i) {
        return Optional.ofNullable(this.getNullable(i));
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public Tag get(int i) {
        return this.list.get(i);
    }

    @Override
    public Tag set(int i, Tag tag) {
        return this.list.set(i, tag);
    }

    @Override
    public void add(int i, Tag tag) {
        this.list.add(i, tag);
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        this.list.set(i, tag);
        return true;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        this.list.add(i, tag);
        return true;
    }

    @Override
    public ListTag copy() {
        ArrayList<Tag> list = new ArrayList<Tag>(this.list.size());
        for (Tag tag : this.list) {
            list.add(tag.copy());
        }
        return new ListTag(list);
    }

    @Override
    public Optional<ListTag> asList() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ListTag && Objects.equals(this.list, ((ListTag)object).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public Stream<Tag> stream() {
        return super.stream();
    }

    public Stream<CompoundTag> compoundStream() {
        return this.stream().mapMulti((tag, consumer) -> {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                consumer.accept(compoundTag);
            }
        });
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitList(this);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        byte b = this.identifyRawElementType();
        switch (streamTagVisitor.visitList(TagTypes.getType(b), this.list.size())) {
            case HALT: {
                return StreamTagVisitor.ValueResult.HALT;
            }
            case BREAK: {
                return streamTagVisitor.visitContainerEnd();
            }
        }
        block13: for (int i = 0; i < this.list.size(); ++i) {
            Tag tag = ListTag.wrapIfNeeded(b, this.list.get(i));
            switch (streamTagVisitor.visitElement(tag.getType(), i)) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return streamTagVisitor.visitContainerEnd();
                }
                default: {
                    switch (tag.accept(streamTagVisitor)) {
                        case HALT: {
                            return StreamTagVisitor.ValueResult.HALT;
                        }
                        case BREAK: {
                            return streamTagVisitor.visitContainerEnd();
                        }
                    }
                }
            }
        }
        return streamTagVisitor.visitContainerEnd();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (Tag)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (Tag)object);
    }

    @Override
    public /* synthetic */ Object get(int i) {
        return this.get(i);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

