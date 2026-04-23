package com.ruinedportaloverhaul.config;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class ModConfigManager {
    private static final ModConfigAccess DEFAULTS = new DefaultConfig();
    private static volatile Supplier<ModConfigAccess> runtimeSource = () -> DEFAULTS;
    private static volatile boolean clothConfigAvailable;

    private ModConfigManager() {
    }

    public static void initialize() {
        // Fix: the major gameplay systems were hardcoded, so optional config support now installs lazily without forcing Cloth Config on every runtime.
        runtimeSource = () -> DEFAULTS;
        clothConfigAvailable = false;
        if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            return;
        }

        try {
            Class<?> bootstrapClass = Class.forName("com.ruinedportaloverhaul.config.cloth.ClothConfigBootstrap");
            bootstrapClass.getMethod("initialize").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            RuinedPortalOverhaul.LOGGER.warn("Failed to initialize optional Cloth Config support. Falling back to built-in defaults.", exception);
        }
    }

    public static void installRuntimeSource(Supplier<? extends ModConfigAccess> source) {
        runtimeSource = () -> Objects.requireNonNull(source.get(), "config source returned null");
        clothConfigAvailable = true;
    }

    public static boolean isClothConfigAvailable() {
        return clothConfigAvailable;
    }

    public static ModConfigAccess current() {
        return runtimeSource.get();
    }

    public static boolean enableAmbientNetherSpawns() {
        return current().enableAmbientNetherSpawns();
    }

    public static boolean enableOuterZoneScatter() {
        return current().enableOuterZoneScatter();
    }

    public static double raidTriggerRadius() {
        return current().raidTriggerRadius();
    }

    public static double waveCountMultiplier() {
        return current().waveCountMultiplier();
    }

    public static int interWaveDelayTicks() {
        return current().interWaveDelayTicks();
    }

    public static boolean enableBossBar() {
        return current().enableBossBar();
    }

    public static boolean enableRedStorm() {
        return current().enableRedStorm();
    }

    public static double stormIntensity() {
        return current().stormIntensity();
    }

    public static int thunderFrequency() {
        return current().thunderFrequency();
    }

    public static double mobHealthMultiplier() {
        return current().mobHealthMultiplier();
    }

    public static double mobDamageMultiplier() {
        return current().mobDamageMultiplier();
    }

    public static int ambientMobCap() {
        return current().ambientMobCap();
    }

    public static boolean enablePostRaidSuppression() {
        return current().enablePostRaidSuppression();
    }

    public static double netherStarDropRate() {
        return current().netherStarDropRate();
    }

    public static boolean enableNetherDragon() {
        return current().enableNetherDragon();
    }

    private static final class DefaultConfig implements ModConfigAccess {
        @Override
        public boolean enableAmbientNetherSpawns() {
            return true;
        }

        @Override
        public boolean enableOuterZoneScatter() {
            return true;
        }

        @Override
        public double raidTriggerRadius() {
            return 24.0;
        }

        @Override
        public double waveCountMultiplier() {
            return 1.0;
        }

        @Override
        public int interWaveDelayTicks() {
            return 300;
        }

        @Override
        public boolean enableBossBar() {
            return true;
        }

        @Override
        public boolean enableRedStorm() {
            return true;
        }

        @Override
        public double stormIntensity() {
            return 0.6;
        }

        @Override
        public int thunderFrequency() {
            return 80;
        }

        @Override
        public double mobHealthMultiplier() {
            return 1.0;
        }

        @Override
        public double mobDamageMultiplier() {
            return 1.0;
        }

        @Override
        public int ambientMobCap() {
            return 180;
        }

        @Override
        public boolean enablePostRaidSuppression() {
            return true;
        }

        @Override
        public double netherStarDropRate() {
            return 1.0;
        }

        @Override
        public boolean enableNetherDragon() {
            return true;
        }
    }
}
