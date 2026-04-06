/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskScheduler<R extends Runnable>
extends AutoCloseable {
    public String name();

    public void schedule(R var1);

    @Override
    default public void close() {
    }

    public R wrapRunnable(Runnable var1);

    default public <Source> CompletableFuture<Source> scheduleWithResult(Consumer<CompletableFuture<Source>> consumer) {
        CompletableFuture completableFuture = new CompletableFuture();
        this.schedule(this.wrapRunnable(() -> consumer.accept(completableFuture)));
        return completableFuture;
    }

    public static TaskScheduler<Runnable> wrapExecutor(final String string, final Executor executor) {
        return new TaskScheduler<Runnable>(){

            @Override
            public String name() {
                return string;
            }

            @Override
            public void schedule(Runnable runnable) {
                executor.execute(runnable);
            }

            @Override
            public Runnable wrapRunnable(Runnable runnable) {
                return runnable;
            }

            public String toString() {
                return string;
            }
        };
    }
}

