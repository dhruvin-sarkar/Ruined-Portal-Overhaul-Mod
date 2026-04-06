/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;

public class StringTagVisitor
implements TagVisitor {
    private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
    private final StringBuilder builder = new StringBuilder();

    public String build() {
        return this.builder.toString();
    }

    @Override
    public void visitString(StringTag stringTag) {
        this.builder.append(StringTag.quoteAndEscape(stringTag.value()));
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        this.builder.append(byteTag.value()).append('b');
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        this.builder.append(shortTag.value()).append('s');
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.builder.append(intTag.value());
    }

    @Override
    public void visitLong(LongTag longTag) {
        this.builder.append(longTag.value()).append('L');
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        this.builder.append(floatTag.value()).append('f');
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        this.builder.append(doubleTag.value()).append('d');
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        this.builder.append("[B;");
        byte[] bs = byteArrayTag.getAsByteArray();
        for (int i = 0; i < bs.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(bs[i]).append('B');
        }
        this.builder.append(']');
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        this.builder.append("[I;");
        int[] is = intArrayTag.getAsIntArray();
        for (int i = 0; i < is.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(is[i]);
        }
        this.builder.append(']');
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        this.builder.append("[L;");
        long[] ls = longArrayTag.getAsLongArray();
        for (int i = 0; i < ls.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(ls[i]).append('L');
        }
        this.builder.append(']');
    }

    @Override
    public void visitList(ListTag listTag) {
        this.builder.append('[');
        for (int i = 0; i < listTag.size(); ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            listTag.get(i).accept(this);
        }
        this.builder.append(']');
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        this.builder.append('{');
        ArrayList<Map.Entry<String, Tag>> list = new ArrayList<Map.Entry<String, Tag>>(compoundTag.entrySet());
        list.sort(Map.Entry.comparingByKey());
        for (int i = 0; i < list.size(); ++i) {
            Map.Entry entry = (Map.Entry)list.get(i);
            if (i != 0) {
                this.builder.append(',');
            }
            this.handleKeyEscape((String)entry.getKey());
            this.builder.append(':');
            ((Tag)entry.getValue()).accept(this);
        }
        this.builder.append('}');
    }

    private void handleKeyEscape(String string) {
        if (!string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false") && UNQUOTED_KEY_MATCH.matcher(string).matches()) {
            this.builder.append(string);
        } else {
            StringTag.quoteAndEscape(string, this.builder);
        }
    }

    @Override
    public void visitEnd(EndTag endTag) {
        this.builder.append("END");
    }
}

