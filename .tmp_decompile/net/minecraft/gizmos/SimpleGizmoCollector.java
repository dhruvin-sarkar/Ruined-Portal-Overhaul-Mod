/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class SimpleGizmoCollector
implements GizmoCollector {
    private final List<GizmoInstance> gizmos = new ArrayList<GizmoInstance>();
    private final List<GizmoInstance> temporaryGizmos = new ArrayList<GizmoInstance>();

    @Override
    public GizmoProperties add(Gizmo gizmo) {
        GizmoInstance gizmoInstance = new GizmoInstance(gizmo);
        this.gizmos.add(gizmoInstance);
        return gizmoInstance;
    }

    public List<GizmoInstance> drainGizmos() {
        ArrayList<GizmoInstance> arrayList = new ArrayList<GizmoInstance>(this.gizmos);
        arrayList.addAll(this.temporaryGizmos);
        long l = Util.getMillis();
        this.gizmos.removeIf(gizmoInstance -> gizmoInstance.getExpireTimeMillis() < l);
        this.temporaryGizmos.clear();
        return arrayList;
    }

    public List<GizmoInstance> getGizmos() {
        return this.gizmos;
    }

    public void addTemporaryGizmos(Collection<GizmoInstance> collection) {
        this.temporaryGizmos.addAll(collection);
    }

    public static class GizmoInstance
    implements GizmoProperties {
        private final Gizmo gizmo;
        private boolean isAlwaysOnTop;
        private long startTimeMillis;
        private long expireTimeMillis;
        private boolean shouldFadeOut;

        GizmoInstance(Gizmo gizmo) {
            this.gizmo = gizmo;
        }

        @Override
        public GizmoProperties setAlwaysOnTop() {
            this.isAlwaysOnTop = true;
            return this;
        }

        @Override
        public GizmoProperties persistForMillis(int i) {
            this.startTimeMillis = Util.getMillis();
            this.expireTimeMillis = this.startTimeMillis + (long)i;
            return this;
        }

        @Override
        public GizmoProperties fadeOut() {
            this.shouldFadeOut = true;
            return this;
        }

        public float getAlphaMultiplier(long l) {
            if (this.shouldFadeOut) {
                long m = this.expireTimeMillis - this.startTimeMillis;
                long n = l - this.startTimeMillis;
                return 1.0f - Mth.clamp((float)n / (float)m, 0.0f, 1.0f);
            }
            return 1.0f;
        }

        public boolean isAlwaysOnTop() {
            return this.isAlwaysOnTop;
        }

        public long getExpireTimeMillis() {
            return this.expireTimeMillis;
        }

        public Gizmo gizmo() {
            return this.gizmo;
        }
    }
}

