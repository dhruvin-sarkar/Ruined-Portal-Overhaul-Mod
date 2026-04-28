package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.world.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class CorruptedNetheriteEvents {
    private static final Identifier TOUGHNESS_MODIFIER_ID = ModItems.id("corrupted_netherite_toughness");
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int EFFECT_DURATION_TICKS = 60;

    private CorruptedNetheriteEvents() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(CorruptedNetheriteEvents::tick);
    }

    private static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                applySetBonuses(level, player, gameTime);
            }
        }
    }

    private static void applySetBonuses(ServerLevel level, ServerPlayer player, long gameTime) {
        int pieces = countCorruptedArmorPieces(player);
        if (pieces >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, EFFECT_DURATION_TICKS, 0, true, false, true));
        }
        if (pieces >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, EFFECT_DURATION_TICKS, 0, true, false, true));
        }

        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) {
            if (pieces >= 4) {
                toughness.addOrUpdateTransientModifier(new AttributeModifier(
                    TOUGHNESS_MODIFIER_ID,
                    4.0,
                    AttributeModifier.Operation.ADD_VALUE
                ));
            } else {
                toughness.removeModifier(TOUGHNESS_MODIFIER_ID);
            }
        }

        if (pieces >= 4 && gameTime % 40 == 0) {
            level.sendParticles(ModParticles.NETHER_EMBER, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.35, 0.8, 0.35, 0.015);
        }
    }

    private static int countCorruptedArmorPieces(ServerPlayer player) {
        int count = 0;
        for (EquipmentSlot slot : CorruptedNetheriteArmorItem.ARMOR_SLOTS) {
            if (CorruptedNetheriteArmorItem.isCorruptedNetherite(player.getItemBySlot(slot))) {
                count++;
            }
        }
        return count;
    }
}
