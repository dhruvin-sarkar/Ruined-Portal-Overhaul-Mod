/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFixer
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.LegacyTagFixer;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIndexSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jspecify.annotations.Nullable;

public class LegacyStructureDataHandler
implements LegacyTagFixer {
    public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
    private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Village", "Village");
        hashMap.put("Mineshaft", "Mineshaft");
        hashMap.put("Mansion", "Mansion");
        hashMap.put("Igloo", "Temple");
        hashMap.put("Desert_Pyramid", "Temple");
        hashMap.put("Jungle_Pyramid", "Temple");
        hashMap.put("Swamp_Hut", "Temple");
        hashMap.put("Stronghold", "Stronghold");
        hashMap.put("Monument", "Monument");
        hashMap.put("Fortress", "Fortress");
        hashMap.put("EndCity", "EndCity");
    });
    private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Iglu", "Igloo");
        hashMap.put("TeDP", "Desert_Pyramid");
        hashMap.put("TeJP", "Jungle_Pyramid");
        hashMap.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of((Object[])new String[]{"pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant"});
    private final boolean hasLegacyData;
    private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
    private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
    private final @Nullable DimensionDataStorage dimensionDataStorage;
    private final List<String> legacyKeys;
    private final List<String> currentKeys;
    private final DataFixer dataFixer;
    private boolean cachesInitialized;

    public LegacyStructureDataHandler(@Nullable DimensionDataStorage dimensionDataStorage, List<String> list, List<String> list2, DataFixer dataFixer) {
        this.dimensionDataStorage = dimensionDataStorage;
        this.legacyKeys = list;
        this.currentKeys = list2;
        this.dataFixer = dataFixer;
        boolean bl = false;
        for (String string : this.currentKeys) {
            bl |= this.dataMap.get(string) != null;
        }
        this.hasLegacyData = bl;
    }

    @Override
    public void markChunkDone(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        for (String string : this.legacyKeys) {
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = this.indexMap.get(string);
            if (structureFeatureIndexSavedData == null || !structureFeatureIndexSavedData.hasUnhandledIndex(l)) continue;
            structureFeatureIndexSavedData.removeIndex(l);
        }
    }

    @Override
    public int targetDataVersion() {
        return 1493;
    }

    @Override
    public CompoundTag applyFix(CompoundTag compoundTag2) {
        int i;
        if (!this.cachesInitialized && this.dimensionDataStorage != null) {
            this.populateCaches(this.dimensionDataStorage);
        }
        if ((i = NbtUtils.getDataVersion(compoundTag2)) < 1493 && (compoundTag2 = DataFixTypes.CHUNK.update(this.dataFixer, compoundTag2, i, 1493)).getCompound("Level").flatMap(compoundTag -> compoundTag.getBoolean("hasLegacyStructureData")).orElse(false).booleanValue()) {
            compoundTag2 = this.updateFromLegacy(compoundTag2);
        }
        return compoundTag2;
    }

    private CompoundTag updateFromLegacy(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("Level");
        ChunkPos chunkPos = new ChunkPos(compoundTag2.getIntOr("xPos", 0), compoundTag2.getIntOr("zPos", 0));
        if (this.isUnhandledStructureStart(chunkPos.x, chunkPos.z)) {
            compoundTag = this.updateStructureStart(compoundTag, chunkPos);
        }
        CompoundTag compoundTag3 = compoundTag2.getCompoundOrEmpty("Structures");
        CompoundTag compoundTag4 = compoundTag3.getCompoundOrEmpty("References");
        for (String string : this.currentKeys) {
            boolean bl = OLD_STRUCTURE_REGISTRY_KEYS.contains(string.toLowerCase(Locale.ROOT));
            if (compoundTag4.getLongArray(string).isPresent() || !bl) continue;
            int i = 8;
            LongArrayList longList = new LongArrayList();
            for (int j = chunkPos.x - 8; j <= chunkPos.x + 8; ++j) {
                for (int k = chunkPos.z - 8; k <= chunkPos.z + 8; ++k) {
                    if (!this.hasLegacyStart(j, k, string)) continue;
                    longList.add(ChunkPos.asLong(j, k));
                }
            }
            compoundTag4.putLongArray(string, longList.toLongArray());
        }
        compoundTag3.put("References", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private boolean hasLegacyStart(int i, int j, String string) {
        if (!this.hasLegacyData) {
            return false;
        }
        return this.dataMap.get(string) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasStartIndex(ChunkPos.asLong(i, j));
    }

    private boolean isUnhandledStructureStart(int i, int j) {
        if (!this.hasLegacyData) {
            return false;
        }
        for (String string : this.currentKeys) {
            if (this.dataMap.get(string) == null || !this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(ChunkPos.asLong(i, j))) continue;
            return true;
        }
        return false;
    }

    private CompoundTag updateStructureStart(CompoundTag compoundTag, ChunkPos chunkPos) {
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("Level");
        CompoundTag compoundTag3 = compoundTag2.getCompoundOrEmpty("Structures");
        CompoundTag compoundTag4 = compoundTag3.getCompoundOrEmpty("Starts");
        for (String string : this.currentKeys) {
            CompoundTag compoundTag5;
            Long2ObjectMap<CompoundTag> long2ObjectMap = this.dataMap.get(string);
            if (long2ObjectMap == null) continue;
            long l = chunkPos.toLong();
            if (!this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(string)).hasUnhandledIndex(l) || (compoundTag5 = (CompoundTag)long2ObjectMap.get(l)) == null) continue;
            compoundTag4.put(string, compoundTag5);
        }
        compoundTag3.put("Starts", compoundTag4);
        compoundTag2.put("Structures", compoundTag3);
        compoundTag.put("Level", compoundTag2);
        return compoundTag;
    }

    private synchronized void populateCaches(DimensionDataStorage dimensionDataStorage) {
        if (this.cachesInitialized) {
            return;
        }
        for (String string2 : this.legacyKeys) {
            CompoundTag compoundTag = new CompoundTag();
            try {
                compoundTag = dimensionDataStorage.readTagFromDisk(string2, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493).getCompoundOrEmpty("data").getCompoundOrEmpty("Features");
                if (compoundTag.isEmpty()) {
                    continue;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            compoundTag.forEach((string3, tag) -> {
                if (!(tag instanceof CompoundTag)) {
                    return;
                }
                CompoundTag compoundTag2 = (CompoundTag)tag;
                long l = ChunkPos.asLong(compoundTag2.getIntOr("ChunkX", 0), compoundTag2.getIntOr("ChunkZ", 0));
                ListTag listTag = compoundTag2.getListOrEmpty("Children");
                if (!listTag.isEmpty()) {
                    Optional<String> optional = listTag.getCompound(0).flatMap(compoundTag -> compoundTag.getString("id"));
                    optional.map(LEGACY_TO_CURRENT_MAP::get).ifPresent(string -> compoundTag2.putString("id", (String)string));
                }
                compoundTag2.getString("id").ifPresent(string2 -> this.dataMap.computeIfAbsent((String)string2, string -> new Long2ObjectOpenHashMap()).put(l, (Object)compoundTag2));
            });
            String string22 = string2 + "_index";
            StructureFeatureIndexSavedData structureFeatureIndexSavedData = dimensionDataStorage.computeIfAbsent(StructureFeatureIndexSavedData.type(string22));
            if (structureFeatureIndexSavedData.getAll().isEmpty()) {
                StructureFeatureIndexSavedData structureFeatureIndexSavedData2 = new StructureFeatureIndexSavedData();
                this.indexMap.put(string2, structureFeatureIndexSavedData2);
                compoundTag.forEach((string, tag) -> {
                    if (tag instanceof CompoundTag) {
                        CompoundTag compoundTag = (CompoundTag)tag;
                        structureFeatureIndexSavedData2.addIndex(ChunkPos.asLong(compoundTag.getIntOr("ChunkX", 0), compoundTag.getIntOr("ChunkZ", 0)));
                    }
                });
                continue;
            }
            this.indexMap.put(string2, structureFeatureIndexSavedData);
        }
        this.cachesInitialized = true;
    }

    public static Supplier<LegacyTagFixer> getLegacyTagFixer(ResourceKey<Level> resourceKey, Supplier<@Nullable DimensionDataStorage> supplier, DataFixer dataFixer) {
        if (resourceKey == Level.OVERWORLD) {
            return () -> new LegacyStructureDataHandler((DimensionDataStorage)supplier.get(), (List<String>)ImmutableList.of((Object)"Monument", (Object)"Stronghold", (Object)"Village", (Object)"Mineshaft", (Object)"Temple", (Object)"Mansion"), (List<String>)ImmutableList.of((Object)"Village", (Object)"Mineshaft", (Object)"Mansion", (Object)"Igloo", (Object)"Desert_Pyramid", (Object)"Jungle_Pyramid", (Object)"Swamp_Hut", (Object)"Stronghold", (Object)"Monument"), dataFixer);
        }
        if (resourceKey == Level.NETHER) {
            ImmutableList list = ImmutableList.of((Object)"Fortress");
            return () -> LegacyStructureDataHandler.method_76204(supplier, (List)list, dataFixer);
        }
        if (resourceKey == Level.END) {
            ImmutableList list = ImmutableList.of((Object)"EndCity");
            return () -> LegacyStructureDataHandler.method_76203(supplier, (List)list, dataFixer);
        }
        return LegacyTagFixer.EMPTY;
    }

    private static /* synthetic */ LegacyTagFixer method_76203(Supplier supplier, List list, DataFixer dataFixer) {
        return new LegacyStructureDataHandler((DimensionDataStorage)supplier.get(), list, list, dataFixer);
    }

    private static /* synthetic */ LegacyTagFixer method_76204(Supplier supplier, List list, DataFixer dataFixer) {
        return new LegacyStructureDataHandler((DimensionDataStorage)supplier.get(), list, list, dataFixer);
    }
}

