/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Stopwatch;
import net.minecraft.world.Stopwatches;

public class StopwatchCommand {
    private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.stopwatch.already_exists", object));
    public static final DynamicCommandExceptionType ERROR_DOES_NOT_EXIST = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.stopwatch.does_not_exist", object));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_STOPWATCHES = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(((CommandSourceStack)commandContext.getSource()).getServer().getStopwatches().ids(), suggestionsBuilder);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stopwatch").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("create").then(Commands.argument("id", IdentifierArgument.id()).executes(commandContext -> StopwatchCommand.createStopwatch((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id")))))).then(Commands.literal("query").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_STOPWATCHES).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> StopwatchCommand.queryStopwatch((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))))).executes(commandContext -> StopwatchCommand.queryStopwatch((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), 1.0))))).then(Commands.literal("restart").then(Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_STOPWATCHES).executes(commandContext -> StopwatchCommand.restartStopwatch((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id")))))).then(Commands.literal("remove").then(Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_STOPWATCHES).executes(commandContext -> StopwatchCommand.removeStopwatch((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"))))));
    }

    private static int createStopwatch(CommandSourceStack commandSourceStack, Identifier identifier) throws CommandSyntaxException {
        Stopwatch stopwatch;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        Stopwatches stopwatches = minecraftServer.getStopwatches();
        if (!stopwatches.add(identifier, stopwatch = new Stopwatch(Stopwatches.currentTime()))) {
            throw ERROR_ALREADY_EXISTS.create((Object)identifier);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopwatch.create.success", Component.translationArg(identifier)), true);
        return 1;
    }

    private static int queryStopwatch(CommandSourceStack commandSourceStack, Identifier identifier, double d) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        Stopwatches stopwatches = minecraftServer.getStopwatches();
        Stopwatch stopwatch = stopwatches.get(identifier);
        if (stopwatch == null) {
            throw ERROR_DOES_NOT_EXIST.create((Object)identifier);
        }
        long l = Stopwatches.currentTime();
        double e = stopwatch.elapsedSeconds(l);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopwatch.query", Component.translationArg(identifier), e), true);
        return (int)(e * d);
    }

    private static int restartStopwatch(CommandSourceStack commandSourceStack, Identifier identifier) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        Stopwatches stopwatches = minecraftServer.getStopwatches();
        if (!stopwatches.update(identifier, stopwatch -> new Stopwatch(Stopwatches.currentTime()))) {
            throw ERROR_DOES_NOT_EXIST.create((Object)identifier);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopwatch.restart.success", Component.translationArg(identifier)), true);
        return 1;
    }

    private static int removeStopwatch(CommandSourceStack commandSourceStack, Identifier identifier) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        Stopwatches stopwatches = minecraftServer.getStopwatches();
        if (!stopwatches.remove(identifier)) {
            throw ERROR_DOES_NOT_EXIST.create((Object)identifier);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopwatch.remove.success", Component.translationArg(identifier)), true);
        return 1;
    }
}

