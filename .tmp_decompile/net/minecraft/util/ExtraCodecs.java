/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.common.primitives.UnsignedBytes
 *  com.google.gson.JsonElement
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.BaseMapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.util.HexFormat
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.joml.AxisAngle4f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector2f
 *  org.joml.Vector2fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector3i
 *  org.joml.Vector3ic
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = ExtraCodecs.converter(JsonOps.INSTANCE);
    public static final Codec<Object> JAVA = ExtraCodecs.converter(JavaOps.INSTANCE);
    public static final Codec<Tag> NBT = ExtraCodecs.converter(NbtOps.INSTANCE);
    public static final Codec<Vector2fc> VECTOR2F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 2).map(list -> new Vector2f(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue())), vector2fc -> List.of((Object)Float.valueOf(vector2fc.x()), (Object)Float.valueOf(vector2fc.y())));
    public static final Codec<Vector3fc> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 3).map(list -> new Vector3f(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue())), vector3fc -> List.of((Object)Float.valueOf(vector3fc.x()), (Object)Float.valueOf(vector3fc.y()), (Object)Float.valueOf(vector3fc.z())));
    public static final Codec<Vector3ic> VECTOR3I = Codec.INT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 3).map(list -> new Vector3i(((Integer)list.get(0)).intValue(), ((Integer)list.get(1)).intValue(), ((Integer)list.get(2)).intValue())), vector3ic -> List.of((Object)vector3ic.x(), (Object)vector3ic.y(), (Object)vector3ic.z()));
    public static final Codec<Vector4fc> VECTOR4F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 4).map(list -> new Vector4f(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue(), ((Float)list.get(3)).floatValue())), vector4fc -> List.of((Object)Float.valueOf(vector4fc.x()), (Object)Float.valueOf(vector4fc.y()), (Object)Float.valueOf(vector4fc.z()), (Object)Float.valueOf(vector4fc.w())));
    public static final Codec<Quaternionfc> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 4).map(list -> new Quaternionf(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue(), ((Float)list.get(3)).floatValue()).normalize()), quaternionfc -> List.of((Object)Float.valueOf(quaternionfc.x()), (Object)Float.valueOf(quaternionfc.y()), (Object)Float.valueOf(quaternionfc.z()), (Object)Float.valueOf(quaternionfc.w())));
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("angle").forGetter(axisAngle4f -> Float.valueOf(axisAngle4f.angle)), (App)VECTOR3F.fieldOf("axis").forGetter(axisAngle4f -> new Vector3f(axisAngle4f.x, axisAngle4f.y, axisAngle4f.z))).apply((Applicative)instance, AxisAngle4f::new));
    public static final Codec<Quaternionfc> QUATERNIONF = Codec.withAlternative(QUATERNIONF_COMPONENTS, (Codec)AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static final Codec<Matrix4fc> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 16).map(list -> {
        Matrix4f matrix4f = new Matrix4f();
        for (int i = 0; i < list.size(); ++i) {
            matrix4f.setRowColumn(i >> 2, i & 3, ((Float)list.get(i)).floatValue());
        }
        return matrix4f.determineProperties();
    }), matrix4fc -> {
        FloatArrayList floatList = new FloatArrayList(16);
        for (int i = 0; i < 16; ++i) {
            floatList.add(matrix4fc.getRowColumn(i >> 2, i & 3));
        }
        return floatList;
    });
    private static final String HEX_COLOR_PREFIX = "#";
    public static final Codec<Integer> RGB_COLOR_CODEC = Codec.withAlternative((Codec)Codec.INT, VECTOR3F, vector3fc -> ARGB.colorFromFloat(1.0f, vector3fc.x(), vector3fc.y(), vector3fc.z()));
    public static final Codec<Integer> ARGB_COLOR_CODEC = Codec.withAlternative((Codec)Codec.INT, VECTOR4F, vector4fc -> ARGB.colorFromFloat(vector4fc.w(), vector4fc.x(), vector4fc.y(), vector4fc.z()));
    public static final Codec<Integer> STRING_RGB_COLOR = Codec.withAlternative((Codec)ExtraCodecs.hexColor(6).xmap(ARGB::opaque, ARGB::transparent), RGB_COLOR_CODEC);
    public static final Codec<Integer> STRING_ARGB_COLOR = Codec.withAlternative(ExtraCodecs.hexColor(8), ARGB_COLOR_CODEC);
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, integer -> {
        if (integer > 255) {
            return DataResult.error(() -> "Unsigned byte was too large: " + integer + " > 255");
        }
        return DataResult.success((Object)integer.byteValue());
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);
    public static final Codec<Long> NON_NEGATIVE_LONG = ExtraCodecs.longRangeWithMessage(0L, Long.MAX_VALUE, long_ -> "Value must be non-negative: " + long_);
    public static final Codec<Long> POSITIVE_LONG = ExtraCodecs.longRangeWithMessage(1L, Long.MAX_VALUE, long_ -> "Value must be positive: " + long_);
    public static final Codec<Float> NON_NEGATIVE_FLOAT = ExtraCodecs.floatRangeMinInclusiveWithMessage(0.0f, Float.MAX_VALUE, float_ -> "Value must be non-negative: " + float_);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, float_ -> "Value must be positive: " + float_);
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Pattern.compile(string));
        }
        catch (PatternSyntaxException patternSyntaxException) {
            return DataResult.error(() -> "Invalid regex pattern '" + string + "': " + patternSyntaxException.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = ExtraCodecs.temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Base64.getDecoder().decode((String)string));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, bs -> Base64.getEncoder().encodeToString((byte[])bs));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap(string -> DataResult.success((Object)StringEscapeUtils.unescapeJava((String)string)), StringEscapeUtils::escapeJava);
    public static final Codec<TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap(string -> string.startsWith(HEX_COLOR_PREFIX) ? Identifier.read(string.substring(1)).map(identifier -> new TagOrElementLocation((Identifier)identifier, true)) : Identifier.read(string).map(identifier -> new TagOrElementLocation((Identifier)identifier, false)), TagOrElementLocation::decoratedId);
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> Arrays.stream(bitSet.toLongArray()));
    public static final int MAX_PROPERTY_NAME_LENGTH = 64;
    public static final int MAX_PROPERTY_VALUE_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
    public static final int MAX_PROPERTIES = 16;
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create(instance -> instance.group((App)Codec.sizeLimitedString((int)64).fieldOf("name").forGetter(Property::name), (App)Codec.sizeLimitedString((int)Short.MAX_VALUE).fieldOf("value").forGetter(Property::value), (App)Codec.sizeLimitedString((int)1024).optionalFieldOf("signature").forGetter(property -> Optional.ofNullable(property.signature()))).apply((Applicative)instance, (string, string2, optional) -> new Property(string, string2, (String)optional.orElse(null))));
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either((Codec)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING.listOf()).validate(map -> map.size() > 16 ? DataResult.error(() -> "Cannot have more than 16 properties, but was " + map.size()) : DataResult.success((Object)map)), (Codec)PROPERTY.sizeLimitedListOf(16)).xmap(either -> {
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        either.ifLeft(map -> map.forEach((string, list) -> {
            for (String string2 : list) {
                builder.put(string, (Object)new Property(string, string2));
            }
        })).ifRight(list -> {
            for (Property property : list) {
                builder.put((Object)property.name(), (Object)property);
            }
        });
        return new PropertyMap((Multimap)builder.build());
    }, propertyMap -> Either.right((Object)propertyMap.values().stream().toList()));
    public static final Codec<String> PLAYER_NAME = Codec.string((int)0, (int)16).validate(string -> {
        if (StringUtil.isValidPlayerName(string)) {
            return DataResult.success((Object)string);
        }
        return DataResult.error(() -> "Player name contained disallowed characters: '" + string + "'");
    });
    public static final Codec<GameProfile> AUTHLIB_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.AUTHLIB_CODEC).codec();
    public static final MapCodec<GameProfile> STORED_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.CODEC);
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate(string -> string.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success((Object)string));
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(string -> {
        int[] is = string.codePoints().toArray();
        if (is.length != 1) {
            return DataResult.error(() -> "Expected one codepoint, got: " + string);
        }
        return DataResult.success((Object)is[0]);
    }, (Function<Integer, String>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, toString(int ), (Ljava/lang/Integer;)Ljava/lang/String;)());
    public static final Codec<String> RESOURCE_PATH_CODEC = Codec.STRING.validate(string -> {
        if (!Identifier.isValidPath(string)) {
            return DataResult.error(() -> "Invalid string to use as a resource path element: " + string);
        }
        return DataResult.success((Object)string);
    });
    public static final Codec<URI> UNTRUSTED_URI = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Util.parseAndValidateUntrustedUri(string));
        }
        catch (URISyntaxException uRISyntaxException) {
            return DataResult.error(uRISyntaxException::getMessage);
        }
    }, URI::toString);
    public static final Codec<String> CHAT_STRING = Codec.STRING.validate(string -> {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (StringUtil.isAllowedChatCharacter(c)) continue;
            return DataResult.error(() -> "Disallowed chat character: '" + c + "'");
        }
        return DataResult.success((Object)string);
    });

    public static <T> Codec<T> converter(DynamicOps<T> dynamicOps) {
        return Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(dynamicOps).getValue(), object -> new Dynamic(dynamicOps, object));
    }

    private static Codec<Integer> hexColor(int i) {
        long l = (1L << i * 4) - 1L;
        return Codec.STRING.comapFlatMap(string -> {
            if (!string.startsWith(HEX_COLOR_PREFIX)) {
                return DataResult.error(() -> "Hex color must begin with #");
            }
            int j = string.length() - HEX_COLOR_PREFIX.length();
            if (j != i) {
                return DataResult.error(() -> "Hex color is wrong size, expected " + i + " digits but got " + j);
            }
            try {
                long m = HexFormat.fromHexDigitsToLong((CharSequence)string, (int)HEX_COLOR_PREFIX.length(), (int)string.length());
                if (m < 0L || m > l) {
                    return DataResult.error(() -> "Color value out of range: " + string);
                }
                return DataResult.success((Object)((int)m));
            }
            catch (NumberFormatException numberFormatException) {
                return DataResult.error(() -> "Invalid color value: " + string);
            }
        }, integer -> HEX_COLOR_PREFIX + HexFormat.of().toHexDigits((long)integer.intValue(), i));
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String string, String string2, BiFunction<P, P, DataResult<I>> biFunction, Function<I, P> function, Function<I, P> function2) {
        Codec codec2 = Codec.list(codec).comapFlatMap(list2 -> Util.fixedSize(list2, 2).flatMap(list -> {
            Object object = list.get(0);
            Object object2 = list.get(1);
            return (DataResult)biFunction.apply(object, object2);
        }), object -> ImmutableList.of(function.apply(object), function2.apply(object)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf(string).forGetter(Pair::getFirst), (App)codec.fieldOf(string2).forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of)).comapFlatMap(pair -> (DataResult)biFunction.apply(pair.getFirst(), pair.getSecond()), object -> Pair.of(function.apply(object), function2.apply(object)));
        Codec codec4 = Codec.withAlternative((Codec)codec2, (Codec)codec3);
        return Codec.either(codec, (Codec)codec4).comapFlatMap(either -> (DataResult)either.map(object -> (DataResult)biFunction.apply(object, object), DataResult::success), object -> {
            Object object3;
            Object object2 = function.apply(object);
            if (Objects.equals(object2, object3 = function2.apply(object))) {
                return Either.left(object2);
            }
            return Either.right((Object)object);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A object) {
        return new Codec.ResultFunction<A>(){

            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicOps, T object2, DataResult<Pair<A, T>> dataResult) {
                MutableObject mutableObject = new MutableObject();
                Optional optional = dataResult.resultOrPartial(arg_0 -> ((MutableObject)mutableObject).setValue(arg_0));
                if (optional.isPresent()) {
                    return dataResult;
                }
                return DataResult.error(() -> "(" + (String)mutableObject.get() + " -> using default)", (Object)Pair.of((Object)object, object2));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, A object2, DataResult<T> dataResult) {
                return dataResult;
            }

            public String toString() {
                return "OrElsePartial[" + String.valueOf(object) + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toIntFunction, IntFunction<@Nullable E> intFunction, int i) {
        return Codec.INT.flatXmap(integer -> Optional.ofNullable(intFunction.apply((int)integer)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + integer)), object -> {
            int j = toIntFunction.applyAsInt(object);
            return j == i ? DataResult.error(() -> "Element with unknown id: " + String.valueOf(object)) : DataResult.success((Object)j);
        });
    }

    public static <I, E> Codec<E> idResolverCodec(Codec<I> codec, Function<I, @Nullable E> function, Function<E, @Nullable I> function2) {
        return codec.flatXmap(object -> {
            Object object2 = function.apply(object);
            return object2 == null ? DataResult.error(() -> "Unknown element id: " + String.valueOf(object)) : DataResult.success(object2);
        }, object -> {
            Object object2 = function2.apply(object);
            if (object2 == null) {
                return DataResult.error(() -> "Element with unknown id: " + String.valueOf(object));
            }
            return DataResult.success(object2);
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec2) {
        return new Codec<E>(){

            public <T> DataResult<T> encode(E object, DynamicOps<T> dynamicOps, T object2) {
                if (dynamicOps.compressMaps()) {
                    return codec2.encode(object, dynamicOps, object2);
                }
                return codec.encode(object, dynamicOps, object2);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
                if (dynamicOps.compressMaps()) {
                    return codec2.decode(dynamicOps, object);
                }
                return codec.decode(dynamicOps, object);
            }

            public String toString() {
                return String.valueOf(codec) + " orCompressed " + String.valueOf(codec2);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> mapCodec, final MapCodec<E> mapCodec2) {
        return new MapCodec<E>(){

            public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                if (dynamicOps.compressMaps()) {
                    return mapCodec2.encode(object, dynamicOps, recordBuilder);
                }
                return mapCodec.encode(object, dynamicOps, recordBuilder);
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                if (dynamicOps.compressMaps()) {
                    return mapCodec2.decode(dynamicOps, mapLike);
                }
                return mapCodec.decode(dynamicOps, mapLike);
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return mapCodec2.keys(dynamicOps);
            }

            public String toString() {
                return String.valueOf(mapCodec) + " orCompressed " + String.valueOf(mapCodec2);
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function2) {
        return codec.mapResult(new Codec.ResultFunction<E>(){

            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicOps, T object, DataResult<Pair<E, T>> dataResult) {
                return dataResult.result().map(pair -> dataResult.setLifecycle((Lifecycle)function.apply(pair.getFirst()))).orElse(dataResult);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, E object, DataResult<T> dataResult) {
                return dataResult.setLifecycle((Lifecycle)function2.apply(object));
            }

            public String toString() {
                return "WithLifecycle[" + String.valueOf(function) + " " + String.valueOf(function2) + "]";
            }
        });
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> function) {
        return ExtraCodecs.overrideLifecycle(codec, function, function);
    }

    public static <K, V> StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> codec, Codec<V> codec2) {
        return new StrictUnboundedMapCodec<K, V>(codec, codec2);
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> codec) {
        return ExtraCodecs.compactListCodec(codec, codec.listOf());
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> codec, Codec<List<E>> codec2) {
        return Codec.either(codec2, codec).xmap(either -> (List)either.map(list -> list, (Function<Object, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, of(java.lang.Object ), (Ljava/lang/Object;)Ljava/util/List;)()), list -> list.size() == 1 ? Either.right((Object)list.getFirst()) : Either.left((Object)list));
    }

    private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
        return Codec.INT.validate(integer -> {
            if (integer.compareTo(i) >= 0 && integer.compareTo(j) <= 0) {
                return DataResult.success((Object)integer);
            }
            return DataResult.error(() -> (String)function.apply((Integer)integer));
        });
    }

    public static Codec<Integer> intRange(int i, int j) {
        return ExtraCodecs.intRangeWithMessage(i, j, integer -> "Value must be within range [" + i + ";" + j + "]: " + integer);
    }

    private static Codec<Long> longRangeWithMessage(long l, long m, Function<Long, String> function) {
        return Codec.LONG.validate(long_ -> {
            if ((long)long_.compareTo(l) >= 0L && (long)long_.compareTo(m) <= 0L) {
                return DataResult.success((Object)long_);
            }
            return DataResult.error(() -> (String)function.apply((Long)long_));
        });
    }

    public static Codec<Long> longRange(int i, int j) {
        return ExtraCodecs.longRangeWithMessage(i, j, long_ -> "Value must be within range [" + i + ";" + j + "]: " + long_);
    }

    private static Codec<Float> floatRangeMinInclusiveWithMessage(float f, float g, Function<Float, String> function) {
        return Codec.FLOAT.validate(float_ -> {
            if (float_.compareTo(Float.valueOf(f)) >= 0 && float_.compareTo(Float.valueOf(g)) <= 0) {
                return DataResult.success((Object)float_);
            }
            return DataResult.error(() -> (String)function.apply((Float)float_));
        });
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float g, Function<Float, String> function) {
        return Codec.FLOAT.validate(float_ -> {
            if (float_.compareTo(Float.valueOf(f)) > 0 && float_.compareTo(Float.valueOf(g)) <= 0) {
                return DataResult.success((Object)float_);
            }
            return DataResult.error(() -> (String)function.apply((Float)float_));
        });
    }

    public static Codec<Float> floatRange(float f, float g) {
        return ExtraCodecs.floatRangeMinInclusiveWithMessage(f, g, float_ -> "Value must be within range [" + f + ";" + g + "]: " + float_);
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return codec.validate(list -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success((Object)list));
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> codec) {
        return codec.validate(holderSet -> {
            if (holderSet.unwrap().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error(() -> "List must have contents");
            }
            return DataResult.success((Object)holderSet);
        });
    }

    public static <M extends Map<?, ?>> Codec<M> nonEmptyMap(Codec<M> codec) {
        return codec.validate(map -> map.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success((Object)map));
    }

    public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> function) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function val$getter;

            ContextRetrievalCodec(Function function) {
                this.val$getter = function;
            }

            public <T> RecordBuilder<T> encode(E object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                return recordBuilder;
            }

            public <T> DataResult<E> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                return (DataResult)this.val$getter.apply(dynamicOps);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + String.valueOf(this.val$getter) + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec(function);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> function) {
        return collection -> {
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                Object object = function.apply(iterator.next());
                while (iterator.hasNext()) {
                    Object object2 = iterator.next();
                    Object object3 = function.apply(object2);
                    if (object3 == object) continue;
                    return DataResult.error(() -> "Mixed type list: element " + String.valueOf(object2) + " had type " + String.valueOf(object3) + ", but list is of type " + String.valueOf(object));
                }
            }
            return DataResult.success((Object)collection, (Lifecycle)Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> codec) {
        return Codec.of(codec, (Decoder)new Decoder<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
                try {
                    return codec.decode(dynamicOps, object);
                }
                catch (Exception exception) {
                    return DataResult.error(() -> "Caught exception decoding " + String.valueOf(object) + ": " + exception.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter dateTimeFormatter) {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success((Object)dateTimeFormatter.parse((CharSequence)string));
            }
            catch (Exception exception) {
                return DataResult.error(exception::getMessage);
            }
        }, dateTimeFormatter::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> mapCodec) {
        return mapCodec.xmap(toOptionalLong, fromOptionalLong);
    }

    private static MapCodec<GameProfile> gameProfileCodec(Codec<UUID> codec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)codec.fieldOf("id").forGetter(GameProfile::id), (App)PLAYER_NAME.fieldOf("name").forGetter(GameProfile::name), (App)PROPERTY_MAP.optionalFieldOf("properties", (Object)PropertyMap.EMPTY).forGetter(GameProfile::properties)).apply((Applicative)instance, GameProfile::new));
    }

    public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> codec, int i) {
        return codec.validate(map -> {
            if (map.size() > i) {
                return DataResult.error(() -> "Map is too long: " + map.size() + ", expected range [0-" + i + "]");
            }
            return DataResult.success((Object)map);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> codec) {
        return Codec.unboundedMap(codec, (Codec)Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    @Deprecated
    public static <K, V> MapCodec<V> dispatchOptionalValue(final String string, final String string2, final Codec<K> codec, final Function<? super V, ? extends K> function, final Function<? super K, ? extends Codec<? extends V>> function2) {
        return new MapCodec<V>(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.of(dynamicOps.createString(string), dynamicOps.createString(string2));
            }

            public <T> DataResult<V> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                Object object = mapLike.get(string);
                if (object == null) {
                    return DataResult.error(() -> "Missing \"" + string + "\" in: " + String.valueOf(mapLike));
                }
                return codec.decode(dynamicOps, object).flatMap(pair -> {
                    Object object = Objects.requireNonNullElseGet((Object)mapLike.get(string2), () -> ((DynamicOps)dynamicOps).emptyMap());
                    return ((Codec)function2.apply(pair.getFirst())).decode(dynamicOps, object).map(Pair::getFirst);
                });
            }

            public <T> RecordBuilder<T> encode(V object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                Object object2 = function.apply(object);
                recordBuilder.add(string, codec.encodeStart(dynamicOps, object2));
                DataResult<T> dataResult = this.encode((Codec)function2.apply(object2), object, dynamicOps);
                if (dataResult.result().isEmpty() || !Objects.equals(dataResult.result().get(), dynamicOps.emptyMap())) {
                    recordBuilder.add(string2, dataResult);
                }
                return recordBuilder;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec2, V object, DynamicOps<T> dynamicOps) {
                return codec2.encodeStart(dynamicOps, object);
            }
        };
    }

    public static <A> Codec<Optional<A>> optionalEmptyMap(final Codec<A> codec) {
        return new Codec<Optional<A>>(){

            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> dynamicOps, T object) {
                if (7.isEmptyMap(dynamicOps, object)) {
                    return DataResult.success((Object)Pair.of(Optional.empty(), object));
                }
                return codec.decode(dynamicOps, object).map(pair -> pair.mapFirst(Optional::of));
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> dynamicOps, T object) {
                Optional optional = dynamicOps.getMap(object).result();
                return optional.isPresent() && ((MapLike)optional.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> optional, DynamicOps<T> dynamicOps, T object) {
                if (optional.isEmpty()) {
                    return DataResult.success((Object)dynamicOps.emptyMap());
                }
                return codec.encode(optional.get(), dynamicOps, object);
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((Optional)object, dynamicOps, object2);
            }
        };
    }

    @Deprecated
    public static <E extends Enum<E>> Codec<E> legacyEnum(Function<String, E> function) {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success((Object)((Enum)function.apply((String)string)));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return DataResult.error(() -> "No value with id: " + string);
            }
        }, Enum::toString);
    }

    public static final class StrictUnboundedMapCodec<K, V>
    extends Record
    implements Codec<Map<K, V>>,
    BaseMapCodec<K, V> {
        private final Codec<K> a;
        private final Codec<V> b;

        public StrictUnboundedMapCodec(Codec<K> codec, Codec<V> codec2) {
            this.a = codec;
            this.b = codec2;
        }

        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Pair pair : mapLike.entries().toList()) {
                DataResult dataResult2;
                DataResult dataResult = this.keyCodec().parse(dynamicOps, pair.getFirst());
                DataResult dataResult3 = dataResult.apply2stable(Pair::of, dataResult2 = this.elementCodec().parse(dynamicOps, pair.getSecond()));
                Optional optional = dataResult3.error();
                if (optional.isPresent()) {
                    String string = ((DataResult.Error)optional.get()).message();
                    return DataResult.error(() -> {
                        if (dataResult.result().isPresent()) {
                            return "Map entry '" + String.valueOf(dataResult.result().get()) + "' : " + string;
                        }
                        return string;
                    });
                }
                if (dataResult3.result().isPresent()) {
                    Pair pair2 = (Pair)dataResult3.result().get();
                    builder.put(pair2.getFirst(), pair2.getSecond());
                    continue;
                }
                return DataResult.error(() -> "Empty or invalid map contents are not allowed");
            }
            ImmutableMap map = builder.build();
            return DataResult.success((Object)map);
        }

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> dynamicOps, T object) {
            return dynamicOps.getMap(object).setLifecycle(Lifecycle.stable()).flatMap(mapLike -> this.decode(dynamicOps, (Object)mapLike)).map(map -> Pair.of((Object)map, (Object)object));
        }

        public <T> DataResult<T> encode(Map<K, V> map, DynamicOps<T> dynamicOps, T object) {
            return this.encode(map, dynamicOps, dynamicOps.mapBuilder()).build(object);
        }

        public String toString() {
            return "StrictUnboundedMapCodec[" + String.valueOf(this.a) + " -> " + String.valueOf(this.b) + "]";
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StrictUnboundedMapCodec.class, "keyCodec;elementCodec", "a", "b"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StrictUnboundedMapCodec.class, "keyCodec;elementCodec", "a", "b"}, this, object);
        }

        public Codec<K> keyCodec() {
            return this.a;
        }

        public Codec<V> elementCodec() {
            return this.b;
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Map)object, dynamicOps, object2);
        }
    }

    public record TagOrElementLocation(Identifier id, boolean tag) {
        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? ExtraCodecs.HEX_COLOR_PREFIX + String.valueOf(this.id) : this.id.toString();
        }
    }

    public static class LateBoundIdMapper<I, V> {
        private final BiMap<I, V> idToValue = HashBiMap.create();

        public Codec<V> codec(Codec<I> codec) {
            BiMap biMap = this.idToValue.inverse();
            return ExtraCodecs.idResolverCodec(codec, arg_0 -> this.idToValue.get(arg_0), arg_0 -> biMap.get(arg_0));
        }

        public LateBoundIdMapper<I, V> put(I object, V object2) {
            Objects.requireNonNull(object2, () -> "Value for " + String.valueOf(object) + " is null");
            this.idToValue.put(object, object2);
            return this;
        }

        public Set<V> values() {
            return Collections.unmodifiableSet(this.idToValue.values());
        }
    }
}

