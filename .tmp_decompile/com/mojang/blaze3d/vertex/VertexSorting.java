/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Floats
 *  it.unimi.dsi.fastutil.ints.IntArrays
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public interface VertexSorting {
    public static final VertexSorting DISTANCE_TO_ORIGIN = VertexSorting.byDistance(0.0f, 0.0f, 0.0f);
    public static final VertexSorting ORTHOGRAPHIC_Z = VertexSorting.byDistance((Vector3f vector3f) -> -vector3f.z());

    public static VertexSorting byDistance(float f, float g, float h) {
        return VertexSorting.byDistance((Vector3fc)new Vector3f(f, g, h));
    }

    public static VertexSorting byDistance(Vector3fc vector3fc) {
        return VertexSorting.byDistance(arg_0 -> ((Vector3fc)vector3fc).distanceSquared(arg_0));
    }

    public static VertexSorting byDistance(DistanceFunction distanceFunction) {
        return compactVectorArray -> {
            Vector3f vector3f = new Vector3f();
            float[] fs = new float[compactVectorArray.size()];
            int[] is = new int[compactVectorArray.size()];
            for (int i2 = 0; i2 < compactVectorArray.size(); ++i2) {
                fs[i2] = distanceFunction.apply(compactVectorArray.get(i2, vector3f));
                is[i2] = i2;
            }
            IntArrays.mergeSort((int[])is, (i, j) -> Floats.compare((float)fs[j], (float)fs[i]));
            return is;
        };
    }

    public int[] sort(CompactVectorArray var1);

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface DistanceFunction {
        public float apply(Vector3f var1);
    }
}

