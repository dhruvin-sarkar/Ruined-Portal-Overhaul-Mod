/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AttachedToLogsDecorator
extends TreeDecorator {
    public static final MapCodec<AttachedToLogsDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(attachedToLogsDecorator -> Float.valueOf(attachedToLogsDecorator.probability)), (App)BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(attachedToLogsDecorator -> attachedToLogsDecorator.blockProvider), (App)ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(attachedToLogsDecorator -> attachedToLogsDecorator.directions)).apply((Applicative)instance, AttachedToLogsDecorator::new));
    private final float probability;
    private final BlockStateProvider blockProvider;
    private final List<Direction> directions;

    public AttachedToLogsDecorator(float f, BlockStateProvider blockStateProvider, List<Direction> list) {
        this.probability = f;
        this.blockProvider = blockStateProvider;
        this.directions = list;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        for (BlockPos blockPos : Util.shuffledCopy(context.logs(), randomSource)) {
            Direction direction = Util.getRandom(this.directions, randomSource);
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!(randomSource.nextFloat() <= this.probability) || !context.isAir(blockPos2)) continue;
            context.setBlock(blockPos2, this.blockProvider.getState(randomSource, blockPos2));
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ATTACHED_TO_LOGS;
    }
}

