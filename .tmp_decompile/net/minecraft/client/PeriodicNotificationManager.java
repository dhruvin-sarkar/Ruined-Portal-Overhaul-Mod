/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.math.LongMath
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2BooleanFunction
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PeriodicNotificationManager
extends SimplePreparableReloadListener<Map<String, List<Notification>>>
implements AutoCloseable {
    private static final Codec<Map<String, List<Notification>>> CODEC = Codec.unboundedMap((Codec)Codec.STRING, (Codec)RecordCodecBuilder.create(instance -> instance.group((App)Codec.LONG.optionalFieldOf("delay", (Object)0L).forGetter(Notification::delay), (App)Codec.LONG.fieldOf("period").forGetter(Notification::period), (App)Codec.STRING.fieldOf("title").forGetter(Notification::title), (App)Codec.STRING.fieldOf("message").forGetter(Notification::message)).apply((Applicative)instance, Notification::new)).listOf());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier notifications;
    private final Object2BooleanFunction<String> selector;
    private @Nullable Timer timer;
    private @Nullable NotificationTask notificationTask;

    public PeriodicNotificationManager(Identifier identifier, Object2BooleanFunction<String> object2BooleanFunction) {
        this.notifications = identifier;
        this.selector = object2BooleanFunction;
    }

    @Override
    protected Map<String, List<Notification>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map map;
        block8: {
            BufferedReader reader = resourceManager.openAsReader(this.notifications);
            try {
                map = (Map)CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)StrictJsonParser.parse(reader)).result().orElseThrow();
                if (reader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            ((Reader)reader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to load {}", (Object)this.notifications, (Object)exception);
                    return ImmutableMap.of();
                }
            }
            ((Reader)reader).close();
        }
        return map;
    }

    @Override
    protected void apply(Map<String, List<Notification>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<Notification> list = map.entrySet().stream().filter(entry -> (Boolean)this.selector.apply((Object)((String)entry.getKey()))).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toList());
        if (list.isEmpty()) {
            this.stopTimer();
            return;
        }
        if (list.stream().anyMatch(notification -> notification.period == 0L)) {
            Util.logAndPauseIfInIde("A periodic notification in " + String.valueOf(this.notifications) + " has a period of zero minutes");
            this.stopTimer();
            return;
        }
        long l = this.calculateInitialDelay(list);
        long m = this.calculateOptimalPeriod(list, l);
        if (this.timer == null) {
            this.timer = new Timer();
        }
        this.notificationTask = this.notificationTask == null ? new NotificationTask(list, l, m) : this.notificationTask.reset(list, m);
        this.timer.scheduleAtFixedRate((TimerTask)this.notificationTask, TimeUnit.MINUTES.toMillis(l), TimeUnit.MINUTES.toMillis(m));
    }

    @Override
    public void close() {
        this.stopTimer();
    }

    private void stopTimer() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    private long calculateOptimalPeriod(List<Notification> list, long l) {
        return list.stream().mapToLong(notification -> {
            long m = notification.delay - l;
            return LongMath.gcd((long)m, (long)notification.period);
        }).reduce(LongMath::gcd).orElseThrow(() -> new IllegalStateException("Empty notifications from: " + String.valueOf(this.notifications)));
    }

    private long calculateInitialDelay(List<Notification> list) {
        return list.stream().mapToLong(notification -> notification.delay).min().orElse(0L);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    static class NotificationTask
    extends TimerTask {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final List<Notification> notifications;
        private final long period;
        private final AtomicLong elapsed;

        public NotificationTask(List<Notification> list, long l, long m) {
            this.notifications = list;
            this.period = m;
            this.elapsed = new AtomicLong(l);
        }

        public NotificationTask reset(List<Notification> list, long l) {
            this.cancel();
            return new NotificationTask(list, this.elapsed.get(), l);
        }

        @Override
        public void run() {
            long l = this.elapsed.getAndAdd(this.period);
            long m = this.elapsed.get();
            for (Notification notification : this.notifications) {
                long o;
                long n;
                if (l < notification.delay || (n = l / notification.period) == (o = m / notification.period)) continue;
                this.minecraft.execute(() -> SystemToast.add(Minecraft.getInstance().getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable(notification.title, n), Component.translatable(notification.message, n)));
                return;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Notification
    extends Record {
        final long delay;
        final long period;
        final String title;
        final String message;

        public Notification(long l, long m, String string, String string2) {
            this.delay = l != 0L ? l : m;
            this.period = m;
            this.title = string;
            this.message = string2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Notification.class, "delay;period;title;message", "delay", "period", "title", "message"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Notification.class, "delay;period;title;message", "delay", "period", "title", "message"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Notification.class, "delay;period;title;message", "delay", "period", "title", "message"}, this, object);
        }

        public long delay() {
            return this.delay;
        }

        public long period() {
            return this.period;
        }

        public String title() {
            return this.title;
        }

        public String message() {
            return this.message;
        }
    }
}

