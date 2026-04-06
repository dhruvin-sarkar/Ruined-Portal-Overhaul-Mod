/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sounds;

import java.util.concurrent.locks.LockSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;

@Environment(value=EnvType.CLIENT)
public class SoundEngineExecutor
extends BlockableEventLoop<Runnable> {
    private Thread thread = this.createThread();
    private volatile boolean shutdown;

    public SoundEngineExecutor() {
        super("Sound executor");
    }

    private Thread createThread() {
        Thread thread2 = new Thread(this::run);
        thread2.setDaemon(true);
        thread2.setName("Sound engine");
        thread2.setUncaughtExceptionHandler((thread, throwable) -> Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Uncaught exception on thread: " + thread.getName())));
        thread2.start();
        return thread2;
    }

    @Override
    public Runnable wrapRunnable(Runnable runnable) {
        return runnable;
    }

    @Override
    public void schedule(Runnable runnable) {
        if (!this.shutdown) {
            super.schedule(runnable);
        }
    }

    @Override
    protected boolean shouldRun(Runnable runnable) {
        return !this.shutdown;
    }

    @Override
    protected Thread getRunningThread() {
        return this.thread;
    }

    private void run() {
        while (!this.shutdown) {
            this.managedBlock(() -> this.shutdown);
        }
    }

    @Override
    protected void waitForTasks() {
        LockSupport.park("waiting for tasks");
    }

    public void shutDown() {
        this.shutdown = true;
        this.dropAllTasks();
        this.thread.interrupt();
        try {
            this.thread.join();
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public void startUp() {
        this.shutdown = false;
        this.thread = this.createThread();
    }
}

