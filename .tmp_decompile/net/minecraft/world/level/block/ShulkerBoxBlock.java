/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ShulkerBoxBlock
extends BaseEntityBlock {
    public static final MapCodec<ShulkerBoxBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DyeColor.CODEC.optionalFieldOf("color").forGetter(shulkerBoxBlock -> Optional.ofNullable(shulkerBoxBlock.color)), ShulkerBoxBlock.propertiesCodec()).apply((Applicative)instance, (optional, properties) -> new ShulkerBoxBlock(optional.orElse(null), (BlockBehaviour.Properties)properties)));
    public static final Map<Direction, VoxelShape> SHAPES_OPEN_SUPPORT = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final Identifier CONTENTS = Identifier.withDefaultNamespace("contents");
    private final @Nullable DyeColor color;

    public MapCodec<ShulkerBoxBlock> codec() {
        return CODEC;
    }

    public ShulkerBoxBlock(@Nullable DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShulkerBoxBlockEntity(this.color, blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return ShulkerBoxBlock.createTickerHelper(blockEntityType, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level instanceof ServerLevel) {
            ShulkerBoxBlockEntity shulkerBoxBlockEntity;
            ServerLevel serverLevel = (ServerLevel)level;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof ShulkerBoxBlockEntity && ShulkerBoxBlock.canOpen(blockState, level, blockPos, shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity)) {
                player.openMenu(shulkerBoxBlockEntity);
                player.awardStat(Stats.OPEN_SHULKER_BOX);
                PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean canOpen(BlockState blockState, Level level, BlockPos blockPos, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        if (shulkerBoxBlockEntity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
            return true;
        }
        AABB aABB = Shulker.getProgressDeltaAabb(1.0f, blockState.getValue(FACING), 0.0f, 0.5f, blockPos.getBottomCenter()).deflate(1.0E-6);
        return level.noCollision(aABB);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
            if (!level.isClientSide() && player.preventsBlockDrops() && !shulkerBoxBlockEntity.isEmpty()) {
                ItemStack itemStack = ShulkerBoxBlock.getColoredItemStack(this.getColor());
                itemStack.applyComponents(blockEntity.collectComponents());
                ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, itemStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            } else {
                shulkerBoxBlockEntity.unpackLootTable(player);
            }
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
            builder = builder.withDynamicDrop(CONTENTS, consumer -> {
                for (int i = 0; i < shulkerBoxBlockEntity.getContainerSize(); ++i) {
                    consumer.accept(shulkerBoxBlockEntity.getItem(i));
                }
            });
        }
        return super.getDrops(blockState, builder);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        ShulkerBoxBlockEntity shulkerBoxBlockEntity;
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof ShulkerBoxBlockEntity && !(shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity).isClosed()) {
            return SHAPES_OPEN_SUPPORT.get(blockState.getValue(FACING).getOpposite());
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
            return Shapes.create(shulkerBoxBlockEntity.getBoundingBox(blockState));
        }
        return Shapes.block();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    public static Block getBlockByColor(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return Blocks.SHULKER_BOX;
        }
        return switch (dyeColor) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> Blocks.WHITE_SHULKER_BOX;
            case DyeColor.ORANGE -> Blocks.ORANGE_SHULKER_BOX;
            case DyeColor.MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
            case DyeColor.LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
            case DyeColor.YELLOW -> Blocks.YELLOW_SHULKER_BOX;
            case DyeColor.LIME -> Blocks.LIME_SHULKER_BOX;
            case DyeColor.PINK -> Blocks.PINK_SHULKER_BOX;
            case DyeColor.GRAY -> Blocks.GRAY_SHULKER_BOX;
            case DyeColor.LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
            case DyeColor.CYAN -> Blocks.CYAN_SHULKER_BOX;
            case DyeColor.BLUE -> Blocks.BLUE_SHULKER_BOX;
            case DyeColor.BROWN -> Blocks.BROWN_SHULKER_BOX;
            case DyeColor.GREEN -> Blocks.GREEN_SHULKER_BOX;
            case DyeColor.RED -> Blocks.RED_SHULKER_BOX;
            case DyeColor.BLACK -> Blocks.BLACK_SHULKER_BOX;
            case DyeColor.PURPLE -> Blocks.PURPLE_SHULKER_BOX;
        };
    }

    public @Nullable DyeColor getColor() {
        return this.color;
    }

    public static ItemStack getColoredItemStack(@Nullable DyeColor dyeColor) {
        return new ItemStack(ShulkerBoxBlock.getBlockByColor(dyeColor));
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }
}

