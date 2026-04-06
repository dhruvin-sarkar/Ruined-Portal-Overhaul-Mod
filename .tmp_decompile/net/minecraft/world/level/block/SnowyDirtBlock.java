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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyDirtBlock
extends Block {
    public static final MapCodec<SnowyDirtBlock> CODEC = SnowyDirtBlock.simpleCodec(SnowyDirtBlock::new);
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    protected MapCodec<? extends SnowyDirtBlock> codec() {
        return CODEC;
    }

    protected SnowyDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SNOWY, false));
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.UP) {
            return (BlockState)blockState.setValue(SNOWY, SnowyDirtBlock.isSnowySetting(blockState2));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
        return (BlockState)this.defaultBlockState().setValue(SNOWY, SnowyDirtBlock.isSnowySetting(blockState));
    }

    protected static boolean isSnowySetting(BlockState blockState) {
        return blockState.is(BlockTags.SNOW);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SNOWY);
    }
}

