/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  com.mojang.util.UndashedUuid
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.blaze3d.TracyBootstrap;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Optionull;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@net.fabricmc.api.Environment(value=EnvType.CLIENT)
public class Main {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @DontObfuscate
    public static void main(String[] strings) {
        GameConfig gameConfig;
        Logger logger;
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("disableMultiplayer");
        optionParser.accepts("disableChat");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpecBuilder optionSpec = optionParser.accepts("renderDebugLabels");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("jfrProfile");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("tracy");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("tracyNoImages");
        ArgumentAcceptingOptionSpec optionSpec5 = optionParser.accepts("quickPlayPath").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec6 = optionParser.accepts("quickPlaySingleplayer").withOptionalArg();
        ArgumentAcceptingOptionSpec optionSpec7 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec8 = optionParser.accepts("quickPlayRealms").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec9 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo((Object)new File("."), (Object[])new File[0]);
        ArgumentAcceptingOptionSpec optionSpec10 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec optionSpec11 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec optionSpec12 = optionParser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec13 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo((Object)"8080", (Object[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec optionSpec14 = optionParser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec15 = optionParser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec16 = optionParser.accepts("username").withRequiredArg().defaultsTo((Object)("Player" + System.currentTimeMillis() % 1000L), (Object[])new String[0]);
        OptionSpecBuilder optionSpec17 = optionParser.accepts("offlineDeveloperMode");
        ArgumentAcceptingOptionSpec optionSpec18 = optionParser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec19 = optionParser.accepts("xuid").withOptionalArg().defaultsTo((Object)"", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec20 = optionParser.accepts("clientId").withOptionalArg().defaultsTo((Object)"", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec21 = optionParser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec optionSpec22 = optionParser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec optionSpec23 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo((Object)854, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec optionSpec24 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo((Object)480, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec optionSpec25 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec optionSpec26 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec optionSpec27 = optionParser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec28 = optionParser.accepts("versionType").withRequiredArg().defaultsTo((Object)"release", (Object[])new String[0]);
        NonOptionArgumentSpec optionSpec29 = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(strings);
        File file = (File)Main.parseArgument(optionSet, optionSpec9);
        String string = (String)Main.parseArgument(optionSet, optionSpec22);
        String string2 = "Pre-bootstrap";
        try {
            if (optionSet.has((OptionSpec)optionSpec2)) {
                JvmProfiler.INSTANCE.start(Environment.CLIENT);
            }
            if (optionSet.has((OptionSpec)optionSpec3)) {
                TracyBootstrap.setup();
            }
            Stopwatch stopwatch = Stopwatch.createStarted((Ticker)Ticker.systemTicker());
            Stopwatch stopwatch2 = Stopwatch.createStarted((Ticker)Ticker.systemTicker());
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, stopwatch);
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, stopwatch2);
            SharedConstants.tryDetectVersion();
            TracyClient.reportAppInfo((String)("Minecraft Java Edition " + SharedConstants.getCurrentVersion().name()));
            CompletableFuture<?> completableFuture = DataFixers.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST);
            CrashReport.preload();
            logger = LogUtils.getLogger();
            string2 = "Bootstrap";
            Bootstrap.bootStrap();
            ClientBootstrap.bootstrap();
            GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
            Bootstrap.validate();
            string2 = "Argument parsing";
            List list = optionSet.valuesOf((OptionSpec)optionSpec29);
            if (!list.isEmpty()) {
                logger.info("Completely ignored arguments: {}", (Object)list);
            }
            String string3 = (String)Main.parseArgument(optionSet, optionSpec12);
            Proxy proxy = Proxy.NO_PROXY;
            if (string3 != null) {
                try {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(string3, (int)((Integer)Main.parseArgument(optionSet, optionSpec13))));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            final String string4 = (String)Main.parseArgument(optionSet, optionSpec14);
            final String string5 = (String)Main.parseArgument(optionSet, optionSpec15);
            if (!proxy.equals(Proxy.NO_PROXY) && Main.stringHasValue(string4) && Main.stringHasValue(string5)) {
                Authenticator.setDefault(new Authenticator(){

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(string4, string5.toCharArray());
                    }
                });
            }
            int i = (Integer)Main.parseArgument(optionSet, optionSpec23);
            int j = (Integer)Main.parseArgument(optionSet, optionSpec24);
            OptionalInt optionalInt = Main.ofNullable((Integer)Main.parseArgument(optionSet, optionSpec25));
            OptionalInt optionalInt2 = Main.ofNullable((Integer)Main.parseArgument(optionSet, optionSpec26));
            boolean bl = optionSet.has("fullscreen");
            boolean bl2 = optionSet.has("demo");
            boolean bl3 = optionSet.has("disableMultiplayer");
            boolean bl4 = optionSet.has("disableChat");
            boolean bl5 = !optionSet.has((OptionSpec)optionSpec4);
            boolean bl6 = optionSet.has((OptionSpec)optionSpec);
            String string6 = (String)Main.parseArgument(optionSet, optionSpec28);
            File file2 = optionSet.has((OptionSpec)optionSpec10) ? (File)Main.parseArgument(optionSet, optionSpec10) : new File(file, "assets/");
            File file3 = optionSet.has((OptionSpec)optionSpec11) ? (File)Main.parseArgument(optionSet, optionSpec11) : new File(file, "resourcepacks/");
            UUID uUID = Main.hasValidUuid((OptionSpec<String>)optionSpec18, optionSet, logger) ? UndashedUuid.fromStringLenient((String)((String)optionSpec18.value(optionSet))) : UUIDUtil.createOfflinePlayerUUID((String)optionSpec16.value(optionSet));
            String string7 = optionSet.has((OptionSpec)optionSpec27) ? (String)optionSpec27.value(optionSet) : null;
            String string8 = (String)optionSet.valueOf((OptionSpec)optionSpec19);
            String string9 = (String)optionSet.valueOf((OptionSpec)optionSpec20);
            String string10 = (String)Main.parseArgument(optionSet, optionSpec5);
            GameConfig.QuickPlayVariant quickPlayVariant = Main.getQuickPlayVariant(optionSet, (OptionSpec<String>)optionSpec6, (OptionSpec<String>)optionSpec7, (OptionSpec<String>)optionSpec8);
            User user = new User((String)optionSpec16.value(optionSet), uUID, (String)optionSpec21.value(optionSet), Main.emptyStringToEmptyOptional(string8), Main.emptyStringToEmptyOptional(string9));
            gameConfig = new GameConfig(new GameConfig.UserData(user, proxy), new DisplayData(i, j, optionalInt, optionalInt2, bl), new GameConfig.FolderData(file, file3, file2, string7), new GameConfig.GameData(bl2, string, string6, bl3, bl4, bl5, bl6, optionSet.has((OptionSpec)optionSpec17)), new GameConfig.QuickPlayData(string10, quickPlayVariant));
            Util.startTimerHackThread();
            completableFuture.join();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, string2);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
            NativeModuleLister.addCrashSection(crashReportCategory);
            Minecraft.fillReport(null, null, string, null, crashReport);
            Minecraft.crash(null, file, crashReport);
            return;
        }
        Thread thread = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft == null) {
                    return;
                }
                IntegratedServer integratedServer = minecraft.getSingleplayerServer();
                if (integratedServer != null) {
                    integratedServer.halt(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(logger));
        Runtime.getRuntime().addShutdownHook(thread);
        Minecraft minecraft = null;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            minecraft = new Minecraft(gameConfig);
        }
        catch (SilentInitException silentInitException) {
            Util.shutdownExecutors();
            logger.warn("Failed to create window: ", (Throwable)silentInitException);
            return;
        }
        catch (Throwable throwable2) {
            CrashReport crashReport2 = CrashReport.forThrowable(throwable2, "Initializing game");
            CrashReportCategory crashReportCategory2 = crashReport2.addCategory("Initialization");
            NativeModuleLister.addCrashSection(crashReportCategory2);
            Minecraft.fillReport(minecraft, null, gameConfig.game.launchVersion, null, crashReport2);
            Minecraft.crash(minecraft, gameConfig.location.gameDirectory, crashReport2);
            return;
        }
        Minecraft minecraft2 = minecraft;
        minecraft2.run();
        try {
            minecraft2.stop();
        }
        finally {
            minecraft2.destroy();
        }
    }

    private static GameConfig.QuickPlayVariant getQuickPlayVariant(OptionSet optionSet, OptionSpec<String> optionSpec, OptionSpec<String> optionSpec2, OptionSpec<String> optionSpec3) {
        long l = Stream.of(optionSpec, optionSpec2, optionSpec3).filter(arg_0 -> ((OptionSet)optionSet).has(arg_0)).count();
        if (l == 0L) {
            return GameConfig.QuickPlayVariant.DISABLED;
        }
        if (l > 1L) {
            throw new IllegalArgumentException("Only one quick play option can be specified");
        }
        if (optionSet.has(optionSpec)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec));
            return new GameConfig.QuickPlaySinglePlayerData(string);
        }
        if (optionSet.has(optionSpec2)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec2));
            return Optionull.mapOrDefault(string, GameConfig.QuickPlayMultiplayerData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        if (optionSet.has(optionSpec3)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec3));
            return Optionull.mapOrDefault(string, GameConfig.QuickPlayRealmsData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        return GameConfig.QuickPlayVariant.DISABLED;
    }

    private static @Nullable String unescapeJavaArgument(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava((String)string);
    }

    private static Optional<String> emptyStringToEmptyOptional(String string) {
        return string.isEmpty() ? Optional.empty() : Optional.of(string);
    }

    private static OptionalInt ofNullable(@Nullable Integer integer) {
        return integer != null ? OptionalInt.of(integer) : OptionalInt.empty();
    }

    private static <T> @Nullable T parseArgument(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return (T)optionSet.valueOf(optionSpec);
        }
        catch (Throwable throwable) {
            ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec;
            List list;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(list = (argumentAcceptingOptionSpec = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)list.get(0);
            }
            throw throwable;
        }
    }

    private static boolean stringHasValue(@Nullable String string) {
        return string != null && !string.isEmpty();
    }

    private static boolean hasValidUuid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
        return optionSet.has(optionSpec) && Main.isUuidValid(optionSpec, optionSet, logger);
    }

    private static boolean isUuidValid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
        try {
            UndashedUuid.fromStringLenient((String)((String)optionSpec.value(optionSet)));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Invalid UUID: '{}", optionSpec.value(optionSet));
            return false;
        }
        return true;
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

