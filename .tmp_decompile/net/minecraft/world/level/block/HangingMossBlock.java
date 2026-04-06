/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingMossBlock
extends Block
implements BonemealableBlock {
    public static final MapCodec<HangingMossBlock> CODEC = HangingMossBlock.simpleCodec(HangingMossBlock::new);
    private static final VoxelShape SHAPE_BASE = Block.column(14.0, 0.0, 16.0);
    private static final VoxelShape SHAPE_TIP = Block.column(14.0, 2.0, 16.0);
    public static final BooleanProperty TIP = BlockStateProperties.TIP;

    public MapCodec<HangingMossBlock> codec() {
        return CODEC;
    }

    public HangingMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(TIP, true));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(TIP) != false ? SHAPE_TIP : SHAPE_BASE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        BlockState blockState2;
        if (randomSource.nextInt(500) == 0 && ((blockState2 = level.getBlockState(blockPos.above())).is(BlockTags.PALE_OAK_LOGS) || blockState2.is(Blocks.PALE_OAK_LEAVES))) {
            level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.PALE_HANGING_MOSS_IDLE, SoundSource.AMBIENT, 1.0f, 1.0f, false);
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return this.canStayAtPosition(levelReader, blockPos);
    }

    private boolean canStayAtPosition(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState;
        BlockPos blockPos2 = blockPos.relative(Direction.UP);
        return MultifaceBlock.canAttachTo(blockGetter, Direction.UP, blockPos2, blockState = blockGetter.getBlockState(blockPos2)) || blockState.is(Blocks.PALE_HANGING_MOSS);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (!this.canStayAtPosition(levelReader, blockPos)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        return (BlockState)blockState.setValue(TIP, !levelReader.getBlockState(blockPos.below()).is(this));
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.canStayAtPosition(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return this.canGrowInto(levelReader.getBlockState(this.getTip(levelReader, blockPos).below()));
    }

    private boolean canGrowInto(BlockState blockState) {
        return blockState.isAir();
    }

    public BlockPos getTip(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        do {
            mutableBlockPos.move(Direction.DOWN);
        } while ((blockState = blockGetter.getBlockState(mutableBlockPos)).is(this));
        return ((BlockPos)mutableBlockPos.relative(Direction.UP)).immutable();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = this.getTip(serverLevel, blockPos).below();
        if (!this.canGrowInto(serverLevel.getBlockState(blockPos2))) {
            return;
        }
        serverLevel.setBlockAndUpdate(blockPos2, (BlockState)blockState.setValue(TIP, true));
    }
}

