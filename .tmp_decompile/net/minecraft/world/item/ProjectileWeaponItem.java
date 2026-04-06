/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class ProjectileWeaponItem
extends Item {
    public static final Predicate<ItemStack> ARROW_ONLY = itemStack -> itemStack.is(ItemTags.ARROWS);
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(itemStack -> itemStack.is(Items.FIREWORK_ROCKET));

    public ProjectileWeaponItem(Item.Properties properties) {
        super(properties);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return this.getAllSupportedProjectiles();
    }

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public static ItemStack getHeldProjectile(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.OFF_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.MAIN_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return ItemStack.EMPTY;
    }

    public abstract int getDefaultProjectileRange();

    protected void shoot(ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, List<ItemStack> list, float f, float g, boolean bl, @Nullable LivingEntity livingEntity2) {
        float h = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, livingEntity, 0.0f);
        float i = list.size() == 1 ? 0.0f : 2.0f * h / (float)(list.size() - 1);
        float j = (float)((list.size() - 1) % 2) * i / 2.0f;
        float k = 1.0f;
        for (int l = 0; l < list.size(); ++l) {
            ItemStack itemStack2 = list.get(l);
            if (itemStack2.isEmpty()) continue;
            float m = j + k * (float)((l + 1) / 2) * i;
            k = -k;
            int n = l;
            Projectile.spawnProjectile(this.createProjectile(serverLevel, livingEntity, itemStack, itemStack2, bl), serverLevel, itemStack2, projectile -> this.shootProjectile(livingEntity, (Projectile)projectile, n, f, g, m, livingEntity2));
            itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, interactionHand.asEquipmentSlot());
            if (itemStack.isEmpty()) break;
        }
    }

    protected int getDurabilityUse(ItemStack itemStack) {
        return 1;
    }

    protected abstract void shootProjectile(LivingEntity var1, Projectile var2, int var3, float var4, float var5, float var6, @Nullable LivingEntity var7);

    protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        ArrowItem arrowItem;
        Item item = itemStack2.getItem();
        ArrowItem arrowItem2 = item instanceof ArrowItem ? (arrowItem = (ArrowItem)item) : (ArrowItem)Items.ARROW;
        AbstractArrow abstractArrow = arrowItem2.createArrow(level, itemStack2, livingEntity, itemStack);
        if (bl) {
            abstractArrow.setCritArrow(true);
        }
        return abstractArrow;
    }

    protected static List<ItemStack> draw(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity) {
        int n;
        if (itemStack2.isEmpty()) {
            return List.of();
        }
        Level level = livingEntity.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            n = EnchantmentHelper.processProjectileCount(serverLevel, itemStack, livingEntity, 1);
        } else {
            n = 1;
        }
        int i = n;
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(i);
        ItemStack itemStack3 = itemStack2.copy();
        for (int j = 0; j < i; ++j) {
            ItemStack itemStack4 = ProjectileWeaponItem.useAmmo(itemStack, j == 0 ? itemStack2 : itemStack3, livingEntity, j > 0);
            if (itemStack4.isEmpty()) continue;
            list.add(itemStack4);
        }
        return list;
    }

    protected static ItemStack useAmmo(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity, boolean bl) {
        ItemStack itemStack3;
        int i;
        Level level;
        if (!bl && !livingEntity.hasInfiniteMaterials() && (level = livingEntity.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            v0 = EnchantmentHelper.processAmmoUse(serverLevel, itemStack, itemStack2, 1);
        } else {
            v0 = i = 0;
        }
        if (i > itemStack2.getCount()) {
            return ItemStack.EMPTY;
        }
        if (i == 0) {
            itemStack3 = itemStack2.copyWithCount(1);
            itemStack3.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            return itemStack3;
        }
        itemStack3 = itemStack2.split(i);
        if (itemStack2.isEmpty() && livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.getInventory().removeItem(itemStack2);
        }
        return itemStack3;
    }
}

