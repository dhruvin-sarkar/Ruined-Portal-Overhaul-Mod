/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record ArrowGizmo(Vec3 start, Vec3 end, int color, float width) implements Gizmo
{
    public static final float DEFAULT_WIDTH = 2.5f;

    @Override
    public void emit(GizmoPrimitives gizmoPrimitives, float f) {
        Vector3f[] vector3fs;
        int i = ARGB.multiplyAlpha(this.color, f);
        gizmoPrimitives.addLine(this.start, this.end, i, this.width);
        Quaternionf quaternionf = new Quaternionf().rotationTo((Vector3fc)new Vector3f(1.0f, 0.0f, 0.0f), (Vector3fc)this.end.subtract(this.start).toVector3f().normalize());
        float g = (float)Mth.clamp(this.end.distanceTo(this.start) * (double)0.1f, (double)0.1f, 1.0);
        for (Vector3f vector3f : vector3fs = new Vector3f[]{quaternionf.transform(-g, g, 0.0f, new Vector3f()), quaternionf.transform(-g, 0.0f, g, new Vector3f()), quaternionf.transform(-g, -g, 0.0f, new Vector3f()), quaternionf.transform(-g, 0.0f, -g, new Vector3f())}) {
            gizmoPrimitives.addLine(this.end.add(vector3f.x, vector3f.y, vector3f.z), this.end, i, this.width);
        }
    }
}

