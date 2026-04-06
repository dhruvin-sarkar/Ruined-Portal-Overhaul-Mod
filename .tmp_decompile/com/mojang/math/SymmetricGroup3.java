/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Vector3f
 *  org.joml.Vector3i
 */
package com.mojang.math;

import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3i;

public enum SymmetricGroup3 {
    P123(0, 1, 2),
    P213(1, 0, 2),
    P132(0, 2, 1),
    P312(2, 0, 1),
    P231(1, 2, 0),
    P321(2, 1, 0);

    private final int p0;
    private final int p1;
    private final int p2;
    private final Matrix3fc transformation;
    private static final SymmetricGroup3[][] CAYLEY_TABLE;
    private static final SymmetricGroup3[] INVERSE_TABLE;

    private SymmetricGroup3(int j, int k, int l) {
        this.p0 = j;
        this.p1 = k;
        this.p2 = l;
        this.transformation = new Matrix3f().zero().set(this.permute(0), 0, 1.0f).set(this.permute(1), 1, 1.0f).set(this.permute(2), 2, 1.0f);
    }

    public SymmetricGroup3 compose(SymmetricGroup3 symmetricGroup3) {
        return CAYLEY_TABLE[this.ordinal()][symmetricGroup3.ordinal()];
    }

    public SymmetricGroup3 inverse() {
        return INVERSE_TABLE[this.ordinal()];
    }

    public int permute(int i) {
        return switch (i) {
            case 0 -> this.p0;
            case 1 -> this.p1;
            case 2 -> this.p2;
            default -> throw new IllegalArgumentException("Must be 0, 1 or 2, but got " + i);
        };
    }

    public Direction.Axis permuteAxis(Direction.Axis axis) {
        return Direction.Axis.VALUES[this.permute(axis.ordinal())];
    }

    public Vector3f permuteVector(Vector3f vector3f) {
        float f = vector3f.get(this.p0);
        float g = vector3f.get(this.p1);
        float h = vector3f.get(this.p2);
        return vector3f.set(f, g, h);
    }

    public Vector3i permuteVector(Vector3i vector3i) {
        int i = vector3i.get(this.p0);
        int j = vector3i.get(this.p1);
        int k = vector3i.get(this.p2);
        return vector3i.set(i, j, k);
    }

    public Matrix3fc transformation() {
        return this.transformation;
    }

    static {
        CAYLEY_TABLE = Util.make(() -> {
            SymmetricGroup3[] symmetricGroup3s = SymmetricGroup3.values();
            SymmetricGroup3[][] symmetricGroup3s2 = new SymmetricGroup3[symmetricGroup3s.length][symmetricGroup3s.length];
            for (SymmetricGroup3 symmetricGroup32 : symmetricGroup3s) {
                for (SymmetricGroup3 symmetricGroup322 : symmetricGroup3s) {
                    SymmetricGroup3 symmetricGroup33;
                    int i = symmetricGroup32.permute(symmetricGroup322.p0);
                    int j = symmetricGroup32.permute(symmetricGroup322.p1);
                    int k = symmetricGroup32.permute(symmetricGroup322.p2);
                    symmetricGroup3s2[symmetricGroup32.ordinal()][symmetricGroup322.ordinal()] = symmetricGroup33 = Arrays.stream(symmetricGroup3s).filter(symmetricGroup3 -> symmetricGroup3.p0 == i && symmetricGroup3.p1 == j && symmetricGroup3.p2 == k).findFirst().get();
                }
            }
            return symmetricGroup3s2;
        });
        INVERSE_TABLE = Util.make(() -> {
            SymmetricGroup3[] symmetricGroup3s = SymmetricGroup3.values();
            return (SymmetricGroup3[])Arrays.stream(symmetricGroup3s).map(symmetricGroup3 -> Arrays.stream(SymmetricGroup3.values()).filter(symmetricGroup32 -> symmetricGroup3.compose((SymmetricGroup3)((Object)((Object)((Object)symmetricGroup32)))) == P123).findAny().get()).toArray(SymmetricGroup3[]::new);
        });
    }
}

