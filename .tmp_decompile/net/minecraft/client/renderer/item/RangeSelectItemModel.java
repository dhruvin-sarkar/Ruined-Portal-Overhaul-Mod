/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
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
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RangeSelectItemModel
implements ItemModel {
    private static final int LINEAR_SEARCH_THRESHOLD = 16;
    private final RangeSelectItemModelProperty property;
    private final float scale;
    private final float[] thresholds;
    private final ItemModel[] models;
    private final ItemModel fallback;

    RangeSelectItemModel(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, float[] fs, ItemModel[] itemModels, ItemModel itemModel) {
        this.property = rangeSelectItemModelProperty;
        this.thresholds = fs;
        this.models = itemModels;
        this.fallback = itemModel;
        this.scale = f;
    }

    private static int lastIndexLessOrEqual(float[] fs, float f) {
        if (fs.length < 16) {
            for (int i = 0; i < fs.length; ++i) {
                if (!(fs[i] > f)) continue;
                return i - 1;
            }
            return fs.length - 1;
        }
        int i = Arrays.binarySearch(fs, f);
        if (i < 0) {
            int j = ~i;
            return j - 1;
        }
        return i;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        int j;
        itemStackRenderState.appendModelIdentityElement(this);
        float f = this.property.get(itemStack, clientLevel, itemOwner, i) * this.scale;
        ItemModel itemModel = Float.isNaN(f) ? this.fallback : ((j = RangeSelectItemModel.lastIndexLessOrEqual(this.thresholds, f)) == -1 ? this.fallback : this.models[j]);
        itemModel.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Entry
    extends Record {
        final float threshold;
        final ItemModel.Unbaked model;
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("threshold").forGetter(Entry::threshold), (App)ItemModels.CODEC.fieldOf("model").forGetter(Entry::model)).apply((Applicative)instance, Entry::new));
        public static final Comparator<Entry> BY_THRESHOLD = Comparator.comparingDouble(Entry::threshold);

        public Entry(float f, ItemModel.Unbaked unbaked) {
            this.threshold = f;
            this.model = unbaked;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this, object);
        }

        public float threshold() {
            return this.threshold;
        }

        public ItemModel.Unbaked model() {
            return this.model;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(RangeSelectItemModelProperty property, float scale, List<Entry> entries, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RangeSelectItemModelProperties.MAP_CODEC.forGetter(Unbaked::property), (App)Codec.FLOAT.optionalFieldOf("scale", (Object)Float.valueOf(1.0f)).forGetter(Unbaked::scale), (App)Entry.CODEC.listOf().fieldOf("entries").forGetter(Unbaked::entries), (App)ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            float[] fs = new float[this.entries.size()];
            ItemModel[] itemModels = new ItemModel[this.entries.size()];
            ArrayList<Entry> list = new ArrayList<Entry>(this.entries);
            list.sort(Entry.BY_THRESHOLD);
            for (int i = 0; i < list.size(); ++i) {
                Entry entry = (Entry)((Object)list.get(i));
                fs[i] = entry.threshold;
                itemModels[i] = entry.model.bake(bakingContext);
            }
            ItemModel itemModel = this.fallback.map(unbaked -> unbaked.bake(bakingContext)).orElse(bakingContext.missingItemModel());
            return new RangeSelectItemModel(this.property, this.scale, fs, itemModels, itemModel);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.ifPresent(unbaked -> unbaked.resolveDependencies(resolver));
            this.entries.forEach(entry -> entry.model.resolveDependencies(resolver));
        }
    }
}

