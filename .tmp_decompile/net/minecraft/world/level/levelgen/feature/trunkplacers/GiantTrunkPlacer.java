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

public class GiantTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> GiantTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, GiantTrunkPlacer::new));

    public GiantTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.GIANT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2 = blockPos.below();
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.east(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south(), treeConfiguration);
        GiantTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south().east(), treeConfiguration);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < i; ++j) {
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 0, j, 0);
            if (j >= i - 1) continue;
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 1, j, 0);
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 1, j, 1);
            this.placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, blockPos, 0, j, 1);
        }
        return ImmutableList.of((Object)new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, true));
    }

    private void placeLogIfFreeWithOffset(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, int j, int k) {
        mutableBlockPos.setWithOffset(blockPos, i, j, k);
        this.placeLogIfFree(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration);
    }
}

