/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(InitConfig initConfig, WorldDataSupplier<D> worldDataSupplier, ResultFactory<D, R> resultFactory, Executor executor, Executor executor2) {
        try {
            Pair<WorldDataConfiguration, CloseableResourceManager> pair = initConfig.packConfig.createResourceManager();
            CloseableResourceManager closeableResourceManager = (CloseableResourceManager)pair.getSecond();
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = RegistryLayer.createRegistryAccess();
            List<Registry.PendingTags<?>> list = TagLoader.loadTagsForExistingRegistries(closeableResourceManager, layeredRegistryAccess.getLayer(RegistryLayer.STATIC));
            RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(RegistryLayer.WORLDGEN);
            List<HolderLookup.RegistryLookup<?>> list2 = TagLoader.buildUpdatedLookups(frozen, list);
            RegistryAccess.Frozen frozen2 = RegistryDataLoader.load(closeableResourceManager, list2, RegistryDataLoader.WORLDGEN_REGISTRIES);
            List list3 = Stream.concat(list2.stream(), frozen2.listRegistries()).toList();
            RegistryAccess.Frozen frozen3 = RegistryDataLoader.load(closeableResourceManager, list3, RegistryDataLoader.DIMENSION_REGISTRIES);
            WorldDataConfiguration worldDataConfiguration = (WorldDataConfiguration)((Object)pair.getFirst());
            HolderLookup.Provider provider = HolderLookup.Provider.create(list3.stream());
            DataLoadOutput<D> dataLoadOutput = worldDataSupplier.get(new DataLoadContext(closeableResourceManager, worldDataConfiguration, provider, frozen3));
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = layeredRegistryAccess.replaceFrom(RegistryLayer.WORLDGEN, frozen2, dataLoadOutput.finalDimensions);
            return ((CompletableFuture)ReloadableServerResources.loadResources(closeableResourceManager, layeredRegistryAccess2, list, worldDataConfiguration.enabledFeatures(), initConfig.commandSelection(), initConfig.functionCompilationPermissions(), executor, executor2).whenComplete((reloadableServerResources, throwable) -> {
                if (throwable != null) {
                    closeableResourceManager.close();
                }
            })).thenApplyAsync(reloadableServerResources -> {
                reloadableServerResources.updateStaticRegistryTags();
                return resultFactory.create(closeableResourceManager, (ReloadableServerResources)reloadableServerResources, layeredRegistryAccess2, dataLoadOutput.cookie);
            }, executor2);
        }
        catch (Exception exception) {
            return CompletableFuture.failedFuture((Throwable)exception);
        }
    }

    public static final class InitConfig
    extends Record {
        final PackConfig packConfig;
        private final Commands.CommandSelection commandSelection;
        private final PermissionSet functionCompilationPermissions;

        public InitConfig(PackConfig packConfig, Commands.CommandSelection commandSelection, PermissionSet permissionSet) {
            this.packConfig = packConfig;
            this.commandSelection = commandSelection;
            this.functionCompilationPermissions = permissionSet;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this, object);
        }

        public PackConfig packConfig() {
            return this.packConfig;
        }

        public Commands.CommandSelection commandSelection() {
            return this.commandSelection;
        }

        public PermissionSet functionCompilationPermissions() {
            return this.functionCompilationPermissions;
        }
    }

    public record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
            WorldDataConfiguration worldDataConfiguration = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig, this.initMode, this.safeMode);
            List<PackResources> list = this.packRepository.openAllSelected();
            MultiPackResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
            return Pair.of((Object)((Object)worldDataConfiguration), (Object)closeableResourceManager);
        }
    }

    public record DataLoadContext(ResourceManager resources, WorldDataConfiguration dataConfiguration, HolderLookup.Provider datapackWorldgen, RegistryAccess.Frozen datapackDimensions) {
    }

    @FunctionalInterface
    public static interface WorldDataSupplier<D> {
        public DataLoadOutput<D> get(DataLoadContext var1);
    }

    public static final class DataLoadOutput<D>
    extends Record {
        final D cookie;
        final RegistryAccess.Frozen finalDimensions;

        public DataLoadOutput(D object, RegistryAccess.Frozen frozen) {
            this.cookie = object;
            this.finalDimensions = frozen;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this, object);
        }

        public D cookie() {
            return this.cookie;
        }

        public RegistryAccess.Frozen finalDimensions() {
            return this.finalDimensions;
        }
    }

    @FunctionalInterface
    public static interface ResultFactory<D, R> {
        public R create(CloseableResourceManager var1, ReloadableServerResources var2, LayeredRegistryAccess<RegistryLayer> var3, D var4);
    }
}

