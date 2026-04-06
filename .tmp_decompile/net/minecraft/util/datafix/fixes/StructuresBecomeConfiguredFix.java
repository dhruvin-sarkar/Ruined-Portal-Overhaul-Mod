/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Conversion> CONVERSION_MAP = ImmutableMap.builder().put((Object)"mineshaft", (Object)Conversion.biomeMapped(Map.of((Object)List.of((Object)"minecraft:badlands", (Object)"minecraft:eroded_badlands", (Object)"minecraft:wooded_badlands"), (Object)"minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put((Object)"shipwreck", (Object)Conversion.biomeMapped(Map.of((Object)List.of((Object)"minecraft:beach", (Object)"minecraft:snowy_beach"), (Object)"minecraft:shipwreck_beached"), "minecraft:shipwreck")).put((Object)"ocean_ruin", (Object)Conversion.biomeMapped(Map.of((Object)List.of((Object)"minecraft:warm_ocean", (Object)"minecraft:lukewarm_ocean", (Object)"minecraft:deep_lukewarm_ocean"), (Object)"minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put((Object)"village", (Object)Conversion.biomeMapped(Map.of((Object)List.of((Object)"minecraft:desert"), (Object)"minecraft:village_desert", (Object)List.of((Object)"minecraft:savanna"), (Object)"minecraft:village_savanna", (Object)List.of((Object)"minecraft:snowy_plains"), (Object)"minecraft:village_snowy", (Object)List.of((Object)"minecraft:taiga"), (Object)"minecraft:village_taiga"), "minecraft:village_plains")).put((Object)"ruined_portal", (Object)Conversion.biomeMapped(Map.of((Object)List.of((Object)"minecraft:desert"), (Object)"minecraft:ruined_portal_desert", (Object)List.of((Object[])new String[]{"minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"}), (Object)"minecraft:ruined_portal_mountain", (Object)List.of((Object)"minecraft:bamboo_jungle", (Object)"minecraft:jungle", (Object)"minecraft:sparse_jungle"), (Object)"minecraft:ruined_portal_jungle", (Object)List.of((Object)"minecraft:deep_frozen_ocean", (Object)"minecraft:deep_cold_ocean", (Object)"minecraft:deep_ocean", (Object)"minecraft:deep_lukewarm_ocean", (Object)"minecraft:frozen_ocean", (Object)"minecraft:ocean", (Object)"minecraft:cold_ocean", (Object)"minecraft:lukewarm_ocean", (Object)"minecraft:warm_ocean"), (Object)"minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put((Object)"pillager_outpost", (Object)Conversion.trivial("minecraft:pillager_outpost")).put((Object)"mansion", (Object)Conversion.trivial("minecraft:mansion")).put((Object)"jungle_pyramid", (Object)Conversion.trivial("minecraft:jungle_pyramid")).put((Object)"desert_pyramid", (Object)Conversion.trivial("minecraft:desert_pyramid")).put((Object)"igloo", (Object)Conversion.trivial("minecraft:igloo")).put((Object)"swamp_hut", (Object)Conversion.trivial("minecraft:swamp_hut")).put((Object)"stronghold", (Object)Conversion.trivial("minecraft:stronghold")).put((Object)"monument", (Object)Conversion.trivial("minecraft:monument")).put((Object)"fortress", (Object)Conversion.trivial("minecraft:fortress")).put((Object)"endcity", (Object)Conversion.trivial("minecraft:end_city")).put((Object)"buried_treasure", (Object)Conversion.trivial("minecraft:buried_treasure")).put((Object)"nether_fossil", (Object)Conversion.trivial("minecraft:nether_fossil")).put((Object)"bastion_remnant", (Object)Conversion.trivial("minecraft:bastion_remnant")).build();

    public StructuresBecomeConfiguredFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        Type type2 = this.getInputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.update("structures", dynamic22 -> dynamic22.update("starts", dynamic2 -> this.updateStarts((Dynamic<?>)dynamic2, dynamic)).update("References", dynamic2 -> this.updateReferences((Dynamic<?>)dynamic2, dynamic)));
    }

    private Dynamic<?> updateStarts(Dynamic<?> dynamic, Dynamic<?> dynamic22) {
        Map map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic2, dynamic32) -> {
            if (dynamic32.get("id").asString("INVALID").equals("INVALID")) {
                return;
            }
            Dynamic<?> dynamic4 = this.findUpdatedStructureType((Dynamic<?>)dynamic2, dynamic22);
            if (dynamic4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: {}", (Object)dynamic2.asString("<missing key>"));
                return;
            }
            hashMap.computeIfAbsent(dynamic4, dynamic3 -> dynamic32.set("id", dynamic4));
        });
        return dynamic22.createMap((Map)hashMap);
    }

    private Dynamic<?> updateReferences(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Map map = dynamic.getMapValues().result().orElse(Map.of());
        HashMap hashMap = Maps.newHashMap();
        map.forEach((dynamic22, dynamic32) -> {
            if (dynamic32.asLongStream().count() == 0L) {
                return;
            }
            Dynamic<?> dynamic4 = this.findUpdatedStructureType((Dynamic<?>)dynamic22, dynamic2);
            if (dynamic4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: {}", (Object)dynamic22.asString("<missing key>"));
                return;
            }
            hashMap.compute(dynamic4, (dynamic2, dynamic3) -> {
                if (dynamic3 == null) {
                    return dynamic32;
                }
                return dynamic32.createLongList(LongStream.concat(dynamic3.asLongStream(), dynamic32.asLongStream()));
            });
        });
        return dynamic2.createMap((Map)hashMap);
    }

    private @Nullable Dynamic<?> findUpdatedStructureType(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
        Optional<String> optional;
        String string = dynamic.asString("UNKNOWN").toLowerCase(Locale.ROOT);
        Conversion conversion = CONVERSION_MAP.get(string);
        if (conversion == null) {
            return null;
        }
        String string2 = conversion.fallback;
        if (!conversion.biomeMapping().isEmpty() && (optional = this.guessConfiguration(dynamic2, conversion)).isPresent()) {
            string2 = optional.get();
        }
        return dynamic2.createString(string2);
    }

    private Optional<String> guessConfiguration(Dynamic<?> dynamic, Conversion conversion) {
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        dynamic.get("sections").asList(Function.identity()).forEach(dynamic2 -> dynamic2.get("biomes").get("palette").asList(Function.identity()).forEach(dynamic -> {
            String string = conversion.biomeMapping().get(dynamic.asString(""));
            if (string != null) {
                object2IntArrayMap.mergeInt((Object)string, 1, Integer::sum);
            }
        }));
        return object2IntArrayMap.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
    }

    static final class Conversion
    extends Record {
        private final Map<String, String> biomeMapping;
        final String fallback;

        private Conversion(Map<String, String> map, String string) {
            this.biomeMapping = map;
            this.fallback = string;
        }

        public static Conversion trivial(String string) {
            return new Conversion(Map.of(), string);
        }

        public static Conversion biomeMapped(Map<List<String>, String> map, String string) {
            return new Conversion(Conversion.unpack(map), string);
        }

        private static Map<String, String> unpack(Map<List<String>, String> map) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Map.Entry<List<String>, String> entry : map.entrySet()) {
                entry.getKey().forEach(string -> builder.put(string, (Object)((String)entry.getValue())));
            }
            return builder.build();
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this, object);
        }

        public Map<String, String> biomeMapping() {
            return this.biomeMapping;
        }

        public String fallback() {
            return this.fallback;
        }
    }
}

