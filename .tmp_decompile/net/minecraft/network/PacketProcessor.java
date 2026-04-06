/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.slf4j.Logger;

public class PacketProcessor
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<ListenerAndPacket<?>> packetsToBeHandled = Queues.newConcurrentLinkedQueue();
    private final Thread runningThread;
    private boolean closed;

    public PacketProcessor(Thread thread) {
        this.runningThread = thread;
    }

    public boolean isSameThread() {
        return Thread.currentThread() == this.runningThread;
    }

    public <T extends PacketListener> void scheduleIfPossible(T packetListener, Packet<T> packet) {
        if (this.closed) {
            throw new RejectedExecutionException("Server already shutting down");
        }
        this.packetsToBeHandled.add(new ListenerAndPacket<T>(packetListener, packet));
    }

    public void processQueuedPackets() {
        if (!this.closed) {
            while (!this.packetsToBeHandled.isEmpty()) {
                this.packetsToBeHandled.poll().handle();
            }
        }
    }

    @Override
    public void close() {
        this.closed = true;
    }

    record ListenerAndPacket<T extends PacketListener>(T listener, Packet<T> packet) {
        public void handle() {
            if (this.listener.shouldHandleMessage(this.packet)) {
                try {
                    this.packet.handle(this.listener);
                }
                catch (Exception exception) {
                    ReportedException reportedException;
                    if (exception instanceof ReportedException && (reportedException = (ReportedException)exception).getCause() instanceof OutOfMemoryError) {
                        throw PacketUtils.makeReportedException(exception, this.packet, this.listener);
                    }
                    this.listener.onPacketError(this.packet, exception);
                }
            } else {
                LOGGER.debug("Ignoring packet due to disconnection: {}", this.packet);
            }
        }
    }
}

