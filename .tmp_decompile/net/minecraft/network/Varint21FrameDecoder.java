/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.CorruptedFrameException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.VarInt;
import org.jspecify.annotations.Nullable;

public class Varint21FrameDecoder
extends ByteToMessageDecoder {
    private static final int MAX_VARINT21_BYTES = 3;
    private final ByteBuf helperBuf = Unpooled.directBuffer((int)3);
    private final @Nullable BandwidthDebugMonitor monitor;

    public Varint21FrameDecoder(@Nullable BandwidthDebugMonitor bandwidthDebugMonitor) {
        this.monitor = bandwidthDebugMonitor;
    }

    protected void handlerRemoved0(ChannelHandlerContext channelHandlerContext) {
        this.helperBuf.release();
    }

    private static boolean copyVarint(ByteBuf byteBuf, ByteBuf byteBuf2) {
        for (int i = 0; i < 3; ++i) {
            if (!byteBuf.isReadable()) {
                return false;
            }
            byte b = byteBuf.readByte();
            byteBuf2.writeByte((int)b);
            if (VarInt.hasContinuationBit(b)) continue;
            return true;
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        byteBuf.markReaderIndex();
        this.helperBuf.clear();
        if (!Varint21FrameDecoder.copyVarint(byteBuf, this.helperBuf)) {
            byteBuf.resetReaderIndex();
            return;
        }
        int i = VarInt.read(this.helperBuf);
        if (i == 0) {
            throw new CorruptedFrameException("Frame length cannot be zero");
        }
        if (byteBuf.readableBytes() < i) {
            byteBuf.resetReaderIndex();
            return;
        }
        if (this.monitor != null) {
            this.monitor.onReceive(i + VarInt.getByteSize(i));
        }
        list.add(byteBuf.readBytes(i));
    }
}

