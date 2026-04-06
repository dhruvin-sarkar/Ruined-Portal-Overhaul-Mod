/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SculkVeinBlock
extends MultifaceSpreadeableBlock
implements SculkBehaviour {
    public static final MapCodec<SculkVeinBlock> CODEC = SculkVeinBlock.simpleCodec(SculkVeinBlock::new);
    private final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinSpreaderConfig(this, MultifaceSpreader.DEFAULT_SPREAD_ORDER));
    private final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(new SculkVeinSpreaderConfig(this, MultifaceSpreader.SpreadType.SAME_POSITION));

    public MapCodec<SculkVeinBlock> codec() {
        return CODEC;
    }

    public SculkVeinBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.veinSpreader;
    }

    public MultifaceSpreader getSameSpaceSpreader() {
        return this.sameSpaceSpreader;
    }

    public static boolean regrow(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Collection<Direction> collection) {
        boolean bl = false;
        BlockState blockState2 = Blocks.SCULK_VEIN.defaultBlockState();
        for (Direction direction : collection) {
            if (!SculkVeinBlock.canAttachTo(levelAccessor, blockPos, direction)) continue;
            blockState2 = (BlockState)blockState2.setValue(SculkVeinBlock.getFaceProperty(direction), true);
            bl = true;
        }
        if (!bl) {
            return false;
        }
        if (!blockState.getFluidState().isEmpty()) {
            blockState2 = (BlockState)blockState2.setValue(MultifaceBlock.WATERLOGGED, true);
        }
        levelAccessor.setBlock(blockPos, blockState2, 3);
        return true;
    }

    @Override
    public void onDischarged(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.is(this)) {
            return;
        }
        for (Direction direction : DIRECTIONS) {
            BooleanProperty booleanProperty = SculkVeinBlock.getFaceProperty(direction);
            if (!blockState.getValue(booleanProperty).booleanValue() || !levelAccessor.getBlockState(blockPos.relative(direction)).is(Blocks.SCULK)) continue;
            blockState = (BlockState)blockState.setValue(booleanProperty, false);
        }
        if (!SculkVeinBlock.hasAnyFace(blockState)) {
            FluidState fluidState = levelAccessor.getFluidState(blockPos);
            blockState = (fluidState.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
        }
        levelAccessor.setBlock(blockPos, blockState, 3);
        SculkBehaviour.super.onDischarged(levelAccessor, blockState, blockPos, randomSource);
    }

    @Override
    public int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, SculkSpreader sculkSpreader, boolean bl) {
        if (bl && this.attemptPlaceSculk(sculkSpreader, levelAccessor, chargeCursor.getPos(), randomSource)) {
            return chargeCursor.getCharge() - 1;
        }
        return randomSource.nextInt(sculkSpreader.chargeDecayRate()) == 0 ? Mth.floor((float)chargeCursor.getCharge() * 0.5f) : chargeCursor.getCharge();
    }

    private boolean attemptPlaceSculk(SculkSpreader sculkSpreader, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        TagKey<Block> tagKey = sculkSpreader.replaceableBlocks();
        for (Direction direction : Direction.allShuffled(randomSource)) {
            BlockPos blockPos2;
            BlockState blockState2;
            if (!SculkVeinBlock.hasFace(blockState, direction) || !(blockState2 = levelAccessor.getBlockState(blockPos2 = blockPos.relative(direction))).is(tagKey)) continue;
            BlockState blockState3 = Blocks.SCULK.defaultBlockState();
            levelAccessor.setBlock(blockPos2, blockState3, 3);
            Block.pushEntitiesUp(blockState2, blockState3, levelAccessor, blockPos2);
            levelAccessor.playSound(null, blockPos2, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.veinSpreader.spreadAll(blockState3, levelAccessor, blockPos2, sculkSpreader.isWorldGeneration());
            Direction direction2 = direction.getOpposite();
            for (Direction direction3 : DIRECTIONS) {
                BlockPos blockPos3;
                BlockState blockState4;
                if (direction3 == direction2 || !(blockState4 = levelAccessor.getBlockState(blockPos3 = blockPos2.relative(direction3))).is(this)) continue;
                this.onDischarged(levelAccessor, blockState4, blockPos3, randomSource);
            }
            return true;
        }
        return false;
    }

    public static boolean hasSubstrateAccess(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos) {
        if (!blockState.is(Blocks.SCULK_VEIN)) {
            return false;
        }
        for (Direction direction : DIRECTIONS) {
            if (!SculkVeinBlock.hasFace(blockState, direction) || !levelAccessor.getBlockState(blockPos.relative(direction)).is(BlockTags.SCULK_REPLACEABLE)) continue;
            return true;
        }
        return false;
    }

    class SculkVeinSpreaderConfig
    extends MultifaceSpreader.DefaultSpreaderConfig {
        private final MultifaceSpreader.SpreadType[] spreadTypes;

        public SculkVeinSpreaderConfig(SculkVeinBlock sculkVeinBlock, MultifaceSpreader.SpreadType ... spreadTypes) {
            super(sculkVeinBlock);
            this.spreadTypes = spreadTypes;
        }

        @Override
        public boolean stateCanBeReplaced(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction, BlockState blockState) {
            BlockPos blockPos3;
            BlockState blockState2 = blockGetter.getBlockState(blockPos2.relative(direction));
            if (blockState2.is(Blocks.SCULK) || blockState2.is(Blocks.SCULK_CATALYST) || blockState2.is(Blocks.MOVING_PISTON)) {
                return false;
            }
            if (blockPos.distManhattan(blockPos2) == 2 && blockGetter.getBlockState(blockPos3 = blockPos.relative(direction.getOpposite())).isFaceSturdy(blockGetter, blockPos3, direction)) {
                return false;
            }
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && !fluidState.is(Fluids.WATER)) {
                return false;
            }
            if (blockState.is(BlockTags.FIRE)) {
                return false;
            }
            return blockState.canBeReplaced() || super.stateCanBeReplaced(blockGetter, blockPos, blockPos2, direction, blockState);
        }

        @Override
        public MultifaceSpreader.SpreadType[] getSpreadTypes() {
            return this.spreadTypes;
        }

        @Override
        public boolean isOtherBlockValidAsSource(BlockState blockState) {
            return !blockState.is(Blocks.SCULK_VEIN);
        }
    }
}

