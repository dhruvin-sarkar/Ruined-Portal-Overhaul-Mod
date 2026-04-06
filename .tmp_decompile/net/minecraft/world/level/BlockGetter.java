/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public interface BlockGetter
extends LevelHeightAccessor {
    public @Nullable BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity == null || blockEntity.getType() != blockEntityType) {
            return Optional.empty();
        }
        return Optional.of(blockEntity);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLightEmission(BlockPos blockPos) {
        return this.getBlockState(blockPos).getLightEmission();
    }

    default public Stream<BlockState> getBlockStates(AABB aABB) {
        return BlockPos.betweenClosedStream(aABB).map(this::getBlockState);
    }

    default public BlockHitResult isBlockInLine(ClipBlockStateContext clipBlockStateContext2) {
        return BlockGetter.traverseBlocks(clipBlockStateContext2.getFrom(), clipBlockStateContext2.getTo(), clipBlockStateContext2, (clipBlockStateContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return clipBlockStateContext.isTargetBlock().test(blockState) ? new BlockHitResult(clipBlockStateContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContext.getTo()), false) : null;
        }, clipBlockStateContext -> {
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return BlockHitResult.miss(clipBlockStateContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContext.getTo()));
        });
    }

    default public BlockHitResult clip(ClipContext clipContext2) {
        return BlockGetter.traverseBlocks(clipContext2.getFrom(), clipContext2.getTo(), clipContext2, (clipContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            FluidState fluidState = this.getFluidState((BlockPos)blockPos);
            Vec3 vec3 = clipContext.getFrom();
            Vec3 vec32 = clipContext.getTo();
            VoxelShape voxelShape = clipContext.getBlockShape(blockState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult = this.clipWithInteractionOverride(vec3, vec32, (BlockPos)blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = clipContext.getFluidShape(fluidState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, (BlockPos)blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult.getLocation());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult2.getLocation());
            return d <= e ? blockHitResult : blockHitResult2;
        }, clipContext -> {
            Vec3 vec3 = clipContext.getFrom().subtract(clipContext.getTo());
            return BlockHitResult.miss(clipContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipContext.getTo()));
        });
    }

    default public @Nullable BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState) {
        BlockHitResult blockHitResult2;
        BlockHitResult blockHitResult = voxelShape.clip(vec3, vec32, blockPos);
        if (blockHitResult != null && (blockHitResult2 = blockState.getInteractionShape(this, blockPos).clip(vec3, vec32, blockPos)) != null && blockHitResult2.getLocation().subtract(vec3).lengthSqr() < blockHitResult.getLocation().subtract(vec3).lengthSqr()) {
            return blockHitResult.withDirection(blockHitResult2.getDirection());
        }
        return blockHitResult;
    }

    default public double getBlockFloorHeight(VoxelShape voxelShape, Supplier<VoxelShape> supplier) {
        if (!voxelShape.isEmpty()) {
            return voxelShape.max(Direction.Axis.Y);
        }
        double d = supplier.get().max(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getBlockFloorHeight(BlockPos blockPos) {
        return this.getBlockFloorHeight(this.getBlockState(blockPos).getCollisionShape(this, blockPos), () -> {
            BlockPos blockPos2 = blockPos.below();
            return this.getBlockState(blockPos2).getCollisionShape(this, blockPos2);
        });
    }

    public static <T, C> T traverseBlocks(Vec3 vec3, Vec3 vec32, C object, BiFunction<C, BlockPos, @Nullable T> biFunction, Function<C, T> function) {
        int l;
        int k;
        if (vec3.equals(vec32)) {
            return function.apply(object);
        }
        double d = Mth.lerp(-1.0E-7, vec32.x, vec3.x);
        double e = Mth.lerp(-1.0E-7, vec32.y, vec3.y);
        double f = Mth.lerp(-1.0E-7, vec32.z, vec3.z);
        double g = Mth.lerp(-1.0E-7, vec3.x, vec32.x);
        double h = Mth.lerp(-1.0E-7, vec3.y, vec32.y);
        double i = Mth.lerp(-1.0E-7, vec3.z, vec32.z);
        int j = Mth.floor(g);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(j, k = Mth.floor(h), l = Mth.floor(i));
        T object2 = biFunction.apply(object, mutableBlockPos);
        if (object2 != null) {
            return object2;
        }
        double m = d - g;
        double n = e - h;
        double o = f - i;
        int p = Mth.sign(m);
        int q = Mth.sign(n);
        int r = Mth.sign(o);
        double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
        double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
        double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
        double v = s * (p > 0 ? 1.0 - Mth.frac(g) : Mth.frac(g));
        double w = t * (q > 0 ? 1.0 - Mth.frac(h) : Mth.frac(h));
        double x = u * (r > 0 ? 1.0 - Mth.frac(i) : Mth.frac(i));
        while (v <= 1.0 || w <= 1.0 || x <= 1.0) {
            T object3;
            if (v < w) {
                if (v < x) {
                    j += p;
                    v += s;
                } else {
                    l += r;
                    x += u;
                }
            } else if (w < x) {
                k += q;
                w += t;
            } else {
                l += r;
                x += u;
            }
            if ((object3 = biFunction.apply(object, mutableBlockPos.set(j, k, l))) == null) continue;
            return object3;
        }
        return function.apply(object);
    }

    public static boolean forEachBlockIntersectedBetween(Vec3 vec3, Vec3 vec32, AABB aABB, BlockStepVisitor blockStepVisitor) {
        Vec3 vec33 = vec32.subtract(vec3);
        if (vec33.lengthSqr() < (double)Mth.square(1.0E-5f)) {
            for (BlockPos blockPos : BlockPos.betweenClosed(aABB)) {
                if (blockStepVisitor.visit(blockPos, 0)) continue;
                return false;
            }
            return true;
        }
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.betweenCornersInDirection(aABB.move(vec33.scale(-1.0)), vec33)) {
            if (!blockStepVisitor.visit(blockPos2, 0)) {
                return false;
            }
            longSet.add(blockPos2.asLong());
        }
        int i = BlockGetter.addCollisionsAlongTravel((LongSet)longSet, vec33, aABB, blockStepVisitor);
        if (i < 0) {
            return false;
        }
        for (BlockPos blockPos3 : BlockPos.betweenCornersInDirection(aABB, vec33)) {
            if (!longSet.add(blockPos3.asLong()) || blockStepVisitor.visit(blockPos3, i + 1)) continue;
            return false;
        }
        return true;
    }

    private static int addCollisionsAlongTravel(LongSet longSet, Vec3 vec3, AABB aABB, BlockStepVisitor blockStepVisitor) {
        double d = aABB.getXsize();
        double e = aABB.getYsize();
        double f = aABB.getZsize();
        Vec3i vec3i = BlockGetter.getFurthestCorner(vec3);
        Vec3 vec32 = aABB.getCenter();
        Vec3 vec33 = new Vec3(vec32.x() + d * 0.5 * (double)vec3i.getX(), vec32.y() + e * 0.5 * (double)vec3i.getY(), vec32.z() + f * 0.5 * (double)vec3i.getZ());
        Vec3 vec34 = vec33.subtract(vec3);
        int i = Mth.floor(vec34.x);
        int j = Mth.floor(vec34.y);
        int k = Mth.floor(vec34.z);
        int l = Mth.sign(vec3.x);
        int m = Mth.sign(vec3.y);
        int n = Mth.sign(vec3.z);
        double g = l == 0 ? Double.MAX_VALUE : (double)l / vec3.x;
        double h = m == 0 ? Double.MAX_VALUE : (double)m / vec3.y;
        double o = n == 0 ? Double.MAX_VALUE : (double)n / vec3.z;
        double p = g * (l > 0 ? 1.0 - Mth.frac(vec34.x) : Mth.frac(vec34.x));
        double q = h * (m > 0 ? 1.0 - Mth.frac(vec34.y) : Mth.frac(vec34.y));
        double r = o * (n > 0 ? 1.0 - Mth.frac(vec34.z) : Mth.frac(vec34.z));
        int s = 0;
        while (p <= 1.0 || q <= 1.0 || r <= 1.0) {
            if (p < q) {
                if (p < r) {
                    i += l;
                    p += g;
                } else {
                    k += n;
                    r += o;
                }
            } else if (q < r) {
                j += m;
                q += h;
            } else {
                k += n;
                r += o;
            }
            Optional<Vec3> optional = AABB.clip(i, j, k, i + 1, j + 1, k + 1, vec34, vec33);
            if (optional.isEmpty()) continue;
            Vec3 vec35 = optional.get();
            double t = Mth.clamp(vec35.x, (double)i + (double)1.0E-5f, (double)i + 1.0 - (double)1.0E-5f);
            double u = Mth.clamp(vec35.y, (double)j + (double)1.0E-5f, (double)j + 1.0 - (double)1.0E-5f);
            double v = Mth.clamp(vec35.z, (double)k + (double)1.0E-5f, (double)k + 1.0 - (double)1.0E-5f);
            int w = Mth.floor(t - d * (double)vec3i.getX());
            int x = Mth.floor(u - e * (double)vec3i.getY());
            int y = Mth.floor(v - f * (double)vec3i.getZ());
            int z = ++s;
            for (BlockPos blockPos : BlockPos.betweenCornersInDirection(i, j, k, w, x, y, vec3)) {
                if (!longSet.add(blockPos.asLong()) || blockStepVisitor.visit(blockPos, z)) continue;
                return -1;
            }
        }
        return s;
    }

    private static Vec3i getFurthestCorner(Vec3 vec3) {
        int k;
        double d = Math.abs(Vec3.X_AXIS.dot(vec3));
        double e = Math.abs(Vec3.Y_AXIS.dot(vec3));
        double f = Math.abs(Vec3.Z_AXIS.dot(vec3));
        int i = vec3.x >= 0.0 ? 1 : -1;
        int j = vec3.y >= 0.0 ? 1 : -1;
        int n = k = vec3.z >= 0.0 ? 1 : -1;
        if (d <= e && d <= f) {
            return new Vec3i(-i, -k, j);
        }
        if (e <= f) {
            return new Vec3i(k, -j, -i);
        }
        return new Vec3i(-j, i, -k);
    }

    @FunctionalInterface
    public static interface BlockStepVisitor {
        public boolean visit(BlockPos var1, int var2);
    }
}

