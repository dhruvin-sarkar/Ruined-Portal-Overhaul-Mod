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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec2;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPos.containing(((CommandSourceStack)commandContext.getSource()).getPosition()), WorldCoordinates.ZERO_ROTATION))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)commandContext, "pos"), WorldCoordinates.ZERO_ROTATION))).then(Commands.argument("rotation", RotationArgument.rotation()).executes(commandContext -> SetWorldSpawnCommand.setSpawn((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getSpawnablePos((CommandContext<CommandSourceStack>)commandContext, "pos"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"))))));
    }

    private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos, Coordinates coordinates) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Vec2 vec2 = coordinates.getRotation(commandSourceStack);
        float f = vec2.y;
        float g = vec2.x;
        LevelData.RespawnData respawnData = LevelData.RespawnData.of(serverLevel.dimension(), blockPos, f, g);
        serverLevel.setRespawnData(respawnData);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.setworldspawn.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), Float.valueOf(respawnData.yaw()), Float.valueOf(respawnData.pitch()), serverLevel.dimension().identifier().toString()), true);
        return 1;
    }
}

