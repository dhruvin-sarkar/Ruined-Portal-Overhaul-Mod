/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  java.lang.MatchException
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArrayVoxelShape
extends VoxelShape {
    private final DoubleList xs;
    private final DoubleList ys;
    private final DoubleList zs;

    protected ArrayVoxelShape(DiscreteVoxelShape discreteVoxelShape, double[] ds, double[] es, double[] fs) {
        this(discreteVoxelShape, (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(ds, discreteVoxelShape.getXSize() + 1)), (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(es, discreteVoxelShape.getYSize() + 1)), (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(fs, discreteVoxelShape.getZSize() + 1)));
    }

    ArrayVoxelShape(DiscreteVoxelShape discreteVoxelShape, DoubleList doubleList, DoubleList doubleList2, DoubleList doubleList3) {
        super(discreteVoxelShape);
        int i = discreteVoxelShape.getXSize() + 1;
        int j = discreteVoxelShape.getYSize() + 1;
        int k = discreteVoxelShape.getZSize() + 1;
        if (i != doubleList.size() || j != doubleList2.size() || k != doubleList3.size()) {
            throw Util.pauseInIde(new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."));
        }
        this.xs = doubleList;
        this.ys = doubleList2;
        this.zs = doubleList3;
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> this.xs;
            case Direction.Axis.Y -> this.ys;
            case Direction.Axis.Z -> this.zs;
        };
    }
}

