/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public interface CrossbowAttackMob
extends RangedAttackMob {
    public void setChargingCrossbow(boolean var1);

    public @Nullable LivingEntity getTarget();

    public void onCrossbowAttackPerformed();

    default public void performCrossbowAttack(LivingEntity livingEntity, float f) {
        InteractionHand interactionHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
        ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (item instanceof CrossbowItem) {
            CrossbowItem crossbowItem = (CrossbowItem)item;
            crossbowItem.performShooting(livingEntity.level(), livingEntity, interactionHand, itemStack, f, 14 - livingEntity.level().getDifficulty().getId() * 4, this.getTarget());
        }
        this.onCrossbowAttackPerformed();
    }
}

