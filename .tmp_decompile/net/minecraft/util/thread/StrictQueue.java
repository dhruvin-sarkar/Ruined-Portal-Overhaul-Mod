/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

public interface StrictQueue<T extends Runnable> {
    public @Nullable Runnable pop();

    public boolean push(T var1);

    public boolean isEmpty();

    public int size();

    public static final class FixedPriorityQueue
    implements StrictQueue<RunnableWithPriority> {
        private final Queue<Runnable>[] queues;
        private final AtomicInteger size = new AtomicInteger();

        public FixedPriorityQueue(int i) {
            this.queues = new Queue[i];
            for (int j = 0; j < i; ++j) {
                this.queues[j] = Queues.newConcurrentLinkedQueue();
            }
        }

        @Override
        public @Nullable Runnable pop() {
            for (Queue<Runnable> queue : this.queues) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                this.size.decrementAndGet();
                return runnable;
            }
            return null;
        }

        @Override
        public boolean push(RunnableWithPriority runnableWithPriority) {
            int i = runnableWithPriority.priority;
            if (i >= this.queues.length || i < 0) {
                throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", i, this.queues.length - 1));
            }
            this.queues[i].add(runnableWithPriority);
            this.size.incrementAndGet();
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.size.get() == 0;
        }

        @Override
        public int size() {
            return this.size.get();
        }
    }

    public static final class RunnableWithPriority
    extends Record
    implements Runnable {
        final int priority;
        private final Runnable task;

        public RunnableWithPriority(int i, Runnable runnable) {
            this.priority = i;
            this.task = runnable;
        }

        @Override
        public void run() {
            this.task.run();
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RunnableWithPriority.class, "priority;task", "priority", "task"}, this, object);
        }

        public int priority() {
            return this.priority;
        }

        public Runnable task() {
            return this.task;
        }
    }

    public static final class QueueStrictQueue
    implements StrictQueue<Runnable> {
        private final Queue<Runnable> queue;

        public QueueStrictQueue(Queue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public @Nullable Runnable pop() {
            return this.queue.poll();
        }

        @Override
        public boolean push(Runnable runnable) {
            return this.queue.add(runnable);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public int size() {
            return this.queue.size();
        }
    }
}

