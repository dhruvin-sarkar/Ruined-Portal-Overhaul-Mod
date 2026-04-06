/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.chars.CharList
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharList;
import java.lang.invoke.LambdaMetafactory;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
    public static Term<StringReader> word(String string) {
        return new TerminalWord(string);
    }

    public static Term<StringReader> character(final char c) {
        return new TerminalCharacters(CharList.of((char)c)){

            @Override
            protected boolean isAccepted(char c2) {
                return c == c2;
            }
        };
    }

    public static Term<StringReader> characters(final char c, final char d) {
        return new TerminalCharacters(CharList.of((char)c, (char)d)){

            @Override
            protected boolean isAccepted(char c2) {
                return c2 == c || c2 == d;
            }
        };
    }

    public static StringReader createReader(String string, int i) {
        StringReader stringReader = new StringReader(string);
        stringReader.setCursor(i);
        return stringReader;
    }

    public static final class TerminalWord
    implements Term<StringReader> {
        private final String value;
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalWord(String string) {
            this.value = string;
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), string);
            this.suggestions = parseState -> Stream.of(string);
        }

        @Override
        public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
            parseState.input().skipWhitespace();
            int i = parseState.mark();
            String string = parseState.input().readUnquotedString();
            if (!string.equals(this.value)) {
                parseState.errorCollector().store(i, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        public String toString() {
            return "terminal[" + this.value + "]";
        }
    }

    public static abstract class TerminalCharacters
    implements Term<StringReader> {
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalCharacters(CharList charList) {
            String string = charList.intStream().mapToObj((IntFunction<String>)LambdaMetafactory.metafactory(null, null, null, (I)Ljava/lang/Object;, toString(int ), (I)Ljava/lang/String;)()).collect(Collectors.joining("|"));
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), string);
            this.suggestions = parseState -> charList.intStream().mapToObj((IntFunction<String>)LambdaMetafactory.metafactory(null, null, null, (I)Ljava/lang/Object;, toString(int ), (I)Ljava/lang/String;)());
        }

        @Override
        public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
            parseState.input().skipWhitespace();
            int i = parseState.mark();
            if (!parseState.input().canRead() || !this.isAccepted(parseState.input().read())) {
                parseState.errorCollector().store(i, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        protected abstract boolean isAccepted(char var1);
    }
}

