/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jspecify.annotations.Nullable;

public class LevelChunkTicks<T>
implements SerializableTickContainer<T>,
TickContainerAccess<T> {
    private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue(ScheduledTick.DRAIN_ORDER);
    private @Nullable List<SavedTick<T>> pendingTicks;
    private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet(ScheduledTick.UNIQUE_TICK_HASH);
    private @Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

    public LevelChunkTicks() {
    }

    public LevelChunkTicks(List<SavedTick<T>> list) {
        this.pendingTicks = list;
        for (SavedTick<T> savedTick : list) {
            this.ticksPerPosition.add(ScheduledTick.probe(savedTick.type(), savedTick.pos()));
        }
    }

    public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> biConsumer) {
        this.onTickAdded = biConsumer;
    }

    public @Nullable ScheduledTick<T> peek() {
        return this.tickQueue.peek();
    }

    public @Nullable ScheduledTick<T> poll() {
        ScheduledTick<T> scheduledTick = this.tickQueue.poll();
        if (scheduledTick != null) {
            this.ticksPerPosition.remove(scheduledTick);
        }
        return scheduledTick;
    }

    @Override
    public void schedule(ScheduledTick<T> scheduledTick) {
        if (this.ticksPerPosition.add(scheduledTick)) {
            this.scheduleUnchecked(scheduledTick);
        }
    }

    private void scheduleUnchecked(ScheduledTick<T> scheduledTick) {
        this.tickQueue.add(scheduledTick);
        if (this.onTickAdded != null) {
            this.onTickAdded.accept(this, scheduledTick);
        }
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return this.ticksPerPosition.contains(ScheduledTick.probe(object, blockPos));
    }

    public void removeIf(Predicate<ScheduledTick<T>> predicate) {
        Iterator iterator = this.tickQueue.iterator();
        while (iterator.hasNext()) {
            ScheduledTick scheduledTick = (ScheduledTick)((Object)iterator.next());
            if (!predicate.test(scheduledTick)) continue;
            iterator.remove();
            this.ticksPerPosition.remove((Object)scheduledTick);
        }
    }

    public Stream<ScheduledTick<T>> getAll() {
        return this.tickQueue.stream();
    }

    @Override
    public int count() {
        return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
    }

    @Override
    public List<SavedTick<T>> pack(long l) {
        ArrayList<SavedTick<T>> list = new ArrayList<SavedTick<T>>(this.tickQueue.size());
        if (this.pendingTicks != null) {
            list.addAll(this.pendingTicks);
        }
        for (ScheduledTick scheduledTick : this.tickQueue) {
            list.add(scheduledTick.toSavedTick(l));
        }
        return list;
    }

    public void unpack(long l) {
        if (this.pendingTicks != null) {
            int i = -this.pendingTicks.size();
            for (SavedTick<T> savedTick : this.pendingTicks) {
                this.scheduleUnchecked(savedTick.unpack(l, i++));
            }
        }
        this.pendingTicks = null;
    }
}

