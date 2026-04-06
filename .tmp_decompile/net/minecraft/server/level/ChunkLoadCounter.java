/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkLoadCounter {
    private final List<ChunkHolder> pendingChunks = new ArrayList<ChunkHolder>();
    private int totalChunks;

    public void track(ServerLevel serverLevel, Runnable runnable) {
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        LongOpenHashSet longSet = new LongOpenHashSet();
        serverChunkCache.runDistanceManagerUpdates();
        serverChunkCache.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach(arg_0 -> ChunkLoadCounter.method_72250((LongSet)longSet, arg_0));
        runnable.run();
        serverChunkCache.runDistanceManagerUpdates();
        serverChunkCache.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach(arg_0 -> this.method_72248((LongSet)longSet, arg_0));
    }

    public int readyChunks() {
        return this.totalChunks - this.pendingChunks();
    }

    public int pendingChunks() {
        this.pendingChunks.removeIf(chunkHolder -> chunkHolder.getLatestStatus() == ChunkStatus.FULL);
        return this.pendingChunks.size();
    }

    public int totalChunks() {
        return this.totalChunks;
    }

    private /* synthetic */ void method_72248(LongSet longSet, ChunkHolder chunkHolder) {
        if (!longSet.contains(chunkHolder.getPos().toLong())) {
            this.pendingChunks.add(chunkHolder);
            ++this.totalChunks;
        }
    }

    private static /* synthetic */ void method_72250(LongSet longSet, ChunkHolder chunkHolder) {
        longSet.add(chunkHolder.getPos().toLong());
    }
}

