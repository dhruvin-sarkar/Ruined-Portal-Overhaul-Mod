/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.BuiltInExceptionProvider
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.advancements.criterion;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public interface MinMaxBounds<T extends Number> {
    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.swapped"));

    public Bounds<T> bounds();

    default public Optional<T> min() {
        return this.bounds().min;
    }

    default public Optional<T> max() {
        return this.bounds().max;
    }

    default public boolean isAny() {
        return this.bounds().isAny();
    }

    public static final class Bounds<T extends Number>
    extends Record {
        final Optional<T> min;
        final Optional<T> max;

        public Bounds(Optional<T> optional, Optional<T> optional2) {
            this.min = optional;
            this.max = optional2;
        }

        public boolean isAny() {
            return this.min().isEmpty() && this.max().isEmpty();
        }

        public DataResult<Bounds<T>> validateSwappedBoundsInCodec() {
            if (this.areSwapped()) {
                return DataResult.error(() -> "Swapped bounds in range: " + String.valueOf(this.min()) + " is higher than " + String.valueOf(this.max()));
            }
            return DataResult.success((Object)((Object)this));
        }

        public boolean areSwapped() {
            return this.min.isPresent() && this.max.isPresent() && ((Comparable)((Object)((Number)this.min.get()))).compareTo((Number)this.max.get()) > 0;
        }

        public Optional<T> asPoint() {
            Optional<T> optional2;
            Optional<T> optional = this.min();
            return optional.equals(optional2 = this.max()) ? optional : Optional.empty();
        }

        public static <T extends Number> Bounds<T> any() {
            return new Bounds(Optional.empty(), Optional.empty());
        }

        public static <T extends Number> Bounds<T> exactly(T number) {
            Optional<T> optional = Optional.of(number);
            return new Bounds<T>(optional, optional);
        }

        public static <T extends Number> Bounds<T> between(T number, T number2) {
            return new Bounds<T>(Optional.of(number), Optional.of(number2));
        }

        public static <T extends Number> Bounds<T> atLeast(T number) {
            return new Bounds<T>(Optional.of(number), Optional.empty());
        }

        public static <T extends Number> Bounds<T> atMost(T number) {
            return new Bounds(Optional.empty(), Optional.of(number));
        }

        public <U extends Number> Bounds<U> map(Function<T, U> function) {
            return new Bounds<U>(this.min.map(function), this.max.map(function));
        }

        static <T extends Number> Codec<Bounds<T>> createCodec(Codec<T> codec) {
            Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)codec.optionalFieldOf("min").forGetter(Bounds::min), (App)codec.optionalFieldOf("max").forGetter(Bounds::max)).apply((Applicative)instance, Bounds::new));
            return Codec.either((Codec)codec2, codec).xmap(either -> (Bounds)((Object)((Object)either.map(bounds -> bounds, object -> Bounds.exactly((Number)object)))), bounds -> {
                Optional optional = bounds.asPoint();
                return optional.isPresent() ? Either.right((Object)((Number)optional.get())) : Either.left((Object)bounds);
            });
        }

        static <B extends ByteBuf, T extends Number> StreamCodec<B, Bounds<T>> createStreamCodec(final StreamCodec<B, T> streamCodec) {
            return new StreamCodec<B, Bounds<T>>(){
                private static final int MIN_FLAG = 1;
                private static final int MAX_FLAG = 2;

                @Override
                public Bounds<T> decode(B byteBuf) {
                    byte b = byteBuf.readByte();
                    Optional optional = (b & 1) != 0 ? Optional.of((Number)streamCodec.decode(byteBuf)) : Optional.empty();
                    Optional optional2 = (b & 2) != 0 ? Optional.of((Number)streamCodec.decode(byteBuf)) : Optional.empty();
                    return new Bounds(optional, optional2);
                }

                @Override
                public void encode(B byteBuf, Bounds<T> bounds) {
                    Optional<Number> optional = bounds.min();
                    Optional<Number> optional2 = bounds.max();
                    byteBuf.writeByte((optional.isPresent() ? 1 : 0) | (optional2.isPresent() ? 2 : 0));
                    optional.ifPresent(number -> streamCodec.encode(byteBuf, number));
                    optional2.ifPresent(number -> streamCodec.encode(byteBuf, number));
                }

                @Override
                public /* synthetic */ void encode(Object object, Object object2) {
                    this.encode((ByteBuf)object, (Bounds)((Object)object2));
                }

                @Override
                public /* synthetic */ Object decode(Object object) {
                    return this.decode((B)((ByteBuf)object));
                }
            };
        }

        public static <T extends Number> Bounds<T> fromReader(StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
            if (!stringReader.canRead()) {
                throw ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
            }
            int i = stringReader.getCursor();
            try {
                Optional<T> optional2;
                Optional<T> optional = Bounds.readNumber(stringReader, function, supplier);
                if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
                    stringReader.skip();
                    stringReader.skip();
                    optional2 = Bounds.readNumber(stringReader, function, supplier);
                } else {
                    optional2 = optional;
                }
                if (optional.isEmpty() && optional2.isEmpty()) {
                    throw ERROR_EMPTY.createWithContext((ImmutableStringReader)stringReader);
                }
                return new Bounds<T>(optional, optional2);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                stringReader.setCursor(i);
                throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), i);
            }
        }

        private static <T extends Number> Optional<T> readNumber(StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
            int i = stringReader.getCursor();
            while (stringReader.canRead() && Bounds.isAllowedInputChar(stringReader)) {
                stringReader.skip();
            }
            String string = stringReader.getString().substring(i, stringReader.getCursor());
            if (string.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of((Number)function.apply(string));
            }
            catch (NumberFormatException numberFormatException) {
                throw supplier.get().createWithContext((ImmutableStringReader)stringReader, (Object)string);
            }
        }

        private static boolean isAllowedInputChar(StringReader stringReader) {
            char c = stringReader.peek();
            if (c >= '0' && c <= '9' || c == '-') {
                return true;
            }
            if (c == '.') {
                return !stringReader.canRead(2) || stringReader.peek(1) != '.';
            }
            return false;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this, object);
        }

        public Optional<T> min() {
            return this.min;
        }

        public Optional<T> max() {
            return this.max;
        }
    }

    public record FloatDegrees(Bounds<Float> bounds) implements MinMaxBounds<Float>
    {
        public static final FloatDegrees ANY = new FloatDegrees(Bounds.any());
        public static final Codec<FloatDegrees> CODEC = Bounds.createCodec(Codec.FLOAT).xmap(FloatDegrees::new, FloatDegrees::bounds);
        public static final StreamCodec<ByteBuf, FloatDegrees> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.FLOAT).map(FloatDegrees::new, FloatDegrees::bounds);

        public static FloatDegrees fromReader(StringReader stringReader) throws CommandSyntaxException {
            Bounds<Float> bounds = Bounds.fromReader(stringReader, Float::parseFloat, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidFloat());
            return new FloatDegrees(bounds);
        }
    }

    public record Doubles(Bounds<Double> bounds, Bounds<Double> boundsSqr) implements MinMaxBounds<Double>
    {
        public static final Doubles ANY = new Doubles(Bounds.any());
        public static final Codec<Doubles> CODEC = Bounds.createCodec(Codec.DOUBLE).validate(Bounds::validateSwappedBoundsInCodec).xmap(Doubles::new, Doubles::bounds);
        public static final StreamCodec<ByteBuf, Doubles> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.DOUBLE).map(Doubles::new, Doubles::bounds);

        private Doubles(Bounds<Double> bounds) {
            this(bounds, bounds.map(Mth::square));
        }

        public static Doubles exactly(double d) {
            return new Doubles(Bounds.exactly(d));
        }

        public static Doubles between(double d, double e) {
            return new Doubles(Bounds.between(d, e));
        }

        public static Doubles atLeast(double d) {
            return new Doubles(Bounds.atLeast(d));
        }

        public static Doubles atMost(double d) {
            return new Doubles(Bounds.atMost(d));
        }

        public boolean matches(double d) {
            if (this.bounds.min.isPresent() && (Double)this.bounds.min.get() > d) {
                return false;
            }
            return this.bounds.max.isEmpty() || !((Double)this.bounds.max.get() < d);
        }

        public boolean matchesSqr(double d) {
            if (this.boundsSqr.min.isPresent() && (Double)this.boundsSqr.min.get() > d) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || !((Double)this.boundsSqr.max.get() < d);
        }

        public static Doubles fromReader(StringReader stringReader) throws CommandSyntaxException {
            int i = stringReader.getCursor();
            Bounds<Double> bounds = Bounds.fromReader(stringReader, Double::parseDouble, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidDouble());
            if (bounds.areSwapped()) {
                stringReader.setCursor(i);
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)stringReader);
            }
            return new Doubles(bounds);
        }
    }

    public record Ints(Bounds<Integer> bounds, Bounds<Long> boundsSqr) implements MinMaxBounds<Integer>
    {
        public static final Ints ANY = new Ints(Bounds.any());
        public static final Codec<Ints> CODEC = Bounds.createCodec(Codec.INT).validate(Bounds::validateSwappedBoundsInCodec).xmap(Ints::new, Ints::bounds);
        public static final StreamCodec<ByteBuf, Ints> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.INT).map(Ints::new, Ints::bounds);

        private Ints(Bounds<Integer> bounds) {
            this(bounds, bounds.map(integer -> Mth.square(integer.longValue())));
        }

        public static Ints exactly(int i) {
            return new Ints(Bounds.exactly(i));
        }

        public static Ints between(int i, int j) {
            return new Ints(Bounds.between(i, j));
        }

        public static Ints atLeast(int i) {
            return new Ints(Bounds.atLeast(i));
        }

        public static Ints atMost(int i) {
            return new Ints(Bounds.atMost(i));
        }

        public boolean matches(int i) {
            if (this.bounds.min.isPresent() && (Integer)this.bounds.min.get() > i) {
                return false;
            }
            return this.bounds.max.isEmpty() || (Integer)this.bounds.max.get() >= i;
        }

        public boolean matchesSqr(long l) {
            if (this.boundsSqr.min.isPresent() && (Long)this.boundsSqr.min.get() > l) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || (Long)this.boundsSqr.max.get() >= l;
        }

        public static Ints fromReader(StringReader stringReader) throws CommandSyntaxException {
            int i = stringReader.getCursor();
            Bounds<Integer> bounds = Bounds.fromReader(stringReader, Integer::parseInt, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidInt());
            if (bounds.areSwapped()) {
                stringReader.setCursor(i);
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)stringReader);
            }
            return new Ints(bounds);
        }
    }
}

