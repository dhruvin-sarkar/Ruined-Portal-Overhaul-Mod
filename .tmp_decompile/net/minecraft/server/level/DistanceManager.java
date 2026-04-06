/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ByteMaps
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMaps
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongConsumer
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectSet
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMaps;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.LoadingChunkTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.SimulationChunkTracker;
import net.minecraft.server.level.ThrottlingChunkTaskDispatcher;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.TriState;
import net.minecraft.util.thread.TaskScheduler;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class DistanceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final int PLAYER_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap();
    private final LoadingChunkTracker loadingChunkTracker;
    private final SimulationChunkTracker simulationChunkTracker;
    final TicketStorage ticketStorage;
    private final FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new FixedPlayerDistanceChunkTracker(8);
    private final PlayerTicketTracker playerTicketManager = new PlayerTicketTracker(32);
    protected final Set<ChunkHolder> chunksToUpdateFutures = new ReferenceOpenHashSet();
    final ThrottlingChunkTaskDispatcher ticketDispatcher;
    final LongSet ticketsToRelease = new LongOpenHashSet();
    final Executor mainThreadExecutor;
    private int simulationDistance = 10;

    protected DistanceManager(TicketStorage ticketStorage, Executor executor, Executor executor2) {
        this.ticketStorage = ticketStorage;
        this.loadingChunkTracker = new LoadingChunkTracker(this, ticketStorage);
        this.simulationChunkTracker = new SimulationChunkTracker(ticketStorage);
        TaskScheduler<Runnable> taskScheduler = TaskScheduler.wrapExecutor("player ticket throttler", executor2);
        this.ticketDispatcher = new ThrottlingChunkTaskDispatcher(taskScheduler, executor, 4);
        this.mainThreadExecutor = executor2;
    }

    protected abstract boolean isChunkToRemove(long var1);

    protected abstract @Nullable ChunkHolder getChunk(long var1);

    protected abstract @Nullable ChunkHolder updateChunkScheduling(long var1, int var3, @Nullable ChunkHolder var4, int var5);

    public boolean runAllUpdates(ChunkMap chunkMap) {
        boolean bl;
        this.naturalSpawnChunkCounter.runAllUpdates();
        this.simulationChunkTracker.runAllUpdates();
        this.playerTicketManager.runAllUpdates();
        int i = Integer.MAX_VALUE - this.loadingChunkTracker.runDistanceUpdates(Integer.MAX_VALUE);
        boolean bl2 = bl = i != 0;
        if (bl && SharedConstants.DEBUG_VERBOSE_SERVER_EVENTS) {
            LOGGER.debug("DMU {}", (Object)i);
        }
        if (!this.chunksToUpdateFutures.isEmpty()) {
            for (ChunkHolder chunkHolder : this.chunksToUpdateFutures) {
                chunkHolder.updateHighestAllowedStatus(chunkMap);
            }
            for (ChunkHolder chunkHolder : this.chunksToUpdateFutures) {
                chunkHolder.updateFutures(chunkMap, this.mainThreadExecutor);
            }
            this.chunksToUpdateFutures.clear();
            return true;
        }
        if (!this.ticketsToRelease.isEmpty()) {
            LongIterator longIterator = this.ticketsToRelease.iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                if (!this.ticketStorage.getTickets(l).stream().anyMatch(ticket -> ticket.getType() == TicketType.PLAYER_LOADING)) continue;
                ChunkHolder chunkHolder2 = chunkMap.getUpdatingChunkIfPresent(l);
                if (chunkHolder2 == null) {
                    throw new IllegalStateException();
                }
                CompletableFuture<ChunkResult<LevelChunk>> completableFuture = chunkHolder2.getEntityTickingChunkFuture();
                completableFuture.thenAccept(chunkResult -> this.mainThreadExecutor.execute(() -> this.ticketDispatcher.release(l, () -> {}, false)));
            }
            this.ticketsToRelease.clear();
        }
        return bl;
    }

    public void addPlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
        ChunkPos chunkPos = sectionPos.chunk();
        long l2 = chunkPos.toLong();
        ((ObjectSet)this.playersPerChunk.computeIfAbsent(l2, l -> new ObjectOpenHashSet())).add((Object)serverPlayer);
        this.naturalSpawnChunkCounter.update(l2, 0, true);
        this.playerTicketManager.update(l2, 0, true);
        this.ticketStorage.addTicket(new Ticket(TicketType.PLAYER_SIMULATION, this.getPlayerTicketLevel()), chunkPos);
    }

    public void removePlayer(SectionPos sectionPos, ServerPlayer serverPlayer) {
        ChunkPos chunkPos = sectionPos.chunk();
        long l = chunkPos.toLong();
        ObjectSet objectSet = (ObjectSet)this.playersPerChunk.get(l);
        objectSet.remove((Object)serverPlayer);
        if (objectSet.isEmpty()) {
            this.playersPerChunk.remove(l);
            this.naturalSpawnChunkCounter.update(l, Integer.MAX_VALUE, false);
            this.playerTicketManager.update(l, Integer.MAX_VALUE, false);
            this.ticketStorage.removeTicket(new Ticket(TicketType.PLAYER_SIMULATION, this.getPlayerTicketLevel()), chunkPos);
        }
    }

    private int getPlayerTicketLevel() {
        return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.simulationDistance);
    }

    public boolean inEntityTickingRange(long l) {
        return ChunkLevel.isEntityTicking(this.simulationChunkTracker.getLevel(l));
    }

    public boolean inBlockTickingRange(long l) {
        return ChunkLevel.isBlockTicking(this.simulationChunkTracker.getLevel(l));
    }

    public int getChunkLevel(long l, boolean bl) {
        if (bl) {
            return this.simulationChunkTracker.getLevel(l);
        }
        return this.loadingChunkTracker.getLevel(l);
    }

    protected void updatePlayerTickets(int i) {
        this.playerTicketManager.updateViewDistance(i);
    }

    public void updateSimulationDistance(int i) {
        if (i != this.simulationDistance) {
            this.simulationDistance = i;
            this.ticketStorage.replaceTicketLevelOfType(this.getPlayerTicketLevel(), TicketType.PLAYER_SIMULATION);
        }
    }

    public int getNaturalSpawnChunkCount() {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.size();
    }

    public TriState hasPlayersNearby(long l) {
        this.naturalSpawnChunkCounter.runAllUpdates();
        int i = this.naturalSpawnChunkCounter.getLevel(l);
        if (i <= NaturalSpawner.INSCRIBED_SQUARE_SPAWN_DISTANCE_CHUNK) {
            return TriState.TRUE;
        }
        if (i > 8) {
            return TriState.FALSE;
        }
        return TriState.DEFAULT;
    }

    public void forEachEntityTickingChunk(LongConsumer longConsumer) {
        for (Long2ByteMap.Entry entry : Long2ByteMaps.fastIterable((Long2ByteMap)this.simulationChunkTracker.chunks)) {
            byte b = entry.getByteValue();
            long l = entry.getLongKey();
            if (!ChunkLevel.isEntityTicking(b)) continue;
            longConsumer.accept(l);
        }
    }

    public LongIterator getSpawnCandidateChunks() {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.keySet().iterator();
    }

    public String getDebugStatus() {
        return this.ticketDispatcher.getDebugStatus();
    }

    public boolean hasTickets() {
        return this.ticketStorage.hasTickets();
    }

    class FixedPlayerDistanceChunkTracker
    extends ChunkTracker {
        protected final Long2ByteMap chunks;
        protected final int maxDistance;

        protected FixedPlayerDistanceChunkTracker(int i) {
            super(i + 2, 16, 256);
            this.chunks = new Long2ByteOpenHashMap();
            this.maxDistance = i;
            this.chunks.defaultReturnValue((byte)(i + 2));
        }

        @Override
        protected int getLevel(long l) {
            return this.chunks.get(l);
        }

        @Override
        protected void setLevel(long l, int i) {
            byte b = i > this.maxDistance ? this.chunks.remove(l) : this.chunks.put(l, (byte)i);
            this.onLevelChange(l, b, i);
        }

        protected void onLevelChange(long l, int i, int j) {
        }

        @Override
        protected int getLevelFromSource(long l) {
            return this.havePlayer(l) ? 0 : Integer.MAX_VALUE;
        }

        private boolean havePlayer(long l) {
            ObjectSet objectSet = (ObjectSet)DistanceManager.this.playersPerChunk.get(l);
            return objectSet != null && !objectSet.isEmpty();
        }

        public void runAllUpdates() {
            this.runUpdates(Integer.MAX_VALUE);
        }
    }

    class PlayerTicketTracker
    extends FixedPlayerDistanceChunkTracker {
        private int viewDistance;
        private final Long2IntMap queueLevels;
        private final LongSet toUpdate;

        protected PlayerTicketTracker(int i) {
            super(i);
            this.queueLevels = Long2IntMaps.synchronize((Long2IntMap)new Long2IntOpenHashMap());
            this.toUpdate = new LongOpenHashSet();
            this.viewDistance = 0;
            this.queueLevels.defaultReturnValue(i + 2);
        }

        @Override
        protected void onLevelChange(long l, int i, int j) {
            this.toUpdate.add(l);
        }

        public void updateViewDistance(int i) {
            for (Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
                byte b = entry.getByteValue();
                long l = entry.getLongKey();
                this.onLevelChange(l, b, this.haveTicketFor(b), b <= i);
            }
            this.viewDistance = i;
        }

        private void onLevelChange(long l, int i, boolean bl, boolean bl2) {
            if (bl != bl2) {
                Ticket ticket = new Ticket(TicketType.PLAYER_LOADING, PLAYER_TICKET_LEVEL);
                if (bl2) {
                    DistanceManager.this.ticketDispatcher.submit(() -> DistanceManager.this.mainThreadExecutor.execute(() -> {
                        if (this.haveTicketFor(this.getLevel(l))) {
                            DistanceManager.this.ticketStorage.addTicket(l, ticket);
                            DistanceManager.this.ticketsToRelease.add(l);
                        } else {
                            DistanceManager.this.ticketDispatcher.release(l, () -> {}, false);
                        }
                    }), l, () -> i);
                } else {
                    DistanceManager.this.ticketDispatcher.release(l, () -> DistanceManager.this.mainThreadExecutor.execute(() -> DistanceManager.this.ticketStorage.removeTicket(l, ticket)), true);
                }
            }
        }

        @Override
        public void runAllUpdates() {
            super.runAllUpdates();
            if (!this.toUpdate.isEmpty()) {
                LongIterator longIterator = this.toUpdate.iterator();
                while (longIterator.hasNext()) {
                    int j;
                    long l = longIterator.nextLong();
                    int i2 = this.queueLevels.get(l);
                    if (i2 == (j = this.getLevel(l))) continue;
                    DistanceManager.this.ticketDispatcher.onLevelChange(new ChunkPos(l), () -> this.queueLevels.get(l), j, i -> {
                        if (i >= this.queueLevels.defaultReturnValue()) {
                            this.queueLevels.remove(l);
                        } else {
                            this.queueLevels.put(l, i);
                        }
                    });
                    this.onLevelChange(l, j, this.haveTicketFor(i2), this.haveTicketFor(j));
                }
                this.toUpdate.clear();
            }
        }

        private boolean haveTicketFor(int i) {
            return i <= this.viewDistance;
        }
    }
}

