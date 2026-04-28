package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.component.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

public class CorruptedNetheriteArmorItem extends Item {
    public static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private static final String CORRUPTED_KEY = RuinedPortalOverhaul.MOD_ID + ":corrupted";

    public CorruptedNetheriteArmorItem(Properties properties) {
        super(properties);
    }

    public static boolean isCorruptedNetherite(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getOrDefault(ModDataComponents.CORRUPTED_NETHERITE, false)) {
            return true;
        }

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return false;
        }
        CompoundTag tag = data.copyTag();
        return tag.getBooleanOr(CORRUPTED_KEY, false);
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
        int pieces = TooltipClientState.currentCorruptedArmorPieces();
        if (pieces <= 0 && isCorruptedNetherite(stack)) {
            pieces = 1;
        }
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.corrupted_netherite.tooltip.set", pieces).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.corrupted_netherite.tooltip.two").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.corrupted_netherite.tooltip.three").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.corrupted_netherite.tooltip.four").withStyle(ChatFormatting.DARK_RED));
    }
}
