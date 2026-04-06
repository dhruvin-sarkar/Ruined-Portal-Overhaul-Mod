/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SelectItemModel<T>
implements ItemModel {
    private final SelectItemModelProperty<T> property;
    private final ModelSelector<T> models;

    public SelectItemModel(SelectItemModelProperty<T> selectItemModelProperty, ModelSelector<T> modelSelector) {
        this.property = selectItemModelProperty;
        this.models = modelSelector;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        itemStackRenderState.appendModelIdentityElement(this);
        T object = this.property.get(itemStack, clientLevel, itemOwner == null ? null : itemOwner.asLivingEntity(), i, itemDisplayContext);
        ItemModel itemModel = this.models.get(object, clientLevel);
        if (itemModel != null) {
            itemModel.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ModelSelector<T> {
        public @Nullable ItemModel get(@Nullable T var1, @Nullable ClientLevel var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class SwitchCase<T>
    extends Record {
        final List<T> values;
        final ItemModel.Unbaked model;

        public SwitchCase(List<T> list, ItemModel.Unbaked unbaked) {
            this.values = list;
            this.model = unbaked;
        }

        public static <T> Codec<SwitchCase<T>> codec(Codec<T> codec) {
            return RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(codec)).fieldOf("when").forGetter(SwitchCase::values), (App)ItemModels.CODEC.fieldOf("model").forGetter(SwitchCase::model)).apply((Applicative)instance, SwitchCase::new));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SwitchCase.class, "values;model", "values", "model"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SwitchCase.class, "values;model", "values", "model"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SwitchCase.class, "values;model", "values", "model"}, this, object);
        }

        public List<T> values() {
            return this.values;
        }

        public ItemModel.Unbaked model() {
            return this.model;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record UnbakedSwitch<P extends SelectItemModelProperty<T>, T>(P property, List<SwitchCase<T>> cases) {
        public static final MapCodec<UnbakedSwitch<?, ?>> MAP_CODEC = SelectItemModelProperties.CODEC.dispatchMap("property", unbakedSwitch -> unbakedSwitch.property().type(), SelectItemModelProperty.Type::switchCodec);

        public ItemModel bake(ItemModel.BakingContext bakingContext, ItemModel itemModel) {
            Object2ObjectOpenHashMap object2ObjectMap = new Object2ObjectOpenHashMap();
            for (SwitchCase<T> switchCase : this.cases) {
                ItemModel.Unbaked unbaked = switchCase.model;
                ItemModel itemModel2 = unbaked.bake(bakingContext);
                for (Object object : switchCase.values) {
                    object2ObjectMap.put(object, (Object)itemModel2);
                }
            }
            object2ObjectMap.defaultReturnValue((Object)itemModel);
            return new SelectItemModel<T>(this.property, this.createModelGetter((Object2ObjectMap<T, ItemModel>)object2ObjectMap, bakingContext.contextSwapper()));
        }

        private ModelSelector<T> createModelGetter(Object2ObjectMap<T, ItemModel> object2ObjectMap, @Nullable RegistryContextSwapper registryContextSwapper) {
            if (registryContextSwapper == null) {
                return (object, clientLevel) -> (ItemModel)object2ObjectMap.get(object);
            }
            ItemModel itemModel = (ItemModel)object2ObjectMap.defaultReturnValue();
            CacheSlot<ClientLevel, Object2ObjectMap> cacheSlot = new CacheSlot<ClientLevel, Object2ObjectMap>(clientLevel -> {
                Object2ObjectOpenHashMap object2ObjectMap2 = new Object2ObjectOpenHashMap(object2ObjectMap.size());
                object2ObjectMap2.defaultReturnValue((Object)itemModel);
                object2ObjectMap.forEach((arg_0, arg_1) -> this.method_67280(registryContextSwapper, clientLevel, (Object2ObjectMap)object2ObjectMap2, arg_0, arg_1));
                return object2ObjectMap2;
            });
            return (object, clientLevel) -> {
                if (clientLevel == null) {
                    return (ItemModel)object2ObjectMap.get(object);
                }
                if (object == null) {
                    return itemModel;
                }
                return (ItemModel)((Object2ObjectMap)cacheSlot.compute(clientLevel)).get(object);
            };
        }

        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            for (SwitchCase<T> switchCase : this.cases) {
                switchCase.model.resolveDependencies(resolver);
            }
        }

        private /* synthetic */ void method_67280(RegistryContextSwapper registryContextSwapper, ClientLevel clientLevel, Object2ObjectMap object2ObjectMap, Object object2, ItemModel itemModel) {
            registryContextSwapper.swapTo(this.property.valueCodec(), object2, clientLevel.registryAccess()).ifSuccess(object -> object2ObjectMap.put(object, (Object)itemModel));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(UnbakedSwitch<?, ?> unbakedSwitch, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)UnbakedSwitch.MAP_CODEC.forGetter(Unbaked::unbakedSwitch), (App)ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            ItemModel itemModel = this.fallback.map(unbaked -> unbaked.bake(bakingContext)).orElse(bakingContext.missingItemModel());
            return this.unbakedSwitch.bake(bakingContext, itemModel);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.unbakedSwitch.resolveDependencies(resolver);
            this.fallback.ifPresent(unbaked -> unbaked.resolveDependencies(resolver));
        }
    }
}

