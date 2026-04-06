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
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class FogEnvironment {
    public abstract void setupFog(FogData var1, Camera var2, ClientLevel var3, float var4, DeltaTracker var5);

    public boolean providesColor() {
        return true;
    }

    public int getBaseColor(ClientLevel clientLevel, Camera camera, int i, float f) {
        return -1;
    }

    public boolean modifiesDarkness() {
        return false;
    }

    public float getModifiedDarkness(LivingEntity livingEntity, float f, float g) {
        return f;
    }

    public abstract boolean isApplicable(@Nullable FogType var1, Entity var2);
}

