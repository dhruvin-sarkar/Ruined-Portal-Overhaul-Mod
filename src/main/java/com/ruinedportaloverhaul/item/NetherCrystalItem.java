package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class NetherCrystalItem extends Item {
    public NetherCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        BlockState baseState = level.getBlockState(basePos);

        if (!baseState.is(Blocks.NETHERITE_BLOCK) && !baseState.is(Blocks.OBSIDIAN)) {
            return InteractionResult.FAIL;
        }

        BlockPos crystalPos = basePos.above();
        if (!level.isEmptyBlock(crystalPos)) {
            return InteractionResult.FAIL;
        }

        double x = crystalPos.getX();
        double y = crystalPos.getY();
        double z = crystalPos.getZ();
        if (!level.getEntities((Entity) null, new AABB(x, y, z, x + 1.0, y + 2.0, z + 1.0)).isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            NetherCrystalEntity crystal = new NetherCrystalEntity(serverLevel, x + 0.5, y, z + 0.5);
            crystal.setShowBottom(false);
            serverLevel.addFreshEntity(crystal);
            serverLevel.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, crystalPos);
            serverLevel.playSound(null, crystalPos, ModSounds.RITUAL_CRYSTAL_PLACE, SoundSource.BLOCKS, 0.9f, 1.0f);
            NetherDragonRituals.onNetherCrystalPlaced(serverLevel, basePos, crystal);
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
