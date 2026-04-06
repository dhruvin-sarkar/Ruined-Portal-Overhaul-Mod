/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.invoke.LambdaMetafactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;

public class TagNetworkSerialization {
    public static Map<ResourceKey<? extends Registry<?>>, NetworkPayload> serializeTagsToNetwork(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        return RegistrySynchronization.networkSafeRegistries(layeredRegistryAccess).map(registryEntry -> Pair.of(registryEntry.key(), (Object)TagNetworkSerialization.serializeToNetwork(registryEntry.value()))).filter(pair -> !((NetworkPayload)pair.getSecond()).isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> NetworkPayload serializeToNetwork(Registry<T> registry) {
        HashMap<Identifier, IntList> map = new HashMap<Identifier, IntList>();
        registry.getTags().forEach(named -> {
            IntArrayList intList = new IntArrayList(named.size());
            for (Holder holder : named) {
                if (holder.kind() != Holder.Kind.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + String.valueOf(holder));
                }
                intList.add(registry.getId(holder.value()));
            }
            map.put(named.key().location(), (IntList)intList);
        });
        return new NetworkPayload(map);
    }

    static <T> TagLoader.LoadResult<T> deserializeTagsFromNetwork(Registry<T> registry, NetworkPayload networkPayload) {
        ResourceKey resourceKey = registry.key();
        HashMap map = new HashMap();
        networkPayload.tags.forEach((identifier, intList) -> {
            TagKey tagKey = TagKey.create(resourceKey, identifier);
            List list = (List)intList.intStream().mapToObj(registry::get).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).collect(Collectors.toUnmodifiableList());
            map.put(tagKey, list);
        });
        return new TagLoader.LoadResult<T>(resourceKey, map);
    }

    public static final class NetworkPayload {
        public static final NetworkPayload EMPTY = new NetworkPayload(Map.of());
        final Map<Identifier, IntList> tags;

        NetworkPayload(Map<Identifier, IntList> map) {
            this.tags = map;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeMap(this.tags, FriendlyByteBuf::writeIdentifier, FriendlyByteBuf::writeIntIdList);
        }

        public static NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
            return new NetworkPayload(friendlyByteBuf.readMap(FriendlyByteBuf::readIdentifier, FriendlyByteBuf::readIntIdList));
        }

        public boolean isEmpty() {
            return this.tags.isEmpty();
        }

        public int size() {
            return this.tags.size();
        }

        public <T> TagLoader.LoadResult<T> resolve(Registry<T> registry) {
            return TagNetworkSerialization.deserializeTagsFromNetwork(registry, this);
        }
    }
}

