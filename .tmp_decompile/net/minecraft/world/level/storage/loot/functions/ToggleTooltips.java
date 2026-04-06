/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips
extends LootItemConditionalFunction {
    public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec(instance -> ToggleTooltips.commonFields(instance).and((App)Codec.unboundedMap(DataComponentType.CODEC, (Codec)Codec.BOOL).fieldOf("toggles").forGetter(toggleTooltips -> toggleTooltips.values)).apply((Applicative)instance, ToggleTooltips::new));
    private final Map<DataComponentType<?>, Boolean> values;

    private ToggleTooltips(List<LootItemCondition> list, Map<DataComponentType<?>, Boolean> map) {
        super(list);
        this.values = map;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, tooltipDisplay -> {
            Iterator<Map.Entry<DataComponentType<?>, Boolean>> iterator = this.values.entrySet().iterator();
            while (iterator.hasNext()) {
                boolean bl;
                Map.Entry<DataComponentType<?>, Boolean> entry;
                tooltipDisplay = tooltipDisplay.withHidden(entry.getKey(), !(bl = (entry = iterator.next()).getValue().booleanValue()));
            }
            return tooltipDisplay;
        });
        return itemStack;
    }

    public LootItemFunctionType<ToggleTooltips> getType() {
        return LootItemFunctions.TOGGLE_TOOLTIPS;
    }
}

