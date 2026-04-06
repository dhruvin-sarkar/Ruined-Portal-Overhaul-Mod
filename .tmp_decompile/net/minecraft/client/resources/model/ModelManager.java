/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Multimaps
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MissingBlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelGroupCollector;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ModelManager
implements PreparableReloadListener {
    public static final Identifier BLOCK_OR_ITEM = Identifier.withDefaultNamespace("block_or_item");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    private Map<Identifier, ItemModel> bakedItemStackModels = Map.of();
    private Map<Identifier, ClientItem.Properties> itemProperties = Map.of();
    private final AtlasManager atlasManager;
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private EntityModelSet entityModelSet = EntityModelSet.EMPTY;
    private SpecialBlockModelRenderer specialBlockModelRenderer = SpecialBlockModelRenderer.EMPTY;
    private ModelBakery.MissingModels missingModels;
    private Object2IntMap<BlockState> modelGroups = Object2IntMaps.emptyMap();

    public ModelManager(BlockColors blockColors, AtlasManager atlasManager, PlayerSkinRenderCache playerSkinRenderCache) {
        this.blockColors = blockColors;
        this.atlasManager = atlasManager;
        this.playerSkinRenderCache = playerSkinRenderCache;
        this.blockModelShaper = new BlockModelShaper(this);
    }

    public BlockStateModel getMissingBlockStateModel() {
        return this.missingModels.block();
    }

    public ItemModel getItemModel(Identifier identifier) {
        return this.bakedItemStackModels.getOrDefault(identifier, this.missingModels.item());
    }

    public ClientItem.Properties getItemProperties(Identifier identifier) {
        return this.itemProperties.getOrDefault(identifier, ClientItem.Properties.DEFAULT);
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        ResourceManager resourceManager = sharedState.resourceManager();
        CompletableFuture<EntityModelSet> completableFuture = CompletableFuture.supplyAsync(EntityModelSet::vanilla, executor);
        CompletionStage completableFuture2 = completableFuture.thenApplyAsync(entityModelSet -> SpecialBlockModelRenderer.vanilla(new SpecialModelRenderer.BakingContext.Simple((EntityModelSet)entityModelSet, this.atlasManager, this.playerSkinRenderCache)), executor);
        CompletableFuture<Map<Identifier, UnbakedModel>> completableFuture3 = ModelManager.loadBlockModels(resourceManager, executor);
        CompletableFuture<BlockStateModelLoader.LoadedModels> completableFuture4 = BlockStateModelLoader.loadBlockStates(resourceManager, executor);
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> completableFuture5 = ClientItemInfoLoader.scheduleLoad(resourceManager, executor);
        CompletionStage completableFuture6 = CompletableFuture.allOf(completableFuture3, completableFuture4, completableFuture5).thenApplyAsync(void_ -> ModelManager.discoverModelDependencies((Map)completableFuture3.join(), (BlockStateModelLoader.LoadedModels)((Object)((Object)completableFuture4.join())), (ClientItemInfoLoader.LoadedClientInfos)((Object)((Object)completableFuture5.join()))), executor);
        CompletionStage completableFuture7 = completableFuture4.thenApplyAsync(loadedModels -> ModelManager.buildModelGroups(this.blockColors, loadedModels), executor);
        AtlasManager.PendingStitchResults pendingStitchResults = sharedState.get(AtlasManager.PENDING_STITCH);
        CompletableFuture<SpriteLoader.Preparations> completableFuture8 = pendingStitchResults.get(AtlasIds.BLOCKS);
        CompletableFuture<SpriteLoader.Preparations> completableFuture9 = pendingStitchResults.get(AtlasIds.ITEMS);
        return ((CompletableFuture)((CompletableFuture)CompletableFuture.allOf(new CompletableFuture[]{completableFuture8, completableFuture9, completableFuture6, completableFuture7, completableFuture4, completableFuture5, completableFuture, completableFuture2, completableFuture3}).thenComposeAsync(arg_0 -> this.method_65753(completableFuture8, completableFuture9, (CompletableFuture)completableFuture6, (CompletableFuture)completableFuture7, completableFuture3, completableFuture, completableFuture4, completableFuture5, (CompletableFuture)completableFuture2, executor, arg_0), executor)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(this::apply, executor2);
    }

    private static CompletableFuture<Map<Identifier, UnbakedModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> MODEL_LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<@Nullable Pair>> list2 = new ArrayList<CompletableFuture<Pair>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                list2.add(CompletableFuture.supplyAsync(() -> {
                    Pair pair;
                    block8: {
                        Identifier identifier = MODEL_LISTER.fileToId((Identifier)entry.getKey());
                        @Nullable BufferedReader reader = ((Resource)entry.getValue()).openAsReader();
                        try {
                            pair = Pair.of((Object)identifier, (Object)BlockModel.fromStream(reader));
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
                                LOGGER.error("Failed to load model {}", entry.getKey(), (Object)exception);
                                return null;
                            }
                        }
                        ((Reader)reader).close();
                    }
                    return pair;
                }, executor));
            }
            return Util.sequence(list2).thenApply(list -> (Map)list.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static ResolvedModels discoverModelDependencies(Map<Identifier, UnbakedModel> map, BlockStateModelLoader.LoadedModels loadedModels, ClientItemInfoLoader.LoadedClientInfos loadedClientInfos) {
        try (Zone zone = Profiler.get().zone("dependencies");){
            ModelDiscovery modelDiscovery = new ModelDiscovery(map, MissingBlockModel.missingModel());
            modelDiscovery.addSpecialModel(ItemModelGenerator.GENERATED_ITEM_MODEL_ID, new ItemModelGenerator());
            loadedModels.models().values().forEach(modelDiscovery::addRoot);
            loadedClientInfos.contents().values().forEach(clientItem -> modelDiscovery.addRoot(clientItem.model()));
            ResolvedModels resolvedModels = new ResolvedModels(modelDiscovery.missingModel(), modelDiscovery.resolve());
            return resolvedModels;
        }
    }

    private static CompletableFuture<ReloadState> loadModels(final SpriteLoader.Preparations preparations, final SpriteLoader.Preparations preparations2, ModelBakery modelBakery, Object2IntMap<BlockState> object2IntMap, EntityModelSet entityModelSet, SpecialBlockModelRenderer specialBlockModelRenderer, Executor executor) {
        final Multimap multimap = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
        final Multimap multimap2 = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
        return modelBakery.bakeModels(new SpriteGetter(){
            private final TextureAtlasSprite blockMissing;
            private final TextureAtlasSprite itemMissing;
            {
                this.blockMissing = preparations.missing();
                this.itemMissing = preparations2.missing();
            }

            @Override
            public TextureAtlasSprite get(Material material, ModelDebugName modelDebugName) {
                TextureAtlasSprite textureAtlasSprite;
                Identifier identifier = material.atlasLocation();
                boolean bl = identifier.equals(BLOCK_OR_ITEM);
                boolean bl2 = identifier.equals(TextureAtlas.LOCATION_ITEMS);
                boolean bl3 = identifier.equals(TextureAtlas.LOCATION_BLOCKS);
                if ((bl || bl2) && (textureAtlasSprite = preparations2.getSprite(material.texture())) != null) {
                    return textureAtlasSprite;
                }
                if ((bl || bl3) && (textureAtlasSprite = preparations.getSprite(material.texture())) != null) {
                    return textureAtlasSprite;
                }
                multimap.put((Object)modelDebugName.debugName(), (Object)material);
                return bl2 ? this.itemMissing : this.blockMissing;
            }

            @Override
            public TextureAtlasSprite reportMissingReference(String string, ModelDebugName modelDebugName) {
                multimap2.put((Object)modelDebugName.debugName(), (Object)string);
                return this.blockMissing;
            }
        }, executor).thenApply(bakingResult -> {
            multimap.asMap().forEach((string, collection) -> LOGGER.warn("Missing textures in model {}:\n{}", string, (Object)collection.stream().sorted(Material.COMPARATOR).map(material -> "    " + String.valueOf(material.atlasLocation()) + ":" + String.valueOf(material.texture())).collect(Collectors.joining("\n"))));
            multimap2.asMap().forEach((string2, collection) -> LOGGER.warn("Missing texture references in model {}:\n{}", string2, (Object)collection.stream().sorted().map(string -> "    " + string).collect(Collectors.joining("\n"))));
            Map<BlockState, BlockStateModel> map = ModelManager.createBlockStateToModelDispatch(bakingResult.blockStateModels(), bakingResult.missingModels().block());
            return new ReloadState((ModelBakery.BakingResult)((Object)bakingResult), object2IntMap, map, entityModelSet, specialBlockModelRenderer);
        });
    }

    private static Map<BlockState, BlockStateModel> createBlockStateToModelDispatch(Map<BlockState, BlockStateModel> map, BlockStateModel blockStateModel) {
        try (Zone zone = Profiler.get().zone("block state dispatch");){
            IdentityHashMap<BlockState, BlockStateModel> map2 = new IdentityHashMap<BlockState, BlockStateModel>(map);
            for (Block block : BuiltInRegistries.BLOCK) {
                block.getStateDefinition().getPossibleStates().forEach(blockState -> {
                    if (map.putIfAbsent((BlockState)blockState, blockStateModel) == null) {
                        LOGGER.warn("Missing model for variant: '{}'", blockState);
                    }
                });
            }
            IdentityHashMap<BlockState, BlockStateModel> identityHashMap = map2;
            return identityHashMap;
        }
    }

    private static Object2IntMap<BlockState> buildModelGroups(BlockColors blockColors, BlockStateModelLoader.LoadedModels loadedModels) {
        try (Zone zone = Profiler.get().zone("block groups");){
            Object2IntMap<BlockState> object2IntMap = ModelGroupCollector.build(blockColors, loadedModels);
            return object2IntMap;
        }
    }

    private void apply(ReloadState reloadState) {
        ModelBakery.BakingResult bakingResult = reloadState.bakedModels;
        this.bakedItemStackModels = bakingResult.itemStackModels();
        this.itemProperties = bakingResult.itemProperties();
        this.modelGroups = reloadState.modelGroups;
        this.missingModels = bakingResult.missingModels();
        this.blockModelShaper.replaceCache(reloadState.modelCache);
        this.specialBlockModelRenderer = reloadState.specialBlockModelRenderer;
        this.entityModelSet = reloadState.entityModelSet;
    }

    public boolean requiresRender(BlockState blockState, BlockState blockState2) {
        int j;
        if (blockState == blockState2) {
            return false;
        }
        int i = this.modelGroups.getInt((Object)blockState);
        if (i != -1 && i == (j = this.modelGroups.getInt((Object)blockState2))) {
            FluidState fluidState2;
            FluidState fluidState = blockState.getFluidState();
            return fluidState != (fluidState2 = blockState2.getFluidState());
        }
        return true;
    }

    public SpecialBlockModelRenderer specialBlockModelRenderer() {
        return this.specialBlockModelRenderer;
    }

    public Supplier<EntityModelSet> entityModels() {
        return () -> this.entityModelSet;
    }

    private /* synthetic */ CompletionStage method_65753(CompletableFuture completableFuture, CompletableFuture completableFuture2, CompletableFuture completableFuture3, CompletableFuture completableFuture4, CompletableFuture completableFuture5, CompletableFuture completableFuture6, CompletableFuture completableFuture7, CompletableFuture completableFuture8, CompletableFuture completableFuture9, Executor executor, Void void_) {
        SpriteLoader.Preparations preparations = (SpriteLoader.Preparations)((Object)completableFuture.join());
        SpriteLoader.Preparations preparations2 = (SpriteLoader.Preparations)((Object)completableFuture2.join());
        ResolvedModels resolvedModels = (ResolvedModels)((Object)completableFuture3.join());
        Object2IntMap object2IntMap = (Object2IntMap)completableFuture4.join();
        Sets.SetView set = Sets.difference(((Map)completableFuture5.join()).keySet(), resolvedModels.models.keySet());
        if (!set.isEmpty()) {
            LOGGER.debug("Unreferenced models: \n{}", (Object)set.stream().sorted().map(identifier -> "\t" + String.valueOf(identifier) + "\n").collect(Collectors.joining()));
        }
        ModelBakery modelBakery = new ModelBakery((EntityModelSet)completableFuture6.join(), this.atlasManager, this.playerSkinRenderCache, ((BlockStateModelLoader.LoadedModels)((Object)completableFuture7.join())).models(), ((ClientItemInfoLoader.LoadedClientInfos)((Object)completableFuture8.join())).contents(), resolvedModels.models(), resolvedModels.missing());
        return ModelManager.loadModels(preparations, preparations2, modelBakery, (Object2IntMap<BlockState>)object2IntMap, (EntityModelSet)completableFuture6.join(), (SpecialBlockModelRenderer)completableFuture9.join(), executor);
    }

    @Environment(value=EnvType.CLIENT)
    static final class ResolvedModels
    extends Record {
        private final ResolvedModel missing;
        final Map<Identifier, ResolvedModel> models;

        ResolvedModels(ResolvedModel resolvedModel, Map<Identifier, ResolvedModel> map) {
            this.missing = resolvedModel;
            this.models = map;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this, object);
        }

        public ResolvedModel missing() {
            return this.missing;
        }

        public Map<Identifier, ResolvedModel> models() {
            return this.models;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ReloadState
    extends Record {
        final ModelBakery.BakingResult bakedModels;
        final Object2IntMap<BlockState> modelGroups;
        final Map<BlockState, BlockStateModel> modelCache;
        final EntityModelSet entityModelSet;
        final SpecialBlockModelRenderer specialBlockModelRenderer;

        ReloadState(ModelBakery.BakingResult bakingResult, Object2IntMap<BlockState> object2IntMap, Map<BlockState, BlockStateModel> map, EntityModelSet entityModelSet, SpecialBlockModelRenderer specialBlockModelRenderer) {
            this.bakedModels = bakingResult;
            this.modelGroups = object2IntMap;
            this.modelCache = map;
            this.entityModelSet = entityModelSet;
            this.specialBlockModelRenderer = specialBlockModelRenderer;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;entityModelSet;specialBlockModelRenderer", "bakedModels", "modelGroups", "modelCache", "entityModelSet", "specialBlockModelRenderer"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;entityModelSet;specialBlockModelRenderer", "bakedModels", "modelGroups", "modelCache", "entityModelSet", "specialBlockModelRenderer"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;entityModelSet;specialBlockModelRenderer", "bakedModels", "modelGroups", "modelCache", "entityModelSet", "specialBlockModelRenderer"}, this, object);
        }

        public ModelBakery.BakingResult bakedModels() {
            return this.bakedModels;
        }

        public Object2IntMap<BlockState> modelGroups() {
            return this.modelGroups;
        }

        public Map<BlockState, BlockStateModel> modelCache() {
            return this.modelCache;
        }

        public EntityModelSet entityModelSet() {
            return this.entityModelSet;
        }

        public SpecialBlockModelRenderer specialBlockModelRenderer() {
            return this.specialBlockModelRenderer;
        }
    }
}

