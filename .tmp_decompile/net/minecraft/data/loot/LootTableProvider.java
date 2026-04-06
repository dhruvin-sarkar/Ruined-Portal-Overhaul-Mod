/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.data.loot;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput.PathProvider pathProvider;
    private final Set<ResourceKey<LootTable>> requiredTables;
    private final List<SubProviderEntry> subProviders;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public LootTableProvider(PackOutput packOutput, Set<ResourceKey<LootTable>> set, List<SubProviderEntry> list, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.pathProvider = packOutput.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
        this.subProviders = list;
        this.requiredTables = set;
        this.registries = completableFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> this.run(cachedOutput, (HolderLookup.Provider)provider));
    }

    private CompletableFuture<?> run(CachedOutput cachedOutput, HolderLookup.Provider provider) {
        MappedRegistry<LootTable> writableRegistry = new MappedRegistry<LootTable>(Registries.LOOT_TABLE, Lifecycle.experimental());
        Object2ObjectOpenHashMap map = new Object2ObjectOpenHashMap();
        this.subProviders.forEach(arg_0 -> LootTableProvider.method_10410(provider, (Map)map, writableRegistry, arg_0));
        writableRegistry.freeze();
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        RegistryAccess.Frozen provider2 = new RegistryAccess.ImmutableRegistryAccess(List.of(writableRegistry)).freeze();
        ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, provider2);
        Sets.SetView set = Sets.difference(this.requiredTables, writableRegistry.registryKeySet());
        for (ResourceKey resourceKey : set) {
            collector.report(new MissingTableProblem(resourceKey));
        }
        writableRegistry.listElements().forEach(reference -> ((LootTable)reference.value()).validate(validationContext.setContextKeySet(((LootTable)reference.value()).getParamSet()).enterElement(new ProblemReporter.RootElementPathElement(reference.key()), reference.key())));
        if (!collector.isEmpty()) {
            collector.forEach((string, problem) -> LOGGER.warn("Found validation problem in {}: {}", string, (Object)problem.description()));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        return CompletableFuture.allOf((CompletableFuture[])writableRegistry.entrySet().stream().map(entry -> {
            ResourceKey resourceKey = (ResourceKey)entry.getKey();
            LootTable lootTable = (LootTable)entry.getValue();
            Path path = this.pathProvider.json(resourceKey.identifier());
            return DataProvider.saveStable(cachedOutput, provider, LootTable.DIRECT_CODEC, lootTable, path);
        }).toArray(CompletableFuture[]::new));
    }

    private static Identifier sequenceIdForLootTable(ResourceKey<LootTable> resourceKey) {
        return resourceKey.identifier();
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    private static /* synthetic */ void method_10410(HolderLookup.Provider provider, Map map, WritableRegistry writableRegistry, SubProviderEntry subProviderEntry) {
        subProviderEntry.provider().apply(provider).generate((resourceKey, builder) -> {
            Identifier identifier = LootTableProvider.sequenceIdForLootTable(resourceKey);
            Identifier identifier2 = map.put(RandomSequence.seedForKey(identifier), identifier);
            if (identifier2 != null) {
                Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + String.valueOf(identifier2) + " and " + String.valueOf(resourceKey.identifier()));
            }
            builder.setRandomSequence(identifier);
            LootTable lootTable = builder.setParamSet(subProviderEntry.paramSet).build();
            writableRegistry.register(resourceKey, lootTable, RegistrationInfo.BUILT_IN);
        });
    }

    public record MissingTableProblem(ResourceKey<LootTable> id) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Missing built-in table: " + String.valueOf(this.id.identifier());
        }
    }

    public static final class SubProviderEntry
    extends Record {
        private final Function<HolderLookup.Provider, LootTableSubProvider> provider;
        final ContextKeySet paramSet;

        public SubProviderEntry(Function<HolderLookup.Provider, LootTableSubProvider> function, ContextKeySet contextKeySet) {
            this.provider = function;
            this.paramSet = contextKeySet;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this, object);
        }

        public Function<HolderLookup.Provider, LootTableSubProvider> provider() {
            return this.provider;
        }

        public ContextKeySet paramSet() {
            return this.paramSet;
        }
    }
}

