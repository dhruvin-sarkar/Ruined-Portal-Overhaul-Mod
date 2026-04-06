/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  java.lang.MatchException
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class FilterMask {
    public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(FilterMask::type, Type::codec);
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), Type.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.filtered")));
    static final MapCodec<FilterMask> PASS_THROUGH_CODEC = MapCodec.unit((Object)PASS_THROUGH);
    static final MapCodec<FilterMask> FULLY_FILTERED_CODEC = MapCodec.unit((Object)FULLY_FILTERED);
    static final MapCodec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask).fieldOf("value");
    private static final char HASH = '#';
    private final BitSet mask;
    private final Type type;

    private FilterMask(BitSet bitSet, Type type) {
        this.mask = bitSet;
        this.type = type;
    }

    private FilterMask(BitSet bitSet) {
        this.mask = bitSet;
        this.type = Type.PARTIALLY_FILTERED;
    }

    public FilterMask(int i) {
        this(new BitSet(i), Type.PARTIALLY_FILTERED);
    }

    private Type type() {
        return this.type;
    }

    private BitSet mask() {
        return this.mask;
    }

    public static FilterMask read(FriendlyByteBuf friendlyByteBuf) {
        Type type = friendlyByteBuf.readEnum(Type.class);
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PASS_THROUGH;
            case 1 -> FULLY_FILTERED;
            case 2 -> new FilterMask(friendlyByteBuf.readBitSet(), Type.PARTIALLY_FILTERED);
        };
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, FilterMask filterMask) {
        friendlyByteBuf.writeEnum(filterMask.type);
        if (filterMask.type == Type.PARTIALLY_FILTERED) {
            friendlyByteBuf.writeBitSet(filterMask.mask);
        }
    }

    public void setFiltered(int i) {
        this.mask.set(i);
    }

    public @Nullable String apply(String string) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> string;
            case 2 -> {
                char[] cs = string.toCharArray();
                for (int i = 0; i < cs.length && i < this.mask.length(); ++i) {
                    if (!this.mask.get(i)) continue;
                    cs[i] = 35;
                }
                yield new String(cs);
            }
        };
    }

    public @Nullable Component applyWithFormatting(String string) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> Component.literal(string);
            case 2 -> {
                MutableComponent mutableComponent = Component.empty();
                int i = 0;
                boolean bl = this.mask.get(0);
                while (true) {
                    int j = bl ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
                    int v1 = j = j < 0 ? string.length() : j;
                    if (j == i) break;
                    if (bl) {
                        mutableComponent.append(Component.literal(StringUtils.repeat((char)'#', (int)(j - i))).withStyle(FILTERED_STYLE));
                    } else {
                        mutableComponent.append(string.substring(i, j));
                    }
                    bl = !bl;
                    i = j;
                }
                yield mutableComponent;
            }
        };
    }

    public boolean isEmpty() {
        return this.type == Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == Type.FULLY_FILTERED;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        FilterMask filterMask = (FilterMask)object;
        return this.mask.equals(filterMask.mask) && this.type == filterMask.type;
    }

    public int hashCode() {
        int i = this.mask.hashCode();
        i = 31 * i + this.type.hashCode();
        return i;
    }

    static enum Type implements StringRepresentable
    {
        PASS_THROUGH("pass_through", () -> PASS_THROUGH_CODEC),
        FULLY_FILTERED("fully_filtered", () -> FULLY_FILTERED_CODEC),
        PARTIALLY_FILTERED("partially_filtered", () -> PARTIALLY_FILTERED_CODEC);

        private final String serializedName;
        private final Supplier<MapCodec<FilterMask>> codec;

        private Type(String string2, Supplier<MapCodec<FilterMask>> supplier) {
            this.serializedName = string2;
            this.codec = supplier;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        private MapCodec<FilterMask> codec() {
            return this.codec.get();
        }
    }
}

