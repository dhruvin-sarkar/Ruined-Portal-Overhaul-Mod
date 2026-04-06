/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("defaultgamemode").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes(commandContext -> DefaultGameModeCommands.setMode((CommandSourceStack)commandContext.getSource(), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode")))));
    }

    private static int setMode(CommandSourceStack commandSourceStack, GameType gameType) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        minecraftServer.setDefaultGameType(gameType);
        int i = minecraftServer.enforceGameTypeForPlayers(minecraftServer.getForcedGameType());
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.defaultgamemode.success", gameType.getLongDisplayName()), true);
        return i;
    }
}

