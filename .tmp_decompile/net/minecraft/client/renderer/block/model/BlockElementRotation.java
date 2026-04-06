/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Math
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.MatrixUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record BlockElementRotation(Vector3fc origin, RotationValue value, boolean rescale, Matrix4fc transform) {
    public BlockElementRotation(Vector3fc vector3fc, RotationValue rotationValue, boolean bl) {
        this(vector3fc, rotationValue, bl, (Matrix4fc)BlockElementRotation.computeTransform(rotationValue, bl));
    }

    private static Matrix4f computeTransform(RotationValue rotationValue, boolean bl) {
        Matrix4f matrix4f = rotationValue.transformation();
        if (bl && !MatrixUtil.isIdentity((Matrix4fc)matrix4f)) {
            Vector3fc vector3fc = BlockElementRotation.computeRescale((Matrix4fc)matrix4f);
            matrix4f.scale(vector3fc);
        }
        return matrix4f;
    }

    private static Vector3fc computeRescale(Matrix4fc matrix4fc) {
        Vector3f vector3f = new Vector3f();
        float f = BlockElementRotation.scaleFactorForAxis(matrix4fc, Direction.Axis.X, vector3f);
        float g = BlockElementRotation.scaleFactorForAxis(matrix4fc, Direction.Axis.Y, vector3f);
        float h = BlockElementRotation.scaleFactorForAxis(matrix4fc, Direction.Axis.Z, vector3f);
        return vector3f.set(f, g, h);
    }

    private static float scaleFactorForAxis(Matrix4fc matrix4fc, Direction.Axis axis, Vector3f vector3f) {
        Vector3f vector3f2 = vector3f.set(axis.getPositive().getUnitVec3f());
        Vector3f vector3f3 = matrix4fc.transformDirection(vector3f2);
        float f = Math.abs((float)vector3f3.x);
        float g = Math.abs((float)vector3f3.y);
        float h = Math.abs((float)vector3f3.z);
        float i = Math.max((float)Math.max((float)f, (float)g), (float)h);
        return 1.0f / i;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface RotationValue {
        public Matrix4f transformation();
    }

    @Environment(value=EnvType.CLIENT)
    public record EulerXYZRotation(float x, float y, float z) implements RotationValue
    {
        @Override
        public Matrix4f transformation() {
            return new Matrix4f().rotationZYX(this.z * ((float)java.lang.Math.PI / 180), this.y * ((float)java.lang.Math.PI / 180), this.x * ((float)java.lang.Math.PI / 180));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record SingleAxisRotation(Direction.Axis axis, float angle) implements RotationValue
    {
        @Override
        public Matrix4f transformation() {
            Matrix4f matrix4f = new Matrix4f();
            if (this.angle == 0.0f) {
                return matrix4f;
            }
            Vector3fc vector3fc = this.axis.getPositive().getUnitVec3f();
            matrix4f.rotation(this.angle * ((float)java.lang.Math.PI / 180), vector3fc);
            return matrix4f;
        }
    }
}

