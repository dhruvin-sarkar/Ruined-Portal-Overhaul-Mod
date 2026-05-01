package com.ruinedportaloverhaul.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;

final class RuinedPortalSwingAnimations {
    static final int HUMANOID_ATTACK_TICKS = 10;

    private RuinedPortalSwingAnimations() {
    }

    static ItemStack withHumanoidAttackTiming(ItemStack stack) {
        stack.set(DataComponents.SWING_ANIMATION, new SwingAnimation(SwingAnimationType.WHACK, HUMANOID_ATTACK_TICKS));
        return stack;
    }
}
