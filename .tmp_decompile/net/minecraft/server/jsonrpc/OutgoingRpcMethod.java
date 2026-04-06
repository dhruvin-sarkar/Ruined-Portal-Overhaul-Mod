/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import org.jspecify.annotations.Nullable;

public interface OutgoingRpcMethod<Params, Result> {
    public static final String NOTIFICATION_PREFIX = "notification/";

    public MethodInfo<Params, Result> info();

    public Attributes attributes();

    default public @Nullable JsonElement encodeParams(Params object) {
        return null;
    }

    default public @Nullable Result decodeResult(JsonElement jsonElement) {
        return null;
    }

    public static OutgoingRpcMethodBuilder<Void, Void> notification() {
        return new OutgoingRpcMethodBuilder<Void, Void>(ParmeterlessNotification::new);
    }

    public static <Params> OutgoingRpcMethodBuilder<Params, Void> notificationWithParams() {
        return new OutgoingRpcMethodBuilder(Notification::new);
    }

    public static <Result> OutgoingRpcMethodBuilder<Void, Result> request() {
        return new OutgoingRpcMethodBuilder(ParameterlessMethod::new);
    }

    public static <Params, Result> OutgoingRpcMethodBuilder<Params, Result> requestWithParams() {
        return new OutgoingRpcMethodBuilder(Method::new);
    }

    public static class OutgoingRpcMethodBuilder<Params, Result> {
        public static final Attributes DEFAULT_ATTRIBUTES = new Attributes(true);
        private final Factory<Params, Result> method;
        private String description = "";
        private @Nullable ParamInfo<Params> paramInfo;
        private @Nullable ResultInfo<Result> resultInfo;

        public OutgoingRpcMethodBuilder(Factory<Params, Result> factory) {
            this.method = factory;
        }

        public OutgoingRpcMethodBuilder<Params, Result> description(String string) {
            this.description = string;
            return this;
        }

        public OutgoingRpcMethodBuilder<Params, Result> response(String string, Schema<Result> schema) {
            this.resultInfo = new ResultInfo<Result>(string, schema);
            return this;
        }

        public OutgoingRpcMethodBuilder<Params, Result> param(String string, Schema<Params> schema) {
            this.paramInfo = new ParamInfo<Params>(string, schema);
            return this;
        }

        private OutgoingRpcMethod<Params, Result> build() {
            MethodInfo<Params, Result> methodInfo = new MethodInfo<Params, Result>(this.description, this.paramInfo, this.resultInfo);
            return this.method.create(methodInfo, DEFAULT_ATTRIBUTES);
        }

        public Holder.Reference<OutgoingRpcMethod<Params, Result>> register(String string) {
            return this.register(Identifier.withDefaultNamespace(OutgoingRpcMethod.NOTIFICATION_PREFIX + string));
        }

        private Holder.Reference<OutgoingRpcMethod<Params, Result>> register(Identifier identifier) {
            return Registry.registerForHolder(BuiltInRegistries.OUTGOING_RPC_METHOD, identifier, this.build());
        }
    }

    @FunctionalInterface
    public static interface Factory<Params, Result> {
        public OutgoingRpcMethod<Params, Result> create(MethodInfo<Params, Result> var1, Attributes var2);
    }

    public record Method<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes) implements OutgoingRpcMethod<Params, Result>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params object) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object).getOrThrow();
        }

        @Override
        public Result decodeResult(JsonElement jsonElement) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow();
        }
    }

    public record ParameterlessMethod<Result>(MethodInfo<Void, Result> info, Attributes attributes) implements OutgoingRpcMethod<Void, Result>
    {
        @Override
        public Result decodeResult(JsonElement jsonElement) {
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("Method defined as having no result");
            }
            return (Result)this.info.result().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow();
        }
    }

    public record Notification<Params>(MethodInfo<Params, Void> info, Attributes attributes) implements OutgoingRpcMethod<Params, Void>
    {
        @Override
        public @Nullable JsonElement encodeParams(Params object) {
            if (this.info.params().isEmpty()) {
                throw new IllegalStateException("Method defined as having no parameters");
            }
            return (JsonElement)this.info.params().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object).getOrThrow();
        }
    }

    public record ParmeterlessNotification(MethodInfo<Void, Void> info, Attributes attributes) implements OutgoingRpcMethod<Void, Void>
    {
    }

    public record Attributes(boolean discoverable) {
    }
}

