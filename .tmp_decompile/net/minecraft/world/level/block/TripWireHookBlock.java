/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TripWireHookBlock
extends Block {
    public static final MapCodec<TripWireHookBlock> CODEC = TripWireHookBlock.simpleCodec(TripWireHookBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final int WIRE_DIST_MIN = 1;
    protected static final int WIRE_DIST_MAX = 42;
    private static final int RECHECK_PERIOD = 10;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(6.0, 0.0, 10.0, 10.0, 16.0));

    public MapCodec<TripWireHookBlock> codec() {
        return CODEC;
    }

    public TripWireHookBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(ATTACHED, false));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(FACING));
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return direction.getAxis().isHorizontal() && blockState2.isFaceSturdy(levelReader, blockPos2, direction);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction[] directions;
        BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(POWERED, false)).setValue(ATTACHED, false);
        Level levelReader = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : directions = blockPlaceContext.getNearestLookingDirections()) {
            Direction direction2;
            if (!direction.getAxis().isHorizontal() || !(blockState = (BlockState)blockState.setValue(FACING, direction2 = direction.getOpposite())).canSurvive(levelReader, blockPos)) continue;
            return blockState;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        TripWireHookBlock.calculateState(level, blockPos, blockState, false, false, -1, null);
    }

    public static void calculateState(Level level, BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2, int i, @Nullable BlockState blockState2) {
        BlockPos blockPos2;
        Optional<Direction> optional = blockState.getOptionalValue(FACING);
        if (!optional.isPresent()) {
            return;
        }
        Direction direction = optional.get();
        boolean bl3 = blockState.getOptionalValue(ATTACHED).orElse(false);
        boolean bl4 = blockState.getOptionalValue(POWERED).orElse(false);
        Block block = blockState.getBlock();
        boolean bl5 = !bl;
        boolean bl6 = false;
        int j = 0;
        BlockState[] blockStates = new BlockState[42];
        for (int k = 1; k < 42; ++k) {
            blockPos2 = blockPos.relative(direction, k);
            BlockState blockState3 = level.getBlockState(blockPos2);
            if (blockState3.is(Blocks.TRIPWIRE_HOOK)) {
                if (blockState3.getValue(FACING) != direction.getOpposite()) break;
                j = k;
                break;
            }
            if (blockState3.is(Blocks.TRIPWIRE) || k == i) {
                if (k == i) {
                    blockState3 = (BlockState)MoreObjects.firstNonNull((Object)blockState2, (Object)blockState3);
                }
                boolean bl7 = blockState3.getValue(TripWireBlock.DISARMED) == false;
                boolean bl8 = blockState3.getValue(TripWireBlock.POWERED);
                bl6 |= bl7 && bl8;
                blockStates[k] = blockState3;
                if (k != i) continue;
                level.scheduleTick(blockPos, block, 10);
                bl5 &= bl7;
                continue;
            }
            blockStates[k] = null;
            bl5 = false;
        }
        BlockState blockState4 = (BlockState)((BlockState)block.defaultBlockState().trySetValue(ATTACHED, bl5)).trySetValue(POWERED, bl6 &= (bl5 &= j > 1));
        if (j > 0) {
            blockPos2 = blockPos.relative(direction, j);
            Direction direction2 = direction.getOpposite();
            level.setBlock(blockPos2, (BlockState)blockState4.setValue(FACING, direction2), 3);
            TripWireHookBlock.notifyNeighbors(block, level, blockPos2, direction2);
            TripWireHookBlock.emitState(level, blockPos2, bl5, bl6, bl3, bl4);
        }
        TripWireHookBlock.emitState(level, blockPos, bl5, bl6, bl3, bl4);
        if (!bl) {
            level.setBlock(blockPos, (BlockState)blockState4.setValue(FACING, direction), 3);
            if (bl2) {
                TripWireHookBlock.notifyNeighbors(block, level, blockPos, direction);
            }
        }
        if (bl3 != bl5) {
            for (int l = 1; l < j; ++l) {
                BlockState blockState6;
                BlockPos blockPos3 = blockPos.relative(direction, l);
                BlockState blockState5 = blockStates[l];
                if (blockState5 == null || !(blockState6 = level.getBlockState(blockPos3)).is(Blocks.TRIPWIRE) && !blockState6.is(Blocks.TRIPWIRE_HOOK)) continue;
                level.setBlock(blockPos3, (BlockState)blockState5.trySetValue(ATTACHED, bl5), 3);
            }
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        TripWireHookBlock.calculateState(serverLevel, blockPos, blockState, false, true, -1, null);
    }

    private static void emitState(Level level, BlockPos blockPos, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        if (bl2 && !bl4) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4f, 0.6f);
            level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, blockPos);
        } else if (!bl2 && bl4) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4f, 0.5f);
            level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, blockPos);
        } else if (bl && !bl3) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4f, 0.7f);
            level.gameEvent(null, GameEvent.BLOCK_ATTACH, blockPos);
        } else if (!bl && bl3) {
            level.playSound(null, blockPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4f, 1.2f / (level.random.nextFloat() * 0.2f + 0.9f));
            level.gameEvent(null, GameEvent.BLOCK_DETACH, blockPos);
        }
    }

    private static void notifyNeighbors(Block block, Level level, BlockPos blockPos, Direction direction) {
        Direction direction2 = direction.getOpposite();
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction2, Direction.UP);
        level.updateNeighborsAt(blockPos, block, orientation);
        level.updateNeighborsAt(blockPos.relative(direction2), block, orientation);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (bl) {
            return;
        }
        boolean bl2 = blockState.getValue(ATTACHED);
        boolean bl3 = blockState.getValue(POWERED);
        if (bl2 || bl3) {
            TripWireHookBlock.calculateState(serverLevel, blockPos, blockState, true, false, -1, null);
        }
        if (bl3) {
            TripWireHookBlock.notifyNeighbors(this, serverLevel, blockPos, blockState.getValue(FACING));
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return 0;
        }
        if (blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, ATTACHED);
    }
}

