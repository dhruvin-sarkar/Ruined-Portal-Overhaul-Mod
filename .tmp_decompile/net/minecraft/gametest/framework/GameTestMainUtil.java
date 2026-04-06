/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SuppressForbidden;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class GameTestMainUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_UNIVERSE_DIR = "gametestserver";
    private static final String LEVEL_NAME = "gametestworld";
    private static final OptionParser parser = new OptionParser();
    private static final OptionSpec<String> universe = parser.accepts("universe", "The path to where the test server world will be created. Any existing folder will be replaced.").withRequiredArg().defaultsTo((Object)"gametestserver", (Object[])new String[0]);
    private static final OptionSpec<File> report = parser.accepts("report", "Exports results in a junit-like XML report at the given path.").withRequiredArg().ofType(File.class);
    private static final OptionSpec<String> tests = parser.accepts("tests", "Which test(s) to run (namespaced ID selector using wildcards). Empty means run all.").withRequiredArg();
    private static final OptionSpec<Boolean> verify = parser.accepts("verify", "Runs the tests specified with `test` or `testNamespace` 100 times for each 90 degree rotation step").withRequiredArg().ofType(Boolean.class).defaultsTo((Object)false, (Object[])new Boolean[0]);
    private static final OptionSpec<String> packs = parser.accepts("packs", "A folder of datapacks to include in the world").withRequiredArg();
    private static final OptionSpec<Void> help = parser.accepts("help").forHelp();

    @SuppressForbidden(reason="Using System.err due to no bootstrap")
    public static void runGameTestServer(String[] strings, Consumer<String> consumer) throws Exception {
        parser.allowsUnrecognizedOptions();
        OptionSet optionSet = parser.parse(strings);
        if (optionSet.has(help)) {
            parser.printHelpOn((OutputStream)System.err);
            return;
        }
        if (((Boolean)optionSet.valueOf(verify)).booleanValue() && !optionSet.has(tests)) {
            LOGGER.error("Please specify a test selection to run the verify option. For example: --verify --tests example:test_something_*");
            System.exit(-1);
        }
        LOGGER.info("Running GameTestMain with cwd '{}', universe path '{}'", (Object)System.getProperty("user.dir"), optionSet.valueOf(universe));
        if (optionSet.has(report)) {
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter((File)report.value(optionSet)));
        }
        Bootstrap.bootStrap();
        Util.startTimerHackThread();
        String string = (String)optionSet.valueOf(universe);
        GameTestMainUtil.createOrResetDir(string);
        consumer.accept(string);
        if (optionSet.has(packs)) {
            String string2 = (String)optionSet.valueOf(packs);
            GameTestMainUtil.copyPacks(string, string2);
        }
        LevelStorageSource.LevelStorageAccess levelStorageAccess = LevelStorageSource.createDefault(Paths.get(string, new String[0])).createAccess(LEVEL_NAME);
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        MinecraftServer.spin(thread -> GameTestServer.create(thread, levelStorageAccess, packRepository, GameTestMainUtil.optionalFromOption(optionSet, tests), optionSet.has(verify)));
    }

    private static Optional<String> optionalFromOption(OptionSet optionSet, OptionSpec<String> optionSpec) {
        return optionSet.has(optionSpec) ? Optional.of((String)optionSet.valueOf(optionSpec)) : Optional.empty();
    }

    private static void createOrResetDir(String string) throws IOException {
        Path path = Paths.get(string, new String[0]);
        if (Files.exists(path, new LinkOption[0])) {
            FileUtils.deleteDirectory((File)path.toFile());
        }
        Files.createDirectories(path, new FileAttribute[0]);
    }

    private static void copyPacks(String string, String string2) throws IOException {
        Path path2;
        Path path = Paths.get(string, new String[0]).resolve(LEVEL_NAME).resolve("datapacks");
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        if (Files.exists(path2 = Paths.get(string2, new String[0]), new LinkOption[0])) {
            try (Stream<Path> stream = Files.list(path2);){
                for (Path path3 : stream.toList()) {
                    Path path4 = path.resolve(path3.getFileName());
                    if (Files.isDirectory(path3, new LinkOption[0])) {
                        if (!Files.isRegularFile(path3.resolve("pack.mcmeta"), new LinkOption[0])) continue;
                        FileUtils.copyDirectory((File)path3.toFile(), (File)path4.toFile());
                        LOGGER.info("Included folder pack {}", (Object)path3.getFileName());
                        continue;
                    }
                    if (!path3.toString().endsWith(".zip")) continue;
                    Files.copy(path3, path4, new CopyOption[0]);
                    LOGGER.info("Included zip pack {}", (Object)path3.getFileName());
                }
            }
        }
    }
}

