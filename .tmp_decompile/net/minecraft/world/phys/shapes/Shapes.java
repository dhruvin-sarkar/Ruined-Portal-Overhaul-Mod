/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.google.common.math.DoubleMath
 *  com.google.common.math.IntMath
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import com.mojang.math.OctahedralGroup;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CubePointRange;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteCubeMerger;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.IdenticalMerger;
import net.minecraft.world.phys.shapes.IndexMerger;
import net.minecraft.world.phys.shapes.IndirectMerger;
import net.minecraft.world.phys.shapes.NonOverlappingMerger;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Shapes {
    public static final double EPSILON = 1.0E-7;
    public static final double BIG_EPSILON = 1.0E-6;
    private static final VoxelShape BLOCK = Util.make(() -> {
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(1, 1, 1);
        ((DiscreteVoxelShape)discreteVoxelShape).fill(0, 0, 0);
        return new CubeVoxelShape(discreteVoxelShape);
    });
    private static final Vec3 BLOCK_CENTER = new Vec3(0.5, 0.5, 0.5);
    public static final VoxelShape INFINITY = Shapes.box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final VoxelShape EMPTY = new ArrayVoxelShape((DiscreteVoxelShape)new BitSetDiscreteVoxelShape(0, 0, 0), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}));

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape block() {
        return BLOCK;
    }

    public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
        if (d > g || e > h || f > i) {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
        return Shapes.create(d, e, f, g, h, i);
    }

    public static VoxelShape create(double d, double e, double f, double g, double h, double i) {
        if (g - d < 1.0E-7 || h - e < 1.0E-7 || i - f < 1.0E-7) {
            return Shapes.empty();
        }
        int j = Shapes.findBits(d, g);
        int k = Shapes.findBits(e, h);
        int l = Shapes.findBits(f, i);
        if (j < 0 || k < 0 || l < 0) {
            return new ArrayVoxelShape(Shapes.BLOCK.shape, (DoubleList)DoubleArrayList.wrap((double[])new double[]{d, g}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{e, h}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{f, i}));
        }
        if (j == 0 && k == 0 && l == 0) {
            return Shapes.block();
        }
        int m = 1 << j;
        int n = 1 << k;
        int o = 1 << l;
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.withFilledBounds(m, n, o, (int)Math.round(d * (double)m), (int)Math.round(e * (double)n), (int)Math.round(f * (double)o), (int)Math.round(g * (double)m), (int)Math.round(h * (double)n), (int)Math.round(i * (double)o));
        return new CubeVoxelShape(bitSetDiscreteVoxelShape);
    }

    public static VoxelShape create(AABB aABB) {
        return Shapes.create(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }

    @VisibleForTesting
    protected static int findBits(double d, double e) {
        if (d < -1.0E-7 || e > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            boolean bl2;
            int j = 1 << i;
            double f = d * (double)j;
            double g = e * (double)j;
            boolean bl = Math.abs(f - (double)Math.round(f)) < 1.0E-7 * (double)j;
            boolean bl3 = bl2 = Math.abs(g - (double)Math.round(g)) < 1.0E-7 * (double)j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    protected static long lcm(int i, int j) {
        return (long)i * (long)(j / IntMath.gcd((int)i, (int)j));
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return Shapes.join(voxelShape, voxelShape2, BooleanOp.OR);
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape ... voxelShapes) {
        return Arrays.stream(voxelShapes).reduce(voxelShape, Shapes::or);
    }

    public static VoxelShape join(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        return Shapes.joinUnoptimized(voxelShape, voxelShape2, booleanOp).optimize();
    }

    public static VoxelShape joinUnoptimized(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true) ? voxelShape : Shapes.empty();
        }
        boolean bl = booleanOp.apply(true, false);
        boolean bl2 = booleanOp.apply(false, true);
        if (voxelShape.isEmpty()) {
            return bl2 ? voxelShape2 : Shapes.empty();
        }
        if (voxelShape2.isEmpty()) {
            return bl ? voxelShape : Shapes.empty();
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl, bl2);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl, bl2);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl, bl2);
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.join(voxelShape.shape, voxelShape2.shape, indexMerger, indexMerger2, indexMerger3, booleanOp);
        if (indexMerger instanceof DiscreteCubeMerger && indexMerger2 instanceof DiscreteCubeMerger && indexMerger3 instanceof DiscreteCubeMerger) {
            return new CubeVoxelShape(bitSetDiscreteVoxelShape);
        }
        return new ArrayVoxelShape((DiscreteVoxelShape)bitSetDiscreteVoxelShape, indexMerger.getList(), indexMerger2.getList(), indexMerger3.getList());
    }

    public static boolean joinIsNotEmpty(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        boolean bl = voxelShape.isEmpty();
        boolean bl2 = voxelShape2.isEmpty();
        if (bl || bl2) {
            return booleanOp.apply(!bl, !bl2);
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true);
        }
        boolean bl3 = booleanOp.apply(true, false);
        boolean bl4 = booleanOp.apply(false, true);
        for (Direction.Axis axis : AxisCycle.AXIS_VALUES) {
            if (voxelShape.max(axis) < voxelShape2.min(axis) - 1.0E-7) {
                return bl3 || bl4;
            }
            if (!(voxelShape2.max(axis) < voxelShape.min(axis) - 1.0E-7)) continue;
            return bl3 || bl4;
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl3, bl4);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl3, bl4);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl3, bl4);
        return Shapes.joinIsNotEmpty(indexMerger, indexMerger2, indexMerger3, voxelShape.shape, voxelShape2.shape, booleanOp);
    }

    private static boolean joinIsNotEmpty(IndexMerger indexMerger, IndexMerger indexMerger2, IndexMerger indexMerger3, DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape discreteVoxelShape2, BooleanOp booleanOp) {
        return !indexMerger.forMergedIndexes((i, j, k2) -> indexMerger2.forMergedIndexes((k, l, m2) -> indexMerger3.forMergedIndexes((m, n, o) -> !booleanOp.apply(discreteVoxelShape.isFullWide(i, k, m), discreteVoxelShape2.isFullWide(j, l, n)))));
    }

    public static double collide(Direction.Axis axis, AABB aABB, Iterable<VoxelShape> iterable, double d) {
        for (VoxelShape voxelShape : iterable) {
            if (Math.abs(d) < 1.0E-7) {
                return 0.0;
            }
            d = voxelShape.collide(axis, aABB, d);
        }
        return d;
    }

    public static boolean blockOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        if (voxelShape == Shapes.block() && voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape2.isEmpty()) {
            return false;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        BooleanOp booleanOp = axisDirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
        return DoubleMath.fuzzyEquals((double)voxelShape3.max(axis), (double)1.0, (double)1.0E-7) && DoubleMath.fuzzyEquals((double)voxelShape4.min(axis), (double)0.0, (double)1.0E-7) && !Shapes.joinIsNotEmpty(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), booleanOp);
    }

    public static boolean mergedFaceOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        VoxelShape voxelShape4;
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape5 = voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        if (!DoubleMath.fuzzyEquals((double)voxelShape3.max(axis), (double)1.0, (double)1.0E-7)) {
            voxelShape3 = Shapes.empty();
        }
        if (!DoubleMath.fuzzyEquals((double)voxelShape4.min(axis), (double)0.0, (double)1.0E-7)) {
            voxelShape4 = Shapes.empty();
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    public static boolean faceShapeOccludes(VoxelShape voxelShape, VoxelShape voxelShape2) {
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape.isEmpty() && voxelShape2.isEmpty()) {
            return false;
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(voxelShape, voxelShape2, BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    @VisibleForTesting
    protected static IndexMerger createIndexMerger(int i, DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
        long l;
        int j = doubleList.size() - 1;
        int k = doubleList2.size() - 1;
        if (doubleList instanceof CubePointRange && doubleList2 instanceof CubePointRange && (long)i * (l = Shapes.lcm(j, k)) <= 256L) {
            return new DiscreteCubeMerger(j, k);
        }
        if (doubleList.getDouble(j) < doubleList2.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList, doubleList2, false);
        }
        if (doubleList2.getDouble(k) < doubleList.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList2, doubleList, true);
        }
        if (j == k && Objects.equals(doubleList, doubleList2)) {
            return new IdenticalMerger(doubleList);
        }
        return new IndirectMerger(doubleList, doubleList2, bl, bl2);
    }

    public static VoxelShape rotate(VoxelShape voxelShape, OctahedralGroup octahedralGroup) {
        return Shapes.rotate(voxelShape, octahedralGroup, BLOCK_CENTER);
    }

    public static VoxelShape rotate(VoxelShape voxelShape, OctahedralGroup octahedralGroup, Vec3 vec3) {
        if (octahedralGroup == OctahedralGroup.IDENTITY) {
            return voxelShape;
        }
        DiscreteVoxelShape discreteVoxelShape = voxelShape.shape.rotate(octahedralGroup);
        if (voxelShape instanceof CubeVoxelShape && BLOCK_CENTER.equals(vec3)) {
            return new CubeVoxelShape(discreteVoxelShape);
        }
        Direction.Axis axis = octahedralGroup.permutation().permuteAxis(Direction.Axis.X);
        Direction.Axis axis2 = octahedralGroup.permutation().permuteAxis(Direction.Axis.Y);
        Direction.Axis axis3 = octahedralGroup.permutation().permuteAxis(Direction.Axis.Z);
        DoubleList doubleList = voxelShape.getCoords(axis);
        DoubleList doubleList2 = voxelShape.getCoords(axis2);
        DoubleList doubleList3 = voxelShape.getCoords(axis3);
        boolean bl = octahedralGroup.inverts(Direction.Axis.X);
        boolean bl2 = octahedralGroup.inverts(Direction.Axis.Y);
        boolean bl3 = octahedralGroup.inverts(Direction.Axis.Z);
        return new ArrayVoxelShape(discreteVoxelShape, Shapes.flipAxisIfNeeded(doubleList, bl, vec3.get(axis), vec3.x), Shapes.flipAxisIfNeeded(doubleList2, bl2, vec3.get(axis2), vec3.y), Shapes.flipAxisIfNeeded(doubleList3, bl3, vec3.get(axis3), vec3.z));
    }

    @VisibleForTesting
    static DoubleList flipAxisIfNeeded(DoubleList doubleList, boolean bl, double d, double e) {
        if (!bl && d == e) {
            return doubleList;
        }
        int i = doubleList.size();
        DoubleArrayList doubleList2 = new DoubleArrayList(i);
        if (bl) {
            for (int j = i - 1; j >= 0; --j) {
                doubleList2.add(-(doubleList.getDouble(j) - d) + e);
            }
        } else {
            for (int j = 0; j >= 0 && j < i; ++j) {
                doubleList2.add(doubleList.getDouble(j) - d + e);
            }
        }
        return doubleList2;
    }

    public static boolean equal(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return !Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME);
    }

    public static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape voxelShape) {
        return Shapes.rotateHorizontalAxis(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap((Map)Map.of((Object)Direction.Axis.Z, (Object)voxelShape, (Object)Direction.Axis.X, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_90, vec3)));
    }

    public static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape voxelShape) {
        return Shapes.rotateAllAxis(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap((Map)Map.of((Object)Direction.Axis.Z, (Object)voxelShape, (Object)Direction.Axis.X, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_90, vec3), (Object)Direction.Axis.Y, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_X_90, vec3)));
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape voxelShape) {
        return Shapes.rotateHorizontal(voxelShape, OctahedralGroup.IDENTITY, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape voxelShape, OctahedralGroup octahedralGroup) {
        return Shapes.rotateHorizontal(voxelShape, octahedralGroup, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape voxelShape, OctahedralGroup octahedralGroup, Vec3 vec3) {
        return Maps.newEnumMap((Map)Map.of((Object)Direction.NORTH, (Object)Shapes.rotate(voxelShape, octahedralGroup), (Object)Direction.EAST, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_90.compose(octahedralGroup), vec3), (Object)Direction.SOUTH, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_180.compose(octahedralGroup), vec3), (Object)Direction.WEST, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_270.compose(octahedralGroup), vec3)));
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape voxelShape) {
        return Shapes.rotateAll(voxelShape, OctahedralGroup.IDENTITY, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape voxelShape, Vec3 vec3) {
        return Shapes.rotateAll(voxelShape, OctahedralGroup.IDENTITY, vec3);
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape voxelShape, OctahedralGroup octahedralGroup, Vec3 vec3) {
        return Maps.newEnumMap((Map)Map.of((Object)Direction.NORTH, (Object)Shapes.rotate(voxelShape, octahedralGroup), (Object)Direction.EAST, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_90.compose(octahedralGroup), vec3), (Object)Direction.SOUTH, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_180.compose(octahedralGroup), vec3), (Object)Direction.WEST, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_Y_270.compose(octahedralGroup), vec3), (Object)Direction.UP, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_X_270.compose(octahedralGroup), vec3), (Object)Direction.DOWN, (Object)Shapes.rotate(voxelShape, OctahedralGroup.BLOCK_ROT_X_90.compose(octahedralGroup), vec3)));
    }

    public static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape voxelShape) {
        return Shapes.rotateAttachFace(voxelShape, OctahedralGroup.IDENTITY);
    }

    public static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape voxelShape, OctahedralGroup octahedralGroup) {
        return Map.of((Object)AttachFace.WALL, Shapes.rotateHorizontal(voxelShape, octahedralGroup), (Object)AttachFace.FLOOR, Shapes.rotateHorizontal(voxelShape, OctahedralGroup.BLOCK_ROT_X_270.compose(octahedralGroup)), (Object)AttachFace.CEILING, Shapes.rotateHorizontal(voxelShape, OctahedralGroup.BLOCK_ROT_Y_180.compose(OctahedralGroup.BLOCK_ROT_X_90).compose(octahedralGroup)));
    }

    public static interface DoubleLineConsumer {
        public void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}

