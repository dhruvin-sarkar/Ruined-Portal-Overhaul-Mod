/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTicks;
import org.jspecify.annotations.Nullable;

public class DriedGhastBlock
extends HorizontalDirectionalBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<DriedGhastBlock> CODEC = DriedGhastBlock.simpleCodec(DriedGhastBlock::new);
    public static final int MAX_HYDRATION_LEVEL = 3;
    public static final IntegerProperty HYDRATION_LEVEL = BlockStateProperties.DRIED_GHAST_HYDRATION_LEVELS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final int HYDRATION_TICK_DELAY = 5000;
    private static final VoxelShape SHAPE = Block.column(10.0, 10.0, 0.0, 10.0);

    public MapCodec<DriedGhastBlock> codec() {
        return CODEC;
    }

    public DriedGhastBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(HYDRATION_LEVEL, 0)).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HYDRATION_LEVEL, WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    public int getHydrationLevel(BlockState blockState) {
        return blockState.getValue(HYDRATION_LEVEL);
    }

    private boolean isReadyToSpawn(BlockState blockState) {
        return this.getHydrationLevel(blockState) == 3;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            this.tickWaterlogged(blockState, serverLevel, blockPos, randomSource);
            return;
        }
        int i = this.getHydrationLevel(blockState);
        if (i > 0) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(HYDRATION_LEVEL, i - 1), 2);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState));
        }
    }

    private void tickWaterlogged(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.isReadyToSpawn(blockState)) {
            serverLevel.playSound(null, blockPos, SoundEvents.DRIED_GHAST_TRANSITION, SoundSource.BLOCKS, 1.0f, 1.0f);
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(HYDRATION_LEVEL, this.getHydrationLevel(blockState) + 1), 2);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState));
        } else {
            this.spawnGhastling(serverLevel, blockPos, blockState);
        }
    }

    private void spawnGhastling(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        serverLevel.removeBlock(blockPos, false);
        HappyGhast happyGhast = EntityType.HAPPY_GHAST.create(serverLevel, EntitySpawnReason.BREEDING);
        if (happyGhast != null) {
            Vec3 vec3 = blockPos.getBottomCenter();
            happyGhast.setBaby(true);
            float f = Direction.getYRot((Direction)blockState.getValue(FACING));
            happyGhast.setYHeadRot(f);
            happyGhast.snapTo(vec3.x(), vec3.y(), vec3.z(), f, 0.0f);
            serverLevel.addFreshEntity(happyGhast);
            serverLevel.playSound(null, happyGhast, SoundEvents.GHASTLING_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getY() + 0.5;
        double f = (double)blockPos.getZ() + 0.5;
        if (!blockState.getValue(WATERLOGGED).booleanValue()) {
            if (randomSource.nextInt(40) == 0 && level.getBlockState(blockPos.below()).is(BlockTags.TRIGGERS_AMBIENT_DRIED_GHAST_BLOCK_SOUNDS)) {
                level.playLocalSound(d, e, f, SoundEvents.DRIED_GHAST_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
            if (randomSource.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.WHITE_SMOKE, d, e, f, 0.0, 0.02, 0.0);
            }
        } else {
            if (randomSource.nextInt(40) == 0) {
                level.playLocalSound(d, e, f, SoundEvents.DRIED_GHAST_AMBIENT_WATER, SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
            if (randomSource.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, d + (double)((randomSource.nextFloat() * 2.0f - 1.0f) / 3.0f), e + 0.4, f + (double)((randomSource.nextFloat() * 2.0f - 1.0f) / 3.0f), 0.0, randomSource.nextFloat(), 0.0);
            }
        }
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if ((blockState.getValue(WATERLOGGED).booleanValue() || blockState.getValue(HYDRATION_LEVEL) > 0) && !((LevelTicks)serverLevel.getBlockTicks()).hasScheduledTick(blockPos, this)) {
            serverLevel.scheduleTick(blockPos, this, 5000);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        return (BlockState)((BlockState)super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl)).setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue() || fluidState.getType() != Fluids.WATER) {
            return false;
        }
        if (!levelAccessor.isClientSide()) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, true), 3);
            levelAccessor.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            levelAccessor.playSound(null, blockPos, SoundEvents.DRIED_GHAST_PLACE_IN_WATER, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        level.playSound(null, blockPos, blockState.getValue(WATERLOGGED) != false ? SoundEvents.DRIED_GHAST_PLACE_IN_WATER : SoundEvents.DRIED_GHAST_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

