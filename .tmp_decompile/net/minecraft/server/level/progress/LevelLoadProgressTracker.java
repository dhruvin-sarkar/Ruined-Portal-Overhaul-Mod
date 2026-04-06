/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level.progress;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class LevelLoadProgressTracker
implements LevelLoadListener {
    private static final int PREPARE_SERVER_WEIGHT = 10;
    private static final int EXPECTED_PLAYER_CHUNKS = Mth.square(7);
    private final boolean includePlayerChunks;
    private int totalWeight;
    private int finalizedWeight;
    private int segmentWeight;
    private float segmentFraction;
    private volatile float progress;

    public LevelLoadProgressTracker(boolean bl) {
        this.includePlayerChunks = bl;
    }

    @Override
    public void start(LevelLoadListener.Stage stage, int i) {
        if (!this.tracksStage(stage)) {
            return;
        }
        switch (stage) {
            case LOAD_INITIAL_CHUNKS: {
                int j = this.includePlayerChunks ? EXPECTED_PLAYER_CHUNKS : 0;
                this.totalWeight = 10 + i + j;
                this.beginSegment(10);
                this.finishSegment();
                this.beginSegment(i);
                break;
            }
            case LOAD_PLAYER_CHUNKS: {
                this.beginSegment(EXPECTED_PLAYER_CHUNKS);
            }
        }
    }

    private void beginSegment(int i) {
        this.segmentWeight = i;
        this.segmentFraction = 0.0f;
        this.updateProgress();
    }

    @Override
    public void update(LevelLoadListener.Stage stage, int i, int j) {
        if (this.tracksStage(stage)) {
            this.segmentFraction = j == 0 ? 0.0f : (float)i / (float)j;
            this.updateProgress();
        }
    }

    @Override
    public void finish(LevelLoadListener.Stage stage) {
        if (this.tracksStage(stage)) {
            this.finishSegment();
        }
    }

    private void finishSegment() {
        this.finalizedWeight += this.segmentWeight;
        this.segmentWeight = 0;
        this.updateProgress();
    }

    private boolean tracksStage(LevelLoadListener.Stage stage) {
        return switch (stage) {
            case LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS -> true;
            case LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS -> this.includePlayerChunks;
            default -> false;
        };
    }

    private void updateProgress() {
        if (this.totalWeight == 0) {
            this.progress = 0.0f;
        } else {
            float f = (float)this.finalizedWeight + this.segmentFraction * (float)this.segmentWeight;
            this.progress = f / (float)this.totalWeight;
        }
    }

    public float get() {
        return this.progress;
    }

    @Override
    public void updateFocus(ResourceKey<Level> resourceKey, ChunkPos chunkPos) {
    }
}

