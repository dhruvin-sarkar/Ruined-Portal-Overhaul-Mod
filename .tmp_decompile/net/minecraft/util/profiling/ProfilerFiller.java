/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
    public static final String ROOT = "root";

    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void popPush(String var1);

    public void popPush(Supplier<String> var1);

    default public void addZoneText(String string) {
    }

    default public void addZoneValue(long l) {
    }

    default public void setZoneColor(int i) {
    }

    default public Zone zone(String string) {
        this.push(string);
        return new Zone(this);
    }

    default public Zone zone(Supplier<String> supplier) {
        this.push(supplier);
        return new Zone(this);
    }

    public void markForCharting(MetricCategory var1);

    default public void incrementCounter(String string) {
        this.incrementCounter(string, 1);
    }

    public void incrementCounter(String var1, int var2);

    default public void incrementCounter(Supplier<String> supplier) {
        this.incrementCounter(supplier, 1);
    }

    public void incrementCounter(Supplier<String> var1, int var2);

    public static ProfilerFiller combine(ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2) {
        if (profilerFiller == InactiveProfiler.INSTANCE) {
            return profilerFiller2;
        }
        if (profilerFiller2 == InactiveProfiler.INSTANCE) {
            return profilerFiller;
        }
        return new CombinedProfileFiller(profilerFiller, profilerFiller2);
    }

    public static class CombinedProfileFiller
    implements ProfilerFiller {
        private final ProfilerFiller first;
        private final ProfilerFiller second;

        public CombinedProfileFiller(ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2) {
            this.first = profilerFiller;
            this.second = profilerFiller2;
        }

        @Override
        public void startTick() {
            this.first.startTick();
            this.second.startTick();
        }

        @Override
        public void endTick() {
            this.first.endTick();
            this.second.endTick();
        }

        @Override
        public void push(String string) {
            this.first.push(string);
            this.second.push(string);
        }

        @Override
        public void push(Supplier<String> supplier) {
            this.first.push(supplier);
            this.second.push(supplier);
        }

        @Override
        public void markForCharting(MetricCategory metricCategory) {
            this.first.markForCharting(metricCategory);
            this.second.markForCharting(metricCategory);
        }

        @Override
        public void pop() {
            this.first.pop();
            this.second.pop();
        }

        @Override
        public void popPush(String string) {
            this.first.popPush(string);
            this.second.popPush(string);
        }

        @Override
        public void popPush(Supplier<String> supplier) {
            this.first.popPush(supplier);
            this.second.popPush(supplier);
        }

        @Override
        public void incrementCounter(String string, int i) {
            this.first.incrementCounter(string, i);
            this.second.incrementCounter(string, i);
        }

        @Override
        public void incrementCounter(Supplier<String> supplier, int i) {
            this.first.incrementCounter(supplier, i);
            this.second.incrementCounter(supplier, i);
        }

        @Override
        public void addZoneText(String string) {
            this.first.addZoneText(string);
            this.second.addZoneText(string);
        }

        @Override
        public void addZoneValue(long l) {
            this.first.addZoneValue(l);
            this.second.addZoneValue(l);
        }

        @Override
        public void setZoneColor(int i) {
            this.first.setZoneColor(i);
            this.second.setZoneColor(i);
        }
    }
}

