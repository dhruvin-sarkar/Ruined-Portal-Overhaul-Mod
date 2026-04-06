/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;

class BadOmenMobEffect
extends MobEffect {
    protected BadOmenMobEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity livingEntity, int i) {
        Raid raid;
        ServerPlayer serverPlayer;
        if (livingEntity instanceof ServerPlayer && !(serverPlayer = (ServerPlayer)livingEntity).isSpectator() && serverLevel.getDifficulty() != Difficulty.PEACEFUL && serverLevel.isVillage(serverPlayer.blockPosition()) && ((raid = serverLevel.getRaidAt(serverPlayer.blockPosition())) == null || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel())) {
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.RAID_OMEN, 600, i));
            serverPlayer.setRaidOmenPosition(serverPlayer.blockPosition());
            return false;
        }
        return true;
    }
}

