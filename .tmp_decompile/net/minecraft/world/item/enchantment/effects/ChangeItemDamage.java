/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record ChangeItemDamage(LevelBasedValue amount) implements EnchantmentEntityEffect
{
    public static final MapCodec<ChangeItemDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LevelBasedValue.CODEC.fieldOf("amount").forGetter(changeItemDamage -> changeItemDamage.amount)).apply((Applicative)instance, ChangeItemDamage::new));

    @Override
    public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        ItemStack itemStack = enchantedItemInUse.itemStack();
        if (itemStack.has(DataComponents.MAX_DAMAGE) && itemStack.has(DataComponents.DAMAGE)) {
            ServerPlayer serverPlayer;
            LivingEntity livingEntity = enchantedItemInUse.owner();
            ServerPlayer serverPlayer2 = livingEntity instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)livingEntity) : null;
            int j = (int)this.amount.calculate(i);
            itemStack.hurtAndBreak(j, serverLevel, serverPlayer2, enchantedItemInUse.onBreak());
        }
    }

    public MapCodec<ChangeItemDamage> codec() {
        return CODEC;
    }
}

