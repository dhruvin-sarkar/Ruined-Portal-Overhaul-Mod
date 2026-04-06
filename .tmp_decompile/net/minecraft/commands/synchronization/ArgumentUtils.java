/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  java.lang.runtime.SwitchBootstraps
 *  org.slf4j.Logger
 */
package net.minecraft.commands.synchronization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionProviderCheck;
import org.slf4j.Logger;

public class ArgumentUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte NUMBER_FLAG_MIN = 1;
    private static final byte NUMBER_FLAG_MAX = 2;

    public static int createNumberFlags(boolean bl, boolean bl2) {
        int i = 0;
        if (bl) {
            i |= 1;
        }
        if (bl2) {
            i |= 2;
        }
        return i;
    }

    public static boolean numberHasMin(byte b) {
        return (b & 1) != 0;
    }

    public static boolean numberHasMax(byte b) {
        return (b & 2) != 0;
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeArgumentCap(JsonObject jsonObject, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
        argumentTypeInfo.serializeToJson(template, jsonObject);
    }

    private static <T extends ArgumentType<?>> void serializeArgumentToJson(JsonObject jsonObject, T argumentType) {
        ArgumentTypeInfo.Template<T> template = ArgumentTypeInfos.unpack(argumentType);
        jsonObject.addProperty("type", "argument");
        jsonObject.addProperty("parser", String.valueOf(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(template.type())));
        JsonObject jsonObject2 = new JsonObject();
        ArgumentUtils.serializeArgumentCap(jsonObject2, template.type(), template);
        if (!jsonObject2.isEmpty()) {
            jsonObject.add("properties", (JsonElement)jsonObject2);
        }
    }

    public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> commandDispatcher, CommandNode<S> commandNode) {
        Collection collection2;
        Object rootCommandNode;
        JsonObject jsonObject = new JsonObject();
        CommandNode<S> commandNode2 = commandNode;
        Objects.requireNonNull(commandNode2);
        CommandNode<S> commandNode3 = commandNode2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, LiteralCommandNode.class, ArgumentCommandNode.class}, commandNode3, (int)n)) {
            case 0: {
                rootCommandNode = (RootCommandNode)commandNode3;
                jsonObject.addProperty("type", "root");
                break;
            }
            case 1: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode3;
                jsonObject.addProperty("type", "literal");
                break;
            }
            case 2: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode3;
                ArgumentUtils.serializeArgumentToJson(jsonObject, argumentCommandNode.getType());
                break;
            }
            default: {
                LOGGER.error("Could not serialize node {} ({})!", commandNode, commandNode.getClass());
                jsonObject.addProperty("type", "unknown");
            }
        }
        Collection collection = commandNode.getChildren();
        if (!collection.isEmpty()) {
            JsonObject jsonObject2 = new JsonObject();
            rootCommandNode = collection.iterator();
            while (rootCommandNode.hasNext()) {
                CommandNode commandNode22 = (CommandNode)rootCommandNode.next();
                jsonObject2.add(commandNode22.getName(), (JsonElement)ArgumentUtils.serializeNodeToJson(commandDispatcher, commandNode22));
            }
            jsonObject.add("children", (JsonElement)jsonObject2);
        }
        if (commandNode.getCommand() != null) {
            jsonObject.addProperty("executable", Boolean.valueOf(true));
        }
        if ((rootCommandNode = commandNode.getRequirement()) instanceof PermissionProviderCheck) {
            PermissionProviderCheck permissionProviderCheck = (PermissionProviderCheck)rootCommandNode;
            JsonElement jsonElement = (JsonElement)PermissionCheck.CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)permissionProviderCheck.test()).getOrThrow(string -> new IllegalStateException("Failed to serialize requirement: " + string));
            jsonObject.add("permissions", jsonElement);
        }
        if (commandNode.getRedirect() != null && !(collection2 = commandDispatcher.getPath(commandNode.getRedirect())).isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (String string2 : collection2) {
                jsonArray.add(string2);
            }
            jsonObject.add("redirect", (JsonElement)jsonArray);
        }
        return jsonObject;
    }

    public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> commandNode) {
        ReferenceOpenHashSet set = new ReferenceOpenHashSet();
        HashSet set2 = new HashSet();
        ArgumentUtils.findUsedArgumentTypes(commandNode, set2, set);
        return set2;
    }

    private static <T> void findUsedArgumentTypes(CommandNode<T> commandNode2, Set<ArgumentType<?>> set, Set<CommandNode<T>> set2) {
        if (!set2.add(commandNode2)) {
            return;
        }
        if (commandNode2 instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode2;
            set.add(argumentCommandNode.getType());
        }
        commandNode2.getChildren().forEach(commandNode -> ArgumentUtils.findUsedArgumentTypes(commandNode, set, set2));
        CommandNode commandNode22 = commandNode2.getRedirect();
        if (commandNode22 != null) {
            ArgumentUtils.findUsedArgumentTypes(commandNode22, set, set2);
        }
    }
}

