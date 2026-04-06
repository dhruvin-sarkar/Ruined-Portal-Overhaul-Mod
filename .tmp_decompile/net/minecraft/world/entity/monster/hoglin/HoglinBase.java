/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
    public static final int ATTACK_ANIMATION_DURATION = 10;
    public static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2f;

    public int getAttackAnimationRemainingTicks();

    public static boolean hurtAndThrowTarget(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        float f = (float)livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float g = !livingEntity.isBaby() && (int)f > 0 ? f / 2.0f + (float)serverLevel.random.nextInt((int)f) : f;
        DamageSource damageSource = livingEntity.damageSources().mobAttack(livingEntity);
        boolean bl = livingEntity2.hurtServer(serverLevel, damageSource, g);
        if (bl) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity2, damageSource);
            if (!livingEntity.isBaby()) {
                HoglinBase.throwTarget(livingEntity, livingEntity2);
            }
        }
        return bl;
    }

    public static void throwTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        double e;
        double d = livingEntity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double f = d - (e = livingEntity2.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (f <= 0.0) {
            return;
        }
        double g = livingEntity2.getX() - livingEntity.getX();
        double h = livingEntity2.getZ() - livingEntity.getZ();
        float i = livingEntity.level().random.nextInt(21) - 10;
        double j = f * (double)(livingEntity.level().random.nextFloat() * 0.5f + 0.2f);
        Vec3 vec3 = new Vec3(g, 0.0, h).normalize().scale(j).yRot(i);
        double k = f * (double)livingEntity.level().random.nextFloat() * 0.5;
        livingEntity2.push(vec3.x, k, vec3.z);
        livingEntity2.hurtMarked = true;
    }
}

