/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.AnyValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentPredicate {
    public static final Codec<Map<Type<?>, DataComponentPredicate>> CODEC = Codec.dispatchedMap(Type.CODEC, Type::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Single<?>> SINGLE_STREAM_CODEC = Type.STREAM_CODEC.dispatch(Single::type, Type::singleStreamCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<Type<?>, DataComponentPredicate>> STREAM_CODEC = SINGLE_STREAM_CODEC.apply(ByteBufCodecs.list(64)).map(list -> list.stream().collect(Collectors.toMap(Single::type, Single::predicate)), map -> map.entrySet().stream().map(Single::fromEntry).toList());

    public static MapCodec<Single<?>> singleCodec(String string) {
        return Type.CODEC.dispatchMap(string, Single::type, Type::wrappedCodec);
    }

    public boolean matches(DataComponentGetter var1);

    public static interface Type<T extends DataComponentPredicate> {
        public static final Codec<Type<?>> CODEC = Codec.either(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec(), BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()).xmap(Type::copyOrCreateType, Type::unpackType);
        public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.registry(Registries.DATA_COMPONENT_PREDICATE_TYPE), ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE)).map(Type::copyOrCreateType, Type::unpackType);

        private static <T extends Type<?>> Either<T, DataComponentType<?>> unpackType(T type) {
            Either either;
            if (type instanceof AnyValueType) {
                AnyValueType anyValueType = (AnyValueType)type;
                either = Either.right(anyValueType.componentType());
            } else {
                either = Either.left(type);
            }
            return either;
        }

        private static Type<?> copyOrCreateType(Either<Type<?>, DataComponentType<?>> either) {
            return (Type)either.map(type -> type, AnyValueType::create);
        }

        public Codec<T> codec();

        public MapCodec<Single<T>> wrappedCodec();

        public StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec();
    }

    public record Single<T extends DataComponentPredicate>(Type<T> type, T predicate) {
        static <T extends DataComponentPredicate> MapCodec<Single<T>> wrapCodec(Type<T> type, Codec<T> codec) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)codec.fieldOf("value").forGetter(Single::predicate)).apply((Applicative)instance, dataComponentPredicate -> new Single<DataComponentPredicate>(type, (DataComponentPredicate)dataComponentPredicate)));
        }

        private static <T extends DataComponentPredicate> Single<T> fromEntry(Map.Entry<Type<?>, T> entry) {
            return new Single<DataComponentPredicate>(entry.getKey(), (DataComponentPredicate)entry.getValue());
        }
    }

    public static final class AnyValueType
    extends TypeBase<AnyValue> {
        private final AnyValue predicate;

        public AnyValueType(AnyValue anyValue) {
            super(MapCodec.unitCodec((Object)anyValue));
            this.predicate = anyValue;
        }

        public AnyValue predicate() {
            return this.predicate;
        }

        public DataComponentType<?> componentType() {
            return this.predicate.type();
        }

        public static AnyValueType create(DataComponentType<?> dataComponentType) {
            return new AnyValueType(new AnyValue(dataComponentType));
        }
    }

    public static final class ConcreteType<T extends DataComponentPredicate>
    extends TypeBase<T> {
        public ConcreteType(Codec<T> codec) {
            super(codec);
        }
    }

    public static abstract class TypeBase<T extends DataComponentPredicate>
    implements Type<T> {
        private final Codec<T> codec;
        private final MapCodec<Single<T>> wrappedCodec;
        private final StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec;

        public TypeBase(Codec<T> codec) {
            this.codec = codec;
            this.wrappedCodec = Single.wrapCodec(this, codec);
            this.singleStreamCodec = ByteBufCodecs.fromCodecWithRegistries(codec).map(dataComponentPredicate -> new Single<DataComponentPredicate>(this, (DataComponentPredicate)dataComponentPredicate), Single::predicate);
        }

        @Override
        public Codec<T> codec() {
            return this.codec;
        }

        @Override
        public MapCodec<Single<T>> wrappedCodec() {
            return this.wrappedCodec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec() {
            return this.singleStreamCodec;
        }
    }
}

