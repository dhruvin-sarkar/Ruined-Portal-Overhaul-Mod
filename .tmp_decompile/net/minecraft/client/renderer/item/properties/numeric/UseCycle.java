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
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record UseCycle(float period) implements RangeSelectItemModelProperty
{
    public static final MapCodec<UseCycle> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("period", (Object)Float.valueOf(1.0f)).forGetter(UseCycle::period)).apply((Applicative)instance, UseCycle::new));

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        LivingEntity livingEntity;
        LivingEntity livingEntity2 = livingEntity = itemOwner == null ? null : itemOwner.asLivingEntity();
        if (livingEntity == null || livingEntity.getUseItem() != itemStack) {
            return 0.0f;
        }
        return (float)livingEntity.getUseItemRemainingTicks() % this.period;
    }

    public MapCodec<UseCycle> type() {
        return MAP_CODEC;
    }
}

