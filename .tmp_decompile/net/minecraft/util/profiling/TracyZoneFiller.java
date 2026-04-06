/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  java.lang.StackWalker
 *  java.lang.StackWalker$Option
 *  java.lang.StackWalker$StackFrame
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller
implements ProfilerFiller {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance((Set)Set.of((Object)StackWalker.Option.RETAIN_CLASS_REFERENCE), (int)5);
    private final List<Zone> activeZones = new ArrayList<Zone>();
    private final Map<String, PlotAndValue> plots = new HashMap<String, PlotAndValue>();
    private final String name = Thread.currentThread().getName();

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
        for (PlotAndValue plotAndValue : this.plots.values()) {
            plotAndValue.set(0);
        }
    }

    @Override
    public void push(String string) {
        Optional optional;
        String string2 = "";
        String string3 = "";
        int i = 0;
        if (SharedConstants.IS_RUNNING_IN_IDE && (optional = (Optional)STACK_WALKER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != TracyZoneFiller.class && stackFrame.getDeclaringClass() != ProfilerFiller.CombinedProfileFiller.class).findFirst())).isPresent()) {
            StackWalker.StackFrame stackFrame = (StackWalker.StackFrame)optional.get();
            string2 = stackFrame.getMethodName();
            string3 = stackFrame.getFileName();
            i = stackFrame.getLineNumber();
        }
        Zone zone = TracyClient.beginZone((String)string, (String)string2, (String)string3, (int)i);
        this.activeZones.add(zone);
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.push(supplier.get());
    }

    @Override
    public void pop() {
        if (this.activeZones.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        Zone zone = (Zone)this.activeZones.removeLast();
        zone.close();
    }

    @Override
    public void popPush(String string) {
        this.pop();
        this.push(string);
    }

    @Override
    public void popPush(Supplier<String> supplier) {
        this.pop();
        this.push(supplier.get());
    }

    @Override
    public void markForCharting(MetricCategory metricCategory) {
    }

    @Override
    public void incrementCounter(String string, int i) {
        this.plots.computeIfAbsent(string, string2 -> new PlotAndValue(this.name + " " + string)).add(i);
    }

    @Override
    public void incrementCounter(Supplier<String> supplier, int i) {
        this.incrementCounter(supplier.get(), i);
    }

    private Zone activeZone() {
        return (Zone)this.activeZones.getLast();
    }

    @Override
    public void addZoneText(String string) {
        this.activeZone().addText(string);
    }

    @Override
    public void addZoneValue(long l) {
        this.activeZone().addValue(l);
    }

    @Override
    public void setZoneColor(int i) {
        this.activeZone().setColor(i);
    }

    static final class PlotAndValue {
        private final Plot plot;
        private int value;

        PlotAndValue(String string) {
            this.plot = TracyClient.createPlot((String)string);
            this.value = 0;
        }

        void set(int i) {
            this.value = i;
            this.plot.setValue((double)i);
        }

        void add(int i) {
            this.set(this.value + i);
        }
    }
}

