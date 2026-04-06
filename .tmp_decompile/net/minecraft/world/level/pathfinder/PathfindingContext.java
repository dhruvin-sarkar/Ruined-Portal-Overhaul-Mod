/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jspecify.annotations.Nullable;

public class PathfindingContext {
    private final CollisionGetter level;
    private final @Nullable PathTypeCache cache;
    private final BlockPos mobPosition;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public PathfindingContext(CollisionGetter collisionGetter, Mob mob) {
        this.level = collisionGetter;
        Level level = mob.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.cache = serverLevel.getPathTypeCache();
        } else {
            this.cache = null;
        }
        this.mobPosition = mob.blockPosition();
    }

    public PathType getPathTypeFromState(int i, int j, int k) {
        BlockPos.MutableBlockPos blockPos = this.mutablePos.set(i, j, k);
        if (this.cache == null) {
            return WalkNodeEvaluator.getPathTypeFromState(this.level, blockPos);
        }
        return this.cache.getOrCompute(this.level, blockPos);
    }

    public BlockState getBlockState(BlockPos blockPos) {
        return this.level.getBlockState(blockPos);
    }

    public CollisionGetter level() {
        return this.level;
    }

    public BlockPos mobPosition() {
        return this.mobPosition;
    }
}

