/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 */
package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.SharedConstants;

public record TracingExecutor(ExecutorService service) implements Executor
{
    public Executor forName(String string) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return runnable -> this.service.execute(() -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try (Zone zone = TracyClient.beginZone((String)string, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    runnable.run();
                }
                finally {
                    thread.setName(string2);
                }
            });
        }
        if (TracyClient.isAvailable()) {
            return runnable -> this.service.execute(() -> {
                try (Zone zone = TracyClient.beginZone((String)string, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    runnable.run();
                }
            });
        }
        return this.service;
    }

    @Override
    public void execute(Runnable runnable) {
        this.service.execute(TracingExecutor.wrapUnnamed(runnable));
    }

    public void shutdownAndAwait(long l, TimeUnit timeUnit) {
        boolean bl;
        this.service.shutdown();
        try {
            bl = this.service.awaitTermination(l, timeUnit);
        }
        catch (InterruptedException interruptedException) {
            bl = false;
        }
        if (!bl) {
            this.service.shutdownNow();
        }
    }

    private static Runnable wrapUnnamed(Runnable runnable) {
        if (!TracyClient.isAvailable()) {
            return runnable;
        }
        return () -> {
            try (Zone zone = TracyClient.beginZone((String)"task", (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                runnable.run();
            }
        };
    }
}

