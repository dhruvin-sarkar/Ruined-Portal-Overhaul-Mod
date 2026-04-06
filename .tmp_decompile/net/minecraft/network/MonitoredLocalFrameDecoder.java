/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.HiddenByteBuf;

public class MonitoredLocalFrameDecoder
extends ChannelInboundHandlerAdapter {
    private final BandwidthDebugMonitor monitor;

    public MonitoredLocalFrameDecoder(BandwidthDebugMonitor bandwidthDebugMonitor) {
        this.monitor = bandwidthDebugMonitor;
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        if ((object = HiddenByteBuf.unpack(object)) instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf)object;
            this.monitor.onReceive(byteBuf.readableBytes());
        }
        channelHandlerContext.fireChannelRead(object);
    }
}

