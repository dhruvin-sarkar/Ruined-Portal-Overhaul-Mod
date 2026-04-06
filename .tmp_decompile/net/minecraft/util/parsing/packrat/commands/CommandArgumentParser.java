/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandArgumentParser<T> {
    public T parseForCommands(StringReader var1) throws CommandSyntaxException;

    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder var1);

    default public <S> CommandArgumentParser<S> mapResult(final Function<T, S> function) {
        return new CommandArgumentParser<S>(){

            @Override
            public S parseForCommands(StringReader stringReader) throws CommandSyntaxException {
                return function.apply(CommandArgumentParser.this.parseForCommands(stringReader));
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsBuilder) {
                return CommandArgumentParser.this.parseForSuggestions(suggestionsBuilder);
            }
        };
    }

    default public <T, O> CommandArgumentParser<T> withCodec(final DynamicOps<O> dynamicOps, final CommandArgumentParser<O> commandArgumentParser, final Codec<T> codec, final DynamicCommandExceptionType dynamicCommandExceptionType) {
        return new CommandArgumentParser<T>(){

            @Override
            public T parseForCommands(StringReader stringReader) throws CommandSyntaxException {
                int i = stringReader.getCursor();
                Object object = commandArgumentParser.parseForCommands(stringReader);
                DataResult dataResult = codec.parse(dynamicOps, object);
                return dataResult.getOrThrow(string -> {
                    stringReader.setCursor(i);
                    return dynamicCommandExceptionType.createWithContext((ImmutableStringReader)stringReader, string);
                });
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsBuilder) {
                return CommandArgumentParser.this.parseForSuggestions(suggestionsBuilder);
            }
        };
    }
}

