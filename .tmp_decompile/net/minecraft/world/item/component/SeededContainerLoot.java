/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.storage.loot.LootTable;

public record SeededContainerLoot(ResourceKey<LootTable> lootTable, long seed) implements TooltipProvider
{
    private static final Component UNKNOWN_CONTENTS = Component.translatable("item.container.loot_table.unknown");
    public static final Codec<SeededContainerLoot> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(SeededContainerLoot::lootTable), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(SeededContainerLoot::seed)).apply((Applicative)instance, SeededContainerLoot::new));

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        consumer.accept(UNKNOWN_CONTENTS);
    }
}

