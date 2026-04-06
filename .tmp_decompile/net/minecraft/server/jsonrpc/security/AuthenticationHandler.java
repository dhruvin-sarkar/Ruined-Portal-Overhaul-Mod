/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelDuplexHandler
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelPromise
 *  io.netty.handler.codec.http.DefaultFullHttpResponse
 *  io.netty.handler.codec.http.HttpHeaderNames
 *  io.netty.handler.codec.http.HttpRequest
 *  io.netty.handler.codec.http.HttpResponse
 *  io.netty.handler.codec.http.HttpResponseStatus
 *  io.netty.handler.codec.http.HttpVersion
 *  io.netty.util.AttributeKey
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.jsonrpc.security;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import net.minecraft.server.jsonrpc.security.SecurityConfig;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@ChannelHandler.Sharable
public class AuthenticationHandler
extends ChannelDuplexHandler {
    private final Logger LOGGER = LogUtils.getLogger();
    private static final AttributeKey<Boolean> AUTHENTICATED_KEY = AttributeKey.valueOf((String)"authenticated");
    private static final AttributeKey<Boolean> ATTR_WEBSOCKET_ALLOWED = AttributeKey.valueOf((String)"websocket_auth_allowed");
    private static final String SUBPROTOCOL_VALUE = "minecraft-v1";
    private static final String SUBPROTOCOL_HEADER_PREFIX = "minecraft-v1,";
    public static final String BEARER_PREFIX = "Bearer ";
    private final SecurityConfig securityConfig;
    private final Set<String> allowedOrigins;

    public AuthenticationHandler(SecurityConfig securityConfig, String string) {
        this.securityConfig = securityConfig;
        this.allowedOrigins = Sets.newHashSet((Object[])string.split(","));
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        Boolean boolean_;
        String string = this.getClientIp(channelHandlerContext);
        if (object instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest)object;
            SecurityCheckResult securityCheckResult = this.performSecurityChecks(httpRequest);
            if (securityCheckResult.isAllowed()) {
                channelHandlerContext.channel().attr(AUTHENTICATED_KEY).set((Object)true);
                if (securityCheckResult.isTokenSentInSecWebsocketProtocol()) {
                    channelHandlerContext.channel().attr(ATTR_WEBSOCKET_ALLOWED).set((Object)Boolean.TRUE);
                }
            } else {
                this.LOGGER.debug("Authentication rejected for connection with ip {}: {}", (Object)string, (Object)securityCheckResult.getReason());
                channelHandlerContext.channel().attr(AUTHENTICATED_KEY).set((Object)false);
                this.sendUnauthorizedResponse(channelHandlerContext, securityCheckResult.getReason());
                return;
            }
        }
        if (Boolean.TRUE.equals(boolean_ = (Boolean)channelHandlerContext.channel().attr(AUTHENTICATED_KEY).get())) {
            super.channelRead(channelHandlerContext, object);
        } else {
            this.LOGGER.debug("Dropping unauthenticated connection with ip {}", (Object)string);
            channelHandlerContext.close();
        }
    }

    public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
        HttpResponse httpResponse;
        if (object instanceof HttpResponse && (httpResponse = (HttpResponse)object).status().code() == HttpResponseStatus.SWITCHING_PROTOCOLS.code() && channelHandlerContext.channel().attr(ATTR_WEBSOCKET_ALLOWED).get() != null && ((Boolean)channelHandlerContext.channel().attr(ATTR_WEBSOCKET_ALLOWED).get()).equals(Boolean.TRUE)) {
            httpResponse.headers().set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, (Object)SUBPROTOCOL_VALUE);
        }
        super.write(channelHandlerContext, object, channelPromise);
    }

    private SecurityCheckResult performSecurityChecks(HttpRequest httpRequest) {
        String string = this.parseTokenInAuthorizationHeader(httpRequest);
        if (string != null) {
            if (this.isValidApiKey(string)) {
                return SecurityCheckResult.allowed();
            }
            return SecurityCheckResult.denied("Invalid API key");
        }
        String string2 = this.parseTokenInSecWebsocketProtocolHeader(httpRequest);
        if (string2 != null) {
            if (!this.isAllowedOriginHeader(httpRequest)) {
                return SecurityCheckResult.denied("Origin Not Allowed");
            }
            if (this.isValidApiKey(string2)) {
                return SecurityCheckResult.allowed(true);
            }
            return SecurityCheckResult.denied("Invalid API key");
        }
        return SecurityCheckResult.denied("Missing API key");
    }

    private boolean isAllowedOriginHeader(HttpRequest httpRequest) {
        String string = httpRequest.headers().get((CharSequence)HttpHeaderNames.ORIGIN);
        if (string == null || string.isEmpty()) {
            return false;
        }
        return this.allowedOrigins.contains(string);
    }

    private @Nullable String parseTokenInAuthorizationHeader(HttpRequest httpRequest) {
        String string = httpRequest.headers().get((CharSequence)HttpHeaderNames.AUTHORIZATION);
        if (string != null && string.startsWith(BEARER_PREFIX)) {
            return string.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    private @Nullable String parseTokenInSecWebsocketProtocolHeader(HttpRequest httpRequest) {
        String string = httpRequest.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        if (string != null && string.startsWith(SUBPROTOCOL_HEADER_PREFIX)) {
            return string.substring(SUBPROTOCOL_HEADER_PREFIX.length()).trim();
        }
        return null;
    }

    public boolean isValidApiKey(String string) {
        if (string.isEmpty()) {
            return false;
        }
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        byte[] cs = this.securityConfig.secretKey().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bs, cs);
    }

    private String getClientIp(ChannelHandlerContext channelHandlerContext) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }

    private void sendUnauthorizedResponse(ChannelHandlerContext channelHandlerContext, String string) {
        String string2 = "{\"error\":\"Unauthorized\",\"message\":\"" + string + "\"}";
        byte[] bs = string2.getBytes(StandardCharsets.UTF_8);
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.wrappedBuffer((byte[])bs));
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_TYPE, (Object)"application/json");
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (Object)bs.length);
        defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.CONNECTION, (Object)"close");
        channelHandlerContext.writeAndFlush((Object)defaultFullHttpResponse).addListener(future -> channelHandlerContext.close());
    }

    static class SecurityCheckResult {
        private final boolean allowed;
        private final String reason;
        private final boolean tokenSentInSecWebsocketProtocol;

        private SecurityCheckResult(boolean bl, String string, boolean bl2) {
            this.allowed = bl;
            this.reason = string;
            this.tokenSentInSecWebsocketProtocol = bl2;
        }

        public static SecurityCheckResult allowed() {
            return new SecurityCheckResult(true, null, false);
        }

        public static SecurityCheckResult allowed(boolean bl) {
            return new SecurityCheckResult(true, null, bl);
        }

        public static SecurityCheckResult denied(String string) {
            return new SecurityCheckResult(false, string, false);
        }

        public boolean isAllowed() {
            return this.allowed;
        }

        public String getReason() {
            return this.reason;
        }

        public boolean isTokenSentInSecWebsocketProtocol() {
            return this.tokenSentInSecWebsocketProtocol;
        }
    }
}

