/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class FlyNodeEvaluator
extends WalkNodeEvaluator {
    private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap();
    private static final float SMALL_MOB_SIZE = 1.0f;
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.1f;
    private static final int MAX_START_NODE_CANDIDATES = 10;

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypeByPosCache.clear();
        mob.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos blockPos;
        int i;
        if (this.canFloat() && this.mob.isInWater()) {
            i = this.mob.getBlockY();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());
            BlockState blockState = this.currentContext.getBlockState(mutableBlockPos);
            while (blockState.is(Blocks.WATER)) {
                mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ());
                blockState = this.currentContext.getBlockState(mutableBlockPos);
            }
        } else {
            i = Mth.floor(this.mob.getY() + 0.5);
        }
        if (!this.canStartAt(blockPos = BlockPos.containing(this.mob.getX(), i, this.mob.getZ()))) {
            for (BlockPos blockPos2 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
                if (!this.canStartAt(blockPos2)) continue;
                return super.getStartNode(blockPos2);
            }
        }
        return super.getStartNode(blockPos);
    }

    @Override
    protected boolean canStartAt(BlockPos blockPos) {
        PathType pathType = this.getCachedPathType(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return this.mob.getPathfindingMalus(pathType) >= 0.0f;
    }

    @Override
    public Target getTarget(double d, double e, double f) {
        return this.getTargetNodeAt(d, e, f);
    }

    @Override
    public int getNeighbors(Node[] nodes, Node node) {
        Node node27;
        Node node26;
        Node node25;
        Node node24;
        Node node23;
        Node node22;
        Node node21;
        Node node20;
        Node node19;
        Node node18;
        Node node17;
        Node node16;
        Node node15;
        Node node14;
        Node node13;
        Node node12;
        Node node11;
        Node node10;
        Node node9;
        Node node8;
        Node node7;
        Node node6;
        Node node5;
        Node node4;
        Node node3;
        int i = 0;
        Node node2 = this.findAcceptedNode(node.x, node.y, node.z + 1);
        if (this.isOpen(node2)) {
            nodes[i++] = node2;
        }
        if (this.isOpen(node3 = this.findAcceptedNode(node.x - 1, node.y, node.z))) {
            nodes[i++] = node3;
        }
        if (this.isOpen(node4 = this.findAcceptedNode(node.x + 1, node.y, node.z))) {
            nodes[i++] = node4;
        }
        if (this.isOpen(node5 = this.findAcceptedNode(node.x, node.y, node.z - 1))) {
            nodes[i++] = node5;
        }
        if (this.isOpen(node6 = this.findAcceptedNode(node.x, node.y + 1, node.z))) {
            nodes[i++] = node6;
        }
        if (this.isOpen(node7 = this.findAcceptedNode(node.x, node.y - 1, node.z))) {
            nodes[i++] = node7;
        }
        if (this.isOpen(node8 = this.findAcceptedNode(node.x, node.y + 1, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node6)) {
            nodes[i++] = node8;
        }
        if (this.isOpen(node9 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z)) && this.hasMalus(node3) && this.hasMalus(node6)) {
            nodes[i++] = node9;
        }
        if (this.isOpen(node10 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z)) && this.hasMalus(node4) && this.hasMalus(node6)) {
            nodes[i++] = node10;
        }
        if (this.isOpen(node11 = this.findAcceptedNode(node.x, node.y + 1, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node6)) {
            nodes[i++] = node11;
        }
        if (this.isOpen(node12 = this.findAcceptedNode(node.x, node.y - 1, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node7)) {
            nodes[i++] = node12;
        }
        if (this.isOpen(node13 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z)) && this.hasMalus(node3) && this.hasMalus(node7)) {
            nodes[i++] = node13;
        }
        if (this.isOpen(node14 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z)) && this.hasMalus(node4) && this.hasMalus(node7)) {
            nodes[i++] = node14;
        }
        if (this.isOpen(node15 = this.findAcceptedNode(node.x, node.y - 1, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node7)) {
            nodes[i++] = node15;
        }
        if (this.isOpen(node16 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node4)) {
            nodes[i++] = node16;
        }
        if (this.isOpen(node17 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node4)) {
            nodes[i++] = node17;
        }
        if (this.isOpen(node18 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node3)) {
            nodes[i++] = node18;
        }
        if (this.isOpen(node19 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node3)) {
            nodes[i++] = node19;
        }
        if (this.isOpen(node20 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z - 1)) && this.hasMalus(node16) && this.hasMalus(node5) && this.hasMalus(node4) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node10)) {
            nodes[i++] = node20;
        }
        if (this.isOpen(node21 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z + 1)) && this.hasMalus(node17) && this.hasMalus(node2) && this.hasMalus(node4) && this.hasMalus(node6) && this.hasMalus(node8) && this.hasMalus(node10)) {
            nodes[i++] = node21;
        }
        if (this.isOpen(node22 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z - 1)) && this.hasMalus(node18) && this.hasMalus(node5) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node9)) {
            nodes[i++] = node22;
        }
        if (this.isOpen(node23 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z + 1)) && this.hasMalus(node19) && this.hasMalus(node2) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node8) && this.hasMalus(node9)) {
            nodes[i++] = node23;
        }
        if (this.isOpen(node24 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z - 1)) && this.hasMalus(node16) && this.hasMalus(node5) && this.hasMalus(node4) && this.hasMalus(node7) && this.hasMalus(node15) && this.hasMalus(node14)) {
            nodes[i++] = node24;
        }
        if (this.isOpen(node25 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z + 1)) && this.hasMalus(node17) && this.hasMalus(node2) && this.hasMalus(node4) && this.hasMalus(node7) && this.hasMalus(node12) && this.hasMalus(node14)) {
            nodes[i++] = node25;
        }
        if (this.isOpen(node26 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z - 1)) && this.hasMalus(node18) && this.hasMalus(node5) && this.hasMalus(node3) && this.hasMalus(node7) && this.hasMalus(node15) && this.hasMalus(node13)) {
            nodes[i++] = node26;
        }
        if (this.isOpen(node27 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z + 1)) && this.hasMalus(node19) && this.hasMalus(node2) && this.hasMalus(node3) && this.hasMalus(node7) && this.hasMalus(node12) && this.hasMalus(node13)) {
            nodes[i++] = node27;
        }
        return i;
    }

    private boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    private boolean isOpen(@Nullable Node node) {
        return node != null && !node.closed;
    }

    protected @Nullable Node findAcceptedNode(int i, int j, int k) {
        Node node = null;
        PathType pathType = this.getCachedPathType(i, j, k);
        float f = this.mob.getPathfindingMalus(pathType);
        if (f >= 0.0f) {
            node = this.getNode(i, j, k);
            node.type = pathType;
            node.costMalus = Math.max(node.costMalus, f);
            if (pathType == PathType.WALKABLE) {
                node.costMalus += 1.0f;
            }
        }
        return node;
    }

    @Override
    protected PathType getCachedPathType(int i, int j, int k) {
        return (PathType)((Object)this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), l -> this.getPathTypeOfMob(this.currentContext, i, j, k, this.mob)));
    }

    @Override
    public PathType getPathType(PathfindingContext pathfindingContext, int i, int j, int k) {
        PathType pathType = pathfindingContext.getPathTypeFromState(i, j, k);
        if (pathType == PathType.OPEN && j >= pathfindingContext.level().getMinY() + 1) {
            BlockPos blockPos = new BlockPos(i, j - 1, k);
            PathType pathType2 = pathfindingContext.getPathTypeFromState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
                pathType = PathType.DAMAGE_FIRE;
            } else if (pathType2 == PathType.DAMAGE_OTHER) {
                pathType = PathType.DAMAGE_OTHER;
            } else if (pathType2 == PathType.COCOA) {
                pathType = PathType.COCOA;
            } else if (pathType2 == PathType.FENCE) {
                if (!blockPos.equals(pathfindingContext.mobPosition())) {
                    pathType = PathType.FENCE;
                }
            } else {
                PathType pathType3 = pathType = pathType2 == PathType.WALKABLE || pathType2 == PathType.OPEN || pathType2 == PathType.WATER ? PathType.OPEN : PathType.WALKABLE;
            }
        }
        if (pathType == PathType.WALKABLE || pathType == PathType.OPEN) {
            pathType = FlyNodeEvaluator.checkNeighbourBlocks(pathfindingContext, i, j, k, pathType);
        }
        return pathType;
    }

    private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob mob) {
        boolean bl;
        AABB aABB = mob.getBoundingBox();
        boolean bl2 = bl = aABB.getSize() < 1.0;
        if (!bl) {
            return List.of((Object)BlockPos.containing(aABB.minX, mob.getBlockY(), aABB.minZ), (Object)BlockPos.containing(aABB.minX, mob.getBlockY(), aABB.maxZ), (Object)BlockPos.containing(aABB.maxX, mob.getBlockY(), aABB.minZ), (Object)BlockPos.containing(aABB.maxX, mob.getBlockY(), aABB.maxZ));
        }
        double d = Math.max(0.0, (double)1.1f - aABB.getZsize());
        double e = Math.max(0.0, (double)1.1f - aABB.getXsize());
        double f = Math.max(0.0, (double)1.1f - aABB.getYsize());
        AABB aABB2 = aABB.inflate(e, f, d);
        return BlockPos.randomBetweenClosed(mob.getRandom(), 10, Mth.floor(aABB2.minX), Mth.floor(aABB2.minY), Mth.floor(aABB2.minZ), Mth.floor(aABB2.maxX), Mth.floor(aABB2.maxY), Mth.floor(aABB2.maxZ));
    }
}

