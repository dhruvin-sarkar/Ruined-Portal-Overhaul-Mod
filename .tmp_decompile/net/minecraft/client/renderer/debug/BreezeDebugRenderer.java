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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class BreezeDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final int JUMP_TARGET_LINE_COLOR = ARGB.color(255, 255, 100, 255);
    private static final int TARGET_LINE_COLOR = ARGB.color(255, 100, 255, 255);
    private static final int INNER_CIRCLE_COLOR = ARGB.color(255, 0, 255, 0);
    private static final int MIDDLE_CIRCLE_COLOR = ARGB.color(255, 255, 165, 0);
    private static final int OUTER_CIRCLE_COLOR = ARGB.color(255, 255, 0, 0);
    private final Minecraft minecraft;

    public BreezeDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        ClientLevel clientLevel = this.minecraft.level;
        debugValueAccess.forEachEntity(DebugSubscriptions.BREEZES, (entity2, debugBreezeInfo) -> {
            debugBreezeInfo.attackTarget().map(clientLevel::getEntity).map(entity -> entity.getPosition(this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true))).ifPresent(vec3 -> {
                Gizmos.arrow(entity2.position(), vec3, TARGET_LINE_COLOR);
                Vec3 vec32 = vec3.add(0.0, 0.01f, 0.0);
                Gizmos.circle(vec32, 4.0f, GizmoStyle.stroke(INNER_CIRCLE_COLOR));
                Gizmos.circle(vec32, 8.0f, GizmoStyle.stroke(MIDDLE_CIRCLE_COLOR));
                Gizmos.circle(vec32, 24.0f, GizmoStyle.stroke(OUTER_CIRCLE_COLOR));
            });
            debugBreezeInfo.jumpTarget().ifPresent(blockPos -> {
                Gizmos.arrow(entity2.position(), blockPos.getCenter(), JUMP_TARGET_LINE_COLOR);
                Gizmos.cuboid(AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos)), GizmoStyle.fill(ARGB.colorFromFloat(1.0f, 1.0f, 0.0f, 0.0f)));
            });
        });
    }
}

