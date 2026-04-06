/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.GeometryUtils
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.math.MatrixUtil;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import org.joml.GeometryUtils;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FaceBakery {
    private static final Vector3fc BLOCK_MIDDLE = new Vector3f(0.5f, 0.5f, 0.5f);

    @VisibleForTesting
    static BlockElementFace.UVs defaultFaceUV(Vector3fc vector3fc, Vector3fc vector3fc2, Direction direction) {
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> new BlockElementFace.UVs(vector3fc.x(), 16.0f - vector3fc2.z(), vector3fc2.x(), 16.0f - vector3fc.z());
            case Direction.UP -> new BlockElementFace.UVs(vector3fc.x(), vector3fc.z(), vector3fc2.x(), vector3fc2.z());
            case Direction.NORTH -> new BlockElementFace.UVs(16.0f - vector3fc2.x(), 16.0f - vector3fc2.y(), 16.0f - vector3fc.x(), 16.0f - vector3fc.y());
            case Direction.SOUTH -> new BlockElementFace.UVs(vector3fc.x(), 16.0f - vector3fc2.y(), vector3fc2.x(), 16.0f - vector3fc.y());
            case Direction.WEST -> new BlockElementFace.UVs(vector3fc.z(), 16.0f - vector3fc2.y(), vector3fc2.z(), 16.0f - vector3fc.y());
            case Direction.EAST -> new BlockElementFace.UVs(16.0f - vector3fc2.z(), 16.0f - vector3fc2.y(), 16.0f - vector3fc.z(), 16.0f - vector3fc.y());
        };
    }

    public static BakedQuad bakeQuad(ModelBaker.PartCache partCache, Vector3fc vector3fc, Vector3fc vector3fc2, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, @Nullable BlockElementRotation blockElementRotation, boolean bl, int i) {
        BlockElementFace.UVs uVs = blockElementFace.uvs();
        if (uVs == null) {
            uVs = FaceBakery.defaultFaceUV(vector3fc, vector3fc2, direction);
        }
        Matrix4fc matrix4fc = modelState.inverseFaceTransformation(direction);
        Vector3fc[] vector3fcs = new Vector3fc[4];
        long[] ls = new long[4];
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        for (int j = 0; j < 4; ++j) {
            FaceBakery.bakeVertex(j, faceInfo, uVs, blockElementFace.rotation(), matrix4fc, vector3fc, vector3fc2, textureAtlasSprite, modelState.transformation(), blockElementRotation, vector3fcs, ls, partCache);
        }
        Direction direction2 = FaceBakery.calculateFacing(vector3fcs);
        if (blockElementRotation == null && direction2 != null) {
            FaceBakery.recalculateWinding(vector3fcs, ls, direction2);
        }
        return new BakedQuad(vector3fcs[0], vector3fcs[1], vector3fcs[2], vector3fcs[3], ls[0], ls[1], ls[2], ls[3], blockElementFace.tintIndex(), (Direction)Objects.requireNonNullElse((Object)direction2, (Object)Direction.UP), textureAtlasSprite, bl, i);
    }

    private static void bakeVertex(int i, FaceInfo faceInfo, BlockElementFace.UVs uVs, Quadrant quadrant, Matrix4fc matrix4fc, Vector3fc vector3fc, Vector3fc vector3fc2, TextureAtlasSprite textureAtlasSprite, Transformation transformation, @Nullable BlockElementRotation blockElementRotation, Vector3fc[] vector3fcs, long[] ls, ModelBaker.PartCache partCache) {
        float j;
        float h;
        FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(i);
        Vector3f vector3f = vertexInfo.select(vector3fc, vector3fc2).div(16.0f);
        if (blockElementRotation != null) {
            FaceBakery.rotateVertexBy(vector3f, blockElementRotation.origin(), blockElementRotation.transform());
        }
        if (transformation != Transformation.identity()) {
            FaceBakery.rotateVertexBy(vector3f, BLOCK_MIDDLE, transformation.getMatrix());
        }
        float f = BlockElementFace.getU(uVs, quadrant, i);
        float g = BlockElementFace.getV(uVs, quadrant, i);
        if (MatrixUtil.isIdentity(matrix4fc)) {
            h = f;
            j = g;
        } else {
            Vector3f vector3f2 = matrix4fc.transformPosition(new Vector3f(FaceBakery.cornerToCenter(f), FaceBakery.cornerToCenter(g), 0.0f));
            h = FaceBakery.centerToCorner(vector3f2.x);
            j = FaceBakery.centerToCorner(vector3f2.y);
        }
        vector3fcs[i] = partCache.vector((Vector3fc)vector3f);
        ls[i] = UVPair.pack(textureAtlasSprite.getU(h), textureAtlasSprite.getV(j));
    }

    private static float cornerToCenter(float f) {
        return f - 0.5f;
    }

    private static float centerToCorner(float f) {
        return f + 0.5f;
    }

    private static void rotateVertexBy(Vector3f vector3f, Vector3fc vector3fc, Matrix4fc matrix4fc) {
        vector3f.sub(vector3fc);
        matrix4fc.transformPosition(vector3f);
        vector3f.add(vector3fc);
    }

    private static @Nullable Direction calculateFacing(Vector3fc[] vector3fcs) {
        Vector3f vector3f = new Vector3f();
        GeometryUtils.normal((Vector3fc)vector3fcs[0], (Vector3fc)vector3fcs[1], (Vector3fc)vector3fcs[2], (Vector3f)vector3f);
        return FaceBakery.findClosestDirection(vector3f);
    }

    private static @Nullable Direction findClosestDirection(Vector3f vector3f) {
        if (!vector3f.isFinite()) {
            return null;
        }
        Direction direction = null;
        float f = 0.0f;
        for (Direction direction2 : Direction.values()) {
            float g = vector3f.dot(direction2.getUnitVec3f());
            if (!(g >= 0.0f) || !(g > f)) continue;
            f = g;
            direction = direction2;
        }
        return direction;
    }

    private static void recalculateWinding(Vector3fc[] vector3fcs, long[] ls, Direction direction) {
        float o;
        float n;
        float f = 999.0f;
        float g = 999.0f;
        float h = 999.0f;
        float i = -999.0f;
        float j = -999.0f;
        float k = -999.0f;
        for (int l = 0; l < 4; ++l) {
            Vector3fc vector3fc = vector3fcs[l];
            float m = vector3fc.x();
            n = vector3fc.y();
            o = vector3fc.z();
            if (m < f) {
                f = m;
            }
            if (n < g) {
                g = n;
            }
            if (o < h) {
                h = o;
            }
            if (m > i) {
                i = m;
            }
            if (n > j) {
                j = n;
            }
            if (!(o > k)) continue;
            k = o;
        }
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        for (int p = 0; p < 4; ++p) {
            float q;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(p);
            n = vertexInfo.xFace().select(f, g, h, i, j, k);
            int r = FaceBakery.findVertex(vector3fcs, p, n, o = vertexInfo.yFace().select(f, g, h, i, j, k), q = vertexInfo.zFace().select(f, g, h, i, j, k));
            if (r == -1) {
                throw new IllegalStateException("Can't find vertex to swap");
            }
            if (r == p) continue;
            FaceBakery.swap(vector3fcs, r, p);
            FaceBakery.swap(ls, r, p);
        }
    }

    private static int findVertex(Vector3fc[] vector3fcs, int i, float f, float g, float h) {
        for (int j = i; j < 4; ++j) {
            Vector3fc vector3fc = vector3fcs[j];
            if (f != vector3fc.x() || g != vector3fc.y() || h != vector3fc.z()) continue;
            return j;
        }
        return -1;
    }

    private static void swap(Vector3fc[] vector3fcs, int i, int j) {
        Vector3fc vector3fc = vector3fcs[i];
        vector3fcs[i] = vector3fcs[j];
        vector3fcs[j] = vector3fc;
    }

    private static void swap(long[] ls, int i, int j) {
        long l = ls[i];
        ls[i] = ls[j];
        ls[j] = l;
    }
}

