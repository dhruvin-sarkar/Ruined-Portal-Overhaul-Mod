/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SignItem
extends StandingAndWallBlockItem {
    public SignItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, Direction.DOWN, properties);
    }

    public SignItem(Item.Properties properties, Block block, Block block2, Direction direction) {
        super(block, block2, direction, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
        Object object;
        boolean bl = super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
        if (!level.isClientSide() && !bl && player != null && (object = level.getBlockEntity(blockPos)) instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)object;
            object = level.getBlockState(blockPos).getBlock();
            if (object instanceof SignBlock) {
                SignBlock signBlock = (SignBlock)object;
                signBlock.openTextEdit(player, signBlockEntity, true);
            }
        }
        return bl;
    }
}

