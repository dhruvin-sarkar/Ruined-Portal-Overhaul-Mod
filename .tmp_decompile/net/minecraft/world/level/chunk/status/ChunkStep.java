/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTask;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.jspecify.annotations.Nullable;

public final class ChunkStep
extends Record {
    final ChunkStatus targetStatus;
    private final ChunkDependencies directDependencies;
    final ChunkDependencies accumulatedDependencies;
    private final int blockStateWriteRadius;
    private final ChunkStatusTask task;

    public ChunkStep(ChunkStatus chunkStatus, ChunkDependencies chunkDependencies, ChunkDependencies chunkDependencies2, int i, ChunkStatusTask chunkStatusTask) {
        this.targetStatus = chunkStatus;
        this.directDependencies = chunkDependencies;
        this.accumulatedDependencies = chunkDependencies2;
        this.blockStateWriteRadius = i;
        this.task = chunkStatusTask;
    }

    public int getAccumulatedRadiusOf(ChunkStatus chunkStatus) {
        if (chunkStatus == this.targetStatus) {
            return 0;
        }
        return this.accumulatedDependencies.getRadiusOf(chunkStatus);
    }

    public CompletableFuture<ChunkAccess> apply(WorldGenContext worldGenContext, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess2) {
        if (chunkAccess2.getPersistedStatus().isBefore(this.targetStatus)) {
            ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onChunkGenerate(chunkAccess2.getPos(), worldGenContext.level().dimension(), this.targetStatus.getName());
            return this.task.doWork(worldGenContext, this, staticCache2D, chunkAccess2).thenApply(chunkAccess -> this.completeChunkGeneration((ChunkAccess)chunkAccess, profiledDuration));
        }
        return this.task.doWork(worldGenContext, this, staticCache2D, chunkAccess2);
    }

    private ChunkAccess completeChunkGeneration(ChunkAccess chunkAccess, @Nullable ProfiledDuration profiledDuration) {
        ProtoChunk protoChunk;
        if (chunkAccess instanceof ProtoChunk && (protoChunk = (ProtoChunk)chunkAccess).getPersistedStatus().isBefore(this.targetStatus)) {
            protoChunk.setPersistedStatus(this.targetStatus);
        }
        if (profiledDuration != null) {
            profiledDuration.finish(true);
        }
        return chunkAccess;
    }

    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this);
    }

    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this);
    }

    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this, object);
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public ChunkDependencies directDependencies() {
        return this.directDependencies;
    }

    public ChunkDependencies accumulatedDependencies() {
        return this.accumulatedDependencies;
    }

    public int blockStateWriteRadius() {
        return this.blockStateWriteRadius;
    }

    public ChunkStatusTask task() {
        return this.task;
    }

    public static class Builder {
        private final ChunkStatus status;
        private final @Nullable ChunkStep parent;
        private ChunkStatus[] directDependenciesByRadius;
        private int blockStateWriteRadius = -1;
        private ChunkStatusTask task = ChunkStatusTasks::passThrough;

        protected Builder(ChunkStatus chunkStatus) {
            if (chunkStatus.getParent() != chunkStatus) {
                throw new IllegalArgumentException("Not starting with the first status: " + String.valueOf(chunkStatus));
            }
            this.status = chunkStatus;
            this.parent = null;
            this.directDependenciesByRadius = new ChunkStatus[0];
        }

        protected Builder(ChunkStatus chunkStatus, ChunkStep chunkStep) {
            if (chunkStep.targetStatus.getIndex() != chunkStatus.getIndex() - 1) {
                throw new IllegalArgumentException("Out of order status: " + String.valueOf(chunkStatus));
            }
            this.status = chunkStatus;
            this.parent = chunkStep;
            this.directDependenciesByRadius = new ChunkStatus[]{chunkStep.targetStatus};
        }

        public Builder addRequirement(ChunkStatus chunkStatus, int i) {
            if (chunkStatus.isOrAfter(this.status)) {
                throw new IllegalArgumentException("Status " + String.valueOf(chunkStatus) + " can not be required by " + String.valueOf(this.status));
            }
            int j = i + 1;
            ChunkStatus[] chunkStatuss = this.directDependenciesByRadius;
            if (j > chunkStatuss.length) {
                this.directDependenciesByRadius = new ChunkStatus[j];
                Arrays.fill(this.directDependenciesByRadius, chunkStatus);
            }
            for (int k = 0; k < Math.min(j, chunkStatuss.length); ++k) {
                this.directDependenciesByRadius[k] = ChunkStatus.max(chunkStatuss[k], chunkStatus);
            }
            return this;
        }

        public Builder blockStateWriteRadius(int i) {
            this.blockStateWriteRadius = i;
            return this;
        }

        public Builder setTask(ChunkStatusTask chunkStatusTask) {
            this.task = chunkStatusTask;
            return this;
        }

        public ChunkStep build() {
            return new ChunkStep(this.status, new ChunkDependencies((ImmutableList<ChunkStatus>)ImmutableList.copyOf((Object[])this.directDependenciesByRadius)), new ChunkDependencies((ImmutableList<ChunkStatus>)ImmutableList.copyOf((Object[])this.buildAccumulatedDependencies())), this.blockStateWriteRadius, this.task);
        }

        private ChunkStatus[] buildAccumulatedDependencies() {
            if (this.parent == null) {
                return this.directDependenciesByRadius;
            }
            int i = this.getRadiusOfParent(this.parent.targetStatus);
            ChunkDependencies chunkDependencies = this.parent.accumulatedDependencies;
            ChunkStatus[] chunkStatuss = new ChunkStatus[Math.max(i + chunkDependencies.size(), this.directDependenciesByRadius.length)];
            for (int j = 0; j < chunkStatuss.length; ++j) {
                int k = j - i;
                chunkStatuss[j] = k < 0 || k >= chunkDependencies.size() ? this.directDependenciesByRadius[j] : (j >= this.directDependenciesByRadius.length ? chunkDependencies.get(k) : ChunkStatus.max(this.directDependenciesByRadius[j], chunkDependencies.get(k)));
            }
            return chunkStatuss;
        }

        private int getRadiusOfParent(ChunkStatus chunkStatus) {
            for (int i = this.directDependenciesByRadius.length - 1; i >= 0; --i) {
                if (!this.directDependenciesByRadius[i].isOrAfter(chunkStatus)) continue;
                return i;
            }
            return 0;
        }
    }
}

