/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;

public class FetchProfileCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fetchprofile").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("name").then(Commands.argument("name", StringArgumentType.greedyString()).executes(commandContext -> FetchProfileCommand.resolveName((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"name")))))).then(Commands.literal("id").then(Commands.argument("id", UuidArgument.uuid()).executes(commandContext -> FetchProfileCommand.resolveId((CommandSourceStack)commandContext.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "id"))))));
    }

    private static void reportResolvedProfile(CommandSourceStack commandSourceStack, GameProfile gameProfile, String string, Component component) {
        ResolvableProfile resolvableProfile = ResolvableProfile.createResolved(gameProfile);
        ResolvableProfile.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)resolvableProfile).ifSuccess(tag2 -> {
            String string2 = tag2.toString();
            MutableComponent mutableComponent = Component.object(new PlayerSprite(resolvableProfile, true));
            ComponentSerialization.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)mutableComponent).ifSuccess(tag -> {
                String string3 = tag.toString();
                commandSourceStack.sendSuccess(() -> {
                    MutableComponent component2 = ComponentUtils.formatList(List.of((Object)Component.translatable("commands.fetchprofile.copy_component").withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string2))), (Object)Component.translatable("commands.fetchprofile.give_item").withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("give @s minecraft:player_head[profile=" + string2 + "]"))), (Object)Component.translatable("commands.fetchprofile.summon_mannequin").withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("summon minecraft:mannequin ~ ~ ~ {profile:" + string2 + "}"))), (Object)Component.translatable("commands.fetchprofile.copy_text", mutableComponent.withStyle(ChatFormatting.WHITE)).withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string3)))), CommonComponents.SPACE, mutableComponent -> ComponentUtils.wrapInSquareBrackets(mutableComponent.withStyle(ChatFormatting.GREEN)));
                    return Component.translatable(string, component, component2);
                }, false);
            }).ifError(error -> commandSourceStack.sendFailure(Component.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
        }).ifError(error -> commandSourceStack.sendFailure(Component.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
    }

    private static int resolveName(CommandSourceStack commandSourceStack, String string) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        ProfileResolver profileResolver = minecraftServer.services().profileResolver();
        Util.nonCriticalIoPool().execute(() -> {
            MutableComponent component = Component.literal(string);
            Optional<GameProfile> optional = profileResolver.fetchByName(string);
            minecraftServer.execute(() -> optional.ifPresentOrElse(gameProfile -> FetchProfileCommand.reportResolvedProfile(commandSourceStack, gameProfile, "commands.fetchprofile.name.success", component), () -> commandSourceStack.sendFailure(Component.translatable("commands.fetchprofile.name.failure", component))));
        });
        return 1;
    }

    private static int resolveId(CommandSourceStack commandSourceStack, UUID uUID) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        ProfileResolver profileResolver = minecraftServer.services().profileResolver();
        Util.nonCriticalIoPool().execute(() -> {
            Component component = Component.translationArg(uUID);
            Optional<GameProfile> optional = profileResolver.fetchById(uUID);
            minecraftServer.execute(() -> optional.ifPresentOrElse(gameProfile -> FetchProfileCommand.reportResolvedProfile(commandSourceStack, gameProfile, "commands.fetchprofile.id.success", component), () -> commandSourceStack.sendFailure(Component.translatable("commands.fetchprofile.id.failure", component))));
        });
        return 1;
    }
}

