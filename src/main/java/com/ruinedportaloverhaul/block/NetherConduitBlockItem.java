package com.ruinedportaloverhaul.block;

import com.ruinedportaloverhaul.block.entity.ModBlockEntities;
import com.ruinedportaloverhaul.item.TooltipClientState;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class NetherConduitBlockItem extends BlockItem {
    private static final String CONDUIT_LEVEL_TAG = "conduit_level";
    private static final int MAX_CONDUIT_LEVEL = 2;

    public NetherConduitBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
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
        // The original tooltip hardcoded a flat description and hid the carried conduit tier data entirely,
        // so this now presents the ritual, any stored upgrade level, and the live effect breakdown.
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        int conduitLevel = getStoredConduitLevel(stack);

        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_conduit.tooltip.lore").withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_conduit.tooltip.frame").withStyle(ChatFormatting.GRAY));

        if (conduitLevel > 0) {
            tooltip.accept(
                Component.translatable(
                    "item.ruined_portal_overhaul.nether_conduit.tooltip.stored_level",
                    conduitLevel,
                    MAX_CONDUIT_LEVEL,
                    progressBar(conduitLevel)
                ).withStyle(ChatFormatting.GRAY)
            );
        }

        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_conduit.tooltip.repel").withStyle(ChatFormatting.DARK_RED));

        if (TooltipClientState.isShiftDown()) {
            tooltip.accept(
                Component.translatable(
                    "item.ruined_portal_overhaul.nether_conduit.tooltip.details.fire_resistance",
                    conduitLevel >= 1 ? 2 : 1
                ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.accept(
                Component.translatable(
                    "item.ruined_portal_overhaul.nether_conduit.tooltip.details.haste",
                    conduitLevel >= 1 ? 2 : 1
                ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.accept(
                Component.translatable(
                    "item.ruined_portal_overhaul.nether_conduit.tooltip.details.regeneration",
                    conduitLevel >= 1 ? 2 : 1
                ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.accept(
                Component.translatable(
                    "item.ruined_portal_overhaul.nether_conduit.tooltip.details.strike",
                    attackDamage(conduitLevel),
                    attackRadius(conduitLevel)
                ).withStyle(ChatFormatting.DARK_GRAY)
            );

            if (conduitLevel >= MAX_CONDUIT_LEVEL) {
                tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_conduit.tooltip.details.lava").withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            tooltip.accept(Component.translatable("item.ruined_portal_overhaul.nether_conduit.tooltip.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static int getStoredConduitLevel(ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null || blockEntityData.type() != ModBlockEntities.NETHER_CONDUIT || !blockEntityData.contains(CONDUIT_LEVEL_TAG)) {
            return 0;
        }

        CompoundTag tag = blockEntityData.copyTagWithoutId();
        return Math.max(0, Math.min(MAX_CONDUIT_LEVEL, tag.getInt(CONDUIT_LEVEL_TAG).orElse(0)));
    }

    private static String progressBar(int conduitLevel) {
        return "#".repeat(Math.max(0, conduitLevel)) + "-".repeat(Math.max(0, MAX_CONDUIT_LEVEL - conduitLevel));
    }

    private static int attackRadius(int conduitLevel) {
        // Fix: the tooltip mirrored an oversized 16/20/24 combat radius. Keep player-facing numbers tied to the corrected 8/12/16 tier progression.
        return switch (conduitLevel) {
            case 1 -> 12;
            case 2 -> 16;
            default -> 8;
        };
    }

    private static int attackDamage(int conduitLevel) {
        return switch (conduitLevel) {
            case 1 -> 6;
            case 2 -> 8;
            default -> 4;
        };
    }
}
