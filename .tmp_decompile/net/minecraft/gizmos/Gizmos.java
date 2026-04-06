/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gizmos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.ArrowGizmo;
import net.minecraft.gizmos.CircleGizmo;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.LineGizmo;
import net.minecraft.gizmos.PointGizmo;
import net.minecraft.gizmos.RectGizmo;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Gizmos {
    static final ThreadLocal<@Nullable GizmoCollector> collector = new ThreadLocal();

    private Gizmos() {
    }

    public static TemporaryCollection withCollector(GizmoCollector gizmoCollector) {
        TemporaryCollection temporaryCollection = new TemporaryCollection();
        collector.set(gizmoCollector);
        return temporaryCollection;
    }

    public static GizmoProperties addGizmo(Gizmo gizmo) {
        GizmoCollector gizmoCollector = collector.get();
        if (gizmoCollector == null) {
            throw new IllegalStateException("Gizmos cannot be created here! No GizmoCollector has been registered.");
        }
        return gizmoCollector.add(gizmo);
    }

    public static GizmoProperties cuboid(AABB aABB, GizmoStyle gizmoStyle) {
        return Gizmos.cuboid(aABB, gizmoStyle, false);
    }

    public static GizmoProperties cuboid(AABB aABB, GizmoStyle gizmoStyle, boolean bl) {
        return Gizmos.addGizmo(new CuboidGizmo(aABB, gizmoStyle, bl));
    }

    public static GizmoProperties cuboid(BlockPos blockPos, GizmoStyle gizmoStyle) {
        return Gizmos.cuboid(new AABB(blockPos), gizmoStyle);
    }

    public static GizmoProperties cuboid(BlockPos blockPos, float f, GizmoStyle gizmoStyle) {
        return Gizmos.cuboid(new AABB(blockPos).inflate(f), gizmoStyle);
    }

    public static GizmoProperties circle(Vec3 vec3, float f, GizmoStyle gizmoStyle) {
        return Gizmos.addGizmo(new CircleGizmo(vec3, f, gizmoStyle));
    }

    public static GizmoProperties line(Vec3 vec3, Vec3 vec32, int i) {
        return Gizmos.addGizmo(new LineGizmo(vec3, vec32, i, 3.0f));
    }

    public static GizmoProperties line(Vec3 vec3, Vec3 vec32, int i, float f) {
        return Gizmos.addGizmo(new LineGizmo(vec3, vec32, i, f));
    }

    public static GizmoProperties arrow(Vec3 vec3, Vec3 vec32, int i) {
        return Gizmos.addGizmo(new ArrowGizmo(vec3, vec32, i, 2.5f));
    }

    public static GizmoProperties arrow(Vec3 vec3, Vec3 vec32, int i, float f) {
        return Gizmos.addGizmo(new ArrowGizmo(vec3, vec32, i, f));
    }

    public static GizmoProperties rect(Vec3 vec3, Vec3 vec32, Direction direction, GizmoStyle gizmoStyle) {
        return Gizmos.addGizmo(RectGizmo.fromCuboidFace(vec3, vec32, direction, gizmoStyle));
    }

    public static GizmoProperties rect(Vec3 vec3, Vec3 vec32, Vec3 vec33, Vec3 vec34, GizmoStyle gizmoStyle) {
        return Gizmos.addGizmo(new RectGizmo(vec3, vec32, vec33, vec34, gizmoStyle));
    }

    public static GizmoProperties point(Vec3 vec3, int i, float f) {
        return Gizmos.addGizmo(new PointGizmo(vec3, i, f));
    }

    public static GizmoProperties billboardTextOverBlock(String string, BlockPos blockPos, int i, int j, float f) {
        double d = 1.3;
        double e = 0.2;
        GizmoProperties gizmoProperties = Gizmos.billboardText(string, Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.3 + (double)i * 0.2, 0.5), TextGizmo.Style.forColorAndCentered(j).withScale(f));
        gizmoProperties.setAlwaysOnTop();
        return gizmoProperties;
    }

    public static GizmoProperties billboardTextOverMob(Entity entity, int i, String string, int j, float f) {
        double d = 2.4;
        double e = 0.25;
        double g = (double)entity.getBlockX() + 0.5;
        double h = entity.getY() + 2.4 + (double)i * 0.25;
        double k = (double)entity.getBlockZ() + 0.5;
        float l = 0.5f;
        GizmoProperties gizmoProperties = Gizmos.billboardText(string, new Vec3(g, h, k), TextGizmo.Style.forColor(j).withScale(f).withLeftAlignment(0.5f));
        gizmoProperties.setAlwaysOnTop();
        return gizmoProperties;
    }

    public static GizmoProperties billboardText(String string, Vec3 vec3, TextGizmo.Style style) {
        return Gizmos.addGizmo(new TextGizmo(vec3, string, style));
    }

    public static class TemporaryCollection
    implements AutoCloseable {
        private final @Nullable GizmoCollector old = collector.get();
        private boolean closed;

        TemporaryCollection() {
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                collector.set(this.old);
            }
        }
    }
}

