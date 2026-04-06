/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int UNSET_HEIGHT = Integer.MIN_VALUE;

    public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext generationContext, Holder<StructureTemplatePool> holder, Optional<Identifier> optional, int i, BlockPos blockPos, boolean bl, Optional<Heightmap.Types> optional2, JigsawStructure.MaxDistance maxDistance, PoolAliasLookup poolAliasLookup, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        BlockPos blockPos2;
        RegistryAccess registryAccess = generationContext.registryAccess();
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        StructureTemplateManager structureTemplateManager = generationContext.structureTemplateManager();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        WorldgenRandom worldgenRandom = generationContext.random();
        HolderLookup.RegistryLookup registry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        StructureTemplatePool structureTemplatePool = holder.unwrapKey().flatMap(arg_0 -> JigsawPlacement.method_55604((Registry)registry, poolAliasLookup, arg_0)).orElse(holder.value());
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (optional.isPresent()) {
            Identifier identifier = optional.get();
            Optional<BlockPos> optional3 = JigsawPlacement.getRandomNamedJigsaw(structurePoolElement, identifier, blockPos, rotation, structureTemplateManager, worldgenRandom);
            if (optional3.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)identifier, (Object)holder.unwrapKey().map(resourceKey -> resourceKey.identifier().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            blockPos2 = optional3.get();
        } else {
            blockPos2 = blockPos;
        }
        BlockPos vec3i = blockPos2.subtract(blockPos);
        BlockPos blockPos3 = blockPos.subtract(vec3i);
        PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(structureTemplateManager, structurePoolElement, blockPos3, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos3, rotation), liquidSettings);
        BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
        int j = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int k = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int l = optional2.isEmpty() ? blockPos3.getY() : blockPos.getY() + chunkGenerator.getFirstFreeHeight(j, k, optional2.get(), levelHeightAccessor, generationContext.randomState());
        int m = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
        poolElementStructurePiece.move(0, l - m, 0);
        if (JigsawPlacement.isStartTooCloseToWorldHeightLimits(levelHeightAccessor, dimensionPadding, poolElementStructurePiece.getBoundingBox())) {
            LOGGER.debug("Center piece {} with bounding box {} does not fit dimension padding {}", new Object[]{structurePoolElement, poolElementStructurePiece.getBoundingBox(), dimensionPadding});
            return Optional.empty();
        }
        int n = l + vec3i.getY();
        return Optional.of(new Structure.GenerationStub(new BlockPos(j, n, k), arg_0 -> JigsawPlacement.method_39824(poolElementStructurePiece, i, j, maxDistance, n, levelHeightAccessor, dimensionPadding, k, boundingBox, generationContext, bl, chunkGenerator, structureTemplateManager, worldgenRandom, (Registry)registry, poolAliasLookup, liquidSettings, arg_0)));
    }

    private static boolean isStartTooCloseToWorldHeightLimits(LevelHeightAccessor levelHeightAccessor, DimensionPadding dimensionPadding, BoundingBox boundingBox) {
        if (dimensionPadding == DimensionPadding.ZERO) {
            return false;
        }
        int i = levelHeightAccessor.getMinY() + dimensionPadding.bottom();
        int j = levelHeightAccessor.getMaxY() - dimensionPadding.top();
        return boundingBox.minY() < i || boundingBox.maxY() > j;
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement structurePoolElement, Identifier identifier, BlockPos blockPos, Rotation rotation, StructureTemplateManager structureTemplateManager, WorldgenRandom worldgenRandom) {
        List<StructureTemplate.JigsawBlockInfo> list = structurePoolElement.getShuffledJigsawBlocks(structureTemplateManager, blockPos, rotation, worldgenRandom);
        for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo : list) {
            if (!identifier.equals(jigsawBlockInfo.name())) continue;
            return Optional.of(jigsawBlockInfo.info().pos());
        }
        return Optional.empty();
    }

    private static void addPieces(RandomState randomState, int i, boolean bl, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor levelHeightAccessor, RandomSource randomSource, Registry<StructureTemplatePool> registry, PoolElementStructurePiece poolElementStructurePiece, List<PoolElementStructurePiece> list, VoxelShape voxelShape, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings) {
        Placer placer = new Placer(registry, i, chunkGenerator, structureTemplateManager, list, randomSource);
        placer.tryPlacingChildren(poolElementStructurePiece, (MutableObject<VoxelShape>)new MutableObject((Object)voxelShape), 0, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
        while (placer.placing.hasNext()) {
            PieceState pieceState = (PieceState)((Object)placer.placing.next());
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
        }
    }

    public static boolean generateJigsaw(ServerLevel serverLevel, Holder<StructureTemplatePool> holder2, Identifier identifier, int i, BlockPos blockPos, boolean bl) {
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        StructureManager structureManager = serverLevel.structureManager();
        RandomSource randomSource = serverLevel.getRandom();
        Structure.GenerationContext generationContext = new Structure.GenerationContext(serverLevel.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), structureTemplateManager, serverLevel.getSeed(), new ChunkPos(blockPos), serverLevel, holder -> true);
        Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(generationContext, holder2, Optional.of(identifier), i, blockPos, false, Optional.empty(), new JigsawStructure.MaxDistance(128), PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            StructurePiecesBuilder structurePiecesBuilder = optional.get().getPiecesBuilder();
            for (StructurePiece structurePiece : structurePiecesBuilder.build().pieces()) {
                if (!(structurePiece instanceof PoolElementStructurePiece)) continue;
                PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                poolElementStructurePiece.place(serverLevel, structureManager, chunkGenerator, randomSource, BoundingBox.infinite(), blockPos, bl);
            }
            return true;
        }
        return false;
    }

    private static /* synthetic */ void method_39824(PoolElementStructurePiece poolElementStructurePiece, int i, int j, JigsawStructure.MaxDistance maxDistance, int k, LevelHeightAccessor levelHeightAccessor, DimensionPadding dimensionPadding, int l, BoundingBox boundingBox, Structure.GenerationContext generationContext, boolean bl, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, WorldgenRandom worldgenRandom, Registry registry, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings, StructurePiecesBuilder structurePiecesBuilder) {
        ArrayList list = Lists.newArrayList();
        list.add(poolElementStructurePiece);
        if (i <= 0) {
            return;
        }
        AABB aABB = new AABB(j - maxDistance.horizontal(), Math.max(k - maxDistance.vertical(), levelHeightAccessor.getMinY() + dimensionPadding.bottom()), l - maxDistance.horizontal(), j + maxDistance.horizontal() + 1, Math.min(k + maxDistance.vertical() + 1, levelHeightAccessor.getMaxY() + 1 - dimensionPadding.top()), l + maxDistance.horizontal() + 1);
        VoxelShape voxelShape = Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
        JigsawPlacement.addPieces(generationContext.randomState(), i, bl, chunkGenerator, structureTemplateManager, levelHeightAccessor, worldgenRandom, registry, poolElementStructurePiece, list, voxelShape, poolAliasLookup, liquidSettings);
        list.forEach(structurePiecesBuilder::addPiece);
    }

    private static /* synthetic */ Optional method_55604(Registry registry, PoolAliasLookup poolAliasLookup, ResourceKey resourceKey) {
        return registry.getOptional(poolAliasLookup.lookup(resourceKey));
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        final SequencedPriorityIterator<PieceState> placing = new SequencedPriorityIterator();

        Placer(Registry<StructureTemplatePool> registry, int i, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolElementStructurePiece> list, RandomSource randomSource) {
            this.pools = registry;
            this.maxDepth = i;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.pieces = list;
            this.random = randomSource;
        }

        void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<@Nullable VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int j = boundingBox.minY();
            block0: for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureTemplateManager, blockPos, rotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject3;
                StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawBlockInfo2.info();
                Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state());
                BlockPos blockPos2 = structureBlockInfo.pos();
                BlockPos blockPos3 = blockPos2.relative(direction);
                int k = blockPos2.getY() - j;
                int l = Integer.MIN_VALUE;
                ResourceKey<StructureTemplatePool> resourceKey2 = poolAliasLookup.lookup(jigsawBlockInfo2.pool());
                Optional optional = this.pools.get(resourceKey2);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceKey2.identifier());
                    continue;
                }
                Holder holder = (Holder)optional.get();
                if (((StructureTemplatePool)holder.value()).size() == 0 && !holder.is(Pools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceKey2.identifier());
                    continue;
                }
                Holder<StructureTemplatePool> holder2 = ((StructureTemplatePool)holder.value()).getFallback();
                if (holder2.value().size() == 0 && !holder2.is(Pools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)holder2.unwrapKey().map(resourceKey -> resourceKey.identifier().toString()).orElse("<unregistered>"));
                    continue;
                }
                boolean bl3 = boundingBox.isInside(blockPos3);
                if (bl3) {
                    mutableObject3 = mutableObject2;
                    if (mutableObject2.get() == null) {
                        mutableObject2.setValue((Object)Shapes.create(AABB.of(boundingBox)));
                    }
                } else {
                    mutableObject3 = mutableObject;
                }
                ArrayList list = Lists.newArrayList();
                if (i != this.maxDepth) {
                    list.addAll(((StructureTemplatePool)holder.value()).getShuffledTemplates(this.random));
                }
                list.addAll(holder2.value().getShuffledTemplates(this.random));
                int m = jigsawBlockInfo2.placementPriority();
                Iterator iterator = list.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.JigsawBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation2);
                        int n = !bl || boundingBox2.getYSpan() > 16 ? 0 : list2.stream().mapToInt(jigsawBlockInfo -> {
                            StructureTemplate.StructureBlockInfo structureBlockInfo = jigsawBlockInfo.info();
                            if (!boundingBox2.isInside(structureBlockInfo.pos().relative(JigsawBlock.getFrontFacing(structureBlockInfo.state())))) {
                                return 0;
                            }
                            ResourceKey<StructureTemplatePool> resourceKey = poolAliasLookup.lookup(jigsawBlockInfo.pool());
                            Optional optional = this.pools.get(resourceKey);
                            Optional<Holder> optional2 = optional.map(holder -> ((StructureTemplatePool)holder.value()).getFallback());
                            int i = optional.map(holder -> ((StructureTemplatePool)holder.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
                            int j = optional2.map(holder -> ((StructureTemplatePool)holder.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
                            return Math.max(i, j);
                        }).max().orElse(0);
                        for (StructureTemplate.JigsawBlockInfo jigsawBlockInfo22 : list2) {
                            int v;
                            int t;
                            int r;
                            if (!JigsawBlock.canAttach(jigsawBlockInfo2, jigsawBlockInfo22)) continue;
                            BlockPos blockPos4 = jigsawBlockInfo22.info().pos();
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, rotation2);
                            int o = boundingBox3.minY();
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int p = blockPos4.getY();
                            int q = k - p + JigsawBlock.getFrontFacing(structureBlockInfo.state()).getStepY();
                            if (bl2 && bl4) {
                                r = j + q;
                            } else {
                                if (l == Integer.MIN_VALUE) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
                                }
                                r = l - p;
                            }
                            int s = r - o;
                            BoundingBox boundingBox4 = boundingBox3.moved(0, s, 0);
                            BlockPos blockPos6 = blockPos5.offset(0, s, 0);
                            if (n > 0) {
                                t = Math.max(n + 1, boundingBox4.maxY() - boundingBox4.minY());
                                boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + t, boundingBox4.minZ()));
                            }
                            if (Shapes.joinIsNotEmpty((VoxelShape)mutableObject3.get(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) continue;
                            mutableObject3.setValue((Object)Shapes.joinUnoptimized((VoxelShape)mutableObject3.get(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                            t = poolElementStructurePiece.getGroundLevelDelta();
                            int u = bl4 ? t - q : structurePoolElement2.getGroundLevelDelta();
                            PoolElementStructurePiece poolElementStructurePiece2 = new PoolElementStructurePiece(this.structureTemplateManager, structurePoolElement2, blockPos6, u, rotation2, boundingBox4, liquidSettings);
                            if (bl2) {
                                v = j + k;
                            } else if (bl4) {
                                v = r + p;
                            } else {
                                if (l == Integer.MIN_VALUE) {
                                    l = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
                                }
                                v = l + q / 2;
                            }
                            poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), v - k + t, blockPos3.getZ(), q, projection2));
                            poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), v - p + u, blockPos2.getZ(), -q, projection));
                            this.pieces.add(poolElementStructurePiece2);
                            if (i + 1 > this.maxDepth) continue block0;
                            PieceState pieceState = new PieceState(poolElementStructurePiece2, mutableObject3, i + 1);
                            this.placing.add(pieceState, m);
                            continue block0;
                        }
                    }
                }
            }
        }
    }

    static final class PieceState
    extends Record {
        final PoolElementStructurePiece piece;
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i) {
            this.piece = poolElementStructurePiece;
            this.free = mutableObject;
            this.depth = i;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this, object);
        }

        public PoolElementStructurePiece piece() {
            return this.piece;
        }

        public MutableObject<VoxelShape> free() {
            return this.free;
        }

        public int depth() {
            return this.depth;
        }
    }
}

