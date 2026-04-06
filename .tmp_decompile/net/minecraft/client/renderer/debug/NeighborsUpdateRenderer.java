/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.debug;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class NeighborsUpdateRenderer
implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        LastUpdate lastUpdate;
        BlockPos blockPos2;
        int i2 = DebugSubscriptions.NEIGHBOR_UPDATES.expireAfterTicks();
        double h = 1.0 / (double)(i2 * 2);
        HashMap map = new HashMap();
        debugValueAccess.forEachEvent(DebugSubscriptions.NEIGHBOR_UPDATES, (blockPos, i, j) -> {
            long l = j - i;
            LastUpdate lastUpdate = map.getOrDefault(blockPos, LastUpdate.NONE);
            map.put(blockPos, lastUpdate.tryCount((int)l));
        });
        for (Map.Entry entry : map.entrySet()) {
            blockPos2 = (BlockPos)entry.getKey();
            lastUpdate = (LastUpdate)((Object)entry.getValue());
            AABB aABB = new AABB(blockPos2).inflate(0.002).deflate(h * (double)lastUpdate.age);
            Gizmos.cuboid(aABB, GizmoStyle.stroke(-1));
        }
        for (Map.Entry entry : map.entrySet()) {
            blockPos2 = (BlockPos)entry.getKey();
            lastUpdate = (LastUpdate)((Object)entry.getValue());
            Gizmos.billboardText(String.valueOf(lastUpdate.count), Vec3.atCenterOf(blockPos2), TextGizmo.Style.whiteAndCentered());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class LastUpdate
    extends Record {
        final int count;
        final int age;
        static final LastUpdate NONE = new LastUpdate(0, Integer.MAX_VALUE);

        private LastUpdate(int i, int j) {
            this.count = i;
            this.age = j;
        }

        public LastUpdate tryCount(int i) {
            if (i == this.age) {
                return new LastUpdate(this.count + 1, i);
            }
            if (i < this.age) {
                return new LastUpdate(1, i);
            }
            return this;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LastUpdate.class, "count;age", "count", "age"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LastUpdate.class, "count;age", "count", "age"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LastUpdate.class, "count;age", "count", "age"}, this, object);
        }

        public int count() {
            return this.count;
        }

        public int age() {
            return this.age;
        }
    }
}

