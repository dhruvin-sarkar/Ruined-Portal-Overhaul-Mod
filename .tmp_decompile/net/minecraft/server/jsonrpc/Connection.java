/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.handler.timeout.ReadTimeoutException
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.jsonrpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.JsonRPCErrors;
import net.minecraft.server.jsonrpc.JsonRPCUtils;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.PendingRpcRequest;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidRequestJsonRpcException;
import net.minecraft.server.jsonrpc.methods.MethodNotFoundJsonRpcException;
import net.minecraft.server.jsonrpc.methods.RemoteRpcErrorException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Connection
extends SimpleChannelInboundHandler<JsonElement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger CONNECTION_ID_COUNTER = new AtomicInteger(0);
    private final JsonRpcLogger jsonRpcLogger;
    private final ClientInfo clientInfo;
    private final ManagementServer managementServer;
    private final Channel channel;
    private final MinecraftApi minecraftApi;
    private final AtomicInteger transactionId = new AtomicInteger();
    private final Int2ObjectMap<PendingRpcRequest<?>> pendingRequests = Int2ObjectMaps.synchronize((Int2ObjectMap)new Int2ObjectOpenHashMap());

    public Connection(Channel channel, ManagementServer managementServer, MinecraftApi minecraftApi, JsonRpcLogger jsonRpcLogger) {
        this.clientInfo = ClientInfo.of(CONNECTION_ID_COUNTER.incrementAndGet());
        this.managementServer = managementServer;
        this.minecraftApi = minecraftApi;
        this.channel = channel;
        this.jsonRpcLogger = jsonRpcLogger;
    }

    public void tick() {
        long l = Util.getMillis();
        this.pendingRequests.int2ObjectEntrySet().removeIf(entry -> {
            boolean bl = ((PendingRpcRequest)((Object)((Object)entry.getValue()))).timedOut(l);
            if (bl) {
                ((PendingRpcRequest)((Object)((Object)entry.getValue()))).resultFuture().completeExceptionally((Throwable)new ReadTimeoutException("RPC method " + String.valueOf(((PendingRpcRequest)((Object)((Object)entry.getValue()))).method().key().identifier()) + " timed out waiting for response"));
            }
            return bl;
        });
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        this.jsonRpcLogger.log(this.clientInfo, "Management connection opened for {}", this.channel.remoteAddress());
        super.channelActive(channelHandlerContext);
        this.managementServer.onConnected(this);
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        this.jsonRpcLogger.log(this.clientInfo, "Management connection closed for {}", this.channel.remoteAddress());
        super.channelInactive(channelHandlerContext);
        this.managementServer.onDisconnected(this);
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        if (throwable.getCause() instanceof JsonParseException) {
            this.channel.writeAndFlush((Object)JsonRPCErrors.PARSE_ERROR.createWithUnknownId(throwable.getMessage()));
            return;
        }
        super.exceptionCaught(channelHandlerContext, throwable);
        this.channel.close().awaitUninterruptibly();
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = this.handleJsonObject(jsonElement.getAsJsonObject());
            if (jsonObject != null) {
                this.channel.writeAndFlush((Object)jsonObject);
            }
        } else if (jsonElement.isJsonArray()) {
            this.channel.writeAndFlush((Object)this.handleBatchRequest(jsonElement.getAsJsonArray().asList()));
        } else {
            this.channel.writeAndFlush((Object)JsonRPCErrors.INVALID_REQUEST.createWithUnknownId(null));
        }
    }

    private JsonArray handleBatchRequest(List<JsonElement> list) {
        JsonArray jsonArray = new JsonArray();
        list.stream().map(jsonElement -> this.handleJsonObject(jsonElement.getAsJsonObject())).filter(Objects::nonNull).forEach(arg_0 -> ((JsonArray)jsonArray).add(arg_0));
        return jsonArray;
    }

    public void sendNotification(Holder.Reference<? extends OutgoingRpcMethod<Void, ?>> reference) {
        this.sendRequest(reference, null, false);
    }

    public <Params> void sendNotification(Holder.Reference<? extends OutgoingRpcMethod<Params, ?>> reference, Params object) {
        this.sendRequest(reference, object, false);
    }

    public <Result> CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Void, Result>> reference) {
        return this.sendRequest(reference, null, true);
    }

    public <Params, Result> CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Params, Result>> reference, Params object) {
        return this.sendRequest(reference, object, true);
    }

    @Contract(value="_,_,false->null;_,_,true->!null")
    private <Params, Result> @Nullable CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Params, ? extends Result>> reference, @Nullable Params object, boolean bl) {
        List list;
        List list2 = list = object != null ? List.of((Object)Objects.requireNonNull(reference.value().encodeParams(object))) : List.of();
        if (bl) {
            CompletableFuture completableFuture = new CompletableFuture();
            int i = this.transactionId.incrementAndGet();
            long l = Util.timeSource.get(TimeUnit.MILLISECONDS);
            this.pendingRequests.put(i, new PendingRpcRequest(reference, completableFuture, l + 5000L));
            this.channel.writeAndFlush((Object)JsonRPCUtils.createRequest(i, reference.key().identifier(), list));
            return completableFuture;
        }
        this.channel.writeAndFlush((Object)JsonRPCUtils.createRequest(null, reference.key().identifier(), list));
        return null;
    }

    @VisibleForTesting
    @Nullable JsonObject handleJsonObject(JsonObject jsonObject) {
        try {
            JsonElement jsonElement = JsonRPCUtils.getRequestId(jsonObject);
            String string = JsonRPCUtils.getMethodName(jsonObject);
            JsonElement jsonElement2 = JsonRPCUtils.getResult(jsonObject);
            JsonElement jsonElement3 = JsonRPCUtils.getParams(jsonObject);
            JsonObject jsonObject2 = JsonRPCUtils.getError(jsonObject);
            if (string != null && jsonElement2 == null && jsonObject2 == null) {
                if (jsonElement != null && !Connection.isValidRequestId(jsonElement)) {
                    return JsonRPCErrors.INVALID_REQUEST.createWithUnknownId("Invalid request id - only String, Number and NULL supported");
                }
                return this.handleIncomingRequest(jsonElement, string, jsonElement3);
            }
            if (string == null && jsonElement2 != null && jsonObject2 == null && jsonElement != null) {
                if (Connection.isValidResponseId(jsonElement)) {
                    this.handleRequestResponse(jsonElement.getAsInt(), jsonElement2);
                } else {
                    LOGGER.warn("Received respose {} with id {} we did not request", (Object)jsonElement2, (Object)jsonElement);
                }
                return null;
            }
            if (string == null && jsonElement2 == null && jsonObject2 != null) {
                return this.handleError(jsonElement, jsonObject2);
            }
            return JsonRPCErrors.INVALID_REQUEST.createWithoutData((JsonElement)Objects.requireNonNullElse((Object)jsonElement, (Object)JsonNull.INSTANCE));
        }
        catch (Exception exception) {
            LOGGER.error("Error while handling rpc request", (Throwable)exception);
            return JsonRPCErrors.INTERNAL_ERROR.createWithUnknownId("Unknown error handling request - check server logs for stack trace");
        }
    }

    private static boolean isValidRequestId(JsonElement jsonElement) {
        return jsonElement.isJsonNull() || GsonHelper.isNumberValue(jsonElement) || GsonHelper.isStringValue(jsonElement);
    }

    private static boolean isValidResponseId(JsonElement jsonElement) {
        return GsonHelper.isNumberValue(jsonElement);
    }

    private @Nullable JsonObject handleIncomingRequest(@Nullable JsonElement jsonElement, String string, @Nullable JsonElement jsonElement2) {
        boolean bl = jsonElement != null;
        try {
            JsonElement jsonElement3 = this.dispatchIncomingRequest(string, jsonElement2);
            if (jsonElement3 == null || !bl) {
                return null;
            }
            return JsonRPCUtils.createSuccessResult(jsonElement, jsonElement3);
        }
        catch (InvalidParameterJsonRpcException invalidParameterJsonRpcException) {
            LOGGER.debug("Invalid parameter invocation {}: {}, {}", new Object[]{string, jsonElement2, invalidParameterJsonRpcException.getMessage()});
            return bl ? JsonRPCErrors.INVALID_PARAMS.create(jsonElement, invalidParameterJsonRpcException.getMessage()) : null;
        }
        catch (EncodeJsonRpcException encodeJsonRpcException) {
            LOGGER.error("Failed to encode json rpc response {}: {}", (Object)string, (Object)encodeJsonRpcException.getMessage());
            return bl ? JsonRPCErrors.INTERNAL_ERROR.create(jsonElement, encodeJsonRpcException.getMessage()) : null;
        }
        catch (InvalidRequestJsonRpcException invalidRequestJsonRpcException) {
            return bl ? JsonRPCErrors.INVALID_REQUEST.create(jsonElement, invalidRequestJsonRpcException.getMessage()) : null;
        }
        catch (MethodNotFoundJsonRpcException methodNotFoundJsonRpcException) {
            return bl ? JsonRPCErrors.METHOD_NOT_FOUND.create(jsonElement, methodNotFoundJsonRpcException.getMessage()) : null;
        }
        catch (Exception exception) {
            LOGGER.error("Error while dispatching rpc method {}", (Object)string, (Object)exception);
            return bl ? JsonRPCErrors.INTERNAL_ERROR.createWithoutData(jsonElement) : null;
        }
    }

    public @Nullable JsonElement dispatchIncomingRequest(String string, @Nullable JsonElement jsonElement) {
        Identifier identifier = Identifier.tryParse(string);
        if (identifier == null) {
            throw new InvalidRequestJsonRpcException("Failed to parse method value: " + string);
        }
        Optional<IncomingRpcMethod<?, ?>> optional = BuiltInRegistries.INCOMING_RPC_METHOD.getOptional(identifier);
        if (optional.isEmpty()) {
            throw new MethodNotFoundJsonRpcException("Method not found: " + string);
        }
        if (optional.get().attributes().runOnMainThread()) {
            try {
                return this.minecraftApi.submit(() -> ((IncomingRpcMethod)optional.get()).apply(this.minecraftApi, jsonElement, this.clientInfo)).join();
            }
            catch (CompletionException completionException) {
                Throwable throwable = completionException.getCause();
                if (throwable instanceof RuntimeException) {
                    RuntimeException runtimeException = (RuntimeException)throwable;
                    throw runtimeException;
                }
                throw completionException;
            }
        }
        return optional.get().apply(this.minecraftApi, jsonElement, this.clientInfo);
    }

    private void handleRequestResponse(int i, JsonElement jsonElement) {
        PendingRpcRequest pendingRpcRequest = (PendingRpcRequest)((Object)this.pendingRequests.remove(i));
        if (pendingRpcRequest == null) {
            LOGGER.warn("Received unknown response (id: {}): {}", (Object)i, (Object)jsonElement);
        } else {
            pendingRpcRequest.accept(jsonElement);
        }
    }

    private @Nullable JsonObject handleError(@Nullable JsonElement jsonElement, JsonObject jsonObject) {
        PendingRpcRequest pendingRpcRequest;
        if (jsonElement != null && Connection.isValidResponseId(jsonElement) && (pendingRpcRequest = (PendingRpcRequest)((Object)this.pendingRequests.remove(jsonElement.getAsInt()))) != null) {
            pendingRpcRequest.resultFuture().completeExceptionally(new RemoteRpcErrorException(jsonElement, jsonObject));
        }
        LOGGER.error("Received error (id: {}): {}", (Object)jsonElement, (Object)jsonObject);
        return null;
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.channelRead0(channelHandlerContext, (JsonElement)object);
    }
}

