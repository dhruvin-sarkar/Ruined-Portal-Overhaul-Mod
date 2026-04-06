/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
    public boolean isValidBonemealTarget(LevelReader var1, BlockPos var2, BlockState var3);

    public boolean isBonemealSuccess(Level var1, RandomSource var2, BlockPos var3, BlockState var4);

    public void performBonemeal(ServerLevel var1, RandomSource var2, BlockPos var3, BlockState var4);

    public static boolean hasSpreadableNeighbourPos(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return BonemealableBlock.getSpreadableNeighbourPos(Direction.Plane.HORIZONTAL.stream().toList(), levelReader, blockPos, blockState).isPresent();
    }

    public static Optional<BlockPos> findSpreadableNeighbourPos(Level level, BlockPos blockPos, BlockState blockState) {
        return BonemealableBlock.getSpreadableNeighbourPos(Direction.Plane.HORIZONTAL.shuffledCopy(level.random), level, blockPos, blockState);
    }

    private static Optional<BlockPos> getSpreadableNeighbourPos(List<Direction> list, LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        for (Direction direction : list) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!levelReader.isEmptyBlock(blockPos2) || !blockState.canSurvive(levelReader, blockPos2)) continue;
            return Optional.of(blockPos2);
        }
        return Optional.empty();
    }

    default public BlockPos getParticlePos(BlockPos blockPos) {
        return switch (this.getType().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> blockPos.above();
            case 1 -> blockPos;
        };
    }

    default public Type getType() {
        return Type.GROWER;
    }

    public static enum Type {
        NEIGHBOR_SPREADER,
        GROWER;

    }
}

