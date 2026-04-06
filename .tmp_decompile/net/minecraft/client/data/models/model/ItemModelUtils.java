/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data.models.model;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.ContextDimension;
import net.minecraft.client.renderer.item.properties.select.ItemBlockState;
import net.minecraft.client.renderer.item.properties.select.LocalTime;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.SpecialDates;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(value=EnvType.CLIENT)
public class ItemModelUtils {
    public static ItemModel.Unbaked plainModel(Identifier identifier) {
        return new BlockModelWrapper.Unbaked(identifier, List.of());
    }

    public static ItemModel.Unbaked tintedModel(Identifier identifier, ItemTintSource ... itemTintSources) {
        return new BlockModelWrapper.Unbaked(identifier, List.of((Object[])itemTintSources));
    }

    public static ItemTintSource constantTint(int i) {
        return new Constant(i);
    }

    public static ItemModel.Unbaked composite(ItemModel.Unbaked ... unbakeds) {
        return new CompositeModel.Unbaked(List.of((Object[])unbakeds));
    }

    public static ItemModel.Unbaked specialModel(Identifier identifier, SpecialModelRenderer.Unbaked unbaked) {
        return new SpecialModelWrapper.Unbaked(identifier, unbaked);
    }

    public static RangeSelectItemModel.Entry override(ItemModel.Unbaked unbaked, float f) {
        return new RangeSelectItemModel.Entry(f, unbaked);
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, ItemModel.Unbaked unbaked, RangeSelectItemModel.Entry ... entrys) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, List.of((Object[])entrys), Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, ItemModel.Unbaked unbaked, RangeSelectItemModel.Entry ... entrys) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, f, List.of((Object[])entrys), Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, ItemModel.Unbaked unbaked, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, list, Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, list, Optional.empty());
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, f, list, Optional.empty());
    }

    public static ItemModel.Unbaked conditional(ConditionalItemModelProperty conditionalItemModelProperty, ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        return new ConditionalItemModel.Unbaked(conditionalItemModelProperty, unbaked, unbaked2);
    }

    public static <T> SelectItemModel.SwitchCase<T> when(T object, ItemModel.Unbaked unbaked) {
        return new SelectItemModel.SwitchCase(List.of(object), unbaked);
    }

    public static <T> SelectItemModel.SwitchCase<T> when(List<T> list, ItemModel.Unbaked unbaked) {
        return new SelectItemModel.SwitchCase<T>(list, unbaked);
    }

    @SafeVarargs
    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, ItemModel.Unbaked unbaked, SelectItemModel.SwitchCase<T> ... switchCases) {
        return ItemModelUtils.select(selectItemModelProperty, unbaked, List.of(switchCases));
    }

    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, ItemModel.Unbaked unbaked, List<SelectItemModel.SwitchCase<T>> list) {
        return new SelectItemModel.Unbaked(new SelectItemModel.UnbakedSwitch<SelectItemModelProperty<T>, T>(selectItemModelProperty, list), Optional.of(unbaked));
    }

    @SafeVarargs
    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, SelectItemModel.SwitchCase<T> ... switchCases) {
        return ItemModelUtils.select(selectItemModelProperty, List.of(switchCases));
    }

    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, List<SelectItemModel.SwitchCase<T>> list) {
        return new SelectItemModel.Unbaked(new SelectItemModel.UnbakedSwitch<SelectItemModelProperty<T>, T>(selectItemModelProperty, list), Optional.empty());
    }

    public static ConditionalItemModelProperty isUsingItem() {
        return new IsUsingItem();
    }

    public static ConditionalItemModelProperty hasComponent(DataComponentType<?> dataComponentType) {
        return new HasComponent(dataComponentType, false);
    }

    public static ItemModel.Unbaked inOverworld(ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        return ItemModelUtils.select(new ContextDimension(), unbaked2, ItemModelUtils.when(Level.OVERWORLD, unbaked));
    }

    public static <T extends Comparable<T>> ItemModel.Unbaked selectBlockItemProperty(Property<T> property, ItemModel.Unbaked unbaked, Map<T, ItemModel.Unbaked> map) {
        List list = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            String string = property.getName((Comparable)entry.getKey());
            return new SelectItemModel.SwitchCase(List.of((Object)string), (ItemModel.Unbaked)entry.getValue());
        }).toList();
        return ItemModelUtils.select(new ItemBlockState(property.getName()), unbaked, list);
    }

    public static ItemModel.Unbaked isXmas(ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd", Locale.ROOT);
        List list = SpecialDates.CHRISTMAS_RANGE.stream().map(dateTimeFormatter::format).toList();
        return ItemModelUtils.select(LocalTime.create("MM-dd", "", Optional.empty()), unbaked2, List.of(ItemModelUtils.when(list, unbaked)));
    }
}

