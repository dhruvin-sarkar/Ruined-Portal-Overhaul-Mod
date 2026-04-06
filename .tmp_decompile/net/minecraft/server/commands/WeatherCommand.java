/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
    private static final int DEFAULT_TIME = -1;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("weather").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("clear").executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"duration")))))).then(((LiteralArgumentBuilder)Commands.literal("rain").executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"duration")))))).then(((LiteralArgumentBuilder)Commands.literal("thunder").executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"duration"))))));
    }

    private static int getDuration(CommandSourceStack commandSourceStack, int i, IntProvider intProvider) {
        if (i == -1) {
            return intProvider.sample(commandSourceStack.getServer().overworld().getRandom());
        }
        return i;
    }

    private static int setClear(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getServer().overworld().setWeatherParameters(WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.RAIN_DELAY), 0, false, false);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.clear"), true);
        return i;
    }

    private static int setRain(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getServer().overworld().setWeatherParameters(0, WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.RAIN_DURATION), true, false);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.rain"), true);
        return i;
    }

    private static int setThunder(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getServer().overworld().setWeatherParameters(0, WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.THUNDER_DURATION), true, true);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.thunder"), true);
        return i;
    }
}

