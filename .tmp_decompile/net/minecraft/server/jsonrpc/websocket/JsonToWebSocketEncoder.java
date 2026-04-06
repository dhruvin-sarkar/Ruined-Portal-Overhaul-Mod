/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToMessageEncoder
 *  io.netty.handler.codec.http.websocketx.TextWebSocketFrame
 */
package net.minecraft.server.jsonrpc.websocket;

import com.google.gson.JsonElement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class JsonToWebSocketEncoder
extends MessageToMessageEncoder<JsonElement> {
    protected void encode(ChannelHandlerContext channelHandlerContext, JsonElement jsonElement, List<Object> list) {
        list.add(new TextWebSocketFrame(jsonElement.toString()));
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, List list) throws Exception {
        this.encode(channelHandlerContext, (JsonElement)object, (List<Object>)list);
    }
}

