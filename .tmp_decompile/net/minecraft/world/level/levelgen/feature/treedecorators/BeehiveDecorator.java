/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class BeehiveDecorator
extends TreeDecorator {
    public static final MapCodec<BeehiveDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(BeehiveDecorator::new, beehiveDecorator -> Float.valueOf(beehiveDecorator.probability));
    private static final Direction WORLDGEN_FACING = Direction.SOUTH;
    private static final Direction[] SPAWN_DIRECTIONS = (Direction[])Direction.Plane.HORIZONTAL.stream().filter(direction -> direction != WORLDGEN_FACING.getOpposite()).toArray(Direction[]::new);
    private final float probability;

    public BeehiveDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        ObjectArrayList<BlockPos> list = context.leaves();
        ObjectArrayList<BlockPos> list2 = context.logs();
        if (list2.isEmpty()) {
            return;
        }
        RandomSource randomSource = context.random();
        if (randomSource.nextFloat() >= this.probability) {
            return;
        }
        int i = !list.isEmpty() ? Math.max(((BlockPos)list.getFirst()).getY() - 1, ((BlockPos)list2.getFirst()).getY() + 1) : Math.min(((BlockPos)list2.getFirst()).getY() + 1 + randomSource.nextInt(3), ((BlockPos)list2.getLast()).getY());
        List list3 = list2.stream().filter(blockPos -> blockPos.getY() == i).flatMap(blockPos -> Stream.of(SPAWN_DIRECTIONS).map(blockPos::relative)).collect(Collectors.toList());
        if (list3.isEmpty()) {
            return;
        }
        Util.shuffle(list3, randomSource);
        Optional<BlockPos> optional = list3.stream().filter(blockPos -> context.isAir((BlockPos)blockPos) && context.isAir(blockPos.relative(WORLDGEN_FACING))).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        context.setBlock(optional.get(), (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
        context.level().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent(beehiveBlockEntity -> {
            int i = 2 + randomSource.nextInt(2);
            for (int j = 0; j < i; ++j) {
                beehiveBlockEntity.storeBee(BeehiveBlockEntity.Occupant.create(randomSource.nextInt(599)));
            }
        });
    }
}

