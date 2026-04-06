/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public record StringTag(String value) implements PrimitiveTag
{
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>(){

        @Override
        public StringTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return StringTag.valueOf(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static String readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(36L);
            String string = dataInput.readUTF();
            nbtAccounter.accountBytes(2L, string.length());
            return string;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            StringTag.skipString(dataInput);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '\"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';

    public static void skipString(DataInput dataInput) throws IOException {
        dataInput.skipBytes(dataInput.readUnsignedShort());
    }

    public static StringTag valueOf(String string) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        return new StringTag(string);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 36 + 2 * this.value.length();
    }

    @Override
    public byte getId() {
        return 8;
    }

    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitString(this);
        return stringTagVisitor.build();
    }

    @Override
    public StringTag copy() {
        return this;
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(this.value);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitString(this);
    }

    public static String quoteAndEscape(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        StringTag.quoteAndEscape(string, stringBuilder);
        return stringBuilder.toString();
    }

    public static void quoteAndEscape(String string, StringBuilder stringBuilder) {
        int i = stringBuilder.length();
        stringBuilder.append(' ');
        int c = 0;
        for (int j = 0; j < string.length(); ++j) {
            int d = string.charAt(j);
            if (d == 92) {
                stringBuilder.append("\\\\");
                continue;
            }
            if (d == 34 || d == 39) {
                if (c == 0) {
                    int n = c = d == 34 ? 39 : 34;
                }
                if (c == d) {
                    stringBuilder.append('\\');
                }
                stringBuilder.append((char)d);
                continue;
            }
            String string2 = SnbtGrammar.escapeControlCharacters((char)d);
            if (string2 != null) {
                stringBuilder.append('\\');
                stringBuilder.append(string2);
                continue;
            }
            stringBuilder.append((char)d);
        }
        if (c == 0) {
            c = 34;
        }
        stringBuilder.setCharAt(i, (char)c);
        stringBuilder.append((char)c);
    }

    public static String escapeWithoutQuotes(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        StringTag.escapeWithoutQuotes(string, stringBuilder);
        return stringBuilder.toString();
    }

    public static void escapeWithoutQuotes(String string, StringBuilder stringBuilder) {
        block3: for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '\"': 
                case '\'': 
                case '\\': {
                    stringBuilder.append('\\');
                    stringBuilder.append(c);
                    continue block3;
                }
                default: {
                    String string2 = SnbtGrammar.escapeControlCharacters(c);
                    if (string2 != null) {
                        stringBuilder.append('\\');
                        stringBuilder.append(string2);
                        continue block3;
                    }
                    stringBuilder.append(c);
                }
            }
        }
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.value);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

