/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public enum EntityAttachment {
    PASSENGER(Fallback.AT_HEIGHT),
    VEHICLE(Fallback.AT_FEET),
    NAME_TAG(Fallback.AT_HEIGHT),
    WARDEN_CHEST(Fallback.AT_CENTER);

    private final Fallback fallback;

    private EntityAttachment(Fallback fallback) {
        this.fallback = fallback;
    }

    public List<Vec3> createFallbackPoints(float f, float g) {
        return this.fallback.create(f, g);
    }

    public static interface Fallback {
        public static final List<Vec3> ZERO = List.of((Object)Vec3.ZERO);
        public static final Fallback AT_FEET = (f, g) -> ZERO;
        public static final Fallback AT_HEIGHT = (f, g) -> List.of((Object)new Vec3(0.0, g, 0.0));
        public static final Fallback AT_CENTER = (f, g) -> List.of((Object)new Vec3(0.0, (double)g / 2.0, 0.0));

        public List<Vec3> create(float var1, float var2);
    }
}

