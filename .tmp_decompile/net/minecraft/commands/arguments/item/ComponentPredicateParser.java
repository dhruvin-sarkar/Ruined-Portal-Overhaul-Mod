/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.IdentifierParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {
    public static <T, C, P> Grammar<List<T>> createGrammar(Context<T, C, P> context) {
        Atom atom = Atom.of("top");
        Atom atom2 = Atom.of("type");
        Atom atom3 = Atom.of("any_type");
        Atom atom4 = Atom.of("element_type");
        Atom atom5 = Atom.of("tag_type");
        Atom atom6 = Atom.of("conditions");
        Atom atom7 = Atom.of("alternatives");
        Atom atom8 = Atom.of("term");
        Atom atom9 = Atom.of("negation");
        Atom atom10 = Atom.of("test");
        Atom atom11 = Atom.of("component_type");
        Atom atom12 = Atom.of("predicate_type");
        Atom atom13 = Atom.of("id");
        Atom atom14 = Atom.of("tag");
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        NamedRule<StringReader, Identifier> namedRule = dictionary.put(atom13, IdentifierParseRule.INSTANCE);
        NamedRule namedRule2 = dictionary.put(atom, Term.alternative(Term.sequence(dictionary.named(atom2), StringReaderTerms.character('['), Term.cut(), Term.optional(dictionary.named(atom6)), StringReaderTerms.character(']')), dictionary.named(atom2)), scope -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)scope.getOrThrow(atom2)).ifPresent(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
            List list = (List)scope.get(atom6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        dictionary.put(atom2, Term.alternative(dictionary.named(atom4), Term.sequence(StringReaderTerms.character('#'), Term.cut(), dictionary.named(atom5)), dictionary.named(atom3)), scope -> Optional.ofNullable(scope.getAny(atom4, atom5)));
        dictionary.put(atom3, StringReaderTerms.character('*'), scope -> Unit.INSTANCE);
        dictionary.put(atom4, new ElementLookupRule<T, C, P>(namedRule, context));
        dictionary.put(atom5, new TagLookupRule<T, C, P>(namedRule, context));
        dictionary.put(atom6, Term.sequence(dictionary.named(atom7), Term.optional(Term.sequence(StringReaderTerms.character(','), dictionary.named(atom6)))), scope -> {
            Object object = context.anyOf((List)scope.getOrThrow(atom7));
            return Optional.ofNullable((List)scope.get(atom6)).map(list -> Util.copyAndAdd(object, list)).orElse(List.of(object));
        });
        dictionary.put(atom7, Term.sequence(dictionary.named(atom8), Term.optional(Term.sequence(StringReaderTerms.character('|'), dictionary.named(atom7)))), scope -> {
            Object object = scope.getOrThrow(atom8);
            return Optional.ofNullable((List)scope.get(atom7)).map(list -> Util.copyAndAdd(object, list)).orElse(List.of(object));
        });
        dictionary.put(atom8, Term.alternative(dictionary.named(atom10), Term.sequence(StringReaderTerms.character('!'), dictionary.named(atom9))), scope -> scope.getAnyOrThrow(atom10, atom9));
        dictionary.put(atom9, dictionary.named(atom10), scope -> context.negate(scope.getOrThrow(atom10)));
        dictionary.putComplex(atom10, Term.alternative(Term.sequence(dictionary.named(atom11), StringReaderTerms.character('='), Term.cut(), dictionary.named(atom14)), Term.sequence(dictionary.named(atom12), StringReaderTerms.character('~'), Term.cut(), dictionary.named(atom14)), dictionary.named(atom11)), parseState -> {
            Scope scope = parseState.scope();
            Object object = scope.get(atom12);
            try {
                if (object != null) {
                    Dynamic dynamic = (Dynamic)scope.getOrThrow(atom14);
                    return context.createPredicateTest((ImmutableStringReader)parseState.input(), object, dynamic);
                }
                Object object2 = scope.getOrThrow(atom11);
                Dynamic dynamic2 = (Dynamic)scope.get(atom14);
                return dynamic2 != null ? context.createComponentTest((ImmutableStringReader)parseState.input(), object2, dynamic2) : context.createComponentTest((ImmutableStringReader)parseState.input(), object2);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                parseState.errorCollector().store(parseState.mark(), (Object)commandSyntaxException);
                return null;
            }
        });
        dictionary.put(atom11, new ComponentLookupRule<T, C, P>(namedRule, context));
        dictionary.put(atom12, new PredicateLookupRule<T, C, P>(namedRule, context));
        dictionary.put(atom14, new TagParseRule<Tag>(NbtOps.INSTANCE));
        return new Grammar<List<T>>(dictionary, namedRule2);
    }

    static class ElementLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        ElementLookupRule(NamedRule<StringReader, Identifier> namedRule, Context<T, C, P> context) {
            super(namedRule, context);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutableStringReader, Identifier identifier) throws Exception {
            return ((Context)this.context).forElementType(immutableStringReader, identifier);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listElementTypes();
        }
    }

    public static interface Context<T, C, P> {
        public T forElementType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listElementTypes();

        public T forTagType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listTagTypes();

        public C lookupComponentType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listComponentTypes();

        public T createComponentTest(ImmutableStringReader var1, C var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T createComponentTest(ImmutableStringReader var1, C var2);

        public P lookupPredicateType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listPredicateTypes();

        public T createPredicateTest(ImmutableStringReader var1, P var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        TagLookupRule(NamedRule<StringReader, Identifier> namedRule, Context<T, C, P> context) {
            super(namedRule, context);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutableStringReader, Identifier identifier) throws Exception {
            return ((Context)this.context).forTagType(immutableStringReader, identifier);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listTagTypes();
        }
    }

    static class ComponentLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, C> {
        ComponentLookupRule(NamedRule<StringReader, Identifier> namedRule, Context<T, C, P> context) {
            super(namedRule, context);
        }

        @Override
        protected C validateElement(ImmutableStringReader immutableStringReader, Identifier identifier) throws Exception {
            return ((Context)this.context).lookupComponentType(immutableStringReader, identifier);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listComponentTypes();
        }
    }

    static class PredicateLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, P> {
        PredicateLookupRule(NamedRule<StringReader, Identifier> namedRule, Context<T, C, P> context) {
            super(namedRule, context);
        }

        @Override
        protected P validateElement(ImmutableStringReader immutableStringReader, Identifier identifier) throws Exception {
            return ((Context)this.context).lookupPredicateType(immutableStringReader, identifier);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listPredicateTypes();
        }
    }
}

