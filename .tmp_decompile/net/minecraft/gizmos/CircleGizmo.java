/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;

public record CircleGizmo(Vec3 pos, float radius, GizmoStyle style) implements Gizmo
{
    private static final int CIRCLE_VERTICES = 20;
    private static final float SEGMENT_SIZE_RADIANS = 0.31415927f;

    @Override
    public void emit(GizmoPrimitives gizmoPrimitives, float f) {
        int i;
        if (!this.style.hasStroke() && !this.style.hasFill()) {
            return;
        }
        Vec3[] vec3s = new Vec3[21];
        for (i = 0; i < 20; ++i) {
            Vec3 vec3;
            float g = (float)i * 0.31415927f;
            vec3s[i] = vec3 = this.pos.add((float)((double)this.radius * Math.cos(g)), 0.0, (float)((double)this.radius * Math.sin(g)));
        }
        vec3s[20] = vec3s[0];
        if (this.style.hasFill()) {
            i = this.style.multipliedFill(f);
            gizmoPrimitives.addTriangleFan(vec3s, i);
        }
        if (this.style.hasStroke()) {
            i = this.style.multipliedStroke(f);
            for (int j = 0; j < 20; ++j) {
                gizmoPrimitives.addLine(vec3s[j], vec3s[j + 1], i, this.style.strokeWidth());
            }
        }
    }
}

