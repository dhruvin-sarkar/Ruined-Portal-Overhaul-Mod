/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TrapDoorBlock
extends HorizontalDirectionalBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<TrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockSetType.CODEC.fieldOf("block_set_type").forGetter(trapDoorBlock -> trapDoorBlock.type), TrapDoorBlock.propertiesCodec()).apply((Applicative)instance, TrapDoorBlock::new));
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Block.boxZ(16.0, 13.0, 16.0));
    private final BlockSetType type;

    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    protected TrapDoorBlock(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
        super(properties.sound(blockSetType.soundType()));
        this.type = blockSetType;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(OPEN, false)).setValue(HALF, Half.BOTTOM)).setValue(POWERED, false)).setValue(WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(OPEN) != false ? blockState.getValue(FACING) : (blockState.getValue(HALF) == Half.TOP ? Direction.DOWN : Direction.UP));
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND: {
                return blockState.getValue(OPEN);
            }
            case WATER: {
                return blockState.getValue(WATERLOGGED);
            }
            case AIR: {
                return blockState.getValue(OPEN);
            }
        }
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!this.type.canOpenByHand()) {
            return InteractionResult.PASS;
        }
        this.toggle(blockState, level, blockPos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onExplosionHit(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (explosion.canTriggerBlocks() && this.type.canOpenByWindCharge() && !blockState.getValue(POWERED).booleanValue()) {
            this.toggle(blockState, serverLevel, blockPos, null);
        }
        super.onExplosionHit(blockState, serverLevel, blockPos, explosion, biConsumer);
    }

    private void toggle(BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player) {
        BlockState blockState2 = (BlockState)blockState.cycle(OPEN);
        level.setBlock(blockPos, blockState2, 2);
        if (blockState2.getValue(WATERLOGGED).booleanValue()) {
            level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        this.playSound(player, level, blockPos, blockState2.getValue(OPEN));
    }

    protected void playSound(@Nullable Player player, Level level, BlockPos blockPos, boolean bl) {
        level.playSound((Entity)player, blockPos, bl ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        level.gameEvent((Entity)player, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.isClientSide()) {
            return;
        }
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (bl2 != blockState.getValue(POWERED)) {
            if (blockState.getValue(OPEN) != bl2) {
                blockState = (BlockState)blockState.setValue(OPEN, bl2);
                this.playSound(null, level, blockPos, bl2);
            }
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl2), 2);
            if (blockState.getValue(WATERLOGGED).booleanValue()) {
                level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = this.defaultBlockState();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        Direction direction = blockPlaceContext.getClickedFace();
        blockState = blockPlaceContext.replacingClickedOnBlock() || !direction.getAxis().isHorizontal() ? (BlockState)((BlockState)blockState.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP) : (BlockState)((BlockState)blockState.setValue(FACING, direction)).setValue(HALF, blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
        if (blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())) {
            blockState = (BlockState)((BlockState)blockState.setValue(OPEN, true)).setValue(POWERED, true);
        }
        return (BlockState)blockState.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    protected BlockSetType getType() {
        return this.type;
    }
}

