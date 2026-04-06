/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class StraightTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<StraightTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> StraightTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, StraightTrunkPlacer::new));

    public StraightTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        StraightTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos.below(), treeConfiguration);
        for (int j = 0; j < i; ++j) {
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos.above(j), treeConfiguration);
        }
        return ImmutableList.of((Object)new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, false));
    }
}

