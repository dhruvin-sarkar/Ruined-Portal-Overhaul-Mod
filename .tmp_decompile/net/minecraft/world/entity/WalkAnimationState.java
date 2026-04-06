/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
    private float speedOld;
    private float speed;
    private float position;
    private float positionScale = 1.0f;

    public void setSpeed(float f) {
        this.speed = f;
    }

    public void update(float f, float g, float h) {
        this.speedOld = this.speed;
        this.speed += (f - this.speed) * g;
        this.position += this.speed;
        this.positionScale = h;
    }

    public void stop() {
        this.speedOld = 0.0f;
        this.speed = 0.0f;
        this.position = 0.0f;
    }

    public float speed() {
        return this.speed;
    }

    public float speed(float f) {
        return Math.min(Mth.lerp(f, this.speedOld, this.speed), 1.0f);
    }

    public float position() {
        return this.position * this.positionScale;
    }

    public float position(float f) {
        return (this.position - this.speed * (1.0f - f)) * this.positionScale;
    }

    public boolean isMoving() {
        return this.speed > 1.0E-5f;
    }
}

