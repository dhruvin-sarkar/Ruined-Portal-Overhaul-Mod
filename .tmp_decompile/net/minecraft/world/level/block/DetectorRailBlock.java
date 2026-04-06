/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock
extends BaseRailBlock {
    public static final MapCodec<DetectorRailBlock> CODEC = DetectorRailBlock.simpleCodec(DetectorRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int PRESSED_CHECK_PERIOD = 20;

    public MapCodec<DetectorRailBlock> codec() {
        return CODEC;
    }

    public DetectorRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false)).setValue(SHAPE, RailShape.NORTH_SOUTH)).setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (level.isClientSide()) {
            return;
        }
        if (blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(level, blockPos, blockState);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(serverLevel, blockPos, blockState);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return 0;
        }
        return direction == Direction.UP ? 15 : 0;
    }

    private void checkPressed(Level level, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2;
        if (!this.canSurvive(blockState, level, blockPos)) {
            return;
        }
        boolean bl = blockState.getValue(POWERED);
        boolean bl2 = false;
        List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, entity -> true);
        if (!list.isEmpty()) {
            bl2 = true;
        }
        if (bl2 && !bl) {
            blockState2 = (BlockState)blockState.setValue(POWERED, true);
            level.setBlock(blockPos, blockState2, 3);
            this.updatePowerToConnected(level, blockPos, blockState2, true);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (!bl2 && bl) {
            blockState2 = (BlockState)blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState2, 3);
            this.updatePowerToConnected(level, blockPos, blockState2, false);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (bl2) {
            level.scheduleTick(blockPos, this, 20);
        }
        level.updateNeighbourForOutputSignal(blockPos, this);
    }

    protected void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        RailState railState = new RailState(level, blockPos, blockState);
        List<BlockPos> list = railState.getConnections();
        for (BlockPos blockPos2 : list) {
            BlockState blockState2 = level.getBlockState(blockPos2);
            level.neighborChanged(blockState2, blockPos2, blockState2.getBlock(), null, false);
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        BlockState blockState3 = this.updateState(blockState, level, blockPos, bl);
        this.checkPressed(level, blockPos, blockState3);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(POWERED).booleanValue()) {
            List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(level, blockPos, MinecartCommandBlock.class, entity -> true);
            if (!list.isEmpty()) {
                return list.get(0).getCommandBlock().getSuccessCount();
            }
            List<AbstractMinecart> list2 = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list2.isEmpty()) {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((Object)list2.get(0)));
            }
        }
        return 0;
    }

    private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> class_, Predicate<Entity> predicate) {
        return level.getEntitiesOfClass(class_, this.getSearchBB(blockPos), predicate);
    }

    private AABB getSearchBB(BlockPos blockPos) {
        double d = 0.2;
        return new AABB((double)blockPos.getX() + 0.2, blockPos.getY(), (double)blockPos.getZ() + 0.2, (double)(blockPos.getX() + 1) - 0.2, (double)(blockPos.getY() + 1) - 0.2, (double)(blockPos.getZ() + 1) - 0.2);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        RailShape railShape = blockState.getValue(SHAPE);
        RailShape railShape2 = this.rotate(railShape, rotation);
        return (BlockState)blockState.setValue(SHAPE, railShape2);
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        RailShape railShape = blockState.getValue(SHAPE);
        RailShape railShape2 = this.mirror(railShape, mirror);
        return (BlockState)blockState.setValue(SHAPE, railShape2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }
}

