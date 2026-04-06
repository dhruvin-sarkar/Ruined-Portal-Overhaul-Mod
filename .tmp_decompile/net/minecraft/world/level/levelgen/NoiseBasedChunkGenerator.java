/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public final class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final MapCodec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource), (App)NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)).apply((Applicative)instance, instance.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final Holder<NoiseGeneratorSettings> settings;
    private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
        super(biomeSource);
        this.settings = holder;
        this.globalFluidPicker = Suppliers.memoize(() -> NoiseBasedChunkGenerator.createFluidPicker((NoiseGeneratorSettings)((Object)((Object)holder.value()))));
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noiseGeneratorSettings) {
        Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = noiseGeneratorSettings.seaLevel();
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.defaultFluid());
        Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        return (j, k, l) -> {
            if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) {
                return fluidStatus3;
            }
            if (k < Math.min(-54, i)) {
                return fluidStatus;
            }
            return fluidStatus2;
        };
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(() -> {
            this.doCreateBiomes(blender, randomState, structureManager, chunkAccess);
            return chunkAccess;
        }, Util.backgroundExecutor().forName("init_biomes"));
    }

    private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess2) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunkAccess2);
        chunkAccess2.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget()));
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(chunkAccess, randomState, Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()), this.settings.value(), this.globalFluidPicker.get(), blender);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.settings.is(resourceKey);
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, null, types.isOpaque()).orElse(levelHeightAccessor.getMinY());
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        MutableObject mutableObject = new MutableObject();
        this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, (MutableObject<NoiseColumn>)mutableObject, null);
        return (NoiseColumn)mutableObject.get();
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ROOT));
        NoiseRouter noiseRouter = randomState.router();
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        double d = noiseRouter.ridges().compute(singlePointContext);
        list.add("NoiseRouter T: " + decimalFormat.format(noiseRouter.temperature().compute(singlePointContext)) + " V: " + decimalFormat.format(noiseRouter.vegetation().compute(singlePointContext)) + " C: " + decimalFormat.format(noiseRouter.continents().compute(singlePointContext)) + " E: " + decimalFormat.format(noiseRouter.erosion().compute(singlePointContext)) + " D: " + decimalFormat.format(noiseRouter.depth().compute(singlePointContext)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format(NoiseRouterData.peaksAndValleys((float)d)) + " PS: " + decimalFormat.format(noiseRouter.preliminarySurfaceLevel().compute(singlePointContext)) + " N: " + decimalFormat.format(noiseRouter.finalDensity().compute(singlePointContext)));
    }

    private OptionalInt iterateNoiseColumn(LevelHeightAccessor levelHeightAccessor, RandomState randomState, int i, int j, @Nullable MutableObject<NoiseColumn> mutableObject, @Nullable Predicate<BlockState> predicate) {
        BlockState[] blockStates;
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(levelHeightAccessor);
        int k = noiseSettings.getCellHeight();
        int l = noiseSettings.minY();
        int m = Mth.floorDiv(l, k);
        int n = Mth.floorDiv(noiseSettings.height(), k);
        if (n <= 0) {
            return OptionalInt.empty();
        }
        if (mutableObject == null) {
            blockStates = null;
        } else {
            blockStates = new BlockState[noiseSettings.height()];
            mutableObject.setValue((Object)new NoiseColumn(l, blockStates));
        }
        int o = noiseSettings.getCellWidth();
        int p = Math.floorDiv(i, o);
        int q = Math.floorDiv(j, o);
        int r = Math.floorMod(i, o);
        int s = Math.floorMod(j, o);
        int t = p * o;
        int u = q * o;
        double d = (double)r / (double)o;
        double e = (double)s / (double)o;
        NoiseChunk noiseChunk = new NoiseChunk(1, randomState, t, u, noiseSettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
        noiseChunk.initializeForFirstCellX();
        noiseChunk.advanceCellX(0);
        for (int v = n - 1; v >= 0; --v) {
            noiseChunk.selectCellYZ(v, 0);
            for (int w = k - 1; w >= 0; --w) {
                BlockState blockState2;
                int x = (m + v) * k + w;
                double f = (double)w / (double)k;
                noiseChunk.updateForY(x, f);
                noiseChunk.updateForX(i, d);
                noiseChunk.updateForZ(j, e);
                BlockState blockState = noiseChunk.getInterpolatedState();
                BlockState blockState3 = blockState2 = blockState == null ? this.settings.value().defaultBlock() : blockState;
                if (blockStates != null) {
                    int y = v * k + w;
                    blockStates[y] = blockState2;
                }
                if (predicate == null || !predicate.test(blockState2)) continue;
                noiseChunk.stopInterpolation();
                return OptionalInt.of(x + 1);
            }
        }
        noiseChunk.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
        if (SharedConstants.debugVoidTerrain(chunkAccess.getPos()) || SharedConstants.DEBUG_DISABLE_SURFACE) {
            return;
        }
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
        this.buildSurface(chunkAccess, worldGenerationContext, randomState, structureManager, worldGenRegion.getBiomeManager(), (Registry<Biome>)worldGenRegion.registryAccess().lookupOrThrow(Registries.BIOME), Blender.of(worldGenRegion));
    }

    @VisibleForTesting
    public void buildSurface(ChunkAccess chunkAccess2, WorldGenerationContext worldGenerationContext, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> registry, Blender blender) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        randomState.surfaceSystem().buildSurface(randomState, biomeManager, registry, noiseGeneratorSettings.useLegacyRandomSource(), worldGenerationContext, chunkAccess2, noiseChunk, noiseGeneratorSettings.surfaceRule());
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess2) {
        if (SharedConstants.DEBUG_DISABLE_CARVERS) {
            return;
        }
        BiomeManager biomeManager2 = biomeManager.withDifferentSource((i, j, k) -> this.biomeSource.getNoiseBiome(i, j, k, randomState.sampler()));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int i2 = 8;
        ChunkPos chunkPos = chunkAccess2.getPos();
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, Blender.of(worldGenRegion), randomState));
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess2.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule());
        CarvingMask carvingMask = ((ProtoChunk)chunkAccess2).getOrCreateCarvingMask();
        for (int j2 = -8; j2 <= 8; ++j2) {
            for (int k2 = -8; k2 <= 8; ++k2) {
                ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j2, chunkPos.z + k2);
                ChunkAccess chunkAccess22 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
                BiomeGenerationSettings biomeGenerationSettings = chunkAccess22.carverBiome(() -> this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), randomState.sampler())));
                Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers();
                int m = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(l + (long)m, chunkPos2.x, chunkPos2.z);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        configuredWorldCarver.carve(carvingContext, chunkAccess2, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                    }
                    ++m;
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunkAccess.getHeightAccessorForGeneration());
        int i = noiseSettings.minY();
        int j = Mth.floorDiv(i, noiseSettings.getCellHeight());
        int k = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunkAccess);
        }
        return CompletableFuture.supplyAsync(() -> {
            int l = chunkAccess.getSectionIndex(k * noiseSettings.getCellHeight() - 1 + i);
            int m = chunkAccess.getSectionIndex(i);
            HashSet set = Sets.newHashSet();
            for (int n = l; n >= m; --n) {
                LevelChunkSection levelChunkSection = chunkAccess.getSection(n);
                levelChunkSection.acquire();
                set.add(levelChunkSection);
            }
            try {
                ChunkAccess chunkAccess2 = this.doFill(blender, structureManager, randomState, chunkAccess, j, k);
                return chunkAccess2;
            }
            finally {
                for (LevelChunkSection levelChunkSection2 : set) {
                    levelChunkSection2.release();
                }
            }
        }, Util.backgroundExecutor().forName("wgen_fill_noise"));
    }

    private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess2, int i, int j) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        Heightmap heightmap = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess2.getPos();
        int k = chunkPos.getMinBlockX();
        int l = chunkPos.getMinBlockZ();
        Aquifer aquifer = noiseChunk.aquifer();
        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int m = noiseChunk.cellWidth();
        int n = noiseChunk.cellHeight();
        int o = 16 / m;
        int p = 16 / m;
        for (int q = 0; q < o; ++q) {
            noiseChunk.advanceCellX(q);
            for (int r = 0; r < p; ++r) {
                int s = chunkAccess2.getSectionsCount() - 1;
                LevelChunkSection levelChunkSection = chunkAccess2.getSection(s);
                for (int t = j - 1; t >= 0; --t) {
                    noiseChunk.selectCellYZ(t, r);
                    for (int u = n - 1; u >= 0; --u) {
                        int v = (i + t) * n + u;
                        int w = v & 0xF;
                        int x = chunkAccess2.getSectionIndex(v);
                        if (s != x) {
                            s = x;
                            levelChunkSection = chunkAccess2.getSection(x);
                        }
                        double d = (double)u / (double)n;
                        noiseChunk.updateForY(v, d);
                        for (int y = 0; y < m; ++y) {
                            int z = k + q * m + y;
                            int aa = z & 0xF;
                            double e = (double)y / (double)m;
                            noiseChunk.updateForX(z, e);
                            for (int ab = 0; ab < m; ++ab) {
                                int ac = l + r * m + ab;
                                int ad = ac & 0xF;
                                double f = (double)ab / (double)m;
                                noiseChunk.updateForZ(ac, f);
                                BlockState blockState = noiseChunk.getInterpolatedState();
                                if (blockState == null) {
                                    blockState = this.settings.value().defaultBlock();
                                }
                                if ((blockState = this.debugPreliminarySurfaceLevel(noiseChunk, z, v, ac, blockState)) == AIR || SharedConstants.debugVoidTerrain(chunkAccess2.getPos())) continue;
                                levelChunkSection.setBlockState(aa, w, ad, blockState, false);
                                heightmap.update(aa, v, ad, blockState);
                                heightmap2.update(aa, v, ad, blockState);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(z, v, ac);
                                chunkAccess2.markPosForPostprocessing(mutableBlockPos);
                            }
                        }
                    }
                }
            }
            noiseChunk.swapSlices();
        }
        noiseChunk.stopInterpolation();
        return chunkAccess2;
    }

    private BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk, int i, int j, int k, BlockState blockState) {
        int l;
        int m;
        if (SharedConstants.DEBUG_AQUIFERS && k >= 0 && k % 4 == 0 && j == (m = (l = noiseChunk.preliminarySurfaceLevel(i, k)) + 8)) {
            blockState = m < this.getSeaLevel() ? Blocks.SLIME_BLOCK.defaultBlockState() : Blocks.HONEY_BLOCK.defaultBlockState();
        }
        return blockState;
    }

    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        if (this.settings.value().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxY()));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
    }
}

