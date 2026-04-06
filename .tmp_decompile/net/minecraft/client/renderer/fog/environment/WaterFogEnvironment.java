/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WaterFogEnvironment
extends FogEnvironment {
    @Override
    public void setupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        float g = deltaTracker.getGameTimeDeltaPartialTick(false);
        fogData.environmentalStart = camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_START_DISTANCE, g).floatValue();
        fogData.environmentalEnd = camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_END_DISTANCE, g).floatValue();
        Entity entity = camera.entity();
        if (entity instanceof LocalPlayer) {
            LocalPlayer localPlayer = (LocalPlayer)entity;
            fogData.environmentalEnd *= Math.max(0.25f, localPlayer.getWaterVision());
        }
        fogData.skyEnd = fogData.environmentalEnd;
        fogData.cloudEnd = fogData.environmentalEnd;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.WATER;
    }

    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int i, float f) {
        return camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_COLOR, f);
    }
}

