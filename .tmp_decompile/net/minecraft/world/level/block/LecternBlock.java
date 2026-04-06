/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class LecternBlock
extends BaseEntityBlock {
    public static final MapCodec<LecternBlock> CODEC = LecternBlock.simpleCodec(LecternBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    private static final VoxelShape SHAPE_COLLISION = Shapes.or(Block.column(16.0, 0.0, 2.0), Block.column(8.0, 2.0, 14.0));
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(Block.boxZ(16.0, 10.0, 14.0, 1.0, 5.333333), Block.boxZ(16.0, 12.0, 16.0, 5.333333, 9.666667), Block.boxZ(16.0, 14.0, 18.0, 9.666667, 14.0), SHAPE_COLLISION));
    private static final int PAGE_CHANGE_IMPULSE_TICKS = 2;

    public MapCodec<LecternBlock> codec() {
        return CODEC;
    }

    protected LecternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(HAS_BOOK, false));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState) {
        return SHAPE_COLLISION;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        TypedEntityData<BlockEntityType<?>> typedEntityData;
        Level level = blockPlaceContext.getLevel();
        ItemStack itemStack = blockPlaceContext.getItemInHand();
        Player player = blockPlaceContext.getPlayer();
        boolean bl = false;
        if (!level.isClientSide() && player != null && player.canUseGameMasterBlocks() && (typedEntityData = itemStack.get(DataComponents.BLOCK_ENTITY_DATA)) != null && typedEntityData.contains("Book")) {
            bl = true;
        }
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HAS_BOOK, bl);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(FACING));
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
        builder.add(FACING, POWERED, HAS_BOOK);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new LecternBlockEntity(blockPos, blockState);
    }

    public static boolean tryPlaceBook(@Nullable LivingEntity livingEntity, Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        if (!blockState.getValue(HAS_BOOK).booleanValue()) {
            if (!level.isClientSide()) {
                LecternBlock.placeBook(livingEntity, level, blockPos, blockState, itemStack);
            }
            return true;
        }
        return false;
    }

    private static void placeBook(@Nullable LivingEntity livingEntity, Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
            lecternBlockEntity.setBook(itemStack.consumeAndReturn(1, livingEntity));
            LecternBlock.resetBookState(livingEntity, level, blockPos, blockState, true);
            level.playSound(null, blockPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static void resetBookState(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        BlockState blockState2 = (BlockState)((BlockState)blockState.setValue(POWERED, false)).setValue(HAS_BOOK, bl);
        level.setBlock(blockPos, blockState2, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
        LecternBlock.updateBelow(level, blockPos, blockState);
    }

    public static void signalPageChange(Level level, BlockPos blockPos, BlockState blockState) {
        LecternBlock.changePowered(level, blockPos, blockState, true);
        level.scheduleTick(blockPos, blockState.getBlock(), 2);
        level.levelEvent(1043, blockPos, 0);
    }

    private static void changePowered(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl), 3);
        LecternBlock.updateBelow(level, blockPos, blockState);
    }

    private static void updateBelow(Level level, BlockPos blockPos, BlockState blockState) {
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, blockState.getValue(FACING).getOpposite(), Direction.UP);
        level.updateNeighborsAt(blockPos.below(), blockState.getBlock(), orientation);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        LecternBlock.changePowered(serverLevel, blockPos, blockState, false);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (blockState.getValue(POWERED).booleanValue()) {
            LecternBlock.updateBelow(serverLevel, blockPos, blockState);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return direction == Direction.UP && blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity;
        if (blockState.getValue(HAS_BOOK).booleanValue() && (blockEntity = level.getBlockEntity(blockPos)) instanceof LecternBlockEntity) {
            return ((LecternBlockEntity)blockEntity).getRedstoneSignal();
        }
        return 0;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (blockState.getValue(HAS_BOOK).booleanValue()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (itemStack.is(ItemTags.LECTERN_BOOKS)) {
            return LecternBlock.tryPlaceBook(player, level, blockPos, blockState, itemStack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (itemStack.isEmpty() && interactionHand == InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (blockState.getValue(HAS_BOOK).booleanValue()) {
            if (!level.isClientSide()) {
                this.openScreen(level, blockPos, player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        if (!blockState.getValue(HAS_BOOK).booleanValue()) {
            return null;
        }
        return super.getMenuProvider(blockState, level, blockPos);
    }

    private void openScreen(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            player.openMenu((LecternBlockEntity)blockEntity);
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

