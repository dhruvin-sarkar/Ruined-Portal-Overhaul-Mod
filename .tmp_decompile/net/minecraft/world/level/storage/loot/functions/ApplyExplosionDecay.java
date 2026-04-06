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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay
extends LootItemConditionalFunction {
    public static final MapCodec<ApplyExplosionDecay> CODEC = RecordCodecBuilder.mapCodec(instance -> ApplyExplosionDecay.commonFields(instance).apply((Applicative)instance, ApplyExplosionDecay::new));

    private ApplyExplosionDecay(List<LootItemCondition> list) {
        super(list);
    }

    public LootItemFunctionType<ApplyExplosionDecay> getType() {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Float float_ = lootContext.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        if (float_ != null) {
            RandomSource randomSource = lootContext.getRandom();
            float f = 1.0f / float_.floatValue();
            int i = itemStack.getCount();
            int j = 0;
            for (int k = 0; k < i; ++k) {
                if (!(randomSource.nextFloat() <= f)) continue;
                ++j;
            }
            itemStack.setCount(j);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay() {
        return ApplyExplosionDecay.simpleBuilder(ApplyExplosionDecay::new);
    }
}

