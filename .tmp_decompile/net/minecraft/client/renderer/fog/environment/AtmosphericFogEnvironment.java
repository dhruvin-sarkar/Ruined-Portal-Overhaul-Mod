/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.fog.environment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PanoramicScreenshotParameters;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AtmosphericFogEnvironment
extends FogEnvironment {
    private static final int MIN_RAIN_FOG_SKY_LIGHT = 8;
    private static final float RAIN_FOG_START_OFFSET = -160.0f;
    private static final float RAIN_FOG_END_OFFSET = -256.0f;
    private float rainFogMultiplier;

    @Override
    public int getBaseColor(ClientLevel clientLevel, Camera camera, int i, float f) {
        float h;
        int j = camera.attributeProbe().getValue(EnvironmentAttributes.FOG_COLOR, f);
        if (i >= 4) {
            int l;
            float m;
            float g = camera.attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, f).floatValue() * ((float)Math.PI / 180);
            h = Mth.sin(g) > 0.0f ? -1.0f : 1.0f;
            PanoramicScreenshotParameters panoramicScreenshotParameters = Minecraft.getInstance().gameRenderer.getPanoramicScreenshotParameters();
            Vector3fc vector3fc = panoramicScreenshotParameters != null ? panoramicScreenshotParameters.forwardVector() : camera.forwardVector();
            float k = vector3fc.dot(h, 0.0f, 0.0f);
            if (k > 0.0f && (m = ARGB.alphaFloat(l = camera.attributeProbe().getValue(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, f).intValue())) > 0.0f) {
                j = ARGB.srgbLerp(k * m, j, ARGB.opaque(l));
            }
        }
        int n = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_COLOR, f);
        n = AtmosphericFogEnvironment.applyWeatherDarken(n, clientLevel.getRainLevel(f), clientLevel.getThunderLevel(f));
        h = Math.min(camera.attributeProbe().getValue(EnvironmentAttributes.SKY_FOG_END_DISTANCE, f).floatValue() / 16.0f, (float)i);
        float o = Mth.clampedLerp(h / 32.0f, 0.25f, 1.0f);
        o = 1.0f - (float)Math.pow(o, 0.25);
        j = ARGB.srgbLerp(o, j, n);
        return j;
    }

    private static int applyWeatherDarken(int i, float f, float g) {
        if (f > 0.0f) {
            float h = 1.0f - f * 0.5f;
            float j = 1.0f - f * 0.4f;
            i = ARGB.scaleRGB(i, h, h, j);
        }
        if (g > 0.0f) {
            i = ARGB.scaleRGB(i, 1.0f - g * 0.5f);
        }
        return i;
    }

    @Override
    public void setupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        this.updateRainFogState(camera, clientLevel, deltaTracker);
        float g = deltaTracker.getGameTimeDeltaPartialTick(false);
        fogData.environmentalStart = camera.attributeProbe().getValue(EnvironmentAttributes.FOG_START_DISTANCE, g).floatValue();
        fogData.environmentalEnd = camera.attributeProbe().getValue(EnvironmentAttributes.FOG_END_DISTANCE, g).floatValue();
        fogData.environmentalStart += -160.0f * this.rainFogMultiplier;
        float h = Math.min(96.0f, fogData.environmentalEnd);
        fogData.environmentalEnd = Math.max(h, fogData.environmentalEnd + -256.0f * this.rainFogMultiplier);
        fogData.skyEnd = Math.min(f, camera.attributeProbe().getValue(EnvironmentAttributes.SKY_FOG_END_DISTANCE, g).floatValue());
        fogData.cloudEnd = Math.min((float)(Minecraft.getInstance().options.cloudRange().get() * 16), camera.attributeProbe().getValue(EnvironmentAttributes.CLOUD_FOG_END_DISTANCE, g).floatValue());
        if (Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) {
            fogData.environmentalStart = Math.min(fogData.environmentalStart, 10.0f);
            fogData.skyEnd = fogData.environmentalEnd = Math.min(fogData.environmentalEnd, 96.0f);
            fogData.cloudEnd = fogData.environmentalEnd;
        }
    }

    private void updateRainFogState(Camera camera, ClientLevel clientLevel, DeltaTracker deltaTracker) {
        BlockPos blockPos = camera.blockPosition();
        Biome biome = clientLevel.getBiome(blockPos).value();
        float f = deltaTracker.getGameTimeDeltaTicks();
        float g = deltaTracker.getGameTimeDeltaPartialTick(false);
        boolean bl = biome.hasPrecipitation();
        float h = Mth.clamp(((float)clientLevel.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(blockPos) - 8.0f) / 7.0f, 0.0f, 1.0f);
        float i = clientLevel.getRainLevel(g) * h * (bl ? 1.0f : 0.5f);
        this.rainFogMultiplier += (i - this.rainFogMultiplier) * f * 0.2f;
    }

    @Override
    public boolean isApplicable(@Nullable FogType fogType, Entity entity) {
        return fogType == FogType.ATMOSPHERIC;
    }
}

