/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.telemetry;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryPropertyMap;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface TelemetryEventSender {
    public static final TelemetryEventSender DISABLED = (telemetryEventType, consumer) -> {};

    default public TelemetryEventSender decorate(Consumer<TelemetryPropertyMap.Builder> consumer) {
        return (telemetryEventType, consumer2) -> this.send(telemetryEventType, builder -> {
            consumer2.accept(builder);
            consumer.accept((TelemetryPropertyMap.Builder)builder);
        });
    }

    public void send(TelemetryEventType var1, Consumer<TelemetryPropertyMap.Builder> var2);
}

