/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CubePointRange;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.SubShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SliceShape
extends VoxelShape {
    private final VoxelShape delegate;
    private final Direction.Axis axis;
    private static final DoubleList SLICE_COORDS = new CubePointRange(1);

    public SliceShape(VoxelShape voxelShape, Direction.Axis axis, int i) {
        super(SliceShape.makeSlice(voxelShape.shape, axis, i));
        this.delegate = voxelShape;
        this.axis = axis;
    }

    private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape discreteVoxelShape, Direction.Axis axis, int i) {
        return new SubShape(discreteVoxelShape, axis.choose(i, 0, 0), axis.choose(0, i, 0), axis.choose(0, 0, i), axis.choose(i + 1, discreteVoxelShape.xSize, discreteVoxelShape.xSize), axis.choose(discreteVoxelShape.ySize, i + 1, discreteVoxelShape.ySize), axis.choose(discreteVoxelShape.zSize, discreteVoxelShape.zSize, i + 1));
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        if (axis == this.axis) {
            return SLICE_COORDS;
        }
        return this.delegate.getCoords(axis);
    }
}

