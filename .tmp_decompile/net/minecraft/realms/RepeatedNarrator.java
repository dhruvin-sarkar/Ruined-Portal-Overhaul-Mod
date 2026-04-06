/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.RateLimiter
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<@Nullable Params> params = new AtomicReference();

    public RepeatedNarrator(Duration duration) {
        this.permitsPerSecond = 1000.0f / (float)duration.toMillis();
    }

    public void narrate(GameNarrator gameNarrator, Component component) {
        Params params2 = this.params.updateAndGet(params -> {
            if (params == null || !component.equals(params.narration)) {
                return new Params(component, RateLimiter.create((double)this.permitsPerSecond));
            }
            return params;
        });
        if (params2.rateLimiter.tryAcquire(1)) {
            gameNarrator.saySystemNow(component);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Params {
        final Component narration;
        final RateLimiter rateLimiter;

        Params(Component component, RateLimiter rateLimiter) {
            this.narration = component;
            this.rateLimiter = rateLimiter;
        }
    }
}

