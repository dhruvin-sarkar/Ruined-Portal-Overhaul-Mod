/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.ItemModelPropertyTest;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ConditionalItemModel
implements ItemModel {
    private final ItemModelPropertyTest property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionalItemModel(ItemModelPropertyTest itemModelPropertyTest, ItemModel itemModel, ItemModel itemModel2) {
        this.property = itemModelPropertyTest;
        this.onTrue = itemModel;
        this.onFalse = itemModel2;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        itemStackRenderState.appendModelIdentityElement(this);
        (this.property.get(itemStack, clientLevel, itemOwner == null ? null : itemOwner.asLivingEntity(), i, itemDisplayContext) ? this.onTrue : this.onFalse).update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(ConditionalItemModelProperty property, ItemModel.Unbaked onTrue, ItemModel.Unbaked onFalse) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ConditionalItemModelProperties.MAP_CODEC.forGetter(Unbaked::property), (App)ItemModels.CODEC.fieldOf("on_true").forGetter(Unbaked::onTrue), (App)ItemModels.CODEC.fieldOf("on_false").forGetter(Unbaked::onFalse)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            return new ConditionalItemModel(this.adaptProperty(this.property, bakingContext.contextSwapper()), this.onTrue.bake(bakingContext), this.onFalse.bake(bakingContext));
        }

        private ItemModelPropertyTest adaptProperty(ConditionalItemModelProperty conditionalItemModelProperty, @Nullable RegistryContextSwapper registryContextSwapper) {
            if (registryContextSwapper == null) {
                return conditionalItemModelProperty;
            }
            CacheSlot<ClientLevel, ItemModelPropertyTest> cacheSlot = new CacheSlot<ClientLevel, ItemModelPropertyTest>(clientLevel -> Unbaked.swapContext(conditionalItemModelProperty, registryContextSwapper, clientLevel));
            return (itemStack, clientLevel, livingEntity, i, itemDisplayContext) -> {
                ConditionalItemModelProperty itemModelPropertyTest = clientLevel == null ? conditionalItemModelProperty : (ItemModelPropertyTest)cacheSlot.compute(clientLevel);
                return itemModelPropertyTest.get(itemStack, clientLevel, livingEntity, i, itemDisplayContext);
            };
        }

        private static <T extends ConditionalItemModelProperty> T swapContext(T conditionalItemModelProperty, RegistryContextSwapper registryContextSwapper, ClientLevel clientLevel) {
            return registryContextSwapper.swapTo(conditionalItemModelProperty.type().codec(), conditionalItemModelProperty, clientLevel.registryAccess()).result().orElse(conditionalItemModelProperty);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.onTrue.resolveDependencies(resolver);
            this.onFalse.resolveDependencies(resolver);
        }
    }
}

