/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T>
implements PreparableReloadListener {
    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        ResourceManager resourceManager = sharedState.resourceManager();
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, Profiler.get()), executor).thenCompose(preparationBarrier::wait)).thenAcceptAsync(object -> this.apply(object, resourceManager, Profiler.get()), executor2);
    }

    protected abstract T prepare(ResourceManager var1, ProfilerFiller var2);

    protected abstract void apply(T var1, ResourceManager var2, ProfilerFiller var3);
}

