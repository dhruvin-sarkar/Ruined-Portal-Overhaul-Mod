/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.local.LocalAddress
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  io.netty.util.HashedWheelTimer
 *  io.netty.util.Timeout
 *  io.netty.util.Timer
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConnectionListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    final MinecraftServer server;
    public volatile boolean running;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
    final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public ServerConnectionListener(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
        this.running = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startTcpServerListener(@Nullable InetAddress inetAddress, int i) throws IOException {
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            EventLoopGroupHolder eventLoopGroupHolder = EventLoopGroupHolder.remote(this.server.useNativeTransport());
            this.channels.add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(eventLoopGroupHolder.serverChannelCls())).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                    }
                    catch (ChannelException channelException) {
                        // empty catch block
                    }
                    ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                    if (ServerConnectionListener.this.server.repliesToStatus()) {
                        channelPipeline.addLast("legacy_query", (ChannelHandler)new LegacyQueryHandler(ServerConnectionListener.this.getServer()));
                    }
                    Connection.configureSerialization(channelPipeline, PacketFlow.SERVERBOUND, false, null);
                    int i = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
                    Connection connection = i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND);
                    ServerConnectionListener.this.connections.add(connection);
                    connection.configurePacketHandler(channelPipeline);
                    connection.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
                }
            }).group(eventLoopGroupHolder.eventLoopGroup()).localAddress(inetAddress, i)).bind().syncUninterruptibly());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SocketAddress startMemoryChannel() {
        ChannelFuture channelFuture;
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            channelFuture = ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(EventLoopGroupHolder.local().serverChannelCls())).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

                protected void initChannel(Channel channel) {
                    Connection connection = new Connection(PacketFlow.SERVERBOUND);
                    connection.setListenerForServerboundHandshake(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
                    ServerConnectionListener.this.connections.add(connection);
                    ChannelPipeline channelPipeline = channel.pipeline();
                    Connection.configureInMemoryPipeline(channelPipeline, PacketFlow.SERVERBOUND);
                    if (SharedConstants.DEBUG_FAKE_LATENCY_MS > 0) {
                        channelPipeline.addLast("latency", (ChannelHandler)new LatencySimulator(SharedConstants.DEBUG_FAKE_LATENCY_MS, SharedConstants.DEBUG_FAKE_JITTER_MS));
                    }
                    connection.configurePacketHandler(channelPipeline);
                }
            }).group(EventLoopGroupHolder.local().eventLoopGroup()).localAddress((SocketAddress)LocalAddress.ANY)).bind().syncUninterruptibly();
            this.channels.add(channelFuture);
        }
        return channelFuture.channel().localAddress();
    }

    public void stop() {
        this.running = false;
        for (ChannelFuture channelFuture : this.channels) {
            try {
                channelFuture.channel().close().sync();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnecting()) continue;
                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    }
                    catch (Exception exception) {
                        if (connection.isMemoryConnection()) {
                            throw new ReportedException(CrashReport.forThrowable(exception, "Ticking memory connection"));
                        }
                        LOGGER.warn("Failed to handle packet for {}", (Object)connection.getLoggableAddress(this.server.logIPs()), (Object)exception);
                        MutableComponent component = Component.literal("Internal server error");
                        connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> connection.disconnect(component)));
                        connection.setReadOnly();
                    }
                    continue;
                }
                iterator.remove();
                connection.handleDisconnection();
            }
        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    static class LatencySimulator
    extends ChannelInboundHandlerAdapter {
        private static final Timer TIMER = new HashedWheelTimer();
        private final int delay;
        private final int jitter;
        private final List<DelayedMessage> queuedMessages = Lists.newArrayList();

        public LatencySimulator(int i, int j) {
            this.delay = i;
            this.jitter = j;
        }

        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
            this.delayDownstream(channelHandlerContext, object);
        }

        private void delayDownstream(ChannelHandlerContext channelHandlerContext, Object object) {
            int i = this.delay + (int)(Math.random() * (double)this.jitter);
            this.queuedMessages.add(new DelayedMessage(channelHandlerContext, object));
            TIMER.newTimeout(this::onTimeout, (long)i, TimeUnit.MILLISECONDS);
        }

        private void onTimeout(Timeout timeout) {
            DelayedMessage delayedMessage = this.queuedMessages.remove(0);
            delayedMessage.ctx.fireChannelRead(delayedMessage.msg);
        }

        static class DelayedMessage {
            public final ChannelHandlerContext ctx;
            public final Object msg;

            public DelayedMessage(ChannelHandlerContext channelHandlerContext, Object object) {
                this.ctx = channelHandlerContext;
                this.msg = object;
            }
        }
    }
}

