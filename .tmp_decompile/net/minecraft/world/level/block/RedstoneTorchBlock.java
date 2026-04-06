/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class RedstoneTorchBlock
extends BaseTorchBlock {
    public static final MapCodec<RedstoneTorchBlock> CODEC = RedstoneTorchBlock.simpleCodec(RedstoneTorchBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter, List<Toggle>> RECENT_TOGGLES = new WeakHashMap<BlockGetter, List<Toggle>>();
    public static final int RECENT_TOGGLE_TIMER = 60;
    public static final int MAX_RECENT_TOGGLES = 8;
    public static final int RESTART_DELAY = 160;
    private static final int TOGGLE_DELAY = 2;

    public MapCodec<? extends RedstoneTorchBlock> codec() {
        return CODEC;
    }

    protected RedstoneTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, true));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.notifyNeighbors(level, blockPos, blockState);
    }

    private void notifyNeighbors(Level level, BlockPos blockPos, BlockState blockState) {
        Orientation orientation = this.randomOrientation(level, blockState);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this, ExperimentalRedstoneUtils.withFront(orientation, direction));
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (!bl) {
            this.notifyNeighbors(serverLevel, blockPos, blockState);
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(LIT).booleanValue() && Direction.UP != direction) {
            return 15;
        }
        return 0;
    }

    protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
        return level.hasSignal(blockPos.below(), Direction.DOWN);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        boolean bl = this.hasNeighborSignal(serverLevel, blockPos, blockState);
        List<Toggle> list = RECENT_TOGGLES.get(serverLevel);
        while (list != null && !list.isEmpty() && serverLevel.getGameTime() - list.get((int)0).when > 60L) {
            list.remove(0);
        }
        if (blockState.getValue(LIT).booleanValue()) {
            if (bl) {
                serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, false), 3);
                if (RedstoneTorchBlock.isToggledTooFrequently(serverLevel, blockPos, true)) {
                    serverLevel.levelEvent(1502, blockPos, 0);
                    serverLevel.scheduleTick(blockPos, serverLevel.getBlockState(blockPos).getBlock(), 160);
                }
            }
        } else if (!bl && !RedstoneTorchBlock.isToggledTooFrequently(serverLevel, blockPos, false)) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, true), 3);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (blockState.getValue(LIT).booleanValue() == this.hasNeighborSignal(level, blockPos, blockState) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            level.scheduleTick(blockPos, this, 2);
        }
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.DOWN) {
            return blockState.getSignal(blockGetter, blockPos, direction);
        }
        return 0;
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.getValue(LIT).booleanValue()) {
            return;
        }
        double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
        double e = (double)blockPos.getY() + 0.7 + (randomSource.nextDouble() - 0.5) * 0.2;
        double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
        level.addParticle(DustParticleOptions.REDSTONE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    private static boolean isToggledTooFrequently(Level level, BlockPos blockPos, boolean bl) {
        List list = RECENT_TOGGLES.computeIfAbsent(level, blockGetter -> Lists.newArrayList());
        if (bl) {
            list.add(new Toggle(blockPos.immutable(), level.getGameTime()));
        }
        int i = 0;
        for (Toggle toggle : list) {
            if (!toggle.pos.equals(blockPos) || ++i < 8) continue;
            return true;
        }
        return false;
    }

    protected @Nullable Orientation randomOrientation(Level level, BlockState blockState) {
        return ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);
    }

    public static class Toggle {
        final BlockPos pos;
        final long when;

        public Toggle(BlockPos blockPos, long l) {
            this.pos = blockPos;
            this.when = l;
        }
    }
}

