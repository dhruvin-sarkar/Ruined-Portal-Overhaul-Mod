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
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ChiseledBookShelfBlock
extends BaseEntityBlock
implements SelectableSlotContainer {
    public static final MapCodec<ChiseledBookShelfBlock> CODEC = ChiseledBookShelfBlock.simpleCodec(ChiseledBookShelfBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty SLOT_0_OCCUPIED = BlockStateProperties.SLOT_0_OCCUPIED;
    public static final BooleanProperty SLOT_1_OCCUPIED = BlockStateProperties.SLOT_1_OCCUPIED;
    public static final BooleanProperty SLOT_2_OCCUPIED = BlockStateProperties.SLOT_2_OCCUPIED;
    public static final BooleanProperty SLOT_3_OCCUPIED = BlockStateProperties.SLOT_3_OCCUPIED;
    public static final BooleanProperty SLOT_4_OCCUPIED = BlockStateProperties.SLOT_4_OCCUPIED;
    public static final BooleanProperty SLOT_5_OCCUPIED = BlockStateProperties.SLOT_5_OCCUPIED;
    private static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final int BOOKS_PER_ROW = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of((Object)SLOT_0_OCCUPIED, (Object)SLOT_1_OCCUPIED, (Object)SLOT_2_OCCUPIED, (Object)SLOT_3_OCCUPIED, (Object)SLOT_4_OCCUPIED, (Object)SLOT_5_OCCUPIED);

    public MapCodec<ChiseledBookShelfBlock> codec() {
        return CODEC;
    }

    @Override
    public int getRows() {
        return 2;
    }

    @Override
    public int getColumns() {
        return 3;
    }

    public ChiseledBookShelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
        BlockState blockState = (BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH);
        for (BooleanProperty booleanProperty : SLOT_OCCUPIED_PROPERTIES) {
            blockState = (BlockState)blockState.setValue(booleanProperty, false);
        }
        this.registerDefaultState(blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof ChiseledBookShelfBlockEntity)) {
            return InteractionResult.PASS;
        }
        ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)blockEntity;
        if (!itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        OptionalInt optionalInt = this.getHitSlot(blockHitResult, blockState.getValue(FACING));
        if (optionalInt.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (((Boolean)blockState.getValue(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        ChiseledBookShelfBlock.addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, optionalInt.getAsInt());
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof ChiseledBookShelfBlockEntity)) {
            return InteractionResult.PASS;
        }
        ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)blockEntity;
        OptionalInt optionalInt = this.getHitSlot(blockHitResult, blockState.getValue(FACING));
        if (optionalInt.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!((Boolean)blockState.getValue(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return InteractionResult.CONSUME;
        }
        ChiseledBookShelfBlock.removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, optionalInt.getAsInt());
        return InteractionResult.SUCCESS;
    }

    private static void addBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i) {
        if (level.isClientSide()) {
            return;
        }
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
        chiseledBookShelfBlockEntity.setItem(i, itemStack.consumeAndReturn(1, player));
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private static void removeBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, int i) {
        if (level.isClientSide()) {
            return;
        }
        ItemStack itemStack = chiseledBookShelfBlockEntity.removeItem(i, 1);
        SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }
        level.gameEvent((Entity)player, GameEvent.BLOCK_CHANGE, blockPos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChiseledBookShelfBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(property -> builder.add((Property<?>)property));
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        if (level.isClientSide()) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)blockEntity;
            return chiseledBookShelfBlockEntity.getLastInteractedSlot() + 1;
        }
        return 0;
    }
}

