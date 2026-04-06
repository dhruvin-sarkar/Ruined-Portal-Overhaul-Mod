/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.FrustumIntersection
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package net.minecraft.client.renderer.culling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final FrustumIntersection intersection = new FrustumIntersection();
    private final Matrix4f matrix = new Matrix4f();
    private Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;

    public Frustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        this.calculateFrustum(matrix4f, matrix4f2);
    }

    public Frustum(Frustum frustum) {
        this.intersection.set((Matrix4fc)frustum.matrix);
        this.matrix.set((Matrix4fc)frustum.matrix);
        this.camX = frustum.camX;
        this.camY = frustum.camY;
        this.camZ = frustum.camZ;
        this.viewVector = frustum.viewVector;
    }

    public Frustum offset(float f) {
        this.camX += (double)(this.viewVector.x * f);
        this.camY += (double)(this.viewVector.y * f);
        this.camZ += (double)(this.viewVector.z * f);
        return this;
    }

    public Frustum offsetToFullyIncludeCameraCube(int i) {
        double d = Math.floor(this.camX / (double)i) * (double)i;
        double e = Math.floor(this.camY / (double)i) * (double)i;
        double f = Math.floor(this.camZ / (double)i) * (double)i;
        double g = Math.ceil(this.camX / (double)i) * (double)i;
        double h = Math.ceil(this.camY / (double)i) * (double)i;
        double j = Math.ceil(this.camZ / (double)i) * (double)i;
        while (this.intersection.intersectAab((float)(d - this.camX), (float)(e - this.camY), (float)(f - this.camZ), (float)(g - this.camX), (float)(h - this.camY), (float)(j - this.camZ)) != -2) {
            this.camX -= (double)(this.viewVector.x() * 4.0f);
            this.camY -= (double)(this.viewVector.y() * 4.0f);
            this.camZ -= (double)(this.viewVector.z() * 4.0f);
        }
        return this;
    }

    public void prepare(double d, double e, double f) {
        this.camX = d;
        this.camY = e;
        this.camZ = f;
    }

    private void calculateFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        matrix4f2.mul((Matrix4fc)matrix4f, this.matrix);
        this.intersection.set((Matrix4fc)this.matrix);
        this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
    }

    public boolean isVisible(AABB aABB) {
        int i = this.cubeInFrustum(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
        return i == -2 || i == -1;
    }

    public int cubeInFrustum(BoundingBox boundingBox) {
        return this.cubeInFrustum(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1);
    }

    private int cubeInFrustum(double d, double e, double f, double g, double h, double i) {
        float j = (float)(d - this.camX);
        float k = (float)(e - this.camY);
        float l = (float)(f - this.camZ);
        float m = (float)(g - this.camX);
        float n = (float)(h - this.camY);
        float o = (float)(i - this.camZ);
        return this.intersection.intersectAab(j, k, l, m, n, o);
    }

    public boolean pointInFrustum(double d, double e, double f) {
        return this.intersection.testPoint((float)(d - this.camX), (float)(e - this.camY), (float)(f - this.camZ));
    }

    public Vector4f[] getFrustumPoints() {
        Vector4f[] vector4fs = new Vector4f[]{new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f), new Vector4f(1.0f, -1.0f, -1.0f, 1.0f), new Vector4f(1.0f, 1.0f, -1.0f, 1.0f), new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f), new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f), new Vector4f(1.0f, -1.0f, 1.0f, 1.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f)};
        Matrix4f matrix4f = this.matrix.invert(new Matrix4f());
        for (int i = 0; i < 8; ++i) {
            matrix4f.transform(vector4fs[i]);
            vector4fs[i].div(vector4fs[i].w());
        }
        return vector4fs;
    }

    public double getCamX() {
        return this.camX;
    }

    public double getCamY() {
        return this.camY;
    }

    public double getCamZ() {
        return this.camZ;
    }
}

