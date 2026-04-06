/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EntityHitboxDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    final Minecraft minecraft;

    public EntityHitboxDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        if (this.minecraft.level == null) {
            return;
        }
        for (Entity entity : this.minecraft.level.entitiesForRendering()) {
            if (entity.isInvisible() || !frustum.isVisible(entity.getBoundingBox()) || entity == this.minecraft.getCameraEntity() && this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) continue;
            this.showHitboxes(entity, g, false);
            if (!SharedConstants.DEBUG_SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES) continue;
            Entity entity2 = this.getServerEntity(entity);
            if (entity2 != null) {
                this.showHitboxes(entity, g, true);
                continue;
            }
            Gizmos.billboardText("Missing Server Entity", entity.getPosition(g).add(0.0, entity.getBoundingBox().getYsize() + 1.5, 0.0), TextGizmo.Style.forColorAndCentered(-65536));
        }
    }

    private @Nullable Entity getServerEntity(Entity entity) {
        ServerLevel serverLevel;
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        if (integratedServer != null && (serverLevel = integratedServer.getLevel(entity.level().dimension())) != null) {
            return serverLevel.getEntity(entity.getId());
        }
        return null;
    }

    private void showHitboxes(Entity entity, float f, boolean bl) {
        Vec3 vec34;
        float h;
        Vec3 vec3 = entity.position();
        Vec3 vec32 = entity.getPosition(f);
        Vec3 vec33 = vec32.subtract(vec3);
        int i = bl ? -16711936 : -1;
        Gizmos.cuboid(entity.getBoundingBox().move(vec33), GizmoStyle.stroke(i));
        Gizmos.point(vec32, i, 2.0f);
        Entity entity2 = entity.getVehicle();
        if (entity2 != null) {
            float g = Math.min(entity2.getBbWidth(), entity.getBbWidth()) / 2.0f;
            h = 0.0625f;
            vec34 = entity2.getPassengerRidingPosition(entity).add(vec33);
            Gizmos.cuboid(new AABB(vec34.x - (double)g, vec34.y, vec34.z - (double)g, vec34.x + (double)g, vec34.y + 0.0625, vec34.z + (double)g), GizmoStyle.stroke(-256));
        }
        if (entity instanceof LivingEntity) {
            AABB aABB = entity.getBoundingBox().move(vec33);
            h = 0.01f;
            Gizmos.cuboid(new AABB(aABB.minX, aABB.minY + (double)entity.getEyeHeight() - (double)0.01f, aABB.minZ, aABB.maxX, aABB.minY + (double)entity.getEyeHeight() + (double)0.01f, aABB.maxZ), GizmoStyle.stroke(-65536));
        }
        if (entity instanceof EnderDragon) {
            EnderDragon enderDragon = (EnderDragon)entity;
            for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
                Vec3 vec35 = enderDragonPart.position();
                Vec3 vec36 = enderDragonPart.getPosition(f);
                Vec3 vec37 = vec36.subtract(vec35);
                Gizmos.cuboid(enderDragonPart.getBoundingBox().move(vec37), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.25f, 1.0f, 0.0f)));
            }
        }
        Vec3 vec38 = vec32.add(0.0, entity.getEyeHeight(), 0.0);
        Vec3 vec39 = entity.getViewVector(f);
        Gizmos.arrow(vec38, vec38.add(vec39.scale(2.0)), -16776961);
        if (bl) {
            vec34 = entity.getDeltaMovement();
            Gizmos.arrow(vec32, vec32.add(vec34), -256);
        }
    }
}

