/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public static CompletableFuture<LoadResult> reload(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<Registry.PendingTags<?>> list2, ResourceManager resourceManager, Executor executor) {
        List<HolderLookup.RegistryLookup<?>> list22 = TagLoader.buildUpdatedLookups(layeredRegistryAccess.getAccessForLoading(RegistryLayer.RELOADABLE), list2);
        HolderLookup.Provider provider = HolderLookup.Provider.create(list22.stream());
        RegistryOps registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
        List list3 = LootDataType.values().map(lootDataType -> ReloadableServerRegistries.scheduleRegistryLoad(lootDataType, registryOps, resourceManager, executor)).toList();
        CompletableFuture completableFuture = Util.sequence(list3);
        return completableFuture.thenApplyAsync(list -> ReloadableServerRegistries.createAndValidateFullContext(layeredRegistryAccess, provider, list), executor);
    }

    private static <T> CompletableFuture<WritableRegistry<?>> scheduleRegistryLoad(LootDataType<T> lootDataType, RegistryOps<JsonElement> registryOps, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            MappedRegistry writableRegistry = new MappedRegistry(lootDataType.registryKey(), Lifecycle.experimental());
            HashMap<Identifier, Object> map = new HashMap<Identifier, Object>();
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager, lootDataType.registryKey(), (DynamicOps<JsonElement>)registryOps, lootDataType.codec(), map);
            map.forEach((identifier, object) -> writableRegistry.register(ResourceKey.create(lootDataType.registryKey(), identifier), object, DEFAULT_REGISTRATION_INFO));
            TagLoader.loadTagsForRegistry(resourceManager, writableRegistry);
            return writableRegistry;
        }, executor);
    }

    private static LoadResult createAndValidateFullContext(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, HolderLookup.Provider provider, List<WritableRegistry<?>> list) {
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = ReloadableServerRegistries.createUpdatedRegistries(layeredRegistryAccess, list);
        HolderLookup.Provider provider2 = ReloadableServerRegistries.concatenateLookups(provider, layeredRegistryAccess2.getLayer(RegistryLayer.RELOADABLE));
        ReloadableServerRegistries.validateLootRegistries(provider2);
        return new LoadResult(layeredRegistryAccess2, provider2);
    }

    private static HolderLookup.Provider concatenateLookups(HolderLookup.Provider provider, HolderLookup.Provider provider2) {
        return HolderLookup.Provider.create(Stream.concat(provider.listRegistries(), provider2.listRegistries()));
    }

    private static void validateLootRegistries(HolderLookup.Provider provider) {
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, provider);
        LootDataType.values().forEach(lootDataType -> ReloadableServerRegistries.validateRegistry(validationContext, lootDataType, provider));
        collector.forEach((string, problem) -> LOGGER.warn("Found loot table element validation problem in {}: {}", string, (Object)problem.description()));
    }

    private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list) {
        return layeredRegistryAccess.replaceFrom(RegistryLayer.RELOADABLE, new RegistryAccess.ImmutableRegistryAccess(list).freeze());
    }

    private static <T> void validateRegistry(ValidationContext validationContext, LootDataType<T> lootDataType, HolderLookup.Provider provider) {
        HolderGetter holderLookup = provider.lookupOrThrow(lootDataType.registryKey());
        holderLookup.listElements().forEach(reference -> lootDataType.runValidation(validationContext, reference.key(), reference.value()));
    }

    public record LoadResult(LayeredRegistryAccess<RegistryLayer> layers, HolderLookup.Provider lookupWithUpdatedTags) {
    }

    public static class Holder {
        private final HolderLookup.Provider registries;

        public Holder(HolderLookup.Provider provider) {
            this.registries = provider;
        }

        public HolderLookup.Provider lookup() {
            return this.registries;
        }

        public LootTable getLootTable(ResourceKey<LootTable> resourceKey) {
            return this.registries.lookup(Registries.LOOT_TABLE).flatMap(registryLookup -> registryLookup.get(resourceKey)).map(net.minecraft.core.Holder::value).orElse(LootTable.EMPTY);
        }
    }
}

