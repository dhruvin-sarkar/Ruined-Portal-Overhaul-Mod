/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ClientAvatarState {
    private Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    private float walkDist;
    private float walkDistO;
    private double xCloak;
    private double yCloak;
    private double zCloak;
    private double xCloakO;
    private double yCloakO;
    private double zCloakO;
    private float bob;
    private float bobO;

    public void tick(Vec3 vec3, Vec3 vec32) {
        this.walkDistO = this.walkDist;
        this.deltaMovementOnPreviousTick = vec32;
        this.moveCloak(vec3);
    }

    public void addWalkDistance(float f) {
        this.walkDist += f;
    }

    public Vec3 deltaMovementOnPreviousTick() {
        return this.deltaMovementOnPreviousTick;
    }

    private void moveCloak(Vec3 vec3) {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d = vec3.x() - this.xCloak;
        double e = vec3.y() - this.yCloak;
        double f = vec3.z() - this.zCloak;
        double g = 10.0;
        if (d > 10.0 || d < -10.0) {
            this.xCloakO = this.xCloak = vec3.x();
        } else {
            this.xCloak += d * 0.25;
        }
        if (e > 10.0 || e < -10.0) {
            this.yCloakO = this.yCloak = vec3.y();
        } else {
            this.yCloak += e * 0.25;
        }
        if (f > 10.0 || f < -10.0) {
            this.zCloakO = this.zCloak = vec3.z();
        } else {
            this.zCloak += f * 0.25;
        }
    }

    public double getInterpolatedCloakX(float f) {
        return Mth.lerp((double)f, this.xCloakO, this.xCloak);
    }

    public double getInterpolatedCloakY(float f) {
        return Mth.lerp((double)f, this.yCloakO, this.yCloak);
    }

    public double getInterpolatedCloakZ(float f) {
        return Mth.lerp((double)f, this.zCloakO, this.zCloak);
    }

    public void updateBob(float f) {
        this.bobO = this.bob;
        this.bob += (f - this.bob) * 0.4f;
    }

    public void resetBob() {
        this.bobO = this.bob;
        this.bob = 0.0f;
    }

    public float getInterpolatedBob(float f) {
        return Mth.lerp(f, this.bobO, this.bob);
    }

    public float getBackwardsInterpolatedWalkDistance(float f) {
        float g = this.walkDist - this.walkDistO;
        return -(this.walkDist + g * f);
    }

    public float getInterpolatedWalkDistance(float f) {
        return Mth.lerp(f, this.walkDistO, this.walkDist);
    }
}

