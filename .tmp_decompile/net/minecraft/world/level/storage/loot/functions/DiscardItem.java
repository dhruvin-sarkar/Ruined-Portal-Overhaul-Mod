/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DiscardItem
extends LootItemConditionalFunction {
    public static final MapCodec<DiscardItem> CODEC = RecordCodecBuilder.mapCodec(instance -> DiscardItem.commonFields(instance).apply((Applicative)instance, DiscardItem::new));

    protected DiscardItem(List<LootItemCondition> list) {
        super(list);
    }

    public LootItemFunctionType<DiscardItem> getType() {
        return LootItemFunctions.DISCARD;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        return ItemStack.EMPTY;
    }

    public static LootItemConditionalFunction.Builder<?> discardItem() {
        return DiscardItem.simpleBuilder(DiscardItem::new);
    }
}

