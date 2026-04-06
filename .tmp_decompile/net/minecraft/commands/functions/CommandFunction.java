/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.functions.FunctionBuilder;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface CommandFunction<T> {
    public Identifier id();

    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag var1, CommandDispatcher<T> var2) throws FunctionInstantiationException;

    private static boolean shouldConcatenateNextLine(CharSequence charSequence) {
        int i = charSequence.length();
        return i > 0 && charSequence.charAt(i - 1) == '\\';
    }

    public static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(Identifier identifier, CommandDispatcher<T> commandDispatcher, T executionCommandSource, List<String> list) {
        FunctionBuilder<T> functionBuilder = new FunctionBuilder<T>();
        for (int i = 0; i < list.size(); ++i) {
            String string3;
            String string2;
            int j = i + 1;
            String string = list.get(i).trim();
            if (CommandFunction.shouldConcatenateNextLine(string)) {
                StringBuilder stringBuilder = new StringBuilder(string);
                do {
                    if (++i == list.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    string2 = list.get(i).trim();
                    stringBuilder.append(string2);
                    CommandFunction.checkCommandLineLength(stringBuilder);
                } while (CommandFunction.shouldConcatenateNextLine(stringBuilder));
                string3 = stringBuilder.toString();
            } else {
                string3 = string;
            }
            CommandFunction.checkCommandLineLength(string3);
            StringReader stringReader = new StringReader(string3);
            if (!stringReader.canRead() || stringReader.peek() == '#') continue;
            if (stringReader.peek() == '/') {
                stringReader.skip();
                if (stringReader.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                }
                string2 = stringReader.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }
            if (stringReader.peek() == '$') {
                functionBuilder.addMacro(string3.substring(1), j, executionCommandSource);
                continue;
            }
            try {
                functionBuilder.addCommand(CommandFunction.parseCommand(commandDispatcher, executionCommandSource, stringReader));
                continue;
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandSyntaxException.getMessage());
            }
        }
        return functionBuilder.build(identifier);
    }

    public static void checkCommandLineLength(CharSequence charSequence) {
        if (charSequence.length() > 2000000) {
            CharSequence charSequence2 = charSequence.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + charSequence.length() + " characters, contents: " + String.valueOf(charSequence2) + "...");
        }
    }

    public static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(CommandDispatcher<T> commandDispatcher, T executionCommandSource, StringReader stringReader) throws CommandSyntaxException {
        ParseResults parseResults = commandDispatcher.parse(stringReader, executionCommandSource);
        Commands.validateParseResults(parseResults);
        Optional optional = ContextChain.tryFlatten((CommandContext)parseResults.getContext().build(stringReader.getString()));
        if (optional.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
        }
        return new BuildContexts.Unbound(stringReader.getString(), (ContextChain)optional.get());
    }
}

