package com.ruinedportaloverhaul.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class NetherTideDiscItem extends Item {
    public NetherTideDiscItem(Properties properties) {
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
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.music_disc_nether_tide.tooltip.line1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.music_disc_nether_tide.tooltip.line2").withStyle(ChatFormatting.GRAY));
    }
}
