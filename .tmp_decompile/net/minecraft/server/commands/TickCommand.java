/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeUtil;

public class TickCommand {
    private static final float MAX_TICKRATE = 10000.0f;
    private static final String DEFAULT_TICKRATE = String.valueOf(20);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tick").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.literal("query").executes(commandContext -> TickCommand.tickQuery((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("rate").then(Commands.argument("rate", FloatArgumentType.floatArg((float)1.0f, (float)10000.0f)).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{DEFAULT_TICKRATE}, suggestionsBuilder)).executes(commandContext -> TickCommand.setTickingRate((CommandSourceStack)commandContext.getSource(), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"rate")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("step").executes(commandContext -> TickCommand.step((CommandSourceStack)commandContext.getSource(), 1))).then(Commands.literal("stop").executes(commandContext -> TickCommand.stopStepping((CommandSourceStack)commandContext.getSource())))).then(Commands.argument("time", TimeArgument.time(1)).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{"1t", "1s"}, suggestionsBuilder)).executes(commandContext -> TickCommand.step((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time")))))).then(((LiteralArgumentBuilder)Commands.literal("sprint").then(Commands.literal("stop").executes(commandContext -> TickCommand.stopSprinting((CommandSourceStack)commandContext.getSource())))).then(Commands.argument("time", TimeArgument.time(1)).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{"60s", "1d", "3d"}, suggestionsBuilder)).executes(commandContext -> TickCommand.sprint((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time")))))).then(Commands.literal("unfreeze").executes(commandContext -> TickCommand.setFreeze((CommandSourceStack)commandContext.getSource(), false)))).then(Commands.literal("freeze").executes(commandContext -> TickCommand.setFreeze((CommandSourceStack)commandContext.getSource(), true))));
    }

    private static String nanosToMilisString(long l) {
        return String.format(Locale.ROOT, "%.1f", Float.valueOf((float)l / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND));
    }

    private static int setTickingRate(CommandSourceStack commandSourceStack, float f) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        serverTickRateManager.setTickRate(f);
        String string = String.format(Locale.ROOT, "%.1f", Float.valueOf(f));
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.rate.success", string), true);
        return (int)f;
    }

    private static int tickQuery(CommandSourceStack commandSourceStack) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        String string = TickCommand.nanosToMilisString(commandSourceStack.getServer().getAverageTickTimeNanos());
        float f = serverTickRateManager.tickrate();
        String string2 = String.format(Locale.ROOT, "%.1f", Float.valueOf(f));
        if (serverTickRateManager.isSprinting()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), false);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.rate.sprinting", string2, string), false);
        } else {
            if (serverTickRateManager.isFrozen()) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), false);
            } else if (serverTickRateManager.nanosecondsPerTick() < commandSourceStack.getServer().getAverageTickTimeNanos()) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.lagging"), false);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.running"), false);
            }
            String string3 = TickCommand.nanosToMilisString(serverTickRateManager.nanosecondsPerTick());
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.rate.running", string2, string, string3), false);
        }
        long[] ls = Arrays.copyOf(commandSourceStack.getServer().getTickTimesNanos(), commandSourceStack.getServer().getTickTimesNanos().length);
        Arrays.sort(ls);
        String string4 = TickCommand.nanosToMilisString(ls[ls.length / 2]);
        String string5 = TickCommand.nanosToMilisString(ls[(int)((double)ls.length * 0.95)]);
        String string6 = TickCommand.nanosToMilisString(ls[(int)((double)ls.length * 0.99)]);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.percentiles", string4, string5, string6, ls.length), false);
        return (int)f;
    }

    private static int sprint(CommandSourceStack commandSourceStack, int i) {
        boolean bl = commandSourceStack.getServer().tickRateManager().requestGameToSprint(i);
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), true);
        return 1;
    }

    private static int setFreeze(CommandSourceStack commandSourceStack, boolean bl) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        if (bl) {
            if (serverTickRateManager.isSprinting()) {
                serverTickRateManager.stopSprinting();
            }
            if (serverTickRateManager.isSteppingForward()) {
                serverTickRateManager.stopStepping();
            }
        }
        serverTickRateManager.setFrozen(bl);
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.running"), true);
        }
        return bl ? 1 : 0;
    }

    private static int step(CommandSourceStack commandSourceStack, int i) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        boolean bl = serverTickRateManager.stepGameIfPaused(i);
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.step.success", i), true);
        } else {
            commandSourceStack.sendFailure(Component.translatable("commands.tick.step.fail"));
        }
        return 1;
    }

    private static int stopStepping(CommandSourceStack commandSourceStack) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        boolean bl = serverTickRateManager.stopStepping();
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.step.stop.success"), true);
            return 1;
        }
        commandSourceStack.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
        return 0;
    }

    private static int stopSprinting(CommandSourceStack commandSourceStack) {
        ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
        boolean bl = serverTickRateManager.stopSprinting();
        if (bl) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
            return 1;
        }
        commandSourceStack.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
        return 0;
    }
}

