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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
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
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WallHangingSignBlock
extends SignBlock {
    public static final MapCodec<WallHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), WallHangingSignBlock.propertiesCodec()).apply((Applicative)instance, WallHangingSignBlock::new));
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction.Axis, VoxelShape> SHAPES_PLANK = Shapes.rotateHorizontalAxis(Block.column(16.0, 4.0, 14.0, 16.0));
    private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Shapes.or(SHAPES_PLANK.get(Direction.Axis.Z), Block.column(14.0, 2.0, 0.0, 10.0)));

    public MapCodec<WallHangingSignBlock> codec() {
        return CODEC;
    }

    public WallHangingSignBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(woodType, properties.sound(woodType.hangingSignSoundType()));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(WATERLOGGED, false));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        SignBlockEntity signBlockEntity;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SignBlockEntity && this.shouldTryToChainAnotherHangingSign(blockState, player, blockHitResult, signBlockEntity = (SignBlockEntity)blockEntity, itemStack)) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private boolean shouldTryToChainAnotherHangingSign(BlockState blockState, Player player, BlockHitResult blockHitResult, SignBlockEntity signBlockEntity, ItemStack itemStack) {
        return !signBlockEntity.canExecuteClickCommands(signBlockEntity.isFacingFrontText(player), player) && itemStack.getItem() instanceof HangingSignItem && !this.isHittingEditableSide(blockHitResult, blockState);
    }

    private boolean isHittingEditableSide(BlockHitResult blockHitResult, BlockState blockState) {
        return blockHitResult.getDirection().getAxis() == blockState.getValue(FACING).getAxis();
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES.get(blockState.getValue(FACING).getAxis());
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.getShape(blockState, blockGetter, blockPos, CollisionContext.empty());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES_PLANK.get(blockState.getValue(FACING).getAxis());
    }

    public boolean canPlace(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction direction = blockState.getValue(FACING).getClockWise();
        Direction direction2 = blockState.getValue(FACING).getCounterClockWise();
        return this.canAttachTo(levelReader, blockState, blockPos.relative(direction), direction2) || this.canAttachTo(levelReader, blockState, blockPos.relative(direction2), direction);
    }

    public boolean canAttachTo(LevelReader levelReader, BlockState blockState, BlockPos blockPos, Direction direction) {
        BlockState blockState2 = levelReader.getBlockState(blockPos);
        if (blockState2.is(BlockTags.WALL_HANGING_SIGNS)) {
            return blockState2.getValue(FACING).getAxis().test(blockState.getValue(FACING));
        }
        return blockState2.isFaceSturdy(levelReader, blockPos, direction, SupportType.FULL);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = this.defaultBlockState();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        Level levelReader = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            Direction direction2;
            if (!direction.getAxis().isHorizontal() || direction.getAxis().test(blockPlaceContext.getClickedFace()) || !(blockState = (BlockState)blockState.setValue(FACING, direction2 = direction.getOpposite())).canSurvive(levelReader, blockPos) || !this.canPlace(blockState, levelReader, blockPos)) continue;
            return (BlockState)blockState.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction.getAxis() == blockState.getValue(FACING).getClockWise().getAxis() && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return blockState.getValue(FACING).toYRot();
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
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HangingSignBlockEntity(blockPos, blockState);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return WallHangingSignBlock.createTickerHelper(blockEntityType, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
    }
}

