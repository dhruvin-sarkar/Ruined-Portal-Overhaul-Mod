/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.telemetry.events;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GameLoadTimesEvent {
    public static final GameLoadTimesEvent INSTANCE = new GameLoadTimesEvent(Ticker.systemTicker());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Ticker timeSource;
    private final Map<TelemetryProperty<Measurement>, Stopwatch> measurements = new HashMap<TelemetryProperty<Measurement>, Stopwatch>();
    private OptionalLong bootstrapTime = OptionalLong.empty();

    protected GameLoadTimesEvent(Ticker ticker) {
        this.timeSource = ticker;
    }

    public synchronized void beginStep(TelemetryProperty<Measurement> telemetryProperty2) {
        this.beginStep(telemetryProperty2, (TelemetryProperty<Measurement> telemetryProperty) -> Stopwatch.createStarted((Ticker)this.timeSource));
    }

    public synchronized void beginStep(TelemetryProperty<Measurement> telemetryProperty2, Stopwatch stopwatch) {
        this.beginStep(telemetryProperty2, (TelemetryProperty<Measurement> telemetryProperty) -> stopwatch);
    }

    private synchronized void beginStep(TelemetryProperty<Measurement> telemetryProperty, Function<TelemetryProperty<Measurement>, Stopwatch> function) {
        this.measurements.computeIfAbsent(telemetryProperty, function);
    }

    public synchronized void endStep(TelemetryProperty<Measurement> telemetryProperty) {
        Stopwatch stopwatch = this.measurements.get(telemetryProperty);
        if (stopwatch == null) {
            LOGGER.warn("Attempted to end step for {} before starting it", (Object)telemetryProperty.id());
            return;
        }
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
    }

    public void send(TelemetryEventSender telemetryEventSender) {
        telemetryEventSender.send(TelemetryEventType.GAME_LOAD_TIMES, builder -> {
            GameLoadTimesEvent gameLoadTimesEvent = this;
            synchronized (gameLoadTimesEvent) {
                this.measurements.forEach((telemetryProperty, stopwatch) -> {
                    if (!stopwatch.isRunning()) {
                        long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                        builder.put(telemetryProperty, new Measurement((int)l));
                    } else {
                        LOGGER.warn("Measurement {} was discarded since it was still ongoing when the event {} was sent.", (Object)telemetryProperty.id(), (Object)TelemetryEventType.GAME_LOAD_TIMES.id());
                    }
                });
                this.bootstrapTime.ifPresent(l -> builder.put(TelemetryProperty.LOAD_TIME_BOOTSTRAP_MS, new Measurement((int)l)));
                this.measurements.clear();
            }
        });
    }

    public synchronized void setBootstrapTime(long l) {
        this.bootstrapTime = OptionalLong.of(l);
    }

    @Environment(value=EnvType.CLIENT)
    public record Measurement(int millis) {
        public static final Codec<Measurement> CODEC = Codec.INT.xmap(Measurement::new, measurement -> measurement.millis);
    }
}

