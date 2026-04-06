/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public final class TranslucencyPointOfView {
    private int x;
    private int y;
    private int z;

    public static TranslucencyPointOfView of(Vec3 vec3, long l) {
        return new TranslucencyPointOfView().set(vec3, l);
    }

    public TranslucencyPointOfView set(Vec3 vec3, long l) {
        this.x = TranslucencyPointOfView.getCoordinate(vec3.x(), SectionPos.x(l));
        this.y = TranslucencyPointOfView.getCoordinate(vec3.y(), SectionPos.y(l));
        this.z = TranslucencyPointOfView.getCoordinate(vec3.z(), SectionPos.z(l));
        return this;
    }

    private static int getCoordinate(double d, int i) {
        int j = SectionPos.blockToSectionCoord(d) - i;
        return Mth.clamp(j, -1, 1);
    }

    public boolean isAxisAligned() {
        return this.x == 0 || this.y == 0 || this.z == 0;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof TranslucencyPointOfView) {
            TranslucencyPointOfView translucencyPointOfView = (TranslucencyPointOfView)object;
            return this.x == translucencyPointOfView.x && this.y == translucencyPointOfView.y && this.z == translucencyPointOfView.z;
        }
        return false;
    }
}

