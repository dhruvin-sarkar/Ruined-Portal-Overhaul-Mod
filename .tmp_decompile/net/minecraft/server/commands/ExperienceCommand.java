/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType((Message)Component.translatable("commands.experience.set.points.invalid"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("experience").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(Commands.argument("target", EntityArgument.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer()).executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.POINTS))).then(Commands.literal("points").executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.LEVELS))))))).then(Commands.literal("set").then(Commands.argument("target", EntityArgument.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer((int)0)).executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.POINTS))).then(Commands.literal("points").executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount"), Type.LEVELS))))))).then(Commands.literal("query").then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.player()).then(Commands.literal("points").executes(commandContext -> ExperienceCommand.queryExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)commandContext, "target"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.queryExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)commandContext, "target"), Type.LEVELS))))));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("xp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).redirect((CommandNode)literalCommandNode));
    }

    private static int queryExperience(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Type type) {
        int i = type.query.applyAsInt(serverPlayer);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.experience.query." + type.name, serverPlayer.getDisplayName(), i), false);
        return i;
    }

    private static int addExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, Type type) {
        for (ServerPlayer serverPlayer : collection) {
            type.add.accept(serverPlayer, i);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.experience.add." + type.name + ".success.single", i, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.experience.add." + type.name + ".success.multiple", i, collection.size()), true);
        }
        return collection.size();
    }

    private static int setExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, Type type) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (!type.set.test(serverPlayer, i)) continue;
            ++j;
        }
        if (j == 0) {
            throw ERROR_SET_POINTS_INVALID.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.experience.set." + type.name + ".success.single", i, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.experience.set." + type.name + ".success.multiple", i, collection.size()), true);
        }
        return collection.size();
    }

    static enum Type {
        POINTS("points", Player::giveExperiencePoints, (serverPlayer, integer) -> {
            if (integer >= serverPlayer.getXpNeededForNextLevel()) {
                return false;
            }
            serverPlayer.setExperiencePoints((int)integer);
            return true;
        }, serverPlayer -> Mth.floor(serverPlayer.experienceProgress * (float)serverPlayer.getXpNeededForNextLevel())),
        LEVELS("levels", ServerPlayer::giveExperienceLevels, (serverPlayer, integer) -> {
            serverPlayer.setExperienceLevels((int)integer);
            return true;
        }, serverPlayer -> serverPlayer.experienceLevel);

        public final BiConsumer<ServerPlayer, Integer> add;
        public final BiPredicate<ServerPlayer, Integer> set;
        public final String name;
        final ToIntFunction<ServerPlayer> query;

        private Type(String string2, BiConsumer<ServerPlayer, Integer> biConsumer, BiPredicate<ServerPlayer, Integer> biPredicate, ToIntFunction<ServerPlayer> toIntFunction) {
            this.add = biConsumer;
            this.name = string2;
            this.set = biPredicate;
            this.query = toIntFunction;
        }
    }
}

