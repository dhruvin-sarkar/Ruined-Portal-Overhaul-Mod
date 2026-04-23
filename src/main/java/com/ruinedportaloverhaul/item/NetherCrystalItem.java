package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.sound.ModSounds;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
    @SuppressWarnings("deprecation")
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay tooltipDisplay,
        Consumer<Component> tooltip,
        TooltipFlag flag
    ) {
        // The crystal previously had no tooltip at all, so this now frames the ritual clearly
        // and tells players what placing all four crystals will actually unleash.
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_crystal.tooltip.line1").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_crystal.tooltip.line2").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_crystal.tooltip.line3").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Fix: crystal placement spawned server-side but still shrank the stack on both logical sides. Consumption now happens only on the server and respects creative mode.
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
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
