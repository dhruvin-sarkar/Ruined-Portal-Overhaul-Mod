/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PistonBaseBlock
extends DirectionalBlock {
    public static final MapCodec<PistonBaseBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("sticky").forGetter(pistonBaseBlock -> pistonBaseBlock.isSticky), PistonBaseBlock.propertiesCodec()).apply((Applicative)instance, PistonBaseBlock::new));
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final int TRIGGER_EXTEND = 0;
    public static final int TRIGGER_CONTRACT = 1;
    public static final int TRIGGER_DROP = 2;
    public static final int PLATFORM_THICKNESS = 4;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Block.boxZ(16.0, 4.0, 16.0));
    private final boolean isSticky;

    public MapCodec<PistonBaseBlock> codec() {
        return CODEC;
    }

    public PistonBaseBlock(boolean bl, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(EXTENDED, false));
        this.isSticky = bl;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(EXTENDED).booleanValue()) {
            return SHAPES.get(blockState.getValue(FACING));
        }
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (!level.isClientSide()) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide()) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (!level.isClientSide() && level.getBlockEntity(blockPos) == null) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite())).setValue(EXTENDED, false);
    }

    private void checkIfExtend(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        boolean bl = this.getNeighborSignal(level, blockPos, direction);
        if (bl && !blockState.getValue(EXTENDED).booleanValue()) {
            if (new PistonStructureResolver(level, blockPos, direction, true).resolve()) {
                level.blockEvent(blockPos, this, 0, direction.get3DDataValue());
            }
        } else if (!bl && blockState.getValue(EXTENDED).booleanValue()) {
            PistonMovingBlockEntity pistonMovingBlockEntity;
            BlockEntity blockEntity;
            BlockPos blockPos2 = blockPos.relative(direction, 2);
            BlockState blockState2 = level.getBlockState(blockPos2);
            int i = 1;
            if (blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == direction && (blockEntity = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity).isExtending() && (pistonMovingBlockEntity.getProgress(0.0f) < 0.5f || level.getGameTime() == pistonMovingBlockEntity.getLastTicked() || ((ServerLevel)level).isHandlingTick())) {
                i = 2;
            }
            level.blockEvent(blockPos, this, i, direction.get3DDataValue());
        }
    }

    private boolean getNeighborSignal(SignalGetter signalGetter, BlockPos blockPos, Direction direction) {
        for (Direction direction2 : Direction.values()) {
            if (direction2 == direction || !signalGetter.hasSignal(blockPos.relative(direction2), direction2)) continue;
            return true;
        }
        if (signalGetter.hasSignal(blockPos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos2 = blockPos.above();
        for (Direction direction3 : Direction.values()) {
            if (direction3 == Direction.DOWN || !signalGetter.hasSignal(blockPos2.relative(direction3), direction3)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockState blockState2 = (BlockState)blockState.setValue(EXTENDED, true);
        if (!level.isClientSide()) {
            boolean bl = this.getNeighborSignal(level, blockPos, direction);
            if (bl && (i == 1 || i == 2)) {
                level.setBlock(blockPos, blockState2, 2);
                return false;
            }
            if (!bl && i == 0) {
                return false;
            }
        }
        if (i == 0) {
            if (!this.moveBlocks(level, blockPos, direction, true)) return false;
            level.setBlock(blockPos, blockState2, 67);
            level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.25f + 0.6f);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(blockState2));
            return true;
        } else {
            if (i != 1 && i != 2) return true;
            BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
            if (blockEntity instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)blockEntity).finalTick();
            }
            BlockState blockState3 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            level.setBlock(blockPos, blockState3, 276);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos, blockState3, (BlockState)this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(j & 7)), direction, false, true));
            level.updateNeighborsAt(blockPos, blockState3.getBlock());
            blockState3.updateNeighbourShapes(level, blockPos, 2);
            if (this.isSticky) {
                PistonMovingBlockEntity pistonMovingBlockEntity;
                BlockEntity blockEntity2;
                BlockPos blockPos2 = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
                BlockState blockState4 = level.getBlockState(blockPos2);
                boolean bl2 = false;
                if (blockState4.is(Blocks.MOVING_PISTON) && (blockEntity2 = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity2).getDirection() == direction && pistonMovingBlockEntity.isExtending()) {
                    pistonMovingBlockEntity.finalTick();
                    bl2 = true;
                }
                if (!bl2) {
                    if (i == 1 && !blockState4.isAir() && PistonBaseBlock.isPushable(blockState4, level, blockPos2, direction.getOpposite(), false, direction) && (blockState4.getPistonPushReaction() == PushReaction.NORMAL || blockState4.is(Blocks.PISTON) || blockState4.is(Blocks.STICKY_PISTON))) {
                        this.moveBlocks(level, blockPos, direction, false);
                    } else {
                        level.removeBlock(blockPos.relative(direction), false);
                    }
                }
            } else {
                level.removeBlock(blockPos.relative(direction), false);
            }
            level.playSound(null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.15f + 0.6f);
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockPos, GameEvent.Context.of(blockState3));
        }
        return true;
    }

    public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2) {
        if (blockPos.getY() < level.getMinY() || blockPos.getY() > level.getMaxY() || !level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        if (blockState.isAir()) {
            return true;
        }
        if (blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.CRYING_OBSIDIAN) || blockState.is(Blocks.RESPAWN_ANCHOR) || blockState.is(Blocks.REINFORCED_DEEPSLATE)) {
            return false;
        }
        if (direction == Direction.DOWN && blockPos.getY() == level.getMinY()) {
            return false;
        }
        if (direction == Direction.UP && blockPos.getY() == level.getMaxY()) {
            return false;
        }
        if (blockState.is(Blocks.PISTON) || blockState.is(Blocks.STICKY_PISTON)) {
            if (blockState.getValue(EXTENDED).booleanValue()) {
                return false;
            }
        } else {
            if (blockState.getDestroySpeed(level, blockPos) == -1.0f) {
                return false;
            }
            switch (blockState.getPistonPushReaction()) {
                case BLOCK: {
                    return false;
                }
                case DESTROY: {
                    return bl;
                }
                case PUSH_ONLY: {
                    return direction == direction2;
                }
            }
        }
        return !blockState.hasBlockEntity();
    }

    /*
     * WARNING - void declaration
     */
    private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl) {
        void var16_30;
        void var16_28;
        BlockState blockState3;
        BlockPos blockPos4;
        int j;
        PistonStructureResolver pistonStructureResolver;
        BlockPos blockPos2 = blockPos.relative(direction);
        if (!bl && level.getBlockState(blockPos2).is(Blocks.PISTON_HEAD)) {
            level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 276);
        }
        if (!(pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, bl)).resolve()) {
            return false;
        }
        HashMap map = Maps.newHashMap();
        List<BlockPos> list = pistonStructureResolver.getToPush();
        ArrayList list2 = Lists.newArrayList();
        for (BlockPos blockPos3 : list) {
            BlockState blockState = level.getBlockState(blockPos3);
            list2.add(blockState);
            map.put(blockPos3, blockState);
        }
        List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
        BlockState[] blockStates = new BlockState[list.size() + list3.size()];
        Direction direction2 = bl ? direction : direction.getOpposite();
        int i = 0;
        for (j = list3.size() - 1; j >= 0; --j) {
            blockPos4 = list3.get(j);
            BlockState blockState = level.getBlockState(blockPos4);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos4) : null;
            PistonBaseBlock.dropResources(blockState, level, blockPos4, blockEntity);
            if (!blockState.is(BlockTags.FIRE) && level.isClientSide()) {
                level.levelEvent(2001, blockPos4, PistonBaseBlock.getId(blockState));
            }
            level.setBlock(blockPos4, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos4, GameEvent.Context.of(blockState));
            blockStates[i++] = blockState;
        }
        for (j = list.size() - 1; j >= 0; --j) {
            blockPos4 = list.get(j);
            BlockState blockState = level.getBlockState(blockPos4);
            blockPos4 = blockPos4.relative(direction2);
            map.remove(blockPos4);
            blockState3 = (BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
            level.setBlock(blockPos4, blockState3, 324);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos4, blockState3, (BlockState)list2.get(j), direction, bl, false));
            blockStates[i++] = blockState;
        }
        if (bl) {
            PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockState4 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction)).setValue(PistonHeadBlock.TYPE, pistonType);
            BlockState blockState = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockPos2);
            level.setBlock(blockPos2, blockState, 324);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos2, blockState, blockState4, direction, true, true));
        }
        BlockState blockState5 = Blocks.AIR.defaultBlockState();
        for (BlockPos blockPos3 : map.keySet()) {
            level.setBlock(blockPos3, blockState5, 82);
        }
        for (Map.Entry entry : map.entrySet()) {
            BlockPos blockPos6 = (BlockPos)entry.getKey();
            BlockState blockState6 = (BlockState)entry.getValue();
            blockState6.updateIndirectNeighbourShapes(level, blockPos6, 2);
            blockState5.updateNeighbourShapes(level, blockPos6, 2);
            blockState5.updateIndirectNeighbourShapes(level, blockPos6, 2);
        }
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, pistonStructureResolver.getPushDirection(), null);
        i = 0;
        int n = list3.size() - 1;
        while (var16_28 >= 0) {
            blockState3 = blockStates[i++];
            BlockPos blockPos7 = list3.get((int)var16_28);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                blockState3.affectNeighborsAfterRemoval(serverLevel, blockPos7, false);
            }
            blockState3.updateIndirectNeighbourShapes(level, blockPos7, 2);
            level.updateNeighborsAt(blockPos7, blockState3.getBlock(), orientation);
            --var16_28;
        }
        int n2 = list.size() - 1;
        while (var16_30 >= 0) {
            level.updateNeighborsAt(list.get((int)var16_30), blockStates[i++].getBlock(), orientation);
            --var16_30;
        }
        if (bl) {
            level.updateNeighborsAt(blockPos2, Blocks.PISTON_HEAD, orientation);
        }
        return true;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(EXTENDED);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

