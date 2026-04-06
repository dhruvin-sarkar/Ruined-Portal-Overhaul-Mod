/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder<T extends PacketListener>
extends ByteToMessageDecoder
implements ProtocolSwapHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketDecoder(ProtocolInfo<T> protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Packet packet;
        int i = byteBuf.readableBytes();
        try {
            packet = (Packet)this.protocolInfo.codec().decode(byteBuf);
        }
        catch (Exception exception) {
            if (exception instanceof SkipPacketException) {
                byteBuf.skipBytes(byteBuf.readableBytes());
            }
            throw exception;
        }
        PacketType packetType = packet.type();
        JvmProfiler.INSTANCE.onPacketReceived(this.protocolInfo.id(), packetType, channelHandlerContext.channel().remoteAddress(), i);
        if (byteBuf.readableBytes() > 0) {
            throw new IOException("Packet " + this.protocolInfo.id().id() + "/" + String.valueOf(packetType) + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + byteBuf.readableBytes() + " bytes extra whilst reading packet " + String.valueOf(packetType));
        }
        list.add(packet);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), packetType, packet.getClass().getName(), i});
        }
        ProtocolSwapHandler.handleInboundTerminalPacket(channelHandlerContext, packet);
    }
}

