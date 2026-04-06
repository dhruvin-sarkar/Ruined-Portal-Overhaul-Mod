/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import org.slf4j.Logger;

public class ServerFunctionLibrary
implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Registry<CommandFunction<CommandSourceStack>>> TYPE_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("function"));
    private static final FileToIdConverter LISTER = new FileToIdConverter(Registries.elementsDirPath(TYPE_KEY), ".mcfunction");
    private volatile Map<Identifier, CommandFunction<CommandSourceStack>> functions = ImmutableMap.of();
    private final TagLoader<CommandFunction<CommandSourceStack>> tagsLoader = new TagLoader((identifier, bl) -> this.getFunction(identifier), Registries.tagsDirPath(TYPE_KEY));
    private volatile Map<Identifier, List<CommandFunction<CommandSourceStack>>> tags = Map.of();
    private final PermissionSet functionCompilationPermissions;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public Optional<CommandFunction<CommandSourceStack>> getFunction(Identifier identifier) {
        return Optional.ofNullable(this.functions.get(identifier));
    }

    public Map<Identifier, CommandFunction<CommandSourceStack>> getFunctions() {
        return this.functions;
    }

    public List<CommandFunction<CommandSourceStack>> getTag(Identifier identifier) {
        return this.tags.getOrDefault(identifier, List.of());
    }

    public Iterable<Identifier> getAvailableTags() {
        return this.tags.keySet();
    }

    public ServerFunctionLibrary(PermissionSet permissionSet, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        this.functionCompilationPermissions = permissionSet;
        this.dispatcher = commandDispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        ResourceManager resourceManager = sharedState.resourceManager();
        CompletableFuture<Map> completableFuture = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(resourceManager), executor);
        CompletionStage completableFuture2 = CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            HashMap map2 = Maps.newHashMap();
            CommandSourceStack commandSourceStack = Commands.createCompilationContext(this.functionCompilationPermissions);
            for (Map.Entry entry : map.entrySet()) {
                Identifier identifier = (Identifier)entry.getKey();
                Identifier identifier2 = LISTER.fileToId(identifier);
                map2.put(identifier2, CompletableFuture.supplyAsync(() -> {
                    List<String> list = ServerFunctionLibrary.readLines((Resource)entry.getValue());
                    return CommandFunction.fromLines(identifier2, this.dispatcher, commandSourceStack, list);
                }, executor));
            }
            CompletableFuture[] completableFutures = map2.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> map2);
        });
        return ((CompletableFuture)((CompletableFuture)completableFuture.thenCombine(completableFuture2, Pair::of)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(pair -> {
            Map map = (Map)pair.getSecond();
            ImmutableMap.Builder builder = ImmutableMap.builder();
            map.forEach((identifier, completableFuture) -> ((CompletableFuture)completableFuture.handle((commandFunction, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Failed to load function {}", identifier, throwable);
                } else {
                    builder.put(identifier, commandFunction);
                }
                return null;
            })).join());
            this.functions = builder.build();
            this.tags = this.tagsLoader.build((Map)pair.getFirst());
        }, executor2);
    }

    private static List<String> readLines(Resource resource) {
        List list;
        block8: {
            BufferedReader bufferedReader = resource.openAsReader();
            try {
                list = bufferedReader.lines().toList();
                if (bufferedReader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new CompletionException(iOException);
                }
            }
            bufferedReader.close();
        }
        return list;
    }
}

