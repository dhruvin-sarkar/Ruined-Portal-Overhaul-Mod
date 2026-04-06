/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.TaskChainer;
import org.slf4j.Logger;

public class FutureChain
implements TaskChainer,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
    private final Executor executor;
    private volatile boolean closed;

    public FutureChain(Executor executor) {
        this.executor = executor;
    }

    @Override
    public <T> void append(CompletableFuture<T> completableFuture, Consumer<T> consumer) {
        this.head = ((CompletableFuture)((CompletableFuture)this.head.thenCombine(completableFuture, (object, object2) -> object2)).thenAcceptAsync(object -> {
            if (!this.closed) {
                consumer.accept(object);
            }
        }, this.executor)).exceptionally(throwable -> {
            if (throwable instanceof CompletionException) {
                CompletionException completionException = (CompletionException)throwable;
                throwable = completionException.getCause();
            }
            if (throwable instanceof CancellationException) {
                CancellationException cancellationException = (CancellationException)throwable;
                throw cancellationException;
            }
            LOGGER.error("Chain link failed, continuing to next one", throwable);
            return null;
        });
    }

    @Override
    public void close() {
        this.closed = true;
    }
}

