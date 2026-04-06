/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer;

import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public enum FaceInfo {
    DOWN(new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MAX_Z), new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MAX_Z)),
    UP(new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MIN_Z), new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MIN_Z)),
    NORTH(new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MIN_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MIN_Z)),
    SOUTH(new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MAX_Z), new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MAX_Z)),
    WEST(new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MIN_Z), new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MIN_X, Extent.MIN_Y, Extent.MAX_Z), new VertexInfo(Extent.MIN_X, Extent.MAX_Y, Extent.MAX_Z)),
    EAST(new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MAX_Z), new VertexInfo(Extent.MAX_X, Extent.MIN_Y, Extent.MIN_Z), new VertexInfo(Extent.MAX_X, Extent.MAX_Y, Extent.MIN_Z));

    private static final Map<Direction, FaceInfo> BY_FACING;
    private final VertexInfo[] infos;

    public static FaceInfo fromFacing(Direction direction) {
        return BY_FACING.get(direction);
    }

    private FaceInfo(VertexInfo ... vertexInfos) {
        this.infos = vertexInfos;
    }

    public VertexInfo getVertexInfo(int i) {
        return this.infos[i];
    }

    static {
        BY_FACING = Util.make(new EnumMap(Direction.class), enumMap -> {
            enumMap.put(Direction.DOWN, DOWN);
            enumMap.put(Direction.UP, UP);
            enumMap.put(Direction.NORTH, NORTH);
            enumMap.put(Direction.SOUTH, SOUTH);
            enumMap.put(Direction.WEST, WEST);
            enumMap.put(Direction.EAST, EAST);
        });
    }

    @Environment(value=EnvType.CLIENT)
    public record VertexInfo(Extent xFace, Extent yFace, Extent zFace) {
        public Vector3f select(Vector3fc vector3fc, Vector3fc vector3fc2) {
            return new Vector3f(this.xFace.select(vector3fc, vector3fc2), this.yFace.select(vector3fc, vector3fc2), this.zFace.select(vector3fc, vector3fc2));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Extent {
        MIN_X,
        MIN_Y,
        MIN_Z,
        MAX_X,
        MAX_Y,
        MAX_Z;


        public float select(Vector3fc vector3fc, Vector3fc vector3fc2) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> vector3fc.x();
                case 1 -> vector3fc.y();
                case 2 -> vector3fc.z();
                case 3 -> vector3fc2.x();
                case 4 -> vector3fc2.y();
                case 5 -> vector3fc2.z();
            };
        }

        public float select(float f, float g, float h, float i, float j, float k) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> f;
                case 1 -> g;
                case 2 -> h;
                case 3 -> i;
                case 4 -> j;
                case 5 -> k;
            };
        }
    }
}

