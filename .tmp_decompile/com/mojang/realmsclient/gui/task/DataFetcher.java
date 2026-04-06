/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.task.RepeatedDelayStrategy;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TimeSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DataFetcher {
    static final Logger LOGGER = LogUtils.getLogger();
    final Executor executor;
    final TimeUnit resolution;
    final TimeSource timeSource;

    public DataFetcher(Executor executor, TimeUnit timeUnit, TimeSource timeSource) {
        this.executor = executor;
        this.resolution = timeUnit;
        this.timeSource = timeSource;
    }

    public <T> Task<T> createTask(String string, Callable<T> callable, Duration duration, RepeatedDelayStrategy repeatedDelayStrategy) {
        long l = this.resolution.convert(duration);
        if (l == 0L) {
            throw new IllegalArgumentException("Period of " + String.valueOf(duration) + " too short for selected resolution of " + String.valueOf((Object)this.resolution));
        }
        return new Task<T>(string, callable, l, repeatedDelayStrategy);
    }

    public Subscription createSubscription() {
        return new Subscription();
    }

    @Environment(value=EnvType.CLIENT)
    public class Task<T> {
        private final String id;
        private final Callable<T> updater;
        private final long period;
        private final RepeatedDelayStrategy repeatStrategy;
        private @Nullable CompletableFuture<ComputationResult<T>> pendingTask;
        @Nullable SuccessfulComputationResult<T> lastResult;
        private long nextUpdate = -1L;

        Task(String string, Callable<T> callable, long l, RepeatedDelayStrategy repeatedDelayStrategy) {
            this.id = string;
            this.updater = callable;
            this.period = l;
            this.repeatStrategy = repeatedDelayStrategy;
        }

        void updateIfNeeded(long l) {
            if (this.pendingTask != null) {
                ComputationResult computationResult = this.pendingTask.getNow(null);
                if (computationResult == null) {
                    return;
                }
                this.pendingTask = null;
                long m = computationResult.time;
                computationResult.value().ifLeft(object -> {
                    this.lastResult = new SuccessfulComputationResult<Object>(object, m);
                    this.nextUpdate = m + this.period * this.repeatStrategy.delayCyclesAfterSuccess();
                }).ifRight(exception -> {
                    long m = this.repeatStrategy.delayCyclesAfterFailure();
                    LOGGER.warn("Failed to process task {}, will repeat after {} cycles", new Object[]{this.id, m, exception});
                    this.nextUpdate = m + this.period * m;
                });
            }
            if (this.nextUpdate <= l) {
                this.pendingTask = CompletableFuture.supplyAsync(() -> {
                    try {
                        T object = this.updater.call();
                        long l = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new ComputationResult(Either.left(object), l);
                    }
                    catch (Exception exception) {
                        long l = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new ComputationResult(Either.right((Object)exception), l);
                    }
                }, DataFetcher.this.executor);
            }
        }

        public void reset() {
            this.pendingTask = null;
            this.lastResult = null;
            this.nextUpdate = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class Subscription {
        private final List<SubscribedTask<?>> subscriptions = new ArrayList();

        public <T> void subscribe(Task<T> task, Consumer<T> consumer) {
            SubscribedTask<T> subscribedTask = new SubscribedTask<T>(DataFetcher.this, task, consumer);
            this.subscriptions.add(subscribedTask);
            subscribedTask.runCallbackIfNeeded();
        }

        public void forceUpdate() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.runCallback();
            }
        }

        public void tick() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.update(DataFetcher.this.timeSource.get(DataFetcher.this.resolution));
            }
        }

        public void reset() {
            for (SubscribedTask<?> subscribedTask : this.subscriptions) {
                subscribedTask.reset();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class SubscribedTask<T> {
        private final Task<T> task;
        private final Consumer<T> output;
        private long lastCheckTime = -1L;

        SubscribedTask(DataFetcher dataFetcher, Task<T> task, Consumer<T> consumer) {
            this.task = task;
            this.output = consumer;
        }

        void update(long l) {
            this.task.updateIfNeeded(l);
            this.runCallbackIfNeeded();
        }

        void runCallbackIfNeeded() {
            SuccessfulComputationResult successfulComputationResult = this.task.lastResult;
            if (successfulComputationResult != null && this.lastCheckTime < successfulComputationResult.time) {
                this.output.accept(successfulComputationResult.value);
                this.lastCheckTime = successfulComputationResult.time;
            }
        }

        void runCallback() {
            SuccessfulComputationResult successfulComputationResult = this.task.lastResult;
            if (successfulComputationResult != null) {
                this.output.accept(successfulComputationResult.value);
                this.lastCheckTime = successfulComputationResult.time;
            }
        }

        void reset() {
            this.task.reset();
            this.lastCheckTime = -1L;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class SuccessfulComputationResult<T>
    extends Record {
        final T value;
        final long time;

        SuccessfulComputationResult(T object, long l) {
            this.value = object;
            this.time = l;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SuccessfulComputationResult.class, "value;time", "value", "time"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SuccessfulComputationResult.class, "value;time", "value", "time"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SuccessfulComputationResult.class, "value;time", "value", "time"}, this, object);
        }

        public T value() {
            return this.value;
        }

        public long time() {
            return this.time;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ComputationResult<T>
    extends Record {
        private final Either<T, Exception> value;
        final long time;

        ComputationResult(Either<T, Exception> either, long l) {
            this.value = either;
            this.time = l;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ComputationResult.class, "value;time", "value", "time"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ComputationResult.class, "value;time", "value", "time"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ComputationResult.class, "value;time", "value", "time"}, this, object);
        }

        public Either<T, Exception> value() {
            return this.value;
        }

        public long time() {
            return this.time;
        }
    }
}

