/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(object -> Component.translatableEscape("clear.failed.single", object));
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(object -> Component.translatableEscape("clear.failed.multiple", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(commandContext -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)commandContext.getSource(), Collections.singleton(((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), itemStack -> true))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), itemStack -> true))).then(((RequiredArgumentBuilder)Commands.argument("item", ItemPredicateArgument.itemPredicate(commandBuildContext)).executes(commandContext -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item")))).then(Commands.argument("maxCount", IntegerArgumentType.integer((int)0)).executes(commandContext -> ClearInventoryCommands.clearInventory((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)commandContext, "item"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"maxCount")))))));
    }

    private static int clearUnlimited(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        return ClearInventoryCommands.clearInventory(commandSourceStack, collection, predicate, -1);
    }

    private static int clearInventory(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate, int i) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayer serverPlayer : collection) {
            j += serverPlayer.getInventory().clearOrCountMatchingItems(predicate, i, serverPlayer.inventoryMenu.getCraftSlots());
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
        }
        if (j == 0) {
            if (collection.size() == 1) {
                throw ERROR_SINGLE.create((Object)collection.iterator().next().getName());
            }
            throw ERROR_MULTIPLE.create((Object)collection.size());
        }
        int k = j;
        if (i == 0) {
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.test.single", k, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.test.multiple", k, collection.size()), true);
            }
        } else if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.success.single", k, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.success.multiple", k, collection.size()), true);
        }
        return j;
    }
}

