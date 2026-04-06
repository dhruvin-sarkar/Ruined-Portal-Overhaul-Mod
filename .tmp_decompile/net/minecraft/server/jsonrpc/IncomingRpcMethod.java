/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import org.jspecify.annotations.Nullable;

public interface IncomingRpcMethod<Params, Result> {
    public MethodInfo<Params, Result> info();

    public Attributes attributes();

    public JsonElement apply(MinecraftApi var1, @Nullable JsonElement var2, ClientInfo var3);

    public static <Result> IncomingRpcMethodBuilder<Void, Result> method(ParameterlessRpcMethodFunction<Result> parameterlessRpcMethodFunction) {
        return new IncomingRpcMethodBuilder(parameterlessRpcMethodFunction);
    }

    public static <Params, Result> IncomingRpcMethodBuilder<Params, Result> method(RpcMethodFunction<Params, Result> rpcMethodFunction) {
        return new IncomingRpcMethodBuilder<Params, Result>(rpcMethodFunction);
    }

    public static <Result> IncomingRpcMethodBuilder<Void, Result> method(Function<MinecraftApi, Result> function) {
        return new IncomingRpcMethodBuilder(function);
    }

    public static class IncomingRpcMethodBuilder<Params, Result> {
        private String description = "";
        private @Nullable ParamInfo<Params> paramInfo;
        private @Nullable ResultInfo<Result> resultInfo;
        private boolean discoverable = true;
        private boolean runOnMainThread = true;
        private @Nullable ParameterlessRpcMethodFunction<Result> parameterlessFunction;
        private @Nullable RpcMethodFunction<Params, Result> parameterFunction;

        public IncomingRpcMethodBuilder(ParameterlessRpcMethodFunction<Result> parameterlessRpcMethodFunction) {
            this.parameterlessFunction = parameterlessRpcMethodFunction;
        }

        public IncomingRpcMethodBuilder(RpcMethodFunction<Params, Result> rpcMethodFunction) {
            this.parameterFunction = rpcMethodFunction;
        }

        public IncomingRpcMethodBuilder(Function<MinecraftApi, Result> function) {
            this.parameterlessFunction = (minecraftApi, clientInfo) -> function.apply(minecraftApi);
        }

        public IncomingRpcMethodBuilder<Params, Result> description(String string) {
            this.description = string;
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> response(String string, Schema<Result> schema) {
            this.resultInfo = new ResultInfo<Result>(string, schema.info());
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> param(String string, Schema<Params> schema) {
            this.paramInfo = new ParamInfo<Params>(string, schema.info());
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> undiscoverable() {
            this.discoverable = false;
            return this;
        }

        public IncomingRpcMethodBuilder<Params, Result> notOnMainThread() {
            this.runOnMainThread = false;
            return this;
        }

        public IncomingRpcMethod<Params, Result> build() {
            if (this.resultInfo == null) {
                throw new IllegalStateException("No response defined");
            }
            Attributes attributes = new Attributes(this.runOnMainThread, this.discoverable);
            MethodInfo<Params, Result> methodInfo = new MethodInfo<Params, Result>(this.description, this.paramInfo, this.resultInfo);
            if (this.parameterlessFunction != null) {
                return new ParameterlessMethod<Params, Result>(methodInfo, attributes, this.parameterlessFunction);
            }
            if (this.parameterFunction != null) {
                if (this.paramInfo == null) {
                    throw new IllegalStateException("No param schema defined");
                }
                return new Method<Params, Result>(methodInfo, attributes, this.parameterFunction);
            }
            throw new IllegalStateException("No method defined");
        }

        public IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> registry, String string) {
            return this.register(registry, Identifier.withDefaultNamespace(string));
        }

        private IncomingRpcMethod<?, ?> register(Registry<IncomingRpcMethod<?, ?>> registry, Identifier identifier) {
            return Registry.register(registry, identifier, this.build());
        }
    }

    @FunctionalInterface
    public static interface ParameterlessRpcMethodFunction<Result> {
        public Result apply(MinecraftApi var1, ClientInfo var2);
    }

    @FunctionalInterface
    public static interface RpcMethodFunction<Params, Result> {
        public Result apply(MinecraftApi var1, Params var2, ClientInfo var3);
    }

    public record Method<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes, RpcMethodFunction<Params, Result> function) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement apply(MinecraftApi minecraftApi, @Nullable JsonElement jsonElement, ClientInfo clientInfo) {
            JsonElement jsonElement3;
            if (jsonElement == null || !jsonElement.isJsonArray() && !jsonElement.isJsonObject()) {
                throw new InvalidParameterJsonRpcException("Expected params as array or named");
            }
            if (this.info.params().isEmpty()) {
                throw new IllegalArgumentException("Method defined as having parameters without describing them");
            }
            if (jsonElement.isJsonObject()) {
                String string = this.info.params().get().name();
                JsonElement jsonElement2 = jsonElement.getAsJsonObject().get(string);
                if (jsonElement2 == null) {
                    throw new InvalidParameterJsonRpcException(String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", string));
                }
                jsonElement3 = jsonElement2;
            } else {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.isEmpty() || jsonArray.size() > 1) {
                    throw new InvalidParameterJsonRpcException("Expected exactly one element in the params array");
                }
                jsonElement3 = jsonArray.get(0);
            }
            Object object = this.info.params().get().schema().codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement3).getOrThrow(InvalidParameterJsonRpcException::new);
            Result object2 = this.function.apply(minecraftApi, object, clientInfo);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object2).getOrThrow(EncodeJsonRpcException::new);
        }
    }

    public record ParameterlessMethod<Params, Result>(MethodInfo<Params, Result> info, Attributes attributes, ParameterlessRpcMethodFunction<Result> supplier) implements IncomingRpcMethod<Params, Result>
    {
        @Override
        public JsonElement apply(MinecraftApi minecraftApi, @Nullable JsonElement jsonElement, ClientInfo clientInfo) {
            if (!(jsonElement == null || jsonElement.isJsonArray() && jsonElement.getAsJsonArray().isEmpty())) {
                throw new InvalidParameterJsonRpcException("Expected no params, or an empty array");
            }
            if (this.info.params().isPresent()) {
                throw new IllegalArgumentException("Parameterless method unexpectedly has parameter description");
            }
            Result object = this.supplier.apply(minecraftApi, clientInfo);
            if (this.info.result().isEmpty()) {
                throw new IllegalStateException("No result codec defined");
            }
            return (JsonElement)this.info.result().get().schema().codec().encodeStart((DynamicOps)JsonOps.INSTANCE, object).getOrThrow(InvalidParameterJsonRpcException::new);
        }
    }

    public record Attributes(boolean runOnMainThread, boolean discoverable) {
    }
}

