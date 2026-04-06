/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  java.lang.ProcessHandle
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  joptsimple.ValueConverter
 *  joptsimple.util.PathConverter
 *  joptsimple.util.PathProperties
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.ValueConverter;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressForbidden(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] strings) {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("recreateRegionFiles");
        OptionSpecBuilder optionSpec8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec9 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo((Object)".", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo((Object)-1, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy((ValueConverter)new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec optionSpec16 = optionParser.nonOptions();
        try {
            WorldStem worldStem;
            Dynamic<?> dynamic;
            OptionSet optionSet = optionParser.parse(strings);
            if (optionSet.has((OptionSpec)optionSpec9)) {
                optionParser.printHelpOn((OutputStream)System.err);
                return;
            }
            Path path = (Path)optionSet.valueOf((OptionSpec)optionSpec15);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.preload();
            if (optionSet.has((OptionSpec)optionSpec14)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path path2 = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path2);
            dedicatedServerSettings.forceSave();
            RegionFileVersion.configure(dedicatedServerSettings.getProperties().regionFileComression);
            Path path3 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path3);
            if (optionSet.has((OptionSpec)optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File((String)optionSet.valueOf((OptionSpec)optionSpec10));
            Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable((String)optionSet.valueOf((OptionSpec)optionSpec11)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.validateAndCreateAccess(string);
            if (levelStorageAccess.hasWorldData()) {
                LevelSummary levelSummary;
                try {
                    dynamic = levelStorageAccess.getDataTag();
                    levelSummary = levelStorageAccess.getSummary(dynamic);
                }
                catch (IOException | NbtException | ReportedNbtException exception) {
                    LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)levelDirectory.dataFile(), (Object)exception);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        dynamic = levelStorageAccess.getDataTagFallback();
                        levelSummary = levelStorageAccess.getSummary(dynamic);
                    }
                    catch (IOException | NbtException | ReportedNbtException exception2) {
                        LOGGER.error("Failed to load world data from {}", (Object)levelDirectory.oldDataFile(), (Object)exception2);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)levelDirectory.dataFile(), (Object)levelDirectory.oldDataFile());
                        return;
                    }
                    levelStorageAccess.restoreLevelDataFromOld();
                }
                if (levelSummary.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!levelSummary.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }
            Dynamic<?> dynamic2 = dynamic;
            boolean bl = optionSet.has((OptionSpec)optionSpec8);
            if (bl) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
            try {
                WorldLoader.InitConfig initConfig = Main.loadOrCreateConfig(dedicatedServerSettings.getProperties(), dynamic2, bl, packRepository);
                worldStem = (WorldStem)Util.blockUntilDone(arg_0 -> Main.method_43612(initConfig, dynamic2, dedicatedServerSettings, optionSet, (OptionSpec)optionSpec3, (OptionSpec)optionSpec4, arg_0)).get();
            }
            catch (Exception exception3) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)exception3);
                return;
            }
            RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
            WorldData worldData = worldStem.worldData();
            boolean bl2 = optionSet.has((OptionSpec)optionSpec7);
            if (optionSet.has((OptionSpec)optionSpec5) || bl2) {
                Main.forceUpgrade(levelStorageAccess, worldData, DataFixers.getDataFixer(), optionSet.has((OptionSpec)optionSpec6), () -> true, frozen, bl2);
            }
            levelStorageAccess.saveDataTag(frozen, worldData);
            final DedicatedServer dedicatedServer = MinecraftServer.spin(arg_0 -> Main.method_29734(levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, services, optionSet, (OptionSpec)optionSpec12, (OptionSpec)optionSpec3, (OptionSpec)optionSpec13, (OptionSpec)optionSpec, (OptionSpec)optionSpec16, arg_0));
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", throwable);
        }
    }

    private static WorldLoader.DataLoadOutput<WorldData> createNewWorldData(DedicatedServerSettings dedicatedServerSettings, WorldLoader.DataLoadContext dataLoadContext, Registry<LevelStem> registry, boolean bl, boolean bl2) {
        WorldDimensions worldDimensions;
        WorldOptions worldOptions;
        LevelSettings levelSettings;
        if (bl) {
            levelSettings = MinecraftServer.DEMO_SETTINGS;
            worldOptions = WorldOptions.DEMO_OPTIONS;
            worldDimensions = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen());
        } else {
            DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
            levelSettings = new LevelSettings(dedicatedServerProperties.levelName, dedicatedServerProperties.gameMode.get(), dedicatedServerProperties.hardcore, dedicatedServerProperties.difficulty.get(), false, new GameRules(dataLoadContext.dataConfiguration().enabledFeatures()), dataLoadContext.dataConfiguration());
            worldOptions = bl2 ? dedicatedServerProperties.worldOptions.withBonusChest(true) : dedicatedServerProperties.worldOptions;
            worldDimensions = dedicatedServerProperties.createDimensions(dataLoadContext.datapackWorldgen());
        }
        WorldDimensions.Complete complete = worldDimensions.bake(registry);
        Lifecycle lifecycle = complete.lifecycle().add(dataLoadContext.datapackWorldgen().allRegistriesLifecycle());
        return new WorldLoader.DataLoadOutput<WorldData>(new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle), complete.dimensionsRegistryAccess());
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString((Path)path, (CharSequence)Long.toString(l), (OpenOption[])new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static WorldLoader.InitConfig loadOrCreateConfig(DedicatedServerProperties dedicatedServerProperties, @Nullable Dynamic<?> dynamic, boolean bl, PackRepository packRepository) {
        WorldDataConfiguration worldDataConfiguration2;
        boolean bl2;
        if (dynamic != null) {
            WorldDataConfiguration worldDataConfiguration = LevelStorageSource.readDataConfig(dynamic);
            bl2 = false;
            worldDataConfiguration2 = worldDataConfiguration;
        } else {
            bl2 = true;
            worldDataConfiguration2 = new WorldDataConfiguration(dedicatedServerProperties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration2, bl, bl2);
        return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, dedicatedServerProperties.functionPermissions);
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, DataFixer dataFixer, boolean bl, BooleanSupplier booleanSupplier, RegistryAccess registryAccess, boolean bl2) {
        LOGGER.info("Forcing world upgrade!");
        try (WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldData, registryAccess, bl, bl2);){
            Component component = null;
            while (!worldUpgrader.isFinished()) {
                int i;
                Component component2 = worldUpgrader.getStatus();
                if (component != component2) {
                    component = component2;
                    LOGGER.info(worldUpgrader.getStatus().getString());
                }
                if ((i = worldUpgrader.getTotalChunks()) > 0) {
                    int j = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
                    LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{Mth.floor((float)j / (float)i * 100.0f), j, i});
                }
                if (!booleanSupplier.getAsBoolean()) {
                    worldUpgrader.cancel();
                    continue;
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
    }

    private static /* synthetic */ DedicatedServer method_29734(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, DedicatedServerSettings dedicatedServerSettings, Services services, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, Thread thread) {
        boolean bl;
        DedicatedServer dedicatedServer = new DedicatedServer(thread, levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, DataFixers.getDataFixer(), services);
        dedicatedServer.setPort((Integer)optionSet.valueOf(optionSpec));
        dedicatedServer.setDemo(optionSet.has(optionSpec2));
        dedicatedServer.setId((String)optionSet.valueOf(optionSpec3));
        boolean bl2 = bl = !optionSet.has(optionSpec4) && !optionSet.valuesOf(optionSpec5).contains("nogui");
        if (bl && !GraphicsEnvironment.isHeadless()) {
            dedicatedServer.showGui();
        }
        return dedicatedServer;
    }

    private static /* synthetic */ CompletableFuture method_43612(WorldLoader.InitConfig initConfig, Dynamic dynamic, DedicatedServerSettings dedicatedServerSettings, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, Executor executor) {
        return WorldLoader.load(initConfig, dataLoadContext -> {
            HolderLookup.RegistryLookup registry = dataLoadContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
            if (dynamic != null) {
                LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, dataLoadContext.dataConfiguration(), (Registry<LevelStem>)registry, dataLoadContext.datapackWorldgen());
                return new WorldLoader.DataLoadOutput<WorldData>(levelDataAndDimensions.worldData(), levelDataAndDimensions.dimensions().dimensionsRegistryAccess());
            }
            LOGGER.info("No existing world data, creating new world");
            return Main.createNewWorldData(dedicatedServerSettings, dataLoadContext, (Registry<LevelStem>)registry, optionSet.has(optionSpec), optionSet.has(optionSpec2));
        }, WorldStem::new, Util.backgroundExecutor(), executor);
    }
}

