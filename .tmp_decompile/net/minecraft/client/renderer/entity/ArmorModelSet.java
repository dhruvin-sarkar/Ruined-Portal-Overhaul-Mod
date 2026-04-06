/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;

@Environment(value=EnvType.CLIENT)
public record ArmorModelSet<T>(T head, T chest, T legs, T feet) {
    public T get(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case EquipmentSlot.HEAD -> this.head;
            case EquipmentSlot.CHEST -> this.chest;
            case EquipmentSlot.LEGS -> this.legs;
            case EquipmentSlot.FEET -> this.feet;
            default -> throw new IllegalStateException("No model for slot: " + String.valueOf(equipmentSlot));
        };
    }

    public <U> ArmorModelSet<U> map(Function<? super T, ? extends U> function) {
        return new ArmorModelSet<U>(function.apply(this.head), function.apply(this.chest), function.apply(this.legs), function.apply(this.feet));
    }

    public void putFrom(ArmorModelSet<LayerDefinition> armorModelSet, ImmutableMap.Builder<T, LayerDefinition> builder) {
        builder.put(this.head, (Object)((LayerDefinition)armorModelSet.head));
        builder.put(this.chest, (Object)((LayerDefinition)armorModelSet.chest));
        builder.put(this.legs, (Object)((LayerDefinition)armorModelSet.legs));
        builder.put(this.feet, (Object)((LayerDefinition)armorModelSet.feet));
    }

    public static <M extends HumanoidModel<?>> ArmorModelSet<M> bake(ArmorModelSet<ModelLayerLocation> armorModelSet, EntityModelSet entityModelSet, Function<ModelPart, M> function) {
        return armorModelSet.map(modelLayerLocation -> (HumanoidModel)function.apply(entityModelSet.bakeLayer((ModelLayerLocation)((Object)modelLayerLocation))));
    }
}

