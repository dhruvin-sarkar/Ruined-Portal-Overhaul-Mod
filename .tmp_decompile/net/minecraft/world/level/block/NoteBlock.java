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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class NoteBlock
extends Block {
    public static final MapCodec<NoteBlock> CODEC = NoteBlock.simpleCodec(NoteBlock::new);
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
    public static final int NOTE_VOLUME = 3;

    public MapCodec<NoteBlock> codec() {
        return CODEC;
    }

    public NoteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(INSTRUMENT, NoteBlockInstrument.HARP)).setValue(NOTE, 0)).setValue(POWERED, false));
    }

    private BlockState setInstrument(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        NoteBlockInstrument noteBlockInstrument = levelReader.getBlockState(blockPos.above()).instrument();
        if (noteBlockInstrument.worksAboveNoteBlock()) {
            return (BlockState)blockState.setValue(INSTRUMENT, noteBlockInstrument);
        }
        NoteBlockInstrument noteBlockInstrument2 = levelReader.getBlockState(blockPos.below()).instrument();
        NoteBlockInstrument noteBlockInstrument3 = noteBlockInstrument2.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : noteBlockInstrument2;
        return (BlockState)blockState.setValue(INSTRUMENT, noteBlockInstrument3);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.setInstrument(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        boolean bl;
        boolean bl2 = bl = direction.getAxis() == Direction.Axis.Y;
        if (bl) {
            return this.setInstrument(levelReader, blockPos, blockState);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (bl2 != blockState.getValue(POWERED)) {
            if (bl2) {
                this.playNote(null, blockState, level, blockPos);
            }
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl2), 3);
        }
    }

    private void playNote(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getValue(INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(blockPos.above()).isAir()) {
            level.blockEvent(blockPos, this, 0, 0);
            level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, blockPos);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && blockHitResult.getDirection() == Direction.UP) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            blockState = (BlockState)blockState.cycle(NOTE);
            level.setBlock(blockPos, blockState, 3);
            this.playNote(player, blockState, level, blockPos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (level.isClientSide()) {
            return;
        }
        this.playNote(player, blockState, level, blockPos);
        player.awardStat(Stats.PLAY_NOTEBLOCK);
    }

    public static float getPitchFromNote(int i) {
        return (float)Math.pow(2.0, (double)(i - 12) / 12.0);
    }

    @Override
    protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        Holder<SoundEvent> holder;
        float f;
        NoteBlockInstrument noteBlockInstrument = blockState.getValue(INSTRUMENT);
        if (noteBlockInstrument.isTunable()) {
            int k = blockState.getValue(NOTE);
            f = NoteBlock.getPitchFromNote(k);
            level.addParticle(ParticleTypes.NOTE, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + 0.5, (double)k / 24.0, 0.0, 0.0);
        } else {
            f = 1.0f;
        }
        if (noteBlockInstrument.hasCustomSound()) {
            Identifier identifier = this.getCustomSoundId(level, blockPos);
            if (identifier == null) {
                return false;
            }
            holder = Holder.direct(SoundEvent.createVariableRangeEvent(identifier));
        } else {
            holder = noteBlockInstrument.getSoundEvent();
        }
        level.playSeededSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, holder, SoundSource.RECORDS, 3.0f, f, level.random.nextLong());
        return true;
    }

    private @Nullable Identifier getCustomSoundId(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos.above());
        if (blockEntity instanceof SkullBlockEntity) {
            SkullBlockEntity skullBlockEntity = (SkullBlockEntity)blockEntity;
            return skullBlockEntity.getNoteBlockSound();
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE);
    }
}

