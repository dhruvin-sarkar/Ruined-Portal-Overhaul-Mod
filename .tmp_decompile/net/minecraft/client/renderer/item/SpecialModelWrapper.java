/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpecialModelWrapper<T>
implements ItemModel {
    private final SpecialModelRenderer<T> specialRenderer;
    private final ModelRenderProperties properties;
    private final Supplier<Vector3fc[]> extents;

    public SpecialModelWrapper(SpecialModelRenderer<T> specialModelRenderer, ModelRenderProperties modelRenderProperties) {
        this.specialRenderer = specialModelRenderer;
        this.properties = modelRenderProperties;
        this.extents = Suppliers.memoize(() -> {
            HashSet set = new HashSet();
            specialModelRenderer.getExtents(set::add);
            return set.toArray(new Vector3fc[0]);
        });
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        itemStackRenderState.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = itemStackRenderState.newLayer();
        if (itemStack.hasFoil()) {
            ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.STANDARD;
            layerRenderState.setFoilType(foilType);
            itemStackRenderState.setAnimated();
            itemStackRenderState.appendModelIdentityElement((Object)foilType);
        }
        T object = this.specialRenderer.extractArgument(itemStack);
        layerRenderState.setExtents(this.extents);
        layerRenderState.setupSpecialModel(this.specialRenderer, object);
        if (object != null) {
            itemStackRenderState.appendModelIdentityElement(object);
        }
        this.properties.applyToLayer(layerRenderState, itemDisplayContext);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier base, SpecialModelRenderer.Unbaked specialModel) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base), (App)SpecialModelRenderers.CODEC.fieldOf("model").forGetter(Unbaked::specialModel)).apply((Applicative)instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            SpecialModelRenderer<?> specialModelRenderer = this.specialModel.bake(bakingContext);
            if (specialModelRenderer == null) {
                return bakingContext.missingItemModel();
            }
            ModelRenderProperties modelRenderProperties = this.getProperties(bakingContext);
            return new SpecialModelWrapper(specialModelRenderer, modelRenderProperties);
        }

        private ModelRenderProperties getProperties(ItemModel.BakingContext bakingContext) {
            ModelBaker modelBaker = bakingContext.blockModelBaker();
            ResolvedModel resolvedModel = modelBaker.getModel(this.base);
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            return ModelRenderProperties.fromResolvedModel(modelBaker, resolvedModel, textureSlots);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

