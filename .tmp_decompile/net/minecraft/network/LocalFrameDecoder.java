/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.HiddenByteBuf;

public class LocalFrameDecoder
extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        channelHandlerContext.fireChannelRead(HiddenByteBuf.unpack(object));
    }
}

