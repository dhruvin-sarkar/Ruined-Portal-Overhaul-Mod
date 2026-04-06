/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public class EffectCommands {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.effect.give.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.effect.clear.everything.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.effect.clear.specific.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("effect").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("clear").executes(commandContext -> EffectCommands.clearEffects((CommandSourceStack)commandContext.getSource(), (Collection<? extends Entity>)ImmutableList.of((Object)((CommandSourceStack)commandContext.getSource()).getEntityOrException())))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).executes(commandContext -> EffectCommands.clearEffects((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets")))).then(Commands.argument("effect", ResourceArgument.resource(commandBuildContext, Registries.MOB_EFFECT)).executes(commandContext -> EffectCommands.clearEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"))))))).then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("effect", ResourceArgument.resource(commandBuildContext, Registries.MOB_EFFECT)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), null, 0, true))).then(((RequiredArgumentBuilder)Commands.argument("seconds", IntegerArgumentType.integer((int)1, (int)1000000)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seconds"), 0, true))).then(((RequiredArgumentBuilder)Commands.argument("amplifier", IntegerArgumentType.integer((int)0, (int)255)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seconds"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amplifier"), true))).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"seconds"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amplifier"), !BoolArgumentType.getBool((CommandContext)commandContext, (String)"hideParticles"))))))).then(((LiteralArgumentBuilder)Commands.literal("infinite").executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), -1, 0, true))).then(((RequiredArgumentBuilder)Commands.argument("amplifier", IntegerArgumentType.integer((int)0, (int)255)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), -1, IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amplifier"), true))).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets"), ResourceArgument.getMobEffect((CommandContext<CommandSourceStack>)commandContext, "effect"), -1, IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amplifier"), !BoolArgumentType.getBool((CommandContext)commandContext, (String)"hideParticles"))))))))));
    }

    private static int giveEffect(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Holder<MobEffect> holder, @Nullable Integer integer, int i, boolean bl) throws CommandSyntaxException {
        MobEffect mobEffect = holder.value();
        int j = 0;
        int k = integer != null ? (mobEffect.isInstantenous() ? integer : (integer == -1 ? -1 : integer * 20)) : (mobEffect.isInstantenous() ? 1 : 600);
        for (Entity entity : collection) {
            MobEffectInstance mobEffectInstance;
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).addEffect(mobEffectInstance = new MobEffectInstance(holder, k, i, false, bl), commandSourceStack.getEntity())) continue;
            ++j;
        }
        if (j == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.give.success.single", mobEffect.getDisplayName(), ((Entity)collection.iterator().next()).getDisplayName(), k / 20), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.give.success.multiple", mobEffect.getDisplayName(), collection.size(), k / 20), true);
        }
        return j;
    }

    private static int clearEffects(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) throws CommandSyntaxException {
        int i = 0;
        for (Entity entity : collection) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).removeAllEffects()) continue;
            ++i;
        }
        if (i == 0) {
            throw ERROR_CLEAR_EVERYTHING_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.single", ((Entity)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.multiple", collection.size()), true);
        }
        return i;
    }

    private static int clearEffect(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Holder<MobEffect> holder) throws CommandSyntaxException {
        MobEffect mobEffect = holder.value();
        int i = 0;
        for (Entity entity : collection) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).removeEffect(holder)) continue;
            ++i;
        }
        if (i == 0) {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.clear.specific.success.single", mobEffect.getDisplayName(), ((Entity)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.effect.clear.specific.success.multiple", mobEffect.getDisplayName(), collection.size()), true);
        }
        return i;
    }
}

