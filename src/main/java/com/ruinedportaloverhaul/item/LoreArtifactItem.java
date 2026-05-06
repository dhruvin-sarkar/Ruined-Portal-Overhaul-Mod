package com.ruinedportaloverhaul.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class LoreArtifactItem extends Item {
    private final String itemPath;
    private final ChatFormatting accent;

    public LoreArtifactItem(Properties properties, String itemPath, ChatFormatting accent) {
        super(properties);
        this.itemPath = itemPath;
        this.accent = accent;
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
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul." + itemPath + ".tooltip.line1").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul." + itemPath + ".tooltip.line2").withStyle(accent, ChatFormatting.ITALIC));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul." + itemPath + ".tooltip.line3").withStyle(ChatFormatting.DARK_GRAY));
    }
}
