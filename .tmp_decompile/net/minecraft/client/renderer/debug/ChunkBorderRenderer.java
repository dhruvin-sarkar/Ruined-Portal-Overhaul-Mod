/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final float THICK_WIDTH = 4.0f;
    private static final float THIN_WIDTH = 1.0f;
    private final Minecraft minecraft;
    private static final int CELL_BORDER = ARGB.color(255, 0, 155, 155);
    private static final int YELLOW = ARGB.color(255, 255, 255, 0);
    private static final int MAJOR_LINES = ARGB.colorFromFloat(1.0f, 0.25f, 0.25f, 1.0f);

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        int m;
        int l;
        Entity entity = this.minecraft.gameRenderer.getMainCamera().entity();
        float h = this.minecraft.level.getMinY();
        float i = this.minecraft.level.getMaxY() + 1;
        SectionPos sectionPos = SectionPos.of(entity.blockPosition());
        double j = sectionPos.minBlockX();
        double k = sectionPos.minBlockZ();
        for (l = -16; l <= 32; l += 16) {
            for (m = -16; m <= 32; m += 16) {
                Gizmos.line(new Vec3(j + (double)l, h, k + (double)m), new Vec3(j + (double)l, i, k + (double)m), ARGB.colorFromFloat(0.5f, 1.0f, 0.0f, 0.0f), 4.0f);
            }
        }
        for (l = 2; l < 16; l += 2) {
            m = l % 4 == 0 ? CELL_BORDER : YELLOW;
            Gizmos.line(new Vec3(j + (double)l, h, k), new Vec3(j + (double)l, i, k), m, 1.0f);
            Gizmos.line(new Vec3(j + (double)l, h, k + 16.0), new Vec3(j + (double)l, i, k + 16.0), m, 1.0f);
        }
        for (l = 2; l < 16; l += 2) {
            m = l % 4 == 0 ? CELL_BORDER : YELLOW;
            Gizmos.line(new Vec3(j, h, k + (double)l), new Vec3(j, i, k + (double)l), m, 1.0f);
            Gizmos.line(new Vec3(j + 16.0, h, k + (double)l), new Vec3(j + 16.0, i, k + (double)l), m, 1.0f);
        }
        for (l = this.minecraft.level.getMinY(); l <= this.minecraft.level.getMaxY() + 1; l += 2) {
            float n = l;
            int o = l % 8 == 0 ? CELL_BORDER : YELLOW;
            Gizmos.line(new Vec3(j, n, k), new Vec3(j, n, k + 16.0), o, 1.0f);
            Gizmos.line(new Vec3(j, n, k + 16.0), new Vec3(j + 16.0, n, k + 16.0), o, 1.0f);
            Gizmos.line(new Vec3(j + 16.0, n, k + 16.0), new Vec3(j + 16.0, n, k), o, 1.0f);
            Gizmos.line(new Vec3(j + 16.0, n, k), new Vec3(j, n, k), o, 1.0f);
        }
        for (l = 0; l <= 16; l += 16) {
            for (int m2 = 0; m2 <= 16; m2 += 16) {
                Gizmos.line(new Vec3(j + (double)l, h, k + (double)m2), new Vec3(j + (double)l, i, k + (double)m2), MAJOR_LINES, 4.0f);
            }
        }
        Gizmos.cuboid(new AABB(sectionPos.minBlockX(), sectionPos.minBlockY(), sectionPos.minBlockZ(), sectionPos.maxBlockX() + 1, sectionPos.maxBlockY() + 1, sectionPos.maxBlockZ() + 1), GizmoStyle.stroke(MAJOR_LINES, 1.0f)).setAlwaysOnTop();
        for (l = this.minecraft.level.getMinY(); l <= this.minecraft.level.getMaxY() + 1; l += 16) {
            Gizmos.line(new Vec3(j, l, k), new Vec3(j, l, k + 16.0), MAJOR_LINES, 4.0f);
            Gizmos.line(new Vec3(j, l, k + 16.0), new Vec3(j + 16.0, l, k + 16.0), MAJOR_LINES, 4.0f);
            Gizmos.line(new Vec3(j + 16.0, l, k + 16.0), new Vec3(j + 16.0, l, k), MAJOR_LINES, 4.0f);
            Gizmos.line(new Vec3(j + 16.0, l, k), new Vec3(j, l, k), MAJOR_LINES, 4.0f);
        }
    }
}

