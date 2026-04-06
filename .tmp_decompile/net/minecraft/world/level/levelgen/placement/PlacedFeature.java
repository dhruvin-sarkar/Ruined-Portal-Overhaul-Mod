/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
    public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ConfiguredFeature.CODEC.fieldOf("feature").forGetter(placedFeature -> placedFeature.feature), (App)PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(placedFeature -> placedFeature.placement)).apply((Applicative)instance, PlacedFeature::new));
    public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC, true).listOf();

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.empty()), randomSource, blockPos);
    }

    public boolean placeWithBiomeCheck(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource randomSource, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.of(this)), randomSource, blockPos);
    }

    private boolean placeWithContext(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos2) {
        Stream<BlockPos> stream = Stream.of(blockPos2);
        for (PlacementModifier placementModifier : this.placement) {
            stream = stream.flatMap(blockPos -> placementModifier.getPositions(placementContext, randomSource, (BlockPos)blockPos));
        }
        ConfiguredFeature<?, ?> configuredFeature = this.feature.value();
        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(blockPos -> {
            if (configuredFeature.place(placementContext.getLevel(), placementContext.generator(), randomSource, (BlockPos)blockPos)) {
                mutableBoolean.setTrue();
                if (SharedConstants.DEBUG_FEATURE_COUNT) {
                    FeatureCountTracker.featurePlaced(placementContext.getLevel().getLevel(), configuredFeature, placementContext.topFeature());
                }
            }
        });
        return mutableBoolean.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.value().getFeatures();
    }

    public String toString() {
        return "Placed " + String.valueOf(this.feature);
    }
}

