/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public class StopSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RequiredArgumentBuilder requiredArgumentBuilder = (RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), null, null))).then(Commands.literal("*").then(Commands.argument("sound", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), null, IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound")))));
        for (SoundSource soundSource : SoundSource.values()) {
            requiredArgumentBuilder.then(((LiteralArgumentBuilder)Commands.literal(soundSource.getName()).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), soundSource, null))).then(Commands.argument("sound", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), soundSource, IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "sound")))));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stopsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then((ArgumentBuilder)requiredArgumentBuilder));
    }

    private static int stopSound(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, @Nullable SoundSource soundSource, @Nullable Identifier identifier) {
        ClientboundStopSoundPacket clientboundStopSoundPacket = new ClientboundStopSoundPacket(identifier, soundSource);
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(clientboundStopSoundPacket);
        }
        if (soundSource != null) {
            if (identifier != null) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.sound", Component.translationArg(identifier), soundSource.getName()), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.any", soundSource.getName()), true);
            }
        } else if (identifier != null) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.sound", Component.translationArg(identifier)), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.any"), true);
        }
        return collection.size();
    }
}

