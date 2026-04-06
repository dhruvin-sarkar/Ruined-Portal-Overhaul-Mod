/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<ServerLevel, LevelData> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build((CacheLoader)new CacheLoader<ServerLevel, LevelData>(){

        public LevelData load(ServerLevel serverLevel) {
            return new LevelData((Object2IntMap<FeatureData>)Object2IntMaps.synchronize((Object2IntMap)new Object2IntOpenHashMap()), new MutableInt(0));
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((ServerLevel)object);
        }
    });

    public static void chunkDecorated(ServerLevel serverLevel) {
        try {
            ((LevelData)((Object)data.get((Object)serverLevel))).chunksWithFeatures().increment();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to increment chunk count", (Throwable)exception);
        }
    }

    public static void featurePlaced(ServerLevel serverLevel, ConfiguredFeature<?, ?> configuredFeature, Optional<PlacedFeature> optional) {
        try {
            ((LevelData)((Object)data.get((Object)serverLevel))).featureData().computeInt((Object)new FeatureData(configuredFeature, optional), (featureData, integer) -> integer == null ? 1 : integer + 1);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to increment feature count", (Throwable)exception);
        }
    }

    public static void clearCounts() {
        data.invalidateAll();
        LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        LOGGER.debug("Logging feature counts:");
        data.asMap().forEach((serverLevel, levelData) -> {
            String string = serverLevel.dimension().identifier().toString();
            boolean bl = serverLevel.getServer().isRunning();
            HolderLookup.RegistryLookup registry = serverLevel.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            String string2 = (bl ? "running" : "dead") + " " + string;
            int i = levelData.chunksWithFeatures().intValue();
            LOGGER.debug("{} total_chunks: {}", (Object)string2, (Object)i);
            levelData.featureData().forEach((arg_0, arg_1) -> FeatureCountTracker.method_39602(string2, i, (Registry)registry, arg_0, arg_1));
        });
    }

    private static /* synthetic */ void method_39602(String string, int i, Registry registry, FeatureData featureData, int j) {
        Object[] objectArray = new Object[6];
        objectArray[0] = string;
        objectArray[1] = String.format(Locale.ROOT, "%10d", j);
        objectArray[2] = String.format(Locale.ROOT, "%10f", (double)j / (double)i);
        objectArray[3] = featureData.topFeature().flatMap(registry::getResourceKey).map(ResourceKey::identifier);
        objectArray[4] = featureData.feature().feature();
        objectArray[5] = featureData.feature();
        LOGGER.debug("{} {} {} {} {} {}", objectArray);
    }

    record LevelData(Object2IntMap<FeatureData> featureData, MutableInt chunksWithFeatures) {
    }

    record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
    }
}

