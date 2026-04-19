package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.network.NetherFireballHandler;
import io.wispforest.accessories.api.core.AccessoryItem;
import io.wispforest.accessories.api.slot.SlotReference;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class GhastTearNecklaceItem extends AccessoryItem {
    private static final int EFFECT_REFRESH_INTERVAL_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 80;
    private static final int SPEED_TWO_AMPLIFIER = 1;
    private static final int JUMP_BOOST_TWO_AMPLIFIER = 1;

    public GhastTearNecklaceItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference reference) {
        if (reference.entity() instanceof ServerPlayer player) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.GHAST_TEAR_NECKLACE_EQUIPPED, player);
        }
    }

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (player.tickCount % EFFECT_REFRESH_INTERVAL_TICKS != 0) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.SPEED, EFFECT_DURATION_TICKS, SPEED_TWO_AMPLIFIER, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, EFFECT_DURATION_TICKS, JUMP_BOOST_TWO_AMPLIFIER, true, false, true));
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay tooltipDisplay,
        Consumer<Component> tooltip,
        TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.ghast_tear_necklace.tooltip.mobility").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(
            "item.ruined_portal_overhaul.ghast_tear_necklace.tooltip.fireball",
            NetherFireballHandler.COOLDOWN_TICKS / 20
        ).withStyle(ChatFormatting.DARK_RED));

        long lastUse = stack.getOrDefault(ModDataComponents.LAST_NECKLACE_FIREBALL_TICK, Long.MIN_VALUE);
        if (lastUse == Long.MIN_VALUE) {
            tooltip.accept(Component.translatable("item.ruined_portal_overhaul.ghast_tear_necklace.tooltip.ready").withStyle(ChatFormatting.GOLD));
        }
    }
}
