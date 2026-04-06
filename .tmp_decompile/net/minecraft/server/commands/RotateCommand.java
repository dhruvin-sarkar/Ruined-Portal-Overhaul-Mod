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
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LookAt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;

public class RotateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("rotate").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).then(Commands.argument("rotation", RotationArgument.rotation()).executes(commandContext -> RotateCommand.rotate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), RotationArgument.getRotation((CommandContext<CommandSourceStack>)commandContext, "rotation"))))).then(((LiteralArgumentBuilder)Commands.literal("facing").then(Commands.literal("entity").then(((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity()).executes(commandContext -> RotateCommand.rotate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "facingEntity"), EntityAnchorArgument.Anchor.FEET)))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes(commandContext -> RotateCommand.rotate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), new LookAt.LookAtEntity(EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "facingEntity"), EntityAnchorArgument.getAnchor((CommandContext<CommandSourceStack>)commandContext, "facingAnchor")))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes(commandContext -> RotateCommand.rotate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), new LookAt.LookAtPosition(Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "facingLocation"))))))));
    }

    private static int rotate(CommandSourceStack commandSourceStack, Entity entity, Coordinates coordinates) {
        Vec2 vec2 = coordinates.getRotation(commandSourceStack);
        float f = coordinates.isYRelative() ? vec2.y - entity.getYRot() : vec2.y;
        float g = coordinates.isXRelative() ? vec2.x - entity.getXRot() : vec2.x;
        entity.forceSetRotation(f, coordinates.isYRelative(), g, coordinates.isXRelative());
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.rotate.success", entity.getDisplayName()), true);
        return 1;
    }

    private static int rotate(CommandSourceStack commandSourceStack, Entity entity, LookAt lookAt) {
        lookAt.perform(commandSourceStack, entity);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.rotate.success", entity.getDisplayName()), true);
        return 1;
    }
}

