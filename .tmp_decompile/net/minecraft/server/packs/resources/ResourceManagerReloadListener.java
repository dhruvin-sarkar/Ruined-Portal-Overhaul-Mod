/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ResourceManagerReloadListener
extends PreparableReloadListener {
    @Override
    default public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        ResourceManager resourceManager = sharedState.resourceManager();
        return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("listener");
            this.onResourceManagerReload(resourceManager);
            profilerFiller.pop();
        }, executor2);
    }

    public void onResourceManagerReload(ResourceManager var1);
}

