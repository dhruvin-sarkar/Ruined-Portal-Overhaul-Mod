/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityDimensions(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {
    private EntityDimensions(float f, float g, boolean bl) {
        this(f, g, EntityDimensions.defaultEyeHeight(g), EntityAttachments.createDefault(f, g), bl);
    }

    private static float defaultEyeHeight(float f) {
        return f * 0.85f;
    }

    public AABB makeBoundingBox(Vec3 vec3) {
        return this.makeBoundingBox(vec3.x, vec3.y, vec3.z);
    }

    public AABB makeBoundingBox(double d, double e, double f) {
        float g = this.width / 2.0f;
        float h = this.height;
        return new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g);
    }

    public EntityDimensions scale(float f) {
        return this.scale(f, f);
    }

    public EntityDimensions scale(float f, float g) {
        if (this.fixed || f == 1.0f && g == 1.0f) {
            return this;
        }
        return new EntityDimensions(this.width * f, this.height * g, this.eyeHeight * g, this.attachments.scale(f, g, f), false);
    }

    public static EntityDimensions scalable(float f, float g) {
        return new EntityDimensions(f, g, false);
    }

    public static EntityDimensions fixed(float f, float g) {
        return new EntityDimensions(f, g, true);
    }

    public EntityDimensions withEyeHeight(float f) {
        return new EntityDimensions(this.width, this.height, f, this.attachments, this.fixed);
    }

    public EntityDimensions withAttachments(EntityAttachments.Builder builder) {
        return new EntityDimensions(this.width, this.height, this.eyeHeight, builder.build(this.width, this.height), this.fixed);
    }
}

