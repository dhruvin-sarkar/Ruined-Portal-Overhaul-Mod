/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;

public record PendingRpcRequest<Result>(Holder.Reference<? extends OutgoingRpcMethod<?, ? extends Result>> method, CompletableFuture<Result> resultFuture, long timeoutTime) {
    public void accept(JsonElement jsonElement) {
        try {
            Result object = this.method.value().decodeResult(jsonElement);
            this.resultFuture.complete(Objects.requireNonNull(object));
        }
        catch (Exception exception) {
            this.resultFuture.completeExceptionally(exception);
        }
    }

    public boolean timedOut(long l) {
        return l > this.timeoutTime;
    }
}

