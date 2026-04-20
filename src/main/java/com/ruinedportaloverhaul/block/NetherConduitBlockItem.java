package com.ruinedportaloverhaul.block;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class NetherConduitBlockItem extends BlockItem {
    public NetherConduitBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay tooltipDisplay,
        Consumer<Component> tooltip,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.literal("Level 0: Fire Resistance, Haste, Regeneration").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Upgrade with ancient debris: 1 for level 1, 2 for level 2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
