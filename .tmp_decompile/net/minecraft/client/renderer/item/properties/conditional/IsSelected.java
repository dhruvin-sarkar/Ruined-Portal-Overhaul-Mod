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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record IsSelected() implements ConditionalItemModelProperty
{
    public static final MapCodec<IsSelected> MAP_CODEC = MapCodec.unit((Object)new IsSelected());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        LocalPlayer localPlayer;
        return livingEntity instanceof LocalPlayer && (localPlayer = (LocalPlayer)livingEntity).getInventory().getSelectedItem() == itemStack;
    }

    public MapCodec<IsSelected> type() {
        return MAP_CODEC;
    }
}

