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
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugStructureInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class StructureRenderer
implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        debugValueAccess.forEachChunk(DebugSubscriptions.STRUCTURES, (chunkPos, list) -> {
            for (DebugStructureInfo debugStructureInfo : list) {
                Gizmos.cuboid(AABB.of(debugStructureInfo.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f)));
                for (DebugStructureInfo.Piece piece : debugStructureInfo.pieces()) {
                    if (piece.isStart()) {
                        Gizmos.cuboid(AABB.of(piece.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.0f, 1.0f, 0.0f)));
                        continue;
                    }
                    Gizmos.cuboid(AABB.of(piece.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.0f, 0.0f, 1.0f)));
                }
            }
        });
    }
}

