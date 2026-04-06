/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public interface Holder<T> {
    public T value();

    public boolean isBound();

    public boolean is(Identifier var1);

    public boolean is(ResourceKey<T> var1);

    public boolean is(Predicate<ResourceKey<T>> var1);

    public boolean is(TagKey<T> var1);

    @Deprecated
    public boolean is(Holder<T> var1);

    public Stream<TagKey<T>> tags();

    public Either<ResourceKey<T>, T> unwrap();

    public Optional<ResourceKey<T>> unwrapKey();

    public Kind kind();

    public boolean canSerializeIn(HolderOwner<T> var1);

    default public String getRegisteredName() {
        return this.unwrapKey().map(resourceKey -> resourceKey.identifier().toString()).orElse("[unregistered]");
    }

    public static <T> Holder<T> direct(T object) {
        return new Direct<T>(object);
    }

    public record Direct<T>(T value) implements Holder<T>
    {
        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean is(Identifier identifier) {
            return false;
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return false;
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return false;
        }

        @Override
        public boolean is(Holder<T> holder) {
            return this.value.equals(holder.value());
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return false;
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Kind kind() {
            return Kind.DIRECT;
        }

        public String toString() {
            return "Direct{" + String.valueOf(this.value) + "}";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return true;
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.of(new TagKey[0]);
        }
    }

    public static class Reference<T>
    implements Holder<T> {
        private final HolderOwner<T> owner;
        private @Nullable Set<TagKey<T>> tags;
        private final Type type;
        private @Nullable ResourceKey<T> key;
        private @Nullable T value;

        protected Reference(Type type, HolderOwner<T> holderOwner, @Nullable ResourceKey<T> resourceKey, @Nullable T object) {
            this.owner = holderOwner;
            this.type = type;
            this.key = resourceKey;
            this.value = object;
        }

        public static <T> Reference<T> createStandAlone(HolderOwner<T> holderOwner, ResourceKey<T> resourceKey) {
            return new Reference<Object>(Type.STAND_ALONE, holderOwner, resourceKey, null);
        }

        @Deprecated
        public static <T> Reference<T> createIntrusive(HolderOwner<T> holderOwner, @Nullable T object) {
            return new Reference<T>(Type.INTRUSIVE, holderOwner, null, object);
        }

        public ResourceKey<T> key() {
            if (this.key == null) {
                throw new IllegalStateException("Trying to access unbound value '" + String.valueOf(this.value) + "' from registry " + String.valueOf(this.owner));
            }
            return this.key;
        }

        @Override
        public T value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + String.valueOf(this.key) + "' from registry " + String.valueOf(this.owner));
            }
            return this.value;
        }

        @Override
        public boolean is(Identifier identifier) {
            return this.key().identifier().equals(identifier);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return this.key() == resourceKey;
        }

        private Set<TagKey<T>> boundTags() {
            if (this.tags == null) {
                throw new IllegalStateException("Tags not bound");
            }
            return this.tags;
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return this.boundTags().contains(tagKey);
        }

        @Override
        public boolean is(Holder<T> holder) {
            return holder.is(this.key());
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return predicate.test(this.key());
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return this.owner.canSerializeIn(holderOwner);
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left(this.key());
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(this.key());
        }

        @Override
        public Kind kind() {
            return Kind.REFERENCE;
        }

        @Override
        public boolean isBound() {
            return this.key != null && this.value != null;
        }

        void bindKey(ResourceKey<T> resourceKey) {
            if (this.key != null && resourceKey != this.key) {
                throw new IllegalStateException("Can't change holder key: existing=" + String.valueOf(this.key) + ", new=" + String.valueOf(resourceKey));
            }
            this.key = resourceKey;
        }

        protected void bindValue(T object) {
            if (this.type == Type.INTRUSIVE && this.value != object) {
                throw new IllegalStateException("Can't change holder " + String.valueOf(this.key) + " value: existing=" + String.valueOf(this.value) + ", new=" + String.valueOf(object));
            }
            this.value = object;
        }

        void bindTags(Collection<TagKey<T>> collection) {
            this.tags = Set.copyOf(collection);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return this.boundTags().stream();
        }

        public String toString() {
            return "Reference{" + String.valueOf(this.key) + "=" + String.valueOf(this.value) + "}";
        }

        protected static enum Type {
            STAND_ALONE,
            INTRUSIVE;

        }
    }

    public static enum Kind {
        REFERENCE,
        DIRECT;

    }
}

