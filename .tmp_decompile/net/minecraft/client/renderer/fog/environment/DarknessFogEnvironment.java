/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.fog.environment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class DarknessFogEnvironment
extends MobEffectFogEnvironment {
    @Override
    public Holder<MobEffect> getMobEffect() {
        return MobEffects.DARKNESS;
    }

    @Override
    public void setupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        LivingEntity livingEntity;
        MobEffectInstance mobEffectInstance;
        Entity entity = camera.entity();
        if (entity instanceof LivingEntity && (mobEffectInstance = (livingEntity = (LivingEntity)entity).getEffect(this.getMobEffect())) != null) {
            float g = Mth.lerp(mobEffectInstance.getBlendFactor(livingEntity, deltaTracker.getGameTimeDeltaPartialTick(false)), f, 15.0f);
            fogData.environmentalStart = g * 0.75f;
            fogData.environmentalEnd = g;
            fogData.skyEnd = g;
            fogData.cloudEnd = g;
        }
    }

    @Override
    public float getModifiedDarkness(LivingEntity livingEntity, float f, float g) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(this.getMobEffect());
        return mobEffectInstance != null ? Math.max(mobEffectInstance.getBlendFactor(livingEntity, g), f) : f;
    }
}

