/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientItemInfoLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("items");

    public static CompletableFuture<LoadedClientInfos> scheduleLoad(ResourceManager resourceManager, Executor executor) {
        RegistryAccess.Frozen frozen = ClientRegistryLayer.createRegistryAccess().compositeAccess();
        return CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList list2 = new ArrayList(map.size());
            map.forEach((identifier, resource) -> list2.add(CompletableFuture.supplyAsync(() -> {
                PendingLoad pendingLoad;
                block8: {
                    Identifier identifier2 = LISTER.fileToId((Identifier)identifier);
                    BufferedReader reader = resource.openAsReader();
                    try {
                        PlaceholderLookupProvider placeholderLookupProvider = new PlaceholderLookupProvider(frozen);
                        RegistryOps dynamicOps = placeholderLookupProvider.createSerializationContext(JsonOps.INSTANCE);
                        ClientItem clientItem2 = ClientItem.CODEC.parse(dynamicOps, (Object)StrictJsonParser.parse(reader)).ifError(error -> LOGGER.error("Couldn't parse item model '{}' from pack '{}': {}", new Object[]{identifier2, resource.sourcePackId(), error.message()})).result().map(clientItem -> {
                            if (placeholderLookupProvider.hasRegisteredPlaceholders()) {
                                return clientItem.withRegistrySwapper(placeholderLookupProvider.createSwapper());
                            }
                            return clientItem;
                        }).orElse(null);
                        pendingLoad = new PendingLoad(identifier2, clientItem2);
                        if (reader == null) break block8;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (reader != null) {
                                try {
                                    ((Reader)reader).close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (Exception exception) {
                            LOGGER.error("Failed to open item model {} from pack '{}'", new Object[]{identifier, resource.sourcePackId(), exception});
                            return new PendingLoad(identifier2, null);
                        }
                    }
                    ((Reader)reader).close();
                }
                return pendingLoad;
            }, executor)));
            return Util.sequence(list2).thenApply(list -> {
                HashMap<Identifier, ClientItem> map = new HashMap<Identifier, ClientItem>();
                for (PendingLoad pendingLoad : list) {
                    if (pendingLoad.clientItemInfo == null) continue;
                    map.put(pendingLoad.id, pendingLoad.clientItemInfo);
                }
                return new LoadedClientInfos(map);
            });
        });
    }

    @Environment(value=EnvType.CLIENT)
    static final class PendingLoad
    extends Record {
        final Identifier id;
        final @Nullable ClientItem clientItemInfo;

        PendingLoad(Identifier identifier, @Nullable ClientItem clientItem) {
            this.id = identifier;
            this.clientItemInfo = clientItem;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public @Nullable ClientItem clientItemInfo() {
            return this.clientItemInfo;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record LoadedClientInfos(Map<Identifier, ClientItem> contents) {
    }
}

