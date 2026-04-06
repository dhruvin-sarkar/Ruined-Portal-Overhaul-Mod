/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0f;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0f;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0f;
    public static final float MIN_ARMOR_RATIO = 0.2f;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(LivingEntity livingEntity, float f, DamageSource damageSource, float g, float h) {
        float l;
        Level level;
        float i = 2.0f + h / 4.0f;
        float j = Mth.clamp(g - f / i, g * 0.2f, 20.0f);
        float k = j / 25.0f;
        ItemStack itemStack = damageSource.getWeaponItem();
        if (itemStack != null && (level = livingEntity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            l = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, livingEntity, damageSource, k), 0.0f, 1.0f);
        } else {
            l = k;
        }
        float m = 1.0f - l;
        return f * m;
    }

    public static float getDamageAfterMagicAbsorb(float f, float g) {
        float h = Mth.clamp(g, 0.0f, 20.0f);
        return f * (1.0f - h / 25.0f);
    }
}

