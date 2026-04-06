/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2BooleanMap
 *  it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructureCheck {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_STRUCTURE = -1;
    private final ChunkScanAccess storageAccess;
    private final RegistryAccess registryAccess;
    private final StructureTemplateManager structureTemplateManager;
    private final ResourceKey<Level> dimension;
    private final ChunkGenerator chunkGenerator;
    private final RandomState randomState;
    private final LevelHeightAccessor heightAccessor;
    private final BiomeSource biomeSource;
    private final long seed;
    private final DataFixer fixerUpper;
    private final Long2ObjectMap<Object2IntMap<Structure>> loadedChunks = new Long2ObjectOpenHashMap();
    private final Map<Structure, Long2BooleanMap> featureChecks = new HashMap<Structure, Long2BooleanMap>();

    public StructureCheck(ChunkScanAccess chunkScanAccess, RegistryAccess registryAccess, StructureTemplateManager structureTemplateManager, ResourceKey<Level> resourceKey, ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor levelHeightAccessor, BiomeSource biomeSource, long l, DataFixer dataFixer) {
        this.storageAccess = chunkScanAccess;
        this.registryAccess = registryAccess;
        this.structureTemplateManager = structureTemplateManager;
        this.dimension = resourceKey;
        this.chunkGenerator = chunkGenerator;
        this.randomState = randomState;
        this.heightAccessor = levelHeightAccessor;
        this.biomeSource = biomeSource;
        this.seed = l;
        this.fixerUpper = dataFixer;
    }

    public StructureCheckResult checkStart(ChunkPos chunkPos, Structure structure2, StructurePlacement structurePlacement, boolean bl) {
        long l2 = chunkPos.toLong();
        Object2IntMap object2IntMap = (Object2IntMap)this.loadedChunks.get(l2);
        if (object2IntMap != null) {
            return this.checkStructureInfo((Object2IntMap<Structure>)object2IntMap, structure2, bl);
        }
        StructureCheckResult structureCheckResult = this.tryLoadFromStorage(chunkPos, structure2, bl, l2);
        if (structureCheckResult != null) {
            return structureCheckResult;
        }
        if (!structurePlacement.applyAdditionalChunkRestrictions(chunkPos.x, chunkPos.z, this.seed)) {
            return StructureCheckResult.START_NOT_PRESENT;
        }
        boolean bl2 = this.featureChecks.computeIfAbsent(structure2, structure -> new Long2BooleanOpenHashMap()).computeIfAbsent(l2, l -> this.canCreateStructure(chunkPos, structure2));
        if (!bl2) {
            return StructureCheckResult.START_NOT_PRESENT;
        }
        return StructureCheckResult.CHUNK_LOAD_NEEDED;
    }

    private boolean canCreateStructure(ChunkPos chunkPos, Structure structure) {
        return structure.findValidGenerationPoint(new Structure.GenerationContext(this.registryAccess, this.chunkGenerator, this.biomeSource, this.randomState, this.structureTemplateManager, this.seed, chunkPos, this.heightAccessor, structure.biomes()::contains)).isPresent();
    }

    private @Nullable StructureCheckResult tryLoadFromStorage(ChunkPos chunkPos, Structure structure, boolean bl, long l) {
        CompoundTag compoundTag2;
        CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"), new FieldSelector("structures", CompoundTag.TYPE, "starts"));
        try {
            this.storageAccess.scanChunk(chunkPos, collectFields).join();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        Tag tag = collectFields.getResult();
        if (!(tag instanceof CompoundTag)) {
            return null;
        }
        CompoundTag compoundTag = (CompoundTag)tag;
        int i = NbtUtils.getDataVersion(compoundTag);
        if (i <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        SimpleRegionStorage.injectDatafixingContext(compoundTag, ChunkMap.getChunkDataFixContextTag(this.dimension, this.chunkGenerator.getTypeNameForDataFixer()));
        try {
            compoundTag2 = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundTag, i);
        }
        catch (Exception exception2) {
            LOGGER.warn("Failed to partially datafix chunk {}", (Object)chunkPos, (Object)exception2);
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        Object2IntMap<Structure> object2IntMap = this.loadStructures(compoundTag2);
        if (object2IntMap == null) {
            return null;
        }
        this.storeFullResults(l, object2IntMap);
        return this.checkStructureInfo(object2IntMap, structure, bl);
    }

    private @Nullable Object2IntMap<Structure> loadStructures(CompoundTag compoundTag2) {
        Optional optional = compoundTag2.getCompound("structures").flatMap(compoundTag -> compoundTag.getCompound("starts"));
        if (optional.isEmpty()) {
            return null;
        }
        CompoundTag compoundTag22 = (CompoundTag)optional.get();
        if (compoundTag22.isEmpty()) {
            return Object2IntMaps.emptyMap();
        }
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        HolderLookup.RegistryLookup registry = this.registryAccess.lookupOrThrow(Registries.STRUCTURE);
        compoundTag22.forEach((arg_0, arg_1) -> StructureCheck.method_68332((Registry)registry, (Object2IntMap)object2IntMap, arg_0, arg_1));
        return object2IntMap;
    }

    private static Object2IntMap<Structure> deduplicateEmptyMap(Object2IntMap<Structure> object2IntMap) {
        return object2IntMap.isEmpty() ? Object2IntMaps.emptyMap() : object2IntMap;
    }

    private StructureCheckResult checkStructureInfo(Object2IntMap<Structure> object2IntMap, Structure structure, boolean bl) {
        int i = object2IntMap.getOrDefault((Object)structure, -1);
        return i != -1 && (!bl || i == 0) ? StructureCheckResult.START_PRESENT : StructureCheckResult.START_NOT_PRESENT;
    }

    public void onStructureLoad(ChunkPos chunkPos, Map<Structure, StructureStart> map) {
        long l = chunkPos.toLong();
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        map.forEach((arg_0, arg_1) -> StructureCheck.method_39839((Object2IntMap)object2IntMap, arg_0, arg_1));
        this.storeFullResults(l, (Object2IntMap<Structure>)object2IntMap);
    }

    private void storeFullResults(long l, Object2IntMap<Structure> object2IntMap) {
        this.loadedChunks.put(l, StructureCheck.deduplicateEmptyMap(object2IntMap));
        this.featureChecks.values().forEach(long2BooleanMap -> long2BooleanMap.remove(l));
    }

    public void incrementReference(ChunkPos chunkPos, Structure structure) {
        this.loadedChunks.compute(chunkPos.toLong(), (long_, object2IntMap) -> {
            if (object2IntMap == null || object2IntMap.isEmpty()) {
                object2IntMap = new Object2IntOpenHashMap();
            }
            object2IntMap.computeInt((Object)structure, (structure, integer) -> integer == null ? 1 : integer + 1);
            return object2IntMap;
        });
    }

    private static /* synthetic */ void method_39839(Object2IntMap object2IntMap, Structure structure, StructureStart structureStart) {
        if (structureStart.isValid()) {
            object2IntMap.put((Object)structure, structureStart.getReferences());
        }
    }

    private static /* synthetic */ void method_68332(Registry registry, Object2IntMap object2IntMap, String string, Tag tag) {
        Identifier identifier = Identifier.tryParse(string);
        if (identifier == null) {
            return;
        }
        Structure structure = (Structure)registry.getValue(identifier);
        if (structure == null) {
            return;
        }
        tag.asCompound().ifPresent(compoundTag -> {
            String string = compoundTag.getStringOr("id", "");
            if (!"INVALID".equals(string)) {
                int i = compoundTag.getIntOr("references", 0);
                object2IntMap.put((Object)structure, i);
            }
        });
    }
}

