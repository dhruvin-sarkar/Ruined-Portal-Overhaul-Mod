/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Table
 *  com.google.common.primitives.UnsignedLong
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.timers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerCallbacks;
import org.slf4j.Logger;

public class TimerQueue<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CALLBACK_DATA_TAG = "Callback";
    private static final String TIMER_NAME_TAG = "Name";
    private static final String TIMER_TRIGGER_TIME_TAG = "TriggerTime";
    private final TimerCallbacks<T> callbacksRegistry;
    private final Queue<Event<T>> queue = new PriorityQueue<Event<T>>(TimerQueue.createComparator());
    private UnsignedLong sequentialId = UnsignedLong.ZERO;
    private final Table<String, Long, Event<T>> events = HashBasedTable.create();

    private static <T> Comparator<Event<T>> createComparator() {
        return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.sequentialId);
    }

    public TimerQueue(TimerCallbacks<T> timerCallbacks, Stream<? extends Dynamic<?>> stream) {
        this(timerCallbacks);
        this.queue.clear();
        this.events.clear();
        this.sequentialId = UnsignedLong.ZERO;
        stream.forEach(dynamic -> {
            Tag tag = (Tag)dynamic.convert((DynamicOps)NbtOps.INSTANCE).getValue();
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                this.loadEvent(compoundTag);
            } else {
                LOGGER.warn("Invalid format of events: {}", (Object)tag);
            }
        });
    }

    public TimerQueue(TimerCallbacks<T> timerCallbacks) {
        this.callbacksRegistry = timerCallbacks;
    }

    public void tick(T object, long l) {
        Event<T> event;
        while ((event = this.queue.peek()) != null && event.triggerTime <= l) {
            this.queue.remove();
            this.events.remove((Object)event.id, (Object)l);
            event.callback.handle(object, this, l);
        }
    }

    public void schedule(String string, long l, TimerCallback<T> timerCallback) {
        if (this.events.contains((Object)string, (Object)l)) {
            return;
        }
        this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
        Event<T> event = new Event<T>(l, this.sequentialId, string, timerCallback);
        this.events.put((Object)string, (Object)l, event);
        this.queue.add(event);
    }

    public int remove(String string) {
        Collection collection = this.events.row((Object)string).values();
        collection.forEach(this.queue::remove);
        int i = collection.size();
        collection.clear();
        return i;
    }

    public Set<String> getEventsIds() {
        return Collections.unmodifiableSet(this.events.rowKeySet());
    }

    private void loadEvent(CompoundTag compoundTag) {
        TimerCallback timerCallback = compoundTag.read(CALLBACK_DATA_TAG, this.callbacksRegistry.codec()).orElse(null);
        if (timerCallback != null) {
            String string = compoundTag.getStringOr(TIMER_NAME_TAG, "");
            long l = compoundTag.getLongOr(TIMER_TRIGGER_TIME_TAG, 0L);
            this.schedule(string, l, timerCallback);
        }
    }

    private CompoundTag storeEvent(Event<T> event) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(TIMER_NAME_TAG, event.id);
        compoundTag.putLong(TIMER_TRIGGER_TIME_TAG, event.triggerTime);
        compoundTag.store(CALLBACK_DATA_TAG, this.callbacksRegistry.codec(), event.callback);
        return compoundTag;
    }

    public ListTag store() {
        ListTag listTag = new ListTag();
        this.queue.stream().sorted(TimerQueue.createComparator()).map(this::storeEvent).forEach(listTag::add);
        return listTag;
    }

    public static class Event<T> {
        public final long triggerTime;
        public final UnsignedLong sequentialId;
        public final String id;
        public final TimerCallback<T> callback;

        Event(long l, UnsignedLong unsignedLong, String string, TimerCallback<T> timerCallback) {
            this.triggerTime = l;
            this.sequentialId = unsignedLong;
            this.id = string;
            this.callback = timerCallback;
        }
    }
}

