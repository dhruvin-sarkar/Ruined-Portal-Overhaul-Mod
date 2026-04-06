/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
    private static final float DEFAULT_X_ROT = 0.2617994f;
    private static final float DEFAULT_Z_ROT = -0.2617994f;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float rotXOld;
    private float rotYOld;
    private float rotZOld;
    private final LivingEntity entity;

    public ElytraAnimationState(LivingEntity livingEntity) {
        this.entity = livingEntity;
    }

    public void tick() {
        float i;
        float h;
        float g;
        this.rotXOld = this.rotX;
        this.rotYOld = this.rotY;
        this.rotZOld = this.rotZ;
        if (this.entity.isFallFlying()) {
            float f = 1.0f;
            Vec3 vec3 = this.entity.getDeltaMovement();
            if (vec3.y < 0.0) {
                Vec3 vec32 = vec3.normalize();
                f = 1.0f - (float)Math.pow(-vec32.y, 1.5);
            }
            g = Mth.lerp(f, 0.2617994f, 0.34906584f);
            h = Mth.lerp(f, -0.2617994f, -1.5707964f);
            i = 0.0f;
        } else if (this.entity.isCrouching()) {
            g = 0.6981317f;
            h = -0.7853982f;
            i = 0.08726646f;
        } else {
            g = 0.2617994f;
            h = -0.2617994f;
            i = 0.0f;
        }
        this.rotX += (g - this.rotX) * 0.3f;
        this.rotY += (i - this.rotY) * 0.3f;
        this.rotZ += (h - this.rotZ) * 0.3f;
    }

    public float getRotX(float f) {
        return Mth.lerp(f, this.rotXOld, this.rotX);
    }

    public float getRotY(float f) {
        return Mth.lerp(f, this.rotYOld, this.rotY);
    }

    public float getRotZ(float f) {
        return Mth.lerp(f, this.rotZOld, this.rotZ);
    }
}

