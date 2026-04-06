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
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record FishingRodCast() implements ConditionalItemModelProperty
{
    public static final MapCodec<FishingRodCast> MAP_CODEC = MapCodec.unit((Object)new FishingRodCast());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (player.fishing != null) {
                HumanoidArm humanoidArm = FishingHookRenderer.getHoldingArm(player);
                return livingEntity.getItemHeldByArm(humanoidArm) == itemStack;
            }
        }
        return false;
    }

    public MapCodec<FishingRodCast> type() {
        return MAP_CODEC;
    }
}

