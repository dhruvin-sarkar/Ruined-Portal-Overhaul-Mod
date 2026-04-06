/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class FireflyBushBlock
extends VegetationBlock
implements BonemealableBlock {
    private static final double FIREFLY_CHANCE_PER_TICK = 0.7;
    private static final double FIREFLY_HORIZONTAL_RANGE = 10.0;
    private static final double FIREFLY_VERTICAL_RANGE = 5.0;
    private static final int FIREFLY_SPAWN_MAX_BRIGHTNESS_LEVEL = 13;
    private static final int FIREFLY_AMBIENT_SOUND_CHANCE_ONE_IN = 30;
    public static final MapCodec<FireflyBushBlock> CODEC = FireflyBushBlock.simpleCodec(FireflyBushBlock::new);

    public FireflyBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends FireflyBushBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(30) == 0 && level.environmentAttributes().getValue(EnvironmentAttributes.FIREFLY_BUSH_SOUNDS, blockPos).booleanValue() && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos) <= blockPos.getY()) {
            level.playLocalSound(blockPos, SoundEvents.FIREFLY_BUSH_IDLE, SoundSource.AMBIENT, 1.0f, 1.0f, false);
        }
        if (level.getMaxLocalRawBrightness(blockPos) <= 13 && randomSource.nextDouble() <= 0.7) {
            double d = (double)blockPos.getX() + randomSource.nextDouble() * 10.0 - 5.0;
            double e = (double)blockPos.getY() + randomSource.nextDouble() * 5.0;
            double f = (double)blockPos.getZ() + randomSource.nextDouble() * 10.0 - 5.0;
            level.addParticle(ParticleTypes.FIREFLY, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return BonemealableBlock.hasSpreadableNeighbourPos(levelReader, blockPos, blockState);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos2, BlockState blockState) {
        BonemealableBlock.findSpreadableNeighbourPos(serverLevel, blockPos2, blockState).ifPresent(blockPos -> serverLevel.setBlockAndUpdate((BlockPos)blockPos, this.defaultBlockState()));
    }
}

