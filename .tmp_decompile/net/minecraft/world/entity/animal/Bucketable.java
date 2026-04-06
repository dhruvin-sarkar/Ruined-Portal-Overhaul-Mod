/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public interface Bucketable {
    public boolean fromBucket();

    public void setFromBucket(boolean var1);

    public void saveToBucketTag(ItemStack var1);

    public void loadFromBucketTag(CompoundTag var1);

    public ItemStack getBucketItemStack();

    public SoundEvent getPickupSound();

    @Deprecated
    public static void saveDefaultDataToBucketTag(Mob mob, ItemStack itemStack) {
        itemStack.copyFrom(DataComponents.CUSTOM_NAME, mob);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> {
            if (mob.isNoAi()) {
                compoundTag.putBoolean("NoAI", mob.isNoAi());
            }
            if (mob.isSilent()) {
                compoundTag.putBoolean("Silent", mob.isSilent());
            }
            if (mob.isNoGravity()) {
                compoundTag.putBoolean("NoGravity", mob.isNoGravity());
            }
            if (mob.hasGlowingTag()) {
                compoundTag.putBoolean("Glowing", mob.hasGlowingTag());
            }
            if (mob.isInvulnerable()) {
                compoundTag.putBoolean("Invulnerable", mob.isInvulnerable());
            }
            compoundTag.putFloat("Health", mob.getHealth());
        });
    }

    @Deprecated
    public static void loadDefaultDataFromBucketTag(Mob mob, CompoundTag compoundTag) {
        compoundTag.getBoolean("NoAI").ifPresent(mob::setNoAi);
        compoundTag.getBoolean("Silent").ifPresent(mob::setSilent);
        compoundTag.getBoolean("NoGravity").ifPresent(mob::setNoGravity);
        compoundTag.getBoolean("Glowing").ifPresent(mob::setGlowingTag);
        compoundTag.getBoolean("Invulnerable").ifPresent(mob::setInvulnerable);
        compoundTag.getFloat("Health").ifPresent(mob::setHealth);
    }

    public static <T extends LivingEntity> Optional<InteractionResult> bucketMobPickup(Player player, InteractionHand interactionHand, T livingEntity) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getItem() == Items.WATER_BUCKET && livingEntity.isAlive()) {
            livingEntity.playSound(((Bucketable)((Object)livingEntity)).getPickupSound(), 1.0f, 1.0f);
            ItemStack itemStack2 = ((Bucketable)((Object)livingEntity)).getBucketItemStack();
            ((Bucketable)((Object)livingEntity)).saveToBucketTag(itemStack2);
            ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2, false);
            player.setItemInHand(interactionHand, itemStack3);
            Level level = livingEntity.level();
            if (!level.isClientSide()) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemStack2);
            }
            livingEntity.discard();
            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.empty();
    }
}

