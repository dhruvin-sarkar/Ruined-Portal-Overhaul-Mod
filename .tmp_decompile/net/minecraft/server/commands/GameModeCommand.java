/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

public class GameModeCommand {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gamemode").requires(Commands.hasPermission(PERMISSION_CHECK))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes(commandContext -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)commandContext, Collections.singleton(((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode")))).then(Commands.argument("target", EntityArgument.players()).executes(commandContext -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)commandContext, EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode"))))));
    }

    private static void logGamemodeChange(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, GameType gameType) {
        MutableComponent component = Component.translatable("gameMode." + gameType.getName());
        if (commandSourceStack.getEntity() == serverPlayer) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.gamemode.success.self", component), true);
        } else {
            if (commandSourceStack.getLevel().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).booleanValue()) {
                serverPlayer.sendSystemMessage(Component.translatable("gameMode.changed", component));
            }
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.gamemode.success.other", serverPlayer.getDisplayName(), component), true);
        }
    }

    private static int setMode(CommandContext<CommandSourceStack> commandContext, Collection<ServerPlayer> collection, GameType gameType) {
        int i = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (!GameModeCommand.setGameMode((CommandSourceStack)commandContext.getSource(), serverPlayer, gameType)) continue;
            ++i;
        }
        return i;
    }

    public static void setGameMode(ServerPlayer serverPlayer, GameType gameType) {
        GameModeCommand.setGameMode(serverPlayer.createCommandSourceStack(), serverPlayer, gameType);
    }

    private static boolean setGameMode(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, GameType gameType) {
        if (serverPlayer.setGameMode(gameType)) {
            GameModeCommand.logGamemodeChange(commandSourceStack, serverPlayer, gameType);
            return true;
        }
        return false;
    }
}

