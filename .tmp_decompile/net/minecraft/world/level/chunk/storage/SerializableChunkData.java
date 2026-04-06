/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record SerializableChunkData(PalettedContainerFactory containerFactory, ChunkPos chunkPos, int minSectionY, long lastUpdateTime, long inhabitedTime, ChunkStatus chunkStatus, @Nullable BlendingData.Packed blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData, long @Nullable [] carvingMask, Map<Heightmap.Types, long[]> heightmaps, ChunkAccess.PackedTicks packedTicks, @Nullable ShortList[] postProcessingSections, boolean lightCorrect, List<SectionData> sectionData, List<CompoundTag> entities, List<CompoundTag> blockEntities, CompoundTag structureData) {
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    public static @Nullable SerializableChunkData parse(LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory palettedContainerFactory, CompoundTag compoundTag2) {
        if (compoundTag2.getString("Status").isEmpty()) {
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(compoundTag2.getIntOr(X_POS_TAG, 0), compoundTag2.getIntOr(Z_POS_TAG, 0));
        long l = compoundTag2.getLongOr("LastUpdate", 0L);
        long m = compoundTag2.getLongOr("InhabitedTime", 0L);
        ChunkStatus chunkStatus = compoundTag2.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
        UpgradeData upgradeData = compoundTag2.getCompound(TAG_UPGRADE_DATA).map(compoundTag -> new UpgradeData((CompoundTag)compoundTag, levelHeightAccessor)).orElse(UpgradeData.EMPTY);
        boolean bl = compoundTag2.getBooleanOr(IS_LIGHT_ON_TAG, false);
        BlendingData.Packed packed = compoundTag2.read("blending_data", BlendingData.Packed.CODEC).orElse(null);
        BelowZeroRetrogen belowZeroRetrogen = compoundTag2.read("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
        long[] ls = compoundTag2.getLongArray("carving_mask").orElse(null);
        EnumMap<Heightmap.Types, long[]> map = new EnumMap<Heightmap.Types, long[]>(Heightmap.Types.class);
        compoundTag2.getCompound(HEIGHTMAPS_TAG).ifPresent(compoundTag -> {
            for (Heightmap.Types types : chunkStatus.heightmapsAfter()) {
                compoundTag.getLongArray(types.getSerializationKey()).ifPresent(ls -> map.put(types, (long[])ls));
            }
        });
        List<SavedTick<Block>> list = SavedTick.filterTickListForChunk(compoundTag2.read(BLOCK_TICKS_TAG, BLOCK_TICKS_CODEC).orElse(List.of()), chunkPos);
        List<SavedTick<Fluid>> list2 = SavedTick.filterTickListForChunk(compoundTag2.read(FLUID_TICKS_TAG, FLUID_TICKS_CODEC).orElse(List.of()), chunkPos);
        ChunkAccess.PackedTicks packedTicks = new ChunkAccess.PackedTicks(list, list2);
        ListTag listTag = compoundTag2.getListOrEmpty("PostProcessing");
        @Nullable ShortList[] shortLists = new ShortList[listTag.size()];
        for (int i = 0; i < listTag.size(); ++i) {
            ListTag listTag2 = listTag.getList(i).orElse(null);
            if (listTag2 == null || listTag2.isEmpty()) continue;
            ShortArrayList shortList = new ShortArrayList(listTag2.size());
            for (int j = 0; j < listTag2.size(); ++j) {
                shortList.add(listTag2.getShortOr(j, (short)0));
            }
            shortLists[i] = shortList;
        }
        List list3 = compoundTag2.getList("entities").stream().flatMap(ListTag::compoundStream).toList();
        List list4 = compoundTag2.getList("block_entities").stream().flatMap(ListTag::compoundStream).toList();
        CompoundTag compoundTag22 = compoundTag2.getCompoundOrEmpty("structures");
        ListTag listTag3 = compoundTag2.getListOrEmpty(SECTIONS_TAG);
        ArrayList<SectionData> list5 = new ArrayList<SectionData>(listTag3.size());
        Codec<PalettedContainerRO<Holder<Biome>>> codec = palettedContainerFactory.biomeContainerCodec();
        Codec<PalettedContainer<BlockState>> codec2 = palettedContainerFactory.blockStatesContainerCodec();
        for (int k = 0; k < listTag3.size(); ++k) {
            LevelChunkSection levelChunkSection;
            Optional<CompoundTag> optional = listTag3.getCompound(k);
            if (optional.isEmpty()) continue;
            CompoundTag compoundTag3 = optional.get();
            byte n = compoundTag3.getByteOr("Y", (byte)0);
            if (n >= levelHeightAccessor.getMinSectionY() && n <= levelHeightAccessor.getMaxSectionY()) {
                PalettedContainer palettedContainer = compoundTag3.getCompound("block_states").map(compoundTag -> (PalettedContainer)codec2.parse((DynamicOps)NbtOps.INSTANCE, compoundTag).promotePartial(string -> SerializableChunkData.logErrors(chunkPos, n, string)).getOrThrow(ChunkReadException::new)).orElseGet(palettedContainerFactory::createForBlockStates);
                PalettedContainerRO palettedContainerRO = compoundTag3.getCompound("biomes").map(compoundTag -> (PalettedContainerRO)codec.parse((DynamicOps)NbtOps.INSTANCE, compoundTag).promotePartial(string -> SerializableChunkData.logErrors(chunkPos, n, string)).getOrThrow(ChunkReadException::new)).orElseGet(palettedContainerFactory::createForBiomes);
                levelChunkSection = new LevelChunkSection(palettedContainer, palettedContainerRO);
            } else {
                levelChunkSection = null;
            }
            DataLayer dataLayer = compoundTag3.getByteArray(BLOCK_LIGHT_TAG).map(DataLayer::new).orElse(null);
            DataLayer dataLayer2 = compoundTag3.getByteArray(SKY_LIGHT_TAG).map(DataLayer::new).orElse(null);
            list5.add(new SectionData(n, levelChunkSection, dataLayer, dataLayer2));
        }
        return new SerializableChunkData(palettedContainerFactory, chunkPos, levelHeightAccessor.getMinSectionY(), l, m, chunkStatus, packed, belowZeroRetrogen, upgradeData, ls, map, packedTicks, shortLists, bl, list5, list3, list4, compoundTag22);
    }

    public ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        ChunkAccess chunkAccess;
        if (!Objects.equals(chunkPos, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkPos, chunkPos, this.chunkPos});
            serverLevel.getServer().reportMisplacedChunk(this.chunkPos, chunkPos, regionStorageInfo);
        }
        int i = serverLevel.getSectionsCount();
        LevelChunkSection[] levelChunkSections = new LevelChunkSection[i];
        boolean bl = serverLevel.dimensionType().hasSkyLight();
        ServerChunkCache chunkSource = serverLevel.getChunkSource();
        LevelLightEngine levelLightEngine = ((ChunkSource)chunkSource).getLightEngine();
        PalettedContainerFactory palettedContainerFactory = serverLevel.palettedContainerFactory();
        boolean bl2 = false;
        for (SectionData sectionData : this.sectionData) {
            boolean bl4;
            SectionPos sectionPos = SectionPos.of(chunkPos, sectionData.y);
            if (sectionData.chunkSection != null) {
                levelChunkSections[serverLevel.getSectionIndexFromSectionY((int)sectionData.y)] = sectionData.chunkSection;
                poiManager.checkConsistencyWithBlocks(sectionPos, sectionData.chunkSection);
            }
            boolean bl3 = sectionData.blockLight != null;
            boolean bl5 = bl4 = bl && sectionData.skyLight != null;
            if (!bl3 && !bl4) continue;
            if (!bl2) {
                levelLightEngine.retainData(chunkPos, true);
                bl2 = true;
            }
            if (bl3) {
                levelLightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, sectionData.blockLight);
            }
            if (!bl4) continue;
            levelLightEngine.queueSectionData(LightLayer.SKY, sectionPos, sectionData.skyLight);
        }
        ChunkType chunkType = this.chunkStatus.getChunkType();
        if (chunkType == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelChunkTicks = new LevelChunkTicks<Block>(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> levelChunkTicks2 = new LevelChunkTicks<Fluid>(this.packedTicks.fluids());
            chunkAccess = new LevelChunk(serverLevel.getLevel(), chunkPos, this.upgradeData, levelChunkTicks, levelChunkTicks2, this.inhabitedTime, levelChunkSections, SerializableChunkData.postLoadChunk(serverLevel, this.entities, this.blockEntities), BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunkTicks<Block> protoChunkTicks = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> protoChunkTicks2 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protoChunk = new ProtoChunk(chunkPos, this.upgradeData, levelChunkSections, protoChunkTicks, protoChunkTicks2, serverLevel, palettedContainerFactory, BlendingData.unpack(this.blendingData));
            chunkAccess = protoChunk;
            chunkAccess.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protoChunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }
            protoChunk.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protoChunk.setLightEngine(levelLightEngine);
            }
        }
        chunkAccess.setLightCorrect(this.lightCorrect);
        EnumSet<Heightmap.Types> enumSet = EnumSet.noneOf(Heightmap.Types.class);
        for (Heightmap.Types types : chunkAccess.getPersistedStatus().heightmapsAfter()) {
            long[] ls = this.heightmaps.get(types);
            if (ls != null) {
                chunkAccess.setHeightmap(types, ls);
                continue;
            }
            enumSet.add(types);
        }
        Heightmap.primeHeightmaps(chunkAccess, enumSet);
        chunkAccess.setAllStarts(SerializableChunkData.unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), this.structureData, serverLevel.getSeed()));
        chunkAccess.setAllReferences(SerializableChunkData.unpackStructureReferences(serverLevel.registryAccess(), chunkPos, this.structureData));
        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            ShortList shortList = this.postProcessingSections[j];
            if (shortList == null) continue;
            chunkAccess.addPackedPostProcess(shortList, j);
        }
        if (chunkType == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)chunkAccess, false);
        }
        ProtoChunk protoChunk2 = (ProtoChunk)chunkAccess;
        for (CompoundTag compoundTag : this.entities) {
            protoChunk2.addEntity(compoundTag);
        }
        for (CompoundTag compoundTag : this.blockEntities) {
            protoChunk2.setBlockEntityNbt(compoundTag);
        }
        if (this.carvingMask != null) {
            protoChunk2.setCarvingMask(new CarvingMask(this.carvingMask, chunkAccess.getMinY()));
        }
        return protoChunk2;
    }

    private static void logErrors(ChunkPos chunkPos, int i, String string) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkPos.x, i, chunkPos.z, string});
    }

    public static SerializableChunkData copyOf(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        if (!chunkAccess.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + String.valueOf(chunkAccess));
        }
        ChunkPos chunkPos = chunkAccess.getPos();
        ArrayList<SectionData> list = new ArrayList<SectionData>();
        LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
        ThreadedLevelLightEngine levelLightEngine = serverLevel.getChunkSource().getLightEngine();
        for (int i = levelLightEngine.getMinLightSection(); i < levelLightEngine.getMaxLightSection(); ++i) {
            DataLayer dataLayer4;
            int j = chunkAccess.getSectionIndexFromSectionY(i);
            boolean bl = j >= 0 && j < levelChunkSections.length;
            DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
            DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
            DataLayer dataLayer3 = dataLayer != null && !dataLayer.isEmpty() ? dataLayer.copy() : null;
            DataLayer dataLayer5 = dataLayer4 = dataLayer2 != null && !dataLayer2.isEmpty() ? dataLayer2.copy() : null;
            if (!bl && dataLayer3 == null && dataLayer4 == null) continue;
            LevelChunkSection levelChunkSection = bl ? levelChunkSections[j].copy() : null;
            list.add(new SectionData(i, levelChunkSection, dataLayer3, dataLayer4));
        }
        ArrayList<CompoundTag> list2 = new ArrayList<CompoundTag>(chunkAccess.getBlockEntitiesPos().size());
        for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
            CompoundTag compoundTag = chunkAccess.getBlockEntityNbtForSaving(blockPos, serverLevel.registryAccess());
            if (compoundTag == null) continue;
            list2.add(compoundTag);
        }
        ArrayList<CompoundTag> list3 = new ArrayList<CompoundTag>();
        long[] ls = null;
        if (chunkAccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            list3.addAll(protoChunk.getEntities());
            CarvingMask carvingMask = protoChunk.getCarvingMask();
            if (carvingMask != null) {
                ls = carvingMask.toArray();
            }
        }
        EnumMap<Heightmap.Types, long[]> map = new EnumMap<Heightmap.Types, long[]>(Heightmap.Types.class);
        for (Map.Entry entry : chunkAccess.getHeightmaps()) {
            if (!chunkAccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) continue;
            long[] ms = ((Heightmap)entry.getValue()).getRawData();
            map.put((Heightmap.Types)entry.getKey(), (long[])ms.clone());
        }
        ChunkAccess.PackedTicks packedTicks = chunkAccess.getTicksForSerialization(serverLevel.getGameTime());
        @Nullable ShortList[] shortListArray = (ShortList[])Arrays.stream(chunkAccess.getPostProcessing()).map(shortList -> shortList != null && !shortList.isEmpty() ? new ShortArrayList(shortList) : null).toArray(ShortList[]::new);
        CompoundTag compoundTag2 = SerializableChunkData.packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences());
        return new SerializableChunkData(serverLevel.palettedContainerFactory(), chunkPos, chunkAccess.getMinSectionY(), serverLevel.getGameTime(), chunkAccess.getInhabitedTime(), chunkAccess.getPersistedStatus(), Optionull.map(chunkAccess.getBlendingData(), BlendingData::pack), chunkAccess.getBelowZeroRetrogen(), chunkAccess.getUpgradeData().copy(), ls, map, packedTicks, shortListArray, chunkAccess.isLightCorrect(), list, list3, list2, compoundTag2);
    }

    public CompoundTag write() {
        CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundTag.putInt(X_POS_TAG, this.chunkPos.x);
        compoundTag.putInt("yPos", this.minSectionY);
        compoundTag.putInt(Z_POS_TAG, this.chunkPos.z);
        compoundTag.putLong("LastUpdate", this.lastUpdateTime);
        compoundTag.putLong("InhabitedTime", this.inhabitedTime);
        compoundTag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        compoundTag.storeNullable("blending_data", BlendingData.Packed.CODEC, this.blendingData);
        compoundTag.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isEmpty()) {
            compoundTag.put(TAG_UPGRADE_DATA, this.upgradeData.write());
        }
        ListTag listTag = new ListTag();
        Codec<PalettedContainer<BlockState>> codec = this.containerFactory.blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> codec2 = this.containerFactory.biomeContainerCodec();
        for (SectionData sectionData : this.sectionData) {
            CompoundTag compoundTag2 = new CompoundTag();
            LevelChunkSection levelChunkSection = sectionData.chunkSection;
            if (levelChunkSection != null) {
                compoundTag2.store("block_states", codec, levelChunkSection.getStates());
                compoundTag2.store("biomes", codec2, levelChunkSection.getBiomes());
            }
            if (sectionData.blockLight != null) {
                compoundTag2.putByteArray(BLOCK_LIGHT_TAG, sectionData.blockLight.getData());
            }
            if (sectionData.skyLight != null) {
                compoundTag2.putByteArray(SKY_LIGHT_TAG, sectionData.skyLight.getData());
            }
            if (compoundTag2.isEmpty()) continue;
            compoundTag2.putByte("Y", (byte)sectionData.y);
            listTag.add(compoundTag2);
        }
        compoundTag.put(SECTIONS_TAG, listTag);
        if (this.lightCorrect) {
            compoundTag.putBoolean(IS_LIGHT_ON_TAG, true);
        }
        ListTag listTag2 = new ListTag();
        listTag2.addAll(this.blockEntities);
        compoundTag.put("block_entities", listTag2);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag listTag3 = new ListTag();
            listTag3.addAll(this.entities);
            compoundTag.put("entities", listTag3);
            if (this.carvingMask != null) {
                compoundTag.putLongArray("carving_mask", this.carvingMask);
            }
        }
        SerializableChunkData.saveTicks(compoundTag, this.packedTicks);
        compoundTag.put("PostProcessing", SerializableChunkData.packOffsets(this.postProcessingSections));
        CompoundTag compoundTag3 = new CompoundTag();
        this.heightmaps.forEach((types, ls) -> compoundTag3.put(types.getSerializationKey(), new LongArrayTag((long[])ls)));
        compoundTag.put(HEIGHTMAPS_TAG, compoundTag3);
        compoundTag.put("structures", this.structureData);
        return compoundTag;
    }

    private static void saveTicks(CompoundTag compoundTag, ChunkAccess.PackedTicks packedTicks) {
        compoundTag.store(BLOCK_TICKS_TAG, BLOCK_TICKS_CODEC, packedTicks.blocks());
        compoundTag.store(FLUID_TICKS_TAG, FLUID_TICKS_CODEC, packedTicks.fluids());
    }

    public static ChunkStatus getChunkStatusFromTag(@Nullable CompoundTag compoundTag) {
        return compoundTag != null ? compoundTag.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    private static @Nullable LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, List<CompoundTag> list, List<CompoundTag> list2) {
        if (list.isEmpty() && list2.isEmpty()) {
            return null;
        }
        return levelChunk -> {
            if (!list.isEmpty()) {
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(levelChunk.problemPath(), LOGGER);){
                    serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)serverLevel.registryAccess(), list), serverLevel, EntitySpawnReason.LOAD));
                }
            }
            for (CompoundTag compoundTag : list2) {
                boolean bl = compoundTag.getBooleanOr("keepPacked", false);
                if (bl) {
                    levelChunk.setBlockEntityNbt(compoundTag);
                    continue;
                }
                BlockPos blockPos = BlockEntity.getPosFromTag(levelChunk.getPos(), compoundTag);
                BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag, serverLevel.registryAccess());
                if (blockEntity == null) continue;
                levelChunk.setBlockEntity(blockEntity);
            }
        };
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        HolderLookup.RegistryLookup registry = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : map.entrySet()) {
            Identifier identifier = registry.getKey(entry.getKey());
            compoundTag2.put(identifier.toString(), entry.getValue().createTag(structurePieceSerializationContext, chunkPos));
        }
        compoundTag.put("starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<Structure, LongSet> entry2 : map2.entrySet()) {
            if (entry2.getValue().isEmpty()) continue;
            Identifier identifier2 = registry.getKey(entry2.getKey());
            compoundTag3.putLongArray(identifier2.toString(), entry2.getValue().toLongArray());
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        HashMap map = Maps.newHashMap();
        HolderLookup.RegistryLookup registry = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("starts");
        for (String string : compoundTag2.keySet()) {
            Identifier identifier = Identifier.tryParse(string);
            Structure structure = (Structure)registry.getValue(identifier);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", (Object)identifier);
                continue;
            }
            StructureStart structureStart = StructureStart.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompoundOrEmpty(string), l);
            if (structureStart == null) continue;
            map.put(structure, structureStart);
        }
        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap map = Maps.newHashMap();
        HolderLookup.RegistryLookup registry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("References");
        compoundTag2.forEach((arg_0, arg_1) -> SerializableChunkData.method_68295((Registry)registry, chunkPos, map, arg_0, arg_1));
        return map;
    }

    private static ListTag packOffsets(@Nullable ShortList[] shortLists) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortLists) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                for (int i = 0; i < shortList.size(); ++i) {
                    listTag2.add(ShortTag.valueOf(shortList.getShort(i)));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }

    private static /* synthetic */ void method_68295(Registry registry, ChunkPos chunkPos, Map map, String string, Tag tag) {
        Identifier identifier = Identifier.tryParse(string);
        Structure structure = (Structure)registry.getValue(identifier);
        if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)identifier, (Object)chunkPos);
            return;
        }
        Optional<long[]> optional = tag.asLongArray();
        if (optional.isEmpty()) {
            return;
        }
        map.put(structure, new LongOpenHashSet(Arrays.stream(optional.get()).filter(l -> {
            ChunkPos chunkPos2 = new ChunkPos(l);
            if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{identifier, chunkPos2, chunkPos});
                return false;
            }
            return true;
        }).toArray()));
    }

    public static final class SectionData
    extends Record {
        final int y;
        final @Nullable LevelChunkSection chunkSection;
        final @Nullable DataLayer blockLight;
        final @Nullable DataLayer skyLight;

        public SectionData(int i, @Nullable LevelChunkSection levelChunkSection, @Nullable DataLayer dataLayer, @Nullable DataLayer dataLayer2) {
            this.y = i;
            this.chunkSection = levelChunkSection;
            this.blockLight = dataLayer;
            this.skyLight = dataLayer2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this, object);
        }

        public int y() {
            return this.y;
        }

        public @Nullable LevelChunkSection chunkSection() {
            return this.chunkSection;
        }

        public @Nullable DataLayer blockLight() {
            return this.blockLight;
        }

        public @Nullable DataLayer skyLight() {
            return this.skyLight;
        }
    }

    public static class ChunkReadException
    extends NbtException {
        public ChunkReadException(String string) {
            super(string);
        }
    }
}

