/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class IceBlock
extends HalfTransparentBlock {
    public static final MapCodec<IceBlock> CODEC = IceBlock.simpleCodec(IceBlock::new);

    public MapCodec<? extends IceBlock> codec() {
        return CODEC;
    }

    public IceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockState meltsInto() {
        return Blocks.WATER.defaultBlockState();
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        if (!EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, blockPos).booleanValue()) {
                level.removeBlock(blockPos, false);
                return;
            }
            BlockState blockState2 = level.getBlockState(blockPos.below());
            if (blockState2.blocksMotion() || blockState2.liquid()) {
                level.setBlockAndUpdate(blockPos, IceBlock.meltsInto());
            }
        }
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11 - blockState.getLightBlock()) {
            this.melt(blockState, serverLevel, blockPos);
        }
    }

    protected void melt(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, blockPos).booleanValue()) {
            level.removeBlock(blockPos, false);
            return;
        }
        level.setBlockAndUpdate(blockPos, IceBlock.meltsInto());
        level.neighborChanged(blockPos, IceBlock.meltsInto().getBlock(), null);
    }
}

