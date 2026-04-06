/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import org.jspecify.annotations.Nullable;

public class SwimNodeEvaluator
extends NodeEvaluator {
    private final boolean allowBreaching;
    private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap();

    public SwimNodeEvaluator(boolean bl) {
        this.allowBreaching = bl;
    }

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Override
    public Node getStart() {
        return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public Target getTarget(double d, double e, double f) {
        return this.getTargetNodeAt(d, e, f);
    }

    @Override
    public int getNeighbors(Node[] nodes, Node node) {
        int i = 0;
        EnumMap map = Maps.newEnumMap(Direction.class);
        for (Direction direction : Direction.values()) {
            Node node2 = this.findAcceptedNode(node.x + direction.getStepX(), node.y + direction.getStepY(), node.z + direction.getStepZ());
            map.put(direction, node2);
            if (!this.isNodeValid(node2)) continue;
            nodes[i++] = node2;
        }
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            Node node3;
            Direction direction3 = direction2.getClockWise();
            if (!SwimNodeEvaluator.hasMalus((Node)map.get(direction2)) || !SwimNodeEvaluator.hasMalus((Node)map.get(direction3)) || !this.isNodeValid(node3 = this.findAcceptedNode(node.x + direction2.getStepX() + direction3.getStepX(), node.y, node.z + direction2.getStepZ() + direction3.getStepZ()))) continue;
            nodes[i++] = node3;
        }
        return i;
    }

    protected boolean isNodeValid(@Nullable Node node) {
        return node != null && !node.closed;
    }

    private static boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    protected @Nullable Node findAcceptedNode(int i, int j, int k) {
        float f;
        Node node = null;
        PathType pathType = this.getCachedBlockType(i, j, k);
        if ((this.allowBreaching && pathType == PathType.BREACH || pathType == PathType.WATER) && (f = this.mob.getPathfindingMalus(pathType)) >= 0.0f) {
            node = this.getNode(i, j, k);
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, f);
            if (this.currentContext.level().getFluidState(new BlockPos(i, j, k)).isEmpty()) {
                node.costMalus += 8.0f;
            }
        }
        return node;
    }

    protected PathType getCachedBlockType(int i, int j, int k) {
        return (PathType)((Object)this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), l -> this.getPathType(this.currentContext, i, j, k)));
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int i, int j, int k) {
        return this.getPathTypeOfMob(pathfindingContext, i, j, k, this.mob);
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathfindingContext, int i, int j, int k, Mob mob) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = i; l < i + this.entityWidth; ++l) {
            for (int m = j; m < j + this.entityHeight; ++m) {
                for (int n = k; n < k + this.entityDepth; ++n) {
                    BlockState blockState = pathfindingContext.getBlockState(mutableBlockPos.set(l, m, n));
                    FluidState fluidState = blockState.getFluidState();
                    if (fluidState.isEmpty() && blockState.isPathfindable(PathComputationType.WATER) && blockState.isAir()) {
                        return PathType.BREACH;
                    }
                    if (fluidState.is(FluidTags.WATER)) continue;
                    return PathType.BLOCKED;
                }
            }
        }
        BlockState blockState2 = pathfindingContext.getBlockState(mutableBlockPos);
        if (blockState2.isPathfindable(PathComputationType.WATER)) {
            return PathType.WATER;
        }
        return PathType.BLOCKED;
    }
}

