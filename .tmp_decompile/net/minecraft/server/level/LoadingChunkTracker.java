/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.TicketStorage;

class LoadingChunkTracker
extends ChunkTracker {
    private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;
    private final DistanceManager distanceManager;
    private final TicketStorage ticketStorage;

    public LoadingChunkTracker(DistanceManager distanceManager, TicketStorage ticketStorage) {
        super(MAX_LEVEL + 1, 16, 256);
        this.distanceManager = distanceManager;
        this.ticketStorage = ticketStorage;
        ticketStorage.setLoadingChunkUpdatedListener(this::update);
    }

    @Override
    protected int getLevelFromSource(long l) {
        return this.ticketStorage.getTicketLevelAt(l, false);
    }

    @Override
    protected int getLevel(long l) {
        ChunkHolder chunkHolder;
        if (!this.distanceManager.isChunkToRemove(l) && (chunkHolder = this.distanceManager.getChunk(l)) != null) {
            return chunkHolder.getTicketLevel();
        }
        return MAX_LEVEL;
    }

    @Override
    protected void setLevel(long l, int i) {
        int j;
        ChunkHolder chunkHolder = this.distanceManager.getChunk(l);
        int n = j = chunkHolder == null ? MAX_LEVEL : chunkHolder.getTicketLevel();
        if (j == i) {
            return;
        }
        if ((chunkHolder = this.distanceManager.updateChunkScheduling(l, i, chunkHolder, j)) != null) {
            this.distanceManager.chunksToUpdateFutures.add(chunkHolder);
        }
    }

    public int runDistanceUpdates(int i) {
        return this.runUpdates(i);
    }
}

