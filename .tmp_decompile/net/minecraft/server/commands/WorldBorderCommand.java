/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.center.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.nochange"));
    private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.small"));
    private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.big", 5.9999968E7));
    private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7));
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.warning.time.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.warning.distance.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.damage.buffer.failed"));
    private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType((Message)Component.translatable("commands.worldborder.damage.amount.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("worldborder").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes(commandContext -> WorldBorderCommand.setSize((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"distance"), 0L))).then(Commands.argument("time", TimeArgument.time(0)).executes(commandContext -> WorldBorderCommand.setSize((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"distance"), ((CommandSourceStack)commandContext.getSource()).getLevel().getWorldBorder().getLerpTime() + (long)IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time"))))))).then(Commands.literal("set").then(((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes(commandContext -> WorldBorderCommand.setSize((CommandSourceStack)commandContext.getSource(), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"distance"), 0L))).then(Commands.argument("time", TimeArgument.time(0)).executes(commandContext -> WorldBorderCommand.setSize((CommandSourceStack)commandContext.getSource(), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"distance"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time"))))))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes(commandContext -> WorldBorderCommand.setCenter((CommandSourceStack)commandContext.getSource(), Vec2Argument.getVec2((CommandContext<CommandSourceStack>)commandContext, "pos")))))).then(((LiteralArgumentBuilder)Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg((float)0.0f)).executes(commandContext -> WorldBorderCommand.setDamageAmount((CommandSourceStack)commandContext.getSource(), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"damagePerBlock")))))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg((float)0.0f)).executes(commandContext -> WorldBorderCommand.setDamageBuffer((CommandSourceStack)commandContext.getSource(), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"distance"))))))).then(Commands.literal("get").executes(commandContext -> WorldBorderCommand.getSize((CommandSourceStack)commandContext.getSource())))).then(((LiteralArgumentBuilder)Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer((int)0)).executes(commandContext -> WorldBorderCommand.setWarningDistance((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"distance")))))).then(Commands.literal("time").then(Commands.argument("time", TimeArgument.time(0)).executes(commandContext -> WorldBorderCommand.setWarningTime((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time")))))));
    }

    private static int setDamageBuffer(CommandSourceStack commandSourceStack, float f) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
        if (worldBorder.getSafeZone() == (double)f) {
            throw ERROR_SAME_DAMAGE_BUFFER.create();
        }
        worldBorder.setSafeZone(f);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(f))), true);
        return (int)f;
    }

    private static int setDamageAmount(CommandSourceStack commandSourceStack, float f) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
        if (worldBorder.getDamagePerBlock() == (double)f) {
            throw ERROR_SAME_DAMAGE_AMOUNT.create();
        }
        worldBorder.setDamagePerBlock(f);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(f))), true);
        return (int)f;
    }

    private static int setWarningTime(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
        if (worldBorder.getWarningTime() == i) {
            throw ERROR_SAME_WARNING_TIME.create();
        }
        worldBorder.setWarningTime(i);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.warning.time.success", WorldBorderCommand.formatTicksToSeconds(i)), true);
        return i;
    }

    private static int setWarningDistance(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
        if (worldBorder.getWarningBlocks() == i) {
            throw ERROR_SAME_WARNING_DISTANCE.create();
        }
        worldBorder.setWarningBlocks(i);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.warning.distance.success", i), true);
        return i;
    }

    private static int getSize(CommandSourceStack commandSourceStack) {
        double d = commandSourceStack.getLevel().getWorldBorder().getSize();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d)), false);
        return Mth.floor(d + 0.5);
    }

    private static int setCenter(CommandSourceStack commandSourceStack, Vec2 vec2) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getLevel().getWorldBorder();
        if (worldBorder.getCenterX() == (double)vec2.x && worldBorder.getCenterZ() == (double)vec2.y) {
            throw ERROR_SAME_CENTER.create();
        }
        if ((double)Math.abs(vec2.x) > 2.9999984E7 || (double)Math.abs(vec2.y) > 2.9999984E7) {
            throw ERROR_TOO_FAR_OUT.create();
        }
        worldBorder.setCenter(vec2.x, vec2.y);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(vec2.x)), String.format(Locale.ROOT, "%.2f", Float.valueOf(vec2.y))), true);
        return 0;
    }

    private static int setSize(CommandSourceStack commandSourceStack, double d, long l) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        double e = worldBorder.getSize();
        if (e == d) {
            throw ERROR_SAME_SIZE.create();
        }
        if (d < 1.0) {
            throw ERROR_TOO_SMALL.create();
        }
        if (d > 5.9999968E7) {
            throw ERROR_TOO_BIG.create();
        }
        String string = String.format(Locale.ROOT, "%.1f", d);
        if (l > 0L) {
            worldBorder.lerpSizeBetween(e, d, l, serverLevel.getGameTime());
            if (d > e) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.set.grow", string, WorldBorderCommand.formatTicksToSeconds(l)), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.set.shrink", string, WorldBorderCommand.formatTicksToSeconds(l)), true);
            }
        } else {
            worldBorder.setSize(d);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", string), true);
        }
        return (int)(d - e);
    }

    private static String formatTicksToSeconds(long l) {
        return String.format(Locale.ROOT, "%.2f", (double)l / 20.0);
    }
}

