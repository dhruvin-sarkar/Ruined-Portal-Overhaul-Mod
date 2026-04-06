/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class DataComponentExactPredicate
implements Predicate<DataComponentGetter> {
    public static final Codec<DataComponentExactPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC.xmap(map -> new DataComponentExactPredicate(map.entrySet().stream().map(TypedDataComponent::fromEntryUnchecked).collect(Collectors.toList())), dataComponentExactPredicate -> dataComponentExactPredicate.expectedComponents.stream().filter(typedDataComponent -> !typedDataComponent.type().isTransient()).collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value)));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentExactPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataComponentExactPredicate::new, dataComponentExactPredicate -> dataComponentExactPredicate.expectedComponents);
    public static final DataComponentExactPredicate EMPTY = new DataComponentExactPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentExactPredicate(List<TypedDataComponent<?>> list) {
        this.expectedComponents = list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static <T> DataComponentExactPredicate expect(DataComponentType<T> dataComponentType, T object) {
        return new DataComponentExactPredicate(List.of(new TypedDataComponent<T>(dataComponentType, object)));
    }

    public static DataComponentExactPredicate allOf(DataComponentMap dataComponentMap) {
        return new DataComponentExactPredicate((List<TypedDataComponent<?>>)ImmutableList.copyOf((Iterable)dataComponentMap));
    }

    public static DataComponentExactPredicate someOf(DataComponentMap dataComponentMap, DataComponentType<?> ... dataComponentTypes) {
        Builder builder = new Builder();
        for (DataComponentType<?> dataComponentType : dataComponentTypes) {
            TypedDataComponent<?> typedDataComponent = dataComponentMap.getTyped(dataComponentType);
            if (typedDataComponent == null) continue;
            builder.expect(typedDataComponent);
        }
        return builder.build();
    }

    public boolean isEmpty() {
        return this.expectedComponents.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (!(object instanceof DataComponentExactPredicate)) return false;
        DataComponentExactPredicate dataComponentExactPredicate = (DataComponentExactPredicate)object;
        if (!this.expectedComponents.equals(dataComponentExactPredicate.expectedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    public String toString() {
        return this.expectedComponents.toString();
    }

    @Override
    public boolean test(DataComponentGetter dataComponentGetter) {
        for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
            Object object = dataComponentGetter.get(typedDataComponent.type());
            if (Objects.equals(typedDataComponent.value(), object)) continue;
            return false;
        }
        return true;
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
            builder.set(typedDataComponent);
        }
        return builder.build();
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((DataComponentGetter)object);
    }

    public static class Builder {
        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList();

        Builder() {
        }

        public <T> Builder expect(TypedDataComponent<T> typedDataComponent) {
            return this.expect(typedDataComponent.type(), typedDataComponent.value());
        }

        public <T> Builder expect(DataComponentType<? super T> dataComponentType, T object) {
            for (TypedDataComponent<?> typedDataComponent : this.expectedComponents) {
                if (typedDataComponent.type() != dataComponentType) continue;
                throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(dataComponentType) + "'");
            }
            this.expectedComponents.add(new TypedDataComponent<T>(dataComponentType, object));
            return this;
        }

        public DataComponentExactPredicate build() {
            return new DataComponentExactPredicate(List.copyOf(this.expectedComponents));
        }
    }
}

