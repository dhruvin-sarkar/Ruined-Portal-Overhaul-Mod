package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.item.ModItems;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class NetherFireballHandler {
    public static final int COOLDOWN_TICKS = 2 * 60 * 20;

    private NetherFireballHandler() {
    }

    public static void handle(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        SlotEntryReference necklace = AccessoriesCapability.getOptionally(player)
            .map(capability -> capability.getFirstEquipped(ModItems.GHAST_TEAR_NECKLACE))
            .orElse(null);

        if (necklace == null) {
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.necklace.missing"), true);
            return;
        }

        ItemStack stack = necklace.stack();
        long now = serverLevel.getGameTime();
        long lastUse = stack.getOrDefault(ModDataComponents.LAST_NECKLACE_FIREBALL_TICK, Long.MIN_VALUE);
        long elapsed = now - lastUse;
        if (lastUse != Long.MIN_VALUE && elapsed < COOLDOWN_TICKS) {
            long secondsRemaining = Math.max(1L, (COOLDOWN_TICKS - elapsed + 19L) / 20L);
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.necklace.cooldown", secondsRemaining), true);
            return;
        }

        spawnFireball(serverLevel, player);
        stack.set(ModDataComponents.LAST_NECKLACE_FIREBALL_TICK, now);
        player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.necklace.fired"), true);
    }

    private static void spawnFireball(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.2));
        SmallFireball fireball = new SmallFireball(level, player, look);
        fireball.setOwner(player);
        fireball.setPos(spawnPos.x, spawnPos.y - 0.1, spawnPos.z);
        fireball.setDeltaMovement(look.scale(1.25));
        level.addFreshEntity(fireball);
        level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
