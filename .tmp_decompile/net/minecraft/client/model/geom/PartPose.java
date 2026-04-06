/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record PartPose(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
    public static final PartPose ZERO = PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    public static PartPose offset(float f, float g, float h) {
        return PartPose.offsetAndRotation(f, g, h, 0.0f, 0.0f, 0.0f);
    }

    public static PartPose rotation(float f, float g, float h) {
        return PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, f, g, h);
    }

    public static PartPose offsetAndRotation(float f, float g, float h, float i, float j, float k) {
        return new PartPose(f, g, h, i, j, k, 1.0f, 1.0f, 1.0f);
    }

    public PartPose translated(float f, float g, float h) {
        return new PartPose(this.x + f, this.y + g, this.z + h, this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
    }

    public PartPose withScale(float f) {
        return new PartPose(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot, f, f, f);
    }

    public PartPose scaled(float f) {
        if (f == 1.0f) {
            return this;
        }
        return this.scaled(f, f, f);
    }

    public PartPose scaled(float f, float g, float h) {
        return new PartPose(this.x * f, this.y * g, this.z * h, this.xRot, this.yRot, this.zRot, this.xScale * f, this.yScale * g, this.zScale * h);
    }
}

