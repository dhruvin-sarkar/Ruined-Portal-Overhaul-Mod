/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.Property;

public final class EnumProperty<T extends Enum<T>>
extends Property<T> {
    private final List<T> values;
    private final Map<String, T> names;
    private final int[] ordinalToIndex;

    private EnumProperty(String string, Class<T> class_, List<T> list) {
        super(string, class_);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Trying to make empty EnumProperty '" + string + "'");
        }
        this.values = List.copyOf(list);
        Enum[] enums = (Enum[])class_.getEnumConstants();
        this.ordinalToIndex = new int[enums.length];
        for (Enum enum_ : enums) {
            this.ordinalToIndex[enum_.ordinal()] = list.indexOf(enum_);
        }
        ImmutableMap.Builder builder = ImmutableMap.builder();
        for (Enum enum2 : list) {
            String string2 = ((StringRepresentable)((Object)enum2)).getSerializedName();
            builder.put((Object)string2, (Object)enum2);
        }
        this.names = builder.buildOrThrow();
    }

    @Override
    public List<T> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<T> getValue(String string) {
        return Optional.ofNullable((Enum)this.names.get(string));
    }

    @Override
    public String getName(T enum_) {
        return ((StringRepresentable)enum_).getSerializedName();
    }

    @Override
    public int getInternalIndex(T enum_) {
        return this.ordinalToIndex[((Enum)enum_).ordinal()];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof EnumProperty) {
            EnumProperty enumProperty = (EnumProperty)object;
            if (super.equals(object)) {
                return this.values.equals(enumProperty.values);
            }
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        int i = super.generateHashCode();
        i = 31 * i + this.values.hashCode();
        return i;
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> class_) {
        return EnumProperty.create(string, class_, (T enum_) -> true);
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> class_, Predicate<T> predicate) {
        return EnumProperty.create(string, class_, Arrays.stream((Enum[])class_.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> class_, T ... enums) {
        return EnumProperty.create(string, class_, List.of((Object[])enums));
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> class_, List<T> list) {
        return new EnumProperty<T>(string, class_, list);
    }

    @Override
    public /* synthetic */ int getInternalIndex(Comparable comparable) {
        return this.getInternalIndex((Enum)((Object)comparable));
    }

    @Override
    public /* synthetic */ String getName(Comparable comparable) {
        return this.getName((Enum)((Object)comparable));
    }
}

