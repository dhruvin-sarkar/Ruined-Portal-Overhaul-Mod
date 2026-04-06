/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Interner
 *  com.google.common.collect.Interners
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.thread.ParallelMapTransform;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ModelBakery {
    public static final Material FIRE_0 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("fire_0");
    public static final Material FIRE_1 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("fire_1");
    public static final Material LAVA_STILL = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("lava_still");
    public static final Material LAVA_FLOW = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("lava_flow");
    public static final Material WATER_STILL = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("water_still");
    public static final Material WATER_FLOW = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("water_flow");
    public static final Material WATER_OVERLAY = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("water_overlay");
    public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, Identifier.withDefaultNamespace("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, Identifier.withDefaultNamespace("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, Identifier.withDefaultNamespace("entity/shield_base_nopattern"));
    public static final int DESTROY_STAGE_COUNT = 10;
    public static final List<Identifier> DESTROY_STAGES = IntStream.range(0, 10).mapToObj(i -> Identifier.withDefaultNamespace("block/destroy_stage_" + i)).collect(Collectors.toList());
    public static final List<Identifier> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map(identifier -> identifier.withPath(string -> "textures/" + string + ".png")).collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderTypes::crumbling).collect(Collectors.toList());
    static final Logger LOGGER = LogUtils.getLogger();
    private final EntityModelSet entityModelSet;
    private final MaterialSet materials;
    private final PlayerSkinRenderCache playerSkinRenderCache;
    private final Map<BlockState, BlockStateModel.UnbakedRoot> unbakedBlockStateModels;
    private final Map<Identifier, ClientItem> clientInfos;
    final Map<Identifier, ResolvedModel> resolvedModels;
    final ResolvedModel missingModel;

    public ModelBakery(EntityModelSet entityModelSet, MaterialSet materialSet, PlayerSkinRenderCache playerSkinRenderCache, Map<BlockState, BlockStateModel.UnbakedRoot> map, Map<Identifier, ClientItem> map2, Map<Identifier, ResolvedModel> map3, ResolvedModel resolvedModel) {
        this.entityModelSet = entityModelSet;
        this.materials = materialSet;
        this.playerSkinRenderCache = playerSkinRenderCache;
        this.unbakedBlockStateModels = map;
        this.clientInfos = map2;
        this.resolvedModels = map3;
        this.missingModel = resolvedModel;
    }

    public CompletableFuture<BakingResult> bakeModels(SpriteGetter spriteGetter, Executor executor) {
        PartCacheImpl partCacheImpl = new PartCacheImpl();
        MissingModels missingModels = MissingModels.bake(this.missingModel, spriteGetter, partCacheImpl);
        ModelBakerImpl modelBakerImpl = new ModelBakerImpl(spriteGetter, partCacheImpl, missingModels);
        CompletableFuture<Map<BlockState, BlockStateModel>> completableFuture = ParallelMapTransform.schedule(this.unbakedBlockStateModels, (blockState, unbakedRoot) -> {
            try {
                return unbakedRoot.bake((BlockState)blockState, modelBakerImpl);
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", blockState, (Object)exception);
                return null;
            }
        }, executor);
        CompletableFuture<Map<Identifier, ItemModel>> completableFuture2 = ParallelMapTransform.schedule(this.clientInfos, (identifier, clientItem) -> {
            try {
                return clientItem.model().bake(new ItemModel.BakingContext(modelBakerImpl, this.entityModelSet, this.materials, this.playerSkinRenderCache, missingModels.item, clientItem.registrySwapper()));
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to bake item model: '{}'", identifier, (Object)exception);
                return null;
            }
        }, executor);
        HashMap map = new HashMap(this.clientInfos.size());
        this.clientInfos.forEach((identifier, clientItem) -> {
            ClientItem.Properties properties = clientItem.properties();
            if (!properties.equals((Object)ClientItem.Properties.DEFAULT)) {
                map.put(identifier, properties);
            }
        });
        return completableFuture.thenCombine(completableFuture2, (map2, map3) -> new BakingResult(missingModels, (Map<BlockState, BlockStateModel>)map2, (Map<Identifier, ItemModel>)map3, map));
    }

    @Environment(value=EnvType.CLIENT)
    static class PartCacheImpl
    implements ModelBaker.PartCache {
        private final Interner<Vector3fc> vectors = Interners.newStrongInterner();

        PartCacheImpl() {
        }

        @Override
        public Vector3fc vector(Vector3fc vector3fc) {
            return (Vector3fc)this.vectors.intern((Object)vector3fc);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class MissingModels
    extends Record {
        final BlockModelPart blockPart;
        private final BlockStateModel block;
        final ItemModel item;

        public MissingModels(BlockModelPart blockModelPart, BlockStateModel blockStateModel, ItemModel itemModel) {
            this.blockPart = blockModelPart;
            this.block = blockStateModel;
            this.item = itemModel;
        }

        public static MissingModels bake(ResolvedModel resolvedModel, final SpriteGetter spriteGetter, final ModelBaker.PartCache partCache) {
            ModelBaker modelBaker = new ModelBaker(){

                @Override
                public ResolvedModel getModel(Identifier identifier) {
                    throw new IllegalStateException("Missing model can't have dependencies, but asked for " + String.valueOf(identifier));
                }

                @Override
                public BlockModelPart missingBlockModelPart() {
                    throw new IllegalStateException();
                }

                @Override
                public <T> T compute(ModelBaker.SharedOperationKey<T> sharedOperationKey) {
                    return sharedOperationKey.compute(this);
                }

                @Override
                public SpriteGetter sprites() {
                    return spriteGetter;
                }

                @Override
                public ModelBaker.PartCache parts() {
                    return partCache;
                }
            };
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            boolean bl = resolvedModel.getTopAmbientOcclusion();
            boolean bl2 = resolvedModel.getTopGuiLight().lightLikeBlock();
            ItemTransforms itemTransforms = resolvedModel.getTopTransforms();
            QuadCollection quadCollection = resolvedModel.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.IDENTITY);
            TextureAtlasSprite textureAtlasSprite = resolvedModel.resolveParticleSprite(textureSlots, modelBaker);
            SimpleModelWrapper simpleModelWrapper = new SimpleModelWrapper(quadCollection, bl, textureAtlasSprite);
            SingleVariant blockStateModel = new SingleVariant(simpleModelWrapper);
            MissingItemModel itemModel = new MissingItemModel(quadCollection.getAll(), new ModelRenderProperties(bl2, textureAtlasSprite, itemTransforms));
            return new MissingModels(simpleModelWrapper, blockStateModel, itemModel);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MissingModels.class, "blockPart;block;item", "blockPart", "block", "item"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MissingModels.class, "blockPart;block;item", "blockPart", "block", "item"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MissingModels.class, "blockPart;block;item", "blockPart", "block", "item"}, this, object);
        }

        public BlockModelPart blockPart() {
            return this.blockPart;
        }

        public BlockStateModel block() {
            return this.block;
        }

        public ItemModel item() {
            return this.item;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ModelBakerImpl
    implements ModelBaker {
        private final SpriteGetter sprites;
        private final ModelBaker.PartCache parts;
        private final MissingModels missingModels;
        private final Map<ModelBaker.SharedOperationKey<Object>, Object> operationCache = new ConcurrentHashMap<ModelBaker.SharedOperationKey<Object>, Object>();
        private final Function<ModelBaker.SharedOperationKey<Object>, Object> cacheComputeFunction = sharedOperationKey -> sharedOperationKey.compute(this);

        ModelBakerImpl(SpriteGetter spriteGetter, ModelBaker.PartCache partCache, MissingModels missingModels) {
            this.sprites = spriteGetter;
            this.parts = partCache;
            this.missingModels = missingModels;
        }

        @Override
        public BlockModelPart missingBlockModelPart() {
            return this.missingModels.blockPart;
        }

        @Override
        public SpriteGetter sprites() {
            return this.sprites;
        }

        @Override
        public ModelBaker.PartCache parts() {
            return this.parts;
        }

        @Override
        public ResolvedModel getModel(Identifier identifier) {
            ResolvedModel resolvedModel = ModelBakery.this.resolvedModels.get(identifier);
            if (resolvedModel == null) {
                LOGGER.warn("Requested a model that was not discovered previously: {}", (Object)identifier);
                return ModelBakery.this.missingModel;
            }
            return resolvedModel;
        }

        @Override
        public <T> T compute(ModelBaker.SharedOperationKey<T> sharedOperationKey) {
            return (T)this.operationCache.computeIfAbsent(sharedOperationKey, this.cacheComputeFunction);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record BakingResult(MissingModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<Identifier, ItemModel> itemStackModels, Map<Identifier, ClientItem.Properties> itemProperties) {
    }
}

