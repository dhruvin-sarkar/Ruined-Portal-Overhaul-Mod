/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record KeyValueCondition(Map<String, Terms> tests) implements Condition
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<KeyValueCondition> CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap((Codec)Codec.STRING, Terms.CODEC)).xmap(KeyValueCondition::new, KeyValueCondition::tests);

    @Override
    public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> stateDefinition) {
        ArrayList list = new ArrayList(this.tests.size());
        this.tests.forEach((string, terms) -> list.add(KeyValueCondition.instantiate(stateDefinition, string, terms)));
        return Util.allOf(list);
    }

    private static <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> stateDefinition, String string, Terms terms) {
        Property<?> property = stateDefinition.getProperty(string);
        if (property == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", string, stateDefinition.getOwner()));
        }
        return terms.instantiate(stateDefinition.getOwner(), property);
    }

    @Environment(value=EnvType.CLIENT)
    public record Terms(List<Term> entries) {
        private static final char SEPARATOR = '|';
        private static final Joiner JOINER = Joiner.on((char)'|');
        private static final Splitter SPLITTER = Splitter.on((char)'|');
        private static final Codec<String> LEGACY_REPRESENTATION_CODEC = Codec.either((Codec)Codec.INT, (Codec)Codec.BOOL).flatComapMap(either -> (String)either.map(String::valueOf, String::valueOf), string -> DataResult.error(() -> "This codec can't be used for encoding"));
        public static final Codec<Terms> CODEC = Codec.withAlternative((Codec)Codec.STRING, LEGACY_REPRESENTATION_CODEC).comapFlatMap(Terms::parse, Terms::toString);

        public Terms {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Empty value for property");
            }
        }

        public static DataResult<Terms> parse(String string) {
            List list = SPLITTER.splitToStream((CharSequence)string).map(Term::parse).toList();
            if (list.isEmpty()) {
                return DataResult.error(() -> "Empty value for property");
            }
            for (Term term : list) {
                if (!term.value.isEmpty()) continue;
                return DataResult.error(() -> "Empty term in value '" + string + "'");
            }
            return DataResult.success((Object)((Object)new Terms(list)));
        }

        public String toString() {
            return JOINER.join(this.entries);
        }

        public <O, S extends StateHolder<O, S>, T extends Comparable<T>> Predicate<S> instantiate(O object, Property<T> property) {
            ArrayList list2;
            boolean bl;
            Predicate predicate = Util.anyOf(Lists.transform(this.entries, term -> this.instantiate(object, property, (Term)((Object)term))));
            ArrayList list = new ArrayList(property.getPossibleValues());
            int i = list.size();
            list.removeIf(predicate.negate());
            int j = list.size();
            if (j == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always false", new Object[]{this, property.getName(), object});
                return stateHolder -> false;
            }
            int k = i - j;
            if (k == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always true", new Object[]{this, property.getName(), object});
                return stateHolder -> true;
            }
            if (j <= k) {
                bl = false;
                list2 = list;
            } else {
                bl = true;
                ArrayList<T> list3 = new ArrayList<T>(property.getPossibleValues());
                list3.removeIf(predicate);
                list2 = list3;
            }
            if (list2.size() == 1) {
                Comparable comparable = (Comparable)list2.getFirst();
                return stateHolder -> {
                    Object comparable2 = stateHolder.getValue(property);
                    return comparable.equals(comparable2) ^ bl;
                };
            }
            return stateHolder -> {
                Object comparable = stateHolder.getValue(property);
                return list2.contains(comparable) ^ bl;
            };
        }

        private <T extends Comparable<T>> T getValueOrThrow(Object object, Property<T> property, String string) {
            Optional<T> optional = property.getValue(string);
            if (optional.isEmpty()) {
                throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", new Object[]{string, property, object, this}));
            }
            return (T)((Comparable)optional.get());
        }

        private <T extends Comparable<T>> Predicate<T> instantiate(Object object, Property<T> property, Term term) {
            Object comparable = this.getValueOrThrow(object, property, term.value);
            if (term.negated) {
                return comparable2 -> !comparable2.equals(comparable);
            }
            return comparable2 -> comparable2.equals(comparable);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Term
    extends Record {
        final String value;
        final boolean negated;
        private static final String NEGATE = "!";

        public Term(String string, boolean bl) {
            if (string.isEmpty()) {
                throw new IllegalArgumentException("Empty term");
            }
            this.value = string;
            this.negated = bl;
        }

        public static Term parse(String string) {
            if (string.startsWith(NEGATE)) {
                return new Term(string.substring(1), true);
            }
            return new Term(string, false);
        }

        public String toString() {
            return this.negated ? NEGATE + this.value : this.value;
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Term.class, "value;negated", "value", "negated"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Term.class, "value;negated", "value", "negated"}, this, object);
        }

        public String value() {
            return this.value;
        }

        public boolean negated() {
            return this.negated;
        }
    }
}

