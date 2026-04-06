/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapDecoder
 *  com.mojang.serialization.MapEncoder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;

public class ComponentSerialization {
    public static final Codec<Component> CODEC = Codec.recursive((String)"Component", ComponentSerialization::createCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> TRUSTED_OPTIONAL_STREAM_CODEC = TRUSTED_STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<ByteBuf, Component> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);

    public static Codec<Component> flatRestrictedCodec(final int i) {
        return new Codec<Component>(){

            public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> dynamicOps, T object) {
                return CODEC.decode(dynamicOps, object).flatMap(pair -> {
                    if (this.isTooLarge(dynamicOps, (Component)pair.getFirst())) {
                        return DataResult.error(() -> "Component was too large: greater than max size " + i);
                    }
                    return DataResult.success((Object)pair);
                });
            }

            public <T> DataResult<T> encode(Component component, DynamicOps<T> dynamicOps, T object) {
                return CODEC.encodeStart(dynamicOps, (Object)component);
            }

            private <T> boolean isTooLarge(DynamicOps<T> dynamicOps, Component component) {
                DataResult dataResult = CODEC.encodeStart(1.asJsonOps(dynamicOps), (Object)component);
                return dataResult.isSuccess() && GsonHelper.encodesLongerThan((JsonElement)dataResult.getOrThrow(), i);
            }

            private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> dynamicOps) {
                if (dynamicOps instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)dynamicOps;
                    return registryOps.withParent(JsonOps.INSTANCE);
                }
                return JsonOps.INSTANCE;
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((Component)object, dynamicOps, object2);
            }
        };
    }

    private static MutableComponent createFromList(List<Component> list) {
        MutableComponent mutableComponent = list.get(0).copy();
        for (int i = 1; i < list.size(); ++i) {
            mutableComponent.append(list.get(i));
        }
        return mutableComponent;
    }

    public static <T> MapCodec<T> createLegacyComponentMatcher(ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends T>> lateBoundIdMapper, Function<T, MapCodec<? extends T>> function, String string) {
        FuzzyCodec<? extends T> mapCodec2 = new FuzzyCodec<T>(lateBoundIdMapper.values(), function);
        MapCodec mapCodec22 = lateBoundIdMapper.codec((Codec<String>)Codec.STRING).dispatchMap(string, function, mapCodec -> mapCodec);
        StrictEither<? extends T> mapCodec3 = new StrictEither<T>(string, mapCodec22, mapCodec2);
        return ExtraCodecs.orCompressed(mapCodec3, mapCodec22);
    }

    private static Codec<Component> createCodec(Codec<Component> codec) {
        ExtraCodecs.LateBoundIdMapper lateBoundIdMapper = new ExtraCodecs.LateBoundIdMapper();
        ComponentSerialization.bootstrap(lateBoundIdMapper);
        MapCodec<ComponentContents> mapCodec = ComponentSerialization.createLegacyComponentMatcher(lateBoundIdMapper, ComponentContents::codec, "type");
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group((App)mapCodec.forGetter(Component::getContents), (App)ExtraCodecs.nonEmptyList(codec.listOf()).optionalFieldOf("extra", (Object)List.of()).forGetter(Component::getSiblings), (App)Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)).apply((Applicative)instance, MutableComponent::new));
        return Codec.either((Codec)Codec.either((Codec)Codec.STRING, ExtraCodecs.nonEmptyList(codec.listOf())), (Codec)codec2).xmap(either2 -> (Component)either2.map(either -> (Component)either.map(Component::literal, ComponentSerialization::createFromList), component -> component), component -> {
            String string = component.tryCollapseToString();
            return string != null ? Either.left((Object)Either.left((Object)string)) : Either.right((Object)component);
        });
    }

    private static void bootstrap(ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> lateBoundIdMapper) {
        lateBoundIdMapper.put("text", PlainTextContents.MAP_CODEC);
        lateBoundIdMapper.put("translatable", TranslatableContents.MAP_CODEC);
        lateBoundIdMapper.put("keybind", KeybindContents.MAP_CODEC);
        lateBoundIdMapper.put("score", ScoreContents.MAP_CODEC);
        lateBoundIdMapper.put("selector", SelectorContents.MAP_CODEC);
        lateBoundIdMapper.put("nbt", NbtContents.MAP_CODEC);
        lateBoundIdMapper.put("object", ObjectContents.MAP_CODEC);
    }

    static class FuzzyCodec<T>
    extends MapCodec<T> {
        private final Collection<MapCodec<? extends T>> codecs;
        private final Function<T, ? extends MapEncoder<? extends T>> encoderGetter;

        public FuzzyCodec(Collection<MapCodec<? extends T>> collection, Function<T, ? extends MapEncoder<? extends T>> function) {
            this.codecs = collection;
            this.encoderGetter = function;
        }

        public <S> DataResult<T> decode(DynamicOps<S> dynamicOps, MapLike<S> mapLike) {
            for (MapDecoder mapDecoder : this.codecs) {
                DataResult dataResult = mapDecoder.decode(dynamicOps, mapLike);
                if (!dataResult.result().isPresent()) continue;
                return dataResult;
            }
            return DataResult.error(() -> "No matching codec found");
        }

        public <S> RecordBuilder<S> encode(T object, DynamicOps<S> dynamicOps, RecordBuilder<S> recordBuilder) {
            MapEncoder<? extends T> mapEncoder = this.encoderGetter.apply(object);
            return mapEncoder.encode(object, dynamicOps, recordBuilder);
        }

        public <S> Stream<S> keys(DynamicOps<S> dynamicOps) {
            return this.codecs.stream().flatMap(mapCodec -> mapCodec.keys(dynamicOps)).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    static class StrictEither<T>
    extends MapCodec<T> {
        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public StrictEither(String string, MapCodec<T> mapCodec, MapCodec<T> mapCodec2) {
            this.typeFieldName = string;
            this.typed = mapCodec;
            this.fuzzy = mapCodec2;
        }

        public <O> DataResult<T> decode(DynamicOps<O> dynamicOps, MapLike<O> mapLike) {
            if (mapLike.get(this.typeFieldName) != null) {
                return this.typed.decode(dynamicOps, mapLike);
            }
            return this.fuzzy.decode(dynamicOps, mapLike);
        }

        public <O> RecordBuilder<O> encode(T object, DynamicOps<O> dynamicOps, RecordBuilder<O> recordBuilder) {
            return this.fuzzy.encode(object, dynamicOps, recordBuilder);
        }

        public <T1> Stream<T1> keys(DynamicOps<T1> dynamicOps) {
            return Stream.concat(this.typed.keys(dynamicOps), this.fuzzy.keys(dynamicOps)).distinct();
        }
    }
}

