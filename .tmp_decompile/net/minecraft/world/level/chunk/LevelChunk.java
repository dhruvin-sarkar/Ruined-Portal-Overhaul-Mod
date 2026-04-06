/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.debug.DebugStructureInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.EuclideanGameEventListenerRegistry;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class LevelChunk
extends ChunkAccess
implements DebugValueSource {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity(){

        @Override
        public void tick() {
        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public BlockPos getPos() {
            return BlockPos.ZERO;
        }

        @Override
        public String getType() {
            return "<null>";
        }
    };
    private final Map<BlockPos, RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
    private boolean loaded;
    final Level level;
    private @Nullable Supplier<FullChunkStatus> fullStatus;
    private @Nullable PostLoadProcessor postLoad;
    private final Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;
    private final LevelChunkTicks<Block> blockTicks;
    private final LevelChunkTicks<Fluid> fluidTicks;
    private UnsavedListener unsavedListener = chunkPos -> {};

    public LevelChunk(Level level, ChunkPos chunkPos) {
        this(level, chunkPos, UpgradeData.EMPTY, new LevelChunkTicks<Block>(), new LevelChunkTicks<Fluid>(), 0L, null, null, null);
    }

    public LevelChunk(Level level, ChunkPos chunkPos2, UpgradeData upgradeData, LevelChunkTicks<Block> levelChunkTicks, LevelChunkTicks<Fluid> levelChunkTicks2, long l, LevelChunkSection @Nullable [] levelChunkSections, @Nullable PostLoadProcessor postLoadProcessor, @Nullable BlendingData blendingData) {
        super(chunkPos2, upgradeData, level, level.palettedContainerFactory(), l, levelChunkSections, blendingData);
        this.level = level;
        this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap();
        for (Heightmap.Types types : Heightmap.Types.values()) {
            if (!ChunkStatus.FULL.heightmapsAfter().contains(types)) continue;
            this.heightmaps.put(types, new Heightmap(this, types));
        }
        this.postLoad = postLoadProcessor;
        this.blockTicks = levelChunkTicks;
        this.fluidTicks = levelChunkTicks2;
    }

    public LevelChunk(ServerLevel serverLevel, ProtoChunk protoChunk, @Nullable PostLoadProcessor postLoadProcessor) {
        this(serverLevel, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.unpackBlockTicks(), protoChunk.unpackFluidTicks(), protoChunk.getInhabitedTime(), protoChunk.getSections(), postLoadProcessor, protoChunk.getBlendingData());
        if (!Collections.disjoint(protoChunk.pendingBlockEntities.keySet(), protoChunk.blockEntities.keySet())) {
            LOGGER.error("Chunk at {} contains duplicated block entities", (Object)protoChunk.getPos());
        }
        for (BlockEntity blockEntity : protoChunk.getBlockEntities().values()) {
            this.setBlockEntity(blockEntity);
        }
        this.pendingBlockEntities.putAll(protoChunk.getBlockEntityNbts());
        for (int i = 0; i < protoChunk.getPostProcessing().length; ++i) {
            this.postProcessing[i] = protoChunk.getPostProcessing()[i];
        }
        this.setAllStarts(protoChunk.getAllStarts());
        this.setAllReferences(protoChunk.getAllReferences());
        for (Map.Entry<Heightmap.Types, Heightmap> entry : protoChunk.getHeightmaps()) {
            if (!ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) continue;
            this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
        }
        this.skyLightSources = protoChunk.skyLightSources;
        this.setLightCorrect(protoChunk.isLightCorrect());
        this.markUnsaved();
    }

    public void setUnsavedListener(UnsavedListener unsavedListener) {
        this.unsavedListener = unsavedListener;
        if (this.isUnsaved()) {
            unsavedListener.setUnsaved(this.chunkPos);
        }
    }

    @Override
    public void markUnsaved() {
        boolean bl = this.isUnsaved();
        super.markUnsaved();
        if (!bl) {
            this.unsavedListener.setUnsaved(this.chunkPos);
        }
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public ChunkAccess.PackedTicks getTicksForSerialization(long l) {
        return new ChunkAccess.PackedTicks(this.blockTicks.pack(l), this.fluidTicks.pack(l));
    }

    @Override
    public GameEventListenerRegistry getListenerRegistry(int i) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return (GameEventListenerRegistry)this.gameEventListenerRegistrySections.computeIfAbsent(i, j -> new EuclideanGameEventListenerRegistry(serverLevel, i, this::removeGameEventListenerRegistry));
        }
        return super.getListenerRegistry(i);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        if (this.level.isDebug()) {
            BlockState blockState = null;
            if (j == 60) {
                blockState = Blocks.BARRIER.defaultBlockState();
            }
            if (j == 70) {
                blockState = DebugLevelSource.getBlockStateFor(i, k);
            }
            return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
        }
        try {
            LevelChunkSection levelChunkSection;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length && !(levelChunkSection = this.sections[l]).hasOnlyAir()) {
                return levelChunkSection.getBlockState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Blocks.AIR.defaultBlockState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting block state");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, i, j, k));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.getFluidState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public FluidState getFluidState(int i, int j, int k) {
        try {
            LevelChunkSection levelChunkSection;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length && !(levelChunkSection = this.sections[l]).hasOnlyAir()) {
                return levelChunkSection.getFluidState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Fluids.EMPTY.defaultFluidState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting fluid state");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
            crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this, i, j, k));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public @Nullable BlockState setBlockState(BlockPos blockPos, BlockState blockState, @Block.UpdateFlags int i) {
        Level level;
        BlockEntity blockEntity;
        boolean bl5;
        int m;
        int l;
        int j = blockPos.getY();
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(j));
        boolean bl = levelChunkSection.hasOnlyAir();
        if (bl && blockState.isAir()) {
            return null;
        }
        int k = blockPos.getX() & 0xF;
        BlockState blockState2 = levelChunkSection.setBlockState(k, l = j & 0xF, m = blockPos.getZ() & 0xF, blockState);
        if (blockState2 == blockState) {
            return null;
        }
        Block block = blockState.getBlock();
        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(k, j, m, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(k, j, m, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(k, j, m, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(k, j, m, blockState);
        boolean bl2 = levelChunkSection.hasOnlyAir();
        if (bl != bl2) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, bl2);
            this.level.getChunkSource().onSectionEmptinessChanged(this.chunkPos.x, SectionPos.blockToSectionCoord(j), this.chunkPos.z, bl2);
        }
        if (LightEngine.hasDifferentLightProperties(blockState2, blockState)) {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("updateSkyLightSources");
            this.skyLightSources.update(this, k, j, m);
            profilerFiller.popPush("queueCheckLight");
            this.level.getChunkSource().getLightEngine().checkBlock(blockPos);
            profilerFiller.pop();
        }
        boolean bl3 = !blockState2.is(block);
        boolean bl4 = (i & 0x40) != 0;
        boolean bl6 = bl5 = (i & 0x100) == 0;
        if (bl3 && blockState2.hasBlockEntity() && !blockState.shouldChangedStateKeepBlockEntity(blockState2)) {
            if (!this.level.isClientSide() && bl5 && (blockEntity = this.level.getBlockEntity(blockPos)) != null) {
                blockEntity.preRemoveSideEffects(blockPos, blockState2);
            }
            this.removeBlockEntity(blockPos);
        }
        if ((bl3 || block instanceof BaseRailBlock) && (level = this.level) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if ((i & 1) != 0 || bl4) {
                blockState2.affectNeighborsAfterRemoval(serverLevel, blockPos, bl4);
            }
        }
        if (!levelChunkSection.getBlockState(k, l, m).is(block)) {
            return null;
        }
        if (!this.level.isClientSide() && (i & 0x200) == 0) {
            blockState.onPlace(this.level, blockPos, blockState2, bl4);
        }
        if (blockState.hasBlockEntity()) {
            blockEntity = this.getBlockEntity(blockPos, EntityCreationType.CHECK);
            if (blockEntity != null && !blockEntity.isValidBlockState(blockState)) {
                LOGGER.warn("Found mismatched block entity @ {}: type = {}, state = {}", new Object[]{blockPos, blockEntity.getType().builtInRegistryHolder().key().identifier(), blockState});
                this.removeBlockEntity(blockPos);
                blockEntity = null;
            }
            if (blockEntity == null) {
                blockEntity = ((EntityBlock)((Object)block)).newBlockEntity(blockPos, blockState);
                if (blockEntity != null) {
                    this.addAndRegisterBlockEntity(blockEntity);
                }
            } else {
                blockEntity.setBlockState(blockState);
                this.updateBlockEntityTicker(blockEntity);
            }
        }
        this.markUnsaved();
        return blockState2;
    }

    @Override
    @Deprecated
    public void addEntity(Entity entity) {
    }

    private @Nullable BlockEntity createBlockEntity(BlockPos blockPos) {
        BlockState blockState = this.getBlockState(blockPos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.getBlockEntity(blockPos, EntityCreationType.CHECK);
    }

    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos, EntityCreationType entityCreationType) {
        BlockEntity blockEntity2;
        CompoundTag compoundTag;
        BlockEntity blockEntity = (BlockEntity)this.blockEntities.get(blockPos);
        if (blockEntity == null && (compoundTag = (CompoundTag)this.pendingBlockEntities.remove(blockPos)) != null && (blockEntity2 = this.promotePendingBlockEntity(blockPos, compoundTag)) != null) {
            return blockEntity2;
        }
        if (blockEntity == null) {
            if (entityCreationType == EntityCreationType.IMMEDIATE && (blockEntity = this.createBlockEntity(blockPos)) != null) {
                this.addAndRegisterBlockEntity(blockEntity);
            }
        } else if (blockEntity.isRemoved()) {
            this.blockEntities.remove(blockPos);
            return null;
        }
        return blockEntity;
    }

    public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        if (this.isInLevel()) {
            Level level = this.level;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.addGameEventListener(blockEntity, serverLevel);
            }
            this.level.onBlockEntityAdded(blockEntity);
            this.updateBlockEntityTicker(blockEntity);
        }
    }

    private boolean isInLevel() {
        return this.loaded || this.level.isClientSide();
    }

    boolean isTicking(BlockPos blockPos) {
        if (!this.level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return this.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING) && serverLevel.areEntitiesLoaded(ChunkPos.asLong(blockPos));
        }
        return true;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        BlockState blockState = this.getBlockState(blockPos);
        if (!blockState.hasBlockEntity()) {
            LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{blockEntity, blockPos, blockState});
            return;
        }
        BlockState blockState2 = blockEntity.getBlockState();
        if (blockState != blockState2) {
            if (!blockEntity.getType().isValid(blockState)) {
                LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{blockEntity, blockPos, blockState});
                return;
            }
            if (blockState.getBlock() != blockState2.getBlock()) {
                LOGGER.warn("Block state mismatch on block entity {} in position {}, {} != {}, updating", new Object[]{blockEntity, blockPos, blockState, blockState2});
            }
            blockEntity.setBlockState(blockState);
        }
        blockEntity.setLevel(this.level);
        blockEntity.clearRemoved();
        BlockEntity blockEntity2 = this.blockEntities.put(blockPos.immutable(), blockEntity);
        if (blockEntity2 != null && blockEntity2 != blockEntity) {
            blockEntity2.setRemoved();
        }
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null && !blockEntity.isRemoved()) {
            CompoundTag compoundTag = blockEntity.saveWithFullMetadata(this.level.registryAccess());
            compoundTag.putBoolean("keepPacked", false);
            return compoundTag;
        }
        CompoundTag compoundTag = (CompoundTag)this.pendingBlockEntities.get(blockPos);
        if (compoundTag != null) {
            compoundTag = compoundTag.copy();
            compoundTag.putBoolean("keepPacked", true);
        }
        return compoundTag;
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity blockEntity;
        if (this.isInLevel() && (blockEntity = (BlockEntity)this.blockEntities.remove(blockPos)) != null) {
            Level level = this.level;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.removeGameEventListener(blockEntity, serverLevel);
                serverLevel.debugSynchronizers().dropBlockEntity(blockPos);
            }
            blockEntity.setRemoved();
        }
        this.removeBlockEntityTicker(blockPos);
    }

    private <T extends BlockEntity> void removeGameEventListener(T blockEntity, ServerLevel serverLevel) {
        GameEventListener gameEventListener;
        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof EntityBlock && (gameEventListener = ((EntityBlock)((Object)block)).getListener(serverLevel, blockEntity)) != null) {
            int i = SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY());
            GameEventListenerRegistry gameEventListenerRegistry = this.getListenerRegistry(i);
            gameEventListenerRegistry.unregister(gameEventListener);
        }
    }

    private void removeGameEventListenerRegistry(int i) {
        this.gameEventListenerRegistrySections.remove(i);
    }

    private void removeBlockEntityTicker(BlockPos blockPos) {
        RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper = this.tickersInLevel.remove(blockPos);
        if (rebindableTickingBlockEntityWrapper != null) {
            rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER);
        }
    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.run(this);
            this.postLoad = null;
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public void replaceWithPacketData(FriendlyByteBuf friendlyByteBuf, Map<Heightmap.Types, long[]> map, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        this.clearAllBlockEntities();
        for (LevelChunkSection levelChunkSection : this.sections) {
            levelChunkSection.read(friendlyByteBuf);
        }
        map.forEach(this::setHeightmap);
        this.initializeLightSources();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER);){
            consumer.accept((blockPos, blockEntityType, compoundTag) -> {
                BlockEntity blockEntity = this.getBlockEntity(blockPos, EntityCreationType.IMMEDIATE);
                if (blockEntity != null && compoundTag != null && blockEntity.getType() == blockEntityType) {
                    blockEntity.loadWithComponents(TagValueInput.create(scopedCollector.forChild(blockEntity.problemPath()), (HolderLookup.Provider)this.level.registryAccess(), compoundTag));
                }
            });
        }
    }

    public void replaceBiomes(FriendlyByteBuf friendlyByteBuf) {
        for (LevelChunkSection levelChunkSection : this.sections) {
            levelChunkSection.readBiomes(friendlyByteBuf);
        }
    }

    public void setLoaded(boolean bl) {
        this.loaded = bl;
    }

    public Level getLevel() {
        return this.level;
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void postProcessGeneration(ServerLevel serverLevel) {
        ChunkPos chunkPos = this.getPos();
        for (int i = 0; i < this.postProcessing.length; ++i) {
            ShortList shortList = this.postProcessing[i];
            if (shortList == null) continue;
            for (Short short_ : shortList) {
                BlockState blockState2;
                BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, this.getSectionYFromSectionIndex(i), chunkPos);
                BlockState blockState = this.getBlockState(blockPos);
                FluidState fluidState = blockState.getFluidState();
                if (!fluidState.isEmpty()) {
                    fluidState.tick(serverLevel, blockPos, blockState);
                }
                if (blockState.getBlock() instanceof LiquidBlock || (blockState2 = Block.updateFromNeighbourShapes(blockState, serverLevel, blockPos)) == blockState) continue;
                serverLevel.setBlock(blockPos, blockState2, 276);
            }
            shortList.clear();
        }
        for (BlockPos blockPos2 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(blockPos2);
        }
        this.pendingBlockEntities.clear();
        this.upgradeData.upgrade(this);
    }

    private @Nullable BlockEntity promotePendingBlockEntity(BlockPos blockPos, CompoundTag compoundTag) {
        BlockEntity blockEntity;
        BlockState blockState = this.getBlockState(blockPos);
        if ("DUMMY".equals(compoundTag.getStringOr("id", ""))) {
            if (blockState.hasBlockEntity()) {
                blockEntity = ((EntityBlock)((Object)blockState.getBlock())).newBlockEntity(blockPos, blockState);
            } else {
                blockEntity = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", (Object)blockPos, (Object)blockState);
            }
        } else {
            blockEntity = BlockEntity.loadStatic(blockPos, blockState, compoundTag, this.level.registryAccess());
        }
        if (blockEntity != null) {
            blockEntity.setLevel(this.level);
            this.addAndRegisterBlockEntity(blockEntity);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", (Object)blockState, (Object)blockPos);
        }
        return blockEntity;
    }

    public void unpackTicks(long l) {
        this.blockTicks.unpack(l);
        this.fluidTicks.unpack(l);
    }

    public void registerTickContainerInLevel(ServerLevel serverLevel) {
        ((LevelTicks)serverLevel.getBlockTicks()).addContainer(this.chunkPos, this.blockTicks);
        ((LevelTicks)serverLevel.getFluidTicks()).addContainer(this.chunkPos, this.fluidTicks);
    }

    public void unregisterTickContainerFromLevel(ServerLevel serverLevel) {
        ((LevelTicks)serverLevel.getBlockTicks()).removeContainer(this.chunkPos);
        ((LevelTicks)serverLevel.getFluidTicks()).removeContainer(this.chunkPos);
    }

    @Override
    public void registerDebugValues(ServerLevel serverLevel, DebugValueSource.Registration registration) {
        if (!this.getAllStarts().isEmpty()) {
            registration.register(DebugSubscriptions.STRUCTURES, () -> {
                ArrayList<DebugStructureInfo> list = new ArrayList<DebugStructureInfo>();
                for (StructureStart structureStart : this.getAllStarts().values()) {
                    BoundingBox boundingBox = structureStart.getBoundingBox();
                    List<StructurePiece> list2 = structureStart.getPieces();
                    ArrayList<DebugStructureInfo.Piece> list3 = new ArrayList<DebugStructureInfo.Piece>(list2.size());
                    for (int i = 0; i < list2.size(); ++i) {
                        boolean bl = i == 0;
                        list3.add(new DebugStructureInfo.Piece(list2.get(i).getBoundingBox(), bl));
                    }
                    list.add(new DebugStructureInfo(boundingBox, list3));
                }
                return list;
            });
        }
        registration.register(DebugSubscriptions.RAIDS, () -> serverLevel.getRaids().getRaidCentersInChunk(this.chunkPos));
    }

    @Override
    public ChunkStatus getPersistedStatus() {
        return ChunkStatus.FULL;
    }

    public FullChunkStatus getFullStatus() {
        if (this.fullStatus == null) {
            return FullChunkStatus.FULL;
        }
        return this.fullStatus.get();
    }

    public void setFullStatus(Supplier<FullChunkStatus> supplier) {
        this.fullStatus = supplier;
    }

    public void clearAllBlockEntities() {
        this.blockEntities.values().forEach(BlockEntity::setRemoved);
        this.blockEntities.clear();
        this.tickersInLevel.values().forEach(rebindableTickingBlockEntityWrapper -> rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER));
        this.tickersInLevel.clear();
    }

    public void registerAllBlockEntitiesAfterLevelLoad() {
        this.blockEntities.values().forEach(blockEntity -> {
            Level level = this.level;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.addGameEventListener(blockEntity, serverLevel);
            }
            this.level.onBlockEntityAdded((BlockEntity)blockEntity);
            this.updateBlockEntityTicker(blockEntity);
        });
    }

    private <T extends BlockEntity> void addGameEventListener(T blockEntity, ServerLevel serverLevel) {
        GameEventListener gameEventListener;
        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof EntityBlock && (gameEventListener = ((EntityBlock)((Object)block)).getListener(serverLevel, blockEntity)) != null) {
            this.getListenerRegistry(SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY())).register(gameEventListener);
        }
    }

    private <T extends BlockEntity> void updateBlockEntityTicker(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        BlockEntityTicker<?> blockEntityTicker = blockState.getTicker(this.level, blockEntity.getType());
        if (blockEntityTicker == null) {
            this.removeBlockEntityTicker(blockEntity.getBlockPos());
        } else {
            this.tickersInLevel.compute(blockEntity.getBlockPos(), (blockPos, rebindableTickingBlockEntityWrapper) -> {
                TickingBlockEntity tickingBlockEntity = this.createTicker(blockEntity, blockEntityTicker);
                if (rebindableTickingBlockEntityWrapper != null) {
                    rebindableTickingBlockEntityWrapper.rebind(tickingBlockEntity);
                    return rebindableTickingBlockEntityWrapper;
                }
                if (this.isInLevel()) {
                    RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper2 = new RebindableTickingBlockEntityWrapper(tickingBlockEntity);
                    this.level.addBlockEntityTicker(rebindableTickingBlockEntityWrapper2);
                    return rebindableTickingBlockEntityWrapper2;
                }
                return null;
            });
        }
    }

    private <T extends BlockEntity> TickingBlockEntity createTicker(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
        return new BoundTickingBlockEntity(this, blockEntity, blockEntityTicker);
    }

    @FunctionalInterface
    public static interface PostLoadProcessor {
        public void run(LevelChunk var1);
    }

    @FunctionalInterface
    public static interface UnsavedListener {
        public void setUnsaved(ChunkPos var1);
    }

    public static enum EntityCreationType {
        IMMEDIATE,
        QUEUED,
        CHECK;

    }

    static class RebindableTickingBlockEntityWrapper
    implements TickingBlockEntity {
        private TickingBlockEntity ticker;

        RebindableTickingBlockEntityWrapper(TickingBlockEntity tickingBlockEntity) {
            this.ticker = tickingBlockEntity;
        }

        void rebind(TickingBlockEntity tickingBlockEntity) {
            this.ticker = tickingBlockEntity;
        }

        @Override
        public void tick() {
            this.ticker.tick();
        }

        @Override
        public boolean isRemoved() {
            return this.ticker.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.ticker.getPos();
        }

        @Override
        public String getType() {
            return this.ticker.getType();
        }

        public String toString() {
            return String.valueOf(this.ticker) + " <wrapped>";
        }
    }

    static class BoundTickingBlockEntity<T extends BlockEntity>
    implements TickingBlockEntity {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean loggedInvalidBlockState;
        final /* synthetic */ LevelChunk field_27223;

        BoundTickingBlockEntity(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
            this.field_27223 = levelChunk;
            this.blockEntity = blockEntity;
            this.ticker = blockEntityTicker;
        }

        @Override
        public void tick() {
            BlockPos blockPos;
            if (!((BlockEntity)this.blockEntity).isRemoved() && ((BlockEntity)this.blockEntity).hasLevel() && this.field_27223.isTicking(blockPos = ((BlockEntity)this.blockEntity).getBlockPos())) {
                try {
                    ProfilerFiller profilerFiller = Profiler.get();
                    profilerFiller.push(this::getType);
                    BlockState blockState = this.field_27223.getBlockState(blockPos);
                    if (((BlockEntity)this.blockEntity).getType().isValid(blockState)) {
                        this.ticker.tick(this.field_27223.level, ((BlockEntity)this.blockEntity).getBlockPos(), blockState, this.blockEntity);
                        this.loggedInvalidBlockState = false;
                    } else if (!this.loggedInvalidBlockState) {
                        this.loggedInvalidBlockState = true;
                        LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", new Object[]{LogUtils.defer(this::getType), LogUtils.defer(this::getPos), blockState});
                    }
                    profilerFiller.pop();
                }
                catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking block entity");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Block entity being ticked");
                    ((BlockEntity)this.blockEntity).fillCrashReportCategory(crashReportCategory);
                    throw new ReportedException(crashReport);
                }
            }
        }

        @Override
        public boolean isRemoved() {
            return ((BlockEntity)this.blockEntity).isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return ((BlockEntity)this.blockEntity).getBlockPos();
        }

        @Override
        public String getType() {
            return BlockEntityType.getKey(((BlockEntity)this.blockEntity).getType()).toString();
        }

        public String toString() {
            return "Level ticker for " + this.getType() + "@" + String.valueOf(this.getPos());
        }
    }
}

