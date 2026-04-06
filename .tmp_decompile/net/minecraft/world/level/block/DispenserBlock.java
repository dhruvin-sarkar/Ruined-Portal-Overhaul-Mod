/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DispenserBlock
extends BaseEntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DispenserBlock> CODEC = DispenserBlock.simpleCodec(DispenserBlock::new);
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final DefaultDispenseItemBehavior DEFAULT_BEHAVIOR = new DefaultDispenseItemBehavior();
    public static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = new IdentityHashMap<Item, DispenseItemBehavior>();
    private static final int TRIGGER_DURATION = 4;

    public MapCodec<? extends DispenserBlock> codec() {
        return CODEC;
    }

    public static void registerBehavior(ItemLike itemLike, DispenseItemBehavior dispenseItemBehavior) {
        DISPENSER_REGISTRY.put(itemLike.asItem(), dispenseItemBehavior);
    }

    public static void registerProjectileBehavior(ItemLike itemLike) {
        DISPENSER_REGISTRY.put(itemLike.asItem(), new ProjectileDispenseBehavior(itemLike.asItem()));
    }

    protected DispenserBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(TRIGGERED, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity;
        if (!level.isClientSide() && (blockEntity = level.getBlockEntity(blockPos)) instanceof DispenserBlockEntity) {
            DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity)blockEntity;
            player.openMenu(dispenserBlockEntity);
            player.awardStat(dispenserBlockEntity instanceof DropperBlockEntity ? Stats.INSPECT_DROPPER : Stats.INSPECT_DISPENSER);
        }
        return InteractionResult.SUCCESS;
    }

    protected void dispenseFrom(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos) {
        DispenserBlockEntity dispenserBlockEntity = serverLevel.getBlockEntity(blockPos, BlockEntityType.DISPENSER).orElse(null);
        if (dispenserBlockEntity == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dispenser without matching block entity at {}", (Object)blockPos);
            return;
        }
        BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, dispenserBlockEntity);
        int i = dispenserBlockEntity.getRandomSlot(serverLevel.random);
        if (i < 0) {
            serverLevel.levelEvent(1001, blockPos, 0);
            serverLevel.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(dispenserBlockEntity.getBlockState()));
            return;
        }
        ItemStack itemStack = dispenserBlockEntity.getItem(i);
        DispenseItemBehavior dispenseItemBehavior = this.getDispenseMethod(serverLevel, itemStack);
        if (dispenseItemBehavior != DispenseItemBehavior.NOOP) {
            dispenserBlockEntity.setItem(i, dispenseItemBehavior.dispense(blockSource, itemStack));
        }
    }

    protected DispenseItemBehavior getDispenseMethod(Level level, ItemStack itemStack) {
        if (!itemStack.isItemEnabled(level.enabledFeatures())) {
            return DEFAULT_BEHAVIOR;
        }
        DispenseItemBehavior dispenseItemBehavior = DISPENSER_REGISTRY.get(itemStack.getItem());
        if (dispenseItemBehavior != null) {
            return dispenseItemBehavior;
        }
        return DispenserBlock.getDefaultDispenseMethod(itemStack);
    }

    private static DispenseItemBehavior getDefaultDispenseMethod(ItemStack itemStack) {
        if (itemStack.has(DataComponents.EQUIPPABLE)) {
            return EquipmentDispenseItemBehavior.INSTANCE;
        }
        return DEFAULT_BEHAVIOR;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
        boolean bl3 = blockState.getValue(TRIGGERED);
        if (bl2 && !bl3) {
            level.scheduleTick(blockPos, this, 4);
            level.setBlock(blockPos, (BlockState)blockState.setValue(TRIGGERED, true), 2);
        } else if (!bl2 && bl3) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(TRIGGERED, false), 2);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.dispenseFrom(serverLevel, blockState, blockPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DispenserBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    public static Position getDispensePosition(BlockSource blockSource) {
        return DispenserBlock.getDispensePosition(blockSource, 0.7, Vec3.ZERO);
    }

    public static Position getDispensePosition(BlockSource blockSource, double d, Vec3 vec3) {
        Direction direction = blockSource.state().getValue(FACING);
        return blockSource.center().add(d * (double)direction.getStepX() + vec3.x(), d * (double)direction.getStepY() + vec3.y(), d * (double)direction.getStepZ() + vec3.z());
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
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
        builder.add(FACING, TRIGGERED);
    }
}

