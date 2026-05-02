package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.item.GhastTearNecklaceItem;
import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class NetherFireballHandler {
    public static final int COOLDOWN_TICKS = 2 * 60 * 20;
    private static final double MIN_LOOK_ALIGNMENT = 0.2;

    private NetherFireballHandler() {
    }

    public static void handle(ServerPlayer player, NetherFireballPayload payload) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack stack = GhastTearNecklaceItem.findCarriedNecklace(player);
        if (stack.isEmpty()) {
            return;
        }

        long now = serverLevel.getGameTime();
        long lastUse = stack.getOrDefault(ModDataComponents.LAST_NECKLACE_FIREBALL_TICK, Long.MIN_VALUE);
        long elapsed = now - lastUse;
        if (lastUse != Long.MIN_VALUE && elapsed < COOLDOWN_TICKS) {
            return;
        }

        spawnFireball(serverLevel, player, validatedLook(payload, player.getLookAngle()));
        stack.set(ModDataComponents.LAST_NECKLACE_FIREBALL_TICK, now);
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.NETHER_FIREBALL_USED, player);
    }

    private static Vec3 validatedLook(NetherFireballPayload payload, Vec3 serverLook) {
        Vec3 fallback = safeNormalize(serverLook);
        Vec3 requestedLook = new Vec3(payload.lookX(), payload.lookY(), payload.lookZ());
        double lengthSquared = requestedLook.lengthSqr();
        if (!Double.isFinite(requestedLook.x)
            || !Double.isFinite(requestedLook.y)
            || !Double.isFinite(requestedLook.z)
            || lengthSquared < 1.0E-6
            || lengthSquared > 4.0) {
            return fallback;
        }
        Vec3 normalizedLook = requestedLook.normalize();
        return normalizedLook.dot(fallback) < MIN_LOOK_ALIGNMENT ? fallback : normalizedLook;
    }

    private static Vec3 safeNormalize(Vec3 look) {
        if (!Double.isFinite(look.x) || !Double.isFinite(look.y) || !Double.isFinite(look.z) || look.lengthSqr() < 1.0E-6) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return look.normalize();
    }

    private static void spawnFireball(ServerLevel level, ServerPlayer player, Vec3 look) {
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.2));
        SmallFireball fireball = new SmallFireball(level, player, look);
        fireball.setOwner(player);
        fireball.setPos(spawnPos.x, spawnPos.y - 0.1, spawnPos.z);
        fireball.setDeltaMovement(look.scale(1.25));
        level.addFreshEntity(fireball);
        level.playSound(null, player.blockPosition(), ModSounds.ITEM_GHAST_TEAR_NECKLACE_FIREBALL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
