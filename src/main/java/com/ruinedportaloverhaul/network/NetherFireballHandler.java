package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.item.GhastTearNecklaceItem;
import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

        ItemStack stack = GhastTearNecklaceItem.findCarriedNecklace(player);
        if (stack.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.necklace.missing"), true);
            return;
        }

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
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.NETHER_FIREBALL_USED, player);
        player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.necklace.fired"), true);
    }

    private static void spawnFireball(ServerLevel level, ServerPlayer player) {
        // Fix: the necklace fireball effect previously used a raw ghast sound, which kept this player ability outside the mod sound registry. The launch cue now routes through a dedicated item sound id.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.2));
        SmallFireball fireball = new SmallFireball(level, player, look);
        fireball.setOwner(player);
        fireball.setPos(spawnPos.x, spawnPos.y - 0.1, spawnPos.z);
        fireball.setDeltaMovement(look.scale(1.25));
        level.addFreshEntity(fireball);
        level.playSound(null, player.blockPosition(), ModSounds.ITEM_GHAST_TEAR_NECKLACE_FIREBALL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
