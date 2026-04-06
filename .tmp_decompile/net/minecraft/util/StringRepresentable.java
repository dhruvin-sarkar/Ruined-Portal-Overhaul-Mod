/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface StringRepresentable {
    public static final int PRE_BUILT_MAP_THRESHOLD = 16;

    public String getSerializedName();

    public static <E extends Enum<E>> EnumCodec<E> fromEnum(Supplier<E[]> supplier) {
        return StringRepresentable.fromEnumWithMapping(supplier, string -> string);
    }

    public static <E extends Enum<E>> EnumCodec<E> fromEnumWithMapping(Supplier<E[]> supplier, Function<String, String> function) {
        Enum[] enums = (Enum[])supplier.get();
        Function<String, Enum> function2 = StringRepresentable.createNameLookup(enums, enum_ -> (String)function.apply(((StringRepresentable)((Object)enum_)).getSerializedName()));
        return new EnumCodec(enums, function2);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> supplier) {
        StringRepresentable[] stringRepresentables = (StringRepresentable[])supplier.get();
        @Nullable Function function = StringRepresentable.createNameLookup((StringRepresentable[])stringRepresentables);
        ToIntFunction<StringRepresentable> toIntFunction = Util.createIndexLookup(Arrays.asList(stringRepresentables));
        return new StringRepresentableCodec(stringRepresentables, function, toIntFunction);
    }

    public static <T extends StringRepresentable> Function<String, @Nullable T> createNameLookup(T[] stringRepresentables) {
        return StringRepresentable.createNameLookup(stringRepresentables, StringRepresentable::getSerializedName);
    }

    public static <T> Function<String, @Nullable T> createNameLookup(T[] objects, Function<T, String> function) {
        if (objects.length > 16) {
            Map<String, Object> map = Arrays.stream(objects).collect(Collectors.toMap(function, object -> object));
            return map::get;
        }
        return string -> {
            for (Object object : objects) {
                if (!((String)function.apply(object)).equals(string)) continue;
                return object;
            }
            return null;
        };
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentables) {
        return new Keyable(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(arg_0 -> dynamicOps.createString(arg_0));
            }
        };
    }

    public static class EnumCodec<E extends Enum<E>>
    extends StringRepresentableCodec<E> {
        private final Function<String, @Nullable E> resolver;

        public EnumCodec(E[] enums, Function<String, E> function) {
            super(enums, function, object -> ((Enum)object).ordinal());
            this.resolver = function;
        }

        public @Nullable E byName(String string) {
            return (E)((Enum)this.resolver.apply(string));
        }

        public E byName(String string, E enum_) {
            return (E)((Enum)Objects.requireNonNullElse(this.byName(string), enum_));
        }

        public E byName(String string, Supplier<? extends E> supplier) {
            return (E)((Enum)Objects.requireNonNullElseGet(this.byName(string), supplier));
        }
    }

    public static class StringRepresentableCodec<S extends StringRepresentable>
    implements Codec<S> {
        private final Codec<S> codec;

        public StringRepresentableCodec(S[] stringRepresentables, Function<String, @Nullable S> function, ToIntFunction<S> toIntFunction) {
            this.codec = ExtraCodecs.orCompressed(Codec.stringResolver(StringRepresentable::getSerializedName, function), ExtraCodecs.idResolverCodec(toIntFunction, i -> i >= 0 && i < stringRepresentables.length ? stringRepresentables[i] : null, -1));
        }

        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> dynamicOps, T object) {
            return this.codec.decode(dynamicOps, object);
        }

        public <T> DataResult<T> encode(S stringRepresentable, DynamicOps<T> dynamicOps, T object) {
            return this.codec.encode(stringRepresentable, dynamicOps, object);
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((S)((StringRepresentable)object), (DynamicOps<T>)dynamicOps, (T)object2);
        }
    }
}

