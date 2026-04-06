/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public interface TagAppender<E, T> {
    public TagAppender<E, T> add(E var1);

    default public TagAppender<E, T> add(E ... objects) {
        return this.addAll(Arrays.stream(objects));
    }

    default public TagAppender<E, T> addAll(Collection<E> collection) {
        collection.forEach(this::add);
        return this;
    }

    default public TagAppender<E, T> addAll(Stream<E> stream) {
        stream.forEach(this::add);
        return this;
    }

    public TagAppender<E, T> addOptional(E var1);

    public TagAppender<E, T> addTag(TagKey<T> var1);

    public TagAppender<E, T> addOptionalTag(TagKey<T> var1);

    public static <T> TagAppender<ResourceKey<T>, T> forBuilder(final TagBuilder tagBuilder) {
        return new TagAppender<ResourceKey<T>, T>(){

            @Override
            public TagAppender<ResourceKey<T>, T> add(ResourceKey<T> resourceKey) {
                tagBuilder.addElement(resourceKey.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptional(ResourceKey<T> resourceKey) {
                tagBuilder.addOptionalElement(resourceKey.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addTag(TagKey<T> tagKey) {
                tagBuilder.addTag(tagKey.location());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptionalTag(TagKey<T> tagKey) {
                tagBuilder.addOptionalTag(tagKey.location());
                return this;
            }
        };
    }

    default public <U> TagAppender<U, T> map(final Function<U, E> function) {
        final TagAppender tagAppender = this;
        return new TagAppender<U, T>(this){

            @Override
            public TagAppender<U, T> add(U object) {
                tagAppender.add(function.apply(object));
                return this;
            }

            @Override
            public TagAppender<U, T> addOptional(U object) {
                tagAppender.add(function.apply(object));
                return this;
            }

            @Override
            public TagAppender<U, T> addTag(TagKey<T> tagKey) {
                tagAppender.addTag(tagKey);
                return this;
            }

            @Override
            public TagAppender<U, T> addOptionalTag(TagKey<T> tagKey) {
                tagAppender.addOptionalTag(tagKey);
                return this;
            }
        };
    }
}

