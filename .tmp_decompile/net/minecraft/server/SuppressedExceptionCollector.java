/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Queue;
import net.minecraft.util.ArrayListDeque;

public class SuppressedExceptionCollector {
    private static final int LATEST_ENTRY_COUNT = 8;
    private final Queue<LongEntry> latestEntries = new ArrayListDeque<LongEntry>();
    private final Object2IntLinkedOpenHashMap<ShortEntry> entryCounts = new Object2IntLinkedOpenHashMap();

    private static long currentTimeMs() {
        return System.currentTimeMillis();
    }

    public synchronized void addEntry(String string, Throwable throwable) {
        long l = SuppressedExceptionCollector.currentTimeMs();
        String string2 = throwable.getMessage();
        this.latestEntries.add(new LongEntry(l, string, throwable.getClass(), string2));
        while (this.latestEntries.size() > 8) {
            this.latestEntries.remove();
        }
        ShortEntry shortEntry = new ShortEntry(string, throwable.getClass());
        int i = this.entryCounts.getInt((Object)shortEntry);
        this.entryCounts.putAndMoveToFirst((Object)shortEntry, i + 1);
    }

    public synchronized String dump() {
        long l = SuppressedExceptionCollector.currentTimeMs();
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.latestEntries.isEmpty()) {
            stringBuilder.append("\n\t\tLatest entries:\n");
            for (LongEntry longEntry : this.latestEntries) {
                stringBuilder.append("\t\t\t").append(longEntry.location).append(":").append(longEntry.cls).append(": ").append(longEntry.message).append(" (").append(l - longEntry.timestampMs).append("ms ago)").append("\n");
            }
        }
        if (!this.entryCounts.isEmpty()) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("\t\tEntry counts:\n");
            for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(this.entryCounts)) {
                stringBuilder.append("\t\t\t").append(((ShortEntry)((Object)entry.getKey())).location).append(":").append(((ShortEntry)((Object)entry.getKey())).cls).append(" x ").append(entry.getIntValue()).append("\n");
            }
        }
        if (stringBuilder.isEmpty()) {
            return "~~NONE~~";
        }
        return stringBuilder.toString();
    }

    static final class LongEntry
    extends Record {
        final long timestampMs;
        final String location;
        final Class<? extends Throwable> cls;
        final String message;

        LongEntry(long l, String string, Class<? extends Throwable> class_, String string2) {
            this.timestampMs = l;
            this.location = string;
            this.cls = class_;
            this.message = string2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this, object);
        }

        public long timestampMs() {
            return this.timestampMs;
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }

        public String message() {
            return this.message;
        }
    }

    static final class ShortEntry
    extends Record {
        final String location;
        final Class<? extends Throwable> cls;

        ShortEntry(String string, Class<? extends Throwable> class_) {
            this.location = string;
            this.cls = class_;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this, object);
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }
    }
}

