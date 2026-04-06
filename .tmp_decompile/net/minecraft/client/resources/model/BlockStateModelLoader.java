/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BlockStateModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static CompletableFuture<LoadedModels> loadBlockStates(ResourceManager resourceManager, Executor executor) {
        Function<Identifier, @Nullable StateDefinition<Block, BlockState>> function = BlockStateDefinitions.definitionLocationToBlockStateMapper();
        return CompletableFuture.supplyAsync(() -> BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<@Nullable LoadedModels>> list2 = new ArrayList<CompletableFuture<LoadedModels>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                list2.add(CompletableFuture.supplyAsync(() -> {
                    @Nullable Identifier identifier = BLOCKSTATE_LISTER.fileToId((Identifier)entry.getKey());
                    @Nullable StateDefinition stateDefinition = (StateDefinition)function.apply(identifier);
                    if (stateDefinition == null) {
                        LOGGER.debug("Discovered unknown block state definition {}, ignoring", (Object)identifier);
                        return null;
                    }
                    List list = (List)entry.getValue();
                    ArrayList<LoadedBlockModelDefinition> list2 = new ArrayList<LoadedBlockModelDefinition>(list.size());
                    for (Resource resource : list) {
                        try {
                            BufferedReader reader = resource.openAsReader();
                            try {
                                JsonElement jsonElement = StrictJsonParser.parse(reader);
                                BlockModelDefinition blockModelDefinition = (BlockModelDefinition)((Object)((Object)((Object)BlockModelDefinition.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonParseException::new))));
                                list2.add(new LoadedBlockModelDefinition(resource.sourcePackId(), blockModelDefinition));
                            }
                            finally {
                                if (reader == null) continue;
                                ((Reader)reader).close();
                            }
                        }
                        catch (Exception exception) {
                            LOGGER.error("Failed to load blockstate definition {} from pack {}", new Object[]{identifier, resource.sourcePackId(), exception});
                        }
                    }
                    try {
                        return BlockStateModelLoader.loadBlockStateDefinitionStack(identifier, stateDefinition, list2);
                    }
                    catch (Exception exception2) {
                        LOGGER.error("Failed to load blockstate definition {}", (Object)identifier, (Object)exception2);
                        return null;
                    }
                }, executor));
            }
            return Util.sequence(list2).thenApply(list -> {
                IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot> map = new IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot>();
                for (LoadedModels loadedModels : list) {
                    if (loadedModels == null) continue;
                    map.putAll(loadedModels.models());
                }
                return new LoadedModels(map);
            });
        });
    }

    private static LoadedModels loadBlockStateDefinitionStack(Identifier identifier, StateDefinition<Block, BlockState> stateDefinition, List<LoadedBlockModelDefinition> list) {
        IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot> map = new IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot>();
        for (LoadedBlockModelDefinition loadedBlockModelDefinition : list) {
            map.putAll(loadedBlockModelDefinition.contents.instantiate(stateDefinition, () -> String.valueOf(identifier) + "/" + loadedBlockModelDefinition.source));
        }
        return new LoadedModels(map);
    }

    @Environment(value=EnvType.CLIENT)
    static final class LoadedBlockModelDefinition
    extends Record {
        final String source;
        final BlockModelDefinition contents;

        LoadedBlockModelDefinition(String string, BlockModelDefinition blockModelDefinition) {
            this.source = string;
            this.contents = blockModelDefinition;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this, object);
        }

        public String source() {
            return this.source;
        }

        public BlockModelDefinition contents() {
            return this.contents;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record LoadedModels(Map<BlockState, BlockStateModel.UnbakedRoot> models) {
    }
}

