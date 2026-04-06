/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record IsUsingItem() implements ConditionalItemModelProperty
{
    public static final MapCodec<IsUsingItem> MAP_CODEC = MapCodec.unit((Object)new IsUsingItem());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        if (livingEntity == null) {
            return false;
        }
        return livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack;
    }

    public MapCodec<IsUsingItem> type() {
        return MAP_CODEC;
    }
}

