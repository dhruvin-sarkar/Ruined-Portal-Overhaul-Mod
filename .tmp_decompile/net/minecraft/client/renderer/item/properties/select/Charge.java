/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Charge() implements SelectItemModelProperty<CrossbowItem.ChargeType>
{
    public static final Codec<CrossbowItem.ChargeType> VALUE_CODEC = CrossbowItem.ChargeType.CODEC;
    public static final SelectItemModelProperty.Type<Charge, CrossbowItem.ChargeType> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit((Object)new Charge()), VALUE_CODEC);

    @Override
    public CrossbowItem.ChargeType get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) {
            return CrossbowItem.ChargeType.NONE;
        }
        if (chargedProjectiles.contains(Items.FIREWORK_ROCKET)) {
            return CrossbowItem.ChargeType.ROCKET;
        }
        return CrossbowItem.ChargeType.ARROW;
    }

    @Override
    public SelectItemModelProperty.Type<Charge, CrossbowItem.ChargeType> type() {
        return TYPE;
    }

    @Override
    public Codec<CrossbowItem.ChargeType> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, i, itemDisplayContext);
    }
}

