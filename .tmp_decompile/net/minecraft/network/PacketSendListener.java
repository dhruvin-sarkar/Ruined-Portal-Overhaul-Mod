/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.channel.ChannelFutureListener
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PacketSendListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ChannelFutureListener thenRun(Runnable runnable) {
        return channelFuture -> {
            runnable.run();
            if (!channelFuture.isSuccess()) {
                channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
            }
        };
    }

    public static ChannelFutureListener exceptionallySend(Supplier<@Nullable Packet<?>> supplier) {
        return channelFuture -> {
            if (!channelFuture.isSuccess()) {
                Packet packet = (Packet)supplier.get();
                if (packet != null) {
                    LOGGER.warn("Failed to deliver packet, sending fallback {}", packet.type(), (Object)channelFuture.cause());
                    channelFuture.channel().writeAndFlush((Object)packet, channelFuture.channel().voidPromise());
                } else {
                    channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
                }
            }
        };
    }
}

