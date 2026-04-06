/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.api.SchemaComponent;

public class DiscoveryService {
    public static DiscoverResponse discover(List<SchemaComponent<?>> list) {
        ArrayList list2 = new ArrayList(BuiltInRegistries.INCOMING_RPC_METHOD.size() + BuiltInRegistries.OUTGOING_RPC_METHOD.size());
        BuiltInRegistries.INCOMING_RPC_METHOD.listElements().forEach(reference -> {
            if (((IncomingRpcMethod)reference.value()).attributes().discoverable()) {
                list2.add(((IncomingRpcMethod)reference.value()).info().named(reference.key().identifier()));
            }
        });
        BuiltInRegistries.OUTGOING_RPC_METHOD.listElements().forEach(reference -> {
            if (((OutgoingRpcMethod)reference.value()).attributes().discoverable()) {
                list2.add(((OutgoingRpcMethod)reference.value()).info().named(reference.key().identifier()));
            }
        });
        HashMap map = new HashMap();
        for (SchemaComponent<?> schemaComponent : list) {
            map.put(schemaComponent.name(), schemaComponent.schema().info());
        }
        DiscoverInfo discoverInfo = new DiscoverInfo("Minecraft Server JSON-RPC", "2.0.0");
        return new DiscoverResponse("1.3.2", discoverInfo, list2, new DiscoverComponents(map));
    }

    public record DiscoverInfo(String title, String version) {
        public static final MapCodec<DiscoverInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("title").forGetter(DiscoverInfo::title), (App)Codec.STRING.fieldOf("version").forGetter(DiscoverInfo::version)).apply((Applicative)instance, DiscoverInfo::new));
    }

    public record DiscoverResponse(String jsonRpcProtocolVersion, DiscoverInfo discoverInfo, List<MethodInfo.Named<?, ?>> methods, DiscoverComponents components) {
        public static final MapCodec<DiscoverResponse> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("openrpc").forGetter(DiscoverResponse::jsonRpcProtocolVersion), (App)DiscoverInfo.CODEC.codec().fieldOf("info").forGetter(DiscoverResponse::discoverInfo), (App)Codec.list(MethodInfo.Named.CODEC).fieldOf("methods").forGetter(DiscoverResponse::methods), (App)DiscoverComponents.CODEC.codec().fieldOf("components").forGetter(DiscoverResponse::components)).apply((Applicative)instance, DiscoverResponse::new));
    }

    public record DiscoverComponents(Map<String, Schema<?>> schemas) {
        public static final MapCodec<DiscoverComponents> CODEC = DiscoverComponents.typedSchema();

        private static MapCodec<DiscoverComponents> typedSchema() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap((Codec)Codec.STRING, Schema.CODEC).fieldOf("schemas").forGetter(DiscoverComponents::schemas)).apply((Applicative)instance, DiscoverComponents::new));
        }
    }
}

