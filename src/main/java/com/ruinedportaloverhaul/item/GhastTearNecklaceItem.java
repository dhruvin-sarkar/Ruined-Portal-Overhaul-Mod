package com.ruinedportaloverhaul.item;

import io.wispforest.accessories.api.core.AccessoryItem;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GhastTearNecklaceItem extends AccessoryItem {
    private static final int EFFECT_REFRESH_INTERVAL_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 80;
    private static final int SPEED_TWO_AMPLIFIER = 1;
    private static final int JUMP_BOOST_TWO_AMPLIFIER = 1;

    public GhastTearNecklaceItem(Item.Properties properties) {
        super(properties);
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
}
