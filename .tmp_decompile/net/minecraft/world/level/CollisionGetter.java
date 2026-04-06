/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public interface CollisionGetter
extends BlockGetter {
    public WorldBorder getWorldBorder();

    public @Nullable BlockGetter getChunkForCollisions(int var1, int var2);

    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return true;
    }

    default public boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
        return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move(blockPos));
    }

    default public boolean isUnobstructed(Entity entity) {
        return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
    }

    default public boolean noCollision(AABB aABB) {
        return this.noCollision(null, aABB);
    }

    default public boolean noCollision(Entity entity) {
        return this.noCollision(entity, entity.getBoundingBox());
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB) {
        return this.noCollision(entity, aABB, false);
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB, boolean bl) {
        return this.noBlockCollision(entity, aABB, bl) && this.noEntityCollision(entity, aABB) && this.noBorderCollision(entity, aABB);
    }

    default public boolean noBlockCollision(@Nullable Entity entity, AABB aABB) {
        return this.noBlockCollision(entity, aABB, false);
    }

    default public boolean noBlockCollision(@Nullable Entity entity, AABB aABB, boolean bl) {
        Iterable<VoxelShape> iterable = bl ? this.getBlockAndLiquidCollisions(entity, aABB) : this.getBlockCollisions(entity, aABB);
        for (VoxelShape voxelShape : iterable) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public boolean noEntityCollision(@Nullable Entity entity, AABB aABB) {
        return this.getEntityCollisions(entity, aABB).isEmpty();
    }

    default public boolean noBorderCollision(@Nullable Entity entity, AABB aABB) {
        if (entity != null) {
            VoxelShape voxelShape = this.borderCollision(entity, aABB);
            return voxelShape == null || !Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aABB), BooleanOp.AND);
        }
        return true;
    }

    public List<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2);

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB) {
        List<VoxelShape> list = this.getEntityCollisions(entity, aABB);
        Iterable iterable = this.getBlockCollisions(entity, aABB);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getPreMoveCollisions(@Nullable Entity entity, AABB aABB, Vec3 vec3) {
        List<VoxelShape> list = this.getEntityCollisions(entity, aABB);
        Iterable iterable = this.getBlockCollisionsFromContext(CollisionContext.withPosition(entity, vec3.y), aABB);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
        return this.getBlockCollisionsFromContext(entity == null ? CollisionContext.empty() : CollisionContext.of(entity), aABB);
    }

    default public Iterable<VoxelShape> getBlockAndLiquidCollisions(@Nullable Entity entity, AABB aABB) {
        return this.getBlockCollisionsFromContext(entity == null ? CollisionContext.emptyWithFluidCollisions() : CollisionContext.of(entity, true), aABB);
    }

    private Iterable<VoxelShape> getBlockCollisionsFromContext(CollisionContext collisionContext, AABB aABB) {
        return () -> new BlockCollisions<VoxelShape>(this, collisionContext, aABB, false, (mutableBlockPos, voxelShape) -> voxelShape);
    }

    private @Nullable VoxelShape borderCollision(Entity entity, AABB aABB) {
        WorldBorder worldBorder = this.getWorldBorder();
        return worldBorder.isInsideCloseToBorder(entity, aABB) ? worldBorder.getCollisionShape() : null;
    }

    default public BlockHitResult clipIncludingBorder(ClipContext clipContext) {
        BlockHitResult blockHitResult = this.clip(clipContext);
        WorldBorder worldBorder = this.getWorldBorder();
        if (worldBorder.isWithinBounds(clipContext.getFrom()) && !worldBorder.isWithinBounds(blockHitResult.getLocation())) {
            Vec3 vec3 = blockHitResult.getLocation().subtract(clipContext.getFrom());
            Direction direction = Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = worldBorder.clampVec3ToBound(blockHitResult.getLocation());
            return new BlockHitResult(vec32, direction, BlockPos.containing(vec32), false, true);
        }
        return blockHitResult;
    }

    default public boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB aABB) {
        BlockCollisions<VoxelShape> blockCollisions = new BlockCollisions<VoxelShape>(this, entity, aABB, true, (mutableBlockPos, voxelShape) -> voxelShape);
        while (blockCollisions.hasNext()) {
            if (((VoxelShape)blockCollisions.next()).isEmpty()) continue;
            return true;
        }
        return false;
    }

    default public Optional<BlockPos> findSupportingBlock(Entity entity, AABB aABB) {
        BlockPos blockPos = null;
        double d = Double.MAX_VALUE;
        BlockCollisions<BlockPos> blockCollisions = new BlockCollisions<BlockPos>(this, entity, aABB, false, (mutableBlockPos, voxelShape) -> mutableBlockPos);
        while (blockCollisions.hasNext()) {
            BlockPos blockPos2 = (BlockPos)blockCollisions.next();
            double e = blockPos2.distToCenterSqr(entity.position());
            if (!(e < d) && (e != d || blockPos != null && blockPos.compareTo(blockPos2) >= 0)) continue;
            blockPos = blockPos2.immutable();
            d = e;
        }
        return Optional.ofNullable(blockPos);
    }

    default public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelShape2, Vec3 vec3, double d, double e, double f) {
        if (voxelShape2.isEmpty()) {
            return Optional.empty();
        }
        AABB aABB2 = voxelShape2.bounds().inflate(d, e, f);
        VoxelShape voxelShape22 = StreamSupport.stream(this.getBlockCollisions(entity, aABB2).spliterator(), false).filter(voxelShape -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(voxelShape.bounds())).flatMap(voxelShape -> voxelShape.toAabbs().stream()).map(aABB -> aABB.inflate(d / 2.0, e / 2.0, f / 2.0)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
        VoxelShape voxelShape3 = Shapes.join(voxelShape2, voxelShape22, BooleanOp.ONLY_FIRST);
        return voxelShape3.closestPointTo(vec3);
    }
}

