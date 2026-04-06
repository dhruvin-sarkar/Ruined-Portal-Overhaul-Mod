/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType((Message)Component.translatable("commands.schedule.same_tick"));
    private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.schedule.cleared.failure", object));
    private static final SimpleCommandExceptionType ERROR_MACRO = new SimpleCommandExceptionType((Message)Component.translatableEscape("commands.schedule.macro", new Object[0]));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), suggestionsBuilder);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("schedule").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("function").then(Commands.argument("function", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("time", TimeArgument.time()).executes(commandContext -> ScheduleCommand.schedule((CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctionOrTag((CommandContext<CommandSourceStack>)commandContext, "function"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time"), true))).then(Commands.literal("append").executes(commandContext -> ScheduleCommand.schedule((CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctionOrTag((CommandContext<CommandSourceStack>)commandContext, "function"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time"), false)))).then(Commands.literal("replace").executes(commandContext -> ScheduleCommand.schedule((CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctionOrTag((CommandContext<CommandSourceStack>)commandContext, "function"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time"), true))))))).then(Commands.literal("clear").then(Commands.argument("function", StringArgumentType.greedyString()).suggests(SUGGEST_SCHEDULE).executes(commandContext -> ScheduleCommand.remove((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"function"))))));
    }

    private static int schedule(CommandSourceStack commandSourceStack, Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> pair, int i, boolean bl) throws CommandSyntaxException {
        if (i == 0) {
            throw ERROR_SAME_TICK.create();
        }
        long l = commandSourceStack.getLevel().getGameTime() + (long)i;
        Identifier identifier = (Identifier)pair.getFirst();
        TimerQueue<MinecraftServer> timerQueue = commandSourceStack.getServer().getWorldData().overworldData().getScheduledEvents();
        Optional optional = ((Either)pair.getSecond()).left();
        if (optional.isPresent()) {
            if (optional.get() instanceof MacroFunction) {
                throw ERROR_MACRO.create();
            }
            String string = identifier.toString();
            if (bl) {
                timerQueue.remove(string);
            }
            timerQueue.schedule(string, l, new FunctionCallback(identifier));
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.schedule.created.function", Component.translationArg(identifier), i, l), true);
        } else {
            String string = "#" + String.valueOf(identifier);
            if (bl) {
                timerQueue.remove(string);
            }
            timerQueue.schedule(string, l, new FunctionTagCallback(identifier));
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.schedule.created.tag", Component.translationArg(identifier), i, l), true);
        }
        return Math.floorMod((long)l, (int)Integer.MAX_VALUE);
    }

    private static int remove(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
        int i = commandSourceStack.getServer().getWorldData().overworldData().getScheduledEvents().remove(string);
        if (i == 0) {
            throw ERROR_CANT_REMOVE.create((Object)string);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.schedule.cleared.success", i, string), true);
        return i;
    }
}

