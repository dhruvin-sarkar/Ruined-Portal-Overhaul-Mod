/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature
extends Feature<TreeConfiguration> {
    @Block.UpdateFlags
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public TreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, FoliagePlacer.FoliageSetter foliageSetter, TreeConfiguration treeConfiguration) {
        int i = treeConfiguration.trunkPlacer.getTreeHeight(randomSource);
        int j = treeConfiguration.foliagePlacer.foliageHeight(randomSource, i, treeConfiguration);
        int k = i - j;
        int l = treeConfiguration.foliagePlacer.foliageRadius(randomSource, k);
        BlockPos blockPos2 = treeConfiguration.rootPlacer.map(rootPlacer -> rootPlacer.getTrunkOrigin(blockPos, randomSource)).orElse(blockPos);
        int m = Math.min(blockPos.getY(), blockPos2.getY());
        int n = Math.max(blockPos.getY(), blockPos2.getY()) + i + 1;
        if (m < worldGenLevel.getMinY() + 1 || n > worldGenLevel.getMaxY() + 1) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        int o = this.getMaxFreeTreeHeight(worldGenLevel, i, blockPos2, treeConfiguration);
        if (o < i && (optionalInt.isEmpty() || o < optionalInt.getAsInt())) {
            return false;
        }
        if (treeConfiguration.rootPlacer.isPresent() && !treeConfiguration.rootPlacer.get().placeRoots(worldGenLevel, biConsumer, randomSource, blockPos, blockPos2, treeConfiguration)) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, biConsumer2, randomSource, o, blockPos2, treeConfiguration);
        list.forEach(foliageAttachment -> treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, foliageSetter, randomSource, treeConfiguration, o, (FoliagePlacer.FoliageAttachment)foliageAttachment, j, l));
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j <= i + 1; ++j) {
            int k = treeConfiguration.minimumSize.getSizeAtHeight(i, j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    mutableBlockPos.setWithOffset(blockPos, l, j, m);
                    if (treeConfiguration.trunkPlacer.isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !TreeFeature.isVine(levelSimulatedReader, mutableBlockPos))) continue;
                    return j - 2;
                }
            }
        }
        return i;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
        final WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet set = Sets.newHashSet();
        HashSet set2 = Sets.newHashSet();
        final HashSet set3 = Sets.newHashSet();
        HashSet set4 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (blockPos, blockState) -> {
            set.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPos, blockState) -> {
            set2.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        FoliagePlacer.FoliageSetter foliageSetter = new FoliagePlacer.FoliageSetter(){

            @Override
            public void set(BlockPos blockPos, BlockState blockState) {
                set3.add(blockPos.immutable());
                worldGenLevel.setBlock(blockPos, blockState, 19);
            }

            @Override
            public boolean isSet(BlockPos blockPos) {
                return set3.contains(blockPos);
            }
        };
        BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPos, blockState) -> {
            set4.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos2, biConsumer, biConsumer2, foliageSetter, treeConfiguration);
        if (!bl || set2.isEmpty() && set3.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, biConsumer3, randomSource, set2, set3, set);
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(context));
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat((Iterable)set, (Iterable)set2, (Iterable)set3, (Iterable)set4)).map(boundingBox -> {
            DiscreteVoxelShape discreteVoxelShape = TreeFeature.updateLeaves(worldGenLevel, boundingBox, set2, set4, set);
            StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            return true;
        }).orElse(false);
    }

    /*
     * Unable to fully structure code
     */
    private static DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2, Set<BlockPos> set3) {
        discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
        i = 7;
        list = Lists.newArrayList();
        for (j = 0; j < 7; ++j) {
            list.add(Sets.newHashSet());
        }
        for (BlockPos blockPos : Lists.newArrayList((Iterable)Sets.union(set2, set3))) {
            if (!boundingBox.isInside(blockPos)) continue;
            discreteVoxelShape.fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
        }
        mutableBlockPos = new BlockPos.MutableBlockPos();
        k = 0;
        ((Set)list.get(0)).addAll(set);
        block2: while (true) {
            if (k < 7 && ((Set)list.get(k)).isEmpty()) {
                ++k;
                continue;
            }
            if (k >= 7) break;
            iterator = ((Set)list.get(k)).iterator();
            blockPos2 = (BlockPos)iterator.next();
            iterator.remove();
            if (!boundingBox.isInside(blockPos2)) continue;
            if (k != 0) {
                blockState = levelAccessor.getBlockState(blockPos2);
                TreeFeature.setBlockKnownShape(levelAccessor, blockPos2, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, k));
            }
            discreteVoxelShape.fill(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
            var12_14 = Direction.values();
            var13_15 = var12_14.length;
            var14_16 = 0;
            while (true) {
                if (var14_16 < var13_15) ** break;
                continue block2;
                direction = var12_14[var14_16];
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, direction);
                if (boundingBox.isInside(mutableBlockPos) && !discreteVoxelShape.isFull(l = mutableBlockPos.getX() - boundingBox.minX(), m = mutableBlockPos.getY() - boundingBox.minY(), n = mutableBlockPos.getZ() - boundingBox.minZ()) && !(optionalInt = LeavesBlock.getOptionalDistanceAt(blockState2 = levelAccessor.getBlockState(mutableBlockPos))).isEmpty() && (o = Math.min(optionalInt.getAsInt(), k + 1)) < 7) {
                    ((Set)list.get(o)).add(mutableBlockPos.immutable());
                    k = Math.min(k, o);
                }
                ++var14_16;
            }
            break;
        }
        return discreteVoxelShape;
    }

    public static List<BlockPos> getLowestTrunkOrRootOfTree(TreeDecorator.Context context) {
        ArrayList list = Lists.newArrayList();
        ObjectArrayList<BlockPos> list2 = context.roots();
        ObjectArrayList<BlockPos> list3 = context.logs();
        if (list2.isEmpty()) {
            list.addAll(list3);
        } else if (!list3.isEmpty() && ((BlockPos)list2.get(0)).getY() == ((BlockPos)list3.get(0)).getY()) {
            list.addAll(list3);
            list.addAll(list2);
        } else {
            list.addAll(list2);
        }
        return list;
    }
}

