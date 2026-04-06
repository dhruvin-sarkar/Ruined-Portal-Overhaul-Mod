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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class PaleMossDecorator
extends TreeDecorator {
    public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("leaves_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.leavesProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("trunk_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.trunkProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("ground_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.groundProbability))).apply((Applicative)instance, PaleMossDecorator::new));
    private final float leavesProbability;
    private final float trunkProbability;
    private final float groundProbability;

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.PALE_MOSS;
    }

    public PaleMossDecorator(float f, float g, float h) {
        this.leavesProbability = f;
        this.trunkProbability = g;
        this.groundProbability = h;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        WorldGenLevel worldGenLevel = (WorldGenLevel)context.level();
        List<BlockPos> list = Util.shuffledCopy(context.logs(), randomSource);
        if (list.isEmpty()) {
            return;
        }
        BlockPos blockPos2 = Collections.min(list, Comparator.comparingInt(Vec3i::getY));
        if (randomSource.nextFloat() < this.groundProbability) {
            worldGenLevel.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap(registry -> registry.get(VegetationFeatures.PALE_MOSS_PATCH)).ifPresent(reference -> ((ConfiguredFeature)((Object)((Object)reference.value()))).place(worldGenLevel, worldGenLevel.getLevel().getChunkSource().getGenerator(), randomSource, blockPos2.above()));
        }
        context.logs().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextFloat() < this.trunkProbability && context.isAir(blockPos2 = blockPos.below())) {
                PaleMossDecorator.addMossHanger(blockPos2, context);
            }
        });
        context.leaves().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextFloat() < this.leavesProbability && context.isAir(blockPos2 = blockPos.below())) {
                PaleMossDecorator.addMossHanger(blockPos2, context);
            }
        });
    }

    private static void addMossHanger(BlockPos blockPos, TreeDecorator.Context context) {
        while (context.isAir(blockPos.below()) && !((double)context.random().nextFloat() < 0.5)) {
            context.setBlock(blockPos, (BlockState)Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
            blockPos = blockPos.below();
        }
        context.setBlock(blockPos, (BlockState)Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
    }
}

