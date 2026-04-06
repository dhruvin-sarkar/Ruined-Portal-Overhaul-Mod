/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class ServerStatsCounter
extends StatsCounter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Map<Stat<?>, Integer>> STATS_CODEC = Codec.dispatchedMap(BuiltInRegistries.STAT_TYPE.byNameCodec(), Util.memoize(ServerStatsCounter::createTypedStatsCodec)).xmap(map -> {
        HashMap map22 = new HashMap();
        map.forEach((statType, map2) -> map22.putAll(map2));
        return map22;
    }, map -> map.entrySet().stream().collect(Collectors.groupingBy(entry -> ((Stat)entry.getKey()).getType(), Util.toMap())));
    private final Path file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    private static <T> Codec<Map<Stat<?>, Integer>> createTypedStatsCodec(StatType<T> statType) {
        Codec<T> codec = statType.getRegistry().byNameCodec();
        Codec codec2 = codec.flatComapMap(statType::get, stat -> {
            if (stat.getType() == statType) {
                return DataResult.success(stat.getValue());
            }
            return DataResult.error(() -> "Expected type " + String.valueOf(statType) + ", but got " + String.valueOf(stat.getType()));
        });
        return Codec.unboundedMap((Codec)codec2, (Codec)Codec.INT);
    }

    public ServerStatsCounter(MinecraftServer minecraftServer, Path path) {
        this.file = path;
        if (Files.isRegularFile(path, new LinkOption[0])) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
                JsonElement jsonElement = StrictJsonParser.parse(reader);
                this.parse(minecraftServer.getFixerUpper(), jsonElement);
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't read statistics file {}", (Object)path, (Object)iOException);
            }
            catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)path, (Object)jsonParseException);
            }
        }
    }

    public void save() {
        try {
            FileUtil.createDirectoriesSafe(this.file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(this.toJson(), GSON.newJsonWriter((Writer)writer));
            }
        }
        catch (JsonIOException | IOException exception) {
            LOGGER.error("Couldn't save stats to {}", (Object)this.file, (Object)exception);
        }
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int i) {
        super.setValue(player, stat, i);
        this.dirty.add(stat);
    }

    private Set<Stat<?>> getDirty() {
        HashSet set = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return set;
    }

    public void parse(DataFixer dataFixer, JsonElement jsonElement) {
        Dynamic dynamic = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement);
        dynamic = DataFixTypes.STATS.updateToCurrentVersion(dataFixer, dynamic, NbtUtils.getDataVersion(dynamic, 1343));
        this.stats.putAll(STATS_CODEC.parse(dynamic.get("stats").orElseEmptyMap()).resultOrPartial(string -> LOGGER.error("Failed to parse statistics for {}: {}", (Object)this.file, string)).orElse(Map.of()));
    }

    protected JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("stats", (JsonElement)STATS_CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this.stats).getOrThrow());
        jsonObject.addProperty("DataVersion", (Number)SharedConstants.getCurrentVersion().dataVersion().version());
        return jsonObject;
    }

    public void markAllDirty() {
        this.dirty.addAll((Collection<Stat<?>>)this.stats.keySet());
    }

    public void sendStats(ServerPlayer serverPlayer) {
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        for (Stat<?> stat : this.getDirty()) {
            object2IntMap.put(stat, this.getValue(stat));
        }
        serverPlayer.connection.send(new ClientboundAwardStatsPacket((Object2IntMap<Stat<?>>)object2IntMap));
    }
}

