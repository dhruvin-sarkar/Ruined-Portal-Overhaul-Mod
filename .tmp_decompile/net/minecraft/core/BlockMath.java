/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.math.MatrixUtil;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class BlockMath {
    private static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Maps.newEnumMap((Map)Map.of((Object)Direction.SOUTH, (Object)Transformation.identity(), (Object)Direction.EAST, (Object)new Transformation(null, (Quaternionfc)new Quaternionf().rotateY(1.5707964f), null, null), (Object)Direction.WEST, (Object)new Transformation(null, (Quaternionfc)new Quaternionf().rotateY(-1.5707964f), null, null), (Object)Direction.NORTH, (Object)new Transformation(null, (Quaternionfc)new Quaternionf().rotateY((float)Math.PI), null, null), (Object)Direction.UP, (Object)new Transformation(null, (Quaternionfc)new Quaternionf().rotateX(-1.5707964f), null, null), (Object)Direction.DOWN, (Object)new Transformation(null, (Quaternionfc)new Quaternionf().rotateX(1.5707964f), null, null)));
    private static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Maps.newEnumMap(Util.mapValues(VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL, Transformation::inverse));

    public static Transformation blockCenterToCorner(Transformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new Transformation((Matrix4fc)matrix4f);
    }

    public static Transformation blockCornerToCenter(Transformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new Transformation((Matrix4fc)matrix4f);
    }

    public static Transformation getFaceTransformation(Transformation transformation, Direction direction) {
        if (MatrixUtil.isIdentity(transformation.getMatrix())) {
            return transformation;
        }
        Transformation transformation2 = VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction);
        transformation2 = transformation.compose(transformation2);
        Vector3f vector3f = transformation2.getMatrix().transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
        Direction direction2 = Direction.getApproximateNearest(vector3f.x, vector3f.y, vector3f.z);
        return VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get(direction2).compose(transformation2);
    }
}

