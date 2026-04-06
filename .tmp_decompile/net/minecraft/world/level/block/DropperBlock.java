/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class DropperBlock
extends DispenserBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<DropperBlock> CODEC = DropperBlock.simpleCodec(DropperBlock::new);
    private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

    public MapCodec<DropperBlock> codec() {
        return CODEC;
    }

    public DropperBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected DispenseItemBehavior getDispenseMethod(Level level, ItemStack itemStack) {
        return DISPENSE_BEHAVIOUR;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DropperBlockEntity(blockPos, blockState);
    }

    @Override
    protected void dispenseFrom(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos) {
        ItemStack itemStack2;
        DispenserBlockEntity dispenserBlockEntity = serverLevel.getBlockEntity(blockPos, BlockEntityType.DROPPER).orElse(null);
        if (dispenserBlockEntity == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", (Object)blockPos);
            return;
        }
        BlockSource blockSource = new BlockSource(serverLevel, blockPos, blockState, dispenserBlockEntity);
        int i = dispenserBlockEntity.getRandomSlot(serverLevel.random);
        if (i < 0) {
            serverLevel.levelEvent(1001, blockPos, 0);
            return;
        }
        ItemStack itemStack = dispenserBlockEntity.getItem(i);
        if (itemStack.isEmpty()) {
            return;
        }
        Direction direction = (Direction)serverLevel.getBlockState(blockPos).getValue(FACING);
        Container container = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(direction));
        if (container == null) {
            itemStack2 = DISPENSE_BEHAVIOUR.dispense(blockSource, itemStack);
        } else {
            itemStack2 = HopperBlockEntity.addItem(dispenserBlockEntity, container, itemStack.copyWithCount(1), direction.getOpposite());
            if (itemStack2.isEmpty()) {
                itemStack2 = itemStack.copy();
                itemStack2.shrink(1);
            } else {
                itemStack2 = itemStack.copy();
            }
        }
        dispenserBlockEntity.setItem(i, itemStack2);
    }
}

