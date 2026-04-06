/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;

public interface Term<S> {
    public boolean parse(ParseState<S> var1, Scope var2, Control var3);

    public static <S, T> Term<S> marker(Atom<T> atom, T object) {
        return new Marker(atom, object);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... terms) {
        return new Sequence<S>(terms);
    }

    @SafeVarargs
    public static <S> Term<S> alternative(Term<S> ... terms) {
        return new Alternative<S>(terms);
    }

    public static <S> Term<S> optional(Term<S> term) {
        return new Maybe<S>(term);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> namedRule, Atom<List<T>> atom) {
        return Term.repeated(namedRule, atom, 0);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> namedRule, Atom<List<T>> atom, int i) {
        return new Repeated<S, T>(namedRule, atom, i);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term) {
        return Term.repeatedWithTrailingSeparator(namedRule, atom, term, 0);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term, int i) {
        return new RepeatedWithSeparator<S, T>(namedRule, atom, term, i, true);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term) {
        return Term.repeatedWithoutTrailingSeparator(namedRule, atom, term, 0);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term, int i) {
        return new RepeatedWithSeparator<S, T>(namedRule, atom, term, i, false);
    }

    public static <S> Term<S> positiveLookahead(Term<S> term) {
        return new LookAhead<S>(term, true);
    }

    public static <S> Term<S> negativeLookahead(Term<S> term) {
        return new LookAhead<S>(term, false);
    }

    public static <S> Term<S> cut() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                control.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> empty() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static <S> Term<S> fail(final Object object) {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                parseState.errorCollector().store(parseState.mark(), object);
                return false;
            }

            public String toString() {
                return "fail";
            }
        };
    }

    public record Marker<S, T>(Atom<T> name, T value) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            scope.put(this.name, this.value);
            return true;
        }
    }

    public record Sequence<S>(Term<S>[] elements) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int i = parseState.mark();
            for (Term<S> term : this.elements) {
                if (term.parse(parseState, scope, control)) continue;
                parseState.restore(i);
                return false;
            }
            return true;
        }
    }

    public record Alternative<S>(Term<S>[] elements) implements Term<S>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            Control control2 = parseState.acquireControl();
            try {
                int i = parseState.mark();
                scope.splitFrame();
                for (Term<S> term : this.elements) {
                    if (term.parse(parseState, scope, control2)) {
                        scope.mergeFrame();
                        boolean bl = true;
                        return bl;
                    }
                    scope.clearFrameValues();
                    parseState.restore(i);
                    if (control2.hasCut()) break;
                }
                scope.popFrame();
                boolean bl = false;
                return bl;
            }
            finally {
                parseState.releaseControl();
            }
        }
    }

    public record Maybe<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int i = parseState.mark();
            if (!this.term.parse(parseState, scope, control)) {
                parseState.restore(i);
            }
            return true;
        }
    }

    public record Repeated<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, int minRepetitions) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int j;
            int i = parseState.mark();
            ArrayList<T> list = new ArrayList<T>(this.minRepetitions);
            while (true) {
                j = parseState.mark();
                T object = parseState.parse(this.element);
                if (object == null) break;
                list.add(object);
            }
            parseState.restore(j);
            if (list.size() < this.minRepetitions) {
                parseState.restore(i);
                return false;
            }
            scope.put(this.listName, list);
            return true;
        }
    }

    public record RepeatedWithSeparator<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int i = parseState.mark();
            ArrayList<T> list = new ArrayList<T>(this.minRepetitions);
            boolean bl = true;
            while (true) {
                int j = parseState.mark();
                if (!bl && !this.separator.parse(parseState, scope, control)) {
                    parseState.restore(j);
                    break;
                }
                int k = parseState.mark();
                T object = parseState.parse(this.element);
                if (object == null) {
                    if (bl) {
                        parseState.restore(k);
                        break;
                    }
                    if (this.allowTrailingSeparator) {
                        parseState.restore(k);
                        break;
                    }
                    parseState.restore(i);
                    return false;
                }
                list.add(object);
                bl = false;
            }
            if (list.size() < this.minRepetitions) {
                parseState.restore(i);
                return false;
            }
            scope.put(this.listName, list);
            return true;
        }
    }

    public record LookAhead<S>(Term<S> term, boolean positive) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int i = parseState.mark();
            boolean bl = this.term.parse(parseState.silent(), scope, control);
            parseState.restore(i);
            return this.positive == bl;
        }
    }
}

