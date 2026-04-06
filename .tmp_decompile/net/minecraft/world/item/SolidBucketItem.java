/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class SolidBucketItem
extends BlockItem
implements DispensibleContainerItem {
    private final SoundEvent placeSound;

    public SolidBucketItem(Block block, SoundEvent soundEvent, Item.Properties properties) {
        super(block, properties);
        this.placeSound = soundEvent;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        InteractionResult interactionResult = super.useOn(useOnContext);
        Player player = useOnContext.getPlayer();
        if (interactionResult.consumesAction() && player != null) {
            player.setItemInHand(useOnContext.getHand(), BucketItem.getEmptySuccessItem(useOnContext.getItemInHand(), player));
        }
        return interactionResult;
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState blockState) {
        return this.placeSound;
    }

    @Override
    public boolean emptyContents(@Nullable LivingEntity livingEntity, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        if (level.isInWorldBounds(blockPos) && level.isEmptyBlock(blockPos)) {
            if (!level.isClientSide()) {
                level.setBlock(blockPos, this.getBlock().defaultBlockState(), 3);
            }
            level.gameEvent((Entity)livingEntity, GameEvent.FLUID_PLACE, blockPos);
            level.playSound((Entity)livingEntity, blockPos, this.placeSound, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

