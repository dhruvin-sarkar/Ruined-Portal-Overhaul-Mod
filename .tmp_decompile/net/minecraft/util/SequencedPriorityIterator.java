/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Queues
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Deque;
import org.jspecify.annotations.Nullable;

public final class SequencedPriorityIterator<T>
extends AbstractIterator<T> {
    private static final int MIN_PRIO = Integer.MIN_VALUE;
    private @Nullable Deque<T> highestPrioQueue = null;
    private int highestPrio = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

    public void add(T object, int i2) {
        if (i2 == this.highestPrio && this.highestPrioQueue != null) {
            this.highestPrioQueue.addLast(object);
            return;
        }
        Deque deque = (Deque)this.queuesByPriority.computeIfAbsent(i2, i -> Queues.newArrayDeque());
        deque.addLast(object);
        if (i2 >= this.highestPrio) {
            this.highestPrioQueue = deque;
            this.highestPrio = i2;
        }
    }

    protected @Nullable T computeNext() {
        if (this.highestPrioQueue == null) {
            return (T)this.endOfData();
        }
        T object = this.highestPrioQueue.removeFirst();
        if (object == null) {
            return (T)this.endOfData();
        }
        if (this.highestPrioQueue.isEmpty()) {
            this.switchCacheToNextHighestPrioQueue();
        }
        return object;
    }

    private void switchCacheToNextHighestPrioQueue() {
        int i = Integer.MIN_VALUE;
        Deque deque = null;
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque deque2 = (Deque)entry.getValue();
            int j = entry.getIntKey();
            if (j <= i || deque2.isEmpty()) continue;
            i = j;
            deque = deque2;
            if (j != this.highestPrio - 1) continue;
            break;
        }
        this.highestPrio = i;
        this.highestPrioQueue = deque;
    }
}

