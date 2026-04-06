/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class HexColorArgument
implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("F00", "FF0000");
    public static final DynamicCommandExceptionType ERROR_INVALID_HEX = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.hexcolor.invalid", object));

    private HexColorArgument() {
    }

    public static HexColorArgument hexColor() {
        return new HexColorArgument();
    }

    public static Integer getHexColor(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Integer)commandContext.getArgument(string, Integer.class);
    }

    public Integer parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        return switch (string.length()) {
            case 3 -> ARGB.color(HexColorArgument.duplicateDigit(Integer.parseInt((CharSequence)string, (int)0, (int)1, (int)16)), HexColorArgument.duplicateDigit(Integer.parseInt((CharSequence)string, (int)1, (int)2, (int)16)), HexColorArgument.duplicateDigit(Integer.parseInt((CharSequence)string, (int)2, (int)3, (int)16)));
            case 6 -> ARGB.color(Integer.parseInt((CharSequence)string, (int)0, (int)2, (int)16), Integer.parseInt((CharSequence)string, (int)2, (int)4, (int)16), Integer.parseInt((CharSequence)string, (int)4, (int)6, (int)16));
            default -> throw ERROR_INVALID_HEX.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        };
    }

    private static int duplicateDigit(int i) {
        return i * 17;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggest(EXAMPLES, suggestionsBuilder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

