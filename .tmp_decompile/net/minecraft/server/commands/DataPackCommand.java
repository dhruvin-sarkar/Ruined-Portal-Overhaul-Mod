/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.FileUtil;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class DataPackCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.unknown", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.enable.failed", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.disable.failed", object));
    private static final DynamicCommandExceptionType ERROR_CANNOT_DISABLE_FEATURE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.disable.failed.feature", object));
    private static final Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.datapack.enable.failed.no_flags", object, object2));
    private static final DynamicCommandExceptionType ERROR_PACK_INVALID_NAME = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.create.invalid_name", object));
    private static final DynamicCommandExceptionType ERROR_PACK_INVALID_FULL_NAME = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.create.invalid_full_name", object));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_EXISTS = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.create.already_exists", object));
    private static final Dynamic2CommandExceptionType ERROR_PACK_METADATA_ENCODE_FAILURE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.datapack.create.metadata_encode_failure", object, object2));
    private static final DynamicCommandExceptionType ERROR_PACK_IO_FAILURE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.datapack.create.io_failure", object));
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (commandContext, suggestionsBuilder) -> {
        PackRepository packRepository = ((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository();
        Collection<String> collection = packRepository.getSelectedIds();
        FeatureFlagSet featureFlagSet = ((CommandSourceStack)commandContext.getSource()).enabledFeatures();
        return SharedSuggestionProvider.suggest(packRepository.getAvailablePacks().stream().filter(pack -> pack.getRequestedFeatures().isSubsetOf(featureFlagSet)).map(Pack::getId).filter(string -> !collection.contains(string)).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("datapack").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("enable").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string()).suggests(UNSELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", true), (list, pack) -> pack.getDefaultPosition().insert(list, pack, Pack::selectionConfig, false)))).then(Commands.literal("after").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", true), (list, pack) -> list.add(list.indexOf(DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "existing", false)) + 1, pack)))))).then(Commands.literal("before").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", true), (list, pack) -> list.add(list.indexOf(DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "existing", false)), pack)))))).then(Commands.literal("last").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", true), List::add)))).then(Commands.literal("first").executes(commandContext -> DataPackCommand.enablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", true), (list, pack) -> list.add(0, pack))))))).then(Commands.literal("disable").then(Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes(commandContext -> DataPackCommand.disablePack((CommandSourceStack)commandContext.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)commandContext, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> DataPackCommand.listPacks((CommandSourceStack)commandContext.getSource()))).then(Commands.literal("available").executes(commandContext -> DataPackCommand.listAvailablePacks((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("enabled").executes(commandContext -> DataPackCommand.listEnabledPacks((CommandSourceStack)commandContext.getSource()))))).then(((LiteralArgumentBuilder)Commands.literal("create").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))).then(Commands.argument("id", StringArgumentType.string()).then(Commands.argument("description", ComponentArgument.textComponent(commandBuildContext)).executes(commandContext -> DataPackCommand.createPack((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"id"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)commandContext, "description")))))));
    }

    private static int createPack(CommandSourceStack commandSourceStack, String string, Component component) throws CommandSyntaxException {
        Path path = commandSourceStack.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
        if (!FileUtil.isValidPathSegment(string)) {
            throw ERROR_PACK_INVALID_NAME.create((Object)string);
        }
        if (!FileUtil.isPathPartPortable(string)) {
            throw ERROR_PACK_INVALID_FULL_NAME.create((Object)string);
        }
        Path path2 = path.resolve(string);
        if (Files.exists(path2, new LinkOption[0])) {
            throw ERROR_PACK_ALREADY_EXISTS.create((Object)string);
        }
        PackMetadataSection packMetadataSection = new PackMetadataSection(component, SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange());
        DataResult dataResult = PackMetadataSection.SERVER_TYPE.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)packMetadataSection);
        Optional optional = dataResult.error();
        if (optional.isPresent()) {
            throw ERROR_PACK_METADATA_ENCODE_FAILURE.create((Object)string, (Object)((DataResult.Error)optional.get()).message());
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PackMetadataSection.SERVER_TYPE.name(), (JsonElement)dataResult.getOrThrow());
        try {
            Files.createDirectory(path2, new FileAttribute[0]);
            Files.createDirectory(path2.resolve(PackType.SERVER_DATA.getDirectory()), new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2.resolve("pack.mcmeta"), StandardCharsets.UTF_8, new OpenOption[0]);
                 JsonWriter jsonWriter = new JsonWriter((Writer)bufferedWriter);){
                jsonWriter.setSerializeNulls(false);
                jsonWriter.setIndent("  ");
                GsonHelper.writeValue(jsonWriter, (JsonElement)jsonObject, null);
            }
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to create pack at {}", (Object)path.toAbsolutePath(), (Object)iOException);
            throw ERROR_PACK_IO_FAILURE.create((Object)string);
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.create.success", string), true);
        return 1;
    }

    private static int enablePack(CommandSourceStack commandSourceStack, Pack pack, Inserter inserter) throws CommandSyntaxException {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList list = Lists.newArrayList(packRepository.getSelectedPacks());
        inserter.apply(list, pack);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.modify.enable", pack.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
        return list.size();
    }

    private static int disablePack(CommandSourceStack commandSourceStack, Pack pack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        ArrayList list = Lists.newArrayList(packRepository.getSelectedPacks());
        list.remove(pack);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.modify.disable", pack.getChatLink(true)), true);
        ReloadCommand.reloadPacks(list.stream().map(Pack::getId).collect(Collectors.toList()), commandSourceStack);
        return list.size();
    }

    private static int listPacks(CommandSourceStack commandSourceStack) {
        return DataPackCommand.listEnabledPacks(commandSourceStack) + DataPackCommand.listAvailablePacks(commandSourceStack);
    }

    private static int listAvailablePacks(CommandSourceStack commandSourceStack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<Pack> collection = packRepository.getSelectedPacks();
        Collection<Pack> collection2 = packRepository.getAvailablePacks();
        FeatureFlagSet featureFlagSet = commandSourceStack.enabledFeatures();
        List list = collection2.stream().filter(pack -> !collection.contains(pack) && pack.getRequestedFeatures().isSubsetOf(featureFlagSet)).toList();
        if (list.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.list.available.none"), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.list.available.success", list.size(), ComponentUtils.formatList(list, pack -> pack.getChatLink(false))), false);
        }
        return list.size();
    }

    private static int listEnabledPacks(CommandSourceStack commandSourceStack) {
        PackRepository packRepository = commandSourceStack.getServer().getPackRepository();
        packRepository.reload();
        Collection<Pack> collection = packRepository.getSelectedPacks();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.none"), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.success", collection.size(), ComponentUtils.formatList(collection, pack -> pack.getChatLink(true))), false);
        }
        return collection.size();
    }

    private static Pack getPack(CommandContext<CommandSourceStack> commandContext, String string, boolean bl) throws CommandSyntaxException {
        String string2 = StringArgumentType.getString(commandContext, (String)string);
        PackRepository packRepository = ((CommandSourceStack)commandContext.getSource()).getServer().getPackRepository();
        Pack pack = packRepository.getPack(string2);
        if (pack == null) {
            throw ERROR_UNKNOWN_PACK.create((Object)string2);
        }
        boolean bl2 = packRepository.getSelectedPacks().contains(pack);
        if (bl && bl2) {
            throw ERROR_PACK_ALREADY_ENABLED.create((Object)string2);
        }
        if (!bl && !bl2) {
            throw ERROR_PACK_ALREADY_DISABLED.create((Object)string2);
        }
        FeatureFlagSet featureFlagSet = ((CommandSourceStack)commandContext.getSource()).enabledFeatures();
        FeatureFlagSet featureFlagSet2 = pack.getRequestedFeatures();
        if (!bl && !featureFlagSet2.isEmpty() && pack.getPackSource() == PackSource.FEATURE) {
            throw ERROR_CANNOT_DISABLE_FEATURE.create((Object)string2);
        }
        if (!featureFlagSet2.isSubsetOf(featureFlagSet)) {
            throw ERROR_PACK_FEATURES_NOT_ENABLED.create((Object)string2, (Object)FeatureFlags.printMissingFlags(featureFlagSet, featureFlagSet2));
        }
        return pack;
    }

    static interface Inserter {
        public void apply(List<Pack> var1, Pack var2) throws CommandSyntaxException;
    }
}

