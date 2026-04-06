/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

public class ComparatorBlock
extends DiodeBlock
implements EntityBlock {
    public static final MapCodec<ComparatorBlock> CODEC = ComparatorBlock.simpleCodec(ComparatorBlock::new);
    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

    public MapCodec<ComparatorBlock> codec() {
        return CODEC;
    }

    public ComparatorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(MODE, ComparatorMode.COMPARE));
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 2;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.DOWN && !this.canSurviveOn(levelReader, blockPos2, blockState2)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof ComparatorBlockEntity) {
            return ((ComparatorBlockEntity)blockEntity).getOutputSignal();
        }
        return 0;
    }

    private int calculateOutputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.getInputSignal(level, blockPos, blockState);
        if (i == 0) {
            return 0;
        }
        int j = this.getAlternateSignal(level, blockPos, blockState);
        if (j > i) {
            return 0;
        }
        if (blockState.getValue(MODE) == ComparatorMode.SUBTRACT) {
            return i - j;
        }
        return i;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.getInputSignal(level, blockPos, blockState);
        if (i == 0) {
            return false;
        }
        int j = this.getAlternateSignal(level, blockPos, blockState);
        if (i > j) {
            return true;
        }
        return i == j && blockState.getValue(MODE) == ComparatorMode.COMPARE;
    }

    @Override
    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        int i = super.getInputSignal(level, blockPos, blockState);
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState2.hasAnalogOutputSignal()) {
            i = blockState2.getAnalogOutputSignal(level, blockPos2, direction.getOpposite());
        } else if (i < 15 && blockState2.isRedstoneConductor(level, blockPos2)) {
            blockPos2 = blockPos2.relative(direction);
            blockState2 = level.getBlockState(blockPos2);
            ItemFrame itemFrame = this.getItemFrame(level, direction, blockPos2);
            int j = Math.max(itemFrame == null ? Integer.MIN_VALUE : itemFrame.getAnalogOutput(), blockState2.hasAnalogOutputSignal() ? blockState2.getAnalogOutputSignal(level, blockPos2, direction.getOpposite()) : Integer.MIN_VALUE);
            if (j != Integer.MIN_VALUE) {
                i = j;
            }
        }
        return i;
    }

    private @Nullable ItemFrame getItemFrame(Level level, Direction direction, BlockPos blockPos) {
        List<ItemFrame> list = level.getEntitiesOfClass(ItemFrame.class, new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1), itemFrame -> itemFrame.getDirection() == direction);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        float f = (blockState = (BlockState)blockState.cycle(MODE)).getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f;
        level.playSound((Entity)player, blockPos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, f);
        level.setBlock(blockPos, blockState, 2);
        this.refreshOutputState(level, blockPos, blockState);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        int j;
        if (level.getBlockTicks().willTickThisTick(blockPos, this)) {
            return;
        }
        int i = this.calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        int n = j = blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockEntity).getOutputSignal() : 0;
        if (i != j || blockState.getValue(POWERED).booleanValue() != this.shouldTurnOn(level, blockPos, blockState)) {
            TickPriority tickPriority = this.shouldPrioritize(level, blockPos, blockState) ? TickPriority.HIGH : TickPriority.NORMAL;
            level.scheduleTick(blockPos, this, 2, tickPriority);
        }
    }

    private void refreshOutputState(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        int j = 0;
        if (blockEntity instanceof ComparatorBlockEntity) {
            ComparatorBlockEntity comparatorBlockEntity = (ComparatorBlockEntity)blockEntity;
            j = comparatorBlockEntity.getOutputSignal();
            comparatorBlockEntity.setOutputSignal(i);
        }
        if (j != i || blockState.getValue(MODE) == ComparatorMode.COMPARE) {
            boolean bl = this.shouldTurnOn(level, blockPos, blockState);
            boolean bl2 = blockState.getValue(POWERED);
            if (bl2 && !bl) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, false), 2);
            } else if (!bl2 && bl) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, true), 2);
            }
            this.updateNeighborsInFront(level, blockPos, blockState);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.refreshOutputState(serverLevel, blockPos, blockState);
    }

    @Override
    protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        super.triggerEvent(blockState, level, blockPos, i, j);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return blockEntity != null && blockEntity.triggerEvent(i, j);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ComparatorBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE, POWERED);
    }
}

