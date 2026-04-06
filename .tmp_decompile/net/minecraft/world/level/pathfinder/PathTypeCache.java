/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.HashCommon
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jspecify.annotations.Nullable;

public class PathTypeCache {
    private static final int SIZE = 4096;
    private static final int MASK = 4095;
    private final long[] positions = new long[4096];
    private final PathType[] pathTypes = new PathType[4096];

    public PathType getOrCompute(BlockGetter blockGetter, BlockPos blockPos) {
        long l = blockPos.asLong();
        int i = PathTypeCache.index(l);
        PathType pathType = this.get(i, l);
        if (pathType != null) {
            return pathType;
        }
        return this.compute(blockGetter, blockPos, i, l);
    }

    private @Nullable PathType get(int i, long l) {
        if (this.positions[i] == l) {
            return this.pathTypes[i];
        }
        return null;
    }

    private PathType compute(BlockGetter blockGetter, BlockPos blockPos, int i, long l) {
        PathType pathType = WalkNodeEvaluator.getPathTypeFromState(blockGetter, blockPos);
        this.positions[i] = l;
        this.pathTypes[i] = pathType;
        return pathType;
    }

    public void invalidate(BlockPos blockPos) {
        long l = blockPos.asLong();
        int i = PathTypeCache.index(l);
        if (this.positions[i] == l) {
            this.pathTypes[i] = null;
        }
    }

    private static int index(long l) {
        return (int)HashCommon.mix((long)l) & 0xFFF;
    }
}

