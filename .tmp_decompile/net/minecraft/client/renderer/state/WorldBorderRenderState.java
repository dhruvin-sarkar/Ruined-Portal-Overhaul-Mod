/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.state;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

@Environment(value=EnvType.CLIENT)
public class WorldBorderRenderState {
    public double minX;
    public double maxX;
    public double minZ;
    public double maxZ;
    public int tint;
    public double alpha;

    public List<DistancePerDirection> closestBorder(double d, double e) {
        DistancePerDirection[] distancePerDirections = new DistancePerDirection[]{new DistancePerDirection(Direction.NORTH, e - this.minZ), new DistancePerDirection(Direction.SOUTH, this.maxZ - e), new DistancePerDirection(Direction.WEST, d - this.minX), new DistancePerDirection(Direction.EAST, this.maxX - d)};
        return Arrays.stream(distancePerDirections).sorted(Comparator.comparingDouble(distancePerDirection -> distancePerDirection.distance)).toList();
    }

    public void reset() {
        this.alpha = 0.0;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class DistancePerDirection
    extends Record {
        private final Direction direction;
        final double distance;

        public DistancePerDirection(Direction direction, double d) {
            this.direction = direction;
            this.distance = d;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this, object);
        }

        public Direction direction() {
            return this.direction;
        }

        public double distance() {
            return this.distance;
        }
    }
}

