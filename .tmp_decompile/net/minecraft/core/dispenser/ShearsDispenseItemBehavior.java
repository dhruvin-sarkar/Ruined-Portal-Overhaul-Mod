/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior
extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverLevel = blockSource.level();
        if (!serverLevel.isClientSide()) {
            BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            this.setSuccess(ShearsDispenseItemBehavior.tryShearBeehive(serverLevel, itemStack, blockPos) || ShearsDispenseItemBehavior.tryShearEntity(serverLevel, blockPos, itemStack));
            if (this.isSuccess()) {
                itemStack.hurtAndBreak(1, serverLevel, null, item -> {});
            }
        }
        return itemStack;
    }

    private static boolean tryShearBeehive(ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        int i;
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.is(BlockTags.BEEHIVES, blockStateBase -> blockStateBase.hasProperty(BeehiveBlock.HONEY_LEVEL) && blockStateBase.getBlock() instanceof BeehiveBlock) && (i = blockState.getValue(BeehiveBlock.HONEY_LEVEL).intValue()) >= 5) {
            serverLevel.playSound(null, blockPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            BeehiveBlock.dropHoneycomb(serverLevel, itemStack, blockState, serverLevel.getBlockEntity(blockPos), null, blockPos);
            ((BeehiveBlock)blockState.getBlock()).releaseBeesAndResetHoneyLevel(serverLevel, blockState, blockPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
            return true;
        }
        return false;
    }

    private static boolean tryShearEntity(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        List<Entity> list = serverLevel.getEntitiesOfClass(Entity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS);
        for (Entity entity : list) {
            Shearable shearable;
            if (entity.shearOffAllLeashConnections(null)) {
                return true;
            }
            if (!(entity instanceof Shearable) || !(shearable = (Shearable)((Object)entity)).readyForShearing()) continue;
            shearable.shear(serverLevel, SoundSource.BLOCKS, itemStack);
            serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
            return true;
        }
        return false;
    }
}

