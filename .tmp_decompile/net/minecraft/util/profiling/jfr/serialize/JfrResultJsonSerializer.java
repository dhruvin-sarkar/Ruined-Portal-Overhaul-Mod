/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.LongSerializationPolicy
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.FpsStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.StructureGenStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class JfrResultJsonSerializer {
    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

    private static void serializePacketId(PacketIdentification packetIdentification, JsonObject jsonObject) {
        jsonObject.addProperty("protocolId", packetIdentification.protocolId());
        jsonObject.addProperty("packetId", packetIdentification.packetId());
    }

    private static void serializeChunkId(ChunkIdentification chunkIdentification, JsonObject jsonObject) {
        jsonObject.addProperty("level", chunkIdentification.level());
        jsonObject.addProperty("dimension", chunkIdentification.dimension());
        jsonObject.addProperty("x", (Number)chunkIdentification.x());
        jsonObject.addProperty("z", (Number)chunkIdentification.z());
    }

    public String format(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("startedEpoch", (Number)jfrStatsResult.recordingStarted().toEpochMilli());
        jsonObject.addProperty("endedEpoch", (Number)jfrStatsResult.recordingEnded().toEpochMilli());
        jsonObject.addProperty("durationMs", (Number)jfrStatsResult.recordingDuration().toMillis());
        Duration duration = jfrStatsResult.worldCreationDuration();
        if (duration != null) {
            jsonObject.addProperty("worldGenDurationMs", (Number)duration.toMillis());
        }
        jsonObject.add("heap", this.heap(jfrStatsResult.heapSummary()));
        jsonObject.add("cpuPercent", this.cpu(jfrStatsResult.cpuLoadStats()));
        jsonObject.add("network", this.network(jfrStatsResult));
        jsonObject.add("fileIO", this.fileIO(jfrStatsResult));
        jsonObject.add("fps", this.fps(jfrStatsResult.fps()));
        jsonObject.add("serverTick", this.serverTicks(jfrStatsResult.serverTickTimes()));
        jsonObject.add("threadAllocation", this.threadAllocations(jfrStatsResult.threadAllocationSummary()));
        jsonObject.add("chunkGen", this.chunkGen(jfrStatsResult.chunkGenSummary()));
        jsonObject.add("structureGen", this.structureGen(jfrStatsResult.structureGenStats()));
        return this.gson.toJson((JsonElement)jsonObject);
    }

    private JsonElement heap(GcHeapStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("allocationRateBytesPerSecond", (Number)summary.allocationRateBytesPerSecond());
        jsonObject.addProperty("gcCount", (Number)summary.totalGCs());
        jsonObject.addProperty("gcOverHeadPercent", (Number)Float.valueOf(summary.gcOverHead()));
        jsonObject.addProperty("gcTotalDurationMs", (Number)summary.gcTotalDuration().toMillis());
        return jsonObject;
    }

    private JsonElement structureGen(List<StructureGenStat> list2) {
        JsonObject jsonObject = new JsonObject();
        Optional<TimedStatSummary<StructureGenStat>> optional = TimedStatSummary.summary(list2);
        if (optional.isEmpty()) {
            return jsonObject;
        }
        TimedStatSummary<StructureGenStat> timedStatSummary = optional.get();
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("structure", (JsonElement)jsonArray);
        list2.stream().collect(Collectors.groupingBy(StructureGenStat::structureName)).forEach((string, list) -> {
            Optional optional = TimedStatSummary.summary(list);
            if (optional.isEmpty()) {
                return;
            }
            TimedStatSummary timedStatSummary2 = optional.get();
            JsonObject jsonObject22 = new JsonObject();
            jsonArray.add((JsonElement)jsonObject22);
            jsonObject22.addProperty("name", string);
            jsonObject22.addProperty(COUNT, (Number)timedStatSummary2.count());
            jsonObject22.addProperty(DURATION_NANOS_TOTAL, (Number)timedStatSummary2.totalDuration().toNanos());
            jsonObject22.addProperty("durationNanosAvg", (Number)(timedStatSummary2.totalDuration().toNanos() / (long)timedStatSummary2.count()));
            JsonObject jsonObject3 = Util.make(new JsonObject(), jsonObject2 -> jsonObject22.add("durationNanosPercentiles", (JsonElement)jsonObject2));
            timedStatSummary2.percentilesNanos().forEach((integer, double_) -> jsonObject3.addProperty("p" + integer, (Number)double_));
            Function<StructureGenStat, JsonElement> function = structureGenStat -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("durationNanos", (Number)structureGenStat.duration().toNanos());
                jsonObject.addProperty("chunkPosX", (Number)structureGenStat.chunkPos().x);
                jsonObject.addProperty("chunkPosZ", (Number)structureGenStat.chunkPos().z);
                jsonObject.addProperty("structureName", structureGenStat.structureName());
                jsonObject.addProperty("level", structureGenStat.level());
                jsonObject.addProperty("success", Boolean.valueOf(structureGenStat.success()));
                return jsonObject;
            };
            jsonObject.add("fastest", function.apply((StructureGenStat)timedStatSummary.fastest()));
            jsonObject.add("slowest", function.apply((StructureGenStat)timedStatSummary.slowest()));
            jsonObject.add("secondSlowest", (JsonElement)(timedStatSummary.secondSlowest() != null ? function.apply((StructureGenStat)timedStatSummary.secondSlowest()) : JsonNull.INSTANCE));
        });
        return jsonObject;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
        JsonObject jsonObject = new JsonObject();
        if (list.isEmpty()) {
            return jsonObject;
        }
        jsonObject.addProperty(DURATION_NANOS_TOTAL, (Number)list.stream().mapToDouble(pair -> ((TimedStatSummary)((Object)((Object)pair.getSecond()))).totalDuration().toNanos()).sum());
        JsonArray jsonArray2 = Util.make(new JsonArray(), jsonArray -> jsonObject.add("status", (JsonElement)jsonArray));
        for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair2 : list) {
            TimedStatSummary timedStatSummary = (TimedStatSummary)((Object)pair2.getSecond());
            JsonObject jsonObject22 = Util.make(new JsonObject(), arg_0 -> ((JsonArray)jsonArray2).add(arg_0));
            jsonObject22.addProperty("state", ((ChunkStatus)pair2.getFirst()).toString());
            jsonObject22.addProperty(COUNT, (Number)timedStatSummary.count());
            jsonObject22.addProperty(DURATION_NANOS_TOTAL, (Number)timedStatSummary.totalDuration().toNanos());
            jsonObject22.addProperty("durationNanosAvg", (Number)(timedStatSummary.totalDuration().toNanos() / (long)timedStatSummary.count()));
            JsonObject jsonObject3 = Util.make(new JsonObject(), jsonObject2 -> jsonObject22.add("durationNanosPercentiles", (JsonElement)jsonObject2));
            timedStatSummary.percentilesNanos().forEach((integer, double_) -> jsonObject3.addProperty("p" + integer, (Number)double_));
            Function<ChunkGenStat, JsonElement> function = chunkGenStat -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("durationNanos", (Number)chunkGenStat.duration().toNanos());
                jsonObject.addProperty("level", chunkGenStat.level());
                jsonObject.addProperty("chunkPosX", (Number)chunkGenStat.chunkPos().x);
                jsonObject.addProperty("chunkPosZ", (Number)chunkGenStat.chunkPos().z);
                jsonObject.addProperty("worldPosX", (Number)chunkGenStat.worldPos().x());
                jsonObject.addProperty("worldPosZ", (Number)chunkGenStat.worldPos().z());
                return jsonObject;
            };
            jsonObject22.add("fastest", function.apply((ChunkGenStat)timedStatSummary.fastest()));
            jsonObject22.add("slowest", function.apply((ChunkGenStat)timedStatSummary.slowest()));
            jsonObject22.add("secondSlowest", (JsonElement)(timedStatSummary.secondSlowest() != null ? function.apply((ChunkGenStat)timedStatSummary.secondSlowest()) : JsonNull.INSTANCE));
        }
        return jsonObject;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.Summary summary) {
        JsonArray jsonArray = new JsonArray();
        summary.allocationsPerSecondByThread().forEach((string, double_) -> jsonArray.add((JsonElement)Util.make(new JsonObject(), jsonObject -> {
            jsonObject.addProperty("thread", string);
            jsonObject.addProperty(BYTES_PER_SECOND, (Number)double_);
        })));
        return jsonArray;
    }

    private JsonElement serverTicks(List<TickTimeStat> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        double[] ds = list.stream().mapToDouble(tickTimeStat -> (double)tickTimeStat.currentAverage().toNanos() / 1000000.0).toArray();
        DoubleSummaryStatistics doubleSummaryStatistics = DoubleStream.of(ds).summaryStatistics();
        jsonObject.addProperty("minMs", (Number)doubleSummaryStatistics.getMin());
        jsonObject.addProperty("averageMs", (Number)doubleSummaryStatistics.getAverage());
        jsonObject.addProperty("maxMs", (Number)doubleSummaryStatistics.getMax());
        Map<Integer, Double> map = Percentiles.evaluate(ds);
        map.forEach((integer, double_) -> jsonObject.addProperty("p" + integer, (Number)double_));
        return jsonObject;
    }

    private JsonElement fps(List<FpsStat> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        int[] is = list.stream().mapToInt(FpsStat::fps).toArray();
        IntSummaryStatistics intSummaryStatistics = IntStream.of(is).summaryStatistics();
        jsonObject.addProperty("minFPS", (Number)intSummaryStatistics.getMin());
        jsonObject.addProperty("averageFPS", (Number)intSummaryStatistics.getAverage());
        jsonObject.addProperty("maxFPS", (Number)intSummaryStatistics.getMax());
        Map<Integer, Double> map = Percentiles.evaluate(is);
        map.forEach((integer, double_) -> jsonObject.addProperty("p" + integer, (Number)double_));
        return jsonObject;
    }

    private JsonElement fileIO(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("write", this.fileIoSummary(jfrStatsResult.fileWrites()));
        jsonObject.add("read", this.fileIoSummary(jfrStatsResult.fileReads()));
        jsonObject.add("chunksRead", this.ioSummary(jfrStatsResult.readChunks(), JfrResultJsonSerializer::serializeChunkId));
        jsonObject.add("chunksWritten", this.ioSummary(jfrStatsResult.writtenChunks(), JfrResultJsonSerializer::serializeChunkId));
        return jsonObject;
    }

    private JsonElement fileIoSummary(FileIOStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, (Number)summary.totalBytes());
        jsonObject.addProperty(COUNT, (Number)summary.counts());
        jsonObject.addProperty(BYTES_PER_SECOND, (Number)summary.bytesPerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, (Number)summary.countsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", (JsonElement)jsonArray);
        summary.topTenContributorsByTotalBytes().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add((JsonElement)jsonObject);
            jsonObject.addProperty("path", (String)pair.getFirst());
            jsonObject.addProperty(TOTAL_BYTES, (Number)pair.getSecond());
        });
        return jsonObject;
    }

    private JsonElement network(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("sent", this.ioSummary(jfrStatsResult.sentPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        jsonObject.add("received", this.ioSummary(jfrStatsResult.receivedPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        return jsonObject;
    }

    private <T> JsonElement ioSummary(IoSummary<T> ioSummary, BiConsumer<T, JsonObject> biConsumer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, (Number)ioSummary.getTotalSize());
        jsonObject.addProperty(COUNT, (Number)ioSummary.getTotalCount());
        jsonObject.addProperty(BYTES_PER_SECOND, (Number)ioSummary.getSizePerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, (Number)ioSummary.getCountsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", (JsonElement)jsonArray);
        ioSummary.largestSizeContributors().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add((JsonElement)jsonObject);
            Object object = pair.getFirst();
            IoSummary.CountAndSize countAndSize = (IoSummary.CountAndSize)((Object)((Object)pair.getSecond()));
            biConsumer.accept(object, jsonObject);
            jsonObject.addProperty(TOTAL_BYTES, (Number)countAndSize.totalSize());
            jsonObject.addProperty(COUNT, (Number)countAndSize.totalCount());
            jsonObject.addProperty("averageSize", (Number)Float.valueOf(countAndSize.averageSize()));
        });
        return jsonObject;
    }

    private JsonElement cpu(List<CpuLoadStat> list2) {
        JsonObject jsonObject = new JsonObject();
        BiFunction<List, ToDoubleFunction, JsonObject> biFunction = (list, toDoubleFunction) -> {
            JsonObject jsonObject = new JsonObject();
            DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(toDoubleFunction).summaryStatistics();
            jsonObject.addProperty("min", (Number)doubleSummaryStatistics.getMin());
            jsonObject.addProperty("average", (Number)doubleSummaryStatistics.getAverage());
            jsonObject.addProperty("max", (Number)doubleSummaryStatistics.getMax());
            return jsonObject;
        };
        jsonObject.add("jvm", (JsonElement)biFunction.apply(list2, CpuLoadStat::jvm));
        jsonObject.add("userJvm", (JsonElement)biFunction.apply(list2, CpuLoadStat::userJvm));
        jsonObject.add("system", (JsonElement)biFunction.apply(list2, CpuLoadStat::system));
        return jsonObject;
    }
}

