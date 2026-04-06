/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.gizmos;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalDouble;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public record TextGizmo(Vec3 pos, String text, Style style) implements Gizmo
{
    @Override
    public void emit(GizmoPrimitives gizmoPrimitives, float f) {
        Style style = f < 1.0f ? new Style(ARGB.multiplyAlpha(this.style.color, f), this.style.scale, this.style.adjustLeft) : this.style;
        gizmoPrimitives.addText(this.pos, this.text, style);
    }

    public static final class Style
    extends Record {
        final int color;
        final float scale;
        final OptionalDouble adjustLeft;
        public static final float DEFAULT_SCALE = 0.32f;

        public Style(int i, float f, OptionalDouble optionalDouble) {
            this.color = i;
            this.scale = f;
            this.adjustLeft = optionalDouble;
        }

        public static Style whiteAndCentered() {
            return new Style(-1, 0.32f, OptionalDouble.empty());
        }

        public static Style forColorAndCentered(int i) {
            return new Style(i, 0.32f, OptionalDouble.empty());
        }

        public static Style forColor(int i) {
            return new Style(i, 0.32f, OptionalDouble.of(0.0));
        }

        public Style withScale(float f) {
            return new Style(this.color, f, this.adjustLeft);
        }

        public Style withLeftAlignment(float f) {
            return new Style(this.color, this.scale, OptionalDouble.of(f));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this, object);
        }

        public int color() {
            return this.color;
        }

        public float scale() {
            return this.scale;
        }

        public OptionalDouble adjustLeft() {
            return this.adjustLeft;
        }
    }
}

